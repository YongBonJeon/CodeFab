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
}
