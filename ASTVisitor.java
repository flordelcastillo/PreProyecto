// ASTVisitor.java - Interface para el patrón Visitor

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

