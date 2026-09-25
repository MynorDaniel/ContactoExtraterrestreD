package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;
import java.util.List;

public class NodoDoWhile extends NodoAST {

    private final List<NodoAST> cuerpo;
    private final NodoAST condicion;

    public NodoDoWhile(int linea, int columna, List<NodoAST> cuerpo, NodoAST condicion) {
        super(linea, columna);
        this.cuerpo = cuerpo;
        this.condicion = condicion;
    }

    public List<NodoAST> getCuerpo() { return cuerpo; }
    public NodoAST getCondicion() { return condicion; }

    @Override
    public <T> T aceptar(VisitanteAST<T> visitante) {
        return visitante.visitar(this);
    }
}
