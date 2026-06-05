package com.codefab.assembler;

import static com.codefab.token.TokenType.*;

import com.codefab.ast.Expr;
import com.codefab.ast.Stmt;
import com.codefab.error.ParseError;
import com.codefab.token.Token;
import com.codefab.token.TokenType;
import java.util.ArrayList;
import java.util.List;

/**
 * Recursive-descent parser. Grammar:
 *
 * <p>program -> declaration* EOF declaration -> varDecl | statement varDecl -> "var" IDENTIFIER
 * ("=" expression)? ";" statement -> printStmt | ifStmt | forStmt | block | exprStmt printStmt ->
 * "print" expression ";" ifStmt -> "if" "(" expression ")" statement ("else" statement)? forStmt ->
 * "for" "(" (varDecl | exprStmt | ";") expression? ";" expression? ")" statement block -> "{"
 * declaration* "}" exprStmt -> expression ";" expression -> assignment assignment -> IDENTIFIER "="
 * assignment | logic_or logic_or -> logic_and ("or" logic_and)* logic_and -> comparison ("and"
 * comparison)* comparison -> term ((">" | "<") term)* term -> factor (("+" | "-") factor)* factor
 * -> unary (("*" | "/") unary)* unary -> ("!" | "-") unary | primary primary -> NUMBER | STRING |
 * "true" | "false" | "(" expression ")" | IDENTIFIER
 */
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
    int line = peek().line;
    Stmt stmt;
    if (match(FUNC)) {
      stmt = function();
    } else if (match(VAR)) {
      stmt = varDeclaration();
    } else {
      stmt = statement();
    }
    if (stmt.line == 0) stmt.line = line;
    return stmt;
  }

  private Stmt function() {
    Token name = consume(IDENTIFIER, "함수 이름이 필요합니다.");
    consume(LEFT_PAREN, "함수 이름 뒤에 '(' 가 필요합니다.");
    List<Token> params = new ArrayList<>();
    if (!check(RIGHT_PAREN)) {
      do {
        params.add(consume(IDENTIFIER, "매개변수 이름이 필요합니다."));
      } while (match(COMMA));
    }
    consume(RIGHT_PAREN, "매개변수 목록 뒤에 ')' 가 필요합니다.");
    consume(LEFT_BRACE, "함수 본문 앞에 '{' 가 필요합니다.");
    return new Stmt.Function(name, params, block());
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
    int line = peek().line;
    Stmt stmt;
    if (match(PRINT)) {
      stmt = printStatement();
    } else if (match(IF)) {
      stmt = ifStatement();
    } else if (match(FOR)) {
      stmt = forStatement();
    } else if (match(RETURN)) {
      stmt = returnStatement();
    } else if (match(LEFT_BRACE)) {
      stmt = new Stmt.Block(block());
    } else {
      stmt = expressionStatement();
    }
    if (stmt.line == 0) stmt.line = line;
    return stmt;
  }

  private Stmt returnStatement() {
    Token keyword = previous();
    Expr value = null;
    if (!check(SEMICOLON)) {
      value = expression();
    }
    consume(SEMICOLON, "return 문 끝에 ';' 가 필요합니다.");
    return new Stmt.Return(keyword, value);
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
    Expr expr = equality();
    while (match(AND)) {
      Token op = previous();
      Expr right = equality();
      expr = new Expr.Logical(expr, op, right);
    }
    return expr;
  }

  private Expr equality() {
    Expr expr = comparison();
    while (match(EQUAL_EQUAL, BANG_EQUAL)) {
      Token op = previous();
      Expr right = comparison();
      expr = new Expr.Binary(expr, op, right);
    }
    return expr;
  }

  private Expr comparison() {
    Expr expr = term();
    while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
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
    while (match(STAR, SLASH, PERCENT)) {
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
    return call();
  }

  private Expr call() {
    Expr expr = primary();
    while (match(LEFT_PAREN)) {
      expr = finishCall(expr);
    }
    return expr;
  }

  private Expr finishCall(Expr callee) {
    List<Expr> arguments = new ArrayList<>();
    if (!check(RIGHT_PAREN)) {
      do {
        arguments.add(expression());
      } while (match(COMMA));
    }
    Token paren = consume(RIGHT_PAREN, "인자 목록 뒤에 ')' 가 필요합니다.");
    return new Expr.Call(callee, paren, arguments);
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
