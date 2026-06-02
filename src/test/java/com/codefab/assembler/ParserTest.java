package com.codefab.assembler;

import static com.codefab.token.TokenType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codefab.ast.Expr;
import com.codefab.ast.Stmt;
import com.codefab.error.ParseError;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ParserTest {

  private List<Stmt> parse(String source) {
    return new Parser(new Tokenizer(source).tokenize()).parse();
  }

  private Expr firstExpr(String source) {
    Stmt stmt = parse(source).get(0);
    return ((Stmt.Expression) stmt).expression;
  }

  @Test
  @DisplayName("숫자 리터럴을 표현식 문으로 감싸 Literal 로 파싱한다")
  void parsesNumberLiteral() {
    Expr expr = firstExpr("42;");
    assertThat(expr).isInstanceOf(Expr.Literal.class);
    assertThat(((Expr.Literal) expr).value).isEqualTo(42.0);
  }

  @Test
  @DisplayName("문자열 리터럴을 Literal 로 파싱한다")
  void parsesStringLiteral() {
    assertThat(((Expr.Literal) firstExpr("\"hi\";")).value).isEqualTo("hi");
  }

  @Test
  @DisplayName("true/false 는 Boolean Literal 로 파싱한다")
  void parsesBooleanLiteral() {
    assertThat(((Expr.Literal) firstExpr("true;")).value).isEqualTo(true);
    assertThat(((Expr.Literal) firstExpr("false;")).value).isEqualTo(false);
  }

  @Test
  @DisplayName("문장 끝에 세미콜론이 없으면 ParseError 를 던진다")
  void throwsWhenSemicolonMissing() {
    assertThatThrownBy(() -> parse("42"))
        .isInstanceOf(ParseError.class)
        .hasMessageContaining("';'");
  }

  @Test
  @DisplayName("식별자는 Variable Expr 로 파싱한다")
  void parsesVariable() {
    Expr expr = firstExpr("a;");
    assertThat(expr).isInstanceOf(Expr.Variable.class);
    assertThat(((Expr.Variable) expr).name.origin).isEqualTo("a");
  }

  @Test
  @DisplayName("괄호는 Grouping Expr 로 감싼다")
  void parsesGrouping() {
    Expr expr = firstExpr("(a);");
    assertThat(expr).isInstanceOf(Expr.Grouping.class);
    assertThat(((Expr.Grouping) expr).expression).isInstanceOf(Expr.Variable.class);
  }

  @Test
  @DisplayName("닫는 괄호가 없으면 ParseError 를 던진다")
  void throwsWhenParenNotClosed() {
    assertThatThrownBy(() -> parse("(a;"))
        .isInstanceOf(ParseError.class)
        .hasMessageContaining("')'");
  }

  @Test
  @DisplayName("단항 연산자는 Unary Expr 로 파싱한다")
  void parsesUnary() {
    Expr expr = firstExpr("-a;");
    assertThat(expr).isInstanceOf(Expr.Unary.class);
    assertThat(((Expr.Unary) expr).operator.type).isEqualTo(MINUS);
  }

  @Test
  @DisplayName("곱셈은 덧셈보다 먼저 묶여 트리 깊은 곳에 위치한다")
  void respectsArithmeticPrecedence() {
    Expr expr = firstExpr("1 + 2 * 3;");
    assertThat(expr).isInstanceOf(Expr.Binary.class);
    Expr.Binary add = (Expr.Binary) expr;
    assertThat(add.operator.type).isEqualTo(PLUS);
    assertThat(add.left).isInstanceOf(Expr.Literal.class);
    assertThat(add.right).isInstanceOf(Expr.Binary.class);
    assertThat(((Expr.Binary) add.right).operator.type).isEqualTo(STAR);
  }

  @Test
  @DisplayName("비교 연산은 Binary Expr 로 파싱한다")
  void parsesComparison() {
    Expr expr = firstExpr("a > 1;");
    assertThat(expr).isInstanceOf(Expr.Binary.class);
    assertThat(((Expr.Binary) expr).operator.type).isEqualTo(GREATER);
  }

  @Test
  @DisplayName("and/or 는 Logical Expr 로 파싱한다")
  void parsesLogical() {
    assertThat(firstExpr("a and b;")).isInstanceOf(Expr.Logical.class);
    assertThat(((Expr.Logical) firstExpr("a or b;")).operator.type).isEqualTo(OR);
  }

  @Test
  @DisplayName("대입은 Assign Expr 로 파싱하고 우변 표현식을 평가식으로 갖는다")
  void parsesAssignment() {
    Expr expr = firstExpr("x = a + b;");
    assertThat(expr).isInstanceOf(Expr.Assign.class);
    Expr.Assign assign = (Expr.Assign) expr;
    assertThat(assign.name.origin).isEqualTo("x");
    assertThat(assign.value).isInstanceOf(Expr.Binary.class);
  }

  @Test
  @DisplayName("대입 대상이 변수가 아니면 ParseError 를 던진다")
  void throwsOnInvalidAssignmentTarget() {
    assertThatThrownBy(() -> parse("1 = 2;"))
        .isInstanceOf(ParseError.class)
        .hasMessageContaining("대입 대상");
  }

  @Test
  @DisplayName("print 문을 Print Stmt 로 파싱한다")
  void parsesPrintStatement() {
    Stmt stmt = parse("print a;").get(0);
    assertThat(stmt).isInstanceOf(Stmt.Print.class);
    assertThat(((Stmt.Print) stmt).expression).isInstanceOf(Expr.Variable.class);
  }

  @Test
  @DisplayName("초기화식이 있는 변수 선언을 VarDeclare Stmt 로 파싱한다")
  void parsesVarDeclarationWithInitializer() {
    Stmt stmt = parse("var a = 3;").get(0);
    assertThat(stmt).isInstanceOf(Stmt.VarDeclare.class);
    Stmt.VarDeclare decl = (Stmt.VarDeclare) stmt;
    assertThat(decl.name.origin).isEqualTo("a");
    assertThat(((Expr.Literal) decl.initializer).value).isEqualTo(3.0);
  }

  @Test
  @DisplayName("초기화식이 없는 변수 선언은 initializer 가 null 이다")
  void parsesVarDeclarationWithoutInitializer() {
    Stmt.VarDeclare decl = (Stmt.VarDeclare) parse("var a;").get(0);
    assertThat(decl.initializer).isNull();
  }

  @Test
  @DisplayName("중괄호 묶음을 Block Stmt 로 파싱한다")
  void parsesBlock() {
    Stmt stmt = parse("{ var a = 1; print a; }").get(0);
    assertThat(stmt).isInstanceOf(Stmt.Block.class);
    assertThat(((Stmt.Block) stmt).statements).hasSize(2);
  }

  @Test
  @DisplayName("else 없는 if 문을 If Stmt 로 파싱한다")
  void parsesIfWithoutElse() {
    Stmt stmt = parse("if (a > 0) a = 1;").get(0);
    assertThat(stmt).isInstanceOf(Stmt.If.class);
    Stmt.If ifStmt = (Stmt.If) stmt;
    assertThat(ifStmt.condition).isInstanceOf(Expr.Binary.class);
    assertThat(ifStmt.thenBranch).isInstanceOf(Stmt.Expression.class);
    assertThat(ifStmt.elseBranch).isNull();
  }

  @Test
  @DisplayName("else 절이 있으면 elseBranch 를 채운다")
  void parsesIfWithElse() {
    Stmt.If ifStmt = (Stmt.If) parse("if (a > 0) a = 1; else a = 2;").get(0);
    assertThat(ifStmt.elseBranch).isInstanceOf(Stmt.Expression.class);
  }

  @Test
  @DisplayName("블록이 닫히지 않으면 ParseError 를 던진다")
  void throwsWhenBlockNotClosed() {
    assertThatThrownBy(() -> parse("{ var a = 1;"))
        .isInstanceOf(ParseError.class)
        .hasMessageContaining("'}'");
  }

  @Test
  @DisplayName("for 문을 초기화/조건/증감/본문을 갖는 For Stmt 로 파싱한다")
  void parsesForStatement() {
    Stmt stmt = parse("for (var i = 0; i < 3; i = i + 1) { print i; }").get(0);
    assertThat(stmt).isInstanceOf(Stmt.For.class);
    Stmt.For forStmt = (Stmt.For) stmt;
    assertThat(forStmt.initializer).isInstanceOf(Stmt.VarDeclare.class);
    assertThat(forStmt.condition).isInstanceOf(Expr.Binary.class);
    assertThat(forStmt.increment).isInstanceOf(Expr.Assign.class);
    assertThat(forStmt.body).isInstanceOf(Stmt.Block.class);
  }

  @Test
  @DisplayName("for 절의 각 항목은 생략될 수 있다")
  void parsesForWithEmptyClauses() {
    Stmt.For forStmt = (Stmt.For) parse("for (;;) print 1;").get(0);
    assertThat(forStmt.initializer).isNull();
    assertThat(forStmt.condition).isNull();
    assertThat(forStmt.increment).isNull();
  }
}
