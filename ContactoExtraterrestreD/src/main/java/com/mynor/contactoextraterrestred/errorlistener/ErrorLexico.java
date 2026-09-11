/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mynor.contactoextraterrestred.errorlistener;

/**
 *
 * @author mynordma
 */
public class ErrorLexico {

    private final int linea;
    private final int columna;
    private final String mensaje;

    public ErrorLexico(int linea, int columna, String mensaje) {
        this.linea = linea;
        this.columna = columna;
        this.mensaje = mensaje;
    }

    public int getLinea() {
        return linea;
    }

    public int getColumna() {
        return columna;
    }

    public String getMensaje() {
        return mensaje;
    }

    @Override
    public String toString() {
        return "Error léxico | Línea "
                + linea
                + ", Columna "
                + columna
                + " | "
                + mensaje;
    }
}
