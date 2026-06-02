package com.codefab.executor;

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
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Object get(String name) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public void assign(String name, Object value) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Environment getEnclosing() {
    return enclosing;
  }
}
