/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mynor.contactoextraterrestred.ast.visitantes.semantico;

import com.mynor.contactoextraterrestred.ast.NodoEstructura;
import com.mynor.contactoextraterrestred.ast.NodoFuncion;
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

    public final NodoEstructura propietario;
    public final String modificador;
    public final NodoFuncion declaracion;
    public final boolean inaccesible;

    private Simbolo(String nombre, NodoTipo tipo, List<NodoTipo> parametros,
            NodoEstructura estructura, boolean esFuncion, boolean esEstructura,
            NodoEstructura propietario, String modificador, NodoFuncion declaracion,
            boolean inaccesible) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.parametros = parametros;
        this.estructura = estructura;
        this.esFuncion = esFuncion;
        this.esEstructura = esEstructura;
        this.propietario = propietario;
        this.modificador = modificador;
        this.declaracion = declaracion;
        this.inaccesible = inaccesible;
    }

    public static Simbolo variable(String nombre, NodoTipo tipo) {
        return new Simbolo(nombre, tipo, null, null, false, false, null, null, null, false);
    }

    public static Simbolo variableHeredada(String nombre, NodoTipo tipo, NodoEstructura propietario,
            boolean inaccesible) {
        return new Simbolo(nombre, tipo, null, null, false, false, propietario, null, null, inaccesible);
    }

    public static Simbolo funcion(String nombre, NodoTipo tipoRetorno, List<NodoTipo> parametros) {
        return new Simbolo(nombre, tipoRetorno, parametros, null, true, false, null, null, null, false);
    }

    public static Simbolo metodo(String nombre, NodoTipo tipoRetorno, List<NodoTipo> parametros,
            NodoFuncion declaracion, NodoEstructura propietario) {
        return new Simbolo(nombre, tipoRetorno, parametros, null, true, false,
                propietario, declaracion.getModificador(), declaracion, false);
    }

    public static Simbolo estructura(String nombre, NodoEstructura definicion) {
        return new Simbolo(nombre, null, null, definicion, false, true, null, null, null, false);
    }
}
