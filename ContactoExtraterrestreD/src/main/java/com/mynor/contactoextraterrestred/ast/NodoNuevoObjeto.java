package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;
import java.util.List;

public class NodoNuevoObjeto extends NodoAST {

    private final String nombreClase;
    private final List<NodoAST> argumentos;

    public NodoNuevoObjeto(int linea, int columna, String nombreClase, List<NodoAST> argumentos) {
        super(linea, columna);
        this.nombreClase = nombreClase;
        this.argumentos = argumentos;
    }

    public String getNombreClase() { return nombreClase; }
    public List<NodoAST> getArgumentos() { return argumentos; }

    @Override
    public <T> T aceptar(VisitanteAST<T> visitante) {
        return visitante.visitar(this);
    }
}
