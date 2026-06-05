package com.codefab.executor;

import com.codefab.ast.Expr;
import com.codefab.ast.Stmt;
import com.codefab.token.Token;
import com.codefab.token.TokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import com.codefab.error.ExecutionError;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArrayTest {

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

    // ── Cycle 1: visitIndex 배열 읽기 ─────────────────────────────────────


    @Test
    @DisplayName("[visitIndex] PASS - null로 초기화된 배열 원소를 읽으면 null이 출력된다")
    void visitIndex_PASS_null로_초기화된_원소를_읽는다() {
        // Arrange
        Stmt arrDecl = new Stmt.VarDeclare(token(TokenType.IDENTIFIER, "arr"),
                new Expr.Literal(new CodeFabArray(3)));
        Stmt print = new Stmt.Print(new Expr.Index(
                new Expr.Variable(token(TokenType.IDENTIFIER, "arr")),
                token(TokenType.LEFT_BRACKET, "["),
                new Expr.Literal(0.0)));

        // Act
        executor.execute(List.of(arrDecl, print));

        // Assert
        assertEquals("null", output());
    }

    // ── Cycle 2: visitIndexSet 배열 쓰기 ───────────────────────────────────

    @Test
    @DisplayName("[visitIndexSet] PASS - 인덱스로 값을 쓰고 읽는다")
    void visitIndexSet_PASS_인덱스로_값을_쓰고_읽는다() {
        // Arrange
        Token arrToken = token(TokenType.IDENTIFIER, "arr");
        Token bracket = token(TokenType.LEFT_BRACKET, "[");

        Stmt arrDecl = new Stmt.VarDeclare(arrToken, new Expr.Literal(new CodeFabArray(3)));
        Stmt write = new Stmt.Expression(new Expr.IndexSet(
                new Expr.Variable(arrToken),
                bracket,
                new Expr.Literal(0.0),
                new Expr.Literal(10.0)));
        Stmt print = new Stmt.Print(new Expr.Index(
                new Expr.Variable(arrToken),
                bracket,
                new Expr.Literal(0.0)));

        // Act
        executor.execute(List.of(arrDecl, write, print));

        // Assert
        assertEquals("10", output());
    }

    // ── Cycle 3: 인덱스 표현식 ────────────────────────────────────────────

    @Test
    @DisplayName("[visitIndex] PASS - 인덱스로 표현식(계산식)을 사용할 수 있다")
    void visitIndex_PASS_인덱스로_표현식을_사용할_수_있다() {
        // Arrange
        Token arrToken = token(TokenType.IDENTIFIER, "arr");
        Token iToken = token(TokenType.IDENTIFIER, "i");
        Token bracket = token(TokenType.LEFT_BRACKET, "[");
        Token minus = token(TokenType.MINUS, "-");

        Stmt arrDecl = new Stmt.VarDeclare(arrToken, new Expr.Literal(new CodeFabArray(3)));
        Stmt iDecl = new Stmt.VarDeclare(iToken, new Expr.Literal(2.0));
        Stmt write = new Stmt.Expression(new Expr.IndexSet(
                new Expr.Variable(arrToken),
                bracket,
                new Expr.Binary(new Expr.Variable(iToken), minus, new Expr.Literal(1.0)),
                new Expr.Literal(7.0)));
        Stmt print = new Stmt.Print(new Expr.Index(
                new Expr.Variable(arrToken),
                bracket,
                new Expr.Literal(1.0)));

        // Act
        executor.execute(List.of(arrDecl, iDecl, write, print));

        // Assert
        assertEquals("7", output());
    }

    // ── Cycle 4: 범위를 벗어난 인덱스 ────────────────────────────────────

    @Test
    @DisplayName("[visitIndex] FAIL - 범위를 벗어난 인덱스 접근은 ExecutionError")
    void visitIndex_FAIL_범위를_벗어난_인덱스는_에러() {
        // Arrange
        Token arrToken = token(TokenType.IDENTIFIER, "arr");
        Token bracket = token(TokenType.LEFT_BRACKET, "[");

        Stmt arrDecl = new Stmt.VarDeclare(arrToken, new Expr.Literal(new CodeFabArray(3)));
        Stmt print = new Stmt.Print(new Expr.Index(
                new Expr.Variable(arrToken),
                bracket,
                new Expr.Literal(5.0)));

        // Act & Assert
        ExecutionError error = assertThrows(ExecutionError.class,
                () -> executor.execute(List.of(arrDecl, print)));
        assertTrue(error.getMessage().contains("범위"));
    }

    // ── Cycle 5: 숫자가 아닌 인덱스 ──────────────────────────────────────

    @Test
    @DisplayName("[visitIndex] FAIL - 숫자가 아닌 인덱스는 ExecutionError")
    void visitIndex_FAIL_숫자가_아닌_인덱스는_에러() {
        // Arrange
        Token arrToken = token(TokenType.IDENTIFIER, "arr");
        Token bracket = token(TokenType.LEFT_BRACKET, "[");

        Stmt arrDecl = new Stmt.VarDeclare(arrToken, new Expr.Literal(new CodeFabArray(3)));
        Stmt print = new Stmt.Print(new Expr.Index(
                new Expr.Variable(arrToken),
                bracket,
                new Expr.Literal("hello")));

        // Act & Assert
        ExecutionError error = assertThrows(ExecutionError.class,
                () -> executor.execute(List.of(arrDecl, print)));
        assertTrue(error.getMessage().contains("인덱스"));
    }
}
