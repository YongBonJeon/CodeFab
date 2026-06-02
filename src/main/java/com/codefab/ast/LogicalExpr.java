package com.codefab.ast;

public class LogicalExpr implements Expr {
  public final Expr left;
  public final String op;
  public final Expr right;

  public LogicalExpr(Expr left, String op, Expr right) {
    this.left = left;
    this.op = op;
    this.right = right;
  }
}
