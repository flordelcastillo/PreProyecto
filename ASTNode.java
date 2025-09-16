// ASTNode.java - Definición de clases para el Árbol Sintáctico Abstracto

import java.util.List;
import java.util.ArrayList;

// Clase base abstracta para todos los nodos del AST
abstract class ASTNode {
    public abstract String toString();
    public abstract Object accept(ASTVisitor visitor);
}

// ========== NODOS DE PROGRAMA ==========

class ProgramNode extends ASTNode {
    private FunctionDefNode mainFunction;

    public ProgramNode(FunctionDefNode mainFunction) {
        this.mainFunction = mainFunction;
    }

    public FunctionDefNode getMainFunction() { return mainFunction; }

    @Override
    public String toString() {
        return "Program(" + mainFunction + ")";
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visitProgram(this);
    }
}

class FunctionDefNode extends ASTNode {
    private String returnType;
    private String functionName;
    private List<ParamNode> parameters;
    private List<StmtNode> statements;

    public FunctionDefNode(String returnType, String functionName,
                           List<ParamNode> parameters, List<StmtNode> statements) {
        this.returnType = returnType;
        this.functionName = functionName;
        this.parameters = parameters != null ? parameters : new ArrayList<>();
        this.statements = statements;
    }

    public String getReturnType() { return returnType; }
    public String getFunctionName() { return functionName; }
    public List<ParamNode> getParameters() { return parameters; }
    public List<StmtNode> getStatements() { return statements; }

    @Override
    public String toString() {
        return "FunctionDef(" + returnType + " " + functionName +
                "(" + parameters + ") {" + statements + "})";
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visitFunctionDef(this);
    }
}

class ParamNode extends ASTNode {
    private String type;
    private String name;

    public ParamNode(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getType() { return type; }
    public String getName() { return name; }

    @Override
    public String toString() {
        return type + " " + name;
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visitParam(this);
    }
}

// ========== NODOS DE SENTENCIAS ==========

abstract class StmtNode extends ASTNode {
    // Clase base para todas las sentencias
}

class DeclarationNode extends StmtNode {
    private String type;
    private List<VarDeclNode> variables;

    public DeclarationNode(String type, List<VarDeclNode> variables) {
        this.type = type;
        this.variables = variables;
    }

    public String getType() { return type; }
    public List<VarDeclNode> getVariables() { return variables; }

    @Override
    public String toString() {
        return "Declaration(" + type + " " + variables + ")";
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visitDeclaration(this);
    }
}

class VarDeclNode extends ASTNode {
    private String name;
    private ExprNode initialValue; // null si no tiene inicialización

    public VarDeclNode(String name, ExprNode initialValue) {
        this.name = name;
        this.initialValue = initialValue;
    }

    public String getName() { return name; }
    public ExprNode getInitialValue() { return initialValue; }
    public boolean hasInitialValue() { return initialValue != null; }

    @Override
    public String toString() {
        return hasInitialValue() ? name + " = " + initialValue : name;
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visitVarDecl(this);
    }
}

class AssignmentNode extends StmtNode {
    private String variableName;
    private ExprNode expression;

    public AssignmentNode(String variableName, ExprNode expression) {
        this.variableName = variableName;
        this.expression = expression;
    }

    public String getVariableName() { return variableName; }
    public ExprNode getExpression() { return expression; }

    @Override
    public String toString() {
        return "Assignment(" + variableName + " = " + expression + ")";
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visitAssignment(this);
    }
}

class ReturnStmtNode extends StmtNode {
    private ExprNode expression; // null para return sin valor

    public ReturnStmtNode(ExprNode expression) {
        this.expression = expression;
    }

    public ExprNode getExpression() { return expression; }
    public boolean hasExpression() { return expression != null; }

    @Override
    public String toString() {
        return hasExpression() ? "Return(" + expression + ")" : "Return()";
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visitReturnStmt(this);
    }
}

class ExprStmtNode extends StmtNode {
    private ExprNode expression;

    public ExprStmtNode(ExprNode expression) {
        this.expression = expression;
    }

    public ExprNode getExpression() { return expression; }

    @Override
    public String toString() {
        return "ExprStmt(" + expression + ")";
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visitExprStmt(this);
    }
}

// ========== NODOS DE EXPRESIONES ==========

abstract class ExprNode extends ASTNode {
    // Clase base para todas las expresiones
}

class BinaryOpNode extends ExprNode {
    public enum Operator {
        PLUS, MINUS, TIMES, DIVIDE
    }

    private ExprNode left;
    private Operator operator;
    private ExprNode right;

    public BinaryOpNode(ExprNode left, Operator operator, ExprNode right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public ExprNode getLeft() { return left; }
    public Operator getOperator() { return operator; }
    public ExprNode getRight() { return right; }

    @Override
    public String toString() {
        String op = "";
        switch (operator) {
            case PLUS: op = "+"; break;
            case MINUS: op = "-"; break;
            case TIMES: op = "*"; break;
            case DIVIDE: op = "/"; break;
        }
        return "(" + left + " " + op + " " + right + ")";
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visitBinaryOp(this);
    }
}

class NumberNode extends ExprNode {
    private int value;

    public NumberNode(int value) {
        this.value = value;
    }

    public int getValue() { return value; }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visitNumber(this);
    }
}

class BooleanNode extends ExprNode {
    private boolean value;

    public BooleanNode(boolean value) {
        this.value = value;
    }

    public boolean getValue() { return value; }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visitBoolean(this);
    }
}

class VariableNode extends ExprNode {
    private String name;

    public VariableNode(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visitVariable(this);
    }
}

// ========== VISITOR PATTERN ==========

interface ASTVisitor {
    Object visitProgram(ProgramNode node);
    Object visitFunctionDef(FunctionDefNode node);
    Object visitParam(ParamNode node);
    Object visitDeclaration(DeclarationNode node);
    Object visitVarDecl(VarDeclNode node);
    Object visitAssignment(AssignmentNode node);
    Object visitReturnStmt(ReturnStmtNode node);
    Object visitExprStmt(ExprStmtNode node);
    Object visitBinaryOp(BinaryOpNode node);
    Object visitNumber(NumberNode node);
    Object visitBoolean(BooleanNode node);
    Object visitVariable(VariableNode node);
}

// ========== CLASES AUXILIARES ==========

class ASTUtils {
    //Metodo para crear una lista de variables a partir de una lista de nombres
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

    //Metodo auxiliar simple para parsear expresiones básicas
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
}