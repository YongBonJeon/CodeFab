package com.codefab.ast;

public class AssignExpr implements Expr {
  public final String name;
  public final Expr value;

  public AssignExpr(String name, Expr value) {
    this.name = name;
    this.value = value;
  }
}
