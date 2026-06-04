package com.codefab.token;

import java.util.Objects;

public final class Token {
    public final TokenType type;
    public final String origin;
    public final Object literal;
    public final int line;

    public Token(TokenType type, String origin, Object literal, int line) {
        this.type = type;
        this.origin = origin;
        this.literal = literal;
        this.line = line;
    }

    public Token(TokenType type, String origin) {
        this(type, origin, null, 1);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Token(").append(type)
                .append(", \"").append(origin).append("\"");
        if (literal != null) sb.append(", value=").append(literal);
        return sb.append(")").toString();

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Token)) return false;
        Token t = (Token) o;
        return type == t.type
                && Objects.equals(origin, t.origin)
                && Objects.equals(literal, t.literal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, origin, literal);
    }
}
