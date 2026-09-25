package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;

public class NodoExpresionSentencia extends NodoAST {

    private final NodoAST expresion;

    public NodoExpresionSentencia(int linea, int columna, NodoAST expresion) {
        super(linea, columna);
        this.expresion = expresion;
    }

    public NodoAST getExpresion() { return expresion; }

    @Override
    public <T> T aceptar(VisitanteAST<T> visitante) {
        return visitante.visitar(this);
    }
}
