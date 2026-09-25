package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;

public abstract class NodoAST {

    private final int linea;

    private final int columna;

    protected NodoAST(int linea, int columna) {
        this.linea = linea;
        this.columna = columna;
    }

    public int getLinea() {
        return linea;
    }

    public int getColumna() {
        return columna;
    }

    public abstract <T> T aceptar(VisitanteAST<T> visitante);

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + linea + ":" + columna + "]";
    }
}
