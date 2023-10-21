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

        // parent scope and a child scope may be easier: this is from professor aashish
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
            }return Environment.NIL;
        });
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.Expression ast) {
        //Evaluates the expression. Returns NIL
        visit(ast.getExpression());
        return Environment.NIL;
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
                scope = new Scope(scope);
                //ensure that the receiver is an Ast.Expr.Access
                Ast.Expr.Access access = Ast.Expr.Access.class.cast(ast.getReceiver());
                if(access.getReceiver().isPresent()){
                    // evaluate it
                    Environment.PlcObject value = visit(access.getReceiver().get());
                    //   public void setField(String name, PlcObject value) {
                    //            scope.lookupVariable(name).setValue(value);
                    //        }
                    value.setField(access.getName(), visit(ast.getValue()));
                }
                else { //otherwise lookup and set a variable in the current scope
                    Environment.Variable variable = scope.lookupVariable(access.getName());
                    variable.setValue(visit(ast.getValue()));
                }
            } finally {
                scope = scope.getParent();
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
        //Evaluates the contained expression, returning it's value
        return visit(ast.getExpression());
    }

//    public static final class Binary extends Ast.Expr {
//
//        private final String operator;
//        private final Expr left;
//        private final Expr right;
//
//        public Binary(String operator, Expr left, Expr right) {
//            this.operator = operator;
//            this.left = left;
//            this.right = right;
//        }
//
//        public String getOperator() {
//            return operator;
//        }
//
//        public Expr getLeft() {
//            return left;
//        }
//
//        public Expr getRight() {
//            return right;
//        }
//
//        @Override
//        public boolean equals(Object obj) {
//            return obj instanceof Binary &&
//                    operator.equals(((Binary) obj).operator) &&
//                    left.equals(((Binary) obj).left) &&
//                    right.equals(((Binary) obj).right);
//        }
//
//        @Override
//        public String toString() {
//            return "Ast.Expr.Binary{" +
//                    "operator='" + operator + '\'' +
//                    ", left=" + left +
//                    ", right=" + right +
//                    '}';
//        }
//
//    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Binary ast) {
        //throw new UnsupportedOperationException(); //TODO
        String binaryOperator = ast.getOperator();
        switch(ast.getOperator())
        {
            case("AND"):
                if (requireType(Boolean.class, visit(ast.getLeft())) == requireType(Boolean.class, visit(ast.getRight())))
                    return visit(ast.getLeft());
                else
                    return Environment.create(Boolean.FALSE);
            case("OR"):
                if (requireType(Boolean.class, visit(ast.getLeft())) == Boolean.TRUE)
                    return visit(ast.getLeft());
                else if (requireType(Boolean.class, visit(ast.getRight())) == Boolean.TRUE)
                    return visit(ast.getRight());
                else
                    return Environment.create(Boolean.FALSE);
            case("<"):
                return Environment.create(requireType(Comparable.class,visit(ast.getLeft())).compareTo(requireType(Comparable.class,visit(ast.getRight()))) < 0);
            case("<="):
                return Environment.create(requireType(Comparable.class,visit(ast.getLeft())).compareTo(requireType(Comparable.class,visit(ast.getRight()))) <= 0);
            case(">"):
                return Environment.create(requireType(Comparable.class,visit(ast.getLeft())).compareTo(requireType(Comparable.class,visit(ast.getRight()))) > 0);
            case(">="):
                return Environment.create(requireType(Comparable.class,visit(ast.getLeft())).compareTo(requireType(Comparable.class,visit(ast.getRight()))) >= 0);
            case("=="):
                return Environment.create(ast.getLeft().equals(ast.getRight()));
            case("!="):
                return Environment.create(!ast.getLeft().equals(ast.getRight()));


        }
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Access ast) {
        //throw new UnsupportedOperationException(); //TODO
        //check if the expression has a receiver
        if (ast.getReceiver().isPresent()){
            //evaluate it
            Environment.PlcObject value = visit(ast.getReceiver().get());
            //return the value of the appropriate field
            return value.getField(ast.getName()).getValue();
        }
        //otherwise return the value of the appropriate variable in the current scope.
        return scope.lookupVariable(ast.getName()).getValue();

    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Function ast) {
        //throw new UnsupportedOperationException(); //TODO
        //create a list to hold arguments
        List<Environment.PlcObject> arguments = new ArrayList<Environment.PlcObject>();
        //loop through ast.getArguments
        for (Ast.Expr args: ast.getArguments()){
            arguments.add(visit(args));
        }
        //check if the expression has a receive;
        if (ast.getReceiver().isPresent()) {
            //evaluate it
            Environment.PlcObject value = visit(ast.getReceiver().get());
            //return the result of calling the appropriate method
            //callMethod(String name, List<PlcObject> arguments)
            return value.callMethod(ast.getName(), arguments);

        }else {
            //otherwise return the value of invoking the appropriate function in the current scope with the evaluated arguments.
            return scope.lookupFunction(ast.getName(), arguments.size()).invoke(arguments);
        }

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
