package com.codefab.executor;

import com.codefab.ast.Stmt;
import com.codefab.token.Token;
import java.util.List;

/** A user-declared function value. Captures the environment in which it was declared (closure). */
public class CodeFabFunction implements CodeFabCallable {

  private final Stmt.Function declaration;
  private final Environment closure;

  public CodeFabFunction(Stmt.Function declaration, Environment closure) {
    this.declaration = declaration;
    this.closure = closure;
  }

  @Override
  public int arity() {
    return declaration.params.size();
  }

  @Override
  public Object call(Executor executor, List<Object> arguments, Token paren) {
    Environment environment = new Environment(closure);
    for (int i = 0; i < declaration.params.size(); i++) {
      environment.define(declaration.params.get(i).origin, arguments.get(i));
    }
    try {
      executor.executeBlock(declaration.body, environment);
    } catch (Return returnValue) {
      return returnValue.value;
    }
    return null;
  }

  @Override
  public String toString() {
    return "<fn " + declaration.name.origin + ">";
  }
}
