package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;
import java.util.List;

public class NodoLlamada extends NodoAST {

    private final NodoAST base;
    private final List<NodoAST> argumentos;

    public NodoLlamada(int linea, int columna, NodoAST base, List<NodoAST> argumentos) {
        super(linea, columna);
        this.base = base;
        this.argumentos = argumentos;
    }

    public NodoAST getBase() { return base; }
    public List<NodoAST> getArgumentos() { return argumentos; }

    @Override
    public <T> T aceptar(VisitanteAST<T> visitante) {
        return visitante.visitar(this);
    }
}
