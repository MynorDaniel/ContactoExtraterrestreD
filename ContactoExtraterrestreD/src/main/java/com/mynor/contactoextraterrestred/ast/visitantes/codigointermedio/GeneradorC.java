/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mynor.contactoextraterrestred.ast.visitantes.codigointermedio;

import com.mynor.contactoextraterrestred.ast.NodoFuncion;
import com.mynor.contactoextraterrestred.ast.NodoTipo;
import com.mynor.contactoextraterrestred.ast.visitantes.codigointermedio.GeneradorCuartetas.Cuarteto;
import com.mynor.contactoextraterrestred.ast.visitantes.codigointermedio.GeneradorCuartetas.InfoNombre;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author mynordma
 */
public class GeneradorC {

    private Map<String, InfoNombre> tablaNombres;
    private Map<String, NodoFuncion> funcionesLibres;
    private Set<String> buffersTexto;

    public String generar(GeneradorCuartetas generador) {
        tablaNombres = generador.getTablaNombres();
        funcionesLibres = generador.getFuncionesLibres();
        List<Cuarteto> codigo = generador.getCodigo();
        buffersTexto = calcularBuffersTexto(codigo);
        Set<String> nombresFunciones = new HashSet<>(generador.getNombresDeFunciones());

        String etiquetaFinPrograma = null;
        if (!nombresFunciones.isEmpty() && !codigo.isEmpty()) {
            Cuarteto ultimo = codigo.get(codigo.size() - 1);
            if ("etq".equals(ultimo.operador())) {
                etiquetaFinPrograma = ultimo.destino();
            }
        }

        List<Cuarteto> principal = new ArrayList<>();
        Map<String, List<Cuarteto>> porFuncion = new LinkedHashMap<>();
        String funcionActual = null;
        for (Cuarteto c : codigo) {
            if ("etq".equals(c.operador()) && nombresFunciones.contains(c.destino())) {
                funcionActual = c.destino();
                porFuncion.put(funcionActual, new ArrayList<>());
                continue;
            }
            if ("etq".equals(c.operador()) && c.destino().equals(etiquetaFinPrograma)) {
                continue;
            }
            if (funcionActual == null) {
                if ("goto".equals(c.operador()) && c.destino() != null && c.destino().equals(etiquetaFinPrograma)) {
                    continue;
                }
                principal.add(c);
            } else {
                porFuncion.get(funcionActual).add(c);
            }
        }

        StringBuilder salida = new StringBuilder();
        salida.append(preambulo()).append("\n");

        for (String nombreFuncion : generador.getNombresDeFunciones()) {
            salida.append(firma(nombreFuncion, porFuncion.get(nombreFuncion))).append(";\n");
        }
        salida.append("\n");

        salida.append("int main(void) {\n");
        Set<String> localesPrincipal = nombresUsados(principal);
        salida.append(declaraciones(localesPrincipal, "    "));
        generarLineasCuerpo(principal, true, salida);
        salida.append("    return 0;\n");
        salida.append("}\n\n");

        for (String nombreFuncion : generador.getNombresDeFunciones()) {
            salida.append(definicionFuncion(nombreFuncion, porFuncion.get(nombreFuncion)));
            salida.append("\n");
        }

        return salida.toString();
    }

    private List<String> parametrosDe(List<Cuarteto> segmentoFuncion) {
        List<String> nombres = new ArrayList<>();
        for (Cuarteto c : segmentoFuncion) {
            if (!"recibir".equals(c.operador())) {
                break;
            }
            nombres.add(c.destino());
        }
        return nombres;
    }

    private boolean esVoidFuncion(String nombreFuncion) {
        NodoFuncion funcion = funcionesLibres.get(nombreFuncion);
        return funcion == null || funcion.getTipoRetorno() == null || "void".equals(funcion.getTipoRetorno().getNombre());
    }

