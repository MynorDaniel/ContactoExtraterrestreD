package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;
import java.util.List;

public class NodoPrograma extends NodoAST {

    private final List<NodoImportacion> importaciones;

    private final List<NodoDeclaracion> variablesGlobales;

    private final List<NodoEstructura> estructuras;

    private final List<NodoFuncion> funciones;

    private final NodoEstructura claseUnica;

    private final List<NodoAST> sentenciasPrincipales;

    public NodoPrograma(int linea, int columna,
                         List<NodoImportacion> importaciones,
                         List<NodoDeclaracion> variablesGlobales,
                         List<NodoEstructura> estructuras,
                         List<NodoFuncion> funciones,
                         NodoEstructura claseUnica,
                         List<NodoAST> sentenciasPrincipales) {
        super(linea, columna);
        this.importaciones = importaciones;
        this.variablesGlobales = variablesGlobales;
        this.estructuras = estructuras;
        this.funciones = funciones;
        this.claseUnica = claseUnica;
        this.sentenciasPrincipales = sentenciasPrincipales;
    }

    public List<NodoImportacion> getImportaciones() { return importaciones; }
    public List<NodoDeclaracion> getVariablesGlobales() { return variablesGlobales; }
    public List<NodoEstructura> getEstructuras() { return estructuras; }
    public List<NodoFuncion> getFunciones() { return funciones; }
    public NodoEstructura getClaseUnica() { return claseUnica; }
    public List<NodoAST> getSentenciasPrincipales() { return sentenciasPrincipales; }

    @Override
    public <T> T aceptar(VisitanteAST<T> visitante) {
        return visitante.visitar(this);
    }
}
