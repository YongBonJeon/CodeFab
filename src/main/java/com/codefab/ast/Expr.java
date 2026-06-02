package com.codefab.ast;

import com.codefab.token.Token;

public abstract class Expr {

  public interface Visitor<R> {
    R visitLiteral(Literal expr);

    R visitVariable(Variable expr);

    R visitGrouping(Grouping expr);
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

  public static final class Variable extends Expr {
    public final Token name;

    public Variable(Token name) {
      this.name = name;
    }

    @Override
    public <R> R accept(Visitor<R> v) {
      return v.visitVariable(this);
    }
  }

  public static final class Grouping extends Expr {
    public final Expr expression;

    public Grouping(Expr expression) {
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> v) {
      return v.visitGrouping(this);
    }
  }
}
