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
    if (match(VAR)) return varDeclaration();
    return statement();
  }

  private Stmt varDeclaration() {
    Token name = consume(IDENTIFIER, "변수 이름이 필요합니다.");
    Expr initializer = null;
    if (match(EQUAL)) {
      initializer = expression();
    }
    consume(SEMICOLON, "변수 선언 끝에 ';' 가 필요합니다.");
    return new Stmt.VarDeclare(name, initializer);
  }

  private Stmt statement() {
    if (match(PRINT)) return printStatement();
    if (match(IF)) return ifStatement();
    if (match(FOR)) return forStatement();
    if (match(LEFT_BRACE)) return new Stmt.Block(block());
    return expressionStatement();
  }

  private Stmt printStatement() {
    Expr value = expression();
    consume(SEMICOLON, "print 문 끝에 ';' 가 필요합니다.");
    return new Stmt.Print(value);
  }

  private Stmt ifStatement() {
    consume(LEFT_PAREN, "if 뒤에 '(' 가 필요합니다.");
    Expr condition = expression();
    consume(RIGHT_PAREN, "if 조건 뒤에 ')' 가 필요합니다.");
    Stmt thenBranch = statement();
    Stmt elseBranch = null;
    if (match(ELSE)) {
      elseBranch = statement();
    }
    return new Stmt.If(condition, thenBranch, elseBranch);
  }

  private Stmt forStatement() {
    consume(LEFT_PAREN, "for 뒤에 '(' 가 필요합니다.");

    Stmt initializer;
    if (match(SEMICOLON)) {
      initializer = null;
    } else if (match(VAR)) {
      initializer = varDeclaration();
    } else {
      initializer = expressionStatement();
    }

    Expr condition = null;
    if (!check(SEMICOLON)) {
      condition = expression();
    }
    consume(SEMICOLON, "for 조건 뒤에 ';' 가 필요합니다.");

    Expr increment = null;
    if (!check(RIGHT_PAREN)) {
      increment = expression();
    }
    consume(RIGHT_PAREN, "for 절 뒤에 ')' 가 필요합니다.");

    Stmt body = statement();
    return new Stmt.For(initializer, condition, increment, body);
  }

  private List<Stmt> block() {
    List<Stmt> stmts = new ArrayList<>();
    while (!check(RIGHT_BRACE) && !isAtEnd()) {
      stmts.add(declaration());
    }
    consume(RIGHT_BRACE, "블록 끝에 '}' 가 필요합니다.");
    return stmts;
  }

  private Stmt expressionStatement() {
    Expr e = expression();
    consume(SEMICOLON, "문장 끝에 ';' 가 필요합니다.");
    return new Stmt.Expression(e);
  }

  private Expr expression() {
    return assignment();
  }

  private Expr assignment() {
    Expr expr = or();
    if (match(EQUAL)) {
      Token equals = previous();
      Expr value = assignment();
      if (expr instanceof Expr.Variable) {
        Token name = ((Expr.Variable) expr).name;
        return new Expr.Assign(name, value);
      }
      throw new ParseError(equals.line, "잘못된 대입 대상입니다.");
    }
    return expr;
  }

  private Expr or() {
    Expr expr = and();
    while (match(OR)) {
      Token op = previous();
      Expr right = and();
      expr = new Expr.Logical(expr, op, right);
    }
    return expr;
  }

  private Expr and() {
    Expr expr = comparison();
    while (match(AND)) {
      Token op = previous();
      Expr right = comparison();
      expr = new Expr.Logical(expr, op, right);
    }
    return expr;
  }

  private Expr comparison() {
    Expr expr = term();
    while (match(GREATER, LESS)) {
      Token op = previous();
      Expr right = term();
      expr = new Expr.Binary(expr, op, right);
    }
    return expr;
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
