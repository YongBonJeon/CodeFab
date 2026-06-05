package com.codefab.shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Factory control shell — the operator interface for the interpreter factory. Selects the run mode
 * from the command line:
 *
 * <ul>
 *   <li>(no args) / {@code repl} — prompt (REPL) mode
 *   <li>{@code run <file>} — file mode
 *   <li>{@code debug <file>} — debug mode
 *   <li>{@code <file>} — shorthand for file mode
 * </ul>
 */
public class FactoryShell {

  private final PrintStream out;
  private final PrintStream err;

  public FactoryShell(PrintStream out, PrintStream err) {
    this.out = out;
    this.err = err;
  }

  /** Dispatches to the selected mode and returns a process exit code. */
  public int dispatch(String[] args) throws IOException {
    if (args.length == 0 || args[0].equals("repl")) {
      new PromptShell().run();
      return 0;
    }
    return switch (args[0]) {
      case "run" -> requireFile(args) ? new FileRunner(out, err).run(args[1]) : 64;
      case "debug" -> requireFile(args) ? debug(args[1]) : 64;
      default -> new FileRunner(out, err).run(args[0]); // shorthand: treat arg as a file path
    };
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
    String source = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
    new Debugger(source, in, out).run(source, path);
    return 0;
  }
}
