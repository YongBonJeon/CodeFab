package com.codefab.shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
      new PromptShell().run();
      return 0;
    }
    FileRunner fileRunner = new FileRunner(out, err);
    return switch (args[0]) {
      case "run" -> requireFile(args) ? fileRunner.run(args[1]) : 64;
      case "debug" -> requireFile(args) ? debug(args[1]) : 64;
      default -> fileRunner.run(args[0]);
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
    String source = Files.readString(file, StandardCharsets.UTF_8);
    new Debugger(source, debugIn, out).run(source, path);
    return 0;
  }
}
