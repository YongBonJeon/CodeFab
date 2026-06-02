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
            case '"': string(); break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    throw new ParseError(line, "예상치 못한 문자: '" + c + "'");
                }
        }
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }
        if (isAtEnd()) {
            throw new ParseError(line, "닫히지 않은 문자열입니다.");
        }
        advance(); // closing "
        String value = source.substring(start + 1, current - 1);
        tokens.add(new Token(STRING, source.substring(start, current), value, line));
    }

    private void number() {
        while (isDigit(peek())) advance();
        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) advance();
        }
        String text = source.substring(start, current);
        tokens.add(new Token(NUMBER, text, Double.parseDouble(text), line));
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        addToken(keywordType(text));
    }

    private TokenType keywordType(String text) {
        switch (text) {
            case "var": return VAR;
            case "if": return IF;
            case "else": return ELSE;
            case "for": return FOR;
            case "true": return TRUE;
            case "false": return FALSE;
            case "and": return AND;
            case "or": return OR;
            case "print": return PRINT;
            default: return IDENTIFIER;
        }
    }

    private char advance() { return source.charAt(current++); }

    private char peek() {
        return isAtEnd() ? '\0' : source.charAt(current);
    }

    private char peekNext() {
        return current + 1 >= source.length() ? '\0' : source.charAt(current + 1);
    }

    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private boolean isAtEnd() { return current >= source.length(); }
    private boolean isDigit(char c) { return c >= '0' && c <= '9'; }
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }
    private boolean isAlphaNumeric(char c) { return isAlpha(c) || isDigit(c); }

    private void addToken(TokenType type) {
        tokens.add(new Token(type, source.substring(start, current), null, line));
    }
}
