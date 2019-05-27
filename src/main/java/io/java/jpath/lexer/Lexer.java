package io.java.jpath.lexer;

import java.util.function.Consumer;

import static java.lang.Math.min;
import static java.util.Objects.requireNonNull;

public class Lexer {

    public static final char EOF = Character.MIN_VALUE;

    private final Consumer<LexToken> sink;
    private final String source;
    private final int length;
    private int start;
    private int pos;

    private Lexer(String str, Consumer<LexToken> sink) {
        this.sink = requireNonNull(sink);
        this.source = requireNonNull(str).trim();
        if (source.isEmpty()) {
            fail("Lexer source must not be empty");
        }
        this.length = source.length();
        this.start = 0;
        this.pos = 0;
    }

    @Override
    public String toString() {
        return "Lexer{" +
                ", source='" + source + '\'' +
                ", length=" + length +
                ", start=" + start +
                ", pos=" + pos +
                '}';
    }

    public static Lexer of(String source, Consumer<LexToken> sink) {
        return new Lexer(source, sink);
    }

    public int pos() {
        return this.pos;
    }

    public char current() {
        return pos >= length ? EOF : source.charAt(pos);
    }

    public Lexer accept() {
        char c = current();
        accept(c);
        return this;
    }

    public Lexer accept(char c) {
        if (c == EOF) {
            fail("Unexpected EOF");
        } else if (c == current()) {
            next();
        } else {
            fail("Expected " + c);
        }
        return this;
    }

    public Lexer accept(Consumer<Lexer> rf) {
        rf.accept(this);
        return this;
    }

    public Lexer acceptInteger() {
        if (!Character.isDigit(current())) {
            fail("Expected digits");
        }
        do {
            accept();
        } while (Character.isDigit(current()));
        return this;
    }

    public Lexer acceptNumber() {
        if (current() == '-') {
            accept('-');
        }
        acceptInteger();
        if (current() == '.') {
            accept();
            acceptInteger();

            if (current() == 'e' || current() == 'E') {
                accept();
                if (current() == '+') {
                    accept('+');
                } else {
                    accept('-');
                }
                acceptInteger();
            }
        }
        return this;
    }

    public Lexer acceptBoolean() {
        if (current() == 't') {
            accept('t');
            accept('r');
            accept('u');
            accept('e');
        } else {
            accept('f');
            accept('a');
            accept('l');
            accept('s');
            accept('e');
        }
        return this;
    }

    public Lexer acceptNull() {
        accept('n');
        accept('u');
        accept('l');
        accept('l');
        return this;
    }

    public Lexer acceptWhitespace() {
        while (Character.isWhitespace(current())) {
            accept();
        }
        return this;
    }

    public Lexer acceptString() {
        char quoteType = current();

        if (quoteType != '"' && quoteType != '\'') {
            fail("Expected ' or \" to open string literal");
        }

        accept(quoteType);
        while (current() != quoteType) {
            //TODO: escape
            accept();
        }
        accept(quoteType);
        return this;
    }

    public Lexer acceptRegex() {
        accept('/');
        do {
            //TODO: escape
            accept();
        } while (current() != '/');
        accept('/');

        if(current() == 'i'){
            accept();
        }
        return this;
    }

    public Lexer acceptJsonArray() {
        accept('[');
        acceptWhitespace();
        if (current() != ']') {
            acceptJsonValue();
            acceptWhitespace();
            while (current() == ',') {
                accept(',');
                acceptWhitespace();
                acceptJsonValue();
                acceptWhitespace();
            }
        }
        accept(']');
        return this;
    }

    public Lexer acceptJsonObject() {
        accept('{');
        acceptWhitespace();
        while (current() == '\'' || current() == '"') {

            acceptString();
            acceptWhitespace();

            accept(':');

            acceptWhitespace();
            acceptJsonValue();
            acceptWhitespace();
        }
        accept('}');
        return this;
    }

    public Lexer acceptJsonValue() {
        switch (current()) {

            case '\'':
            case '"':
                acceptString();
                acceptWhitespace();
                break;
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case '-':
            case '+':
                if (current() == '+' || current() == '-') {
                    accept();
                    acceptWhitespace();
                }
                acceptNumber();
                acceptWhitespace();
                break;
            case 't':
            case 'f':
                acceptBoolean();
                acceptWhitespace();
                break;
            case 'n':
                acceptNull();
                acceptWhitespace();
                break;
            case '[':
                acceptJsonArray();
                acceptWhitespace();
                break;
            case '{':
                acceptJsonObject();
                acceptWhitespace();
                break;
            default:
                fail("");
        }
        return this;
    }

    public Lexer acceptJson() {
        switch (current()) {
            case '[':
                acceptJsonArray();
                break;
            case '{':
                acceptJsonObject();
                break;
            default:
                fail("Expected JSON");
        }
        return this;
    }

    public Lexer emit(TokenType tokenType, boolean skipBlanks) {
        sink.accept(LexToken.of(tokenType, buffer(), start, pos));
        flush();
        return skipBlanks ? skipBlanks() : this;
    }

    public Lexer emit(TokenType tokenType) {
        return emit(tokenType, false);
    }

    public boolean hasMoreTokens() {
        return current() != EOF;
    }

    public void fail(String reason) {
        throw new LexException(reason, this);
    }

    private String buffer() {
        return source.substring(start, pos);
    }

    private String flush() {
        String buffer = buffer();
        start = pos;
        return buffer;
    }

    private Lexer skipBlanks() {
        if (start != pos) {
            fail("Can only skip blanks after emmit");
        }
        while (Character.isWhitespace(current())) {
            accept();
        }
        start = pos;
        return this;
    }

    private char next() {
        pos = min(++pos, length);
        return pos < length ? source.charAt(pos) : EOF;
    }
}
