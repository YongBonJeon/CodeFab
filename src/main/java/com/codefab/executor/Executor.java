package com.codefab.executor;

import com.codefab.ast.Expr;
import com.codefab.ast.LiteralExpr;
import com.codefab.ast.PrintStmt;
import com.codefab.ast.Stmt;
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
    }
  }

  Object evaluate(Expr expr) {
    if (expr instanceof LiteralExpr e) {
      return e.value;
    }
    throw new UnsupportedOperationException("Not implemented yet");
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
