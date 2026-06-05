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

public class CodeFab {

  private final Executor executor;
  private final PrintStream err;

  public CodeFab() {
    this(System.out, System.err);
  }

  public CodeFab(PrintStream out, PrintStream err) {
    this.executor = new Executor(out);
    this.err = err;
  }

  public Executor getExecutor() {
    return executor;
  }

  public boolean run(String source) {
    try {
      execute(compile(source));
      return true;
    } catch (CodeFabError e) {
      err.println(e.formatted());
      return false;
    }
  }

  public List<Stmt> compile(String source) {
    List<Token> tokens = new Tokenizer(source).tokenize();
    List<Stmt> stmts = new Parser(tokens).parse();
    stmts = new Optimizer().optimize(stmts);
    Checker checker = new Checker();
    checker.check(stmts);
    executor.resolve(checker.getLocals());
    return stmts;
  }

  public void execute(List<Stmt> statements) {
    executor.execute(statements);
  }
}
