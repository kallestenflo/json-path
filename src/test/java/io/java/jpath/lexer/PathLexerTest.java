package io.java.jpath.lexer;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static io.java.jpath.lexer.TokenType.PERIOD_TOKEN;
import static io.java.jpath.lexer.TokenType.SCAN_TOKEN;
import static io.java.jpath.lexer.Lexer.EOF;
import static io.java.jpath.lexer.TokenType.*;
import static org.assertj.core.api.Assertions.assertThat;

public class PathLexerTest {

    @Test
    public void lexPeriodsToken_test() {
        assertLex(".",
                PathLexer::lexPeriodsToken,
                LexToken.of(PERIOD_TOKEN, ".", 0, 1)
        );

        assertLex("..",
                PathLexer::lexPeriodsToken,
                LexToken.of(SCAN_TOKEN, "..", 0, 2)
        );
    }

    @Test
    public void lexSelectorToken_property_test() {
        assertLex("['prop']",
                PathLexer::lexSelectorToken,
                LexToken.of(OPEN_SELECTOR, "[", 0, 1),
                LexToken.of(OBJECT_PROPERTY, "'prop'", 1, 7),
                LexToken.of(CLOSE_SELECTOR, "]", 7, 8)
        );

        assertLex("[\"prop\"]",
                PathLexer::lexSelectorToken,
                LexToken.of(OPEN_SELECTOR, "[", 0, 1),
                LexToken.of(OBJECT_PROPERTY, "\"prop\"", 1, 7),
                LexToken.of(CLOSE_SELECTOR, "]", 7, 8)
        );

        assertLex("['a', 'b']",
                PathLexer::lexSelectorToken,
                LexToken.of(OPEN_SELECTOR, "[", 0, 1),
                LexToken.of(OBJECT_PROPERTY, "'a'", 1, 4),
                LexToken.of(COMMA, ",", 4, 5),
                LexToken.of(OBJECT_PROPERTY, "'b'", 6, 9),
                LexToken.of(CLOSE_SELECTOR, "]", 9, 10)
        );

        assertLex("[\"prop-a\", \"prop-b\"]",
                PathLexer::lexSelectorToken,
                LexToken.of(OPEN_SELECTOR, "[", 0, 1),
                LexToken.of(OBJECT_PROPERTY, "\"prop-a\"", 1, 9),
                LexToken.of(COMMA, ",", 9, 10),
                LexToken.of(OBJECT_PROPERTY, "\"prop-b\"", 11, 19),
                LexToken.of(CLOSE_SELECTOR, "]", 19, 20)
        );

        assertLex("[  'prop-a'  ,   \"prop-b\"  ]",
                PathLexer::lexSelectorToken,
                LexToken.of(OPEN_SELECTOR, "[", 0, 1),
                LexToken.of(OBJECT_PROPERTY, "'prop-a'", 3, 11),
                LexToken.of(COMMA, ",", 13, 14),
                LexToken.of(OBJECT_PROPERTY, "\"prop-b\"", 17, 25),
                LexToken.of(CLOSE_SELECTOR, "]", 27, 28)
        );

        assertLexFail("['a', ]",
                PathLexer::lexSelectorToken,
                "Expected ' or \" to open string literal at position: 6 but found: ]");

        assertLexFail("['a',6]",
                PathLexer::lexSelectorToken,
                "Expected ' or \" to open string literal at position: 5 but found: 6");

        assertLexFail("['a'",
                PathLexer::lexSelectorToken,
                "Expected ] at position: 4 but found: EOP");
    }

    @Test
    public void lexSelectorToken_array_indices_test() {
        assertLex("[1]",
                PathLexer::lexSelectorToken,
                LexToken.of(OPEN_SELECTOR, "[", 0, 1),
                LexToken.of(NUMBER_LITERAL, "1", 1, 2),
                LexToken.of(CLOSE_SELECTOR, "]", 2, 3)
        );

        assertLex("[1, 6, 7]",
                PathLexer::lexSelectorToken,
                LexToken.of(OPEN_SELECTOR, "[", 0, 1),
                LexToken.of(NUMBER_LITERAL, "1", 1, 2),
                LexToken.of(COMMA, ",", 2, 3),
                LexToken.of(NUMBER_LITERAL, "6", 4, 5),
                LexToken.of(COMMA, ",", 5, 6),
                LexToken.of(NUMBER_LITERAL, "7", 7, 8),
                LexToken.of(CLOSE_SELECTOR, "]", 8, 9)
        );
    }

