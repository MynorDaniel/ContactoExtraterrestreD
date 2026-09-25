package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;
import java.util.List;

public class NodoCaso extends NodoAST {

    private final NodoLiteral valor;
    private final List<NodoAST> cuerpo;

    public NodoCaso(int linea, int columna, NodoLiteral valor, List<NodoAST> cuerpo) {
        super(linea, columna);
        this.valor = valor;
        this.cuerpo = cuerpo;
    }

    public NodoLiteral getValor() { return valor; }
    public List<NodoAST> getCuerpo() { return cuerpo; }

    @Override
    public <T> T aceptar(VisitanteAST<T> visitante) {
        return visitante.visitar(this);
    }
}
