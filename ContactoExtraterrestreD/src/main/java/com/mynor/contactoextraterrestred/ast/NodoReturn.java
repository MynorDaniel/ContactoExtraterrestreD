package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;

public class NodoReturn extends NodoAST {

    private final NodoAST valor; 

    public NodoReturn(int linea, int columna, NodoAST valor) {
        super(linea, columna);
        this.valor = valor;
    }

    public NodoAST getValor() { return valor; }

    @Override
    public <T> T aceptar(VisitanteAST<T> visitante) {
        return visitante.visitar(this);
    }
}
