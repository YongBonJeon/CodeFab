package com.codefab.checker;

import com.codefab.ast.Expr;
import com.codefab.ast.Stmt;
import com.codefab.error.SemanticError;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Checker implements Expr.Visitor<Void>, Stmt.Visitor<Void> {

    private final Deque<Map<String, Boolean>> scopes = new ArrayDeque<>();

    public void check(List<Stmt> statements) {
        for (Stmt s : statements) resolve(s);
    }

    private void resolve(Stmt stmt) { stmt.accept(this); }
    private void resolve(Expr expr) { expr.accept(this); }

    @Override
    public Void visitBlock(Stmt.Block stmt) {
        scopes.push(new HashMap<>());
        for (Stmt s : stmt.statements) resolve(s);
        scopes.pop();
        return null;
    }

    @Override
    public Void visitVarDeclare(Stmt.VarDeclare stmt) {
        if (!scopes.isEmpty()) {
            Map<String, Boolean> scope = scopes.peek();
            if (scope.containsKey(stmt.name.origin)) {
                throw new SemanticError(stmt.name.line,
                        "'" + stmt.name.origin + "' 에러: 이미 해당 변수는 현재 스코프에서 사용중입니다.");
            }
            scope.put(stmt.name.origin, Boolean.TRUE);
        }
        return null;
    }
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
