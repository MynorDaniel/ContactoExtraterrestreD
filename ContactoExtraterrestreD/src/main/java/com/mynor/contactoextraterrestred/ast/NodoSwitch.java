package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;
import java.util.List;

public class NodoSwitch extends NodoAST {

    private final NodoAST expresion;
    private final List<NodoCaso> casos;
    private final List<NodoAST> casoPorDefecto;

    public NodoSwitch(int linea, int columna, NodoAST expresion, List<NodoCaso> casos,
                       List<NodoAST> casoPorDefecto) {
        super(linea, columna);
        this.expresion = expresion;
        this.casos = casos;
        this.casoPorDefecto = casoPorDefecto;
    }

    public NodoAST getExpresion() { return expresion; }
    public List<NodoCaso> getCasos() { return casos; }
    public List<NodoAST> getCasoPorDefecto() { return casoPorDefecto; }

    @Override
    public <T> T aceptar(VisitanteAST<T> visitante) {
        return visitante.visitar(this);
    }
}
