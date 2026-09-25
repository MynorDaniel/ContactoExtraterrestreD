package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;

public class NodoAsignacion extends NodoAST {

    private final NodoAST destino;
    private final String operador;
    private final NodoAST valor;

    public NodoAsignacion(int linea, int columna, NodoAST destino, String operador, NodoAST valor) {
        super(linea, columna);
        this.destino = destino;
        this.operador = operador;
        this.valor = valor;
    }

    public NodoAST getDestino() { return destino; }
    public String getOperador() { return operador; }
    public NodoAST getValor() { return valor; }

    @Override
    public <T> T aceptar(VisitanteAST<T> visitante) {
        return visitante.visitar(this);
    }
}
