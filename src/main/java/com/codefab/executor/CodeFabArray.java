package com.codefab.executor;

/** A fixed-size array. Created via {@code Array(n)}; every slot starts as {@code null}. */
public class CodeFabArray {

  private final Object[] elements;

  public CodeFabArray(int size) {
    this.elements = new Object[size];
  }

  public int size() {
    return elements.length;
  }

  public Object get(int index) {
    return elements[index];
  }

  public void set(int index, Object value) {
    elements[index] = value;
  }

  /** Renders as {@code [10, 20, null]} — numbers without a trailing {@code .0}, null as {@code null}. */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < elements.length; i++) {
      if (i > 0) sb.append(", ");
      sb.append(format(elements[i]));
    }
    return sb.append("]").toString();
  }

  private String format(Object value) {
    if (value == null) return "null";
    if (value instanceof Double d) {
      String text = d.toString();
      return text.endsWith(".0") ? text.substring(0, text.length() - 2) : text;
    }
    return value.toString();
  }
}
