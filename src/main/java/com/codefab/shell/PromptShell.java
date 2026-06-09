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
    out.println(Ansi.GRAY + "  빈 줄로 실행  |  exit 로 종료" + Ansi.RESET);
    out.println();
    StringBuilder buffer = new StringBuilder();
    while (true) {
      out.print(buffer.length() == 0
          ? Ansi.CYAN + ">>> " + Ansi.RESET
          : Ansi.GRAY + "... " + Ansi.RESET);
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
