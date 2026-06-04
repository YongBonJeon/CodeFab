package com.codefab.checker;

import com.codefab.ast.Expr;
import com.codefab.ast.Stmt;

import java.util.List;

public class Checker implements Expr.Visitor<Void>, Stmt.Visitor<Void> {

    public void check(List<Stmt> statements) {
        for (Stmt s : statements) resolve(s);
    }

    private void resolve(Stmt stmt) { stmt.accept(this); }
    private void resolve(Expr expr) { expr.accept(this); }

    @Override public Void visitBlock(Stmt.Block stmt)           { return null; }
    @Override public Void visitVarDeclare(Stmt.VarDeclare stmt) { return null; }
    @Override public Void visitExpression(Stmt.Expression stmt) { return null; }
    @Override public Void visitPrint(Stmt.Print stmt)           { return null; }
    @Override public Void visitIf(Stmt.If stmt)                 { return null; }
    @Override public Void visitFor(Stmt.For stmt)               { return null; }
    @Override public Void visitLiteral(Expr.Literal expr)       { return null; }
    @Override public Void visitVariable(Expr.Variable expr)     { return null; }
    @Override public Void visitAssign(Expr.Assign expr)         { return null; }
    @Override public Void visitBinary(Expr.Binary expr)         { return null; }
    @Override public Void visitUnary(Expr.Unary expr)           { return null; }
    @Override public Void visitLogical(Expr.Logical expr)       { return null; }
    @Override public Void visitGrouping(Expr.Grouping expr)     { return null; }
}
