package com.codefab.executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.codefab.error.RuntimeError;

import com.codefab.ast.AssignExpr;
import com.codefab.ast.BinaryExpr;
import com.codefab.ast.BlockStmt;
import com.codefab.ast.ExpressionStmt;
import com.codefab.ast.IfStmt;
import com.codefab.ast.LiteralExpr;
import com.codefab.ast.PrintStmt;
import com.codefab.ast.Stmt;
import com.codefab.ast.UnaryExpr;
import com.codefab.ast.VarDeclareStmt;
import com.codefab.ast.VariableExpr;
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

  @Test
  @DisplayName("문자열 리터럴을 출력하면 따옴표 없이 출력된다")
  void printStringLiteral() {
    // given
    Stmt stmt = new PrintStmt(new LiteralExpr("hello"));

    // when
    executor.execute(List.of(stmt));

    // then
    assertEquals("hello", output());
  }

  @Test
  @DisplayName("true를 출력하면 true가 출력된다")
  void printTrueLiteral() {
    // given
    Stmt stmt = new PrintStmt(new LiteralExpr(true));

    // when
    executor.execute(List.of(stmt));

    // then
    assertEquals("true", output());
  }

  @Test
  @DisplayName("false를 출력하면 false가 출력된다")
  void printFalseLiteral() {
    // given
    Stmt stmt = new PrintStmt(new LiteralExpr(false));

    // when
    executor.execute(List.of(stmt));

    // then
    assertEquals("false", output());
  }

  @Test
  @DisplayName("두 숫자를 더한 결과를 출력한다")
  void printAddition() {
    // given
    Stmt stmt = new PrintStmt(new BinaryExpr(new LiteralExpr(3.0), "+", new LiteralExpr(2.0)));

    // when
    executor.execute(List.of(stmt));

    // then
    assertEquals("5", output());
  }

  @Test
  @DisplayName("두 숫자를 뺀 결과를 출력한다")
  void printSubtraction() {
    // given
    Stmt stmt = new PrintStmt(new BinaryExpr(new LiteralExpr(5.0), "-", new LiteralExpr(2.0)));

    // when
    executor.execute(List.of(stmt));

    // then
    assertEquals("3", output());
  }

  @Test
  @DisplayName("두 숫자를 곱한 결과를 출력한다")
  void printMultiplication() {
    // given
    Stmt stmt = new PrintStmt(new BinaryExpr(new LiteralExpr(3.0), "*", new LiteralExpr(4.0)));

    // when
    executor.execute(List.of(stmt));

    // then
    assertEquals("12", output());
  }

  @Test
  @DisplayName("두 숫자를 나눈 결과를 출력한다")
  void printDivision() {
    // given
    Stmt stmt = new PrintStmt(new BinaryExpr(new LiteralExpr(10.0), "/", new LiteralExpr(2.0)));

    // when
    executor.execute(List.of(stmt));

    // then
    assertEquals("5", output());
  }

  @Test
  @DisplayName("왼쪽이 오른쪽보다 크면 true를 출력한다")
  void printGreaterThanTrue() {
    // given
    Stmt stmt = new PrintStmt(new BinaryExpr(new LiteralExpr(5.0), ">", new LiteralExpr(3.0)));

    // when
    executor.execute(List.of(stmt));

    // then
    assertEquals("true", output());
  }

  @Test
  @DisplayName("왼쪽이 오른쪽보다 작거나 같으면 false를 출력한다")
  void printGreaterThanFalse() {
    // given
    Stmt stmt = new PrintStmt(new BinaryExpr(new LiteralExpr(3.0), ">", new LiteralExpr(5.0)));

    // when
    executor.execute(List.of(stmt));

    // then
    assertEquals("false", output());
  }

  @Test
  @DisplayName("왼쪽이 오른쪽보다 작으면 true를 출력한다")
  void printLessThanTrue() {
    // given
    Stmt stmt = new PrintStmt(new BinaryExpr(new LiteralExpr(3.0), "<", new LiteralExpr(5.0)));

    // when
    executor.execute(List.of(stmt));

    // then
    assertEquals("true", output());
  }

  @Test
  @DisplayName("왼쪽이 오른쪽보다 크거나 같으면 false를 출력한다")
  void printLessThanFalse() {
    // given
    Stmt stmt = new PrintStmt(new BinaryExpr(new LiteralExpr(5.0), "<", new LiteralExpr(3.0)));

    // when
    executor.execute(List.of(stmt));

    // then
    assertEquals("false", output());
  }

  @Test
  @DisplayName("변수를 선언하고 출력하면 초기값이 출력된다")
  void printDeclaredVariable() {
    // given
    Stmt varStmt = new VarDeclareStmt("a", new LiteralExpr(10.0));
    Stmt printStmt = new PrintStmt(new VariableExpr("a"));

    // when
    executor.execute(List.of(varStmt, printStmt));

    // then
    assertEquals("10", output());
  }

  @Test
  @DisplayName("변수를 재할당하면 변경된 값이 출력된다")
  void printReassignedVariable() {
    // given
    Stmt varStmt = new VarDeclareStmt("a", new LiteralExpr(10.0));
    Stmt assignStmt = new ExpressionStmt(new AssignExpr("a", new LiteralExpr(99.0)));
    Stmt printStmt = new PrintStmt(new VariableExpr("a"));

    // when
    executor.execute(List.of(varStmt, assignStmt, printStmt));

    // then
    assertEquals("99", output());
  }

  @Test
  @DisplayName("블록 안에서 외부 변수에 접근할 수 있다")
  void blockCanAccessOuterVariable() {
    // given
    Stmt varStmt = new VarDeclareStmt("a", new LiteralExpr(5.0));
    Stmt block = new BlockStmt(List.of(
        new PrintStmt(new VariableExpr("a"))
    ));

    // when
    executor.execute(List.of(varStmt, block));

    // then
    assertEquals("5", output());
  }

  @Test
  @DisplayName("블록 안에서 선언한 변수는 블록 밖에서 접근할 수 없다")
  void blockVariableNotAccessibleOutside() {
    // given
    Stmt block = new BlockStmt(List.of(
        new VarDeclareStmt("a", new LiteralExpr(5.0))
    ));

    // when & then
    executor.execute(List.of(block));
    assertThrows(RuntimeError.class, () -> executor.execute(List.of(
        new PrintStmt(new VariableExpr("a"))
    )));
  }

  @Test
  @DisplayName("조건이 참이면 then 블록을 실행한다")
  void ifExecutesThenWhenTrue() {
    // given
    Stmt ifStmt = new IfStmt(
        new LiteralExpr(true),
        new PrintStmt(new LiteralExpr("then")),
        null
    );

    // when
    executor.execute(List.of(ifStmt));

    // then
    assertEquals("then", output());
  }

  @Test
  @DisplayName("조건이 거짓이면 else 블록을 실행한다")
  void ifExecutesElseWhenFalse() {
    // given
    Stmt ifStmt = new IfStmt(
        new LiteralExpr(false),
        new PrintStmt(new LiteralExpr("then")),
        new PrintStmt(new LiteralExpr("else"))
    );

    // when
    executor.execute(List.of(ifStmt));

    // then
    assertEquals("else", output());
  }

  @Test
  @DisplayName("조건이 거짓이고 else가 없으면 아무것도 실행하지 않는다")
  void ifDoesNothingWhenFalseAndNoElse() {
    // given
    Stmt ifStmt = new IfStmt(
        new LiteralExpr(false),
        new PrintStmt(new LiteralExpr("then")),
        null
    );

    // when
    executor.execute(List.of(ifStmt));

    // then
    assertEquals("", output());
  }

  @Test
  @DisplayName("숫자에 단항 minus를 적용하면 부호가 반전된다")
  void unaryMinusNegatesNumber() {
    // given
    Stmt stmt = new PrintStmt(new UnaryExpr("-", new LiteralExpr(5.0)));

    // when
    executor.execute(List.of(stmt));

    // then
    assertEquals("-5", output());
  }

  @Test
  @DisplayName("true에 논리 not을 적용하면 false가 출력된다")
  void unaryNotNegatesBoolean() {
    // given
    Stmt stmt = new PrintStmt(new UnaryExpr("!", new LiteralExpr(true)));

    // when
    executor.execute(List.of(stmt));

    // then
    assertEquals("false", output());
  }
}
