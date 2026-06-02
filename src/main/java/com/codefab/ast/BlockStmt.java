package com.codefab.ast;

import java.util.List;

public class BlockStmt implements Stmt {
  public final List<Stmt> statements;

  public BlockStmt(List<Stmt> statements) {
    this.statements = statements;
  }
}
