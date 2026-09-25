package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;
import java.util.List;

public class NodoNuevoArreglo extends NodoAST {

    private final String tipoBase;
    private final List<NodoAST> dimensiones;

    public NodoNuevoArreglo(int linea, int columna, String tipoBase, List<NodoAST> dimensiones) {
        super(linea, columna);
        this.tipoBase = tipoBase;
        this.dimensiones = dimensiones;
    }

    public String getTipoBase() { return tipoBase; }
    public List<NodoAST> getDimensiones() { return dimensiones; }

    @Override
    public <T> T aceptar(VisitanteAST<T> visitante) {
        return visitante.visitar(this);
    }
}
