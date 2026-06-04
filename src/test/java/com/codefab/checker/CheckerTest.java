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

    // ── Cycle 1: visitBlock 재선언 ──────────────────────────────────────

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

    // ── Cycle 2: visitVarDeclare/visitVariable 자기참조 ─────────────────

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
    @DisplayName("[visitVariable] PASS - 전역 스코프에서 변수 참조는 검사하지 않으므로 정상")
    void visitVariable_PASS_전역_변수_참조() {
        // Arrange  var a = a;  (블록 없음 = 전역)
        Stmt decl = new Stmt.VarDeclare(id("a"), new Expr.Variable(id("a")));

        // Act & Assert
        assertDoesNotThrow(() -> check(List.of(decl)));
    }

    // ── Cycle 3: visitIf ─────────────────────────────────────────────────

    @Test
    @DisplayName("[visitIf] FAIL - then 블록 내에서 동일 이름 변수를 재선언하면 SemanticError")
    void visitIf_FAIL_then_블록에서_변수_재선언() {
        // Arrange  if (true) { var a = 1; var a = 2; }
        Stmt decl1     = new Stmt.VarDeclare(id("a"), new Expr.Literal(1));
        Stmt decl2     = new Stmt.VarDeclare(id("a"), new Expr.Literal(2));
        Stmt thenBlock = new Stmt.Block(List.of(decl1, decl2));
        Stmt ifStmt    = new Stmt.If(new Expr.Literal(true), thenBlock, null);

        // Act & Assert
        assertThrows(SemanticError.class, () -> check(List.of(ifStmt)));
    }

    @Test
    @DisplayName("[visitIf] PASS - then과 else는 별개의 스코프이므로 같은 이름 변수 선언 허용")
    void visitIf_PASS_then과_else에서_같은_이름_선언() {
        // Arrange  if (true) { var a = 1; } else { var a = 2; }
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
        Stmt ifStmt = new Stmt.If(new Expr.Literal(true),
                new Stmt.Print(new Expr.Literal(1)), null);

        // Act & Assert
        assertDoesNotThrow(() -> check(List.of(ifStmt)));
    }
}
