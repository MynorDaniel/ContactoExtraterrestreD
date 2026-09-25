package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;

public class NodoIncrementoDecremento extends NodoAST {

    private final NodoAST destino;
    private final String operador;

    public NodoIncrementoDecremento(int linea, int columna, NodoAST destino, String operador) {
        super(linea, columna);
        this.destino = destino;
        this.operador = operador;
    }

    public NodoAST getDestino() { return destino; }
    public String getOperador() { return operador; }

    @Override
    public <T> T aceptar(VisitanteAST<T> visitante) {
        return visitante.visitar(this);
    }
}
