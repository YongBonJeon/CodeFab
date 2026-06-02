package com.codefab.assembler;

import static com.codefab.token.TokenType.*;

import com.codefab.error.ParseError;
import com.codefab.token.Token;
import com.codefab.token.TokenType;
import java.util.ArrayList;
import java.util.List;

public class Tokenizer {

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    public Tokenizer(String source) {
        this.source = source;
    }

    public List<Token> tokenize() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ';': addToken(SEMICOLON); break;
            case '+': addToken(PLUS); break;
            case '-': addToken(MINUS); break;
            case '*': addToken(STAR); break;
            case '/':
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(SLASH);
                }
                break;
            case '=': addToken(EQUAL); break;
            case '>': addToken(GREATER); break;
            case '<': addToken(LESS); break;
            case '!': addToken(BANG); break;
            case ' ': case '\r': case '\t': break;
            case '\n': line++; break;
            default:
                throw new ParseError(line, "예상치 못한 문자: '" + c + "'");
        }
    }

    private char advance() { return source.charAt(current++); }

    private char peek() {
        return isAtEnd() ? '\0' : source.charAt(current);
    }

    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private boolean isAtEnd() { return current >= source.length(); }

    private void addToken(TokenType type) {
        tokens.add(new Token(type, source.substring(start, current), null, line));
    }
}
