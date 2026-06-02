package com.codefab.executor;

import com.codefab.error.RuntimeError;
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

  public Object get(String name) {
    if (values.containsKey(name)) {
      return values.get(name);
    }
    throw new RuntimeError("미정의된 변수 '" + name + "'");
  }

  public void assign(String name, Object value) {
    if (values.containsKey(name)) {
      values.put(name, value);
      return;
    }
    if (enclosing != null) {
      enclosing.assign(name, value);
      return;
    }
    throw new RuntimeError("미정의된 변수 '" + name + "'");
  }

  public Environment getEnclosing() {
    return enclosing;
  }
}
