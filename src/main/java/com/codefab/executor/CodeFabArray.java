package com.codefab.executor;

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
}
