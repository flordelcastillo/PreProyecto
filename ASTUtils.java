// ASTUtils.java - Clases auxiliares para el AST

import java.util.List;
import java.util.ArrayList;

class ASTUtils {
    // Método para crear una lista de variables a partir de una lista de nombres
    public static List<VarDeclNode> createVarDeclList(String varListStr) {
        List<VarDeclNode> variables = new ArrayList<>();
        String[] parts = varListStr.split(",");

        for (String part : parts) {
            part = part.trim();
            if (part.contains("=")) {
                // Variable con inicialización
                String[] assignment = part.split("=", 2);
                String varName = assignment[0].trim();
                String initValue = assignment[1].trim();
                // Aquí necesitarías parsear la expresión de inicialización
                // Por simplicidad, asumimos que es un número
                ExprNode initExpr = parseSimpleExpression(initValue);
                variables.add(new VarDeclNode(varName, initExpr));
            } else {
                // Variable sin inicialización
                variables.add(new VarDeclNode(part, null));
            }
        }

        return variables;
    }

    // Método auxiliar simple para parsear expresiones básicas
    private static ExprNode parseSimpleExpression(String expr) {
        expr = expr.trim();
        if (expr.equals("true")) {
            return new BooleanNode(true);
        } else if (expr.equals("false")) {
            return new BooleanNode(false);
        } else if (expr.matches("\\d+")) {
            return new NumberNode(Integer.parseInt(expr));
        } else if (expr.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            return new VariableNode(expr);
        }
        // Para expresiones más complejas, retorna null
        return null;
    }

    // Método para imprimir el AST de forma legible
    public static void printAST(ASTNode node, int indent) {
        String spaces = "  ".repeat(indent);
        System.out.println(spaces + node.getClass().getSimpleName() + ": " + node.toString());
    }

    // Método para crear operadores binarios
    public static BinaryOpNode.Operator stringToOperator(String op) {
        switch (op) {
            case "+": return BinaryOpNode.Operator.PLUS;
            case "-": return BinaryOpNode.Operator.MINUS;
            case "*": return BinaryOpNode.Operator.TIMES;
            case "/": return BinaryOpNode.Operator.DIVIDE;
            default: throw new IllegalArgumentException("Unknown operator: " + op);
        }
    }
}