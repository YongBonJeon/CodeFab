package com.codefab.executor;

import com.codefab.error.ExecutionError;
import com.codefab.token.Token;
import java.util.HashMap;
import java.util.Map;

public class Environment {
  private final Map<String, Object> values = new HashMap<>();
  private final Environment enclosing;

  public Environment() {
    this.enclosing = null;
  }

  public Environment(Environment enclosing) {
    this.enclosing = enclosing;
  }

  public void define(String name, Object value) {
    values.put(name, value);
  }

  public Object get(Token name) {
    if (values.containsKey(name.origin)) {
      return values.get(name.origin);
    }
    if (enclosing != null) {
      return enclosing.get(name);
    }
    throw new ExecutionError(name.line, "미정의된 변수 '" + name.origin + "'");
  }

  public void assign(Token name, Object value) {
    if (values.containsKey(name.origin)) {
      values.put(name.origin, value);
      return;
    }
    if (enclosing != null) {
      enclosing.assign(name, value);
      return;
    }
    throw new ExecutionError(name.line, "미정의된 변수 '" + name.origin + "'");
  }

  // ── Static binding (optimization) ──────────────────────────────────────────
  // When the Checker has pre-computed the distance to a variable's scope, the
  // executor can hop straight there in O(1) instead of walking the chain.

  /** Returns the environment {@code distance} hops up the enclosing chain. */
  public Environment ancestor(int distance) {
    Environment environment = this;
    for (int i = 0; i < distance; i++) {
      environment = environment.enclosing;
    }
    return environment;
  }

  public Object getAt(int distance, String name) {
    return ancestor(distance).values.get(name);
  }

  public void assignAt(int distance, Token name, Object value) {
    ancestor(distance).values.put(name.origin, value);
  }

  // ── Introspection (debug shell) ────────────────────────────────────────────

  public Environment enclosing() {
    return enclosing;
  }

  public boolean containsLocally(String name) {
    return values.containsKey(name);
  }

  public Map<String, Object> values() {
    return values;
  }
}
