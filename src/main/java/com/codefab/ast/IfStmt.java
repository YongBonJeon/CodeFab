package com.codefab.ast;

public class IfStmt implements Stmt {
  public final Expr condition;
  public final Stmt thenBranch;
  public final Stmt elseBranch;

  public IfStmt(Expr condition, Stmt thenBranch, Stmt elseBranch) {
    this.condition = condition;
    this.thenBranch = thenBranch;
    this.elseBranch = elseBranch;
  }
}
