package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;

public class NodoAccesoArreglo extends NodoAST {

    private final NodoAST base;
    private final NodoAST indice;

    public NodoAccesoArreglo(int linea, int columna, NodoAST base, NodoAST indice) {
        super(linea, columna);
        this.base = base;
        this.indice = indice;
    }

    public NodoAST getBase() { return base; }
    public NodoAST getIndice() { return indice; }

    @Override
    public <T> T aceptar(VisitanteAST<T> visitante) {
        return visitante.visitar(this);
    }
}
