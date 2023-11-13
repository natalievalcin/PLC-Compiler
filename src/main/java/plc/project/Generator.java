package plc.project;

import java.io.PrintWriter;

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

            // Max advised in OH 11/13:
            // get the type and the name of the function. print out before printing out the statements in the method
            // can do it in this function or the method function
        }
        newline(0);
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
        print(ast.getFunction().getName());
        print("(");
        for (int i = 0; i < ast.getParameters().size(); i++) {
            print(ast.getParameterTypeNames().get(i));
            print(" ");
            print(ast.getParameters().get(i));
            if (i != ast.getParameters().size() - 1)
                print(", ");
        }
        print(") {");

        if (!ast.getStatements().isEmpty()) {
            indent++;
            for (int i = 0; i < ast.getStatements().size(); i++) {
                newline(indent);
                print(ast.getStatements().get(i));
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
        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Declaration ast) {
        //throw new UnsupportedOperationException(); //TODO
        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Assignment ast) {
        throw new UnsupportedOperationException(); //TODO
        //return null;
    }

    @Override
    public Void visit(Ast.Stmt.If ast) {
        //throw new UnsupportedOperationException(); //TODO
        return null;
    }

    @Override
    public Void visit(Ast.Stmt.For ast) {
        throw new UnsupportedOperationException(); //TODO
        //return null;
    }

    @Override
    public Void visit(Ast.Stmt.While ast) {
        //throw new UnsupportedOperationException(); //TODO
        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Return ast) {
        //throw new UnsupportedOperationException(); //TODO
        return null;
    }

    @Override
    public Void visit(Ast.Expr.Literal ast) {
        //throw new UnsupportedOperationException(); //TODO
        return null;
    }

    @Override
    public Void visit(Ast.Expr.Group ast) {
        //throw new UnsupportedOperationException(); //TODO
        return null;
    }

    @Override
    public Void visit(Ast.Expr.Binary ast) {
        //throw new UnsupportedOperationException(); //TODO
        return null;
    }

    @Override
    public Void visit(Ast.Expr.Access ast) {
        throw new UnsupportedOperationException(); //TODO
        //return null;
    }

    @Override
    public Void visit(Ast.Expr.Function ast) {
        //throw new UnsupportedOperationException(); //TODO
        return null;
    }

}
