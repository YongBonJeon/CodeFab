package com.codefab.shell;

import java.io.IOException;
import java.io.PrintStream;

public class FactoryShell {

  private final PrintStream out;
  private final PrintStream err;

  public FactoryShell(PrintStream out, PrintStream err) {
    this.out = out;
    this.err = err;
  }

  public int dispatch(String[] args) throws IOException {
    FileRunner fileRunner = new FileRunner(out, err);
    if (args[0].equals("run")) {
      if (args.length < 2) {
        err.println("[오류] 파일 경로가 필요합니다. 사용법: factory run <파일경로>");
        return 64;
      }
      return fileRunner.run(args[1]);
    }
    return fileRunner.run(args[0]);
  }
}
