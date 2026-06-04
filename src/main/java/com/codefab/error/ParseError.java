package com.codefab.error;

public class ParseError extends CodeFabError {
    public ParseError(int line, String message) {
        super(line, message);
    }
}
