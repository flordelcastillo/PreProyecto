// SemanticAnalyzer.java - Implementa ASTVisitor para análisis semántico y construcción de tabla de símbolos

import java.util.List;
import java.util.ArrayList;

/**
 * Excepción para errores semánticos
 */
class SemanticException extends RuntimeException {
    public SemanticException(String message) {
        super(message);
    }
}

/**
 * Analizador semántico que implementa el patrón Visitor
 * Construye la tabla de símbolos y realiza verificaciones semánticas
 */
public class SemanticAnalyzer implements ASTVisitor {
    private SymbolTable symbolTable;
    private List<String> errors;
    private List<String> warnings;

    public SemanticAnalyzer() {
        this.symbolTable = new SymbolTable();
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    private void addError(String message) {
        errors.add("ERROR: " + message);
    }

    private void addWarning(String message) {
        warnings.add("WARNING: " + message);
    }

    @Override
    public Object visitProgram(ProgramNode node) {
        System.out.println("=== SEMANTIC ANALYSIS STARTED ===");

        // Visitar la función main
        node.getMainFunction().accept(this);

        System.out.println("=== SEMANTIC ANALYSIS COMPLETED ===");

        if (hasErrors()) {
            System.out.println("\n=== SEMANTIC ERRORS FOUND ===");
            for (String error : errors) {
                System.err.println(error);
            }
        }

        if (!warnings.isEmpty()) {
            System.out.println("\n=== SEMANTIC WARNINGS ===");
            for (String warning : warnings) {
                System.out.println(warning);
            }
        }

        // Imprimir la tabla de símbolos
        symbolTable.printSymbolTable();
        symbolTable.printStatistics();

        return null;
    }

    @Override
    public Object visitFunctionDef(FunctionDefNode node) {
        String functionName = node.getFunctionName();
        String returnType = node.getReturnType();

        System.out.println("Analyzing function: " + functionName + " (" + returnType + ")");

        // Entrar al scope de la función
        symbolTable.enterScope("function_" + functionName);

        // Agregar parámetros a la tabla de símbolos
        for (ParamNode param : node.getParameters()) {
            param.accept(this);
        }

        // Analizar el cuerpo de la función
        for (StmtNode stmt : node.getStatements()) {
            stmt.accept(this);
        }

        // Verificar que las funciones no-void tengan return
        if (!returnType.equals("void")) {
            if (!hasReturnStatement(node.getStatements())) {
                addError("Function '" + functionName + "' with return type '" + returnType + "' must have a return statement");
            }
        }

        // Salir del scope de la función
        symbolTable.exitScope();

        return null;
    }

    @Override
    public Object visitParam(ParamNode node) {
        String paramName = node.getName();
        String paramType = node.getType();

        // Verificar que el parámetro no esté ya declarado
        if (symbolTable.existsLocal(paramName)) {
            addError("Parameter '" + paramName + "' is already declared in this scope");
        } else {
            // Declarar el parámetro como inicializado
            symbolTable.declare(paramName, paramType, getDefaultValue(paramType), -1, -1);
            System.out.println("Declared parameter: " + paramName + " of type " + paramType);
        }

        return null;
    }

    @Override
    public Object visitDeclaration(DeclarationNode node) {
        String type = node.getType();

        for (VarDeclNode varDecl : node.getVariables()) {
            String varName = varDecl.getName();

            // Verificar que la variable no esté ya declarada en el scope actual
            if (symbolTable.existsLocal(varName)) {
                addError("Variable '" + varName + "' is already declared in this scope");
                continue;
            }

            if (varDecl.hasInitialValue()) {
                // Variable con inicialización
                ExprNode initExpr = varDecl.getInitialValue();
                String initType = (String) initExpr.accept(this);

                // Verificar compatibilidad de tipos
                if (!areTypesCompatible(type, initType)) {
                    addError("Cannot assign " + initType + " to variable '" + varName + "' of type " + type);
                }

                // Evaluar la expresión de inicialización
                Object initValue = evaluateExpression(initExpr);
                symbolTable.declare(varName, type, initValue, -1, -1);
                System.out.println("Declared and initialized variable: " + varName + " = " + initValue);
            } else {
                // Variable sin inicialización
                symbolTable.declare(varName, type, -1, -1);
                System.out.println("Declared uninitialized variable: " + varName + " of type " + type);
                addWarning("Variable '" + varName + "' declared but not initialized");
            }
        }

        return null;
    }

    @Override
    public Object visitVarDecl(VarDeclNode node) {
        // Este método se llama desde visitDeclaration, no necesita implementación separada
        return null;
    }

    @Override
    public Object visitAssignment(AssignmentNode node) {
        String varName = node.getVariableName();
        ExprNode expr = node.getExpression();

        // Verificar que la variable existe
        SymbolEntry entry = symbolTable.lookup(varName);
        if (entry == null) {
            addError("Variable '" + varName + "' is not declared");
            return null;
        }

        // Verificar el tipo de la expresión
        String exprType = (String) expr.accept(this);
        if (!areTypesCompatible(entry.getType(), exprType)) {
            addError("Cannot assign " + exprType + " to variable '" + varName + "' of type " + entry.getType());
        }

        // Evaluar y asignar el valor
        Object value = evaluateExpression(expr);
        symbolTable.assign(varName, value);
        System.out.println("Assigned " + varName + " = " + value);

        return null;
    }

    @Override
    public Object visitReturnStmt(ReturnStmtNode node) {
        if (node.hasExpression()) {
            String exprType = (String) node.getExpression().accept(this);
            Object value = evaluateExpression(node.getExpression());
            System.out.println("Return statement with value: " + value + " (type: " + exprType + ")");
            return exprType;
        } else {
            System.out.println("Void return statement");
            return "void";
        }
    }

    @Override
    public Object visitExprStmt(ExprStmtNode node) {
        return node.getExpression().accept(this);
    }

    @Override
    public Object visitBinaryOp(BinaryOpNode node) {
        String leftType = (String) node.getLeft().accept(this);
        String rightType = (String) node.getRight().accept(this);

        // Verificar compatibilidad de tipos para operaciones aritméticas
        if (!areTypesCompatible(leftType, rightType)) {
            addError("Type mismatch in binary operation: " + leftType + " and " + rightType);
            return "error";
        }

        if (!leftType.equals("int")) {
            addError("Arithmetic operations are only supported for int type, got: " + leftType);
            return "error";
        }

        return "int"; // Las operaciones aritméticas devuelven int
    }

    @Override
    public Object visitNumber(NumberNode node) {
        return "int";
    }

    @Override
    public Object visitBoolean(BooleanNode node) {
        return "bool";
    }

    @Override
    public Object visitVariable(VariableNode node) {
        String varName = node.getName();
        SymbolEntry entry = symbolTable.lookup(varName);

        if (entry == null) {
            addError("Variable '" + varName + "' is not declared");
            return "error";
        }

        if (!entry.isInitialized()) {
            addWarning("Variable '" + varName + "' is used before being initialized");
        }

        return entry.getType();
    }

    // Métodos auxiliares

    private boolean areTypesCompatible(String type1, String type2) {
        if (type1 == null || type2 == null) return false;
        if (type1.equals("error") || type2.equals("error")) return false;
        return type1.equals(type2);
    }

    private Object getDefaultValue(String type) {
        switch (type) {
            case "int": return 0;
            case "bool": return false;
            default: return null;
        }
    }

    private boolean hasReturnStatement(List<StmtNode> statements) {
        for (StmtNode stmt : statements) {
            if (stmt instanceof ReturnStmtNode) {
                return true;
            }
        }
        return false;
    }

    private Object evaluateExpression(ExprNode expr) {
        if (expr instanceof NumberNode) {
            return ((NumberNode) expr).getValue();
        } else if (expr instanceof BooleanNode) {
            return ((BooleanNode) expr).getValue();
        } else if (expr instanceof VariableNode) {
            String varName = ((VariableNode) expr).getName();
            return symbolTable.getValue(varName);
        } else if (expr instanceof BinaryOpNode) {
            BinaryOpNode binOp = (BinaryOpNode) expr;
            Object leftVal = evaluateExpression(binOp.getLeft());
            Object rightVal = evaluateExpression(binOp.getRight());

            if (leftVal instanceof Integer && rightVal instanceof Integer) {
                int left = (Integer) leftVal;
                int right = (Integer) rightVal;

                switch (binOp.getOperator()) {
                    case PLUS: return left + right;
                    case MINUS: return left - right;
                    case TIMES: return left * right;
                    case DIVIDE:
                        if (right == 0) {
                            addError("Division by zero");
                            return 0;
                        }
                        return left / right;
                }
            }
        }

        return null;
    }

    public void printSummary() {
        System.out.println("\n=== SEMANTIC ANALYSIS SUMMARY ===");
        System.out.println("Errors: " + errors.size());
        System.out.println("Warnings: " + warnings.size());

        if (hasErrors()) {
            System.out.println("\nSemantic analysis FAILED due to errors.");
        } else {
            System.out.println("\nSemantic analysis PASSED successfully!");
        }
    }
}