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
    out.println("(여러 줄 입력 후 빈 줄(엔터)로 실행 / 종료: Ctrl+D, 'exit', 'quit')");
    StringBuilder buffer = new StringBuilder();
    while (true) {
      out.print(buffer.length() == 0 ? ">>> " : "... ");
      out.flush();
      String line = reader.readLine();
      if (line == null) {
        runIfPresent(buffer);
        break;
      }
      String trimmed = line.trim();
      if (buffer.length() == 0
          && (trimmed.equals("exit") || trimmed.equals("exit;")
              || trimmed.equals("quit") || trimmed.equals("quit;"))) {
        break;
      }
      if (trimmed.isEmpty()) {
        runIfPresent(buffer);
        continue;
      }
      buffer.append(line).append('\n');
    }
  }

  private void runIfPresent(StringBuilder buffer) {
    if (buffer.length() == 0) return;
    fab.run(buffer.toString());
    buffer.setLength(0);
  }

  public static void main(String[] args) throws IOException {
    new PromptShell().run();
  }
}
