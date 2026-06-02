package com.codefab.error;

public abstract class CodeFabError extends RuntimeException {
    public final int line;

    protected CodeFabError(int line, String message) {
        super("[line " + line + "] " + message);
        this.line = line;
    }
}
