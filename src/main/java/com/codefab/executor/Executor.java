package com.codefab.executor;

import com.codefab.ast.Expr;
import com.codefab.ast.Stmt;
import com.codefab.error.RuntimeError;
import java.io.PrintStream;
import java.util.List;

public class Executor {
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

  void execute(Stmt stmt) {
    if (stmt instanceof Stmt.Print s) {
      Object value = evaluate(s.expression);
      out.println(stringify(value));
    } else if (stmt instanceof Stmt.VarDeclare s) {
      Object value = evaluate(s.initializer);
      environment.define(s.name.origin, value);
    } else if (stmt instanceof Stmt.Expression s) {
      evaluate(s.expression);
    } else if (stmt instanceof Stmt.If s) {
      if (isTruthy(evaluate(s.condition))) {
        execute(s.thenBranch);
      } else if (s.elseBranch != null) {
        execute(s.elseBranch);
      }
    } else if (stmt instanceof Stmt.For s) {
      Environment previous = this.environment;
      this.environment = new Environment(previous);
      try {
        if (s.initializer != null) execute(s.initializer);
        while (isTruthy(evaluate(s.condition))) {
          execute(s.body);
          if (s.increment != null) evaluate(s.increment);
        }
      } finally {
        this.environment = previous;
      }
    } else if (stmt instanceof Stmt.Block s) {
      Environment previous = this.environment;
      this.environment = new Environment(previous);
      try {
        for (Stmt statement : s.statements) {
          execute(statement);
        }
      } finally {
        this.environment = previous;
      }
    }
  }

  Object evaluate(Expr expr) {
    if (expr instanceof Expr.Literal e) {
      return e.value;
    }
    if (expr instanceof Expr.Grouping e) {
      return evaluate(e.expression);
    }
    if (expr instanceof Expr.Logical e) {
      Object left = evaluate(e.left);
      if (e.operator.origin.equals("and")) {
        return isTruthy(left) ? evaluate(e.right) : left;
      }
      return isTruthy(left) ? left : evaluate(e.right);
    }
    if (expr instanceof Expr.Unary e) {
      Object right = evaluate(e.right);
      if (e.operator.origin.equals("-")) return -(double) right;
      if (e.operator.origin.equals("!")) return !isTruthy(right);
    }
    if (expr instanceof Expr.Variable e) {
      return environment.get(e.name.origin);
    }
    if (expr instanceof Expr.Assign e) {
      Object value = evaluate(e.value);
      environment.assign(e.name.origin, value);
      return value;
    }
    if (expr instanceof Expr.Binary e) {
      Object leftVal = evaluate(e.left);
      Object rightVal = evaluate(e.right);
      if (!(leftVal instanceof Double) || !(rightVal instanceof Double)) {
        throw new RuntimeError("피연산자는 숫자여야 합니다");
      }
      double left = (double) leftVal;
      double right = (double) rightVal;
      switch (e.operator.origin) {
        case "+": return left + right;
        case "-": return left - right;
        case "*": return left * right;
        case "/":
          if (right == 0) throw new RuntimeError("0으로 나눌 수 없습니다");
          return left / right;
        case ">": return left > right;
        case "<": return left < right;
      }
    }
    throw new UnsupportedOperationException("Not implemented yet");
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
