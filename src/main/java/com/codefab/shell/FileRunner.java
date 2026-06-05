package com.codefab.shell;

import java.io.PrintStream;

public class FileRunner {

  private final PrintStream out;
  private final PrintStream err;

  public FileRunner(PrintStream out, PrintStream err) {
    this.out = out;
    this.err = err;
  }

  public int run(String path) {
    throw new UnsupportedOperationException("FileRunner 미구현");
  }
}
