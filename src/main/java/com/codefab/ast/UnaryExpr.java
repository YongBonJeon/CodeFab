package com.codefab.ast;

public class UnaryExpr implements Expr {
  public final String op;
  public final Expr right;

  public UnaryExpr(String op, Expr right) {
    this.op = op;
    this.right = right;
  }
}
