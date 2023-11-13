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
//        newline(indent);
//
//        print("int main() {");
//
//        indent++;
        for (int i = 0; i < ast.getMethods().size(); i++) {
            newline(indent);
            print(ast.getMethods().get(i));
        }
        newline(0);
        indent--;

        //      print "}" to close the class Main
        newline(indent);
        print("}");

        return null;
    }

    @Override
    public Void visit(Ast.Field ast) {
        //throw new UnsupportedOperationException(); //TODO
        return null;
    }

    @Override
    public Void visit(Ast.Method ast) {
        //throw new UnsupportedOperationException(); //TODO
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
