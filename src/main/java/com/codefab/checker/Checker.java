package com.codefab.checker;

import com.codefab.ast.Expr;
import com.codefab.ast.Stmt;
import com.codefab.error.SemanticError;
import com.codefab.token.Token;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Checker implements Expr.Visitor<Void>, Stmt.Visitor<Void> {

    private final Deque<Map<String, Boolean>> scopes = new ArrayDeque<>();

    public void check(List<Stmt> statements) {
        beginScope();
        for (Stmt s : statements) resolve(s);
        endScope();
    }

    private void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    private void resolve(Expr expr) {
        expr.accept(this);
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    private void endScope() {
        scopes.pop();
    }

    private void declare(Token name) {
        Map<String, Boolean> scope = scopes.peek();
        if (scope.containsKey(name.origin)) {
            throw new SemanticError(name.line,
                    "'" + name.origin + "' 에러: 이미 해당 변수는 현재 스코프에서 사용중입니다.");
        }
        scope.put(name.origin, Boolean.FALSE);
    }

    private void define(Token name) {
        scopes.peek().put(name.origin, Boolean.TRUE);
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
        if (stmt.initializer != null) resolve(stmt.initializer);
        define(stmt.name);
        return null;
    }

    @Override
    public Void visitExpression(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrint(Stmt.Print stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitIf(Stmt.If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if (stmt.elseBranch != null) resolve(stmt.elseBranch);
        return null;
    }

    @Override
    public Void visitFor(Stmt.For stmt) {
        beginScope();
        if (stmt.initializer != null) resolve(stmt.initializer);
        if (stmt.condition != null) resolve(stmt.condition);
        if (stmt.increment != null) resolve(stmt.increment);
        resolve(stmt.body);
        endScope();
        return null;
    }

    @Override
    public Void visitLiteral(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitVariable(Expr.Variable expr) {
        if (Boolean.FALSE.equals(scopes.peek().get(expr.name.origin))) {
            throw new SemanticError(expr.name.line, "자신의 초기화식에서 지역변수를 읽을 수 없습니다.");
        }
        return null;
    }

    @Override
    public Void visitAssign(Expr.Assign expr) {
        resolve(expr.value);
        return null;
    }

    @Override
    public Void visitBinary(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitUnary(Expr.Unary expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitLogical(Expr.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitGrouping(Expr.Grouping expr) {
        resolve(expr.expression);
        return null;
    }
}
