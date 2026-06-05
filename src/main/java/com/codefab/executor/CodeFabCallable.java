package com.codefab.executor;

import com.codefab.token.Token;
import java.util.List;

/** A value that can be invoked with {@code (...)} — user functions and built-in (native) functions. */
public interface CodeFabCallable {

  /** Number of parameters the callable expects. Used for argument-count validation. */
  int arity();

  /**
   * Invokes the callable.
   *
   * @param executor  the executor driving evaluation
   * @param arguments evaluated argument values
   * @param paren     the call's closing parenthesis token, for error line reporting
   */
  Object call(Executor executor, List<Object> arguments, Token paren);
}
