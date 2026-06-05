package com.codefab.token;

public enum TokenType {
    LEFT_PAREN, RIGHT_PAREN,
    LEFT_BRACE, RIGHT_BRACE,
    SEMICOLON,
    PLUS, MINUS, STAR, SLASH, PERCENT,
    EQUAL, GREATER, LESS, BANG,

    IDENTIFIER, STRING, NUMBER,

    VAR, IF, ELSE, FOR,
    TRUE, FALSE,
    AND, OR,
    PRINT,

    EOF
}
