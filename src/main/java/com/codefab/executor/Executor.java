package com.codefab.executor;

import com.codefab.ast.AssignExpr;
import com.codefab.ast.BinaryExpr;
import com.codefab.ast.BlockStmt;
import com.codefab.ast.Expr;
import com.codefab.ast.ExpressionStmt;
import com.codefab.ast.ForStmt;
import com.codefab.ast.GroupingExpr;
import com.codefab.ast.IfStmt;
import com.codefab.ast.LiteralExpr;
import com.codefab.ast.LogicalExpr;
import com.codefab.ast.PrintStmt;
import com.codefab.ast.Stmt;
import com.codefab.ast.UnaryExpr;
import com.codefab.ast.VarDeclareStmt;
import com.codefab.ast.VariableExpr;
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
    if (stmt instanceof PrintStmt s) {
      Object value = evaluate(s.expression);
      out.println(stringify(value));
    } else if (stmt instanceof VarDeclareStmt s) {
      Object value = evaluate(s.initializer);
      environment.define(s.name, value);
    } else if (stmt instanceof ExpressionStmt s) {
      evaluate(s.expression);
    } else if (stmt instanceof IfStmt s) {
      if (isTruthy(evaluate(s.condition))) {
        execute(s.thenBranch);
      } else if (s.elseBranch != null) {
        execute(s.elseBranch);
      }
    } else if (stmt instanceof ForStmt s) {
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
    } else if (stmt instanceof BlockStmt s) {
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
    if (expr instanceof LiteralExpr e) {
      return e.value;
    }
    if (expr instanceof GroupingExpr e) {
      return evaluate(e.expression);
    }
    if (expr instanceof LogicalExpr e) {
      Object left = evaluate(e.left);
      if (e.op.equals("and")) {
        return isTruthy(left) ? evaluate(e.right) : left;
      }
      return isTruthy(left) ? left : evaluate(e.right);
    }
    if (expr instanceof UnaryExpr e) {
      Object right = evaluate(e.right);
      if (e.op.equals("-")) return -(double) right;
      if (e.op.equals("!")) return !isTruthy(right);
    }
    if (expr instanceof VariableExpr e) {
      return environment.get(e.name);
    }
    if (expr instanceof AssignExpr e) {
      Object value = evaluate(e.value);
      environment.assign(e.name, value);
      return value;
    }
    if (expr instanceof BinaryExpr e) {
      double left = (double) evaluate(e.left);
      double right = (double) evaluate(e.right);
      switch (e.op) {
        case "+": return left + right;
        case "-": return left - right;
        case "*": return left * right;
        case "/": return left / right;
        case ">": return left > right;
        case "<": return left < right;
      }
    }
    throw new UnsupportedOperationException("Not implemented yet");
  }

  private boolean isTruthy(Object value) {
    if (value == null) return false;
    if (value instanceof Boolean b) return b;
    return true;
  }

  private String stringify(Object value) {
    if (value == null) {
      return "nil";
    }
    if (value instanceof Double d) {
      String text = d.toString();
      if (text.endsWith(".0")) {
        return text.substring(0, text.length() - 2);
      }
      return text;
    }
    return value.toString();
  }
}
