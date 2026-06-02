package com.codefab.ast;

public class ExpressionStmt implements Stmt {
  public final Expr expression;

  public ExpressionStmt(Expr expression) {
    this.expression = expression;
  }
}
