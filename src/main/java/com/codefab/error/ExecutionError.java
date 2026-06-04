package com.codefab.error;

public class ExecutionError extends CodeFabError {

  public ExecutionError(int line, String message) {
    super(line, message);
  }
}
