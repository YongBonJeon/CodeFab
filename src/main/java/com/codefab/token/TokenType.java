package com.codefab.token;

public enum TokenType {
    // 리터럴
    NUMBER, STRING, TRUE, FALSE, NIL,

    // 식별자
    IDENTIFIER,

    // 연산자
    PLUS, MINUS, STAR, SLASH,
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,
    AND, OR,

    // 구분자
    LEFT_PAREN, RIGHT_PAREN,
    LEFT_BRACE, RIGHT_BRACE,
    SEMICOLON, COMMA,

    // 키워드
    VAR, PRINT, IF, ELSE, FOR,

    EOF
}
