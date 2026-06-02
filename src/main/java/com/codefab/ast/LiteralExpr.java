package com.codefab.ast;

public class LiteralExpr implements Expr {
  public final Object value;

  public LiteralExpr(Object value) {
    this.value = value;
  }
}
