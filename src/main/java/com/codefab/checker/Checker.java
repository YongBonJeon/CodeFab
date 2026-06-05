package com.codefab.checker;

import com.codefab.ast.Expr;
import com.codefab.ast.Stmt;
import com.codefab.error.SemanticError;
import com.codefab.token.Token;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class Checker implements Expr.Visitor<Void>, Stmt.Visitor<Void> {

    private final Deque<Map<String, Boolean>> scopes = new ArrayDeque<>();
    private final Map<Expr, Integer> locals = new IdentityHashMap<>();

    private boolean inFunction = false;

    public Checker() {
        beginScope();
    }

    public void check(List<Stmt> statements) {
        for (Stmt s : statements) resolve(s);
    }

    public Map<Expr, Integer> getLocals() {
        return locals;
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

    private void resolveLocal(Expr expr, String name) {
        int distance = 0;
        for (Map<String, Boolean> scope : scopes) {
            if (scope.containsKey(name)) {
                locals.put(expr, distance);
                return;
            }
            distance++;
        }
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
    public Void visitFunction(Stmt.Function stmt) {
        // Declare the function name in the current scope first so recursion resolves.
        declare(stmt.name);
        define(stmt.name);

        boolean enclosingInFunction = inFunction;
        inFunction = true;
        beginScope();
        for (Token param : stmt.params) {
            if (scopes.peek().containsKey(param.origin)) {
                throw new SemanticError(param.line,
                        "'" + param.origin + "' 에러: 파라미터 이름이 중복되었습니다.");
            }
            declare(param);
            define(param);
        }
        for (Stmt s : stmt.body) resolve(s);
        endScope();
        inFunction = enclosingInFunction;
        return null;
    }

    @Override
    public Void visitReturn(Stmt.Return stmt) {
        if (!inFunction) {
            throw new SemanticError(stmt.keyword.line, "함수 외부에서는 return 을 사용할 수 없습니다.");
        }
        if (stmt.value != null) resolve(stmt.value);
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
        resolveLocal(expr, expr.name.origin);
        return null;
    }

    @Override
    public Void visitAssign(Expr.Assign expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.name.origin);
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

    @Override
    public Void visitCall(Expr.Call expr) {
        resolve(expr.callee);
        for (Expr argument : expr.arguments) resolve(argument);
        return null;
    }
}
