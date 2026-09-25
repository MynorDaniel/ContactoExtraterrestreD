package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;
import java.util.List;

public class NodoBloque extends NodoAST {

    private final List<NodoAST> sentencias;

    public NodoBloque(int linea, int columna, List<NodoAST> sentencias) {
        super(linea, columna);
        this.sentencias = sentencias;
    }

    public List<NodoAST> getSentencias() { return sentencias; }

    @Override
    public <T> T aceptar(VisitanteAST<T> visitante) {
        return visitante.visitar(this);
    }
}
