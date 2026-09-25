package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;

public class NodoLeer extends NodoAST {

    private final NodoAST destino; 

    public NodoLeer(int linea, int columna, NodoAST destino) {
        super(linea, columna);
        this.destino = destino;
    }

    public NodoAST getDestino() { return destino; }

    @Override
    public <T> T aceptar(VisitanteAST<T> visitante) {
        return visitante.visitar(this);
    }
}
