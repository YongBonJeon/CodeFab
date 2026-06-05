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
    System.out.println("(여러 줄 입력 후 빈 줄(엔터)로 실행 / 종료: Ctrl+D, 'exit', 'quit')");

    StringBuilder buffer = new StringBuilder();
    while (true) {
      System.out.print(buffer.length() == 0 ? ">>> " : "... ");
      System.out.flush();
      
      String line = reader.readLine();

      if (line == null) { // Ctrl+D: run whatever is buffered, then exit
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
        runIfPresent(buffer); // blank line == "두 번째 엔터" -> execute the buffer
        continue;
      }

      buffer.append(line).append('\n');
    }
  }

  /** Runs the buffered source (if any) and clears the buffer. */
  private void runIfPresent(StringBuilder buffer) {
    if (buffer.length() == 0) {
      return;
    }
    fab.run(buffer.toString());
    buffer.setLength(0);
  }

  public static void main(String[] args) throws IOException {
    new PromptShell().run();
  }
}
