package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;

public class NodoTipo extends NodoAST {

    private final String nombre;

    private final int nivelesArreglo;

    public NodoTipo(int linea, int columna, String nombre, int nivelesArreglo) {
        super(linea, columna);
        this.nombre = nombre;
        this.nivelesArreglo = nivelesArreglo;
    }

    public String getNombre() {
        return nombre;
    }

    public int getNivelesArreglo() {
        return nivelesArreglo;
    }

    public boolean esArreglo() {
        return nivelesArreglo > 0;
    }

    @Override
    public <T> T aceptar(VisitanteAST<T> visitante) {
        return visitante.visitar(this);
    }
}
