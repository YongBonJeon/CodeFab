package com.codefab;

import com.codefab.assembler.Parser;
import com.codefab.assembler.Tokenizer;
import com.codefab.ast.Stmt;
import com.codefab.checker.Checker;
import com.codefab.error.CodeFabError;
import com.codefab.executor.Executor;
import com.codefab.optimizer.Optimizer;
import com.codefab.token.Token;

import java.io.PrintStream;
import java.util.List;

/**
 * Front-end facade wiring the full pipeline:
 * {@code Tokenizer -> Parser -> Optimizer -> Checker -> Executor}.
 */
public class CodeFab {

  private final Checker checker;
  private final Executor executor;
  private final PrintStream err;

  public CodeFab() {
    this(System.out, System.err);
  }

  public CodeFab(PrintStream out, PrintStream err) {
    this.checker = new Checker();
    this.executor = new Executor(out);
    this.err = err;
  }

  public Executor getExecutor() {
    return executor;
  }

  /**
   * Runs the full pipeline on {@code source}.
   *
   * @return {@code true} on success, {@code false} if a {@link CodeFabError} was reported.
   */
  public boolean run(String source) {
    try {
      List<Token> tokens = new Tokenizer(source).tokenize();
      List<Stmt> stmts = new Parser(tokens).parse();
      checker.check(stmts);
      execute(compile(source));
      return true;
    } catch (CodeFabError e) {
      err.println(e.formatted());
      return false;
    }
  }

  /**
   * Front-end stages only (tokenize, parse, optimize, resolve). The resolved scope distances are
   * pushed into the executor. Returns the optimized, ready-to-run statements.
   */
  public List<Stmt> compile(String source) {
    List<Token> tokens = new Tokenizer(source).tokenize();
    List<Stmt> stmts = new Parser(tokens).parse();
    stmts = new Optimizer().optimize(stmts);
    Checker checker = new Checker();
    checker.check(stmts);
    executor.setLocals(checker.getLocals());
    return stmts;
  }

  public void execute(List<Stmt> statements) {
    executor.execute(statements);
  }
}
