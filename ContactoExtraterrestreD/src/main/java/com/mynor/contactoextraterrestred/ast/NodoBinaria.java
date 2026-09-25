package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;

public class NodoBinaria extends NodoAST {

    private final NodoAST izquierda;
    private final String operador;
    private final NodoAST derecha;

    public NodoBinaria(int linea, int columna, NodoAST izquierda, String operador, NodoAST derecha) {
        super(linea, columna);
        this.izquierda = izquierda;
        this.operador = operador;
        this.derecha = derecha;
    }

    public NodoAST getIzquierda() { return izquierda; }
    public String getOperador() { return operador; }
    public NodoAST getDerecha() { return derecha; }

    @Override
    public <T> T aceptar(VisitanteAST<T> visitante) {
        return visitante.visitar(this);
    }
}
