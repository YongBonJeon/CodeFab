package com.codefab.executor;

import com.codefab.ast.Expr;
import com.codefab.ast.Stmt;
import java.io.PrintStream;
import java.util.List;

public class Executor {
  private final Environment globals = new Environment();
  private Environment environment = globals;
  private final PrintStream out;

  public Executor() {
    this.out = System.out;
  }

  public Executor(PrintStream out) {
    this.out = out;
  }

  public void execute(List<Stmt> statements) {
    for (Stmt stmt : statements) {
      execute(stmt);
    }
  }

  void execute(Stmt stmt) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  Object evaluate(Expr expr) {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