    @Test
    public void lexSelectorToken_array_slice_test() {
        assertLex("[:1]",
                PathLexer::lexSelectorToken,
                LexToken.of(OPEN_SELECTOR, "[", 0, 1),
                LexToken.of(SLICE_OPERATOR, ":", 1, 2),
                LexToken.of(NUMBER_LITERAL, "1", 2, 3),
                LexToken.of(CLOSE_SELECTOR, "]", 3, 4)
        );

        assertLex("[::]",
                PathLexer::lexSelectorToken,
                LexToken.of(OPEN_SELECTOR, "[", 0, 1),
                LexToken.of(SLICE_OPERATOR, ":", 1, 2),
                LexToken.of(SLICE_OPERATOR, ":", 2, 3),
                LexToken.of(CLOSE_SELECTOR, "]", 3, 4)
        );

        assertLex("[-1:]",
                PathLexer::lexSelectorToken,
                LexToken.of(OPEN_SELECTOR, "[", 0, 1),
                LexToken.of(NUMBER_LITERAL, "-1", 1, 3),
                LexToken.of(SLICE_OPERATOR, ":", 3, 4),
                LexToken.of(CLOSE_SELECTOR, "]", 4, 5)
        );

        assertLex("[::2]",
                PathLexer::lexSelectorToken,
                LexToken.of(OPEN_SELECTOR, "[", 0, 1),
                LexToken.of(SLICE_OPERATOR, ":", 1, 2),
                LexToken.of(SLICE_OPERATOR, ":", 2, 3),
                LexToken.of(NUMBER_LITERAL, "2", 3, 4),
                LexToken.of(CLOSE_SELECTOR, "]", 4, 5)
        );

        assertLex("[1 : 2]",
                PathLexer::lexSelectorToken,
                LexToken.of(OPEN_SELECTOR, "[", 0, 1),
                LexToken.of(NUMBER_LITERAL, "1", 1, 2),
                LexToken.of(SLICE_OPERATOR, ":", 3, 4),
                LexToken.of(NUMBER_LITERAL, "2", 5, 6),
                LexToken.of(CLOSE_SELECTOR, "]", 6, 7)
        );

        assertLex("[0 : -1 : 2]",
                PathLexer::lexSelectorToken,
                LexToken.of(OPEN_SELECTOR, "[", 0, 1),
                LexToken.of(NUMBER_LITERAL, "0", 1, 2),
                LexToken.of(SLICE_OPERATOR, ":", 3, 4),
                LexToken.of(NUMBER_LITERAL, "-1", 5, 7),
                LexToken.of(SLICE_OPERATOR, ":", 8, 9),
                LexToken.of(NUMBER_LITERAL, "2", 10, 11),
                LexToken.of(CLOSE_SELECTOR, "]", 11, 12)
        );

        assertLex("[0 : -1 : 2]",
                PathLexer::lexSelectorToken,
                LexToken.of(OPEN_SELECTOR, "[", 0, 1),
                LexToken.of(NUMBER_LITERAL, "0", 1, 2),
                LexToken.of(SLICE_OPERATOR, ":", 3, 4),
                LexToken.of(NUMBER_LITERAL, "-1", 5, 7),
                LexToken.of(SLICE_OPERATOR, ":", 8, 9),
                LexToken.of(NUMBER_LITERAL, "2", 10, 11),
                LexToken.of(CLOSE_SELECTOR, "]", 11, 12)
        );

        assertLex("[0::2]",
                PathLexer::lexSelectorToken,
                LexToken.of(OPEN_SELECTOR, "[", 0, 1),
                LexToken.of(NUMBER_LITERAL, "0", 1, 2),
                LexToken.of(SLICE_OPERATOR, ":", 2, 3),
                LexToken.of(SLICE_OPERATOR, ":", 3, 4),
                LexToken.of(NUMBER_LITERAL, "2", 4, 5),
                LexToken.of(CLOSE_SELECTOR, "]", 5, 6)
        );

        assertLex("[:1:]",
                PathLexer::lexSelectorToken,
                LexToken.of(OPEN_SELECTOR, "[", 0, 1),
                LexToken.of(SLICE_OPERATOR, ":", 1, 2),
                LexToken.of(NUMBER_LITERAL, "1", 2, 3),
                LexToken.of(SLICE_OPERATOR, ":", 3, 4),
                LexToken.of(CLOSE_SELECTOR, "]", 4, 5)
        );

        assertLexFail("[0:1:2:3]",
                PathLexer::lexSelectorToken,
                "Expected ] at position: 6 but found: :"
        );

        assertLexFail("[:1::]",
                PathLexer::lexSelectorToken,
                "Expected ] at position: 4 but found: :"
        );
    }