    private String firma(String nombreFuncion, List<Cuarteto> segmento) {
        List<String> parametros = parametrosDe(segmento);
        String tipoRetorno = esVoidFuncion(nombreFuncion)
                ? "void"
                : tipoPrimitivoC(funcionesLibres.get(nombreFuncion).getTipoRetorno().getNombre());
        StringBuilder texto = new StringBuilder(tipoRetorno).append(" ").append(nombreFuncion).append("(");
        if (parametros.isEmpty()) {
            texto.append("void");
        } else {
            for (int i = 0; i < parametros.size(); i++) {
                if (i > 0) {
                    texto.append(", ");
                }
                texto.append(tipoParaParametro(tablaNombres.get(parametros.get(i)))).append(" ").append(parametros.get(i));
            }
        }
        return texto.append(")").toString();
    }

    private String definicionFuncion(String nombreFuncion, List<Cuarteto> segmento) {
        List<String> parametros = parametrosDe(segmento);
        List<Cuarteto> cuerpo = segmento.subList(parametros.size(), segmento.size());

        StringBuilder texto = new StringBuilder();
        texto.append(firma(nombreFuncion, segmento)).append(" {\n");
        Set<String> locales = nombresUsados(cuerpo);
        locales.removeAll(parametros);
        texto.append(declaraciones(locales, "    "));
        generarLineasCuerpo(cuerpo, esVoidFuncion(nombreFuncion), texto);
        texto.append("}\n");
        return texto.toString();
    }

    private Set<String> nombresUsados(List<Cuarteto> segmento) {
        Set<String> nombres = new LinkedHashSet<>();
        for (Cuarteto c : segmento) {
            if ("recibir".equals(c.operador())) {
                continue;
            }
            agregarSiEsNombre(nombres, c.operando1());
            agregarSiEsNombre(nombres, c.operando2());
            agregarSiEsNombre(nombres, c.destino());
        }
        return nombres;
    }

    private void agregarSiEsNombre(Set<String> nombres, String candidato) {
        if (candidato != null && tablaNombres.containsKey(candidato)) {
            nombres.add(candidato);
        }
    }

    private void generarLineasCuerpo(List<Cuarteto> cuerpo, boolean esVoid, StringBuilder salida) {
        List<String> parametrosPendientes = new ArrayList<>();
        for (Cuarteto c : cuerpo) {
            switch (c.operador()) {
                case "recibir" -> {
                }
                case "param" -> parametrosPendientes.add(valorFormateado(c.operando1()));
                case "call" -> {
                    String nombreFuncion = c.operando1();
                    String llamada = nombreFuncion + "(" + String.join(", ", parametrosPendientes) + ")";
                    parametrosPendientes = new ArrayList<>();
                    if (esVoidFuncion(nombreFuncion)) {
                        salida.append("    ").append(llamada).append(";\n");
                    } else {
                        salida.append("    ").append(c.destino()).append(" = ").append(llamada).append(";\n");
                    }
                }
                default -> {
                    String linea = traducir(c, esVoid);
                    if (linea != null) {
                        salida.append("    ").append(linea).append("\n");
                    }
                }
            }
        }
    }

    private String traducir(Cuarteto c, boolean esVoid) {
        return switch (c.operador()) {
            case "etq" -> c.destino() + ":;";
            case "goto" -> "goto " + c.destino() + ";";
            case "if" -> "if (" + valorFormateado(c.operando1()) + ") goto " + c.destino() + ";";
            case "ifFalse" -> "if (!(" + valorFormateado(c.operando1()) + ")) goto " + c.destino() + ";";
            case "if<" -> condicionRelacional("<", c);
            case "if>" -> condicionRelacional(">", c);
            case "if<=" -> condicionRelacional("<=", c);
            case "if>=" -> condicionRelacional(">=", c);
            case "if==" -> condicionRelacional("==", c);
            case "if!=" -> condicionRelacional("!=", c);
            case "=" -> traducirAsignacionSimple(c);
            case "+", "-", "*", "/", "%" -> traducirAritmetica(c);
            case "concat" -> c.destino() + " = rt_concat(" + comoTexto(c.operando1()) + ", " + comoTexto(c.operando2()) + ");";
            case "return" -> traducirReturn(c, esVoid);
            case "print" -> traducirPrint(c);
            case "SCAN_INT" -> "scanf(\"%d\", &" + c.destino() + ");";
            case "SCAN_FLOAT" -> "scanf(\"%lf\", &" + c.destino() + ");";
            case "SCAN_CHAR" -> "scanf(\" %c\", &" + c.destino() + ");";
            case "SCAN_TEXT" -> "scanf(\"%255s\", " + c.destino() + ");";
            case "[]=" -> traducirAlmacenArreglo(c);
            case "=[]" -> traducirCargaArreglo(c);
            case "reservar" -> traducirReserva(c);
            default -> "// operador no reconocido: " + c;
        };
    }

