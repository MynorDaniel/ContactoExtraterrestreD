package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;
import java.util.List;

public class NodoIf extends NodoAST {

    private final NodoAST condicion;
    private final List<NodoAST> cuerpoSi;
    private final List<NodoSinoSi> ramasSinoSi;
    private final List<NodoAST> cuerpoSino; 

    public NodoIf(int linea, int columna, NodoAST condicion, List<NodoAST> cuerpoSi,
                   List<NodoSinoSi> ramasSinoSi, List<NodoAST> cuerpoSino) {
        super(linea, columna);
        this.condicion = condicion;
        this.cuerpoSi = cuerpoSi;
        this.ramasSinoSi = ramasSinoSi;
        this.cuerpoSino = cuerpoSino;
    }

    public NodoAST getCondicion() { return condicion; }
    public List<NodoAST> getCuerpoSi() { return cuerpoSi; }
    public List<NodoSinoSi> getRamasSinoSi() { return ramasSinoSi; }
    public List<NodoAST> getCuerpoSino() { return cuerpoSino; }

    @Override
    public <T> T aceptar(VisitanteAST<T> visitante) {
        return visitante.visitar(this);
    }
}
