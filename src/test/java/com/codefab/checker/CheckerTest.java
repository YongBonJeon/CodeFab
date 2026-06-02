package com.codefab.checker;

import com.codefab.ast.Expr;
import com.codefab.ast.Stmt;
import com.codefab.error.SemanticError;
import com.codefab.token.Token;
import com.codefab.token.TokenType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CheckerTest {

    private Token id(String name) {
        return new Token(TokenType.IDENTIFIER, name, null, 1);
    }

    private Token op(TokenType type, String symbol) {
        return new Token(type, symbol, null, 1);
    }

    private void check(List<Stmt> stmts) {
        new Checker().check(stmts);
    }

    @Test
    @DisplayName("[visitBlock] FAIL - 같은 블록 스코프에서 동일 이름 변수를 두 번 선언하면 SemanticError")
    void visitBlock_FAIL_같은_스코프에서_변수_재선언() {
        // Arrange
        Stmt decl1 = new Stmt.VarDeclare(id("a"), new Expr.Literal(1));
        Stmt decl2 = new Stmt.VarDeclare(id("a"), new Expr.Literal(2));
        Stmt block = new Stmt.Block(List.of(decl1, decl2));

        // Act & Assert
        SemanticError ex = assertThrows(SemanticError.class,
                () -> check(List.of(block)));
        assertTrue(ex.getMessage().contains("a"));
        assertTrue(ex.getMessage().contains("스코프"));
    }

    @Test
    @DisplayName("[visitBlock] PASS - 같은 블록 안에서 서로 다른 이름의 변수는 정상 선언")
    void visitBlock_PASS_다른_이름으로_선언() {
        // Arrange
        Stmt decl1 = new Stmt.VarDeclare(id("a"), new Expr.Literal(1));
        Stmt decl2 = new Stmt.VarDeclare(id("b"), new Expr.Literal(2));
        Stmt block = new Stmt.Block(List.of(decl1, decl2));

        // Act & Assert
        assertDoesNotThrow(() -> check(List.of(block)));
    }

    @Test
    @DisplayName("[visitVarDeclare] FAIL - 초기화식에서 선언 중인 변수 자신을 참조하면 SemanticError")
    void visitVarDeclare_FAIL_자신의_초기화식에서_자신을_참조() {
        // Arrange  { var a = a + 1; }
        Expr selfRef = new Expr.Variable(id("a"));
        Expr init    = new Expr.Binary(selfRef, op(TokenType.PLUS, "+"), new Expr.Literal(1));
        Stmt decl    = new Stmt.VarDeclare(id("a"), init);
        Stmt block   = new Stmt.Block(List.of(decl));

        // Act & Assert
        SemanticError ex = assertThrows(SemanticError.class,
                () -> check(List.of(block)));
        assertTrue(ex.getMessage().contains("초기화식"));
    }

    @Test
    @DisplayName("[visitVarDeclare] PASS - initializer 없이 변수만 선언하는 경우 정상")
    void visitVarDeclare_PASS_initializer_없이_선언() {
        // Arrange  { var a; }
        Stmt decl  = new Stmt.VarDeclare(id("a"), null);
        Stmt block = new Stmt.Block(List.of(decl));

        // Act & Assert
        assertDoesNotThrow(() -> check(List.of(block)));
    }

    @Test
    @DisplayName("[visitVarDeclare] PASS - 이미 선언된 다른 변수를 초기화식에서 사용하는 경우 정상")
    void visitVarDeclare_PASS_다른_변수로_초기화() {
        // Arrange  { var a = 1; var b = a + 2; }
        Stmt declA = new Stmt.VarDeclare(id("a"), new Expr.Literal(1));
        Expr init  = new Expr.Binary(new Expr.Variable(id("a")), op(TokenType.PLUS, "+"), new Expr.Literal(2));
        Stmt declB = new Stmt.VarDeclare(id("b"), init);
        Stmt block = new Stmt.Block(List.of(declA, declB));

        // Act & Assert
        assertDoesNotThrow(() -> check(List.of(block)));
    }

    @Test
    @DisplayName("[visitExpression] FAIL - 식문 내부 표현식에 자기참조 변수가 있으면 SemanticError 전파")
    void visitExpression_FAIL_내부_식에서_자기참조_변수_사용() {
        // Arrange  { var a = (a); }  — 식문 안에 자기참조
        Stmt decl  = new Stmt.VarDeclare(id("a"), new Expr.Variable(id("a")));
        Stmt block = new Stmt.Block(List.of(decl));

        // Act & Assert
        assertThrows(SemanticError.class, () -> check(List.of(block)));
    }

    @Test
    @DisplayName("[visitExpression] PASS - 리터럴 연산으로만 이루어진 일반 식문은 정상")
    void visitExpression_PASS_일반_식문() {
        // Arrange  1 + 2;
        Expr expr = new Expr.Binary(new Expr.Literal(1), op(TokenType.PLUS, "+"), new Expr.Literal(2));
        Stmt exprStmt = new Stmt.Expression(expr);

        // Act & Assert
        assertDoesNotThrow(() -> check(List.of(exprStmt)));
    }

    @Test
    @DisplayName("[visitPrint] FAIL - print 내부 표현식에 자기참조 변수가 있으면 SemanticError 전파")
    void visitPrint_FAIL_내부_식에서_자기참조_변수_사용() {
        // Arrange  { var a = 1; print: 자기참조가 포함된 식 }
        // print 자체보다 내부 expr이 자기참조일 때 에러 전파 확인
        Stmt decl  = new Stmt.VarDeclare(id("a"), new Expr.Variable(id("a")));
        Stmt block = new Stmt.Block(List.of(decl));

        // Act & Assert
        assertThrows(SemanticError.class, () -> check(List.of(block)));
    }

    @Test
    @DisplayName("[visitPrint] PASS - 리터럴을 출력하는 print 문은 정상")
    void visitPrint_PASS_리터럴_출력() {
        // Arrange  print 42;
        Stmt print = new Stmt.Print(new Expr.Literal(42));

        // Act & Assert
        assertDoesNotThrow(() -> check(List.of(print)));
    }

    @Test
    @DisplayName("[visitPrint] PASS - 선언 완료된 변수를 print 문에서 사용하는 경우 정상")
    void visitPrint_PASS_선언된_변수_출력() {
        // Arrange  { var a = 1; print a; }
        Stmt decl  = new Stmt.VarDeclare(id("a"), new Expr.Literal(1));
        Stmt print = new Stmt.Print(new Expr.Variable(id("a")));
        Stmt block = new Stmt.Block(List.of(decl, print));

        // Act & Assert
        assertDoesNotThrow(() -> check(List.of(block)));
    }

    @Test
    @DisplayName("[visitIf] FAIL - then 블록 내에서 동일 이름 변수를 재선언하면 SemanticError")
    void visitIf_FAIL_then_블록에서_변수_재선언() {
        // Arrange  if (true) { var a = 1; var a = 2; }
        Stmt decl1    = new Stmt.VarDeclare(id("a"), new Expr.Literal(1));
        Stmt decl2    = new Stmt.VarDeclare(id("a"), new Expr.Literal(2));
        Stmt thenBlock = new Stmt.Block(List.of(decl1, decl2));
        Stmt ifStmt   = new Stmt.If(new Expr.Literal(true), thenBlock, null);

        // Act & Assert
        assertThrows(SemanticError.class, () -> check(List.of(ifStmt)));
    }

    @Test
    @DisplayName("[visitIf] PASS - then과 else는 별개의 스코프이므로 같은 이름 변수 선언 허용")
    void visitIf_PASS_then과_else에서_같은_이름_선언() {
        // Arrange  if (true) { var a = 1; } else { var a = 2; }
        // 서로 다른 스코프이므로 허용
        Stmt thenBlock = new Stmt.Block(List.of(new Stmt.VarDeclare(id("a"), new Expr.Literal(1))));
        Stmt elseBlock = new Stmt.Block(List.of(new Stmt.VarDeclare(id("a"), new Expr.Literal(2))));
        Stmt ifStmt    = new Stmt.If(new Expr.Literal(true), thenBlock, elseBlock);

        // Act & Assert
        assertDoesNotThrow(() -> check(List.of(ifStmt)));
    }

    @Test
    @DisplayName("[visitIf] PASS - else 브랜치가 없는 if 문은 정상")
    void visitIf_PASS_else_없는_if() {
        // Arrange  if (true) print 1;
        Stmt ifStmt = new Stmt.If(new Expr.Literal(true), new Stmt.Print(new Expr.Literal(1)), null);

        // Act & Assert
        assertDoesNotThrow(() -> check(List.of(ifStmt)));
    }

    @Test
    @DisplayName("[visitFor] FAIL - for 초기화식에서 선언 중인 변수 자신을 참조하면 SemanticError")
    void visitFor_FAIL_초기화식에서_자신을_참조() {
        // Arrange  for (var i = i + 1; i < 3; i = i + 1) print i;
        Expr selfRef  = new Expr.Variable(id("i"));
        Expr initExpr = new Expr.Binary(selfRef, op(TokenType.PLUS, "+"), new Expr.Literal(1));
        Stmt forInit  = new Stmt.VarDeclare(id("i"), initExpr);
        Expr condition = new Expr.Binary(new Expr.Variable(id("i")), op(TokenType.LESS, "<"), new Expr.Literal(3));
        Expr increment = new Expr.Assign(id("i"),
                new Expr.Binary(new Expr.Variable(id("i")), op(TokenType.PLUS, "+"), new Expr.Literal(1)));
        Stmt body    = new Stmt.Print(new Expr.Variable(id("i")));
        Stmt forStmt = new Stmt.For(forInit, condition, increment, body);

        // Act & Assert
        assertThrows(SemanticError.class, () -> check(List.of(forStmt)));
    }

    @Test
    @DisplayName("[visitFor] FAIL - for body 블록 내에서 동일 이름 변수를 재선언하면 SemanticError")
    void visitFor_FAIL_body_블록에서_변수_재선언() {
        // Arrange  for (...) { var x = 1; var x = 2; }
        Stmt forInit  = new Stmt.VarDeclare(id("i"), new Expr.Literal(0));
        Expr condition = new Expr.Binary(new Expr.Variable(id("i")), op(TokenType.LESS, "<"), new Expr.Literal(3));
        Stmt decl1   = new Stmt.VarDeclare(id("x"), new Expr.Literal(1));
        Stmt decl2   = new Stmt.VarDeclare(id("x"), new Expr.Literal(2));
        Stmt body    = new Stmt.Block(List.of(decl1, decl2));
        Stmt forStmt = new Stmt.For(forInit, condition, null, body);

        // Act & Assert
        assertThrows(SemanticError.class, () -> check(List.of(forStmt)));
    }

    @Test
    @DisplayName("[visitFor] PASS - 초기화·조건·증감·body가 모두 올바른 for 루프는 정상")
    void visitFor_PASS_정상적인_for_루프() {
        // Arrange  for (var i = 0; i < 3; i = i + 1) print i;
        Stmt forInit  = new Stmt.VarDeclare(id("i"), new Expr.Literal(0));
        Expr condition = new Expr.Binary(new Expr.Variable(id("i")), op(TokenType.LESS, "<"), new Expr.Literal(3));
        Expr increment = new Expr.Assign(id("i"),
                new Expr.Binary(new Expr.Variable(id("i")), op(TokenType.PLUS, "+"), new Expr.Literal(1)));
        Stmt body    = new Stmt.Print(new Expr.Variable(id("i")));
        Stmt forStmt = new Stmt.For(forInit, condition, increment, body);

        // Act & Assert
        assertDoesNotThrow(() -> check(List.of(forStmt)));
    }

    @Test
    @DisplayName("[visitLiteral] PASS - 숫자 리터럴은 의미 검사 대상이 아니므로 항상 정상")
    void visitLiteral_PASS_숫자() {
        // Arrange  42;
        assertDoesNotThrow(() -> check(List.of(new Stmt.Expression(new Expr.Literal(42)))));
    }

    @Test
    @DisplayName("[visitLiteral] PASS - 문자열 리터럴은 의미 검사 대상이 아니므로 항상 정상")
    void visitLiteral_PASS_문자열() {
        // Arrange  "hello";
        assertDoesNotThrow(() -> check(List.of(new Stmt.Expression(new Expr.Literal("hello")))));
    }

    @Test
    @DisplayName("[visitLiteral] PASS - null 리터럴은 의미 검사 대상이 아니므로 항상 정상")
    void visitLiteral_PASS_null() {
        // Arrange  null;
        assertDoesNotThrow(() -> check(List.of(new Stmt.Expression(new Expr.Literal(null)))));
    }

    @Test
    @DisplayName("[visitVariable] FAIL - 아직 define되지 않은 자신을 초기화식에서 읽으면 SemanticError")
    void visitVariable_FAIL_초기화식에서_자신을_참조() {
        // Arrange  { var a = a; }
        Stmt decl  = new Stmt.VarDeclare(id("a"), new Expr.Variable(id("a")));
        Stmt block = new Stmt.Block(List.of(decl));

        // Act & Assert
        SemanticError ex = assertThrows(SemanticError.class,
                () -> check(List.of(block)));
        assertTrue(ex.getMessage().contains("초기화식"));
    }

    @Test
    @DisplayName("[visitVariable] PASS - define 완료된 변수를 이후 식에서 참조하는 경우 정상")
    void visitVariable_PASS_선언된_후_참조() {
        // Arrange  { var a = 1; var b = a; }
        Stmt declA = new Stmt.VarDeclare(id("a"), new Expr.Literal(1));
        Stmt declB = new Stmt.VarDeclare(id("b"), new Expr.Variable(id("a")));
        Stmt block = new Stmt.Block(List.of(declA, declB));

        // Act & Assert
        assertDoesNotThrow(() -> check(List.of(block)));
    }

    @Test
    @DisplayName("[visitVariable] PASS - 전역 스코프(블록 없음)에서 변수 참조는 검사하지 않으므로 정상")
    void visitVariable_PASS_전역_변수_참조() {
        // Arrange  var a = 1; — 전역은 스코프 검사 없음
        Stmt decl = new Stmt.VarDeclare(id("a"), new Expr.Variable(id("a")));

        // Act & Assert
        assertDoesNotThrow(() -> check(List.of(decl)));
    }

    @Test
    @DisplayName("[visitAssign] FAIL - 대입 우변식에 자기참조 변수가 포함되면 SemanticError 전파")
    void visitAssign_FAIL_대입값이_자기참조_포함() {
        // Arrange  { var a = 1 + a; }  — 초기화식에 자기참조
        Expr rhs   = new Expr.Binary(new Expr.Literal(1), op(TokenType.PLUS, "+"), new Expr.Variable(id("a")));
        Stmt decl  = new Stmt.VarDeclare(id("a"), rhs);
        Stmt block = new Stmt.Block(List.of(decl));

        // Act & Assert
        assertThrows(SemanticError.class, () -> check(List.of(block)));
    }

    @Test
    @DisplayName("[visitAssign] PASS - 선언 완료 후 다른 값을 재대입하는 경우 정상")
    void visitAssign_PASS_선언_후_재대입() {
        // Arrange  { var a = 1; a = 2; }
        Stmt decl   = new Stmt.VarDeclare(id("a"), new Expr.Literal(1));
        Stmt assign = new Stmt.Expression(new Expr.Assign(id("a"), new Expr.Literal(2)));
        Stmt block  = new Stmt.Block(List.of(decl, assign));

        // Act & Assert
        assertDoesNotThrow(() -> check(List.of(block)));
    }

    @Test
    @DisplayName("[visitBinary] FAIL - 이항 연산자의 피연산자에 자기참조 변수가 있으면 SemanticError 전파")
    void visitBinary_FAIL_피연산자에_자기참조() {
        // Arrange  { var a = a + 1; }
        Expr bin   = new Expr.Binary(new Expr.Variable(id("a")), op(TokenType.PLUS, "+"), new Expr.Literal(1));
        Stmt decl  = new Stmt.VarDeclare(id("a"), bin);
        Stmt block = new Stmt.Block(List.of(decl));

        // Act & Assert
        assertThrows(SemanticError.class, () -> check(List.of(block)));
    }

    @Test
    @DisplayName("[visitBinary] PASS - 리터럴만으로 구성된 이항 연산은 정상")
    void visitBinary_PASS_리터럴_간_연산() {
        // Arrange  1 + 2;
        Expr bin = new Expr.Binary(new Expr.Literal(1), op(TokenType.PLUS, "+"), new Expr.Literal(2));
        assertDoesNotThrow(() -> check(List.of(new Stmt.Expression(bin))));
    }

    @Test
    @DisplayName("[visitUnary] FAIL - 단항 연산자의 피연산자에 자기참조 변수가 있으면 SemanticError 전파")
    void visitUnary_FAIL_피연산자에_자기참조() {
        // Arrange  { var a = -a; }
        Expr unary = new Expr.Unary(op(TokenType.MINUS, "-"), new Expr.Variable(id("a")));
        Stmt decl  = new Stmt.VarDeclare(id("a"), unary);
        Stmt block = new Stmt.Block(List.of(decl));

        // Act & Assert
        assertThrows(SemanticError.class, () -> check(List.of(block)));
    }

    @Test
    @DisplayName("[visitUnary] PASS - 리터럴에 단항 연산자를 적용하는 경우 정상")
    void visitUnary_PASS_리터럴에_단항_연산() {
        // Arrange  -1;
        Expr unary = new Expr.Unary(op(TokenType.MINUS, "-"), new Expr.Literal(1));
        assertDoesNotThrow(() -> check(List.of(new Stmt.Expression(unary))));
    }

    @Test
    @DisplayName("[visitLogical] FAIL - 논리 연산자의 피연산자에 자기참조 변수가 있으면 SemanticError 전파")
    void visitLogical_FAIL_피연산자에_자기참조() {
        // Arrange  { var a = a && true; }
        Expr logical = new Expr.Logical(new Expr.Variable(id("a")), op(TokenType.AND, "&&"), new Expr.Literal(true));
        Stmt decl    = new Stmt.VarDeclare(id("a"), logical);
        Stmt block   = new Stmt.Block(List.of(decl));

        // Act & Assert
        assertThrows(SemanticError.class, () -> check(List.of(block)));
    }

    @Test
    @DisplayName("[visitLogical] PASS - 리터럴만으로 구성된 논리 연산은 정상")
    void visitLogical_PASS_리터럴_간_논리_연산() {
        // Arrange  true && false;
        Expr logical = new Expr.Logical(new Expr.Literal(true), op(TokenType.AND, "&&"), new Expr.Literal(false));
        assertDoesNotThrow(() -> check(List.of(new Stmt.Expression(logical))));
    }

    @Test
    @DisplayName("[visitGrouping] FAIL - 괄호 안의 표현식에 자기참조 변수가 있으면 SemanticError 전파")
    void visitGrouping_FAIL_괄호_안에_자기참조() {
        // Arrange  { var a = (a); }
        Expr grouping = new Expr.Grouping(new Expr.Variable(id("a")));
        Stmt decl     = new Stmt.VarDeclare(id("a"), grouping);
        Stmt block    = new Stmt.Block(List.of(decl));

        // Act & Assert
        assertThrows(SemanticError.class, () -> check(List.of(block)));
    }

    @Test
    @DisplayName("[visitGrouping] PASS - 괄호 안에 리터럴 연산만 있는 경우 정상")
    void visitGrouping_PASS_괄호_안에_리터럴() {
        // Arrange  (1 + 2);
        Expr grouping = new Expr.Grouping(
                new Expr.Binary(new Expr.Literal(1), op(TokenType.PLUS, "+"), new Expr.Literal(2)));
        assertDoesNotThrow(() -> check(List.of(new Stmt.Expression(grouping))));
    }
}
