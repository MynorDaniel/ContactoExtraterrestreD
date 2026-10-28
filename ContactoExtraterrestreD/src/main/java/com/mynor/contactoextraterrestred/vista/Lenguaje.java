/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.mynor.contactoextraterrestred.vista;

/**
 *
 * @author mynordma
 */
public enum Lenguaje {
    Y, Z, PIG, C;

    public static Lenguaje desdeNombreArchivo(String nombreArchivo) {
        String nombre = nombreArchivo.toLowerCase();

        if (nombre.endsWith(".y")) {
            return Y;
        }
        if (nombre.endsWith(".z")) {
            return Z;
        }
        if (nombre.endsWith(".pig")) {
            return PIG;
        }
        if (nombre.endsWith(".c")) {
            return C;
        }

        throw new IllegalArgumentException("Extensión de archivo no compatible: " + nombreArchivo);
    }
}
