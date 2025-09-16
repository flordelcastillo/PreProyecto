// SymbolTable.java - Implementación de tabla de símbolos con scopes anidados

import java.util.*;

/**
 * Entrada en la tabla de símbolos que representa un símbolo
 */
class SymbolEntry {
    private String name;
    private String type;
    private Object value;
    private boolean isInitialized;
    private int declarationLine;
    private int declarationColumn;

    public SymbolEntry(String name, String type, int line, int column) {
        this.name = name;
        this.type = type;
        this.value = null;
        this.isInitialized = false;
        this.declarationLine = line;
        this.declarationColumn = column;
    }

    public SymbolEntry(String name, String type, Object value, int line, int column) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.isInitialized = (value != null);
        this.declarationLine = line;
        this.declarationColumn = column;
    }

    // Getters
    public String getName() { return name; }
    public String getType() { return type; }
    public Object getValue() { return value; }
    public boolean isInitialized() { return isInitialized; }
    public int getDeclarationLine() { return declarationLine; }
    public int getDeclarationColumn() { return declarationColumn; }

    // Setters
    public void setValue(Object value) {
        this.value = value;
        this.isInitialized = true;
    }

    public void setInitialized(boolean initialized) {
        this.isInitialized = initialized;
    }

    @Override
    public String toString() {
        return String.format("Symbol{name='%s', type='%s', value=%s, initialized=%b, line=%d, col=%d}",
                name, type, value, isInitialized, declarationLine, declarationColumn);
    }
}

/**
 * Representa un scope individual
 */
class Scope {
    private String scopeName;
    private Map<String, SymbolEntry> symbols;
    private Scope parent;
    private List<Scope> children;

    public Scope(String name, Scope parent) {
        this.scopeName = name;
        this.symbols = new HashMap<>();
        this.parent = parent;
        this.children = new ArrayList<>();
        if (parent != null) {
            parent.addChild(this);
        }
    }

    public void addChild(Scope child) {
        children.add(child);
    }

    public boolean declare(String name, String type, int line, int column) {
        if (symbols.containsKey(name)) {
            return false; // Ya existe en este scope
        }
        symbols.put(name, new SymbolEntry(name, type, line, column));
        return true;
    }

    public boolean declare(String name, String type, Object value, int line, int column) {
        if (symbols.containsKey(name)) {
            return false; // Ya existe en este scope
        }
        symbols.put(name, new SymbolEntry(name, type, value, line, column));
        return true;
    }

    public SymbolEntry lookup(String name) {
        return symbols.get(name);
    }

    public Set<String> getSymbolNames() {
        return symbols.keySet();
    }

    public Collection<SymbolEntry> getSymbols() {
        return symbols.values();
    }

    // Getters
    public String getScopeName() { return scopeName; }
    public Scope getParent() { return parent; }
    public List<Scope> getChildren() { return children; }

    @Override
    public String toString() {
        return "Scope{" + scopeName + ", symbols=" + symbols.size() + "}";
    }
}

/**
 * Tabla de símbolos principal que maneja múltiples scopes
 */
public class SymbolTable {
    private Scope globalScope;
    private Scope currentScope;
    private int scopeCounter;

    public SymbolTable() {
        this.globalScope = new Scope("global", null);
        this.currentScope = globalScope;
        this.scopeCounter = 0;
    }

    /**
     * Entra a un nuevo scope
     */
    public void enterScope(String scopeName) {
        String fullName = scopeName + "_" + (++scopeCounter);
        currentScope = new Scope(fullName, currentScope);
    }

    /**
     * Sale del scope actual
     */
    public boolean exitScope() {
        if (currentScope.getParent() == null) {
            return false; // No se puede salir del scope global
        }
        currentScope = currentScope.getParent();
        return true;
    }

    /**
     * Declara una variable en el scope actual
     */
    public boolean declare(String name, String type) {
        return currentScope.declare(name, type, -1, -1);
    }

    public boolean declare(String name, String type, int line, int column) {
        return currentScope.declare(name, type, line, column);
    }

    public boolean declare(String name, String type, Object value, int line, int column) {
        return currentScope.declare(name, type, value, line, column);
    }

    /**
     * Busca un símbolo en el scope actual y en los scopes padre
     */
    public SymbolEntry lookup(String name) {
        Scope scope = currentScope;
        while (scope != null) {
            SymbolEntry entry = scope.lookup(name);
            if (entry != null) {
                return entry;
            }
            scope = scope.getParent();
        }
        return null; // No encontrado
    }

    /**
     * Busca un símbolo solo en el scope actual
     */
    public SymbolEntry lookupLocal(String name) {
        return currentScope.lookup(name);
    }

    /**
     * Verifica si un símbolo existe
     */
    public boolean exists(String name) {
        return lookup(name) != null;
    }

    /**
     * Verifica si un símbolo existe en el scope actual
     */
    public boolean existsLocal(String name) {
        return lookupLocal(name) != null;
    }

    /**
     * Asigna un valor a una variable existente
     */
    public boolean assign(String name, Object value) {
        SymbolEntry entry = lookup(name);
        if (entry != null) {
            entry.setValue(value);
            return true;
        }
        return false;
    }

    /**
     * Obtiene el valor de una variable
     */
    public Object getValue(String name) {
        SymbolEntry entry = lookup(name);
        return entry != null ? entry.getValue() : null;
    }

    /**
     * Obtiene el tipo de una variable
     */
    public String getType(String name) {
        SymbolEntry entry = lookup(name);
        return entry != null ? entry.getType() : null;
    }

    /**
     * Verifica si una variable está inicializada
     */
    public boolean isInitialized(String name) {
        SymbolEntry entry = lookup(name);
        return entry != null && entry.isInitialized();
    }

    /**
     * Obtiene información del scope actual
     */
    public Scope getCurrentScope() {
        return currentScope;
    }

    public Scope getGlobalScope() {
        return globalScope;
    }

    /**
     * Imprime el contenido de la tabla de símbolos
     */
    public void printSymbolTable() {
        System.out.println("\n=== SYMBOL TABLE ===");
        printScope(globalScope, 0);
    }

    private void printScope(Scope scope, int indent) {
        String indentation = "  ".repeat(indent);
        System.out.println(indentation + "Scope: " + scope.getScopeName());

        for (SymbolEntry entry : scope.getSymbols()) {
            System.out.println(indentation + "  " + entry);
        }

        for (Scope child : scope.getChildren()) {
            printScope(child, indent + 1);
        }
    }

    /**
     * Obtiene estadísticas de la tabla de símbolos
     */
    public void printStatistics() {
        int totalSymbols = countSymbols(globalScope);
        int totalScopes = countScopes(globalScope);

        System.out.println("\n=== SYMBOL TABLE STATISTICS ===");
        System.out.println("Total scopes: " + totalScopes);
        System.out.println("Total symbols: " + totalSymbols);
        System.out.println("Current scope: " + currentScope.getScopeName());
    }

    private int countSymbols(Scope scope) {
        int count = scope.getSymbols().size();
        for (Scope child : scope.getChildren()) {
            count += countSymbols(child);
        }
        return count;
    }

    private int countScopes(Scope scope) {
        int count = 1; // Este scope
        for (Scope child : scope.getChildren()) {
            count += countScopes(child);
        }
        return count;
    }

    /**
     * Limpia la tabla de símbolos
     */
    public void clear() {
        this.globalScope = new Scope("global", null);
        this.currentScope = globalScope;
        this.scopeCounter = 0;
    }
}