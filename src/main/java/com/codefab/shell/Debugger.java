package com.codefab.shell;

import com.codefab.CodeFab;
import com.codefab.ast.Stmt;
import com.codefab.error.CodeFabError;
import com.codefab.executor.ExecutionListener;
import com.codefab.executor.Executor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.TreeSet;

public class Debugger implements ExecutionListener {

  private enum Mode { STEP, CONTINUE }

  private final CodeFab fab;
  private final Executor executor;
  private final BufferedReader in;
  private final PrintStream out;
  private final String[] sourceLines;

  private final TreeSet<Integer> breakpoints = new TreeSet<>();
  private Mode mode = Mode.STEP;

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
    boolean pauseByStep = mode == Mode.STEP;
    if (!pauseByStep && !atBreakpoint) return;

    String marker = (atBreakpoint && !pauseByStep) ? " (breakpoint)" : "";
    out.println("[DEBUG] " + stmt.line + "번째 줄에서 정지" + marker + " → " + sourceText(stmt.line));
    readCommands();
  }

  private void readCommands() {
    while (true) {
      out.print("> ");
      out.flush();
      String line = readLine();
      if (line == null) return;
      String[] parts = line.trim().split("\\s+");
      switch (parts[0]) {
        case "step" -> { mode = Mode.STEP; return; }
        case "continue" -> { mode = Mode.CONTINUE; return; }
        case "break" -> setBreakpoint(parts);
        default -> { }
      }
    }
  }

  private void setBreakpoint(String[] parts) {
    if (parts.length < 2) return;
    int lineNo = Integer.parseInt(parts[1]);
    breakpoints.add(lineNo);
    out.println("[DEBUG] " + lineNo + "번째 줄에 breakpoint 설정");
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
