package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;

public class NodoTernaria extends NodoAST {

    private final NodoAST condicion;
    private final NodoAST siVerdadero;
    private final NodoAST siFalso;

    public NodoTernaria(int linea, int columna, NodoAST condicion, NodoAST siVerdadero, NodoAST siFalso) {
        super(linea, columna);
        this.condicion = condicion;
        this.siVerdadero = siVerdadero;
        this.siFalso = siFalso;
    }

    public NodoAST getCondicion() { return condicion; }
    public NodoAST getSiVerdadero() { return siVerdadero; }
    public NodoAST getSiFalso() { return siFalso; }

    @Override
    public <T> T aceptar(VisitanteAST<T> visitante) {
        return visitante.visitar(this);
    }
}
