package plc.project;

import javafx.util.converter.BigDecimalStringConverter;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

public final class Generator implements Ast.Visitor<Void> {

    private final PrintWriter writer;
    private int indent = 0;

    public Generator(PrintWriter writer) {
        this.writer = writer;
    }

    private void print(Object... objects) {
        for (Object object : objects) {
            if (object instanceof Ast) {
                visit((Ast) object);
            } else {
                writer.write(object.toString());
            }
        }
    }

    private void newline(int indent) {
        writer.println();
        for (int i = 0; i < indent; i++) {
            writer.write("    ");
        }
    }

    @Override
    public Void visit(Ast.Source ast) {
        //throw new UnsupportedOperationException(); //TODO
        // create a "class Main {"
        print("public class Main {");
        newline(0);
        indent++; // 4 spaces

        //      declare fields
        if (!ast.getFields().isEmpty()) {
            for (int i = 0; i < ast.getFields().size(); i++) {
                newline(indent);
                print(ast.getFields().get(i));
            }
            newline(0);
        }

        //      declare "public static void main(String[] args) {
        newline(indent);
        print("public static void main(String[] args) {");
        indent++; // 8 spaces
        //                   System.exit(new Main().main());
        newline(indent);
        print("System.exit(new Main().main());");
        indent--; // 4 spaces

        //               }"
        newline(indent);
        print("}");

        //      declare each of our methods
        //      one of our methods is called main()!
        newline(0);

        for (int i = 0; i < ast.getMethods().size(); i++) {
            newline(indent);
            print(ast.getMethods().get(i));
            newline(0);

            // Max advised in OH 11/13:
            // get the type and the name of the function. print out before printing out the statements in the method
            // can do it in this function or the method function
        }
        indent--;
        newline(indent);
        print("}");

        return null;
    }

    @Override
    public Void visit(Ast.Field ast) {
        //throw new UnsupportedOperationException(); //TODO
        if (ast.getTypeName().equals("Integer"))
            print("int");
        else if (ast.getTypeName().equals("String"))
            print("String");
        else if (ast.getTypeName().equals("Boolean"))
            print("boolean");
        else if (ast.getTypeName().equals("Character"))
            print("char");
        else if (ast.getTypeName().equals("Decimal"))
            print("double");
        else if (ast.getTypeName().equals("Any"))
            print("Object");
        else if (ast.getTypeName().equals("Nil"))
            print("Void");
        else if (ast.getTypeName().equals("IntegerIterable"))
            print("Iterable<Integer>");
        else if (ast.getTypeName().equals("Comparable"))
            print("Comparable");
        print(" ");
        print(ast.getName());
        if (ast.getValue().isPresent()) {
            print(" = ");
            print(ast.getValue().get());
        }
        print(";");

        return null;
    }

    // Max advised in OH 11/13:
    // get the type and the name of the function. print out before printing out the statements in the method
    // can do it in this function or the method function
    @Override
    public Void visit(Ast.Method ast) {
        //throw new UnsupportedOperationException(); //TODO
        print(ast.getFunction().getReturnType().getJvmName());
        print(" ");
        print(ast.getName());
        print("(");

        for (int i = 0; i < ast.getParameters().size(); i++) {
            //print Jvm name of current parameter type
            print(Environment.getType(ast.getParameterTypeNames().get(i)).getJvmName());
            print(" ");
            print(ast.getParameters().get(i));

            if (i != ast.getParameters().size() - 1) {
                print(", ");
            }
        }

        print(") {");

        if (!ast.getStatements().isEmpty()) {
            indent++;

            for (Ast.Stmt stmt : ast.getStatements()) {
                newline(indent);
                print(stmt);
            }

            indent--;
            newline(indent);
        }

        print("}");

       return null;
    }

    @Override
    public Void visit(Ast.Stmt.Expression ast) {
        //throw new UnsupportedOperationException(); //TODO
        print(ast.getExpression());
        print(";");
        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Declaration ast) {
        //throw new UnsupportedOperationException(); //TODO
        print(ast.getVariable().getType().getJvmName());
        print(" ");
        print(ast.getVariable().getJvmName());
        if(ast.getValue().isPresent()){
            print(" = ");
            print(ast.getValue().get());
        }
        print(";");
        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Assignment ast) {
        //throw new UnsupportedOperationException(); //TODO
        print(ast.getReceiver());
        print(" = ");
        print(ast.getValue());
        print(";");
        return null;
    }

