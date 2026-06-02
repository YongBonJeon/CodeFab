package com.codefab.executor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.codefab.ast.LiteralExpr;
import com.codefab.ast.PrintStmt;
import com.codefab.ast.Stmt;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Executor Unit 테스트")
class ExecutorTest {

  private ByteArrayOutputStream outputStream;
  private Executor executor;

  @BeforeEach
  void setUp() {
    outputStream = new ByteArrayOutputStream();
    executor = new Executor(new PrintStream(outputStream));
  }

  private String output() {
    return outputStream.toString().trim();
  }

  @Test
  @DisplayName("숫자 리터럴을 출력하면 정수 형태로 출력된다")
  void printNumberLiteral() {
    // given
    Stmt stmt = new PrintStmt(new LiteralExpr(3.0));

    // when
    executor.execute(List.of(stmt));

    // then
    assertEquals("3", output());
  }
}
