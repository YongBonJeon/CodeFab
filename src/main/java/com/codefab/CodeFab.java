package com.codefab;

import com.codefab.assembler.Parser;
import com.codefab.assembler.Tokenizer;
import com.codefab.ast.Stmt;
import com.codefab.checker.Checker;
import com.codefab.error.CodeFabError;
import com.codefab.executor.Executor;
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

  public boolean run(String source) {
    try {
      List<Token> tokens = new Tokenizer(source).tokenize();
      List<Stmt> stmts = new Parser(tokens).parse();
      new Checker().check(stmts);
      executor.execute(stmts);
      return true;
    } catch (CodeFabError e) {
      err.println(e.formatted());
      return false;
    }
  }
}
