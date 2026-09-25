/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mynor.contactoextraterrestred.ast;

import java.util.ArrayList;
import java.util.List;

public class Programa {

    private final List<NodoAST> archivos;

    public Programa() {
        this.archivos = new ArrayList<>();
    }

    public void agregarArchivo(NodoAST archivo) {
        archivos.add(archivo);
    }

    public List<NodoAST> getArchivos() {
        return archivos;
    }
}
