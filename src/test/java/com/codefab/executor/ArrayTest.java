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

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    // ── Cycle 1: visitIndex 배열 읽기 ──────────────────────────────────────

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
}
