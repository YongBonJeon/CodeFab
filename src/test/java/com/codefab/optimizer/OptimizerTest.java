package com.codefab.optimizer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.codefab.ast.Expr;
import com.codefab.ast.Stmt;
import com.codefab.token.Token;
import com.codefab.token.TokenType;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OptimizerTest {

  private Optimizer optimizer;

  @BeforeEach
  void setUp() {
    optimizer = new Optimizer();
  }

  private Token op(TokenType type, String symbol) {
    return new Token(type, symbol, null, 1);
  }

  private Token id(String name) {
    return new Token(TokenType.IDENTIFIER, name, null, 1);
  }

  private Expr optimizeExpr(Expr expr) {
    List<Stmt> stmts = optimizer.optimize(List.of(new Stmt.Print(expr)));
    return ((Stmt.Print) stmts.get(0)).expression;
  }

  // ── Binary Folding ────────────────────────────────────────────────────────

  @Test
  @DisplayName("두 숫자 리터럴의 덧셈은 단일 리터럴로 폴딩된다")
  void foldBinary_numericAddition() {
    // 3 + 4 → 7
    Expr expr = new Expr.Binary(
        new Expr.Literal(3.0), op(TokenType.PLUS, "+"), new Expr.Literal(4.0));

    Expr result = optimizeExpr(expr);

    assertInstanceOf(Expr.Literal.class, result);
    assertEquals(7.0, ((Expr.Literal) result).value);
    assertEquals(1, optimizer.getFoldCount());
  }

  @Test
  @DisplayName("두 문자열 리터럴의 연결은 단일 리터럴로 폴딩된다")
  void foldBinary_stringConcatenation() {
    // "Hello" + " World" → "Hello World"
    Expr expr = new Expr.Binary(
        new Expr.Literal("Hello"), op(TokenType.PLUS, "+"), new Expr.Literal(" World"));

    Expr result = optimizeExpr(expr);

    assertInstanceOf(Expr.Literal.class, result);
    assertEquals("Hello World", ((Expr.Literal) result).value);
    assertEquals(1, optimizer.getFoldCount());
  }

  @Test
  @DisplayName("비교 연산도 상수끼리면 폴딩된다")
  void foldBinary_comparison() {
    // 3 > 1 → true
    Expr expr = new Expr.Binary(
        new Expr.Literal(3.0), op(TokenType.GREATER, ">"), new Expr.Literal(1.0));

    Expr result = optimizeExpr(expr);

    assertInstanceOf(Expr.Literal.class, result);
    assertEquals(true, ((Expr.Literal) result).value);
    assertEquals(1, optimizer.getFoldCount());
  }

  @Test
  @DisplayName("중첩된 상수 표현식은 재귀적으로 폴딩된다")
  void foldBinary_nested() {
    // (1 + 2) * 3 → 9, foldCount = 2
    Expr inner = new Expr.Binary(
        new Expr.Literal(1.0), op(TokenType.PLUS, "+"), new Expr.Literal(2.0));
    Expr expr = new Expr.Binary(
        inner, op(TokenType.STAR, "*"), new Expr.Literal(3.0));

    Expr result = optimizeExpr(expr);

    assertInstanceOf(Expr.Literal.class, result);
    assertEquals(9.0, ((Expr.Literal) result).value);
    assertEquals(2, optimizer.getFoldCount());
  }

  @Test
  @DisplayName("변수가 포함된 표현식은 폴딩하지 않는다")
  void foldBinary_withVariable_notFolded() {
    // a + 1 → 그대로 유지
    Expr expr = new Expr.Binary(
        new Expr.Variable(id("a")), op(TokenType.PLUS, "+"), new Expr.Literal(1.0));

    Expr result = optimizeExpr(expr);

    assertInstanceOf(Expr.Binary.class, result);
    assertEquals(0, optimizer.getFoldCount());
  }

  @Test
  @DisplayName("0으로 나누는 상수 표현식은 폴딩하지 않고 런타임에 위임한다")
  void foldBinary_divisionByZero_notFolded() {
    // 1 / 0 → 폴딩 안 됨
    Expr expr = new Expr.Binary(
        new Expr.Literal(1.0), op(TokenType.SLASH, "/"), new Expr.Literal(0.0));

    Expr result = optimizeExpr(expr);

    assertInstanceOf(Expr.Binary.class, result);
    assertEquals(0, optimizer.getFoldCount());
  }

  // ── Unary Folding ─────────────────────────────────────────────────────────

  @Test
  @DisplayName("단항 마이너스 상수는 폴딩된다")
  void foldUnary_negation() {
    // -5 → -5.0
    Expr expr = new Expr.Unary(op(TokenType.MINUS, "-"), new Expr.Literal(5.0));

    Expr result = optimizeExpr(expr);

    assertInstanceOf(Expr.Literal.class, result);
    assertEquals(-5.0, ((Expr.Literal) result).value);
    assertEquals(1, optimizer.getFoldCount());
  }

  @Test
  @DisplayName("논리 NOT 상수는 폴딩된다")
  void foldUnary_logicalNot() {
    // !true → false
    Expr expr = new Expr.Unary(op(TokenType.BANG, "!"), new Expr.Literal(true));

    Expr result = optimizeExpr(expr);

    assertInstanceOf(Expr.Literal.class, result);
    assertEquals(false, ((Expr.Literal) result).value);
    assertEquals(1, optimizer.getFoldCount());
  }

  // ── Logical Folding ───────────────────────────────────────────────────────

  @Test
  @DisplayName("true and false 는 false 로 폴딩된다")
  void foldLogical_andFalse() {
    Expr expr = new Expr.Logical(
        new Expr.Literal(true), op(TokenType.AND, "and"), new Expr.Literal(false));

    Expr result = optimizeExpr(expr);

    assertInstanceOf(Expr.Literal.class, result);
    assertEquals(false, ((Expr.Literal) result).value);
    assertEquals(1, optimizer.getFoldCount());
  }

  @Test
  @DisplayName("true or false 는 true 로 폴딩된다")
  void foldLogical_orTrue() {
    Expr expr = new Expr.Logical(
        new Expr.Literal(true), op(TokenType.OR, "or"), new Expr.Literal(false));

    Expr result = optimizeExpr(expr);

    assertInstanceOf(Expr.Literal.class, result);
    assertEquals(true, ((Expr.Literal) result).value);
    assertEquals(1, optimizer.getFoldCount());
  }

  // ── Grouping Folding ──────────────────────────────────────────────────────

  @Test
  @DisplayName("괄호 안이 상수이면 Grouping 노드가 제거된다")
  void foldGrouping_constantInside() {
    // (1 + 2) → 3 (Grouping 제거)
    Expr expr = new Expr.Grouping(
        new Expr.Binary(
            new Expr.Literal(1.0), op(TokenType.PLUS, "+"), new Expr.Literal(2.0)));

    Expr result = optimizeExpr(expr);

    assertInstanceOf(Expr.Literal.class, result);
    assertEquals(3.0, ((Expr.Literal) result).value);
  }

  // ── foldCount 검증 (PDF 요구사항) ─────────────────────────────────────────

  @Test
  @DisplayName("루프 내 상수 표현식의 계산 횟수가 N 회에서 0 회로 줄어든다")
  void foldCount_loopConstantExpression_reducedToZero() {
    // for (...) { total = total + (1 - 2 * 3); }
    // 상수 부분: 2 * 3 → 6, 1 - 6 → -5 (foldCount = 2)
    // 실행 시 total + (-5) 는 변수 포함이라 폴딩 안 됨
    Expr mulExpr = new Expr.Binary(
        new Expr.Literal(2.0), op(TokenType.STAR, "*"), new Expr.Literal(3.0));
    Expr subExpr = new Expr.Binary(
        new Expr.Literal(1.0), op(TokenType.MINUS, "-"), mulExpr);
    Expr addExpr = new Expr.Binary(
        new Expr.Variable(id("total")), op(TokenType.PLUS, "+"), subExpr);
    Stmt body = new Stmt.Block(List.of(new Stmt.Expression(
        new Expr.Assign(id("total"), addExpr))));

    Stmt forStmt = new Stmt.For(
        new Stmt.VarDeclare(id("i"), new Expr.Literal(0.0)),
        new Expr.Binary(new Expr.Variable(id("i")), op(TokenType.LESS, "<"), new Expr.Literal(1000000.0)),
        new Expr.Assign(id("i"), new Expr.Binary(
            new Expr.Variable(id("i")), op(TokenType.PLUS, "+"), new Expr.Literal(1.0))),
        body);

    List<Stmt> optimized = optimizer.optimize(List.of(forStmt));

    // N회 검증: 상수 표현식 2개(2*3, 1-6)가 폴딩됨
    assertEquals(2, optimizer.getFoldCount());

    // 0회 검증: total + (1 - 2 * 3) 에서 상수 부분이 Literal 로 교체됨
    Stmt.For optFor = (Stmt.For) optimized.get(0);
    Stmt.Block optBody = (Stmt.Block) optFor.body;
    Stmt.Expression optExpr = (Stmt.Expression) optBody.statements.get(0);
    Expr.Assign optAssign = (Expr.Assign) optExpr.expression;
    Expr.Binary optAdd = (Expr.Binary) optAssign.value;
    // total + X 에서 X(상수 부분)가 Literal 로 교체되어 런타임 연산 0회
    assertInstanceOf(Expr.Literal.class, optAdd.right);
    assertEquals(-5.0, ((Expr.Literal) optAdd.right).value);
  }
}
