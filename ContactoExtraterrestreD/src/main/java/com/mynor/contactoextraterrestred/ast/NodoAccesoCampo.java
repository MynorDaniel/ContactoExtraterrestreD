package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;

public class NodoAccesoCampo extends NodoAST {

    private final NodoAST base;
    private final String campo;

    public NodoAccesoCampo(int linea, int columna, NodoAST base, String campo) {
        super(linea, columna);
        this.base = base;
        this.campo = campo;
    }

    public NodoAST getBase() { return base; }
    public String getCampo() { return campo; }

    @Override
    public <T> T aceptar(VisitanteAST<T> visitante) {
        return visitante.visitar(this);
    }
}
