package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;
import java.util.List;

public class NodoEstructura extends NodoAST {

    private final String nombre;
    private final List<NodoAtributo> atributos;
    private final List<NodoFuncion> constructores;
    private final List<NodoFuncion> metodos;
    private String modificador;
    private String clasePadre;

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

    public void setModificador(String modificador) {
        this.modificador = modificador;
    }

    public String getModificador() {
        return modificador;
    }

    public void setClasePadre(String clasePadre) {
        this.clasePadre = clasePadre;
    }

    public String getClasePadre() {
        return clasePadre;
    }

    @Override
    public <T> T aceptar(VisitanteAST<T> visitante) {
        return visitante.visitar(this);
    }
}
