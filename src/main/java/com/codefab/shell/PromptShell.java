package com.codefab.shell;

import com.codefab.CodeFab;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class PromptShell {

  private final CodeFab fab;

  public PromptShell() {
    this.fab = new CodeFab();
  }

  public PromptShell(CodeFab fab) {
    this.fab = fab;
  }

  public void run() throws IOException {
    BufferedReader reader = new BufferedReader(
        new InputStreamReader(System.in, StandardCharsets.UTF_8));
    System.out.println("CodeFab Interpreter v1.0");
    System.out.println("(종료: Ctrl+D 또는 'exit')");
    while (true) {
      System.out.print(">>> ");
      String line = reader.readLine();
      if (line == null) {
        break;
      }
      String trimmed = line.trim();
      if (trimmed.equals("exit") || trimmed.equals("exit;")) {
        break;
      }
      if (trimmed.isEmpty()) {
        continue;
      }
      fab.run(line);
    }
  }

  public static void main(String[] args) throws IOException {
    new PromptShell().run();
  }
}
