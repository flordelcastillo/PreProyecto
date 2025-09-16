// Main.java - Programa principal modificado para incluir análisis semántico

import java.io.FileReader;

public class Main {
  public static void main(String[] args) {
    if (args.length != 1) {
      System.err.println("Usage: java Main <input_file>");
      System.exit(1);
    }

    try {
      // Fase 1: Análisis léxico y sintáctico
      System.out.println("=== LEXICAL AND SYNTACTIC ANALYSIS ===");

      parser p = new parser(new Lexer(new FileReader(args[0])));
      java_cup.runtime.Symbol parseResult = p.parse();
      ProgramNode ast = (ProgramNode) parseResult.value;

      if (ast != null) {
        System.out.println("\n=== PARSING COMPLETED SUCCESSFULLY ===");
        System.out.println("=== AST STRUCTURE ===");
        printAST(ast, 0);

        System.out.println("\n=== AST toString() ===");
        System.out.println(ast.toString());

        // Fase 2: Análisis semántico
        System.out.println("\n" + "=".repeat(50));
        System.out.println("STARTING SEMANTIC ANALYSIS");
        System.out.println("=".repeat(50));

        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        ast.accept(analyzer);

        // Mostrar resultados del análisis semántico
        analyzer.printSummary();

        // Si hay errores semánticos, mostrar detalles
        if (analyzer.hasErrors()) {
          System.out.println("\n=== COMPILATION FAILED ===");
          System.out.println("The program contains semantic errors and cannot be executed.");
          System.exit(1);
        } else {
          System.out.println("\n=== COMPILATION SUCCESSFUL ===");
          System.out.println("The program is semantically correct and ready for execution.");

          // Opcionalmente, aquí podrías agregar una fase de interpretación
          // executeProgram(ast, analyzer.getSymbolTable());
        }

      } else {
        System.err.println("Error: AST is null - parsing failed");
        System.exit(1);
      }

    } catch (Exception e) {
      System.err.println("Error during compilation: ");
      e.printStackTrace();
      System.exit(1);
    }
  }

  //Metodo mejorado para imprimir el AST con mejor formato
  public static void printAST(ASTNode node, int indent) {
    String spaces = "  ".repeat(indent);

    if (node instanceof ProgramNode) {
      ProgramNode program = (ProgramNode) node;
      System.out.println(spaces + "Program");
      printAST(program.getMainFunction(), indent + 1);

    } else if (node instanceof FunctionDefNode) {
      FunctionDefNode func = (FunctionDefNode) node;
      System.out.println(spaces + "FunctionDef: " + func.getReturnType() + " " + func.getFunctionName());

      if (!func.getParameters().isEmpty()) {
        System.out.println(spaces + "  Parameters:");
        for (ParamNode param : func.getParameters()) {
          printAST(param, indent + 2);
        }
      }

      System.out.println(spaces + "  Statements:");
      for (StmtNode stmt : func.getStatements()) {
        printAST(stmt, indent + 2);
      }

    } else if (node instanceof ParamNode) {
      ParamNode param = (ParamNode) node;
      System.out.println(spaces + "Parameter: " + param.getType() + " " + param.getName());

    } else if (node instanceof DeclarationNode) {
      DeclarationNode decl = (DeclarationNode) node;
      System.out.println(spaces + "Declaration: " + decl.getType());
      for (VarDeclNode var : decl.getVariables()) {
        printAST(var, indent + 1);
      }

    } else if (node instanceof VarDeclNode) {
      VarDeclNode var = (VarDeclNode) node;
      System.out.print(spaces + "Variable: " + var.getName());
      if (var.hasInitialValue()) {
        System.out.print(" = ");
        printAST(var.getInitialValue(), 0);
      } else {
        System.out.println();
      }

    } else if (node instanceof AssignmentNode) {
      AssignmentNode assign = (AssignmentNode) node;
      System.out.print(spaces + "Assignment: " + assign.getVariableName() + " = ");
      printAST(assign.getExpression(), 0);

    } else if (node instanceof ReturnStmtNode) {
      ReturnStmtNode ret = (ReturnStmtNode) node;
      if (ret.hasExpression()) {
        System.out.print(spaces + "Return: ");
        printAST(ret.getExpression(), 0);
      } else {
        System.out.println(spaces + "Return (void)");
      }

    } else if (node instanceof ExprStmtNode) {
      ExprStmtNode exprStmt = (ExprStmtNode) node;
      System.out.print(spaces + "ExpressionStatement: ");
      printAST(exprStmt.getExpression(), 0);

    } else if (node instanceof BinaryOpNode) {
      BinaryOpNode binOp = (BinaryOpNode) node;
      System.out.print("BinaryOp: " + binOp.getOperator().toString().toLowerCase());
      System.out.print("\n" + spaces + "  Left: ");
      printAST(binOp.getLeft(), indent + 1);
      System.out.print(spaces + "  Right: ");
      printAST(binOp.getRight(), indent + 1);

    } else if (node instanceof NumberNode) {
      NumberNode num = (NumberNode) node;
      System.out.println("Number: " + num.getValue());

    } else if (node instanceof BooleanNode) {
      BooleanNode bool = (BooleanNode) node;
      System.out.println("Boolean: " + bool.getValue());

    } else if (node instanceof VariableNode) {
      VariableNode var = (VariableNode) node;
      System.out.println("Variable: " + var.getName());

    } else {
      System.out.println(spaces + "Unknown node: " + node.getClass().getSimpleName());
    }
  }

  //Metodo opcional para ejecutar el programa (intérprete simple)
  /*
    private static void executeProgram(ProgramNode program, SymbolTable symbolTable) {
        System.out.println("\n=== PROGRAM EXECUTION ===");

        // Aquí podrías implementar un intérprete simple
        // que ejecute el programa usando la tabla de símbolos

        System.out.println("Program execution completed.");
    }
    */
}