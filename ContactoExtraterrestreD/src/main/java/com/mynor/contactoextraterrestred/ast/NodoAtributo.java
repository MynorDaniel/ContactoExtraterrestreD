package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;
import java.util.List;

public class NodoAtributo extends NodoAST {

    private final NodoTipo tipo;
    private final String nombre;
    private final List<NodoDimension> dimensiones;
    private final NodoAST valorInicial; // puede ser null

    public NodoAtributo(int linea, int columna, NodoTipo tipo, String nombre,
                         List<NodoDimension> dimensiones, NodoAST valorInicial) {
        super(linea, columna);
        this.tipo = tipo;
        this.nombre = nombre;
        this.dimensiones = dimensiones;
        this.valorInicial = valorInicial;
    }

    public NodoTipo getTipo() { return tipo; }
    public String getNombre() { return nombre; }
    public List<NodoDimension> getDimensiones() { return dimensiones; }
    public NodoAST getValorInicial() { return valorInicial; }

    @Override
    public <T> T aceptar(VisitanteAST<T> visitante) {
        return visitante.visitar(this);
    }
}
