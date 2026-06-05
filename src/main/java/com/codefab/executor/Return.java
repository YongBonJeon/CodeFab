package com.codefab.executor;

/**
 * Control-flow signal used to unwind the Java stack back to a function call site when a {@code
 * return} statement runs. Not a real error — stack trace is suppressed for performance.
 */
public class Return extends RuntimeException {

  public final Object value;

  public Return(Object value) {
    super(null, null, false, false);
    this.value = value;
  }
}
