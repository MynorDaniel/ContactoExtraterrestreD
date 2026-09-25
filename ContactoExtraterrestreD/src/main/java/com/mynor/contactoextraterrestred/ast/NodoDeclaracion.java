package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;
import java.util.List;

public class NodoDeclaracion extends NodoAST {

    private final NodoTipo tipo;
    private final String nombre;
    private final List<NodoDimension> dimensiones;
    private final NodoAST inicializador; 

    public NodoDeclaracion(int linea, int columna, NodoTipo tipo, String nombre,
                            List<NodoDimension> dimensiones, NodoAST inicializador) {
        super(linea, columna);
        this.tipo = tipo;
        this.nombre = nombre;
        this.dimensiones = dimensiones;
        this.inicializador = inicializador;
    }

    public NodoTipo getTipo() { return tipo; }
    public String getNombre() { return nombre; }
    public List<NodoDimension> getDimensiones() { return dimensiones; }
    public NodoAST getInicializador() { return inicializador; }

    @Override
    public <T> T aceptar(VisitanteAST<T> visitante) {
        return visitante.visitar(this);
    }
}