    @Test
    public void lexSelectorToken_wildcard_test() {
        assertLex("[*]",
                PathLexer::lexSelectorToken,
                LexToken.of(OPEN_SELECTOR, "[", 0, 1),
                LexToken.of(WILDCARD, "*", 1, 2),
                LexToken.of(CLOSE_SELECTOR, "]", 2, 3)
        );

        assertLex("[  *  ]",
                PathLexer::lexSelectorToken,
                LexToken.of(OPEN_SELECTOR, "[", 0, 1),
                LexToken.of(WILDCARD, "*", 3, 4),
                LexToken.of(CLOSE_SELECTOR, "]", 6, 7)
        );

        assertLexFail("[*",
                PathLexer::lexSelectorToken,
                "Expected ] at position: 2 but found: EOP"
        );
    }

    @Test
    public void lexSelectorToken_empty_selector_test() {
        assertLexFail("[]",
                PathLexer::lexSelectorToken,
                "Expected selector predicate at position: 1 but found: ]"
        );
    }

    @Test
    public void lexSelectorToken_predicate_test() {
        assertLex("[?(true)]",
                PathLexer::lexSelectorToken,
                LexToken.of(OPEN_SELECTOR, "[", 0, 1),
                LexToken.of(FILTER_PREDICATE, "?", 1, 2),
                LexToken.of(OPEN_PARESIS, "(", 2, 3),
                LexToken.of(BOOLEAN_LITERAL, "true", 3, 7),
                LexToken.of(CLOSE_PARESIS, ")", 7, 8),
                LexToken.of(CLOSE_SELECTOR, "]", 8, 9)
        );

        assertLex("[?('bar' == 'bar')]",
                PathLexer::lexSelectorToken,
                LexToken.of(OPEN_SELECTOR, "[", 0, 1),
                LexToken.of(FILTER_PREDICATE, "?", 1, 2),
                LexToken.of(OPEN_PARESIS, "(", 2, 3),
                LexToken.of(STRING_LITERAL, "'bar'", 3, 8),
                LexToken.of(OPERATOR, "==", 9, 11),
                LexToken.of(STRING_LITERAL, "'bar'", 12, 17),
                LexToken.of(CLOSE_PARESIS, ")", 17, 18),
                LexToken.of(CLOSE_SELECTOR, "]", 18, 19)
        );

        assertLex("[?(@.bar == 'bar')]",
                PathLexer::lexSelectorToken,
                LexToken.of(OPEN_SELECTOR, "[", 0, 1),
                LexToken.of(FILTER_PREDICATE, "?", 1, 2),
                LexToken.of(OPEN_PARESIS, "(", 2, 3),
                LexToken.of(CONTEXT_TOKEN, "@", 3, 4),
                LexToken.of(PERIOD_TOKEN, ".", 4, 5),
                LexToken.of(IDENTIFIER, "bar", 5, 8),
                LexToken.of(EOP, "", 8, 8),
                LexToken.of(OPERATOR, "==", 9, 11),
                LexToken.of(STRING_LITERAL, "'bar'", 12, 17),
                LexToken.of(CLOSE_PARESIS, ")", 17, 18),
                LexToken.of(CLOSE_SELECTOR, "]", 18, 19)
        );

        assertLex("[?(@['b a z'] =~ /b.*/i)]",
                PathLexer::lexSelectorToken,
                LexToken.of(OPEN_SELECTOR, "[", 0, 1),
                LexToken.of(FILTER_PREDICATE, "?", 1, 2),
                LexToken.of(OPEN_PARESIS, "(", 2, 3),
                LexToken.of(CONTEXT_TOKEN, "@", 3, 4),
                LexToken.of(OPEN_SELECTOR, "[", 4, 5),
                LexToken.of(OBJECT_PROPERTY, "'b a z'", 5, 12),
                LexToken.of(CLOSE_SELECTOR, "]", 12, 13),
                LexToken.of(EOP, "", 13, 13),
                LexToken.of(OPERATOR, "=~", 14, 16),
                LexToken.of(REGEX, "/b.*/i", 17, 23),
                LexToken.of(CLOSE_PARESIS, ")", 23, 24),
                LexToken.of(CLOSE_SELECTOR, "]", 24, 25)
        );

        assertLex("[?(!!@.bar)]",
                PathLexer::lexSelectorToken,
                LexToken.of(OPEN_SELECTOR, "[", 0, 1),
                LexToken.of(FILTER_PREDICATE, "?", 1, 2),
                LexToken.of(OPEN_PARESIS, "(", 2, 3),
                LexToken.of(NOT, "!", 3, 4),
                LexToken.of(NOT, "!", 4, 5),
                LexToken.of(CONTEXT_TOKEN, "@", 5, 6),
                LexToken.of(PERIOD_TOKEN, ".", 6, 7),
                LexToken.of(IDENTIFIER, "bar", 7, 10),
                LexToken.of(EOP, "", 10, 10),
                LexToken.of(CLOSE_PARESIS, ")", 10, 11),
                LexToken.of(CLOSE_SELECTOR, "]", 11, 12)
        );

        assertLex("[?(!(true && 1 == 1))]",
                PathLexer::lexSelectorToken,
                LexToken.of(OPEN_SELECTOR, "[", 0, 1),
                LexToken.of(FILTER_PREDICATE, "?", 1, 2),
                LexToken.of(OPEN_PARESIS, "(", 2, 3),
                LexToken.of(NOT, "!", 3, 4),
                LexToken.of(OPEN_PARESIS, "(", 4, 5),
                LexToken.of(BOOLEAN_LITERAL, "true", 5, 9),
                LexToken.of(AND, "&&", 10, 12),
                LexToken.of(NUMBER_LITERAL, "1", 13, 14),
                LexToken.of(OPERATOR, "==", 15, 17),
                LexToken.of(NUMBER_LITERAL, "1", 18, 19),
                LexToken.of(CLOSE_PARESIS, ")", 19, 20),
                LexToken.of(CLOSE_PARESIS, ")", 20, 21),
                LexToken.of(CLOSE_SELECTOR, "]", 21, 22)
        );

        assertLex("[?(true)]",
                PathLexer::lexSelectorToken,
                LexToken.of(OPEN_SELECTOR, "[", 0, 1),
                LexToken.of(FILTER_PREDICATE, "?", 1, 2),
                LexToken.of(OPEN_PARESIS, "(", 2, 3),
                LexToken.of(BOOLEAN_LITERAL, "true", 3, 7),
                LexToken.of(CLOSE_PARESIS, ")", 7, 8),
                LexToken.of(CLOSE_SELECTOR, "]", 8, 9)
        );

        assertLex("[?(true && false || (true && !false || true))]",
                PathLexer::lexSelectorToken,
                LexToken.of(OPEN_SELECTOR, "[", 0, 1),
                LexToken.of(FILTER_PREDICATE, "?", 1, 2),
                LexToken.of(OPEN_PARESIS, "(", 2, 3),
                LexToken.of(BOOLEAN_LITERAL, "true", 3, 7),
                LexToken.of(AND, "&&", 8, 10),
                LexToken.of(BOOLEAN_LITERAL, "false", 11, 16),
                LexToken.of(OR, "||", 17, 19),
                LexToken.of(OPEN_PARESIS, "(", 20, 21),
                LexToken.of(BOOLEAN_LITERAL, "true", 21, 25),
                LexToken.of(AND, "&&", 26, 28),
                LexToken.of(NOT, "!", 29, 30),
                LexToken.of(BOOLEAN_LITERAL, "false", 30, 35),
                LexToken.of(OR, "||", 36, 38),
                LexToken.of(BOOLEAN_LITERAL, "true", 39, 43),
                LexToken.of(CLOSE_PARESIS, ")", 43, 44),
                LexToken.of(CLOSE_PARESIS, ")", 44, 45),
                LexToken.of(CLOSE_SELECTOR, "]", 45, 46)
        );
    }

