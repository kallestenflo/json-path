package io.java.jpath.lexer;

public class LexException extends RuntimeException {
    public LexException(String message, Lexer l) {
        super(message + " at position: " + l.pos() + " but found: " + found(l));
    }

    private static String found(Lexer l) {
        return l.current() == Lexer.EOF ? "EOP" : Character.toString(l.current());
    }

    public LexException(String message) {
        super(message);
    }
}