package com.codefab.error;

public abstract class CodeFabError extends RuntimeException {
    public final int line;

    protected CodeFabError(int line, String message) {
        super(message);
        this.line = line;
    }

    public String formatted() {
        return "[" + line + "번째 줄] " + getMessage();
    }
}
