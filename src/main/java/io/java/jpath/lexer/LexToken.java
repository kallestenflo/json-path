package io.java.jpath.lexer;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class LexToken {
    private final TokenType type;
    private final String token;
    private final int startIndex;
    private final int endIndex;

    private LexToken(TokenType type, String token, int startIndex, int endIndex) {
        this.type = requireNonNull(type);
        this.token = requireNonNull(token);

        if (startIndex > endIndex) {
            throw new LexException("token start index must not ");
        }
        if (startIndex < 0) {
            throw new LexException("token start index must be greater than 0");
        }

        this.startIndex =  startIndex;
        this.endIndex = endIndex;
    }

    public boolean is(TokenType other){
        return type == other;
    }

    public TokenType type() {
        return type;
    }

    public String getToken() {
        return token;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public static LexToken of(TokenType type, String token, int startIndex, int endIndex) {
        return new LexToken(type, token, startIndex, endIndex);
    }

    @Override
    public String toString() {
        return "LexToken.visitor(TokenType." + type.name() + ", \""+token+"\", "+startIndex+", "+endIndex+")";
        /*
        return "LexToken{" +
                "type=" + type +
                ", token='" + token + '\'' +
                ", startIndex=" + startIndex +
                ", endIndex=" + endIndex +
                '}';
                */
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LexToken lexToken = (LexToken) o;
        return startIndex == lexToken.startIndex &&
                endIndex == lexToken.endIndex &&
                type == lexToken.type &&
                Objects.equals(token, lexToken.token);
    }

    @Override
    public int hashCode() {

        return Objects.hash(type, token, startIndex, endIndex);
    }
}