    @Test
    public void lexIdentifierToken_test(){
        assertLex("identifier",
                PathLexer::lexIdentifierToken,
                LexToken.of(IDENTIFIER, "identifier", 0, 10)
        );
    }

    //-----------------------------------------------------
    //
    // Helpers
    //
    //-----------------------------------------------------
    private static void assertLexFail(String src, Consumer<Lexer> lexFn, String message) {
        List<LexToken> sink = new ArrayList<>();
        Lexer lexer = Lexer.of(src, sink::add);
        try {
            lexFn.accept(lexer);
        } catch (LexException e) {
            assertThat(e.getMessage()).isEqualTo(message);
            return;
        }
        Assertions.fail("Expected lex to fail with LexException: " + message);
    }

    private static void assertLex(String src, Consumer<Lexer> lexFn, LexToken... expected) {
        List<LexToken> sink = new ArrayList<>();
        Lexer lexer = Lexer.of(src, sink::add);
        lexFn.accept(lexer);
        try {
            assertThat(sink).containsExactly(expected);
        } catch (Throwable e) {
            sink.forEach(t -> {
                String s = String.format("LexToken.visitor(%s, \"%s\", %s, %s),", t.type().name(), t.getToken(), t.getStartIndex(), t.getEndIndex());
                System.out.println(s);
            });

            throw e;
        }
        assertThat(lexer.current()).isEqualTo(EOF);
    }
}