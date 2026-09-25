package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;

public class NodoUnaria extends NodoAST {

    private final String operador;
    private final NodoAST operando;

    public NodoUnaria(int linea, int columna, String operador, NodoAST operando) {
        super(linea, columna);
        this.operador = operador;
        this.operando = operando;
    }

    public String getOperador() { return operador; }
    public NodoAST getOperando() { return operando; }

    @Override
    public <T> T aceptar(VisitanteAST<T> visitante) {
        return visitante.visitar(this);
    }
}
