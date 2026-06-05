package com.codefab.checker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.codefab.ast.Expr;
import com.codefab.ast.Stmt;
import com.codefab.executor.Environment;
import com.codefab.token.Token;
import com.codefab.token.TokenType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("정적 바인딩 (실행 전 거리 계산) 테스트")
class StaticBindingTest {

  private Token id(String name) {
    return new Token(TokenType.IDENTIFIER, name, null, 1);
  }

  @Test
  @DisplayName("Checker 는 중첩 스코프에서 변수까지의 거리(distance)를 미리 계산한다")
  void checkerPrecomputesDistance() {
    // { var a = 1; { { a; } } }  -> 가장 안쪽 a 참조는 distance 2
    Expr.Variable ref = new Expr.Variable(id("a"));
    Stmt innermost = new Stmt.Block(List.of(new Stmt.Expression(ref)));
    Stmt middle = new Stmt.Block(List.of(innermost));
    Stmt outer = new Stmt.Block(List.of(
        new Stmt.VarDeclare(id("a"), new Expr.Literal(1.0)),
        middle));

    Checker checker = new Checker();
    checker.check(List.of(outer));

    assertThat(checker.getLocals()).containsEntry(ref, 2);
  }

  @Test
  @DisplayName("Test Double 검증: 계산된 거리로 즉시 접근하며 스코프를 거슬러 올라가지 않는다")
  void resolvedAccessJumpsDirectlyWithoutWalkingChain() {
    Environment global = new Environment();
    global.define("a", 10.0);
    Environment middle = new Environment(global);
    Environment inner = new Environment(middle);

    Environment spy = spy(inner);

    // 거리 2 로 즉시 접근 (정적 바인딩)
    Object value = spy.getAt(2, "a");

    assertThat(value).isEqualTo(10.0);
    verify(spy).ancestor(2);                 // 미리 계산된 거리로 한 번에 점프
    verify(spy, never()).get(any(Token.class)); // 체인을 따라 거슬러 올라가지 않음
  }
}
