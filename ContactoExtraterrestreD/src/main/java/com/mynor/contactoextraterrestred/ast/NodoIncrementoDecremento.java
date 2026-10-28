package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;

public class NodoIncrementoDecremento extends NodoAST {

    private final NodoAST destino;
    private final String operador;
    private final boolean devuelveValorAnterior;

    public NodoIncrementoDecremento(int linea, int columna, NodoAST destino, String operador) {
        this(linea, columna, destino, operador, false);
    }

    public NodoIncrementoDecremento(int linea, int columna, NodoAST destino, String operador,
            boolean devuelveValorAnterior) {
        super(linea, columna);
        this.destino = destino;
        this.operador = operador;
        this.devuelveValorAnterior = devuelveValorAnterior;
    }

    public NodoAST getDestino() {
        return destino;
    }

    public String getOperador() {
        return operador;
    }

    public boolean devuelveValorAnterior() {
        return devuelveValorAnterior;
    }

    @Override
    public <T> T aceptar(VisitanteAST<T> visitante) {
        return visitante.visitar(this);
    }
}
