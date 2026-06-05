package com.codefab.executor;

import com.codefab.ast.Expr;
import com.codefab.ast.Stmt;
import com.codefab.error.ExecutionError;
import com.codefab.token.Token;
import com.codefab.token.TokenType;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Executor implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
  private final Environment globals;
  private Environment environment;
  private final PrintStream out;
  private Map<Expr, Integer> locals = new HashMap<>();

  public Executor(PrintStream out) {
    this.out = out;
    this.globals = new Environment();
    this.environment = globals;
    defineNatives();
  }

  public void resolve(Map<Expr, Integer> locals) {
    this.locals = locals;
  }

  private void defineNatives() {
    globals.define("Array", new CodeFabCallable() {
      @Override
      public int arity() {
        return 1;
      }

      @Override
      public Object call(Executor executor, List<Object> arguments, Token paren) {
        Object size = arguments.get(0);
        if (!(size instanceof Double d) || d != Math.floor(d) || d < 0) {
          throw new ExecutionError(paren.line, "배열의 크기는 0 이상의 정수여야 합니다.");
        }
        return new CodeFabArray(d.intValue());
      }
    });
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
    Object value = stmt.initializer != null ? evaluate(stmt.initializer) : null;
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
      while (stmt.condition == null || isTruthy(evaluate(stmt.condition))) {
        execute(stmt.body);
        if (stmt.increment != null) evaluate(stmt.increment);
      }
    } finally {
      this.environment = previous;
    }
    return null;
  }

  @Override
  public Void visitFunction(Stmt.Function stmt) {
    CodeFabFunction function = new CodeFabFunction(stmt, environment);
    environment.define(stmt.name.origin, function);
    return null;
  }

  @Override
  public Void visitReturn(Stmt.Return stmt) {
    Object value = stmt.value != null ? evaluate(stmt.value) : null;
    throw new Return(value);
  }

  // ── Expr Visitors ────────────────────────────────────────────────────────

  @Override
  public Object visitLiteral(Expr.Literal expr) {
    return expr.value;
  }

  @Override
  public Object visitVariable(Expr.Variable expr) {
    Integer distance = locals.get(expr);
    if (distance != null) {
      return environment.getAt(distance, expr.name.origin);
    }
    return environment.get(expr.name);
  }

  @Override
  public Object visitAssign(Expr.Assign expr) {
    Object value = evaluate(expr.value);
    Integer distance = locals.get(expr);
    if (distance != null) {
      environment.assignAt(distance, expr.name, value);
    } else {
      environment.assign(expr.name, value);
    }
    return value;
  }

  @Override
  public Object visitCall(Expr.Call expr) {
    Object callee = evaluate(expr.callee);
    List<Object> arguments = new ArrayList<>();
    for (Expr argument : expr.arguments) {
      arguments.add(evaluate(argument));
    }
    if (!(callee instanceof CodeFabCallable callable)) {
      throw new ExecutionError(expr.paren.line, "함수만 호출할 수 있습니다.");
    }
    if (arguments.size() != callable.arity()) {
      throw new ExecutionError(expr.paren.line,
          "인자 개수가 일치하지 않습니다. " + callable.arity() + "개를 기대했지만 " + arguments.size() + "개가 전달되었습니다.");
    }
    return callable.call(this, arguments, expr.paren);
  }

  @Override
  public Object visitBinary(Expr.Binary expr) {
    Object leftVal = evaluate(expr.left);
    Object rightVal = evaluate(expr.right);
    if (expr.operator.type == TokenType.PLUS) {
      if (leftVal instanceof String l && rightVal instanceof String r) {
        return l + r;
      }
    }
    if (expr.operator.type == TokenType.EQUAL_EQUAL) {
      return isEqual(leftVal, rightVal);
    }
    if (expr.operator.type == TokenType.BANG_EQUAL) {
      return !isEqual(leftVal, rightVal);
    }
    checkNumberOperands(leftVal, rightVal, expr.operator);
    double left = (double) leftVal;
    double right = (double) rightVal;
    return switch (expr.operator.type) {
      case PLUS -> left + right;
      case MINUS -> left - right;
      case STAR -> left * right;
      case SLASH -> {
        if (right == 0) throw new ExecutionError(expr.operator.line, "0으로 나눌 수 없습니다");
        yield left / right;
      }
      case PERCENT -> {
        if (right == 0) throw new ExecutionError(expr.operator.line, "0으로 나눌 수 없습니다");
        yield left % right;
      }
      case GREATER -> left > right;
      case GREATER_EQUAL -> left >= right;
      case LESS -> left < right;
      case LESS_EQUAL -> left <= right;
      default -> throw new UnsupportedOperationException("지원하지 않는 연산자: " + expr.operator.type);
    };
  }

  @Override
  public Object visitUnary(Expr.Unary expr) {
    Object right = evaluate(expr.right);
    return switch (expr.operator.type) {
      case MINUS -> {
        checkNumberOperand(right, expr.operator);
        yield -(double) right;
      }
      case BANG -> !isTruthy(right);
      default -> throw new UnsupportedOperationException("지원하지 않는 단항 연산자: " + expr.operator.type);
    };
  }

  @Override
  public Object visitLogical(Expr.Logical expr) {
    Object left = evaluate(expr.left);
    return switch (expr.operator.type) {
      case AND -> isTruthy(left) ? evaluate(expr.right) : left;
      case OR -> isTruthy(left) ? left : evaluate(expr.right);
      default -> throw new UnsupportedOperationException("지원하지 않는 논리 연산자: " + expr.operator.type);
    };
  }

  @Override
  public Object visitGrouping(Expr.Grouping expr) {
    return evaluate(expr.expression);
  }

  @Override
  public Object visitIndex(Expr.Index expr) {
    Object target = evaluate(expr.target);
    if (!(target instanceof CodeFabArray array)) {
      throw new ExecutionError(expr.bracket.line, "인덱스 접근은 배열만 지원합니다.");
    }
    return array.get(indexOf(expr.index, expr.bracket, array));
  }

  @Override
  public Object visitIndexSet(Expr.IndexSet expr) {
    Object target = evaluate(expr.target);
    if (!(target instanceof CodeFabArray array)) {
      throw new ExecutionError(expr.bracket.line, "인덱스 접근은 배열만 지원합니다.");
    }
    int index = indexOf(expr.index, expr.bracket, array);
    Object value = evaluate(expr.value);
    array.set(index, value);
    return value;
  }

  private int indexOf(Expr indexExpr, Token bracket, CodeFabArray array) {
    Object raw = evaluate(indexExpr);
    if (!(raw instanceof Double d) || d != Math.floor(d)) {
      throw new ExecutionError(bracket.line, "배열 인덱스는 정수여야 합니다.");
    }
    int index = d.intValue();
    if (index < 0 || index >= array.size()) {
      throw new ExecutionError(bracket.line,
          "배열 인덱스 " + index + " 가 범위를 벗어났습니다. (크기: " + array.size() + ")");
    }
    return index;
  }

  // ── Helpers ──────────────────────────────────────────────────────────────

  private void checkNumberOperand(Object operand, Token operator) {
    if (!(operand instanceof Double)) {
      throw new ExecutionError(operator.line, "피연산자는 숫자여야 합니다");
    }
  }

  private void checkNumberOperands(Object left, Object right, Token operator) {
    if (!(left instanceof Double) || !(right instanceof Double)) {
      throw new ExecutionError(operator.line, "피연산자는 숫자여야 합니다");
    }
  }

  private boolean isEqual(Object a, Object b) {
    if (a == null && b == null) return true;
    if (a == null) return false;
    return a.equals(b);
  }

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
