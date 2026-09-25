package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;
import java.util.List;

public class NodoWhile extends NodoAST {

    private final NodoAST condicion;
    private final List<NodoAST> cuerpo;

    public NodoWhile(int linea, int columna, NodoAST condicion, List<NodoAST> cuerpo) {
        super(linea, columna);
        this.condicion = condicion;
        this.cuerpo = cuerpo;
    }

    public NodoAST getCondicion() { return condicion; }
    public List<NodoAST> getCuerpo() { return cuerpo; }

    @Override
    public <T> T aceptar(VisitanteAST<T> visitante) {
        return visitante.visitar(this);
    }
}
