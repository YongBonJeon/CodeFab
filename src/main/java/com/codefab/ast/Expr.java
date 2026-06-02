package com.codefab.ast;

import com.codefab.token.Token;

public abstract class Expr {

    public interface Visitor<R> {
        R visitLiteral(Literal expr);
        R visitVariable(Variable expr);
        R visitAssign(Assign expr);
        R visitBinary(Binary expr);
        R visitUnary(Unary expr);
        R visitLogical(Logical expr);
        R visitGrouping(Grouping expr);
    }

    public abstract <R> R accept(Visitor<R> visitor);

    public static final class Literal extends Expr {
        public final Object value;
        public Literal(Object value) { this.value = value; }
        @Override public <R> R accept(Visitor<R> v) { return v.visitLiteral(this); }
    }

    public static final class Variable extends Expr {
        public final Token name;
        public Variable(Token name) { this.name = name; }
        @Override public <R> R accept(Visitor<R> v) { return v.visitVariable(this); }
    }

    public static final class Assign extends Expr {
        public final Token name;
        public final Expr value;
        public Assign(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }
        @Override public <R> R accept(Visitor<R> v) { return v.visitAssign(this); }
    }

    public static final class Binary extends Expr {
        public final Expr left;
        public final Token operator;
        public final Expr right;
        public Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }
        @Override public <R> R accept(Visitor<R> v) { return v.visitBinary(this); }
    }

    public static final class Unary extends Expr {
        public final Token operator;
        public final Expr right;
        public Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }
        @Override public <R> R accept(Visitor<R> v) { return v.visitUnary(this); }
    }

    public static final class Logical extends Expr {
        public final Expr left;
        public final Token operator;
        public final Expr right;
        public Logical(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }
        @Override public <R> R accept(Visitor<R> v) { return v.visitLogical(this); }
    }

    public static final class Grouping extends Expr {
        public final Expr expression;
        public Grouping(Expr expression) { this.expression = expression; }
        @Override public <R> R accept(Visitor<R> v) { return v.visitGrouping(this); }
    }
}
