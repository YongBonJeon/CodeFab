package com.codefab.shell;

import com.codefab.CodeFab;
import com.codefab.ast.Stmt;
import com.codefab.error.CodeFabError;
import com.codefab.executor.ExecutionListener;
import com.codefab.executor.Executor;
import com.codefab.executor.CodeFabCallable;
import com.codefab.executor.Environment;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Debugger implements ExecutionListener {

  private enum Mode { STEP, NEXT, CONTINUE }

  private final CodeFab fab;
  private final Executor executor;
  private final BufferedReader in;
  private final PrintStream out;
  private final String[] sourceLines;

  private final TreeSet<Integer> breakpoints = new TreeSet<>();
  private final Set<String> watches = new LinkedHashSet<>();
  private Mode mode = Mode.STEP;
  private int nextDepthLimit = 0;

  public Debugger(String source, BufferedReader in, PrintStream out) {
    this.fab = new CodeFab(out, out);
    this.executor = fab.getExecutor();
    this.in = in;
    this.out = out;
    this.sourceLines = source.split("\n", -1);
  }

  public void run(String source, String path) {
    out.println("[DEBUG] 소스코드 로딩: " + path);
    List<Stmt> program;
    try {
      program = fab.compile(source);
    } catch (CodeFabError e) {
      out.println("[DEBUG] " + e.formatted());
      return;
    }
    executor.setListener(this);
    try {
      fab.execute(program);
      out.println("[DEBUG] 실행 종료");
    } catch (CodeFabError e) {
      out.println("[DEBUG] " + e.formatted());
    }
  }

  @Override
  public void beforeStatement(Stmt stmt, int depth) {
    boolean atBreakpoint = breakpoints.contains(stmt.line);
    boolean pauseByStep = mode == Mode.STEP || (mode == Mode.NEXT && depth <= nextDepthLimit);
    if (!pauseByStep && !atBreakpoint) return;

    String marker = (atBreakpoint && !pauseByStep) ? " (breakpoint)" : "";
    out.println("[DEBUG] " + stmt.line + "번째 줄에서 정지" + marker + " → " + sourceText(stmt.line));
    printWatches();
    readCommands(depth);
  }

  private void readCommands(int depth) {
    while (true) {
      out.print("> ");
      out.flush();
      String line = readLine();
      if (line == null) return;
      String[] parts = line.trim().split("\\s+");
      switch (parts[0]) {
        case "step" -> { mode = Mode.STEP; return; }
        case "next" -> { mode = Mode.NEXT; nextDepthLimit = depth; return; }
        case "continue" -> { mode = Mode.CONTINUE; return; }
        case "break" -> setBreakpoint(parts);
        case "remove" -> removeBreakpoint(parts);
        case "breakpoints" -> listBreakpoints();
        case "watch" -> addWatch(parts);
        case "inspect" -> inspect();
        default -> { }
      }
    }
  }

  private void inspect() {
    out.println("──── 현재 스코프 변수 ────");
    Map<String, Object> seen = new LinkedHashMap<>();
    for (Environment env = executor.currentEnvironment(); env != null; env = env.enclosing()) {
      boolean isGlobal = env.enclosing() == null;
      String label = isGlobal ? "[전역]" : "[로컬]";
      for (Map.Entry<String, Object> entry : env.values().entrySet()) {
        if (entry.getValue() instanceof CodeFabCallable) continue;
        if (seen.containsKey(entry.getKey())) continue;
        seen.put(entry.getKey(), entry.getValue());
        out.println(label + " " + entry.getKey() + " = " + stringify(entry.getValue()));
      }
    }
  }

  private void addWatch(String[] parts) {
    if (parts.length < 2) return;
    watches.add(parts[1]);
    out.println("[WATCH] '" + parts[1] + "' 감시 등록");
  }

  private void printWatches() {
    for (String name : watches) {
      Environment env = findDefiningScope(name);
      if (env == null) {
        out.println("[WATCH] " + name + " = (정의되지 않음)");
      } else {
        out.println("[WATCH] " + name + " = " + stringify(env.values().get(name)));
      }
    }
  }

  private Environment findDefiningScope(String name) {
    for (Environment env = executor.currentEnvironment(); env != null; env = env.enclosing()) {
      if (env.containsLocally(name)) return env;
    }
    return null;
  }

  private String stringify(Object value) {
    if (value == null) return "null";
    if (value instanceof Double d) {
      String text = d.toString();
      return text.endsWith(".0") ? text.substring(0, text.length() - 2) : text;
    }
    return value.toString();
  }

  private void listBreakpoints() {
    if (breakpoints.isEmpty()) {
      out.println("[DEBUG] 설정된 breakpoint 가 없습니다.");
    } else {
      out.println("[DEBUG] breakpoints: " + breakpoints);
    }
  }

  private void removeBreakpoint(String[] parts) {
    Integer lineNo = parseLine(parts);
    if (lineNo == null) return;
    if (breakpoints.remove(lineNo)) {
      out.println("[DEBUG] " + lineNo + "번째 줄 breakpoint 해제");
    } else {
      out.println("[DEBUG] " + lineNo + "번째 줄에 breakpoint 가 없습니다.");
    }
  }

  private void setBreakpoint(String[] parts) {
    Integer lineNo = parseLine(parts);
    if (lineNo == null) return;
    breakpoints.add(lineNo);
    out.println("[DEBUG] " + lineNo + "번째 줄에 breakpoint 설정");
  }

  private Integer parseLine(String[] parts) {
    if (parts.length < 2) return null;
    try {
      return Integer.parseInt(parts[1]);
    } catch (NumberFormatException e) {
      out.println("[DEBUG] 잘못된 줄 번호: " + parts[1]);
      return null;
    }
  }

  private String sourceText(int line) {
    if (line >= 1 && line <= sourceLines.length) {
      return sourceLines[line - 1].trim();
    }
    return "";
  }

  private String readLine() {
    try {
      return in.readLine();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
