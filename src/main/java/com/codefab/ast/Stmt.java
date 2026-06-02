package com.codefab.ast;

public abstract class Stmt {

  public interface Visitor<R> {
    R visitExpression(Expression stmt);
  }

  public abstract <R> R accept(Visitor<R> visitor);

  public static final class Expression extends Stmt {
    public final Expr expression;

    public Expression(Expr expression) {
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> v) {
      return v.visitExpression(this);
    }
  }
}
