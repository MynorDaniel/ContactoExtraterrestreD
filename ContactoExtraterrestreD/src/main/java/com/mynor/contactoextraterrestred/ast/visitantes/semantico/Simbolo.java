/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mynor.contactoextraterrestred.ast.visitantes.semantico;

import com.mynor.contactoextraterrestred.ast.NodoEstructura;
import com.mynor.contactoextraterrestred.ast.NodoTipo;

import java.util.List;

/**
 *
 * @author mynordma
 */
public class Simbolo {

    public final String nombre;
    public final NodoTipo tipo;
    public final List<NodoTipo> parametros;
    public final NodoEstructura estructura;
    public final boolean esFuncion;
    public final boolean esEstructura;

    private Simbolo(String nombre, NodoTipo tipo, List<NodoTipo> parametros,
            NodoEstructura estructura, boolean esFuncion, boolean esEstructura) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.parametros = parametros;
        this.estructura = estructura;
        this.esFuncion = esFuncion;
        this.esEstructura = esEstructura;
    }

    public static Simbolo variable(String nombre, NodoTipo tipo) {
        return new Simbolo(nombre, tipo, null, null, false, false);
    }

    public static Simbolo funcion(String nombre, NodoTipo tipoRetorno, List<NodoTipo> parametros) {
        return new Simbolo(nombre, tipoRetorno, parametros, null, true, false);
    }

    public static Simbolo estructura(String nombre, NodoEstructura definicion) {
        return new Simbolo(nombre, null, null, definicion, false, true);
    }
}
