/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mynor.contactoextraterrestred.ast.visitantes.semantico;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author mynordma
 */
public class Entorno {

    private final ArrayDeque<TablaSimbolos> ambitos = new ArrayDeque<>();
    private final List<Simbolo> simbolosDeclarados = new ArrayList<>();

    public Entorno() {
        ambitos.push(new TablaSimbolos()); 
    }

    public void entrarAmbito() {
        ambitos.push(new TablaSimbolos());
    }

    public void salirAmbito() {
        ambitos.pop();
    }

    public boolean declarar(Simbolo simbolo) {
        boolean declarado = ambitos.peek().declarar(simbolo);
        if (declarado) {
            simbolosDeclarados.add(simbolo);
        }
        return declarado;
    }

    public Simbolo buscar(String nombre) {
        for (TablaSimbolos ambito : ambitos) {
            Simbolo simbolo = ambito.obtener(nombre);
            if (simbolo != null) {
                return simbolo;
            }
        }
        return null;
    }

    public List<Simbolo> buscarFunciones(String nombre) {
        for (TablaSimbolos ambito : ambitos) {
            List<Simbolo> lista = ambito.obtenerFunciones(nombre);
            if (!lista.isEmpty()) {
                return lista;
            }
        }
        return Collections.emptyList();
    }

    public List<Simbolo> funcionesEnAmbitoActual(String nombre) {
        return ambitos.peek().obtenerFunciones(nombre);
    }

    public List<Simbolo> getSimbolosDeclarados() {
        return Collections.unmodifiableList(simbolosDeclarados);
    }
}
