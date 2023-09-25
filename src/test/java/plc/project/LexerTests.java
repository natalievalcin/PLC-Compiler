package plc.project;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class LexerTests {

    @ParameterizedTest
    @MethodSource
    void testIdentifier(String test, String input, boolean success) {
        test(input, Token.Type.IDENTIFIER, success);
    }

    private static Stream<Arguments> testIdentifier() {
        return Stream.of(
                Arguments.of("Alphabetic", "getName", true),
                Arguments.of("Alphanumeric", "thelegend27", true),
                Arguments.of("Leading Hyphen", "-five", false),
                Arguments.of("Leading Digit", "1fish2fish3fishbluefish", false),
                // additional test cases
                Arguments.of("Short Identifier", "a", true),
                Arguments.of("Hyphenated", "a-b-c", true),
                Arguments.of("Underscore", "___", true),
                Arguments.of("Multiple Spaces", "one   two", false),
                Arguments.of("Trailing Newline", "token\n", false),
                Arguments.of ("Not Whitespace", "one\b", false),
                Arguments.of("Capitals", "ABC", true),
                Arguments.of("Long Identifier", "abcdefghijklmnopqrstuvwxyz0123456789_-", true)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testInteger(String test, String input, boolean success) {
        test(input, Token.Type.INTEGER, success);
    }

    private static Stream<Arguments> testInteger() {
        return Stream.of(
                Arguments.of("Single Digit", "1", true),
                Arguments.of("Decimal", "123.456", false),
                Arguments.of("Signed Decimal", "-1.0", false),
                Arguments.of("Trailing Decimal", "1.", false),
                Arguments.of("Leading Decimal", ".5", false),
                // additional test cases
                Arguments.of("Positive Integer", "+123", true),
                Arguments.of("Leading Zeros", "007", true),
                Arguments.of("Multiple Digits", "123", true),
                Arguments.of("Above Long Max", "123456789123456789123456789", true)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testDecimal(String test, String input, boolean success) {
        test(input, Token.Type.DECIMAL, success);
    }

    private static Stream<Arguments> testDecimal() {
        return Stream.of(
                Arguments.of("Integer", "1", false),
                Arguments.of("Multiple Digits", "123.456", true),
                Arguments.of("Negative Decimal", "-1.0", true),
                Arguments.of("Trailing Decimal", "1.", false),
                Arguments.of("Leading Decimal", ".5", false),
                // additional test cases
                Arguments.of("Leading Zeros", "007.0", true),
                Arguments.of("Double Decimal", "1..0", false),
                Arguments.of("Multiple Decimals", "1.2.3", false),
                Arguments.of("Single Digit", "1.0", true),
                Arguments.of("Above Integer Precision", "9007199254740993.0", true),
                Arguments.of("Trailing Zeros", "111.000", true)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testCharacter(String test, String input, boolean success) {
        test(input, Token.Type.CHARACTER, success);
    }

    private static Stream<Arguments> testCharacter() {
        return Stream.of(
                Arguments.of("Alphabetic", "\'c\'", true),
                Arguments.of("Newline Escape", "\'\\n\'", true),
                Arguments.of("Empty", "\'\'", false),
                Arguments.of("Multiple", "\'abc\'", false),
                // additional test cases
                Arguments.of("Unterminated", "\'", false),
                Arguments.of("Newline", "\'\n\'", false),
                Arguments.of("Weird Quotes", "\'\"\'string\"\'\"", false),
                Arguments.of("Unterminated Character", "\'c", false),
                Arguments.of("Digit", "\'1\'", true),
                Arguments.of("Space", "\' \'", true),
                Arguments.of("Single Quote Escape", "\'\\'\'", true),
                Arguments.of("Unterminated Newline", "\'c\n\'", false),
                Arguments.of("Backslash Escape", "\'\\\\\'", true), // have question about that
                Arguments.of("Unterminated Empty", "\'", false)

        );
    }

    @ParameterizedTest
    @MethodSource
    void testString(String test, String input, boolean success) {
        test(input, Token.Type.STRING, success);
    }

    private static Stream<Arguments> testString() {
        return Stream.of(
                Arguments.of("Empty", "\"\"", true),
                Arguments.of("Alphabetic", "\"abc\"", true),
                Arguments.of("Newline Escape", "\"Hello,\\nWorld\"", true),
                Arguments.of("Unterminated", "\"unterminated", false),
                Arguments.of("Invalid Escape", "\"invalid\\escape\"", false),
                // additional test cases
                Arguments.of("Symbols", "\"!@#$%^&*()\"", true),
                Arguments.of("Newline Unterminated", "\"unterminated\n\"", false),
                Arguments.of("Vertical Tab", "\"Hello\u000Bworld\"", true),
                Arguments.of("Form Feed", "\"Hello\fworld\"", true),
                Arguments.of("Weird Quotes", "\'\"\'string \"\'\"", false),
                Arguments.of("Single Character", "\"c\"", true),
                Arguments.of("Numeric", "\"123\"", true),
                Arguments.of("Whitespace", "\" \b\t\"", true), // not sure
                Arguments.of("Escape", "\"Hello, \\nWorld!\"", true),
                Arguments.of("Alphabetic Escapes", "\"a\\bcdefghijklm\\nopq\\rs\\tuvwxyz\"", true),
                Arguments.of("Special Escape", "\"sq\\'dq\\\"bs\\\\\"", true),
                Arguments.of("Invalid Escape", "\"abc\\0123\"", false),
                Arguments.of("Unicode Escapes", "\"a\u0000b\u12ABc\"", true) // not sure what the heck!
        );
    }

    @ParameterizedTest
    @MethodSource
    void testOperator(String test, String input, boolean success) {
        //this test requires our lex() method, since that's where whitespace is handled.
        test(input, Arrays.asList(new Token(Token.Type.OPERATOR, input, 0)), success);
    }

    private static Stream<Arguments> testOperator() {
        return Stream.of(
                Arguments.of("Character", "(", true),
                Arguments.of("Comparison", "<=", true),
                Arguments.of("Space", " ", false),
                Arguments.of("Tab", "\t", false),
                // additional test cases
                Arguments.of("Symbols", "$", true),
                Arguments.of("Plus Sign", "+", true),
                Arguments.of("Remainder", "%", true),
                Arguments.of("Less Than or Equals", "<=", true),
                Arguments.of("Greater Than", ">", true),
                Arguments.of("Not Equals", "!=", true),
                Arguments.of("Plus", "+", true),
                Arguments.of("Hyphen", "-", true),
                Arguments.of("Form Feed", "\f", false) // not sure
        );
    }

    @ParameterizedTest
    @MethodSource
    void testExamples(String test, String input, List<Token> expected) {
        test(input, expected, true);
    }

    private static Stream<Arguments> testExamples() {
        return Stream.of(
                Arguments.of("Example 1", "LET x = 5;", Arrays.asList(
                        new Token(Token.Type.IDENTIFIER, "LET", 0),
                        new Token(Token.Type.IDENTIFIER, "x", 4),
                        new Token(Token.Type.OPERATOR, "=", 6),
                        new Token(Token.Type.INTEGER, "5", 8),
                        new Token(Token.Type.OPERATOR, ";", 9)
                )),
                Arguments.of("Example 2", "print(\"Hello, World!\");", Arrays.asList(
                        new Token(Token.Type.IDENTIFIER, "print", 0),
                        new Token(Token.Type.OPERATOR, "(", 5),
                        new Token(Token.Type.STRING, "\"Hello, World!\"", 6),
                        new Token(Token.Type.OPERATOR, ")", 21),
                        new Token(Token.Type.OPERATOR, ";", 22)
                )),
                Arguments.of("Example 6", "one\btwo", Arrays.asList(
                        new Token(Token.Type.IDENTIFIER, "one", 0),
                        new Token(Token.Type.IDENTIFIER, "two", 4)
                )),
                Arguments.of("Example 8", "\'\"\'string\"\'\"", Arrays.asList(
                        new Token(Token.Type.CHARACTER, "\'\"\'", 0),
                        new Token(Token.Type.IDENTIFIER, "string", 3),
                        new Token(Token.Type.STRING, "\"'\"", 9)
                )),
                Arguments.of("FizzBuzz", "LET i = 1;\n" +
                        "WHILE i <= 100 DO\n" +
                        "    IF rem(i, 3) == 0 AND rem(i, 5) == 0 DO\n" +
                        "        print(\"FizzBuzz\");\n" +
                        "    ELSE IF rem(i, 3) == 0 DO\n" +
                        "        print(\"Fizz\");\n" +
                        "    ELSE IF rem(i, 5) == 0 DO\n" +
                        "        print(\"Buzz\");\n" +
                        "    ELSE\n" +
                        "        print(i);\n" +
                        "    END END END\n" +
                        "    i = i + 1;\n" +
                        "END", Arrays.asList(
                        new Token(Token.Type.IDENTIFIER, "LET", 0),
                        new Token(Token.Type.IDENTIFIER, "i", 4),
                        new Token(Token.Type.OPERATOR, "=", 6),
                        new Token(Token.Type.INTEGER, "1", 8),
                        new Token(Token.Type.OPERATOR, ";", 9),
                        new Token(Token.Type.IDENTIFIER, "WHILE", 10)
                ))
        );
    }

    @Test
    void testException() {
        ParseException exception = Assertions.assertThrows(ParseException.class,
                () -> new Lexer("\"unterminated").lex());
        Assertions.assertEquals(13, exception.getIndex());
    }

    /**
     * Tests that lexing the input through {@link Lexer#lexToken()} produces a
     * single token with the expected type and literal matching the input.
     */
    private static void test(String input, Token.Type expected, boolean success) {
        try {
            if (success) {
                Assertions.assertEquals(new Token(expected, input, 0), new Lexer(input).lexToken());
            } else {
                Assertions.assertNotEquals(new Token(expected, input, 0), new Lexer(input).lexToken());
            }
        } catch (ParseException e) {
            Assertions.assertFalse(success, e.getMessage());
        }
    }

    /**
     * Tests that lexing the input through {@link Lexer#lex()} matches the
     * expected token list.
     */
    private static void test(String input, List<Token> expected, boolean success) {
        try {
            if (success) {
                Assertions.assertEquals(expected, new Lexer(input).lex());
            } else {
                Assertions.assertNotEquals(expected, new Lexer(input).lex());
            }
        } catch (ParseException e) {
            Assertions.assertFalse(success, e.getMessage());
        }
    }

}
