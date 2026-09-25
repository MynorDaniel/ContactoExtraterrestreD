package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;

public class NodoLiteral extends NodoAST {

    private final Object valor;
    private final TipoLiteral tipo;

    public NodoLiteral(int linea, int columna, Object valor, TipoLiteral tipo) {
        super(linea, columna);
        this.valor = valor;
        this.tipo = tipo;
    }

    public Object getValor() { return valor; }
    public TipoLiteral getTipo() { return tipo; }

    @Override
    public <T> T aceptar(VisitanteAST<T> visitante) {
        return visitante.visitar(this);
    }
}
