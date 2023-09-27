package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The parser takes the sequence of tokens emitted by the lexer and turns that
 * into a structured representation of the program, called the Abstract Syntax
 * Tree (AST).
 *
 * The parser has a similar architecture to the lexer, just with {@link Token}s
 * instead of characters. As before, {@link #peek(Object...)} and {@link
 * #match(Object...)} are helpers to make the implementation easier.
 *
 * This type of parser is called <em>recursive descent</em>. Each rule in our
 * grammar will have it's own function, and reference to other rules correspond
 * to calling that functions.
 */
public final class Parser {

    private final TokenStream tokens;

    public Parser(List<Token> tokens) {
        this.tokens = new TokenStream(tokens);
    }

    /**
     * Parses the {@code source} rule.
     */
    public Ast.Source parseSource() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code field} rule. This method should only be called if the
     * next tokens start a field, aka {@code LET}.
     */
    public Ast.Field parseField() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code method} rule. This method should only be called if the
     * next tokens start a method, aka {@code DEF}.
     */
    public Ast.Method parseMethod() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code statement} rule and delegates to the necessary method.
     * If the next tokens do not start a declaration, if, while, or return
     * statement, then it is an expression/assignment statement.
     */
    public Ast.Stmt parseStatement() throws ParseException {
        // throw new UnsupportedOperationException(); //TODO

        if(peek("LET")){
            return parseDeclarationStatement();
        } else if (peek("IF")) {
            return parseIfStatement();
        } else if (peek("FOR")) {
            return parseForStatement();
        } else if (peek("WHILE")) {
            return parseWhileStatement();
        } else if (peek("RETURN")) {
            return parseReturnStatement();
        } else {
            Ast.Expr expr = parseExpression();
            if(peek("=")){
                match("=");
                Ast.Expr ex = parseExpression();
                return new Ast.Stmt.Assignment(expr, ex);
            }
            else {
                if (peek(";")) {
                    match(";");
                    return new Ast.Stmt.Expression(expr);
                }
                else {
                    throw new ParseException("No semicolon", -1);
                }
            }
        }

    }

    /**
     * Parses a declaration statement from the {@code statement} rule. This
     * method should only be called if the next tokens start a declaration
     * statement, aka {@code LET}.
     */
    public Ast.Stmt.Declaration parseDeclarationStatement() throws ParseException {
        // throw new UnsupportedOperationException(); //TODO

        // 'LET' identifier ('=' expression)? ';'

        match("LET");

        if (!match(Token.Type.IDENTIFIER)) {
            throw new ParseException("Expected Identifier.", -1);
            // TODO: handle actual character index instead of -1
        }

        String name = tokens.get(-1).getLiteral();

        Optional<Ast.Expr> value = Optional.empty();

        if (match("=")) {
            value = Optional.of(parseExpression());
        }

        if (!match(";")) {
            throw new ParseException("Expected semicolon.", -1);
            // TODO: handle actual character index instead of -1
        }
        return new Ast.Stmt.Declaration(name, value);
    }

    /**
     * Parses an if statement from the {@code statement} rule. This method
     * should only be called if the next tokens start an if statement, aka
     * {@code IF}.
     */
    public Ast.Stmt.If parseIfStatement() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        //match IF
        match("IF");
        //Expression
        Ast.Expr expr = parseExpression();
        if(match("DO")){
            List<Ast.Stmt> thenStmt = new ArrayList<>();
            List<Ast.Stmt> elseStmt = new ArrayList<>();

            //parse THEN statement
            while (!peek("ELSE") && !peek("END")){
                thenStmt.add(parseStatement());
            }

            //check for else
            if(peek("ELSE")){
                //parse else statement
                while (!peek("END")) {
                    elseStmt.add(parseStatement());
                }
            }

            //match END
            if(match("END")){
                return new Ast.Stmt.If(expr,thenStmt, elseStmt);
            }
            else {
                throw new ParseException("NO END", -1);
            }

        }
        else {
            throw new ParseException("NO DO", -1);
        }

    }

    /**
     * Parses a for statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a for statement, aka
     * {@code FOR}.
     */
    public Ast.Stmt.For parseForStatement() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        match("FOR");
        if(match(Token.Type.IDENTIFIER)) {
            String name = tokens.get(-1).getLiteral();


            if (!match("IN")) {
                throw new ParseException("Expected IN", tokens.get(0).getIndex());
            }

            Ast.Expr expr = parseExpression();
            if (!match("DO")) {
                throw new ParseException("Expected DO", tokens.get(0).getIndex());
            }

            List<Ast.Stmt> stmt = new ArrayList<>();
            while (!match("END")) {
                stmt.add(parseStatement());
            }

            if (match("END")) {
                return new Ast.Stmt.For(name, expr, stmt);
            } else {
                throw new ParseException("Expected END", tokens.get(0).getIndex());
            }


        }
        throw new ParseException("Expected", -1);

    }

    /**
     * Parses a while statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a while statement, aka
     * {@code WHILE}.
     */
    public Ast.Stmt.While parseWhileStatement() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        match("WHILE");
        //Parse the condition expression
        Ast.Expr expr = parseExpression();
        if(!match("DO")){
            throw new ParseException("Expected DO", tokens.get(0).getIndex());
        }
        List<Ast.Stmt> stmt = new ArrayList<>();

        while (!peek("END")){
            stmt.add(parseStatement());
        }

        if(match("END")){
            return new Ast.Stmt.While(expr, stmt);
        }
        else {
            throw new ParseException("Expected END", tokens.get(0).getIndex());
        }
    }

    /**
     * Parses a return statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a return statement, aka
     * {@code RETURN}.
     */
    public Ast.Stmt.Return parseReturnStatement() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        match("RETURN");

        Ast.Expr expression = parseExpression();

        if(peek(";")){
            match(";");
            return new Ast.Stmt.Return(expression);
        }
        else {
            throw new ParseException("Expected semicolon", tokens.get(0).getIndex());
        }

    }

    /**
     * Parses the {@code expression} rule.
     */
    public Ast.Expr parseExpression() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        return parseLogicalExpression();
    }

    /**
     * Parses the {@code logical-expression} rule.
     */
    public Ast.Expr parseLogicalExpression() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        Ast.Expr leftExpr = parseMultiplicativeExpression();
        while (match("AND") || match("OR")){
            String temp = tokens.get(-1).getLiteral();
            Ast.Expr rightExpr = parseMultiplicativeExpression();
            leftExpr = new Ast.Expr.Binary(temp, leftExpr, rightExpr);
        }
        return leftExpr;

    }

    /**
     * Parses the {@code equality-expression} rule.
     */
    public Ast.Expr parseEqualityExpression() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        Ast.Expr leftExpr = parseAdditiveExpression();
        while (match("<") || match("<=")
                || match(">") || match(">=")
                || match("==") || match("!=")) {
            String temp = tokens.get(-1).getLiteral();
            Ast.Expr rightExpr = parseAdditiveExpression();
            leftExpr = new Ast.Expr.Binary(temp, leftExpr, rightExpr);
        }
        return leftExpr;
    }

    /**
     * Parses the {@code additive-expression} rule.
     */
    public Ast.Expr parseAdditiveExpression() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        Ast.Expr leftExpr = parseMultiplicativeExpression();
        while (match("+") || match("-")){
            String temp = tokens.get(-1).getLiteral();
            Ast.Expr rightExpr = parseMultiplicativeExpression();
            leftExpr = new Ast.Expr.Binary(temp, leftExpr, rightExpr);
        }
        return leftExpr;
    }

    /**
     * Parses the {@code multiplicative-expression} rule.
     */
    public Ast.Expr parseMultiplicativeExpression() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        Ast.Expr secondary_expression = parseSecondaryExpression();

        while (peek("*") || peek("/")) {
            String operation = tokens.get(-1).getLiteral();

            match(Token.Type.OPERATOR);

            Ast.Expr next_secondary_expr = parseSecondaryExpression();

            if (!peek("*") && !peek("/")) {
                return new Ast.Expr.Binary(operation, secondary_expression, next_secondary_expr);
            }
            else
                secondary_expression = new Ast.Expr.Binary(operation, secondary_expression, next_secondary_expr);
        }
        return secondary_expression;
    }

    /**
     * Parses the {@code secondary-expression} rule.
     */
    public Ast.Expr parseSecondaryExpression() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        Ast.Expr primary_expression = parsePrimaryExpression();
        if (!peek("."))
            return primary_expression;

        else {
            String name = "";
            while (peek("."))
            {
                match(".");

                if (peek(Token.Type.IDENTIFIER)) {
                    name = tokens.get(-1).getLiteral();
                    match(Token.Type.IDENTIFIER);
                }

                if (match("(")) {

                    List<Ast.Expr> expressions = new ArrayList<>();

                    while (!peek(")")) {
                        expressions.add(parseExpression());
                        if (peek(",")) {
                            match(",");
                        }
                    }
                    match(")");

                    if (!peek("."))
                        return new Ast.Expr.Function(Optional.of(primary_expression), name, expressions);
                    else
                        primary_expression = new Ast.Expr.Function(Optional.of(primary_expression), name, expressions);
                }
                else {
                    if (!peek(".")) {
                        return new Ast.Expr.Access(Optional.of(primary_expression), name);
                    }
                    else
                        primary_expression = new Ast.Expr.Access(Optional.of(primary_expression), name);
                }
            }
        }
        return null;
    }

    /**
     * Parses the {@code primary-expression} rule. This is the top-level rule
     * for expressions and includes literal values, grouping, variables, and
     * functions. It may be helpful to break these up into other methods but is
     * not strictly necessary.
     */
    public Ast.Expr parsePrimaryExpression() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        if (match("TRUE")) {
            return new Ast.Expr.Literal(true);
        } else if (match("FALSE")) {
            return new Ast.Expr.Literal(false);
        } else if (match("NIL")) {
            return new Ast.Expr.Literal(null);
        } else if (match(Token.Type.INTEGER)) {
            BigInteger integer = new BigInteger(tokens.get(-1).getLiteral());
            return new Ast.Expr.Literal(integer);
        } else if (match(Token.Type.DECIMAL)) {
            BigDecimal decimal = new BigDecimal(tokens.get(-1).getLiteral());
            return new Ast.Expr.Literal(decimal);
        } else if (peek(Token.Type.CHARACTER)) {
            // Professor Aashish assisted me in this portion of the code during OH 09/25
            if (tokens.get(0).getLiteral().length() < 2) {
                Character character = tokens.get(0).getLiteral().charAt(1);
                match(Token.Type.CHARACTER);
                return new Ast.Expr.Literal(character);
            } else {
                String temporary = tokens.get(0).getLiteral();
                temporary = temporary.replace("\\b","\b");
                temporary = temporary.replace("\\n","\n");
                temporary = temporary.replace("\\r","\r");
                temporary = temporary.replace("\\t","\t");
                if (temporary.equals("'\\\"'"))
                    temporary = "'\"'";
                if (temporary.equals("'\\\\'"))
                    temporary = "'\\'";
                if (temporary.equals("'\\\''"))
                    temporary = "'\''";
                Character character = temporary.charAt(1);
                match(Token.Type.CHARACTER);
                return new Ast.Expr.Literal(character);
            }
        } else if (peek(Token.Type.STRING)) {
            String temporary = tokens.get(0).getLiteral();

            temporary = temporary.replace("\\b","\b");
            temporary = temporary.replace("\\n","\n");
            temporary = temporary.replace("\\r","\r");
            temporary = temporary.replace("\\t","\t");

            temporary = temporary.replace("\\\"", "\"");
            temporary = temporary.replace("\\\\", "\\");
            temporary = temporary.replace("\\\'", "\'");
            temporary = temporary.substring(1, temporary.length() - 1);
            match(Token.Type.STRING);
            return new Ast.Expr.Literal(temporary);
        }else if (match(Token.Type.IDENTIFIER)) {
            String name = tokens.get(-1).getLiteral();
            // TODO: handle function case if next token is (
            if (match("(")) {

                List<Ast.Expr> expressions = new ArrayList<Ast.Expr>();

                while (!peek(")")) {
                    expressions.add(parseExpression());
                    if (peek(",")) {
                        match(",");
                    }
                }
                match(")");
                return new Ast.Expr.Function(Optional.empty(), name, expressions);
            }
            else
                return new Ast.Expr.Access(Optional.empty(), name);
            // obj.method()
        }  else if(match("(")) {
            Ast.Expr expr = parseExpression();
            if (!match(")")) {
                throw new ParseException("Expected closing parenthesis.", -1);
                // TODO: handle actual character index instead of -1
            }
            return new Ast.Expr.Group(expr);
        } else {
            throw new ParseException("Invalid primary expression.", -1);
            // TODO: handle actual character index instead of -1
        }
    }

    /**
     * As in the lexer, returns {@code true} if the current sequence of tokens
     * matches the given patterns. Unlike the lexer, the pattern is not a regex;
     * instead it is either a {@link Token.Type}, which matches if the token's
     * type is the same, or a {@link String}, which matches if the token's
     * literal is the same.
     *
     * In other words, {@code Token(IDENTIFIER, "literal")} is matched by both
     * {@code peek(Token.Type.IDENTIFIER)} and {@code peek("literal")}.
     */
    private boolean peek(Object... patterns) {
        for (int i = 0; i < patterns.length; i++) {
            if (!tokens.has(i)) {
                return false;
            } else if (patterns[i] instanceof Token.Type) {
                if (patterns[i] != tokens.get(i).getType()) {
                    return false;
                }
            } else if (patterns[i] instanceof String) {
                if (!patterns[i].equals(tokens.get(i).getLiteral())) {
                    return false;
                }
            } else {
                throw new AssertionError("Invalid pattern object: " +
                        patterns[i].getClass());
            }
        }
        return true;
    }

    /**
     * As in the lexer, returns {@code true} if {@link #peek(Object...)} is true
     * and advances the token stream.
     */
    private boolean match(Object... patterns) {
        boolean peek = peek(patterns);
        if (peek) {
            for (int i = 0; i < patterns.length; i++) {
                tokens.advance();
            }
        }
        return peek;
    }

    private static final class TokenStream {

        private final List<Token> tokens;
        private int index = 0;

        private TokenStream(List<Token> tokens) {
            this.tokens = tokens;
        }

        /**
         * Returns true if there is a token at index + offset.
         */
        public boolean has(int offset) {
            return index + offset < tokens.size();
        }

        /**
         * Gets the token at index + offset.
         */
        public Token get(int offset) {
            return tokens.get(index + offset);
        }

        /**
         * Advances to the next token, incrementing the index.
         */
        public void advance() {
            index++;
        }

    }

}
