package com.codefab.assembler;

import static com.codefab.token.TokenType.*;

import com.codefab.error.ParseError;
import com.codefab.token.Token;
import com.codefab.token.TokenType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tokenizer {

  private static final Map<String, TokenType> KEYWORDS = new HashMap<>();

  static {
    KEYWORDS.put("var", VAR);
    KEYWORDS.put("if", IF);
    KEYWORDS.put("else", ELSE);
    KEYWORDS.put("for", FOR);
    KEYWORDS.put("true", TRUE);
    KEYWORDS.put("false", FALSE);
    KEYWORDS.put("and", AND);
    KEYWORDS.put("or", OR);
    KEYWORDS.put("print", PRINT);
    KEYWORDS.put("Func", FUNC);
    KEYWORDS.put("func", FUNC);
    KEYWORDS.put("return", RETURN);
  }

  private final String source;
  private final List<Token> tokens = new ArrayList<>();
  private int start = 0;
  private int current = 0;
  private int line = 1;

  public Tokenizer(String source) {
    // Strip a leading UTF-8 BOM (U+FEFF) that Windows editors often prepend.
    this.source = source.startsWith("﻿") ? source.substring(1) : source;
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
      case '(' -> addToken(LEFT_PAREN);
      case ')' -> addToken(RIGHT_PAREN);
      case '{' -> addToken(LEFT_BRACE);
      case '}' -> addToken(RIGHT_BRACE);
      case '[' -> addToken(LEFT_BRACKET);
      case ']' -> addToken(RIGHT_BRACKET);
      case ';' -> addToken(SEMICOLON);
      case ',' -> addToken(COMMA);
      case '+' -> addToken(PLUS);
      case '-' -> addToken(MINUS);
      case '*' -> addToken(STAR);
      case '%' -> addToken(PERCENT);
      case '/' -> {
        if (match('/')) {
          while (peek() != '\n' && !isAtEnd()) {
            advance();
          }
        } else {
          addToken(SLASH);
        }
      }
      case '=' -> addToken(match('=') ? EQUAL_EQUAL : EQUAL);
      case '>' -> addToken(match('=') ? GREATER_EQUAL : GREATER);
      case '<' -> addToken(match('=') ? LESS_EQUAL : LESS);
      case '!' -> addToken(match('=') ? BANG_EQUAL : BANG);
      case ' ', '\r', '\t' -> {
      }
      case '\n' -> line++;
      case '"' -> string();
      default -> {
        if (isDigit(c)) {
          number();
        } else if (isAlpha(c)) {
          identifier();
        } else {
          throw new ParseError(line, "예상치 못한 문자: '" + c + "'");
        }
      }
    }
  }

  private void string() {
    while (peek() != '"' && !isAtEnd()) {
      if (peek() == '\n') {
        line++;
      }
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
    while (isDigit(peek())) {
      advance();
    }
    if (peek() == '.' && isDigit(peekNext())) {
      advance();
      while (isDigit(peek())) {
        advance();
      }
    }
    String text = source.substring(start, current);
    tokens.add(new Token(NUMBER, text, Double.parseDouble(text), line));
  }

  private void identifier() {
    while (isAlphaNumeric(peek())) {
      advance();
    }
    String text = source.substring(start, current);
    TokenType type = KEYWORDS.getOrDefault(text, IDENTIFIER);
    addToken(type);
  }

  private char advance() {
    return source.charAt(current++);
  }

  private char peek() {
    return isAtEnd() ? '\0' : source.charAt(current);
  }

  private char peekNext() {
    return current + 1 >= source.length() ? '\0' : source.charAt(current + 1);
  }

  private boolean match(char expected) {
    if (isAtEnd() || source.charAt(current) != expected) {
      return false;
    }
    current++;
    return true;
  }

  private boolean isAtEnd() {
    return current >= source.length();
  }

  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
  }

  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }

  private void addToken(TokenType type) {
    tokens.add(new Token(type, source.substring(start, current), null, line));
  }
}
