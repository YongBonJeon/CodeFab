package com.codefab.executor;

import com.codefab.ast.Expr;
import com.codefab.ast.Stmt;
import com.codefab.error.RuntimeError;
import java.io.PrintStream;
import java.util.List;

public class Executor implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
  private final Environment globals = new Environment();
  private Environment environment = globals;
  private final PrintStream out;

  public Executor() {
    this.out = System.out;
  }

  public Executor(PrintStream out) {
    this.out = out;
  }

  public void execute(List<Stmt> statements) {
    for (Stmt stmt : statements) {
      execute(stmt);
    }
  }

  private void execute(Stmt stmt) {
    stmt.accept(this);
  }

  private Object evaluate(Expr expr) {
    return expr.accept(this);
  }

  void executeBlock(List<Stmt> statements, Environment newEnvironment) {
    Environment previous = this.environment;
    this.environment = newEnvironment;
    try {
      for (Stmt stmt : statements) {
        execute(stmt);
      }
    } finally {
      this.environment = previous;
    }
  }

  // ── Stmt Visitors ────────────────────────────────────────────────────────

  @Override
  public Void visitExpression(Stmt.Expression stmt) {
    evaluate(stmt.expression);
    return null;
  }

  @Override
  public Void visitPrint(Stmt.Print stmt) {
    Object value = evaluate(stmt.expression);
    out.println(stringify(value));
    return null;
  }

  @Override
  public Void visitVarDeclare(Stmt.VarDeclare stmt) {
    Object value = evaluate(stmt.initializer);
    environment.define(stmt.name.origin, value);
    return null;
  }

  @Override
  public Void visitBlock(Stmt.Block stmt) {
    executeBlock(stmt.statements, new Environment(environment));
    return null;
  }

  @Override
  public Void visitIf(Stmt.If stmt) {
    if (isTruthy(evaluate(stmt.condition))) {
      execute(stmt.thenBranch);
    } else if (stmt.elseBranch != null) {
      execute(stmt.elseBranch);
    }
    return null;
  }

  @Override
  public Void visitFor(Stmt.For stmt) {
    Environment previous = this.environment;
    this.environment = new Environment(previous);
    try {
      if (stmt.initializer != null) execute(stmt.initializer);
      while (isTruthy(evaluate(stmt.condition))) {
        execute(stmt.body);
        if (stmt.increment != null) evaluate(stmt.increment);
      }
    } finally {
      this.environment = previous;
    }
    return null;
  }

  // ── Expr Visitors ────────────────────────────────────────────────────────

  @Override
  public Object visitLiteral(Expr.Literal expr) {
    return expr.value;
  }

  @Override
  public Object visitVariable(Expr.Variable expr) {
    return environment.get(expr.name.origin);
  }

  @Override
  public Object visitAssign(Expr.Assign expr) {
    Object value = evaluate(expr.value);
    environment.assign(expr.name.origin, value);
    return value;
  }

  @Override
  public Object visitBinary(Expr.Binary expr) {
    Object leftVal = evaluate(expr.left);
    Object rightVal = evaluate(expr.right);
    if (!(leftVal instanceof Double) || !(rightVal instanceof Double)) {
      throw new RuntimeError("피연산자는 숫자여야 합니다");
    }
    double left = (double) leftVal;
    double right = (double) rightVal;
    return switch (expr.operator.origin) {
      case "+" -> left + right;
      case "-" -> left - right;
      case "*" -> left * right;
      case "/" -> {
        if (right == 0) throw new RuntimeError("0으로 나눌 수 없습니다");
        yield left / right;
      }
      case ">" -> left > right;
      case "<" -> left < right;
      default -> throw new UnsupportedOperationException("지원하지 않는 연산자: " + expr.operator.origin);
    };
  }

  @Override
  public Object visitUnary(Expr.Unary expr) {
    Object right = evaluate(expr.right);
    return switch (expr.operator.origin) {
      case "-" -> -(double) right;
      case "!" -> !isTruthy(right);
      default -> throw new UnsupportedOperationException("지원하지 않는 단항 연산자: " + expr.operator.origin);
    };
  }

  @Override
  public Object visitLogical(Expr.Logical expr) {
    Object left = evaluate(expr.left);
    return switch (expr.operator.origin) {
      case "and" -> isTruthy(left) ? evaluate(expr.right) : left;
      case "or" -> isTruthy(left) ? left : evaluate(expr.right);
      default -> throw new UnsupportedOperationException("지원하지 않는 논리 연산자: " + expr.operator.origin);
    };
  }

  @Override
  public Object visitGrouping(Expr.Grouping expr) {
    return evaluate(expr.expression);
  }

  // ── Helpers ──────────────────────────────────────────────────────────────

  private boolean isTruthy(Object value) {
    if (value == null) return false;
    if (value instanceof Boolean b) return b;
    return true;
  }

  private String stringify(Object value) {
    if (value == null) return "nil";
    if (value instanceof Double d) {
      String text = d.toString();
      if (text.endsWith(".0")) return text.substring(0, text.length() - 2);
      return text;
    }
    return value.toString();
  }
}
