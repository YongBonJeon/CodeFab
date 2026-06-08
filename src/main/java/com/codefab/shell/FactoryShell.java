package com.codefab.shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class FactoryShell {

  private final PrintStream out;
  private final PrintStream err;
  private final BufferedReader debugIn;

  public FactoryShell(PrintStream out, PrintStream err) {
    this(out, err, new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)));
  }

  public FactoryShell(PrintStream out, PrintStream err, BufferedReader debugIn) {
    this.out = out;
    this.err = err;
    this.debugIn = debugIn;
  }

  public int dispatch(String[] args) throws IOException {
    if (args.length == 0 || args[0].equals("repl")) {
      return interactiveMenu();
    }
    FileRunner fileRunner = new FileRunner(out, err);
    return switch (args[0]) {
      case "run"   -> requireFile(args) ? fileRunner.run(args[1]) : 64;
      case "debug" -> requireFile(args) ? debug(args[1]) : 64;
      default      -> fileRunner.run(args[0]);
    };
  }

  private int interactiveMenu() throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
    out.println(Ansi.CYAN + Ansi.BOLD
        + "┌─────────────────────────────────────┐" + Ansi.RESET);
    out.println(Ansi.CYAN + Ansi.BOLD
        + "│   CodeFab Interpreter  v1.0         │" + Ansi.RESET);
    out.println(Ansi.CYAN + Ansi.BOLD
        + "├─────────────────────────────────────┤" + Ansi.RESET);
    out.println(Ansi.CYAN + "│" + Ansi.RESET
        + "  " + Ansi.BOLD + "[1]" + Ansi.RESET + " REPL   대화형 모드           "
        + Ansi.CYAN + "│" + Ansi.RESET);
    out.println(Ansi.CYAN + "│" + Ansi.RESET
        + "  " + Ansi.BOLD + "[2]" + Ansi.RESET + " RUN    파일 실행 모드        "
        + Ansi.CYAN + "│" + Ansi.RESET);
    out.println(Ansi.CYAN + "│" + Ansi.RESET
        + "  " + Ansi.BOLD + "[3]" + Ansi.RESET + " DEBUG  디버그 모드           "
        + Ansi.CYAN + "│" + Ansi.RESET);
    out.println(Ansi.CYAN + Ansi.BOLD
        + "└─────────────────────────────────────┘" + Ansi.RESET);
    out.print(Ansi.BOLD + "선택 (1/2/3): " + Ansi.RESET);
    out.flush();

    String choice = reader.readLine();
    if (choice == null) return 0;

    return switch (choice.trim()) {
      case "1" -> { new PromptShell().run(); yield 0; }
      case "2" -> {
        String path = pickFile(reader);
        yield (path != null) ? new FileRunner(out, err).run(path) : 64;
      }
      case "3" -> {
        String path = pickFile(reader);
        yield (path != null) ? debug(path) : 64;
      }
      default -> {
        err.println(Ansi.RED + "잘못된 선택입니다." + Ansi.RESET);
        yield 1;
      }
    };
  }

  private String pickFile(BufferedReader reader) throws IOException {
    Path scriptsDir = Paths.get("scripts");
    List<Path> files = List.of();
    if (Files.isDirectory(scriptsDir)) {
      files = Files.list(scriptsDir)
          .filter(Files::isRegularFile)
          .sorted()
          .collect(Collectors.toList());
    }

    if (!files.isEmpty()) {
      out.println(Ansi.BOLD + "  scripts/ 파일 목록" + Ansi.RESET);
      out.println(Ansi.GRAY + "  ──────────────────────────────" + Ansi.RESET);
      for (int i = 0; i < files.size(); i++) {
        out.println("  " + Ansi.CYAN + "[" + (i + 1) + "]" + Ansi.RESET
            + "  " + files.get(i).getFileName());
      }
      out.println(Ansi.GRAY + "  ──────────────────────────────" + Ansi.RESET);
    }

    out.print(Ansi.BOLD + "  번호 선택 또는 경로 입력: " + Ansi.RESET);
    out.flush();
    String input = reader.readLine();
    if (input == null || input.isBlank()) return null;
    input = input.trim();

    try {
      int idx = Integer.parseInt(input) - 1;
      if (idx >= 0 && idx < files.size()) {
        return files.get(idx).toString();
      }
      err.println(Ansi.RED + "  잘못된 번호입니다." + Ansi.RESET);
      return null;
    } catch (NumberFormatException e) {
      // 숫자가 아니면 경로로 처리
      Path path = Paths.get(input);
      if (!path.isAbsolute() && !input.contains("/") && !input.contains("\\")) {
        path = scriptsDir.resolve(input);
      }
      return path.toString();
    }
  }

  private boolean requireFile(String[] args) {
    if (args.length < 2) {
      err.println("[오류] 파일 경로가 필요합니다. 사용법: factory " + args[0] + " <파일경로>");
      return false;
    }
    return true;
  }

  private int debug(String path) throws IOException {
    Path file = Paths.get(path);
    if (!Files.exists(file)) {
      err.println("[오류] 파일을 찾을 수 없습니다: " + path);
      return 66;
    }
    String source = Files.readString(file, StandardCharsets.UTF_8);
    new Debugger(source, debugIn, out).run(source, path);
    return 0;
  }
}
