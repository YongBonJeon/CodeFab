package com.codefab.ast;

import com.codefab.token.Token;

import java.util.List;

public abstract class Stmt {

    public interface Visitor<R> {
        R visitExpression(Expression stmt);
        R visitPrint(Print stmt);
        R visitVarDeclare(VarDeclare stmt);
        R visitBlock(Block stmt);
        R visitIf(If stmt);
        R visitFor(For stmt);
    }

    public abstract <R> R accept(Visitor<R> visitor);

    public static final class Expression extends Stmt {
        public final Expr expression;
        public Expression(Expr expression) { this.expression = expression; }
        @Override public <R> R accept(Visitor<R> v) { return v.visitExpression(this); }
    }

    public static final class Print extends Stmt {
        public final Expr expression;
        public Print(Expr expression) { this.expression = expression; }
        @Override public <R> R accept(Visitor<R> v) { return v.visitPrint(this); }
    }

    public static final class VarDeclare extends Stmt {
        public final Token name;
        public final Expr initializer;
        public VarDeclare(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }
        @Override public <R> R accept(Visitor<R> v) { return v.visitVarDeclare(this); }
    }

    public static final class Block extends Stmt {
        public final List<Stmt> statements;
        public Block(List<Stmt> statements) { this.statements = statements; }
        @Override public <R> R accept(Visitor<R> v) { return v.visitBlock(this); }
    }

    public static final class If extends Stmt {
        public final Expr condition;
        public final Stmt thenBranch;
        public final Stmt elseBranch;
        public If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }
        @Override public <R> R accept(Visitor<R> v) { return v.visitIf(this); }
    }

    public static final class For extends Stmt {
        public final Stmt initializer;
        public final Expr condition;
        public final Expr increment;
        public final Stmt body;
        public For(Stmt initializer, Expr condition, Expr increment, Stmt body) {
            this.initializer = initializer;
            this.condition = condition;
            this.increment = increment;
            this.body = body;
        }
        @Override public <R> R accept(Visitor<R> v) { return v.visitFor(this); }
    }
}
