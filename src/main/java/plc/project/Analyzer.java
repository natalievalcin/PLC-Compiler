package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
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
    public Void visit(Ast.Source ast) { // throw new UnsupportedOperationException();  // TODO
        try {
            if (!ast.getFields().isEmpty()) {
                for (Ast.Field field : ast.getFields())
                    visit(field);
            }
// check to see if the main type exist before anything
            boolean main = false;
            if (!ast.getMethods().isEmpty()) {
                for (Ast.Method method : ast.getMethods())
                    visit(method);
                for (Ast.Method method : ast.getMethods()) {
                    Ast.Method temp = method;
                    if (temp.getName().equals("main") && temp.getReturnTypeName().get().equals("Integer") && temp.getParameters().isEmpty()) {
                        main = true;
                    }
                }
            }
            if (!main) {
                throw new RuntimeException("Main method does not exist");
            }
        } catch (RuntimeException exception) {
            throw new RuntimeException(exception);
        }
        return null;
    }

    @Override
    public Void visit(Ast.Field ast) {
        //throw new UnsupportedOperationException();  // TODO
        try {
            // if value of the field is present
            if (ast.getValue().isPresent()) {
                // must be visited before the variable is defined
                visit(ast.getValue().get());
                requireAssignable(Environment.getType(ast.getTypeName()), ast.getValue().get().getType());
                scope.defineVariable(ast.getName(), ast.getName(), ast.getValue().get().getType(), Environment.NIL);
                ast.setVariable(scope.lookupVariable(ast.getName()));
            }
            else {
                scope.defineVariable(ast.getName(), ast.getName(), Environment.getType(ast.getTypeName()), Environment.NIL);
                ast.setVariable(scope.lookupVariable(ast.getName()));
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public Void visit(Ast.Method ast) {
        try{
            /* The function's parameter types and return type are retrieved
             from the environment using the corresponding names in the method.*/
            List<Environment.Type> parameterTypes = new ArrayList<>();
            for (String parameterTypeName : ast.getParameterTypeNames()) {
                parameterTypes.add(Environment.getType(parameterTypeName));
            }

            Environment.Type returnType;
            if (ast.getReturnTypeName().isPresent()) {
                returnType = Environment.getType(ast.getReturnTypeName().get());
            } else {
                returnType = Environment.Type.NIL;
            }

            // Define the function in the current scope
            ast.setFunction(scope.defineFunction(ast.getName(), ast.getName(), parameterTypes, returnType, args -> Environment.NIL));

            // Create a new scope
            scope = new Scope(scope);

            // Define variables in the new scope for each parameter
            for (int i = 0; i < ast.getParameters().size(); i++) {
                String parameterName = ast.getParameters().get(i);
                Environment.Type parameterType = parameterTypes.get(i);
                scope.defineVariable(parameterName, parameterName, parameterType, Environment.NIL);
            }

            // Visit each statement in the method
            for (Ast.Stmt stmt : ast.getStatements()) {
                visit(stmt);
            }
        } finally {
            // Restore the original scope
            scope = scope.getParent();
        }

        return null;
    }



    @Override
    public Void visit(Ast.Stmt.Expression ast) {
        // throw new UnsupportedOperationException();  // TODO
        //check if the expression is not Ast.Expr.Function
        if (ast.getExpression().getClass() != Ast.Expr.Function.class) {
            throw new RuntimeException();
        }
        visit(ast.getExpression());
        return null;

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
        //visit ast.getCondition()
        visit(ast.getCondition());
        //check if the condition is not type Boolean
        requireAssignable(Environment.Type.BOOLEAN, ast.getCondition().getType());

        //check if the thenStatements list is empty
        if(ast.getThenStatements().isEmpty()){
            throw new RuntimeException("No then");
        }
        else{
        //visit the then
            for (Ast.Stmt then : ast.getThenStatements()) {
                try {
                    scope = new Scope(scope);
                    visit(then);
                } finally {
                    scope = scope.getParent();
                }
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
        visit(ast.getValue());
        if(ast.getValue().getType() != Environment.Type.INTEGER_ITERABLE){
            throw new RuntimeException();
        }
        if(ast.getStatements().isEmpty()){
            throw new RuntimeException("Statement list is empty");
        }

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
//        visit(ast.getCondition());
//        if(ast.getCondition().getType() != Environment.Type.BOOLEAN){
//            throw new RuntimeException();
//        }
//        scope = new Scope(scope);
//        for(Ast.Stmt stmt : ast.getStatements()){
//            visit(stmt);
//        }
//        return null;
        visit(ast.getCondition());
        requireAssignable(Environment.Type.BOOLEAN, ast.getCondition().getType());
        try {
            scope = new Scope(scope);
            for (Ast.Stmt stmt : ast.getStatements()){
                visit(stmt);
            }
        } finally {
            scope = scope.getParent();
        }
        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Return ast) {
        //throw new UnsupportedOperationException();  // TODO
        visit(ast.getValue());

        // Method needs to store this return type
        Environment.Variable ret = scope.lookupVariable("returnType");
        requireAssignable(ret.getType(), ast.getValue().getType());
        return null;
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
        visit(ast.getLeft());
        visit(ast.getRight());
        String binaryOperator = ast.getOperator();
        switch (binaryOperator) {
            case ("AND"):
                if (ast.getLeft().getType() == Environment.Type.BOOLEAN && ast.getRight().getType() == Environment.Type.BOOLEAN) {
                    ast.setType(Environment.Type.BOOLEAN);
                } else {
                    throw new RuntimeException("Invalid");
                }
                break;
            case ("OR"):
                if(ast.getLeft().getType() == Environment.Type.BOOLEAN && ast.getRight().getType() == Environment.Type.BOOLEAN){
                    ast.setType(Environment.Type.BOOLEAN);
                }
                else {
                    throw  new RuntimeException("Invalid");
                }
                break;
                //requireAssignable(Environment.Type target, Environment.Type type)
            case ("<"):
                requireAssignable(Environment.Type.COMPARABLE, ast.getLeft().getType());
                requireAssignable(Environment.Type.COMPARABLE, ast.getRight().getType());
                ast.setType(Environment.Type.BOOLEAN);
                break;
            case ("<="):
                requireAssignable(Environment.Type.COMPARABLE, ast.getLeft().getType());
                requireAssignable(Environment.Type.COMPARABLE, ast.getRight().getType());
                ast.setType(Environment.Type.BOOLEAN);
                break;
            case (">"):
                requireAssignable(Environment.Type.COMPARABLE, ast.getLeft().getType());
                requireAssignable(Environment.Type.COMPARABLE, ast.getRight().getType());
                ast.setType(Environment.Type.BOOLEAN);
                break;
            case (">="):
                requireAssignable(Environment.Type.COMPARABLE, ast.getLeft().getType());
                requireAssignable(Environment.Type.COMPARABLE, ast.getRight().getType());
                ast.setType(Environment.Type.BOOLEAN);
                break;
            case ("=="):
                requireAssignable(Environment.Type.COMPARABLE, ast.getLeft().getType());
                requireAssignable(Environment.Type.COMPARABLE, ast.getRight().getType());
                ast.setType(Environment.Type.BOOLEAN);
                break;
            case ("!="):
                requireAssignable(Environment.Type.COMPARABLE, ast.getLeft().getType());
                requireAssignable(Environment.Type.COMPARABLE, ast.getRight().getType());
                ast.setType(Environment.Type.BOOLEAN);
                break;
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
                break;
            case ("-"):
                if(ast.getLeft().getType() == Environment.Type.INTEGER || ast.getLeft().getType() == Environment.Type.DECIMAL){
                    if (ast.getLeft().getType() != ast.getRight().getType()) {
                        throw new RuntimeException("The right hand side and result type are not the same as the left");
                    }
                    ast.setType(ast.getLeft().getType());
                }
                break;
            case ("*"):
                if(ast.getLeft().getType() == Environment.Type.INTEGER || ast.getLeft().getType() == Environment.Type.DECIMAL){
                    if (ast.getLeft().getType() != ast.getRight().getType()) {
                        throw new RuntimeException("The right hand side and result type are not the same as the left");
                    }
                    ast.setType(ast.getLeft().getType());
                }
                break;
            case ("/"):
                if(ast.getLeft().getType() == Environment.Type.INTEGER || ast.getLeft().getType() == Environment.Type.DECIMAL){
                    if (ast.getLeft().getType() != ast.getRight().getType()) {
                        throw new RuntimeException("The right hand side and result type are not the same as the left");
                    }
                    ast.setType(ast.getLeft().getType());
                }
                break;
            default:
                return null;

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
        if (ast.getReceiver().isPresent()) {
            //if there is a receiver
            visit(ast.getReceiver().get());
            //set the function
            ast.setFunction(ast.getReceiver().get().getType().getMethod(ast.getName(), ast.getArguments().size()));

            List<Environment.Type> parameterTypes = ast.getFunction().getParameterTypes();

            for (int i = 0; i < ast.getArguments().size(); i++) {
                visit(ast.getArguments().get(i));
                requireAssignable(parameterTypes.get(i + 1), ast.getArguments().get(i).getType());
            }
        } else {
            // If there is no receiver, set the function based on the current scope
            ast.setFunction(scope.lookupFunction(ast.getName(), ast.getArguments().size()));

            List<Environment.Type> parameterTypes = ast.getFunction().getParameterTypes();

            for (int i = 0; i < ast.getArguments().size(); i++) {
                visit(ast.getArguments().get(i));
                //System.out.println("Print out to see" + ast.getArguments().get(i) );
                requireAssignable(parameterTypes.get(i), ast.getArguments().get(i).getType());
            }
        }

        return null;
    }




    public static void requireAssignable(Environment.Type target, Environment.Type type) {
        // throw new UnsupportedOperationException();  // TODO
        if (target != Environment.Type.COMPARABLE && type != target && target != Environment.Type.ANY)
            throw new RuntimeException("Target type does not match type being used or assigned");
    }

}
