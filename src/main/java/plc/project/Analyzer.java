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
        //throw new UnsupportedOperationException();  // TODO
        if (ast.getReceiver().getClass() != Ast.Expr.Access.class){
            throw new RuntimeException("Sos");
        }
        visit(ast.getReceiver());
        visit(ast.getValue());

        requireAssignable(ast.getReceiver().getType(), ast.getValue().getType());
        return null;

    }

    @Override
    public Void visit(Ast.Stmt.If ast) {
        //throw new UnsupportedOperationException();  // TODO
        //check if the condition is not type Boolean
        if(ast.getCondition().getType() != Environment.Type.BOOLEAN){
            throw new RuntimeException();
        }
        //check if the thenStatements list is empty
        if(ast.getThenStatements().isEmpty()){
            throw new RuntimeException("No then");
        }

        visit(ast.getCondition());
        //visit the then
        for(Ast.Stmt then:  ast.getThenStatements()){
            try{
                scope = new Scope(scope);
                visit(then);
            }
            finally {
                scope = scope.getParent();
            }
        }
        //visit else statements
        for(Ast.Stmt elseStmt : ast.getElseStatements()){
            try{
                scope = new Scope(scope);
                visit(elseStmt);
            }
            finally {
                scope = scope.getParent();
            }
        }
        return null;

    }

    @Override
    public Void visit(Ast.Stmt.For ast) {
        //throw new UnsupportedOperationException();  // TODO
        //check if the value is not type IntegerIterable
        if(ast.getValue().getType() != Environment.Type.INTEGER_ITERABLE){
            throw new RuntimeException();
        }
        if(ast.getStatements().isEmpty()){
            throw new RuntimeException("Statement list is empty");
        }
        visit(ast.getValue());
        for(Ast.Stmt stmt : ast.getStatements()) {
            try {
                scope = new Scope(scope);
                scope.defineVariable(ast.getName(), ast.getName(), Environment.Type.INTEGER, Environment.NIL);
            } finally {
                scope = scope.getParent();
            }
        }
        return null;
    }

    @Override
    public Void visit(Ast.Stmt.While ast) {
        //throw new UnsupportedOperationException();  // TODO
        visit(ast.getCondition());
        if(ast.getCondition().getType() != Environment.Type.BOOLEAN){
            throw new RuntimeException();
        }
        scope = new Scope(scope);
        for(Ast.Stmt stmt : ast.getStatements()){
            visit(stmt);
        }
        return null;
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
        //throw new UnsupportedOperationException();  // TODO
        if(ast.getExpression().getClass() != Ast.Expr.Binary.class){
            throw new RuntimeException("Expected");
        }
        visit(ast.getExpression());
        //setting its type to be the type of the contained expression
        ast.setType(ast.getExpression().getType());

        return null;
    }

    @Override
    public Void visit(Ast.Expr.Binary ast) {
        //throw new UnsupportedOperationException();  // TODO
        String binaryOperator = ast.getOperator();
        switch (binaryOperator) {
            case ("AND"):
                if(ast.getLeft().getType() == Environment.Type.BOOLEAN && ast.getRight().getType() == Environment.Type.BOOLEAN){
                    ast.setType(Environment.Type.BOOLEAN);

                }
            case ("OR"):
                if(ast.getLeft().getType() == Environment.Type.BOOLEAN && ast.getRight().getType() == Environment.Type.BOOLEAN){
                    ast.setType(Environment.Type.BOOLEAN);
                }
                //requireAssignable(Environment.Type target, Environment.Type type)
            case ("<"):
                requireAssignable(Environment.Type.COMPARABLE, ast.getLeft().getType());
                requireAssignable(Environment.Type.COMPARABLE, ast.getRight().getType());
                ast.setType(Environment.Type.BOOLEAN);
            case ("<="):
                requireAssignable(Environment.Type.COMPARABLE, ast.getLeft().getType());
                requireAssignable(Environment.Type.COMPARABLE, ast.getRight().getType());
                ast.setType(Environment.Type.BOOLEAN);
            case (">"):
                requireAssignable(Environment.Type.COMPARABLE, ast.getLeft().getType());
                requireAssignable(Environment.Type.COMPARABLE, ast.getRight().getType());
                ast.setType(Environment.Type.BOOLEAN);
            case (">="):
                requireAssignable(Environment.Type.COMPARABLE, ast.getLeft().getType());
                requireAssignable(Environment.Type.COMPARABLE, ast.getRight().getType());
                ast.setType(Environment.Type.BOOLEAN);
            case ("=="):
                requireAssignable(Environment.Type.COMPARABLE, ast.getLeft().getType());
                requireAssignable(Environment.Type.COMPARABLE, ast.getRight().getType());
                ast.setType(Environment.Type.BOOLEAN);
            case ("!="):
                requireAssignable(Environment.Type.COMPARABLE, ast.getLeft().getType());
                requireAssignable(Environment.Type.COMPARABLE, ast.getRight().getType());
                ast.setType(Environment.Type.BOOLEAN);
            case ("+"):
                if(ast.getLeft().getType() == Environment.Type.STRING || ast.getRight().getType() == Environment.Type.STRING){
                    ast.setType(Environment.Type.STRING);
                } else if (ast.getLeft().getType() == Environment.Type.INTEGER || ast.getLeft().getType() == Environment.Type.DECIMAL) {
                    if (ast.getLeft().getType() != ast.getRight().getType()) {
                        throw new RuntimeException("-.-");
                    }
                    ast.setType(ast.getLeft().getType());
                }
                else {
                    throw new RuntimeException("-.-");
                }
            case ("-"):
                if(ast.getLeft().getType() == Environment.Type.INTEGER || ast.getRight().getType() == Environment.Type.DECIMAL){
                    ast.setType(ast.getLeft().getType());
                }
            case ("*"):
                if(ast.getLeft().getType() == Environment.Type.INTEGER || ast.getRight().getType() == Environment.Type.DECIMAL){
                    ast.setType(ast.getLeft().getType());
                }
            case ("/"):
                if(ast.getLeft().getType() == Environment.Type.INTEGER || ast.getRight().getType() == Environment.Type.DECIMAL){
                    ast.setType(ast.getLeft().getType());
                }

        }
        return null;

    }

    @Override
    public Void visit(Ast.Expr.Access ast) {
        //throw new UnsupportedOperationException();  // TODO
        if(ast.getReceiver().isPresent()){
            visit(ast.getReceiver().get());
            ast.setVariable(ast.getReceiver().get().getType().getField(ast.getName()));
        }
        else{
            ast.setVariable(scope.lookupVariable(ast.getName()));
        }
        return null;
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
