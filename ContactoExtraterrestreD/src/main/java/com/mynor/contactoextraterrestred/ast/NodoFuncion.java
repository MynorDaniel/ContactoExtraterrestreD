package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;
import java.util.List;

public class NodoFuncion extends NodoAST {

    private final String nombre;
    private final List<NodoParametro> parametros;
    private final NodoTipo tipoRetorno; 
    private final List<NodoAST> cuerpo;
    private final boolean esConstructor;

    public NodoFuncion(int linea, int columna, String nombre,
                        List<NodoParametro> parametros, NodoTipo tipoRetorno,
                        List<NodoAST> cuerpo, boolean esConstructor) {
        super(linea, columna);
        this.nombre = nombre;
        this.parametros = parametros;
        this.tipoRetorno = tipoRetorno;
        this.cuerpo = cuerpo;
        this.esConstructor = esConstructor;
    }

    public String getNombre() { return nombre; }
    public List<NodoParametro> getParametros() { return parametros; }
    public NodoTipo getTipoRetorno() { return tipoRetorno; }
    public List<NodoAST> getCuerpo() { return cuerpo; }
    public boolean isEsConstructor() { return esConstructor; }

    @Override
    public <T> T aceptar(VisitanteAST<T> visitante) {
        return visitante.visitar(this);
    }
}
