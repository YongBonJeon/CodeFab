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

    // ── Cycle 1: visitBlock 재선언 ───────────────────────────────────────

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
}
