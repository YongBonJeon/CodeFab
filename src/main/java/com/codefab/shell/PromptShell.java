package com.codefab.shell;

import com.codefab.CodeFab;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class PromptShell {

  private final CodeFab fab;
  private final BufferedReader reader;
  private final PrintStream out;

  public PromptShell() {
    this(new CodeFab(),
        new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)),
        System.out);
  }

  public PromptShell(CodeFab fab) {
    this(fab,
        new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)),
        System.out);
  }

  public PromptShell(CodeFab fab, BufferedReader reader, PrintStream out) {
    this.fab = fab;
    this.reader = reader;
    this.out = out;
  }

  public void run() throws IOException {
    out.println("CodeFab Interpreter v1.0");
    out.println("(종료: Ctrl+D 또는 'exit')");
    while (true) {
      out.print(">>> ");
      out.flush();
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
