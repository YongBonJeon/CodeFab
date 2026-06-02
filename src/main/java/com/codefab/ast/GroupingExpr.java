package com.codefab.ast;

public class GroupingExpr implements Expr {
  public final Expr expression;

  public GroupingExpr(Expr expression) {
    this.expression = expression;
  }
}
