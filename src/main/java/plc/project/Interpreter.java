package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Interpreter implements Ast.Visitor<Environment.PlcObject> {

    private Scope scope = new Scope(null);

    public Interpreter(Scope parent) {
        scope = new Scope(parent);
        scope.defineFunction("print", 1, args -> {
            System.out.println(args.get(0).getValue());
            return Environment.NIL;
        });
    }

    public Scope getScope() {
        return scope;
    }

    @Override
    public Environment.PlcObject visit(Ast.Source ast) {
        //throw new UnsupportedOperationException(); //TODO
        //use for loop to iterate each field
        for(Ast.Field field : ast.getFields()){
            visit(field);
        }
        //use for loop to iterate each method
        for(Ast.Method method : ast.getMethods()){
            visit(method);
        }
        //create a list
        List<Environment.PlcObject> arguments = new ArrayList<Environment.PlcObject>();
        return scope.lookupFunction("main",0).invoke(arguments);

    }

    @Override
    public Environment.PlcObject visit(Ast.Field ast) {
        //throw new UnsupportedOperationException(); //TODO
        if(ast.getValue().isPresent()){
            scope.defineVariable(ast.getName(), visit(ast.getValue().get()));
        }else {
            scope.defineVariable(ast.getName(), Environment.NIL);
        }
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Method ast) {
        //throw new UnsupportedOperationException(); //TODO
        // List<Environment.PlcObject> arguments = new ArrayList<Environment.PlcObject>();
        // Max explained the callback function(lambda), which assisted in reworking the
        // code in this function. OH: 10/16
        scope.defineFunction(ast.getName(), ast.getParameters().size(), arguments -> {
            try {
                scope = new Scope(scope);
                //Define variables for the incoming arguments, using the parameter names.
                for(int i = 0; i < arguments.size(); i++){
                    scope.defineVariable(ast.getParameters().get(i), arguments.get(i));
                }
                //Evaluate the methods statements
                for (Ast.Stmt stmt : ast.getStatements()){
                    visit(stmt);
                }

            }finally {
                //s
                scope = scope.getParent();
            }
            return Environment.NIL;
        });
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.Expression ast) {

        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.Declaration ast) {
        //throw new UnsupportedOperationException(); //TODO (in lecture)
        if (ast.getValue().isPresent())
        {
            scope.defineVariable(ast.getName(), visit(ast.getValue().get()));
        } else {
            scope.defineVariable(ast.getName(), Environment.NIL);
        }
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.Assignment ast) {
        //throw new UnsupportedOperationException(); //TODO
        if (ast.getReceiver().getClass() == Ast.Expr.Access.class) {
            try {

            } finally {

            }
        }
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.If ast) {
        //throw new UnsupportedOperationException(); //TODO
        if (requireType(Boolean.class, visit(ast.getCondition()))) {
            try {
                scope = new Scope(scope);
                ast.getThenStatements().forEach(this::visit);
            } finally {
                scope = scope.getParent();
            }
        }
        else if (!requireType(Boolean.class, visit(ast.getCondition()))) {
            try {
                scope = new Scope(scope);
                ast.getElseStatements().forEach(this::visit);
            } finally {
                scope = scope.getParent();
            }
        }
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.For ast) {
        //throw new UnsupportedOperationException(); //TODO
        Iterable value = requireType(Iterable.class, visit(ast.getValue()));

        //For each element
        value.forEach(element -> {
            try {
                scope = new Scope(scope);
                scope.defineVariable(ast.getName(), Environment.PlcObject.class.cast(element));
                ast.getStatements().forEach(this::visit);
            } finally {
                scope = scope.getParent();
            }
        });
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.While ast) {
        //throw new UnsupportedOperationException(); //TODO (in lecture)
        while(requireType(Boolean.class, visit(ast.getCondition()))) {
            try {
                scope = new Scope(scope);
                ast.getStatements().forEach(this::visit);
            } finally {
                scope = scope.getParent();
            }
        }
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.Return ast) {
       // throw new UnsupportedOperationException(); //TODO
        Environment.PlcObject value = visit(ast.getValue());
        return Environment.NIL;

    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Literal ast) {
        if(ast.getLiteral() != null){
            return Environment.create(ast.getLiteral());
        }
        else {
            return Environment.NIL;
        }
    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Group ast) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Binary ast) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Access ast) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Function ast) {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Helper function to ensure an object is of the appropriate type.
     */
    private static <T> T requireType(Class<T> type, Environment.PlcObject object) {
        if (type.isInstance(object.getValue())) {
            return type.cast(object.getValue());
        } else {
            throw new RuntimeException("Expected type " + type.getName() + ", received " + object.getValue().getClass().getName() + ".");
        }
    }

    /**
     * Exception class for returning values.
     */
    private static class Return extends RuntimeException {

        private final Environment.PlcObject value;

        private Return(Environment.PlcObject value) {
            this.value = value;
        }

    }

}
