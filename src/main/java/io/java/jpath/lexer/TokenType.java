package io.java.jpath.lexer;


public enum TokenType {
    ROOT_TOKEN,
    SCAN_TOKEN,
    PERIOD_TOKEN,
    FUNCTION_NAME,
    OBJECT_PROPERTY,
    COMMA,
    SLICE_OPERATOR,
    FILTER_PREDICATE,
    OPEN_PARESIS,
    CLOSE_PARESIS,
    OPEN_SELECTOR,
    CLOSE_SELECTOR,
    IDENTIFIER,
    WILDCARD,

    //--------------------------------
    //
    // literals
    //
    //--------------------------------
    JSON_LITERAL,
    STRING_LITERAL,
    BOOLEAN_LITERAL,
    NUMBER_LITERAL,
    NULL_LITERAL,
    REGEX,

    //--------------------------------
    //
    // operators
    //
    //--------------------------------
    OR,
    AND,

    CONTEXT_TOKEN,
    NOT,
    EOP,
    OPERATOR;

    public boolean is(TokenType other){
        return this == other;
    }

    public boolean isNot(TokenType other){
        return this != other;
    }

    public boolean in(TokenType... valid) {
        for (TokenType check : valid) {
            if(is(check)){
                return true;
            }
        }
        return false;
    }
}
