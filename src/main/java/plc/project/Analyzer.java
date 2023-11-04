package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * See the specification for information about what the different visit
 * methods should do.
 */
public final class Analyzer implements Ast.Visitor<Void> {

    public Scope scope;
    private Ast.Method method;

    public Analyzer(Scope parent) {
        scope = new Scope(parent);
        scope.defineFunction("print", "System.out.println", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL);
    }

    public Scope getScope() {
        return scope;
    }

    @Override
    public Void visit(Ast.Source ast) { throw new UnsupportedOperationException();  // TODO

    }

    @Override
    public Void visit(Ast.Field ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Method ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Stmt.Expression ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Stmt.Declaration ast) {
        // throw new UnsupportedOperationException();  // TODO

        // 'LET' identifier (':' identifier)? ('=' expression)? ';



        if (!ast.getTypeName().isPresent() && !ast.getValue().isPresent()) {
            throw new RuntimeException("Declaration must have type or value to infer type.");
        }

        Environment.Type type = null;

        if (ast.getTypeName().isPresent()) {
            type = Environment.getType(ast.getTypeName().get());
        }

        if (ast.getValue().isPresent()) {

            visit(ast.getValue().get());

            // if (!ast.getTypeName().isPresent()) {
            if (type == null) {
                type = ast.getValue().get().getType();
            }

            requireAssignable(type, ast.getValue().get().getType());
        }

        ast.setVariable(scope.defineVariable(ast.getName(), ast.getName(), type, Environment.NIL));

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Assignment ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Stmt.If ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Stmt.For ast) {
        // throw new UnsupportedOperationException();  // TODO
        if (ast.getStatements().isEmpty())
            throw new RuntimeException("Statements list is empty");
        return null;
    }

    @Override
    public Void visit(Ast.Stmt.While ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Stmt.Return ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Expr.Literal ast) {
        // throw new UnsupportedOperationException();  // TODO

        // validates and set type NIL
        if (ast.getLiteral() == Environment.NIL)
            ast.setType(Environment.Type.NIL);
        // validates and set type Boolean
        else if (ast.getLiteral() instanceof Boolean)
            ast.setType(Environment.Type.BOOLEAN);
        // validates and set type Character
        else if (ast.getLiteral() instanceof Character)
            ast.setType(Environment.Type.CHARACTER);
        // validates and set type String
        else if (ast.getLiteral() instanceof String)
            ast.setType(Environment.Type.STRING);
        // validates and set type Integer
        else if (ast.getLiteral() instanceof BigInteger) {
            BigInteger value = (BigInteger) ast.getLiteral();
            if (value.intValueExact() > Integer.MAX_VALUE || value.intValueExact() < Integer.MIN_VALUE)
                throw new RuntimeException("Out of range of a Java int (32-bit signed int)");
            ast.setType(Environment.Type.INTEGER);
        }
        // validates and set type Decimal
        else if (ast.getLiteral() instanceof BigDecimal) {
            BigDecimal value = (BigDecimal) ast.getLiteral();
            if (value.doubleValue() > Double.MAX_VALUE || value.doubleValue() < Double.MIN_VALUE)
                throw new RuntimeException("Out of range of a Java double (64-bit signed float)");
            ast.setType(Environment.Type.DECIMAL);
        }
        else
            throw new RuntimeException("Type is invalid");
        return null;
    }

    @Override
    public Void visit(Ast.Expr.Group ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Expr.Binary ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Expr.Access ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Expr.Function ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    public static void requireAssignable(Environment.Type target, Environment.Type type) {
        // throw new UnsupportedOperationException();  // TODO
        if (target != Environment.Type.COMPARABLE && type != target && target != Environment.Type.ANY)
            throw new RuntimeException("Target type does not match type being used or assigned");
    }

}
