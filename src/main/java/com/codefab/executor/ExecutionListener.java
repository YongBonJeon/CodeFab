package com.codefab.executor;

import com.codefab.ast.Stmt;

/**
 * Hook invoked by the {@link Executor} immediately before each statement runs. The debug shell uses
 * it to pause execution at statement granularity (stepping, breakpoints, watches).
 */
public interface ExecutionListener {

  /**
   * @param stmt  the statement about to execute
   * @param depth current block-nesting depth (0 at top level); used to distinguish step / next
   */
  void beforeStatement(Stmt stmt, int depth);
}
