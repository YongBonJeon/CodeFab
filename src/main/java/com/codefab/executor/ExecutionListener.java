package com.codefab.executor;

import com.codefab.ast.Stmt;

/** Hook invoked by the {@link Executor} immediately before each statement runs. */
public interface ExecutionListener {

  /**
   * @param stmt  the statement about to execute
   * @param depth current block-nesting depth (0 = top level)
   */
  void beforeStatement(Stmt stmt, int depth);
}