    @Override
    public Void visit(Ast.Stmt.If ast) {
        //throw new UnsupportedOperationException(); //TODO
        print("if (");
        print(ast.getCondition());
        print(") {");
        indent++;
        for(int i = 0; i < ast.getThenStatements().size(); i++){
            newline(indent);
            print(ast.getThenStatements().get(i));
        }
        indent--;
        newline(indent);
        print("}");

        if (!ast.getElseStatements().isEmpty()) {
            print(" else {");
            indent++;

            for (int j = 0; j < ast.getElseStatements().size(); j++) {
                newline(indent);
                print(ast.getElseStatements().get(j));
            }
            indent--;
            newline(indent);
            print("}");
        }
        return null;
    }

    @Override
    public Void visit(Ast.Stmt.For ast) {
        //throw new UnsupportedOperationException(); //TODO
        print("for (");
        print("int ");
        print(ast.getName());
        print(" : ");
        print(ast.getValue());
        print(") {");
        indent++;
        for(int i = 0; i < ast.getStatements().size(); i++){
            newline(indent);
            print(ast.getStatements().get(i));
        }
        indent--;
        newline(indent);
        print("}");
        return null;
    }

    @Override
    public Void visit(Ast.Stmt.While ast) {
        //throw new UnsupportedOperationException(); //TODO
        print("while (");
        print(ast.getCondition());
        print(") {");

        if (!ast.getStatements().isEmpty())
        {
//            indent++;
            for (int i = 0; i < ast.getStatements().size(); i++)
            {
                newline(indent+1);
                print(ast.getStatements().get(i));
            }
//            indent--;
            newline(indent);
            print("}");
        } else print("}");
        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Return ast) {
        //throw new UnsupportedOperationException(); //TODO
        print("return ");
        print(ast.getValue());
        print(";");
        return null;
    }

    @Override
    public Void visit(Ast.Expr.Literal ast) {
        //throw new UnsupportedOperationException(); //TODO
        if(ast.getType() == Environment.Type.CHARACTER){
            print("'");
            // edit this to print ast.getLiteral()
            print(ast.getLiteral());
            // edit this to print ' not "
            print("'");
        }
        else if(ast.getType() == Environment.Type.STRING){
            print("\"");
            print(ast.getLiteral());
            print("\"");
        } else if (ast.getType() == Environment.Type.DECIMAL) {
            BigDecimal bigDecimal = BigDecimal.class.cast(ast.getLiteral());
            print(bigDecimal.doubleValue());
        } else if (ast.getType() == Environment.Type.INTEGER) {
            BigInteger bigInteger = BigInteger.class.cast(ast.getLiteral());
            print(bigInteger.intValue());
        } else if (ast.getType() == Environment.Type.NIL) {
            print("null");
        }
        else {
            print(ast.getLiteral());
        }
        return null;
    }

    @Override
    public Void visit(Ast.Expr.Group ast) {
        //throw new UnsupportedOperationException(); //TODO
        print("(");
        print(ast.getExpression());
        print(")");
        return null;
    }

    @Override
    public Void visit(Ast.Expr.Binary ast) {
        //throw new UnsupportedOperationException(); //TODO
        print(ast.getLeft());
        print(" ");
        if (ast.getOperator().equals("AND"))
            print("&&");
        else if (ast.getOperator().equals("OR"))
            print("||");
        else
            print(ast.getOperator());
        print(" ");
        print(ast.getRight());
        return null;
    }

    @Override
    public Void visit(Ast.Expr.Access ast) {
        //throw new UnsupportedOperationException(); //TODO
        if (ast.getReceiver().isPresent()) {
            // edit this to print(ast.getReceiver().get())
            print(ast.getReceiver().get());
            print(".");
        }
        print(ast.getVariable().getJvmName());

        return null;
    }

    @Override
    public Void visit(Ast.Expr.Function ast) {
        //throw new UnsupportedOperationException(); //TODO
        if(ast.getReceiver().isPresent()){
            print(ast.getReceiver().get());
            print(".");
        }
        print(ast.getFunction().getJvmName());
        print("(");

        if (!ast.getArguments().isEmpty()) {
            for(int i = 0; i < ast.getArguments().size(); i++){
                print(ast.getArguments().get(i));
                if(i != ast.getArguments().size()-1){
                    print(", ");
                }
            }
        }
        print(")");
        return null;
    }

}
