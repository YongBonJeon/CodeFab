package com.codefab.ast;

import com.codefab.token.Token;

public abstract class Stmt {

  public interface Visitor<R> {
    R visitExpression(Expression stmt);

    R visitPrint(Print stmt);

    R visitVarDeclare(VarDeclare stmt);
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

  public static final class Print extends Stmt {
    public final Expr expression;

    public Print(Expr expression) {
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> v) {
      return v.visitPrint(this);
    }
  }

  public static final class VarDeclare extends Stmt {
    public final Token name;
    public final Expr initializer;

    public VarDeclare(Token name, Expr initializer) {
      this.name = name;
      this.initializer = initializer;
    }

    @Override
    public <R> R accept(Visitor<R> v) {
      return v.visitVarDeclare(this);
    }
  }
}
