package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;
import java.util.List;

public class NodoListaExpresiones extends NodoAST {

    private final List<NodoAST> elementos;

    public NodoListaExpresiones(int linea, int columna, List<NodoAST> elementos) {
        super(linea, columna);
        this.elementos = elementos;
    }

    public List<NodoAST> getElementos() { return elementos; }

    @Override
    public <T> T aceptar(VisitanteAST<T> visitante) {
        return visitante.visitar(this);
    }
}
