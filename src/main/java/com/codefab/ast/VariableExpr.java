package com.codefab.ast;

public class VariableExpr implements Expr {
  public final String name;

  public VariableExpr(String name) {
    this.name = name;
  }
}
