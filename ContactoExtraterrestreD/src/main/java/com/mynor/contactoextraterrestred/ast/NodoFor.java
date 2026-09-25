package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;
import java.util.List;

public class NodoFor extends NodoAST {

    private final NodoAST inicializacion;
    private final NodoAST condicion;
    private final NodoAST actualizacion;
    private final List<NodoAST> cuerpo;

    public NodoFor(int linea, int columna, NodoAST inicializacion, NodoAST condicion,
            NodoAST actualizacion, List<NodoAST> cuerpo) {
        super(linea, columna);
        this.inicializacion = inicializacion;
        this.condicion = condicion;
        this.actualizacion = actualizacion;
        this.cuerpo = cuerpo;
    }

    public NodoAST getInicializacion() {
        return inicializacion;
    }

    public NodoAST getCondicion() {
        return condicion;
    }

    public NodoAST getActualizacion() {
        return actualizacion;
    }

    public List<NodoAST> getCuerpo() {
        return cuerpo;
    }

    @Override
    public <T> T aceptar(VisitanteAST<T> visitante) {
        return visitante.visitar(this);
    }
}
