/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mynor.contactoextraterrestred.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

public final class ImpresorAST {

    private ImpresorAST() {
    }

    public static String imprimirAST(Object objeto) {
        StringBuilder sb = new StringBuilder();
        imprimirAST(objeto, 0, new IdentityHashMap<>(), sb);
        return sb.toString();
    }

    private static void imprimirAST(Object objeto, int nivel, Map<Object, Boolean> visitados, StringBuilder sb) {

        String sangria = "  ".repeat(nivel);

        if (objeto == null) {
            sb.append(sangria).append("null").append(System.lineSeparator());
            return;
        }

        Class<?> clase = objeto.getClass();

        if (esValorSimple(clase)) {
            sb.append(sangria).append(formatearSimple(objeto)).append(System.lineSeparator());
            return;
        }

        if (clase.isArray()) {
            int longitud = Array.getLength(objeto);
            sb.append(sangria).append("Arreglo<").append(clase.getComponentType().getSimpleName()).append(">[").append(longitud).append("]").append(System.lineSeparator());
            for (int i = 0; i < longitud; i++) {
                sb.append(sangria).append("  [").append(i).append("]:").append(System.lineSeparator());
                imprimirAST(Array.get(objeto, i), nivel + 2, visitados, sb);
            }
            return;
        }

        if (objeto instanceof Collection<?> coleccion) {
            sb.append(sangria).append(clase.getSimpleName()).append(" (").append(coleccion.size()).append(" elementos)").append(System.lineSeparator());
            int i = 0;
            for (Object elemento : coleccion) {
                sb.append(sangria).append("  [").append(i++).append("]:").append(System.lineSeparator());
                imprimirAST(elemento, nivel + 2, visitados, sb);
            }
            return;
        }

        if (objeto instanceof Map<?, ?> mapa) {
            sb.append(sangria).append(clase.getSimpleName()).append(" {").append(mapa.size()).append(" entradas}").append(System.lineSeparator());
            for (Map.Entry<?, ?> entrada : mapa.entrySet()) {
                sb.append(sangria).append("  ").append(entrada.getKey()).append(" ->").append(System.lineSeparator());
                imprimirAST(entrada.getValue(), nivel + 2, visitados, sb);
            }
            return;
        }

        sb.append(sangria).append(clase.getSimpleName()).append(System.lineSeparator());

        if (visitados.containsKey(objeto)) {
            sb.append(sangria).append("  ... (referencia ya visitada, se omite para evitar ciclo)").append(System.lineSeparator());
            return;
        }
        visitados.put(objeto, Boolean.TRUE);

        for (Class<?> c = clase; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field campo : c.getDeclaredFields()) {

                if (Modifier.isStatic(campo.getModifiers()) || campo.isSynthetic()) {
                    continue;
                }

                campo.setAccessible(true);

                Object valor;
                try {
                    valor = campo.get(objeto);
                } catch (IllegalAccessException e) {
                    sb.append(sangria).append("  ").append(campo.getName()).append(": <no accesible>").append(System.lineSeparator());
                    continue;
                }

                sb.append(sangria).append("  ").append(campo.getName())
                        .append(" (").append(campo.getType().getSimpleName()).append("):").append(System.lineSeparator());
                imprimirAST(valor, nivel + 2, visitados, sb);
            }
        }
    }

    private static boolean esValorSimple(Class<?> clase) {
        return clase.isPrimitive()
                || Number.class.isAssignableFrom(clase)
                || clase == String.class
                || clase == Boolean.class
                || clase == Character.class
                || clase.isEnum();
    }

    private static String formatearSimple(Object valor) {
        if (valor == null) {
            return "null";
        }
        if (valor instanceof String s) {
            return "\"" + s + "\"";
        }
        return String.valueOf(valor);
    }
}