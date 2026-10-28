/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mynor.contactoextraterrestred.ast.visitantes.semantico;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author mynordma
 */
public class TablaSimbolos {

    private final Map<String, Simbolo> simbolos = new HashMap<>();
    private final Map<String, List<Simbolo>> funciones = new HashMap<>();

    public boolean declarar(Simbolo simbolo) {
        if (simbolo.esFuncion) {
            funciones.computeIfAbsent(simbolo.nombre, k -> new ArrayList<>()).add(simbolo);
            return true;
        }
        if (simbolos.containsKey(simbolo.nombre)) {
            return false;
        }
        simbolos.put(simbolo.nombre, simbolo);
        return true;
    }

    public Simbolo obtener(String nombre) {
        return simbolos.get(nombre);
    }

    public List<Simbolo> obtenerFunciones(String nombre) {
        List<Simbolo> lista = funciones.get(nombre);
        return lista == null ? Collections.emptyList() : lista;
    }
}