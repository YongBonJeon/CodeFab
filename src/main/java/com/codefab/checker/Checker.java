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

    private void beginScope() { scopes.push(new HashMap<>()); }
    private void endScope()   { scopes.pop(); }

    private void declare(Token name) {
        if (scopes.isEmpty()) return;
        Map<String, Boolean> scope = scopes.peek();
        if (scope.containsKey(name.origin)) {
            throw new SemanticError(name.line,
                    "'" + name.origin + "' 에러: 이미 해당 변수는 현재 스코프에서 사용중입니다.");
        }
        scope.put(name.origin, Boolean.TRUE);
    }

    @Override
    public Void visitBlock(Stmt.Block stmt) {
        beginScope();
        for (Stmt s : stmt.statements) resolve(s);
        endScope();
        return null;
    }

    @Override
    public Void visitVarDeclare(Stmt.VarDeclare stmt) {
        declare(stmt.name);
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