    private String condicionRelacional(String simbolo, Cuarteto c) {
        return "if (" + valorFormateado(c.operando1()) + " " + simbolo + " " + valorFormateado(c.operando2()) + ") goto " + c.destino() + ";";
    }

    private String traducirAsignacionSimple(Cuarteto c) {
        if (buffersTexto.contains(c.destino())) {
            return "strcpy(" + c.destino() + ", " + valorFormateado(c.operando1()) + ");";
        }
        return c.destino() + " = " + valorFormateado(c.operando1()) + ";";
    }

    private String traducirAritmetica(Cuarteto c) {
        if (c.operando2() == null) {
            return c.destino() + " = " + c.operador() + valorFormateado(c.operando1()) + ";";
        }
        return c.destino() + " = " + valorFormateado(c.operando1()) + " " + c.operador() + " " + valorFormateado(c.operando2()) + ";";
    }

    private String traducirReturn(Cuarteto c, boolean esVoid) {
        if (esVoid) {
            return "return;";
        }
        return "return " + (c.operando1() != null ? valorFormateado(c.operando1()) : "0") + ";";
    }

    private String traducirPrint(Cuarteto c) {
        String formato = switch (tipoDelValor(c.operando1())) {
            case "DECIMAL" -> "%f";
            case "CARACTER" -> "%c";
            case "CADENA" -> "%s";
            case "REFERENCIA" -> "%p";
            default -> "%d";
        };
        return "printf(\"" + formato + "\", " + valorFormateado(c.operando1()) + ");";
    }

    private String traducirAlmacenArreglo(Cuarteto c) {
        String campo = campoSegunTipo(tipoDelValor(c.operando2()));
        return c.destino() + "[" + valorFormateado(c.operando1()) + "]." + campo + " = " + valorFormateado(c.operando2()) + ";";
    }

    private String traducirCargaArreglo(Cuarteto c) {
        String campo = campoSegunTipo(tipoDelValor(c.destino()));
        return c.destino() + " = " + valorFormateado(c.operando1()) + "[" + valorFormateado(c.operando2()) + "]." + campo + ";";
    }

    private String traducirReserva(Cuarteto c) {
        return c.destino() + " = calloc(" + valorFormateado(c.operando1()) + ", sizeof(Celda));";
    }

    private String valorFormateado(String texto) {
        return "null".equals(texto) ? "NULL" : texto;
    }

    private String comoTexto(String operando) {
        return switch (tipoDelValor(operando)) {
            case "ENTERO" -> "rt_entero_a_texto(" + operando + ")";
            case "DECIMAL" -> "rt_decimal_a_texto(" + operando + ")";
            case "CARACTER" -> "rt_caracter_a_texto(" + operando + ")";
            default -> valorFormateado(operando);
        };
    }

    private String tipoDelValor(String texto) {
        if (texto == null) {
            return "ENTERO";
        }
        InfoNombre info = tablaNombres.get(texto);
        if (info != null) {
            if (esCompuesto(info.tipo())) {
                return "REFERENCIA";
            }
            return tipoSegunNombre(info.tipo().getNombre());
        }
        if ("null".equals(texto) || "NULL".equals(texto)) {
            return "REFERENCIA";
        }
        if (texto.startsWith("\"")) {
            return "CADENA";
        }
        if (texto.startsWith("'")) {
            return "CARACTER";
        }
        if (texto.matches("-?\\d+\\.\\d+")) {
            return "DECIMAL";
        }
        if (texto.matches("-?\\d+")) {
            return "ENTERO";
        }
        return "CADENA";
    }

    private String tipoSegunNombre(String nombre) {
        if (esCategoriaEntero(nombre)) {
            return "ENTERO";
        }
        if (esCategoriaDecimal(nombre)) {
            return "DECIMAL";
        }
        if (esCategoriaCaracter(nombre)) {
            return "CARACTER";
        }
        if (esCategoriaCadena(nombre)) {
            return "CADENA";
        }
        return "ENTERO";
    }

