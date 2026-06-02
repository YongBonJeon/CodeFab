package com.codefab.ast;

public class PrintStmt implements Stmt {
  public final Expr expression;

  public PrintStmt(Expr expression) {
    this.expression = expression;
  }
}
