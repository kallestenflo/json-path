package io.java.jpath.lexer;

import java.util.function.Consumer;

public class PathLexer {

    private static final String OPERATOR_CHARS = "=<>!~";
    private static final String IDENTIFIER_TERMINATOR_CHARS = OPERATOR_CHARS + " .,()[";

    public static void lex(String path, Consumer<LexToken> sink) {
        Lexer lexer = Lexer.of(path, sink);
        lex(lexer);
        assert !lexer.hasMoreTokens();
    }

    public static void lex(Lexer l) {
        loop:
        while (l.hasMoreTokens()) {
            switch (l.current()) {
                case '$':
                    l.accept('$').emit(TokenType.ROOT_TOKEN);
                    break;
                case '@':
                    l.accept('@').emit(TokenType.CONTEXT_TOKEN);
                    break;
                case '[':
                    lexSelectorToken(l);
                    break;
                case '.':
                    lexPeriodsToken(l);
                    break;
                case '*':
                    l.accept('*').emit(TokenType.WILDCARD);
                    break;
                case ' ':
                case '>':
                case '<':
                case '!':
                case '=':
                case ',':
                case ')':
                    break loop;
                default:
                    lexIdentifierToken(l);
            }
        }
        l.emit(TokenType.EOP, true);
    }

    static void lexPeriodsToken(Lexer l) {
        l.accept('.');
        if (l.current() == '.') {
            l.accept('.').emit(TokenType.SCAN_TOKEN);
        } else {
            l.emit(TokenType.PERIOD_TOKEN);
        }
    }

    static void lexSelectorToken(Lexer l) {
        l.accept('[').emit(TokenType.OPEN_SELECTOR, true);
        switch (l.current()) {
            case '"':
            case '\'':
                lexObjectSelector(l);
                break;
            case '?':
                lexPredicateSelector(l);
                break;
            case '*':
                l.accept('*').emit(TokenType.WILDCARD, true);
                break;
            case '-':
            case ':':
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
                lexArraySelector(l);
                break;
            default:
                l.fail("Expected selector predicate");
        }
        l.accept(']').emit(TokenType.CLOSE_SELECTOR, false);
    }

    static void lexIdentifierToken(Lexer l) {
        boolean isIdentifier = !isIdentifierTerminator(l);
        do {
            l.accept();
        }
        while (!isIdentifierTerminator(l));

        if (!isIdentifier) {
            l.fail("Expected identifier");
        }

        if (l.current() == '(') {
            l.emit(TokenType.FUNCTION_NAME);
            lexParameters(l);
        } else {
            l.emit(TokenType.IDENTIFIER);
        }
    }

    private static void lexPredicateSelector(Lexer l) {
        l.accept('?').emit(TokenType.FILTER_PREDICATE, true);
        l.accept('(').emit(TokenType.OPEN_PARESIS, true);
        lexExpression(l);
        l.accept(')').emit(TokenType.CLOSE_PARESIS, true);
    }

    private static void lexExpression(Lexer l) {

        while (l.current() == '!') {
            l.accept('!').emit(TokenType.NOT, true);
        }

        switch (l.current()) {
            case '(':
                l.accept('(').emit(TokenType.OPEN_PARESIS, true);
                lexExpression(l);
                l.accept(')').emit(TokenType.CLOSE_PARESIS, true);
                break;
            default:
                lexLiteral(l);
                if (isComparisonOperator(l)) {
                    // a comparison literal, any of: '=<>!~' can terminate a literal, eg @.open==true
                    //
                    lexComparisonOperator(l);
                    lexExpression(l);
                }
                break;
        }

        while (isLogicalOperator(l)) {
            lexLogicalOperator(l);
            lexExpression(l);
        }
    }

    private static void lexComparisonOperator(Lexer l) {
        boolean isOperator = isComparisonOperator(l);
        do {
            l.accept();
        }
        while (isComparisonOperator(l));

        if (!isOperator) {
            l.fail("Expected identifier");
        }
        l.emit(TokenType.OPERATOR, true);
    }

    private static void lexLogicalOperator(Lexer l) {
        if (l.current() == '&') {
            l.accept('&');
            l.accept('&');
            l.emit(TokenType.AND, true);
        } else {
            l.accept('|');
            l.accept('|');
            l.emit(TokenType.OR, true);
        }
    }

    private static void lexParameters(Lexer l) {
        l.accept('(').emit(TokenType.OPEN_PARESIS, true);

        if (l.current() != ')') {
            lexLiteral(l);

            while (l.current() == ',') {
                l.accept(',').emit(TokenType.COMMA, true);
                lexLiteral(l);
            }
        }
        l.accept(')').emit(TokenType.CLOSE_PARESIS, false);
    }

    private static void lexLiteral(Lexer l) {
        switch (l.current()) {
            case 't':
            case 'f':
                l.acceptBoolean().emit(TokenType.BOOLEAN_LITERAL, true);
                break;
            case 'n':
                l.acceptNull().emit(TokenType.NULL_LITERAL, true);
                break;
            case '\'':
            case '"':
                l.acceptString().emit(TokenType.STRING_LITERAL, true);
                break;
            case '{':
            case '[':
                l.acceptJson().emit(TokenType.JSON_LITERAL, true);
                break;
            case '-':
            case '+':
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
                l.acceptNumber().emit(TokenType.NUMBER_LITERAL, true);
                break;
            case '@':
            case '$':
                lex(l);
                break;
            case '/':
                l.acceptRegex().emit(TokenType.REGEX, true);
                break;
            default:
                l.fail("Expected literal");
        }
    }

    private static void lexObjectSelector(Lexer l) {
        l.acceptString().emit(TokenType.OBJECT_PROPERTY, true);
        while (l.current() == ',') {
            l.accept(',')
             .emit(TokenType.COMMA, true)
             .acceptString()
             .emit(TokenType.OBJECT_PROPERTY, true);
        }
    }

    private static void lexArraySelector(Lexer l) {
        boolean found = false;
        int colonCount = 0;
        if (l.current() == '-' || Character.isDigit(l.current())) {
            l.acceptNumber().emit(TokenType.NUMBER_LITERAL, true);
            found = true;
        }
        while (l.current() == ':') {
            l.accept(':').emit(TokenType.SLICE_OPERATOR, true);
            colonCount++;
            if (l.current() == '-' || Character.isDigit(l.current())) {
                l.acceptNumber().emit(TokenType.NUMBER_LITERAL, true);
            }
            if (colonCount == 2) {
                return;
            }
            found = true;
        }
        while (l.current() == ',') {
            l.accept(',').emit(TokenType.COMMA, true);
            l.acceptNumber().emit(TokenType.NUMBER_LITERAL, true);
            found = true;
        }

        if (!found) {
            l.fail("Expected array selector");
        }
    }

    private static boolean isLogicalOperator(Lexer l) {
        return l.current() == '&' || l.current() == '|';
    }

    private static boolean isComparisonOperator(Lexer l) {
        return OPERATOR_CHARS.indexOf(l.current()) != -1;
    }

    private static boolean isIdentifierTerminator(Lexer l) {
        return IDENTIFIER_TERMINATOR_CHARS.indexOf(l.current()) != -1
                || !l.hasMoreTokens();
    }
}
