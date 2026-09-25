package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;
import java.util.List;

public class NodoImprimir extends NodoAST {

    private final List<NodoAST> expresiones;
    private final boolean saltoDeLinea;

    public NodoImprimir(int linea, int columna, List<NodoAST> expresiones, boolean saltoDeLinea) {
        super(linea, columna);
        this.expresiones = expresiones;
        this.saltoDeLinea = saltoDeLinea;
    }

    public List<NodoAST> getExpresiones() { return expresiones; }
    public boolean isSaltoDeLinea() { return saltoDeLinea; }

    @Override
    public <T> T aceptar(VisitanteAST<T> visitante) {
        return visitante.visitar(this);
    }
}
