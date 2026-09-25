package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;

public class NodoImportacion extends NodoAST {

    private final String ruta;

    public NodoImportacion(int linea, int columna, String ruta) {
        super(linea, columna);
        this.ruta = ruta;
    }

    public String getRuta() { return ruta; }

    @Override
    public <T> T aceptar(VisitanteAST<T> visitante) {
        return visitante.visitar(this);
    }
}
