package com.codefab.error;

public class SemanticError extends CodeFabError {
    public SemanticError(int line, String message) {
        super(line, message);
    }
}