    private String campoSegunTipo(String tipo) {
        return switch (tipo) {
            case "DECIMAL" -> "decimal";
            case "CARACTER" -> "caracter";
            case "CADENA" -> "texto";
            case "REFERENCIA" -> "ref";
            default -> "entero";
        };
    }

    private boolean esCategoriaEntero(String nombre) {
        return "entero".equals(nombre) || "int".equals(nombre) || "numerus".equals(nombre);
    }

    private boolean esCategoriaDecimal(String nombre) {
        return "flotante".equals(nombre) || "double".equals(nombre) || "decimalis".equals(nombre);
    }

    private boolean esCategoriaCaracter(String nombre) {
        return "caracter".equals(nombre) || "char".equals(nombre) || "littera".equals(nombre);
    }

    private boolean esCategoriaCadena(String nombre) {
        return "cadena".equals(nombre) || "String".equals(nombre) || "textum".equals(nombre);
    }

    private boolean esNombreDeClase(String nombreTipo) {
        return !esCategoriaEntero(nombreTipo) && !esCategoriaDecimal(nombreTipo)
                && !esCategoriaCaracter(nombreTipo) && !esCategoriaCadena(nombreTipo)
                && !"bool".equals(nombreTipo) && !"boolean".equals(nombreTipo) && !"void".equals(nombreTipo);
    }

    private boolean esCompuesto(NodoTipo tipo) {
        return tipo.getNivelesArreglo() > 0 || esNombreDeClase(tipo.getNombre());
    }

    private String tipoPrimitivoC(String nombreTipo) {
        if (esCategoriaEntero(nombreTipo)) {
            return "int";
        }
        if (esCategoriaDecimal(nombreTipo)) {
            return "double";
        }
        if (esCategoriaCaracter(nombreTipo)) {
            return "char";
        }
        if (esCategoriaCadena(nombreTipo)) {
            return "const char*";
        }
        return "int";
    }

    private String tipoParaParametro(InfoNombre info) {
        if (info == null) {
            return "int";
        }
        return esCompuesto(info.tipo()) ? "Celda*" : tipoPrimitivoC(info.tipo().getNombre());
    }

    private Set<String> calcularBuffersTexto(List<Cuarteto> codigo) {
        Set<String> buffers = new HashSet<>();
        for (Cuarteto c : codigo) {
            if ("SCAN_TEXT".equals(c.operador())) {
                buffers.add(c.destino());
            }
        }
        return buffers;
    }

    private String declaraciones(Set<String> nombres, String sangria) {
        StringBuilder texto = new StringBuilder();
        for (String nombre : nombres) {
            texto.append(sangria).append(declaracionDeVariable(nombre)).append("\n");
        }
        return texto.toString();
    }

    private String declaracionDeVariable(String nombre) {
        if (buffersTexto.contains(nombre)) {
            return "char " + nombre + "[256];";
        }
        InfoNombre info = tablaNombres.get(nombre);
        if (info == null) {
            return "int " + nombre + ";";
        }
        if (esCompuesto(info.tipo())) {
            return "Celda* " + nombre + ";";
        }
        return tipoPrimitivoC(info.tipo().getNombre()) + " " + nombre + ";";
    }

    private String preambulo() {
        return """
                #include <stdio.h>
                #include <stdlib.h>
                #include <string.h>

                typedef union Celda {
                    int entero;
                    double decimal;
                    char caracter;
                    const char* texto;
                    union Celda* ref;
                } Celda;

                static char* rt_concat(const char* a, const char* b) {
                    char* resultado = malloc(strlen(a) + strlen(b) + 1);
                    strcpy(resultado, a);
                    strcat(resultado, b);
                    return resultado;
                }

                static char* rt_entero_a_texto(int v) {
                    char* buffer = malloc(32);
                    snprintf(buffer, 32, "%d", v);
                    return buffer;
                }

                static char* rt_decimal_a_texto(double v) {
                    char* buffer = malloc(64);
                    snprintf(buffer, 64, "%g", v);
                    return buffer;
                }

                static char* rt_caracter_a_texto(char v) {
                    char* buffer = malloc(2);
                    buffer[0] = v;
                    buffer[1] = '\\0';
                    return buffer;
                }
                """;
    }
}