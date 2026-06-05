package com.codefab.optimizer;

import com.codefab.ast.Expr;
import com.codefab.ast.Stmt;
import com.codefab.token.TokenType;
import java.util.ArrayList;
import java.util.List;

/**
 * Pre-execution constant-folding pass. Any sub-expression whose value is fully determined before
 * runtime (operations over literals only) is replaced with a single literal node, so the executor
 * evaluates it zero times instead of once per loop iteration.
 *
 * <p>Folding is conservative: if evaluating a constant expression would fail (e.g. division by zero
 * or a type mismatch), the original node is kept so the error surfaces at runtime with its line.
 */
public class Optimizer implements Expr.Visitor<Expr>, Stmt.Visitor<Stmt> {

  /** How many constant operations were folded away — used to verify the optimization fired. */
  private int foldCount = 0;

  public int getFoldCount() {
    return foldCount;
  }

  public List<Stmt> optimize(List<Stmt> statements) {
    List<Stmt> result = new ArrayList<>();
    for (Stmt stmt : statements) {
      result.add(opt(stmt));
    }
    return result;
  }

  private Stmt opt(Stmt stmt) {
    Stmt result = stmt.accept(this);
    result.line = stmt.line;
    return result;
  }

  private Expr opt(Expr expr) {
    return expr.accept(this);
  }

  // ── Stmt visitors: rebuild with optimized children ────────────────────────

  @Override
  public Stmt visitExpression(Stmt.Expression stmt) {
    return new Stmt.Expression(opt(stmt.expression));
  }

  @Override
  public Stmt visitPrint(Stmt.Print stmt) {
    return new Stmt.Print(opt(stmt.expression));
  }

  @Override
  public Stmt visitVarDeclare(Stmt.VarDeclare stmt) {
    Expr init = stmt.initializer != null ? opt(stmt.initializer) : null;
    return new Stmt.VarDeclare(stmt.name, init);
  }

  @Override
  public Stmt visitBlock(Stmt.Block stmt) {
    return new Stmt.Block(optimize(stmt.statements));
  }

  @Override
  public Stmt visitIf(Stmt.If stmt) {
    Stmt elseBranch = stmt.elseBranch != null ? opt(stmt.elseBranch) : null;
    return new Stmt.If(opt(stmt.condition), opt(stmt.thenBranch), elseBranch);
  }

  @Override
  public Stmt visitFor(Stmt.For stmt) {
    Stmt init = stmt.initializer != null ? opt(stmt.initializer) : null;
    Expr cond = stmt.condition != null ? opt(stmt.condition) : null;
    Expr inc = stmt.increment != null ? opt(stmt.increment) : null;
    return new Stmt.For(init, cond, inc, opt(stmt.body));
  }

  @Override
  public Stmt visitFunction(Stmt.Function stmt) {
    return new Stmt.Function(stmt.name, stmt.params, optimize(stmt.body));
  }

  @Override
  public Stmt visitReturn(Stmt.Return stmt) {
    Expr value = stmt.value != null ? opt(stmt.value) : null;
    return new Stmt.Return(stmt.keyword, value);
  }

  // ── Expr visitors: fold where everything is constant ─────────────────────

  @Override
  public Expr visitLiteral(Expr.Literal expr) {
    return expr;
  }

  @Override
  public Expr visitVariable(Expr.Variable expr) {
    return expr;
  }

  @Override
  public Expr visitAssign(Expr.Assign expr) {
    return new Expr.Assign(expr.name, opt(expr.value));
  }

  @Override
  public Expr visitGrouping(Expr.Grouping expr) {
    Expr inner = opt(expr.expression);
    if (inner instanceof Expr.Literal) {
      return inner;
    }
    return new Expr.Grouping(inner);
  }

  @Override
  public Expr visitBinary(Expr.Binary expr) {
    Expr left = opt(expr.left);
    Expr right = opt(expr.right);
    if (left instanceof Expr.Literal l && right instanceof Expr.Literal r) {
      try {
        Object folded = foldBinary(l.value, expr.operator.type, r.value);
        if (folded != null) {
          foldCount++;
          return new Expr.Literal(folded);
        }
      } catch (RuntimeException ignored) {
        // Not foldable (e.g. divide-by-zero / type error) — leave for runtime.
      }
    }
    return new Expr.Binary(left, expr.operator, right);
  }

  @Override
  public Expr visitUnary(Expr.Unary expr) {
    Expr right = opt(expr.right);
    if (right instanceof Expr.Literal l) {
      Object folded = foldUnary(expr.operator.type, l.value);
      if (folded != null) {
        foldCount++;
        return new Expr.Literal(folded);
      }
    }
    return new Expr.Unary(expr.operator, right);
  }

  @Override
  public Expr visitCall(Expr.Call expr) {
    List<Expr> args = new ArrayList<>();
    for (Expr arg : expr.arguments) {
      args.add(opt(arg));
    }
    return new Expr.Call(opt(expr.callee), expr.paren, args);
  }

  @Override
  public Expr visitIndex(Expr.Index expr) {
    return new Expr.Index(opt(expr.target), expr.bracket, opt(expr.index));
  }

  @Override
  public Expr visitIndexSet(Expr.IndexSet expr) {
    return new Expr.IndexSet(opt(expr.target), expr.bracket, opt(expr.index), opt(expr.value));
  }

  @Override
  public Expr visitLogical(Expr.Logical expr) {
    Expr left = opt(expr.left);
    Expr right = opt(expr.right);
    if (left instanceof Expr.Literal l && right instanceof Expr.Literal r) {
      boolean leftTruthy = isTruthy(l.value);
      Object folded = expr.operator.type == TokenType.AND
          ? (leftTruthy ? r.value : l.value)
          : (leftTruthy ? l.value : r.value);
      foldCount++;
      return new Expr.Literal(folded);
    }
    return new Expr.Logical(left, expr.operator, right);
  }

  // ── Folding helpers (mirror Executor semantics) ───────────────────────────

  private Object foldBinary(Object left, TokenType op, Object right) {
    if (op == TokenType.PLUS && left instanceof String ls && right instanceof String rs) {
      return ls + rs;
    }
    if (!(left instanceof Double a) || !(right instanceof Double b)) {
      return null;
    }
    return switch (op) {
      case PLUS -> a + b;
      case MINUS -> a - b;
      case STAR -> a * b;
      case SLASH -> {
        if (b == 0) throw new ArithmeticException();
        yield a / b;
      }
      case PERCENT -> {
        if (b == 0) throw new ArithmeticException();
        yield a % b;
      }
      case GREATER -> a > b;
      case LESS -> a < b;
      default -> null;
    };
  }

  private Object foldUnary(TokenType op, Object value) {
    return switch (op) {
      case MINUS -> value instanceof Double d ? -d : null;
      case BANG -> !isTruthy(value);
      default -> null;
    };
  }

  private boolean isTruthy(Object value) {
    if (value == null) return false;
    if (value instanceof Boolean b) return b;
    return true;
  }
}
