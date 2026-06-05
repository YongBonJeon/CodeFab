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
    if (args[0].equals("run")) {
      return new FileRunner(out, err).run(args[1]);
    }
    return new FileRunner(out, err).run(args[0]);
  }
}
