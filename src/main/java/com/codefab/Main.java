package com.codefab;

import com.codefab.shell.PromptShell;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

  public static void main(String[] args) throws IOException {
    if (args.length == 0) {
      new PromptShell().run();
      return;
    }
    String src = new String(Files.readAllBytes(Paths.get(args[0])));
    CodeFab fab = new CodeFab();
    if (!fab.run(src)) {
      System.exit(70);
    }
  }
}
