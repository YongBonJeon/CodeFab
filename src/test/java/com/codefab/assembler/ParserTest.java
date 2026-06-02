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
}
