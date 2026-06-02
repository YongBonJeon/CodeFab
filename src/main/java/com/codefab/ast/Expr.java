package com.codefab.ast;

public abstract class Expr {

  public interface Visitor<R> {
    R visitLiteral(Literal expr);
  }

  public abstract <R> R accept(Visitor<R> visitor);

  public static final class Literal extends Expr {
    public final Object value;

    public Literal(Object value) {
      this.value = value;
    }

    @Override
    public <R> R accept(Visitor<R> v) {
      return v.visitLiteral(this);
    }
  }
}
