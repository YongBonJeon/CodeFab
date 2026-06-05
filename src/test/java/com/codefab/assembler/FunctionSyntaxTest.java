package com.codefab.assembler;

import static com.codefab.token.TokenType.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.codefab.ast.Expr;
import com.codefab.ast.Stmt;
import com.codefab.token.Token;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("함수/배열/연산자 구문 파싱 테스트")
class FunctionSyntaxTest {

  private List<Stmt> parse(String source) {
    return new Parser(new Tokenizer(source).tokenize()).parse();
  }

  private Expr firstExpr(String source) {
    return ((Stmt.Expression) parse(source).get(0)).expression;
  }

  @Test
  @DisplayName("Func 선언을 Function Stmt 로 파싱한다")
  void parsesFunctionDeclaration() {
    Stmt stmt = parse("Func add(a, b) { return a + b; }").get(0);
    assertThat(stmt).isInstanceOf(Stmt.Function.class);
    Stmt.Function fn = (Stmt.Function) stmt;
    assertThat(fn.name.origin).isEqualTo("add");
    assertThat(fn.params).extracting(p -> p.origin).containsExactly("a", "b");
    assertThat(fn.body).hasSize(1);
    assertThat(fn.body.get(0)).isInstanceOf(Stmt.Return.class);
  }

  @Test
  @DisplayName("값 없는 return 은 value 가 null 이다")
  void parsesEmptyReturn() {
    Stmt.Function fn = (Stmt.Function) parse("Func f() { return; }").get(0);
    assertThat(((Stmt.Return) fn.body.get(0)).value).isNull();
  }

  @Test
  @DisplayName("함수 호출을 Call Expr 로 파싱한다")
  void parsesCall() {
    Expr expr = firstExpr("add(1, 2);");
    assertThat(expr).isInstanceOf(Expr.Call.class);
    Expr.Call call = (Expr.Call) expr;
    assertThat(call.callee).isInstanceOf(Expr.Variable.class);
    assertThat(call.arguments).hasSize(2);
  }

  @Test
  @DisplayName("배열 읽기를 Index Expr 로 파싱한다")
  void parsesIndexRead() {
    Expr expr = firstExpr("arr[i];");
    assertThat(expr).isInstanceOf(Expr.Index.class);
    assertThat(((Expr.Index) expr).target).isInstanceOf(Expr.Variable.class);
  }

  @Test
  @DisplayName("배열 쓰기를 IndexSet Expr 로 파싱한다")
  void parsesIndexWrite() {
    Expr expr = firstExpr("arr[0] = 7;");
    assertThat(expr).isInstanceOf(Expr.IndexSet.class);
    Expr.IndexSet set = (Expr.IndexSet) expr;
    assertThat(set.target).isInstanceOf(Expr.Variable.class);
    assertThat(((Expr.Literal) set.value).value).isEqualTo(7.0);
  }

  @Test
  @DisplayName("두 글자 비교/동등 연산자를 토큰화한다")
  void tokenizesTwoCharOperators() {
    List<Token> tokens = new Tokenizer("a <= b >= c == d != e;").tokenize();
    assertThat(tokens).extracting(t -> t.type)
        .containsSequence(IDENTIFIER, LESS_EQUAL, IDENTIFIER, GREATER_EQUAL, IDENTIFIER,
            EQUAL_EQUAL, IDENTIFIER, BANG_EQUAL, IDENTIFIER);
  }

  @Test
  @DisplayName("% 연산자를 Binary Expr 로 파싱한다")
  void parsesModulo() {
    Expr expr = firstExpr("7 % 3;");
    assertThat(expr).isInstanceOf(Expr.Binary.class);
    assertThat(((Expr.Binary) expr).operator.type).isEqualTo(PERCENT);
  }

  @Test
  @DisplayName("문장에 시작 줄 번호가 기록된다")
  void recordsStatementLine() {
    List<Stmt> stmts = parse("var a = 1;\nvar b = 2;\nprint b;");
    assertThat(stmts.get(0).line).isEqualTo(1);
    assertThat(stmts.get(1).line).isEqualTo(2);
    assertThat(stmts.get(2).line).isEqualTo(3);
  }
}
