package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;

public class NodoParametro extends NodoAST {

    private final NodoTipo tipo;
    private final String nombre;
    private final boolean aceptaCualquierArreglo;
    private final boolean aceptaCualquierEstructura;

    public NodoParametro(int linea, int columna, NodoTipo tipo, String nombre) {
        this(linea, columna, tipo, nombre, false, false);
    }

    public NodoParametro(int linea, int columna, NodoTipo tipo, String nombre,
                          boolean aceptaCualquierArreglo, boolean aceptaCualquierEstructura) {
        super(linea, columna);
        this.tipo = tipo;
        this.nombre = nombre;
        this.aceptaCualquierArreglo = aceptaCualquierArreglo;
        this.aceptaCualquierEstructura = aceptaCualquierEstructura;
    }

    public NodoTipo getTipo() { return tipo; }
    public String getNombre() { return nombre; }
    public boolean isAceptaCualquierArreglo() { return aceptaCualquierArreglo; }
    public boolean isAceptaCualquierEstructura() { return aceptaCualquierEstructura; }

    @Override
    public <T> T aceptar(VisitanteAST<T> visitante) {
        return visitante.visitar(this);
    }
}
