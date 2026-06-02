package com.codefab.assembler;

import static com.codefab.token.TokenType.*;

import com.codefab.error.ParseError;
import com.codefab.token.Token;
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
            case '(': tokens.add(new Token(LEFT_PAREN, source.substring(start, current), null, line)); break;
            case ')': tokens.add(new Token(RIGHT_PAREN, source.substring(start, current), null, line)); break;
            case '{': tokens.add(new Token(LEFT_BRACE, source.substring(start, current), null, line)); break;
            case '}': tokens.add(new Token(RIGHT_BRACE, source.substring(start, current), null, line)); break;
            case ';': tokens.add(new Token(SEMICOLON, source.substring(start, current), null, line)); break;
            case ' ': case '\r': case '\t': break;
            case '\n': line++; break;
            default:
                throw new ParseError(line, "예상치 못한 문자: '" + c + "'");
        }
    }

    private char advance() { return source.charAt(current++); }

    private boolean isAtEnd() { return current >= source.length(); }
}
