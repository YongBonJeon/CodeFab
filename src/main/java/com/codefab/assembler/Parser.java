package com.codefab.assembler;

import static com.codefab.token.TokenType.*;

import com.codefab.ast.Expr;
import com.codefab.ast.Stmt;
import com.codefab.error.ParseError;
import com.codefab.token.Token;
import com.codefab.token.TokenType;
import java.util.ArrayList;
import java.util.List;

public class Parser {

  private final List<Token> tokens;
  private int current = 0;

  public Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  public List<Stmt> parse() {
    List<Stmt> statements = new ArrayList<>();
    while (!isAtEnd()) {
      statements.add(declaration());
    }
    return statements;
  }

  private Stmt declaration() {
    return statement();
  }

  private Stmt statement() {
    return expressionStatement();
  }

  private Stmt expressionStatement() {
    Expr e = expression();
    consume(SEMICOLON, "문장 끝에 ';' 가 필요합니다.");
    return new Stmt.Expression(e);
  }

  private Expr expression() {
    return term();
  }

  private Expr term() {
    Expr expr = factor();
    while (match(PLUS, MINUS)) {
      Token op = previous();
      Expr right = factor();
      expr = new Expr.Binary(expr, op, right);
    }
    return expr;
  }

  private Expr factor() {
    Expr expr = unary();
    while (match(STAR, SLASH)) {
      Token op = previous();
      Expr right = unary();
      expr = new Expr.Binary(expr, op, right);
    }
    return expr;
  }

  private Expr unary() {
    if (match(BANG, MINUS)) {
      Token op = previous();
      Expr right = unary();
      return new Expr.Unary(op, right);
    }
    return primary();
  }

  private Expr primary() {
    if (match(FALSE)) return new Expr.Literal(false);
    if (match(TRUE)) return new Expr.Literal(true);
    if (match(NUMBER, STRING)) return new Expr.Literal(previous().literal);
    if (match(IDENTIFIER)) return new Expr.Variable(previous());
    if (match(LEFT_PAREN)) {
      Expr e = expression();
      consume(RIGHT_PAREN, "표현식 뒤에 ')' 가 필요합니다.");
      return new Expr.Grouping(e);
    }
    throw new ParseError(peek().line, "표현식이 필요합니다. ('" + peek().origin + "' 발견)");
  }

  private boolean match(TokenType... types) {
    for (TokenType t : types) {
      if (check(t)) {
        advance();
        return true;
      }
    }
    return false;
  }

  private boolean check(TokenType type) {
    if (isAtEnd()) return false;
    return peek().type == type;
  }

  private Token advance() {
    if (!isAtEnd()) current++;
    return previous();
  }

  private boolean isAtEnd() {
    return peek().type == EOF;
  }

  private Token peek() {
    return tokens.get(current);
  }

  private Token previous() {
    return tokens.get(current - 1);
  }

  private Token consume(TokenType type, String message) {
    if (check(type)) return advance();
    throw new ParseError(peek().line, message);
  }
}
