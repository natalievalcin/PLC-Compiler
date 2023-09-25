package plc.project;

import java.util.ArrayList;
import java.util.List;

/**
 * The lexer works through three main functions:
 *
 *  - {@link #lex()}, which repeatedly calls lexToken() and skips whitespace
 *  - {@link #lexToken()}, which lexes the next token
 *  - {@link CharStream}, which manages the state of the lexer and literals
 *
 * If the lexer fails to parse something (such as an unterminated string) you
 * should throw a {@link ParseException} with an index at the character which is
 * invalid or missing.
 *
 * The {@link #peek(String...)} and {@link #match(String...)} functions are
 * helpers you need to use, they will make the implementation a lot easier.
 */
public final class Lexer {

    private final CharStream chars;

    public Lexer(String input) {
        chars = new CharStream(input);
    }

    /**
     * Repeatedly lexes the input using {@link #lexToken()}, also skipping over
     * whitespace where appropriate.
     */
    public List<Token> lex() {
        // initialize an empty list to store tokens found in source code
        List<Token> tokens = new ArrayList<>();

        while (peek("(.)")) {
            if (peek("[\f\u000B\\s\b\n\r\t]")) {
                chars.advance();
                chars.skip();
            } else {
                // create a token
                Token token = lexToken();
                // add the token to the list
                tokens.add(token);
            }
        }
            return tokens;
    }

    /**
     * This method determines the type of the next token, delegating to the
     * appropriate lex method. As such, it is best for this method to not change
     * the state of the char stream (thus, use peek not match).
     *
     * The next character should start a valid token since whitespace is handled
     * by {@link #lex()}
     */
    public Token lexToken() {
        if (peek("[A-Za-z_]"))  // had to remove the hyphen and digits as an "identifier start" excludes this character
            return lexIdentifier();
        else if (peek("[\\-+]","[0-9]") || peek("[0-9]"))
            return lexNumber();
        else if (peek("\'"))
            return lexCharacter();
        else if (peek("\""))
            return lexString();
        else return lexOperator();

    }

    public Token lexIdentifier() {
        while (peek("[A-Za-z_0-9_-]")) {
            match("[A-Za-z_0-9_-]");
        }
            return chars.emit(Token.Type.IDENTIFIER);
    }

    public Token lexNumber() {
        if (peek("[-+]"))
            match("[-+]");
        while (peek("[0-9]"))
            match("[0-9]");
        if (peek("\\.", "[0-9]")) {
            match("\\.");
            while (peek("[0-9]"))
                match("[0-9]");
            return chars.emit(Token.Type.DECIMAL);
        }
        return chars.emit(Token.Type.INTEGER);
    }

    public Token lexCharacter() {
        if (peek("\'")) {
            match("\'");
        }
        if (peek("\\\\")) {
            lexEscape();
        } else if (peek("A-Za-z")) {
            match("A-Za-z");
        } else if (peek("[^\'\\n\\r\\\\]")) {
            match("[^\'\\n\\r\\\\]");
        } else {
            throw new ParseException("invalid", chars.index); //TODO
        }


        if (peek("\'")) {
            match("\'");
            return chars.emit(Token.Type.CHARACTER);
        }
        throw new ParseException("invalid", chars.index);


    }

    public Token lexString() {
        if (peek("\"")) {
            match("\"");
        }
        // Professor Aashish assisted with the commented portion of the code during 09/15 OH
        // while peek ^\n\r
        // if peeks  4 \ call lexescape
        // else advance
        // check for closing " quotes outside while loop
        while(peek("[^\"\\n\\r]")) {
            if (peek("\\\\"))
                lexEscape();
            else
                match(".");
        }
        if (peek("\""))
            match("\"");
        else
            throw new ParseException("invalid", chars.index);
        return chars.emit(Token.Type.STRING);
    }

    public void lexEscape() { //
        match("\\\\");
        if (!match("[bnrt\'\"\\\\]"))
            throw new ParseException("CANNOT", chars.index);
    }

    public Token lexOperator() {
        if (peek("[<>!=]","="))
            match("[<>!=]","=");
        else
            match("(.)");
        return chars.emit(Token.Type.OPERATOR);
    }

    /**
     * Returns true if the next sequence of characters match the given patterns,
     * which should be a regex. For example, {@code peek("a", "b", "c")} would
     * return true if the next characters are {@code 'a', 'b', 'c'}.
     */
    public boolean peek(String... patterns) {
         for (int i = 0 ; i < patterns.length; i++) {
             if (!chars.has(i) || !String.valueOf(chars.get(i)).matches(patterns[i])) {
                 return false;
             }
         }
         return true;
    }

    /**
     * Returns true in the same way as {@link #peek(String...)}, but also
     * advances the character stream past all matched characters if peek returns
     * true. Hint - it's easiest to have this method simply call peek.
     */
    public boolean match(String... patterns) {
        boolean peek = peek(patterns);

        if (peek) {
            for (int i = 0; i < patterns.length; i++) {
                chars.advance();
            }
        }
        return peek;
    }

    /**
     * A helper class maintaining the input string, current index of the char
     * stream, and the current length of the token being matched.
     *
     * You should rely on peek/match for state management in nearly all cases.
     * The only field you need to access is {@link #index} for any {@link
     * ParseException} which is thrown.
     */
    public static final class CharStream {

        private final String input;
        private int index = 0;
        private int length = 0;

        public CharStream(String input) {
            this.input = input;
        }

        public boolean has(int offset) {
            return index + offset < input.length();
        }

        public char get(int offset) {
            return input.charAt(index + offset);
        }

        public void advance() {
            index++;
            length++;
        }

        public void skip() {
            length = 0;
        }

        public Token emit(Token.Type type) {
            int start = index - length;
            skip();
            return new Token(type, input.substring(start, index), start);
        }

    }

}
