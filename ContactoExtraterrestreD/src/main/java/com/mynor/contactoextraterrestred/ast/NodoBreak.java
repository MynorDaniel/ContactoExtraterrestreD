package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;

public class NodoBreak extends NodoAST {

    public NodoBreak(int linea, int columna) {
        super(linea, columna);
    }

    @Override
    public <T> T aceptar(VisitanteAST<T> visitante) {
        return visitante.visitar(this);
    }
}
