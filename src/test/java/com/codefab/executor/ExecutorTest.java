package com.codefab.executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.codefab.ast.Expr;
import com.codefab.ast.Stmt;
import com.codefab.error.ExecutionError;
import com.codefab.token.Token;
import com.codefab.token.TokenType;
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
    return outputStream.toString().trim().replace("\r\n", "\n");
  }

  private Token token(TokenType type, String origin) {
    return new Token(type, origin, null, 1);
  }

  @Test
  @DisplayName("숫자 리터럴을 출력하면 정수 형태로 출력된다")
  void printNumberLiteral() {
    // given
    Stmt stmt = new Stmt.Print(new Expr.Literal(3.0));

    // when
    executor.execute(List.of(stmt));

    // then
    assertEquals("3", output());
  }

  @Test
  @DisplayName("문자열 리터럴을 출력하면 따옴표 없이 출력된다")
  void printStringLiteral() {
    // given
    Stmt stmt = new Stmt.Print(new Expr.Literal("hello"));

    // when
    executor.execute(List.of(stmt));

    // then
    assertEquals("hello", output());
  }

  @Test
  @DisplayName("true를 출력하면 true가 출력된다")
  void printTrueLiteral() {
    // given
    Stmt stmt = new Stmt.Print(new Expr.Literal(true));

    // when
    executor.execute(List.of(stmt));

    // then
    assertEquals("true", output());
  }

  @Test
  @DisplayName("false를 출력하면 false가 출력된다")
  void printFalseLiteral() {
    // given
    Stmt stmt = new Stmt.Print(new Expr.Literal(false));

    // when
    executor.execute(List.of(stmt));

    // then
    assertEquals("false", output());
  }

  @Test
  @DisplayName("두 숫자를 더한 결과를 출력한다")
  void printAddition() {
    // given
    Stmt stmt = new Stmt.Print(
        new Expr.Binary(new Expr.Literal(3.0), token(TokenType.PLUS, "+"), new Expr.Literal(2.0))
    );

    // when
    executor.execute(List.of(stmt));

    // then
    assertEquals("5", output());
  }

  @Test
  @DisplayName("두 숫자를 뺀 결과를 출력한다")
  void printSubtraction() {
    // given
    Stmt stmt = new Stmt.Print(
        new Expr.Binary(new Expr.Literal(5.0), token(TokenType.MINUS, "-"), new Expr.Literal(2.0))
    );

    // when
    executor.execute(List.of(stmt));

    // then
    assertEquals("3", output());
  }

  @Test
  @DisplayName("두 숫자를 곱한 결과를 출력한다")
  void printMultiplication() {
    // given
    Stmt stmt = new Stmt.Print(
        new Expr.Binary(new Expr.Literal(3.0), token(TokenType.STAR, "*"), new Expr.Literal(4.0))
    );

    // when
    executor.execute(List.of(stmt));

    // then
    assertEquals("12", output());
  }

  @Test
  @DisplayName("두 숫자를 나눈 결과를 출력한다")
  void printDivision() {
    // given
    Stmt stmt = new Stmt.Print(
        new Expr.Binary(new Expr.Literal(10.0), token(TokenType.SLASH, "/"), new Expr.Literal(2.0))
    );

    // when
    executor.execute(List.of(stmt));

    // then
    assertEquals("5", output());
  }

  @Test
  @DisplayName("왼쪽이 오른쪽보다 크면 true를 출력한다")
  void printGreaterThanTrue() {
    // given
    Stmt stmt = new Stmt.Print(
        new Expr.Binary(new Expr.Literal(5.0), token(TokenType.GREATER, ">"), new Expr.Literal(3.0))
    );

    // when
    executor.execute(List.of(stmt));

    // then
    assertEquals("true", output());
  }

  @Test
  @DisplayName("왼쪽이 오른쪽보다 작거나 같으면 false를 출력한다")
  void printGreaterThanFalse() {
    // given
    Stmt stmt = new Stmt.Print(
        new Expr.Binary(new Expr.Literal(3.0), token(TokenType.GREATER, ">"), new Expr.Literal(5.0))
    );

    // when
    executor.execute(List.of(stmt));

    // then
    assertEquals("false", output());
  }

  @Test
  @DisplayName("왼쪽이 오른쪽보다 작으면 true를 출력한다")
  void printLessThanTrue() {
    // given
    Stmt stmt = new Stmt.Print(
        new Expr.Binary(new Expr.Literal(3.0), token(TokenType.LESS, "<"), new Expr.Literal(5.0))
    );

    // when
    executor.execute(List.of(stmt));

    // then
    assertEquals("true", output());
  }

  @Test
  @DisplayName("왼쪽이 오른쪽보다 크거나 같으면 false를 출력한다")
  void printLessThanFalse() {
    // given
    Stmt stmt = new Stmt.Print(
        new Expr.Binary(new Expr.Literal(5.0), token(TokenType.LESS, "<"), new Expr.Literal(3.0))
    );

    // when
    executor.execute(List.of(stmt));

    // then
    assertEquals("false", output());
  }

  @Test
  @DisplayName("변수를 선언하고 출력하면 초기값이 출력된다")
  void printDeclaredVariable() {
    // given
    Stmt varStmt = new Stmt.VarDeclare(token(TokenType.IDENTIFIER, "a"), new Expr.Literal(10.0));
    Stmt printStmt = new Stmt.Print(new Expr.Variable(token(TokenType.IDENTIFIER, "a")));

    // when
    executor.execute(List.of(varStmt, printStmt));

    // then
    assertEquals("10", output());
  }

  @Test
  @DisplayName("변수를 재할당하면 변경된 값이 출력된다")
  void printReassignedVariable() {
    // given
    Stmt varStmt = new Stmt.VarDeclare(token(TokenType.IDENTIFIER, "a"), new Expr.Literal(10.0));
    Stmt assignStmt = new Stmt.Expression(
        new Expr.Assign(token(TokenType.IDENTIFIER, "a"), new Expr.Literal(99.0))
    );
    Stmt printStmt = new Stmt.Print(new Expr.Variable(token(TokenType.IDENTIFIER, "a")));

    // when
    executor.execute(List.of(varStmt, assignStmt, printStmt));

    // then
    assertEquals("99", output());
  }

  @Test
  @DisplayName("블록 안에서 외부 변수에 접근할 수 있다")
  void blockCanAccessOuterVariable() {
    // given
    Stmt varStmt = new Stmt.VarDeclare(token(TokenType.IDENTIFIER, "a"), new Expr.Literal(5.0));
    Stmt block = new Stmt.Block(List.of(
        new Stmt.Print(new Expr.Variable(token(TokenType.IDENTIFIER, "a")))
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
    Stmt block = new Stmt.Block(List.of(
        new Stmt.VarDeclare(token(TokenType.IDENTIFIER, "a"), new Expr.Literal(5.0))
    ));

    // when & then
    executor.execute(List.of(block));
    assertThrows(ExecutionError.class, () -> executor.execute(List.of(
        new Stmt.Print(new Expr.Variable(token(TokenType.IDENTIFIER, "a")))
    )));
  }

  @Test
  @DisplayName("조건이 참이면 then 블록을 실행한다")
  void ifExecutesThenWhenTrue() {
    // given
    Stmt ifStmt = new Stmt.If(
        new Expr.Literal(true),
        new Stmt.Print(new Expr.Literal("then")),
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
    Stmt ifStmt = new Stmt.If(
        new Expr.Literal(false),
        new Stmt.Print(new Expr.Literal("then")),
        new Stmt.Print(new Expr.Literal("else"))
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
    Stmt ifStmt = new Stmt.If(
        new Expr.Literal(false),
        new Stmt.Print(new Expr.Literal("then")),
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
    Stmt stmt = new Stmt.Print(new Expr.Unary(token(TokenType.MINUS, "-"), new Expr.Literal(5.0)));

    // when
    executor.execute(List.of(stmt));

    // then
    assertEquals("-5", output());
  }

  @Test
  @DisplayName("true에 논리 not을 적용하면 false가 출력된다")
  void unaryNotNegatesBoolean() {
    // given
    Stmt stmt = new Stmt.Print(new Expr.Unary(token(TokenType.BANG, "!"), new Expr.Literal(true)));

    // when
    executor.execute(List.of(stmt));

    // then
    assertEquals("false", output());
  }

  @Test
  @DisplayName("괄호로 묶인 표현식을 평가한다")
  void evaluatesGroupingExpr() {
    // given
    // (2 + 3) * 4 = 20
    Stmt stmt = new Stmt.Print(
        new Expr.Binary(
            new Expr.Grouping(
                new Expr.Binary(new Expr.Literal(2.0), token(TokenType.PLUS, "+"), new Expr.Literal(3.0))
            ),
            token(TokenType.STAR, "*"),
            new Expr.Literal(4.0)
        )
    );

    // when
    executor.execute(List.of(stmt));

    // then
    assertEquals("20", output());
  }

  @Test
  @DisplayName("true and true 는 true 를 반환한다")
  void andReturnsTrueWhenBothTrue() {
    // given
    Stmt stmt = new Stmt.Print(
        new Expr.Logical(new Expr.Literal(true), token(TokenType.AND, "and"), new Expr.Literal(true))
    );

    // when
    executor.execute(List.of(stmt));

    // then
    assertEquals("true", output());
  }

  @Test
  @DisplayName("false and 는 오른쪽을 평가하지 않고 false 를 반환한다")
  void andShortCircuitsOnFalse() {
    // given
    Stmt stmt = new Stmt.Print(
        new Expr.Logical(new Expr.Literal(false), token(TokenType.AND, "and"), new Expr.Literal(true))
    );

    // when
    executor.execute(List.of(stmt));

    // then
    assertEquals("false", output());
  }

  @Test
  @DisplayName("true or 는 오른쪽을 평가하지 않고 true 를 반환한다")
  void orShortCircuitsOnTrue() {
    // given
    Stmt stmt = new Stmt.Print(
        new Expr.Logical(new Expr.Literal(true), token(TokenType.OR, "or"), new Expr.Literal(false))
    );

    // when
    executor.execute(List.of(stmt));

    // then
    assertEquals("true", output());
  }

  @Test
  @DisplayName("false or false 는 false 를 반환한다")
  void orReturnsFalseWhenBothFalse() {
    // given
    Stmt stmt = new Stmt.Print(
        new Expr.Logical(new Expr.Literal(false), token(TokenType.OR, "or"), new Expr.Literal(false))
    );

    // when
    executor.execute(List.of(stmt));

    // then
    assertEquals("false", output());
  }

  @Test
  @DisplayName("for 문은 조건이 참인 동안 body 를 반복 실행한다")
  void forLoopExecutesBodyWhileConditionIsTrue() {
    // given
    // for (var i = 0; i < 3; i = i + 1) { print i; }
    Stmt forStmt = new Stmt.For(
        new Stmt.VarDeclare(token(TokenType.IDENTIFIER, "i"), new Expr.Literal(0.0)),
        new Expr.Binary(new Expr.Variable(token(TokenType.IDENTIFIER, "i")), token(TokenType.LESS, "<"), new Expr.Literal(3.0)),
        new Expr.Assign(token(TokenType.IDENTIFIER, "i"),
            new Expr.Binary(new Expr.Variable(token(TokenType.IDENTIFIER, "i")), token(TokenType.PLUS, "+"), new Expr.Literal(1.0))),
        new Stmt.Block(List.of(new Stmt.Print(new Expr.Variable(token(TokenType.IDENTIFIER, "i")))))
    );

    // when
    executor.execute(List.of(forStmt));

    // then
    assertEquals("0\n1\n2", output());
  }

  @Test
  @DisplayName("for 문은 처음부터 조건이 거짓이면 body 를 실행하지 않는다")
  void forLoopSkipsBodyWhenConditionIsFalseFromStart() {
    // given
    Stmt forStmt = new Stmt.For(
        new Stmt.VarDeclare(token(TokenType.IDENTIFIER, "i"), new Expr.Literal(0.0)),
        new Expr.Binary(new Expr.Variable(token(TokenType.IDENTIFIER, "i")), token(TokenType.LESS, "<"), new Expr.Literal(0.0)),
        new Expr.Assign(token(TokenType.IDENTIFIER, "i"),
            new Expr.Binary(new Expr.Variable(token(TokenType.IDENTIFIER, "i")), token(TokenType.PLUS, "+"), new Expr.Literal(1.0))),
        new Stmt.Block(List.of(new Stmt.Print(new Expr.Variable(token(TokenType.IDENTIFIER, "i")))))
    );

    // when
    executor.execute(List.of(forStmt));

    // then
    assertEquals("", output());
  }

  @Test
  @DisplayName("두 문자열을 + 로 연결하면 이어붙인 결과가 출력된다")
  void printStringConcatenation() {
    // given
    Stmt stmt = new Stmt.Print(
        new Expr.Binary(new Expr.Literal("Hello"), token(TokenType.PLUS, "+"), new Expr.Literal(" World"))
    );

    // when
    executor.execute(List.of(stmt));

    // then
    assertEquals("Hello World", output());
  }

  @Test
  @DisplayName("중첩 스코프에서 바깥 변수와 안쪽 변수를 문자열 연결해 출력한다")
  void nestedScopeStringConcatenation() {
    // given
    // var outer = "A";
    // { var inner = "B"; { print outer + inner; } }
    Stmt outerDecl = new Stmt.VarDeclare(token(TokenType.IDENTIFIER, "outer"), new Expr.Literal("A"));
    Stmt innerBlock = new Stmt.Block(List.of(
        new Stmt.VarDeclare(token(TokenType.IDENTIFIER, "inner"), new Expr.Literal("B")),
        new Stmt.Block(List.of(
            new Stmt.Print(new Expr.Binary(
                new Expr.Variable(token(TokenType.IDENTIFIER, "outer")),
                token(TokenType.PLUS, "+"),
                new Expr.Variable(token(TokenType.IDENTIFIER, "inner"))
            ))
        ))
    ));

    // when
    executor.execute(List.of(outerDecl, innerBlock));

    // then
    assertEquals("AB", output());
  }

  @Test
  @DisplayName("숫자가 아닌 값에 산술 연산을 하면 ExecutionError 가 발생한다")
  void arithmeticOnNonNumberThrowsExecutionError() {
    // given
    Stmt stmt = new Stmt.Print(
        new Expr.Binary(new Expr.Literal("hello"), token(TokenType.MINUS, "-"), new Expr.Literal(1.0))
    );

    // when & then
    assertThrows(ExecutionError.class, () -> executor.execute(List.of(stmt)));
  }

  @Test
  @DisplayName("0 으로 나누면 ExecutionError 가 발생한다")
  void divisionByZeroThrowsExecutionError() {
    // given
    Stmt stmt = new Stmt.Print(
        new Expr.Binary(new Expr.Literal(10.0), token(TokenType.SLASH, "/"), new Expr.Literal(0.0))
    );

    // when & then
    assertThrows(ExecutionError.class, () -> executor.execute(List.of(stmt)));
  }

  @Test
  @DisplayName("선언하지 않은 변수를 참조하면 ExecutionError 가 발생한다")
  void undefinedVariableThrowsExecutionError() {
    // given
    Stmt stmt = new Stmt.Print(new Expr.Variable(token(TokenType.IDENTIFIER, "x")));

    // when & then
    assertThrows(ExecutionError.class, () -> executor.execute(List.of(stmt)));
  }

  // ── 정적 바인딩 테스트 ────────────────────────────────────────────────────

  static class TrackingEnvironment extends Environment {
    int getAtCallCount = 0;
    int getDynamicCallCount = 0;

    @Override
    public Object getAt(int distance, String name) {
      getAtCallCount++;
      return super.getAt(distance, name);
    }

    @Override
    public Object get(com.codefab.token.Token name) {
      getDynamicCallCount++;
      return super.get(name);
    }
  }

  @Test
  @DisplayName("정적 바인딩 적용 시 locals 맵에 변수 참조 distance 가 기록된다")
  void staticBinding_localsMapContainsDistanceForVariableRef() {
    // given
    // var a = 1; { print a; }
    Expr.Variable varRef = new Expr.Variable(token(TokenType.IDENTIFIER, "a"));
    Stmt varDecl = new Stmt.VarDeclare(token(TokenType.IDENTIFIER, "a"), new Expr.Literal(1.0));
    Stmt block = new Stmt.Block(List.of(new Stmt.Print(varRef)));
    List<Stmt> stmts = List.of(varDecl, block);

    com.codefab.checker.Checker checker = new com.codefab.checker.Checker();
    checker.check(stmts);

    // when
    executor.resolve(checker.getLocals());
    executor.execute(stmts);

    // then: locals 에 distance 가 기록되어 동적 탐색 없이 즉시 접근 가능
    assertTrue(checker.getLocals().containsKey(varRef), "varRef 의 distance 가 locals 에 기록되어야 한다");
    assertEquals(1, checker.getLocals().get(varRef), "한 단계 바깥 변수의 distance 는 1 이어야 한다");
    assertEquals("1", output());
  }

  @Test
  @DisplayName("정적 바인딩으로 깊은 중첩 스코프에서 바깥 변수에 올바르게 접근한다")
  void staticBinding_deepNestedScopeAccessesOuterVariable() {
    // given
    // var a = 42; { { { { { print a; } } } } }
    Expr.Variable varRef = new Expr.Variable(token(TokenType.IDENTIFIER, "a"));
    Stmt print = new Stmt.Print(varRef);
    Stmt nested = new Stmt.Block(List.of(print));
    for (int i = 0; i < 4; i++) nested = new Stmt.Block(List.of(nested));
    Stmt varDecl = new Stmt.VarDeclare(token(TokenType.IDENTIFIER, "a"), new Expr.Literal(42.0));
    List<Stmt> stmts = List.of(varDecl, nested);

    com.codefab.checker.Checker checker = new com.codefab.checker.Checker();
    checker.check(stmts);
    executor.resolve(checker.getLocals());

    // when
    executor.execute(stmts);

    // then
    assertEquals("42", output());
  }

  @Test
  @DisplayName("정적 바인딩으로 중첩 스코프에서 바깥 변수 수정이 올바르게 반영된다")
  void staticBinding_innerScopeModifiesOuterVariable() {
    // given
    // var count = 0; { { count = 99; } } print count;
    Expr.Assign assign = new Expr.Assign(
        token(TokenType.IDENTIFIER, "count"), new Expr.Literal(99.0));
    Stmt innerBlock = new Stmt.Block(List.of(new Stmt.Block(List.of(new Stmt.Expression(assign)))));
    Stmt varDecl = new Stmt.VarDeclare(token(TokenType.IDENTIFIER, "count"), new Expr.Literal(0.0));
    Expr.Variable varRef = new Expr.Variable(token(TokenType.IDENTIFIER, "count"));
    Stmt print = new Stmt.Print(varRef);
    List<Stmt> stmts = List.of(varDecl, innerBlock, print);

    com.codefab.checker.Checker checker = new com.codefab.checker.Checker();
    checker.check(stmts);
    executor.resolve(checker.getLocals());

    // when
    executor.execute(stmts);

    // then
    assertEquals("99", output());
  }
}
