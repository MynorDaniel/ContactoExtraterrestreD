package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;

public class NodoDimension extends NodoAST {

    private final NodoAST tamano;

    public NodoDimension(int linea, int columna, NodoAST tamano) {
        super(linea, columna);
        this.tamano = tamano;
    }

    public NodoAST getTamano() { return tamano; }

    @Override
    public <T> T aceptar(VisitanteAST<T> visitante) {
        return visitante.visitar(this);
    }
}
