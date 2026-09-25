/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mynor.contactoextraterrestred.ast.visitantes.semantico;

import java.util.ArrayDeque;

/**
 *
 * @author mynordma
 */
public class Entorno {

    private final ArrayDeque<TablaSimbolos> ambitos = new ArrayDeque<>();

    public Entorno() {
        ambitos.push(new TablaSimbolos()); // ámbito global, siempre existe
    }

    public void entrarAmbito() {
        ambitos.push(new TablaSimbolos());
    }

    public void salirAmbito() {
        ambitos.pop();
    }

    public boolean declarar(Simbolo simbolo) {
        return ambitos.peek().declarar(simbolo);
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
}
