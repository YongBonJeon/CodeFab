package com.codefab.shell;

import com.codefab.CodeFab;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileRunner {

  private final PrintStream out;
  private final PrintStream err;

  public FileRunner(PrintStream out, PrintStream err) {
    this.out = out;
    this.err = err;
  }

  public int run(String path) {
    Path file = Paths.get(path);
    if (!Files.exists(file)) {
      err.println("[오류] 파일을 찾을 수 없습니다: " + path);
      return 66;
    }
    String source;
    try {
      source = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    } catch (IOException e) {
      err.println("[오류] 파일을 읽을 수 없습니다: " + path + " (" + e.getMessage() + ")");
      return 66;
    }
    CodeFab fab = new CodeFab(out, err);
    return fab.run(source) ? 0 : 70;
  }
}
