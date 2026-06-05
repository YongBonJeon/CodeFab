package com.codefab.executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codefab.assembler.Parser;
import com.codefab.assembler.Tokenizer;
import com.codefab.ast.Stmt;
import com.codefab.checker.Checker;
import com.codefab.error.ExecutionError;
import com.codefab.error.SemanticError;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("함수 기능 통합 테스트")
class FunctionTest {

  /** Runs source through Tokenizer -> Parser -> Checker -> Executor and returns trimmed stdout. */
  private String run(String source) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Executor executor = new Executor(new PrintStream(out, true, StandardCharsets.UTF_8));
    List<Stmt> stmts = new Parser(new Tokenizer(source).tokenize()).parse();
    new Checker().check(stmts);
    executor.execute(stmts);
    return out.toString(StandardCharsets.UTF_8).trim().replace("\r\n", "\n");
  }

  @Test
  @DisplayName("함수를 선언하고 호출해 반환값을 받는다")
  void declareAndCall() {
    assertThat(run("Func add(a, b) { return a + b; } var ret = add(3, 7); print ret;"))
        .isEqualTo("10");
  }

  @Test
  @DisplayName("재귀 호출로 팩토리얼을 계산한다")
  void recursion() {
    assertThat(run("Func fact(n) { if (n <= 1) return 1; return n * fact(n - 1); } print fact(5);"))
        .isEqualTo("120");
  }

  @Test
  @DisplayName("값 없는 return 은 null(nil) 을 반환한다")
  void emptyReturnGivesNil() {
    assertThat(run("Func f() { return; } print f();")).isEqualTo("nil");
  }

  @Test
  @DisplayName("return 이 없는 함수도 null(nil) 을 반환한다")
  void noReturnGivesNil() {
    assertThat(run("Func f() { var x = 1; } print f();")).isEqualTo("nil");
  }

  @Test
  @DisplayName("매개변수가 호출 인자로 바인딩된다")
  void parametersBindToArguments() {
    assertThat(run("Func sub(a, b) { return a - b; } print sub(10, 4);")).isEqualTo("6");
  }

  @Test
  @DisplayName("함수 외부에서 return 을 사용하면 SemanticError")
  void returnOutsideFunction() {
    assertThatThrownBy(() -> run("return 5;"))
        .isInstanceOf(SemanticError.class)
        .hasMessageContaining("함수 외부");
  }

  @Test
  @DisplayName("파라미터 이름이 중복되면 SemanticError")
  void duplicateParameter() {
    assertThatThrownBy(() -> run("Func foo(a, a) { return a; }"))
        .isInstanceOf(SemanticError.class)
        .hasMessageContaining("중복");
  }

  @Test
  @DisplayName("함수가 아닌 대상을 호출하면 ExecutionError")
  void callNonFunction() {
    assertThatThrownBy(() -> run("var x = \"hello\"; x();"))
        .isInstanceOf(ExecutionError.class)
        .hasMessageContaining("함수");
  }

  @Test
  @DisplayName("인자 개수가 파라미터 개수와 다르면 ExecutionError")
  void argumentCountMismatch() {
    assertThatThrownBy(() -> run("Func foo(a, b, c) { return a; } foo(1, 2);"))
        .isInstanceOf(ExecutionError.class)
        .hasMessageContaining("인자 개수");
  }
}
