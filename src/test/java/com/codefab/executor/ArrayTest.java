package com.codefab.executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codefab.assembler.Parser;
import com.codefab.assembler.Tokenizer;
import com.codefab.ast.Stmt;
import com.codefab.checker.Checker;
import com.codefab.error.ExecutionError;
import com.codefab.optimizer.Optimizer;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("정적 배열 통합 테스트")
class ArrayTest {

  private String run(String source) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Executor executor = new Executor(new PrintStream(out, true, StandardCharsets.UTF_8));
    List<Stmt> stmts = new Optimizer().optimize(new Parser(new Tokenizer(source).tokenize()).parse());
    Checker checker = new Checker();
    checker.check(stmts);
    executor.setLocals(checker.getLocals());
    executor.execute(stmts);
    return out.toString(StandardCharsets.UTF_8).trim().replace("\r\n", "\n");
  }

  @Test
  @DisplayName("Array(n) 은 크기 n 의 null 로 채워진 배열을 만든다")
  void createsNullFilledArray() {
    assertThat(run("var a = Array(3); print a;")).isEqualTo("[null, null, null]");
  }

  @Test
  @DisplayName("인덱스로 값을 쓰고 읽는다")
  void readWriteByIndex() {
    assertThat(run("var a = Array(3); a[0] = 10; a[1] = 20; a[2] = 30; print a[0];"))
        .isEqualTo("10");
  }

  @Test
  @DisplayName("인덱스로 표현식(계산식)을 사용할 수 있다")
  void indexCanBeExpression() {
    assertThat(run("var a = Array(3); var i = 2; a[i - 1] = 7; print a[1];")).isEqualTo("7");
  }

  @Test
  @DisplayName("범위를 벗어난 인덱스 접근은 ExecutionError")
  void outOfRange() {
    assertThatThrownBy(() -> run("var a = Array(3); print a[5];"))
        .isInstanceOf(ExecutionError.class)
        .hasMessageContaining("범위");
  }

  @Test
  @DisplayName("숫자가 아닌 인덱스는 ExecutionError")
  void nonNumberIndex() {
    assertThatThrownBy(() -> run("var a = Array(3); print a[\"hello\"];"))
        .isInstanceOf(ExecutionError.class)
        .hasMessageContaining("인덱스");
  }

  @Test
  @DisplayName("배열이 아닌 값에 인덱스 접근하면 ExecutionError")
  void indexOnNonArray() {
    assertThatThrownBy(() -> run("var x = 10; print x[0];"))
        .isInstanceOf(ExecutionError.class)
        .hasMessageContaining("배열");
  }

  @Test
  @DisplayName("배열 크기로 숫자가 아닌 값을 주면 ExecutionError")
  void nonNumberSize() {
    assertThatThrownBy(() -> run("var b = Array(\"hi\");"))
        .isInstanceOf(ExecutionError.class)
        .hasMessageContaining("크기");
  }
}
