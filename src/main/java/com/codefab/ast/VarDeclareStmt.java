package com.codefab.ast;

public class VarDeclareStmt implements Stmt {
  public final String name;
  public final Expr initializer;

  public VarDeclareStmt(String name, Expr initializer) {
    this.name = name;
    this.initializer = initializer;
  }
}
