package com.codefab.shell;

import com.codefab.ast.Stmt;
import com.codefab.executor.ExecutionListener;
import java.io.BufferedReader;
import java.io.PrintStream;

public class Debugger implements ExecutionListener {

  public Debugger(String source, BufferedReader in, PrintStream out) {
    throw new UnsupportedOperationException("Debugger 미구현");
  }

  public void run(String source, String path) {
    throw new UnsupportedOperationException("Debugger 미구현");
  }

  @Override
  public void beforeStatement(Stmt stmt, int depth) {
    throw new UnsupportedOperationException("Debugger 미구현");
  }
}
