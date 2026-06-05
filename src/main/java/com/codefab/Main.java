package com.codefab;

import com.codefab.shell.FactoryShell;

import java.io.IOException;

public class Main {

  public static void main(String[] args) throws IOException {
    int exitCode = new FactoryShell(System.out, System.err).dispatch(args);
    if (exitCode != 0) {
      System.exit(exitCode);
    }
  }
}
