package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;
import java.util.List;

public class NodoEstructura extends NodoAST {

    private final String nombre;
    private final List<NodoAtributo> atributos;
    private final List<NodoFuncion> constructores;
    private final List<NodoFuncion> metodos;

    public NodoEstructura(int linea, int columna, String nombre,
            List<NodoAtributo> atributos,
            List<NodoFuncion> constructores,
            List<NodoFuncion> metodos) {
        super(linea, columna);
        this.nombre = nombre;
        this.atributos = atributos;
        this.constructores = constructores;
        this.metodos = metodos;
    }

    public String getNombre() {
        return nombre;
    }

    public List<NodoAtributo> getAtributos() {
        return atributos;
    }

    public List<NodoFuncion> getConstructores() {
        return constructores;
    }

    public List<NodoFuncion> getMetodos() {
        return metodos;
    }

    @Override
    public <T> T aceptar(VisitanteAST<T> visitante) {
        return visitante.visitar(this);
    }
}
