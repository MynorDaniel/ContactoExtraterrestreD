/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mynor.contactoextraterrestred.ast.visitantes.codigointermedio;

import com.mynor.contactoextraterrestred.ast.*;
import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;
import com.mynor.contactoextraterrestred.ast.visitantes.semantico.Entorno;
import com.mynor.contactoextraterrestred.ast.visitantes.semantico.Simbolo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author mynordma
 */
public class GeneradorCuartetas implements VisitanteAST<String> {

    public record Cuarteto(String operador, String operando1, String operando2, String destino) {

        @Override
        public String toString() {
            return "(" + operador + ", " + n(operando1) + ", " + n(operando2) + ", " + n(destino) + ")";
        }

        private static String n(String s) {
            return s == null ? "_" : s;
        }
    }

    public record InfoNombre(NodoTipo tipo, int cantidadDeCeldas) {

    }

    private record MapaDeClase(Map<String, Integer> posicionesDeCampos, int tamanoEnCeldas) {

    }

    private record Direccion(String variable, String posicion, boolean puntero) {

        Direccion(String variable, String posicion) {
            this(variable, posicion, false);
        }

        boolean esVariableCompleta() {
            return posicion == null;
        }
    }

    private record MetodoImpl(NodoEstructura duena, NodoFuncion funcion) {

    }

    private record LlamadaResuelta(NodoEstructura claseEstatica, NodoEstructura duena, NodoFuncion funcion,
            boolean receptorImplicito, boolean viaSuper) {

    }

    private static final Set<String> RELACIONALES = Set.of("<", ">", "<=", ">=", "==", "!=", "=");
    private static final int TAMANO_ARREGLO_DESCONOCIDO = 256;

    private static final NodoTipo TIPO_ENTERO = new NodoTipo(0, 0, "entero", 0);
    private static final NodoTipo TIPO_DECIMAL = new NodoTipo(0, 0, "flotante", 0);
    private static final NodoTipo TIPO_CADENA = new NodoTipo(0, 0, "cadena", 0);
    private static final NodoTipo TIPO_CARACTER = new NodoTipo(0, 0, "caracter", 0);
    private static final NodoTipo TIPO_BOOLEANO = new NodoTipo(0, 0, "bool", 0);
    private static final NodoTipo TIPO_NULO = new NodoTipo(0, 0, "null", 0);

    private final List<Cuarteto> codigo = new ArrayList<>();
    private int contadorTemp = 0;
    private int contadorEtiqueta = 0;

    private final Map<String, NodoEstructura> definiciones = new HashMap<>();
    private final List<NodoEstructura> clasesZ = new ArrayList<>();
    private final Map<String, Integer> idsDeClase = new HashMap<>();
    private final Map<String, MapaDeClase> mapasDeClase = new HashMap<>();
    private final Map<String, List<String>> dimensionesArreglo = new HashMap<>();
    private final Map<String, NodoFuncion> funcionesLibres = new HashMap<>();
    private final Map<String, List<NodoFuncion>> funcionesPorNombre = new HashMap<>();
    private final List<String> nombresDeFunciones = new ArrayList<>();
    private final Map<String, InfoNombre> tablaNombres = new HashMap<>();
    private final Entorno tipos = new Entorno();
    private final Deque<String> etiquetasSalidaBucle = new ArrayDeque<>();
    private final Deque<String> etiquetasContinuarBucle = new ArrayDeque<>();
    private final Deque<NodoTipo> tipoDestinoActual = new ArrayDeque<>();

    private NodoEstructura claseActual = null;
    private Set<String> nombresLocales = null; 

    public List<Cuarteto> getCodigo() {
        return codigo;
    }

    public Map<String, InfoNombre> getTablaNombres() {
        return tablaNombres;
    }

    public Map<String, NodoFuncion> getFuncionesLibres() {
        return funcionesLibres;
    }

    public List<String> getNombresDeFunciones() {
        return nombresDeFunciones;
    }

    public String imprimirCodigo() {
        StringBuilder texto = new StringBuilder();
        for (int i = 0; i < codigo.size(); i++) {
            texto.append(String.format("%3d: %s%n", i, codigo.get(i)));
        }
        return texto.toString();
    }

    private String nuevoTemp() {
        return "t" + (++contadorTemp);
    }

    private String nuevoTemp(NodoTipo tipo) {
        String temp = nuevoTemp();
        registrarTipo(temp, tipo, 1);
        return temp;
    }

    private void registrarTipo(String nombre, NodoTipo tipo, int cantidadDeCeldas) {
        tablaNombres.put(nombre, new InfoNombre(tipo, cantidadDeCeldas));
    }

    private String nuevaEtiqueta() {
        return "et" + (++contadorEtiqueta);
    }

    private void emitir(String operador, String operando1, String operando2, String destino) {
        codigo.add(new Cuarteto(operador, operando1, operando2, destino));
    }

    private void etiqueta(String nombre) {
        emitir("etq", null, null, nombre);
    }

    private void emitirReserva(String destino, int cantidadDeCeldas) {
        emitir("reservar", String.valueOf(Math.max(cantidadDeCeldas, 1)), null, destino);
    }

    private void inicializarArregloEnCero(String arreglo, int cantidadDeCeldas) {
        String indice = nuevoTemp(TIPO_ENTERO);
        String inicio = nuevaEtiqueta();
        String cuerpo = nuevaEtiqueta();
        String fin = nuevaEtiqueta();
        emitir("=", "0", null, indice);
        etiqueta(inicio);
        emitir("if<", indice, String.valueOf(cantidadDeCeldas), cuerpo);
        emitir("goto", null, null, fin);
        etiqueta(cuerpo);
        emitir("[]=", indice, "0", arreglo);
        String siguiente = sumar(indice, "1");
        emitir("=", siguiente, null, indice);
        emitir("goto", null, null, inicio);
        etiqueta(fin);
    }

    private String generarExpresion(NodoAST nodo) {
        return nodo.aceptar(this);
    }

    private boolean esLiteralEntero(String texto) {
        try {
            Integer.valueOf(texto);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String sumar(String a, String b) {
        if (esLiteralEntero(a) && esLiteralEntero(b)) {
            return String.valueOf(Integer.parseInt(a) + Integer.parseInt(b));
        }
        String temp = nuevoTemp(TIPO_ENTERO);
        emitir("+", a, b, temp);
        return temp;
    }

    private String multiplicar(String a, String b) {
        if (esLiteralEntero(a) && esLiteralEntero(b)) {
            return String.valueOf(Integer.parseInt(a) * Integer.parseInt(b));
        }
        String temp = nuevoTemp(TIPO_ENTERO);
        emitir("*", a, b, temp);
        return temp;
    }

    private String resolverTamanoArreglo(String tamano) {
        if (nombresLocales != null && !nombresLocales.contains(tamano)
                && claseActual != null && buscarAtributo(claseActual, tamano) != null) {
            return generarExpresion(new NodoIdentificador(0, 0, tamano));
        }
        return tamano;
    }

    private void registrarDimensiones(String nombre, List<String> tamanos) {
        if (!tamanos.isEmpty()) {
            dimensionesArreglo.put(nombre, tamanos);
        }
    }

    private String claveDireccion(Direccion direccion) {
        return direccion.esVariableCompleta() ? direccion.variable()
                : direccion.variable() + "@" + direccion.posicion();
    }

    private NodoTipo tipoEfectivo(NodoTipo tipo, List<NodoDimension> dimensiones) {
        if (dimensiones.isEmpty()) {
            return tipo;
        }
        return new NodoTipo(tipo.getLinea(), tipo.getColumna(), tipo.getNombre(),
                tipo.getNivelesArreglo() + dimensiones.size());
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

    private boolean esCategoriaBool(String nombre) {
        return "bool".equals(nombre) || "boolean".equals(nombre);
    }

    private boolean esVoid(NodoTipo tipo) {
        return tipo == null || "void".equals(tipo.getNombre());
    }

    private boolean esNulo(NodoTipo tipo) {
        return tipo != null && tipo.getNivelesArreglo() == 0 && "null".equals(tipo.getNombre());
    }

    private boolean esClase(NodoEstructura definicion) {
        return definicion != null && definicion.getModificador() != null;
    }

    private boolean esClaseObjeto(String nombre) {
        return esClase(definiciones.get(nombre));
    }

    private boolean esReferenciaObjeto(NodoTipo tipo) {
        return tipo != null && tipo.getNivelesArreglo() == 0 && esClaseObjeto(tipo.getNombre());
    }

    private boolean esReferenciaTipo(NodoTipo tipo) {
        if (tipo == null || esNulo(tipo)) {
            return false;
        }
        return tipo.getNivelesArreglo() > 0 || esCategoriaCadena(tipo.getNombre()) || esClaseObjeto(tipo.getNombre());
    }

    private boolean esCompuesto(NodoTipo tipo) {
        return tipo != null && (tipo.getNivelesArreglo() > 0
                || (esNombreDeClase(tipo.getNombre()) && !esClaseObjeto(tipo.getNombre())));
    }

    private boolean esNombreDeClase(String nombreTipo) {
        return !esCategoriaEntero(nombreTipo) && !esCategoriaDecimal(nombreTipo)
                && !esCategoriaCaracter(nombreTipo) && !esCategoriaCadena(nombreTipo)
                && !esCategoriaBool(nombreTipo) && !"void".equals(nombreTipo);
    }

    private NodoEstructura clasePadre(NodoEstructura clase) {
        return clase == null || clase.getClasePadre() == null ? null : definiciones.get(clase.getClasePadre());
    }

    private List<NodoEstructura> ascendencia(NodoEstructura clase) {
        List<NodoEstructura> lista = new ArrayList<>();
        Set<String> vistos = new HashSet<>();
        NodoEstructura actual = clase;
        while (actual != null && vistos.add(actual.getNombre())) {
            lista.add(actual);
            actual = clasePadre(actual);
        }
        return lista;
    }

    private boolean esSubtipo(String sub, String sup) {
        if (sub.equals(sup)) {
            return true;
        }
        NodoEstructura clase = definiciones.get(sub);
        if (clase == null) {
            return false;
        }
        for (NodoEstructura ancestro : ascendencia(clase)) {
            if (ancestro.getNombre().equals(sup)) {
                return true;
            }
        }
        return false;
    }

    private List<NodoEstructura> subtipos(NodoEstructura base) {
        List<NodoEstructura> resultado = new ArrayList<>();
        for (NodoEstructura clase : clasesZ) {
            for (NodoEstructura ancestro : ascendencia(clase)) {
                if (ancestro.getNombre().equals(base.getNombre())) {
                    resultado.add(clase);
                    break;
                }
            }
        }
        return resultado;
    }

    private NodoAtributo buscarAtributo(NodoEstructura clase, String nombre) {
        for (NodoEstructura actual : ascendencia(clase)) {
            for (NodoAtributo atributo : actual.getAtributos()) {
                if (atributo.getNombre().equals(nombre)) {
                    return atributo;
                }
            }
        }
        return null;
    }

    private int celdasPorElemento(String nombreTipoBase) {
        if (esClaseObjeto(nombreTipoBase)) {
            return 1;
        }
        MapaDeClase mapa = obtenerMapaDeClase(nombreTipoBase);
        return mapa != null ? mapa.tamanoEnCeldas() : 1;
    }

    private int calcularTamanoTotal(List<String> tamanos, int celdasPorElemento) {
        int total = celdasPorElemento;
        for (String tamano : tamanos) {
            if (!esLiteralEntero(tamano)) {
                return TAMANO_ARREGLO_DESCONOCIDO;
            }
            total *= Integer.parseInt(tamano);
        }
        return total;
    }

    private int tamanoParaDeclarar(NodoTipo tipo, List<NodoDimension> dimensionesDeclaradas) {
        if (tipo.getNivelesArreglo() == 0) {
            if (esClaseObjeto(tipo.getNombre())) {
                return 1;
            }
            MapaDeClase mapa = obtenerMapaDeClase(tipo.getNombre());
            return mapa != null ? mapa.tamanoEnCeldas() : 1;
        }
        if (!dimensionesDeclaradas.isEmpty()) {
            List<String> tamanos = new ArrayList<>();
            for (NodoDimension dimension : dimensionesDeclaradas) {
                tamanos.add(generarExpresion(dimension.getTamano()));
            }
            return calcularTamanoTotal(tamanos, celdasPorElemento(tipo.getNombre()));
        }
        return TAMANO_ARREGLO_DESCONOCIDO;
    }

    private MapaDeClase obtenerMapaDeClase(String nombreTipo) {
        if (mapasDeClase.containsKey(nombreTipo)) {
            return mapasDeClase.get(nombreTipo);
        }
        if (!definiciones.containsKey(nombreTipo)) {
            return null;
        }
        return calcularMapaDeClase(nombreTipo, new HashSet<>());
    }

    private MapaDeClase calcularMapaDeClase(String nombreTipo, Set<String> enResolucion) {
        NodoEstructura definicion = definiciones.get(nombreTipo);
        Map<String, Integer> posiciones = new LinkedHashMap<>();
        int acumulado = 0;
        enResolucion.add(nombreTipo);

        if (esClase(definicion)) {
            acumulado = 1;
            NodoEstructura padre = clasePadre(definicion);
            if (padre != null && esClase(padre) && !enResolucion.contains(padre.getNombre())) {
                MapaDeClase mapaPadre = mapasDeClase.containsKey(padre.getNombre())
                        ? mapasDeClase.get(padre.getNombre())
                        : calcularMapaDeClase(padre.getNombre(), enResolucion);
                posiciones.putAll(mapaPadre.posicionesDeCampos());
                acumulado = mapaPadre.tamanoEnCeldas();
            }
        }

        for (NodoAtributo atributo : definicion.getAtributos()) {
            posiciones.put(atributo.getNombre(), acumulado);
            acumulado += celdasDelCampo(atributo, enResolucion);
        }
        enResolucion.remove(nombreTipo);
        MapaDeClase mapa = new MapaDeClase(posiciones, acumulado);
        mapasDeClase.put(nombreTipo, mapa);
        return mapa;
    }

    private int celdasDelCampo(NodoAtributo atributo, Set<String> enResolucion) {
        NodoTipo tipo = atributo.getTipo();
        if (!atributo.getDimensiones().isEmpty()) {
            int total = 1;
            for (NodoDimension dimension : atributo.getDimensiones()) {
                String tamano = generarExpresion(dimension.getTamano());
                total *= esLiteralEntero(tamano) ? Integer.parseInt(tamano) : 1;
            }
            return total;
        }
        if (tipo.getNivelesArreglo() > 0) {
            return 1;
        }
        if (esClaseObjeto(tipo.getNombre())) {
            return 1;
        }
        if (definiciones.containsKey(tipo.getNombre())) {
            if (enResolucion.contains(tipo.getNombre())) {
                return 1;
            }
            MapaDeClase anidado = mapasDeClase.containsKey(tipo.getNombre())
                    ? mapasDeClase.get(tipo.getNombre())
                    : calcularMapaDeClase(tipo.getNombre(), enResolucion);
            return anidado.tamanoEnCeldas();
        }
        return 1;
    }

    private boolean esSuper(NodoAST nodo) {
        return nodo instanceof NodoIdentificador id && "super".equals(id.getNombre());
    }

    private NodoTipo tipoDeClase(NodoEstructura clase) {
        return clase == null ? null : new NodoTipo(0, 0, clase.getNombre(), 0);
    }

    private NodoTipo tipoDeBase(NodoAST base) {
        if (esSuper(base)) {
            return tipoDeClase(clasePadre(claseActual));
        }
        return tipoDeExpresionSimple(base);
    }

    private NodoTipo tipoDeExpresionSimple(NodoAST expr) {
        if (expr instanceof NodoIdentificador id) {
            if ("this".equals(id.getNombre())) {
                return tipoDeClase(claseActual);
            }
            boolean local = nombresLocales != null && nombresLocales.contains(id.getNombre());
            if (!local && claseActual != null) {
                NodoAtributo atributo = buscarAtributo(claseActual, id.getNombre());
                if (atributo != null) {
                    return tipoEfectivo(atributo.getTipo(), atributo.getDimensiones());
                }
            }
            Simbolo simbolo = tipos.buscar(id.getNombre());
            return simbolo == null ? null : simbolo.tipo;
        }
        if (expr instanceof NodoAccesoCampo campo) {
            NodoTipo tipoBase = tipoDeBase(campo.getBase());
            if (tipoBase == null || tipoBase.getNivelesArreglo() > 0) {
                return null;
            }
            NodoEstructura estructura = definiciones.get(tipoBase.getNombre());
            if (estructura == null) {
                return null;
            }
            NodoAtributo atributo = buscarAtributo(estructura, campo.getCampo());
            return atributo == null ? null : tipoEfectivo(atributo.getTipo(), atributo.getDimensiones());
        }
        if (expr instanceof NodoAccesoArreglo arreglo) {
            NodoTipo base = tipoDeExpresionSimple(arreglo.getBase());
            if (base == null || base.getNivelesArreglo() == 0) {
                return null;
            }
            return new NodoTipo(base.getLinea(), base.getColumna(), base.getNombre(), base.getNivelesArreglo() - 1);
        }
        if (expr instanceof NodoNuevoObjeto nuevo) {
            return new NodoTipo(nuevo.getLinea(), nuevo.getColumna(), nuevo.getNombreClase(), 0);
        }
        if (expr instanceof NodoLlamada llamada) {
            LlamadaResuelta resuelta = resolverLlamada(llamada);
            return resuelta == null || resuelta.funcion() == null ? null : resuelta.funcion().getTipoRetorno();
        }
        return null;
    }

    private NodoTipo tipoRetornoNormalizado(NodoTipo tipoRetorno) {
        return esVoid(tipoRetorno) ? TIPO_ENTERO : tipoRetorno;
    }

    private NodoTipo tipoDeLlamada(NodoLlamada llamada) {
        LlamadaResuelta resuelta = resolverLlamada(llamada);
        if (resuelta == null || resuelta.funcion() == null) {
            return TIPO_ENTERO;
        }
        return tipoRetornoNormalizado(resuelta.funcion().getTipoRetorno());
    }

    private String etiquetaTipo(NodoTipo tipo) {
        String nombre = tipo.getNombre();
        String base;
        if (esCategoriaEntero(nombre)) {
            base = "entero";
        } else if (esCategoriaDecimal(nombre)) {
            base = "flotante";
        } else if (esCategoriaCaracter(nombre)) {
            base = "caracter";
        } else if (esCategoriaCadena(nombre)) {
            base = "cadena";
        } else if (esCategoriaBool(nombre)) {
            base = "bool";
        } else {
            base = nombre.replaceAll("[^A-Za-z0-9_]", "_");
        }
        return base + "A".repeat(tipo.getNivelesArreglo());
    }

    private boolean mismoTipo(NodoTipo a, NodoTipo b) {
        return etiquetaTipo(a).equals(etiquetaTipo(b));
    }

    private boolean mismaFirma(List<NodoParametro> a, List<NodoParametro> b) {
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            if (!mismoTipo(a.get(i).getTipo(), b.get(i).getTipo())) {
                return false;
            }
        }
        return true;
    }

    private String sufijoFirma(List<NodoParametro> parametros) {
        if (parametros.isEmpty()) {
            return "";
        }
        List<String> partes = new ArrayList<>();
        for (NodoParametro parametro : parametros) {
            partes.add(etiquetaTipo(parametro.getTipo()));
        }
        return "__" + String.join("_", partes);
    }

    private String etiquetaMetodo(NodoEstructura duena, NodoFuncion metodo) {
        return duena.getNombre() + "__m_" + metodo.getNombre() + sufijoFirma(metodo.getParametros());
    }

    private String etiquetaConstructor(NodoEstructura clase, NodoFuncion constructor) {
        return clase.getNombre() + "__c" + (constructor == null ? "" : sufijoFirma(constructor.getParametros()));
    }

    private String etiquetaLibre(NodoFuncion funcion) {
        List<NodoFuncion> mismas = funcionesPorNombre.get(funcion.getNombre());
        if (mismas != null && mismas.size() > 1) {
            return funcion.getNombre() + sufijoFirma(funcion.getParametros());
        }
        return funcion.getNombre();
    }

    private int puntaje(NodoTipo parametro, NodoTipo argumento) {
        if (argumento == null) {
            return 1;
        }
        if (esNulo(argumento)) {
            return esReferenciaTipo(parametro) ? 2 : -1;
        }
        if (mismoTipo(parametro, argumento)) {
            return 3;
        }
        if (parametro.getNivelesArreglo() != argumento.getNivelesArreglo()) {
            return -1;
        }
        if (esCategoriaDecimal(parametro.getNombre()) && esCategoriaEntero(argumento.getNombre())) {
            return 1;
        }
        if (esSubtipo(argumento.getNombre(), parametro.getNombre())) {
            return 2;
        }
        return -1;
    }

    private <T> T elegirSobrecarga(List<T> candidatos, List<List<NodoParametro>> parametros, List<NodoAST> argumentos) {
        T mejor = null;
        int mejorPuntaje = -1;
        T porAridad = null;
        for (int i = 0; i < candidatos.size(); i++) {
            List<NodoParametro> ps = parametros.get(i);
            if (ps.size() != argumentos.size()) {
                continue;
            }
            if (porAridad == null) {
                porAridad = candidatos.get(i);
            }
            int total = 0;
            boolean aplica = true;
            for (int j = 0; j < ps.size(); j++) {
                int p = puntaje(ps.get(j).getTipo(), inferirTipo(argumentos.get(j)));
                if (p < 0) {
                    aplica = false;
                    break;
                }
                total += p;
            }
            if (aplica && total > mejorPuntaje) {
                mejorPuntaje = total;
                mejor = candidatos.get(i);
            }
        }
        return mejor != null ? mejor : porAridad;
    }

    private MetodoImpl resolverMetodo(NodoEstructura clase, String nombre, List<NodoAST> argumentos) {
        List<MetodoImpl> candidatos = new ArrayList<>();
        for (NodoEstructura actual : ascendencia(clase)) {
            for (NodoFuncion metodo : actual.getMetodos()) {
                if (!metodo.getNombre().equals(nombre)) {
                    continue;
                }
                boolean repetido = false;
                for (MetodoImpl existente : candidatos) {
                    if (mismaFirma(existente.funcion().getParametros(), metodo.getParametros())) {
                        repetido = true; 
                        break;
                    }
                }
                if (!repetido) {
                    candidatos.add(new MetodoImpl(actual, metodo));
                }
            }
        }
        List<List<NodoParametro>> parametros = new ArrayList<>();
        for (MetodoImpl candidato : candidatos) {
            parametros.add(candidato.funcion().getParametros());
        }
        return elegirSobrecarga(candidatos, parametros, argumentos);
    }

    private NodoFuncion resolverLibre(String nombre, List<NodoAST> argumentos) {
        List<NodoFuncion> candidatos = funcionesPorNombre.get(nombre);
        if (candidatos == null || candidatos.isEmpty()) {
            return null;
        }
        List<List<NodoParametro>> parametros = new ArrayList<>();
        for (NodoFuncion candidato : candidatos) {
            parametros.add(candidato.getParametros());
        }
        return elegirSobrecarga(candidatos, parametros, argumentos);
    }

    private NodoFuncion resolverConstructor(NodoEstructura clase, List<NodoAST> argumentos) {
        List<NodoFuncion> candidatos = clase.getConstructores();
        if (candidatos.isEmpty()) {
            return null;
        }
        List<List<NodoParametro>> parametros = new ArrayList<>();
        for (NodoFuncion candidato : candidatos) {
            parametros.add(candidato.getParametros());
        }
        return elegirSobrecarga(candidatos, parametros, argumentos);
    }

    private LlamadaResuelta resolverLlamada(NodoLlamada llamada) {
        NodoAST base = llamada.getBase();
        List<NodoAST> argumentos = llamada.getArgumentos();

        if (base instanceof NodoIdentificador id) {
            String nombre = id.getNombre();
            if (claseActual != null) {
                MetodoImpl metodo = resolverMetodo(claseActual, nombre, argumentos);
                if (metodo != null) {
                    return new LlamadaResuelta(claseActual, metodo.duena(), metodo.funcion(), true, false);
                }
            }
            NodoFuncion libre = resolverLibre(nombre, argumentos);
            return libre == null ? null : new LlamadaResuelta(null, null, libre, false, false);
        }
        if (base instanceof NodoAccesoCampo campo) {
            boolean viaSuper = esSuper(campo.getBase());
            NodoTipo tipoEstatico = tipoDeBase(campo.getBase());
            if (tipoEstatico == null || tipoEstatico.getNivelesArreglo() > 0) {
                return null;
            }
            NodoEstructura estatica = definiciones.get(tipoEstatico.getNombre());
            if (estatica == null) {
                return null;
            }
            MetodoImpl metodo = resolverMetodo(estatica, campo.getCampo(), argumentos);
            return metodo == null ? null
                    : new LlamadaResuelta(estatica, metodo.duena(), metodo.funcion(), false, viaSuper);
        }
        return null;
    }

    private NodoTipo inferirTipo(NodoAST expr) {
        if (expr instanceof NodoLiteral literal) {
            return switch (literal.getTipo()) {
                case ENTERO ->
                    TIPO_ENTERO;
                case DECIMAL ->
                    TIPO_DECIMAL;
                case CADENA ->
                    TIPO_CADENA;
                case CARACTER ->
                    TIPO_CARACTER;
                case BOOLEANO ->
                    TIPO_BOOLEANO;
                case NULO ->
                    TIPO_NULO;
                default ->
                    TIPO_ENTERO;
            };
        }
        if (expr instanceof NodoBinaria binaria) {
            if (esOperadorBooleano(binaria.getOperador())) {
                return TIPO_BOOLEANO;
            }
            if ("+".equals(binaria.getOperador()) && (esCadena(binaria.getIzquierda()) || esCadena(binaria.getDerecha()))) {
                return TIPO_CADENA;
            }
            NodoTipo izquierda = inferirTipo(binaria.getIzquierda());
            NodoTipo derecha = inferirTipo(binaria.getDerecha());
            return esCategoriaDecimal(izquierda.getNombre()) ? izquierda
                    : (esCategoriaDecimal(derecha.getNombre()) ? derecha : izquierda);
        }
        if (expr instanceof NodoUnaria unaria) {
            return "!".equals(unaria.getOperador()) ? TIPO_BOOLEANO : inferirTipo(unaria.getOperando());
        }
        if (expr instanceof NodoIncrementoDecremento incremento) {
            return inferirTipo(incremento.getDestino());
        }
        if (expr instanceof NodoTernaria ternaria) {
            NodoTipo tipo = inferirTipo(ternaria.getSiVerdadero());
            if (esNulo(tipo)) {
                return inferirTipo(ternaria.getSiFalso());
            }
            return tipo != null ? tipo : inferirTipo(ternaria.getSiFalso());
        }
        if (expr instanceof NodoLlamada llamada) {
            return tipoDeLlamada(llamada);
        }
        if (expr instanceof NodoNuevoObjeto nuevo) {
            return new NodoTipo(nuevo.getLinea(), nuevo.getColumna(), nuevo.getNombreClase(), 0);
        }
        if (expr instanceof NodoNuevoArreglo nuevo) {
            return new NodoTipo(nuevo.getLinea(), nuevo.getColumna(), nuevo.getTipoBase(), nuevo.getDimensiones().size());
        }
        if (expr instanceof NodoLecturaEntrada) {
            NodoTipo destino = tipoDestinoActual.peek();
            return destino != null ? destino : TIPO_CADENA;
        }
        NodoTipo simple = tipoDeExpresionSimple(expr);
        return simple != null ? simple : TIPO_ENTERO;
    }

    private boolean esCadena(NodoAST expr) {
        if (expr instanceof NodoLiteral literal) {
            return literal.getTipo() == TipoLiteral.CADENA;
        }
        if (expr instanceof NodoBinaria binaria && "+".equals(binaria.getOperador())) {
            return esCadena(binaria.getIzquierda()) || esCadena(binaria.getDerecha());
        }
        NodoTipo tipo = tipoDeExpresionSimple(expr);
        return tipo != null && tipo.getNivelesArreglo() == 0 && esCategoriaCadena(tipo.getNombre());
    }

    private String mnemonicoScan(NodoTipo tipo) {
        if (tipo == null) {
            return "SCAN_TEXT";
        }
        if (esCategoriaEntero(tipo.getNombre())) {
            return "SCAN_INT";
        }
        if (esCategoriaDecimal(tipo.getNombre())) {
            return "SCAN_FLOAT";
        }
        if (esCategoriaCaracter(tipo.getNombre())) {
            return "SCAN_CHAR";
        }
        return "SCAN_TEXT";
    }

    private Direccion sumarPosicion(Direccion base, String cantidad) {
        if (base.esVariableCompleta()) {
            return new Direccion(base.variable(), cantidad, base.puntero());
        }
        return new Direccion(base.variable(), sumar(base.posicion(), cantidad), base.puntero());
    }

    private Direccion sumarPosicion(Direccion base, int cantidadLiteral) {
        return sumarPosicion(base, String.valueOf(cantidadLiteral));
    }

    private int celdasDelElementoFinal(NodoAST base, int cantidadIndicesAplicados) {
        NodoTipo tipoBase = tipoDeExpresionSimple(base);
        if (tipoBase == null) {
            return 1;
        }
        int nivelesRestantes = tipoBase.getNivelesArreglo() - cantidadIndicesAplicados;
        if (nivelesRestantes > 0) {
            return 1;
        }
        return celdasPorElemento(tipoBase.getNombre());
    }

    private Direccion resolverDireccion(NodoAST nodo) {
        if (nodo instanceof NodoIdentificador id) {
            String nombre = id.getNombre();
            if ("this".equals(nombre)) {
                return new Direccion("this", null);
            }
            boolean local = nombresLocales != null && nombresLocales.contains(nombre);
            if (!local && claseActual != null && buscarAtributo(claseActual, nombre) != null) {
                MapaDeClase mapa = obtenerMapaDeClase(claseActual.getNombre());
                int posicion = mapa != null ? mapa.posicionesDeCampos().getOrDefault(nombre, 0) : 0;
                return new Direccion("this", String.valueOf(posicion), true);
            }
            return new Direccion(nombre, null);
        }
        if (nodo instanceof NodoAccesoCampo campo) {
            NodoTipo tipoBase = tipoDeBase(campo.getBase());
            NodoEstructura definicion = tipoBase != null && tipoBase.getNivelesArreglo() == 0
                    ? definiciones.get(tipoBase.getNombre()) : null;
            MapaDeClase mapa = definicion != null ? obtenerMapaDeClase(definicion.getNombre()) : null;
            int posicionCampo = mapa != null ? mapa.posicionesDeCampos().getOrDefault(campo.getCampo(), 0) : 0;

            if (esClase(definicion)) {
                String puntero = esSuper(campo.getBase()) ? "this" : generarExpresion(campo.getBase());
                return new Direccion(puntero, String.valueOf(posicionCampo), true);
            }
            Direccion base = resolverDireccion(campo.getBase());
            return sumarPosicion(base, posicionCampo);
        }
        if (nodo instanceof NodoAccesoArreglo) {
            List<NodoAST> indices = new ArrayList<>();
            NodoAST actual = nodo;
            while (actual instanceof NodoAccesoArreglo acceso) {
                indices.add(0, acceso.getIndice());
                actual = acceso.getBase();
            }
            Direccion base = resolverDireccion(actual);
            List<String> tamanos = dimensionesArreglo.getOrDefault(claveDireccion(base),
                    dimensionesArreglo.getOrDefault(base.variable(), List.of()));

            String posicion = null;
            for (int i = 0; i < indices.size(); i++) {
                String indice = generarExpresion(indices.get(i));
                if (posicion == null) {
                    posicion = indice;
                } else {
                    String dimensionActual = i < tamanos.size()
                            ? resolverTamanoArreglo(tamanos.get(i)) : "1";
                    posicion = sumar(multiplicar(posicion, dimensionActual), indice);
                }
            }
            int celdasElemento = celdasDelElementoFinal(actual, indices.size());
            if (celdasElemento != 1) {
                posicion = multiplicar(posicion, String.valueOf(celdasElemento));
            }
            if (base.puntero()) {
                String punteroArreglo = cargar(base, tipoDeExpresionSimple(actual));
                return new Direccion(punteroArreglo, posicion, true);
            }
            return sumarPosicion(base, posicion);
        }
        return new Direccion(generarExpresion(nodo), null);
    }

    private String cargar(Direccion direccion, NodoTipo tipoValor) {
        if (direccion.esVariableCompleta()) {
            return direccion.variable();
        }
        return nuevoTempCargado(direccion, tipoValor != null ? tipoValor : TIPO_ENTERO);
    }

    private String nuevoTempCargado(Direccion direccion, NodoTipo tipo) {
        String temp = nuevoTemp(tipo);
        emitir(direccion.puntero() ? "load" : "=[]", direccion.variable(), direccion.posicion(), temp);
        return temp;
    }

    private void guardar(Direccion direccion, String valor) {
        if (direccion.esVariableCompleta()) {
            emitir("=", valor, null, direccion.variable());
            return;
        }
        if (direccion.puntero()) {
            emitir("store", direccion.posicion(), valor, direccion.variable());
            return;
        }
        emitir("[]=", direccion.posicion(), valor, direccion.variable());
    }

    private void generarCondicion(NodoAST nodo, String etVerdadero, String etFalso) {
        if (nodo instanceof NodoUnaria unaria && "!".equals(unaria.getOperador())) {
            generarCondicion(unaria.getOperando(), etFalso, etVerdadero);
            return;
        }
        if (nodo instanceof NodoBinaria binaria) {
            String operador = binaria.getOperador();
            if ("&&".equals(operador)) {
                String etSiguiente = nuevaEtiqueta();
                generarCondicion(binaria.getIzquierda(), etSiguiente, etFalso);
                etiqueta(etSiguiente);
                generarCondicion(binaria.getDerecha(), etVerdadero, etFalso);
                return;
            }
            if ("||".equals(operador)) {
                String etSiguiente = nuevaEtiqueta();
                generarCondicion(binaria.getIzquierda(), etVerdadero, etSiguiente);
                etiqueta(etSiguiente);
                generarCondicion(binaria.getDerecha(), etVerdadero, etFalso);
                return;
            }
            if (RELACIONALES.contains(operador)) {
                String izquierda = generarExpresion(binaria.getIzquierda());
                String derecha = generarExpresion(binaria.getDerecha());
                String mnemonico = "=".equals(operador) ? "if==" : "if" + operador;
                emitir(mnemonico, izquierda, derecha, etVerdadero);
                emitir("goto", null, null, etFalso);
                return;
            }
        }
        String lugar = generarExpresion(nodo);
        emitir("if", lugar, null, etVerdadero);
        emitir("goto", null, null, etFalso);
    }

    private String generarValorBooleano(NodoAST nodo) {
        String etVerdadero = nuevaEtiqueta();
        String etFalso = nuevaEtiqueta();
        String etFin = nuevaEtiqueta();
        String temp = nuevoTemp(TIPO_BOOLEANO);
        generarCondicion(nodo, etVerdadero, etFalso);
        etiqueta(etVerdadero);
        emitir("=", "1", null, temp);
        emitir("goto", null, null, etFin);
        etiqueta(etFalso);
        emitir("=", "0", null, temp);
        etiqueta(etFin);
        return temp;
    }

    private boolean esOperadorBooleano(String operador) {
        return RELACIONALES.contains(operador) || "&&".equals(operador) || "||".equals(operador);
    }

    @Override
    public String visitar(NodoPrograma nodo) {
        generarProgramas(nodo);
        return null;
    }

    private List<NodoEstructura> clasesDe(NodoPrograma programa) {
        List<NodoEstructura> clases = programa.getClases();
        if (clases != null && !clases.isEmpty()) {
            return clases;
        }
        return programa.getClaseUnica() != null ? List.of(programa.getClaseUnica()) : List.of();
    }

    public void generarProgramas(NodoPrograma... programas) {
        int siguienteId = 1;
        for (NodoPrograma p : programas) {
            for (NodoEstructura estructura : p.getEstructuras()) {
                definiciones.put(estructura.getNombre(), estructura);
            }
            for (NodoEstructura clase : clasesDe(p)) {
                definiciones.put(clase.getNombre(), clase);
                if (esClase(clase) && !idsDeClase.containsKey(clase.getNombre())) {
                    idsDeClase.put(clase.getNombre(), siguienteId++);
                    clasesZ.add(clase);
                }
            }
        }

        List<NodoFuncion> todasLasFunciones = new ArrayList<>();
        for (NodoPrograma p : programas) {
            for (NodoFuncion funcion : p.getFunciones()) {
                todasLasFunciones.add(funcion);
                funcionesPorNombre.computeIfAbsent(funcion.getNombre(), k -> new ArrayList<>()).add(funcion);
            }
        }
        for (NodoFuncion funcion : todasLasFunciones) {
            funcionesLibres.put(etiquetaLibre(funcion), funcion);
        }

        for (NodoPrograma p : programas) {
            for (NodoDeclaracion variable : p.getVariablesGlobales()) {
                variable.aceptar(this);
            }
            for (NodoAST sentencia : p.getSentenciasPrincipales()) {
                sentencia.aceptar(this);
            }
        }

        if (!todasLasFunciones.isEmpty() || !clasesZ.isEmpty()) {
            String etFinPrograma = nuevaEtiqueta();
            emitir("goto", null, null, etFinPrograma);
            for (NodoFuncion funcion : todasLasFunciones) {
                funcion.aceptar(this);
            }
            for (NodoEstructura clase : clasesZ) {
                generarClase(clase);
            }
            etiqueta(etFinPrograma);
        }
    }

    @Override
    public String visitar(NodoImportacion nodo) {
        return null;
    }

    @Override
    public String visitar(NodoEstructura nodo) {
        return null;
    }

    @Override
    public String visitar(NodoAtributo nodo) {
        return null;
    }

    @Override
    public String visitar(NodoFuncion nodo) {
        generarCuerpoFuncion(etiquetaLibre(nodo), nodo.getParametros(), false, () -> {
            for (NodoAST sentencia : nodo.getCuerpo()) {
                sentencia.aceptar(this);
            }
        });
        return null;
    }

    private void generarCuerpoFuncion(String etiqueta, List<NodoParametro> parametros, boolean esMetodo, Runnable cuerpo) {
        nombresDeFunciones.add(etiqueta);
        Set<String> localesAnteriores = nombresLocales;
        nombresLocales = esMetodo ? new HashSet<>() : null;
        tipos.entrarAmbito();
        etiqueta(etiqueta);
        for (NodoParametro parametro : parametros) {
            parametro.aceptar(this);
        }
        cuerpo.run();
        emitir("return", null, null, null);
        tipos.salirAmbito();
        nombresLocales = localesAnteriores;
    }

    private List<NodoParametro> parametrosConThis(NodoEstructura clase, List<NodoParametro> originales) {
        List<NodoParametro> lista = new ArrayList<>();
        lista.add(new NodoParametro(clase.getLinea(), clase.getColumna(), tipoDeClase(clase), "this"));
        lista.addAll(originales);
        return lista;
    }

    private void generarClase(NodoEstructura clase) {
        NodoEstructura claseAnterior = claseActual;
        claseActual = clase;
        if (clase.getConstructores().isEmpty()) {
            generarConstructor(clase, null);
        } else {
            for (NodoFuncion constructor : clase.getConstructores()) {
                generarConstructor(clase, constructor);
            }
        }
        for (NodoFuncion metodo : clase.getMetodos()) {
            generarMetodo(clase, metodo);
        }
        claseActual = claseAnterior;
    }

    private void generarMetodo(NodoEstructura clase, NodoFuncion metodo) {
        String etiqueta = etiquetaMetodo(clase, metodo);
        List<NodoParametro> parametros = parametrosConThis(clase, metodo.getParametros());
        funcionesLibres.put(etiqueta, new NodoFuncion(metodo.getLinea(), metodo.getColumna(), etiqueta,
                parametros, metodo.getTipoRetorno(), metodo.getCuerpo(), false));
        generarCuerpoFuncion(etiqueta, parametros, true, () -> {
            for (NodoAST sentencia : metodo.getCuerpo()) {
                sentencia.aceptar(this);
            }
        });
    }

    private void generarConstructor(NodoEstructura clase, NodoFuncion constructor) {
        String etiqueta = etiquetaConstructor(clase, constructor);
        List<NodoParametro> originales = constructor == null ? List.of() : constructor.getParametros();
        List<NodoAST> cuerpo = constructor == null ? List.of() : constructor.getCuerpo();
        List<NodoParametro> parametros = parametrosConThis(clase, originales);
        funcionesLibres.put(etiqueta, new NodoFuncion(clase.getLinea(), clase.getColumna(), etiqueta,
                parametros, null, cuerpo, true));

        generarCuerpoFuncion(etiqueta, parametros, true, () -> {
            int inicio = 0;
            boolean delegaEnThis = false;
            NodoLlamada explicita = llamadaAConstructor(cuerpo.isEmpty() ? null : cuerpo.get(0));
            if (explicita != null) {
                cuerpo.get(0).aceptar(this);
                inicio = 1;
                delegaEnThis = "this".equals(((NodoIdentificador) explicita.getBase()).getNombre());
            } else {
                NodoEstructura padre = clasePadre(clase);
                if (esClase(padre)) {
                    generarLlamadaConstructor(padre, List.of());
                }
            }
            if (!delegaEnThis) {
                inicializarAtributos(clase);
            }
            for (int i = inicio; i < cuerpo.size(); i++) {
                cuerpo.get(i).aceptar(this);
            }
        });
    }

    private NodoLlamada llamadaAConstructor(NodoAST sentencia) {
        if (sentencia instanceof NodoExpresionSentencia es
                && es.getExpresion() instanceof NodoLlamada llamada
                && llamada.getBase() instanceof NodoIdentificador id
                && ("super".equals(id.getNombre()) || "this".equals(id.getNombre()))) {
            return llamada;
        }
        return null;
    }

    private void inicializarAtributos(NodoEstructura clase) {
        MapaDeClase mapa = obtenerMapaDeClase(clase.getNombre());
        for (NodoAtributo atributo : clase.getAtributos()) {
            NodoAST valorInicial = atributo.getValorInicial();
            if (valorInicial == null || valorInicial instanceof NodoListaExpresiones) {
                continue;
            }
            int posicion = mapa != null ? mapa.posicionesDeCampos().getOrDefault(atributo.getNombre(), 0) : 0;
            tipoDestinoActual.push(tipoEfectivo(atributo.getTipo(), atributo.getDimensiones()));
            String valor;
            try {
                valor = generarExpresion(valorInicial);
            } finally {
                tipoDestinoActual.pop();
            }
            emitir("store", String.valueOf(posicion), valor, "this");
        }
    }

    private void generarLlamadaConstructor(NodoEstructura clase, List<NodoAST> argumentosAST) {
        if (!esClase(clase)) {
            return;
        }
        List<String> argumentos = new ArrayList<>();
        for (NodoAST argumento : argumentosAST) {
            argumentos.add(generarExpresion(argumento));
        }
        NodoFuncion constructor = resolverConstructor(clase, argumentosAST);
        emitir("param", "this", null, null);
        for (String argumento : argumentos) {
            emitir("param", argumento, null, null);
        }
        emitir("call", etiquetaConstructor(clase, constructor), String.valueOf(argumentos.size() + 1),
                nuevoTemp(TIPO_ENTERO));
    }

    @Override
    public String visitar(NodoParametro nodo) {
        tipos.declarar(Simbolo.variable(nodo.getNombre(), nodo.getTipo()));
        if (nombresLocales != null) {
            nombresLocales.add(nodo.getNombre());
        }
        registrarTipo(nodo.getNombre(), nodo.getTipo(), tamanoParaDeclarar(nodo.getTipo(), List.of()));
        emitir("recibir", null, null, nodo.getNombre());
        return null;
    }

    @Override
    public String visitar(NodoTipo nodo) {
        return null;
    }

    @Override
    public String visitar(NodoDimension nodo) {
        return generarExpresion(nodo.getTamano());
    }

    @Override
    public String visitar(NodoDeclaracion nodo) {
        NodoTipo tipoDeclarado = tipoEfectivo(nodo.getTipo(), nodo.getDimensiones());
        tipos.declarar(Simbolo.variable(nodo.getNombre(), tipoDeclarado));
        if (nombresLocales != null) {
            nombresLocales.add(nodo.getNombre());
        }
        int cantidadDeCeldas = tamanoParaDeclarar(tipoDeclarado, nodo.getDimensiones());
        registrarTipo(nodo.getNombre(), tipoDeclarado, cantidadDeCeldas);

        if (!nodo.getDimensiones().isEmpty()) {
            List<String> tamanos = new ArrayList<>();
            for (NodoDimension dimension : nodo.getDimensiones()) {
                tamanos.add(generarExpresion(dimension.getTamano()));
            }
            registrarDimensiones(nodo.getNombre(), tamanos);
        }

        NodoAST inicializador = nodo.getInicializador();

        if (esReferenciaObjeto(tipoDeclarado)) {
            if (inicializador == null) {
                emitir("=", "0", null, nodo.getNombre());
                return null;
            }
            tipoDestinoActual.push(tipoDeclarado);
            String valor;
            try {
                valor = generarExpresion(inicializador);
            } finally {
                tipoDestinoActual.pop();
            }
            guardar(new Direccion(nodo.getNombre(), null), valor);
            return null;
        }

        boolean esCompuesto = esCompuesto(tipoDeclarado);
        boolean necesitaReservaPropia = esCompuesto
                && !(inicializador instanceof NodoNuevoObjeto)
                && !(inicializador instanceof NodoNuevoArreglo)
                && (inicializador == null
                || inicializador instanceof NodoListaExpresiones
                || !nodo.getDimensiones().isEmpty());
        if (necesitaReservaPropia) {
            emitirReserva(nodo.getNombre(), cantidadDeCeldas);
        }

        if (inicializador == null) {
            return null;
        }
        if (inicializador instanceof NodoListaExpresiones lista) {
            generarInicializacionLista(nodo.getNombre(), lista, new int[]{0});
            return null;
        }
        if (inicializador instanceof NodoNuevoArreglo nuevoArreglo) {
            List<String> tamanos = new ArrayList<>();
            for (NodoAST dimension : nuevoArreglo.getDimensiones()) {
                tamanos.add(generarExpresion(dimension));
            }
            registrarDimensiones(nodo.getNombre(), tamanos);
            int totalNuevo = calcularTamanoTotal(tamanos, celdasPorElemento(tipoDeclarado.getNombre()));
            registrarTipo(nodo.getNombre(), tipoDeclarado, totalNuevo);
            emitir("alloc", String.valueOf(totalNuevo), null, nodo.getNombre());
            inicializarArregloEnCero(nodo.getNombre(), totalNuevo);
            return null;
        }
        if (inicializador instanceof NodoNuevoObjeto nuevoObjeto) {
            generarInstanciacionEstructura(nuevoObjeto, nodo.getNombre());
            return null;
        }

        tipoDestinoActual.push(tipoDeclarado);
        String valor;
        try {
            valor = generarExpresion(inicializador);
        } finally {
            tipoDestinoActual.pop();
        }
        guardar(new Direccion(nodo.getNombre(), null), valor);
        return null;
    }

    private void generarInicializacionLista(String nombreArreglo, NodoListaExpresiones lista, int[] posicion) {
        for (NodoAST elemento : lista.getElementos()) {
            if (elemento instanceof NodoListaExpresiones sub) {
                generarInicializacionLista(nombreArreglo, sub, posicion);
            } else {
                NodoTipo tipoElemento = tipoDeExpresionSimple(elemento);
                MapaDeClase estructura = tipoElemento == null || esClaseObjeto(tipoElemento.getNombre())
                        ? null : obtenerMapaDeClase(tipoElemento.getNombre());
                if (estructura != null) {
                    String origen = generarExpresion(elemento);
                    for (int i = 0; i < estructura.tamanoEnCeldas(); i++) {
                        String valor = nuevoTemp();
                        emitir("=[]", origen, String.valueOf(i), valor);
                        emitir("[]=", String.valueOf(posicion[0] + i), valor, nombreArreglo);
                    }
                    posicion[0] += estructura.tamanoEnCeldas();
                    continue;
                }
                String valor = generarExpresion(elemento);
                emitir("[]=", String.valueOf(posicion[0]), valor, nombreArreglo);
                posicion[0]++;
            }
        }
    }

    @Override
    public String visitar(NodoListaExpresiones nodo) {
        for (NodoAST elemento : nodo.getElementos()) {
            generarExpresion(elemento);
        }
        return null;
    }

    @Override
    public String visitar(NodoBloque nodo) {
        for (NodoAST sentencia : nodo.getSentencias()) {
            sentencia.aceptar(this);
        }
        return null;
    }

    @Override
    public String visitar(NodoAsignacion nodo) {
        if (nodo.getValor() instanceof NodoNuevoObjeto nuevoObjeto && nodo.getDestino() instanceof NodoIdentificador id
                && definiciones.containsKey(nuevoObjeto.getNombreClase())
                && !esClaseObjeto(nuevoObjeto.getNombreClase())) {
            generarInstanciacionEstructura(nuevoObjeto, id.getNombre());
            return null;
        }

        Direccion destino = resolverDireccion(nodo.getDestino());
        tipoDestinoActual.push(tipoDeExpresionSimple(nodo.getDestino()));
        try {
            if ("=".equals(nodo.getOperador())) {
                String valor = generarExpresion(nodo.getValor());
                guardar(destino, valor);
                if (nodo.getValor() instanceof NodoNuevoArreglo) {
                    List<String> tamanos = dimensionesArreglo.get(valor);
                    if (tamanos != null) {
                        registrarDimensiones(claveDireccion(destino), tamanos);
                    }
                }
            } else {
                String operadorBase = nodo.getOperador().substring(0, 1);
                NodoTipo tipoDestino = tipoDeExpresionSimple(nodo.getDestino());
                String actual = cargar(destino, tipoDestino);
                String derecho = generarExpresion(nodo.getValor());
                boolean concatenar = "+".equals(operadorBase) && (esCadena(nodo.getDestino()) || esCadena(nodo.getValor()));
                String temp = nuevoTemp(concatenar ? TIPO_CADENA : tipoDestino);
                emitir(concatenar ? "concat" : operadorBase, actual, derecho, temp);
                guardar(destino, temp);
            }
        } finally {
            tipoDestinoActual.pop();
        }
        return null;
    }

    @Override
    public String visitar(NodoIncrementoDecremento nodo) {
        Direccion direccion = resolverDireccion(nodo.getDestino());
        NodoTipo tipo = tipoDeExpresionSimple(nodo.getDestino());
        String actual = cargar(direccion, tipo);
        String resultado = actual;
        if (nodo.devuelveValorAnterior()) {
            resultado = nuevoTemp(tipo != null ? tipo : TIPO_ENTERO);
            emitir("=", actual, null, resultado);
        }
        String temp = nuevoTemp(tipo != null ? tipo : TIPO_ENTERO);
        emitir("++".equals(nodo.getOperador()) ? "+" : "-", actual, "1", temp);
        guardar(direccion, temp);
        return nodo.devuelveValorAnterior() ? resultado : temp;
    }

    @Override
    public String visitar(NodoIf nodo) {
        String etFin = nuevaEtiqueta();
        generarRamaIf(nodo.getCondicion(), nodo.getCuerpoSi(), nodo.getRamasSinoSi(), 0, nodo.getCuerpoSino(), etFin);
        etiqueta(etFin);
        return null;
    }

    private void generarRamaIf(NodoAST condicion, List<NodoAST> cuerpo, List<NodoSinoSi> ramas, int indice,
            List<NodoAST> cuerpoSino, String etFin) {
        String etVerdadero = nuevaEtiqueta();
        String etFalso = nuevaEtiqueta();
        generarCondicion(condicion, etVerdadero, etFalso);
        etiqueta(etVerdadero);
        for (NodoAST sentencia : cuerpo) {
            sentencia.aceptar(this);
        }
        emitir("goto", null, null, etFin);
        etiqueta(etFalso);
        if (indice < ramas.size()) {
            NodoSinoSi rama = ramas.get(indice);
            generarRamaIf(rama.getCondicion(), rama.getCuerpo(), ramas, indice + 1, cuerpoSino, etFin);
        } else if (cuerpoSino != null) {
            for (NodoAST sentencia : cuerpoSino) {
                sentencia.aceptar(this);
            }
        }
    }

    @Override
    public String visitar(NodoSinoSi nodo) {
        String etVerdadero = nuevaEtiqueta();
        String etFalso = nuevaEtiqueta();
        generarCondicion(nodo.getCondicion(), etVerdadero, etFalso);
        etiqueta(etVerdadero);
        for (NodoAST sentencia : nodo.getCuerpo()) {
            sentencia.aceptar(this);
        }
        etiqueta(etFalso);
        return null;
    }

    @Override
    public String visitar(NodoSwitch nodo) {
        String valorSwitch = generarExpresion(nodo.getExpresion());
        String etFin = nuevaEtiqueta();
        etiquetasSalidaBucle.push(etFin);

        int cantidadCasos = nodo.getCasos().size();
        List<String> etiquetasCaso = new ArrayList<>();
        for (int i = 0; i < cantidadCasos; i++) {
            etiquetasCaso.add(nuevaEtiqueta());
        }
        String etDefault = nuevaEtiqueta();

        for (int i = 0; i < cantidadCasos; i++) {
            String valorCaso = generarExpresion(nodo.getCasos().get(i).getValor());
            emitir("if==", valorSwitch, valorCaso, etiquetasCaso.get(i));
            String etSiguienteComparacion = (i + 1 < cantidadCasos) ? nuevaEtiqueta() : etDefault;
            emitir("goto", null, null, etSiguienteComparacion);
            if (i + 1 < cantidadCasos) {
                etiqueta(etSiguienteComparacion);
            }
        }

        for (int i = 0; i < cantidadCasos; i++) {
            etiqueta(etiquetasCaso.get(i));
            for (NodoAST sentencia : nodo.getCasos().get(i).getCuerpo()) {
                sentencia.aceptar(this);
            }
        }
        etiqueta(etDefault);
        if (nodo.getCasoPorDefecto() != null) {
            for (NodoAST sentencia : nodo.getCasoPorDefecto()) {
                sentencia.aceptar(this);
            }
        }
        etiqueta(etFin);

        etiquetasSalidaBucle.pop();
        return null;
    }

    @Override
    public String visitar(NodoCaso nodo) {
        for (NodoAST sentencia : nodo.getCuerpo()) {
            sentencia.aceptar(this);
        }
        return generarExpresion(nodo.getValor());
    }

    @Override
    public String visitar(NodoFor nodo) {
        if (nodo.getInicializacion() != null) {
            nodo.getInicializacion().aceptar(this);
        }
        String etInicio = nuevaEtiqueta();
        String etCuerpo = nuevaEtiqueta();
        String etContinuar = nuevaEtiqueta();
        String etFin = nuevaEtiqueta();
        etiquetasSalidaBucle.push(etFin);
        etiquetasContinuarBucle.push(etContinuar);

        etiqueta(etInicio);
        if (nodo.getCondicion() != null) {
            generarCondicion(nodo.getCondicion(), etCuerpo, etFin);
        }
        etiqueta(etCuerpo);
        for (NodoAST sentencia : nodo.getCuerpo()) {
            sentencia.aceptar(this);
        }
        etiqueta(etContinuar);
        if (nodo.getActualizacion() != null) {
            nodo.getActualizacion().aceptar(this);
        }
        emitir("goto", null, null, etInicio);
        etiqueta(etFin);

        etiquetasSalidaBucle.pop();
        etiquetasContinuarBucle.pop();
        return null;
    }

    @Override
    public String visitar(NodoWhile nodo) {
        String etInicio = nuevaEtiqueta();
        String etCuerpo = nuevaEtiqueta();
        String etFin = nuevaEtiqueta();
        etiquetasSalidaBucle.push(etFin);
        etiquetasContinuarBucle.push(etInicio);

        etiqueta(etInicio);
        generarCondicion(nodo.getCondicion(), etCuerpo, etFin);
        etiqueta(etCuerpo);
        for (NodoAST sentencia : nodo.getCuerpo()) {
            sentencia.aceptar(this);
        }
        emitir("goto", null, null, etInicio);
        etiqueta(etFin);

        etiquetasSalidaBucle.pop();
        etiquetasContinuarBucle.pop();
        return null;
    }

    @Override
    public String visitar(NodoDoWhile nodo) {
        String etCuerpo = nuevaEtiqueta();
        String etContinuar = nuevaEtiqueta();
        String etFin = nuevaEtiqueta();
        etiquetasSalidaBucle.push(etFin);
        etiquetasContinuarBucle.push(etContinuar);

        etiqueta(etCuerpo);
        for (NodoAST sentencia : nodo.getCuerpo()) {
            sentencia.aceptar(this);
        }
        etiqueta(etContinuar);
        generarCondicion(nodo.getCondicion(), etCuerpo, etFin);
        etiqueta(etFin);

        etiquetasSalidaBucle.pop();
        etiquetasContinuarBucle.pop();
        return null;
    }

    @Override
    public String visitar(NodoBreak nodo) {
        emitir("goto", null, null, etiquetasSalidaBucle.peek());
        return null;
    }

    @Override
    public String visitar(NodoContinue nodo) {
        emitir("goto", null, null, etiquetasContinuarBucle.peek());
        return null;
    }

    @Override
    public String visitar(NodoReturn nodo) {
        String valor = nodo.getValor() != null ? generarExpresion(nodo.getValor()) : null;
        emitir("return", valor, null, null);
        return null;
    }

    @Override
    public String visitar(NodoImprimir nodo) {
        for (NodoAST expresion : nodo.getExpresiones()) {
            String lugar = generarExpresion(expresion);
            emitir("print", lugar, null, null);
        }
        if (nodo.isSaltoDeLinea()) {
            emitir("print", "\"\\n\"", null, null);
        }
        return null;
    }

    @Override
    public String visitar(NodoLeer nodo) {
        if (nodo.getDestino() == null) {
            emitir("SCAN_TEXT", null, null, nuevoTemp(TIPO_CADENA));
            return null;
        }
        NodoTipo tipo = tipoDeExpresionSimple(nodo.getDestino());
        String mnemonico = mnemonicoScan(tipo);
        Direccion direccion = resolverDireccion(nodo.getDestino());
        if (direccion.esVariableCompleta()) {
            emitir(mnemonico, null, null, direccion.variable());
        } else {
            String temp = nuevoTemp(tipo != null ? tipo : TIPO_CADENA);
            emitir(mnemonico, null, null, temp);
            guardar(direccion, temp);
        }
        return null;
    }

    @Override
    public String visitar(NodoExpresionSentencia nodo) {
        generarExpresion(nodo.getExpresion());
        return null;
    }

    @Override
    public String visitar(NodoBinaria nodo) {
        String operador = nodo.getOperador();
        if (esOperadorBooleano(operador)) {
            return generarValorBooleano(nodo);
        }
        String izquierda = generarExpresion(nodo.getIzquierda());
        String derecha = generarExpresion(nodo.getDerecha());
        NodoTipo tipoResultado = inferirTipo(nodo);
        boolean concatenar = "+".equals(operador) && esCategoriaCadena(tipoResultado.getNombre());
        String temp = nuevoTemp(tipoResultado);
        emitir(concatenar ? "concat" : operador, izquierda, derecha, temp);
        return temp;
    }

    @Override
    public String visitar(NodoUnaria nodo) {
        if ("!".equals(nodo.getOperador())) {
            return generarValorBooleano(nodo);
        }
        String operando = generarExpresion(nodo.getOperando());
        String temp = nuevoTemp(inferirTipo(nodo.getOperando()));
        emitir(nodo.getOperador(), operando, null, temp);
        return temp;
    }

    @Override
    public String visitar(NodoTernaria nodo) {
        String etVerdadero = nuevaEtiqueta();
        String etFalso = nuevaEtiqueta();
        String etFin = nuevaEtiqueta();
        String resultado = nuevoTemp(inferirTipo(nodo));
        generarCondicion(nodo.getCondicion(), etVerdadero, etFalso);
        etiqueta(etVerdadero);
        emitir("=", generarExpresion(nodo.getSiVerdadero()), null, resultado);
        emitir("goto", null, null, etFin);
        etiqueta(etFalso);
        emitir("=", generarExpresion(nodo.getSiFalso()), null, resultado);
        etiqueta(etFin);
        return resultado;
    }

    @Override
    public String visitar(NodoLiteral nodo) {
        return switch (nodo.getTipo()) {
            case CADENA ->
                "\"" + nodo.getValor() + "\"";
            case CARACTER ->
                "'" + nodo.getValor() + "'";
            case BOOLEANO ->
                ((Boolean) nodo.getValor()) ? "1" : "0";
            case NULO ->
                "0";
            default ->
                String.valueOf(nodo.getValor());
        };
    }

    @Override
    public String visitar(NodoIdentificador nodo) {
        return cargar(resolverDireccion(nodo), tipoDeExpresionSimple(nodo));
    }

    @Override
    public String visitar(NodoAccesoArreglo nodo) {
        return cargar(resolverDireccion(nodo), tipoDeExpresionSimple(nodo));
    }

    @Override
    public String visitar(NodoAccesoCampo nodo) {
        return cargar(resolverDireccion(nodo), tipoDeExpresionSimple(nodo));
    }

    @Override
    public String visitar(NodoLlamada nodo) {
        NodoAST base = nodo.getBase();

        if (base instanceof NodoIdentificador id && ("super".equals(id.getNombre()) || "this".equals(id.getNombre()))) {
            NodoEstructura destino = "this".equals(id.getNombre()) ? claseActual : clasePadre(claseActual);
            if (destino != null) {
                generarLlamadaConstructor(destino, nodo.getArgumentos());
            }
            return "0";
        }

        LlamadaResuelta resuelta = resolverLlamada(nodo);

        String receptor = null;
        if (resuelta != null && resuelta.claseEstatica() != null) {
            if (resuelta.receptorImplicito() || resuelta.viaSuper()) {
                receptor = "this";
            } else {
                receptor = generarExpresion(((NodoAccesoCampo) base).getBase());
            }
        }

        List<String> argumentos = new ArrayList<>();
        for (NodoAST argumento : nodo.getArgumentos()) {
            argumentos.add(generarExpresion(argumento));
        }

        if (resuelta == null || resuelta.funcion() == null) {
            if (base instanceof NodoIdentificador id) {
                for (String argumento : argumentos) {
                    emitir("param", argumento, null, null);
                }
                String resultado = nuevoTemp(TIPO_ENTERO);
                emitir("call", id.getNombre(), String.valueOf(argumentos.size()), resultado);
                return resultado;
            }
            return "0";
        }

        if (resuelta.claseEstatica() == null) {
            for (String argumento : argumentos) {
                emitir("param", argumento, null, null);
            }
            String resultado = nuevoTemp(tipoRetornoNormalizado(resuelta.funcion().getTipoRetorno()));
            emitir("call", etiquetaLibre(resuelta.funcion()), String.valueOf(argumentos.size()), resultado);
            return resultado;
        }

        return generarLlamadaMetodo(resuelta, receptor, argumentos);
    }

    private void emitirLlamadaDirecta(String etiqueta, String receptor, List<String> argumentos, String resultado) {
        emitir("param", receptor, null, null);
        for (String argumento : argumentos) {
            emitir("param", argumento, null, null);
        }
        emitir("call", etiqueta, String.valueOf(argumentos.size() + 1), resultado);
    }

    private String generarLlamadaMetodo(LlamadaResuelta resuelta, String receptor, List<String> argumentos) {
        String resultado = nuevoTemp(tipoRetornoNormalizado(resuelta.funcion().getTipoRetorno()));

        if (resuelta.viaSuper()) {
            emitirLlamadaDirecta(etiquetaMetodo(resuelta.duena(), resuelta.funcion()), receptor, argumentos, resultado);
            return resultado;
        }

        Map<NodoFuncion, List<NodoEstructura>> implementaciones = new LinkedHashMap<>();
        Map<NodoFuncion, NodoEstructura> duenas = new HashMap<>();
        for (NodoEstructura subclase : subtipos(resuelta.claseEstatica())) {
            MetodoImpl impl = implementacion(subclase, resuelta.funcion());
            if (impl != null) {
                implementaciones.computeIfAbsent(impl.funcion(), k -> new ArrayList<>()).add(subclase);
                duenas.put(impl.funcion(), impl.duena());
            }
        }

        if (implementaciones.size() <= 1) {
            NodoFuncion funcion = implementaciones.isEmpty() ? resuelta.funcion() : implementaciones.keySet().iterator().next();
            NodoEstructura duena = implementaciones.isEmpty() ? resuelta.duena() : duenas.get(funcion);
            emitirLlamadaDirecta(etiquetaMetodo(duena, funcion), receptor, argumentos, resultado);
            return resultado;
        }

        String idClase = nuevoTemp(TIPO_ENTERO);
        emitir("load", receptor, "0", idClase);

        List<NodoFuncion> orden = new ArrayList<>(implementaciones.keySet());
        List<String> etiquetas = new ArrayList<>();
        for (int i = 0; i < orden.size(); i++) {
            etiquetas.add(nuevaEtiqueta());
        }
        String etFin = nuevaEtiqueta();

        for (int i = 0; i < orden.size() - 1; i++) {
            for (NodoEstructura clase : implementaciones.get(orden.get(i))) {
                emitir("if==", idClase, String.valueOf(idsDeClase.get(clase.getNombre())), etiquetas.get(i));
            }
        }
        emitir("goto", null, null, etiquetas.get(orden.size() - 1));

        for (int i = 0; i < orden.size(); i++) {
            etiqueta(etiquetas.get(i));
            NodoFuncion funcion = orden.get(i);
            emitirLlamadaDirecta(etiquetaMetodo(duenas.get(funcion), funcion), receptor, argumentos, resultado);
            emitir("goto", null, null, etFin);
        }
        etiqueta(etFin);
        return resultado;
    }

    private MetodoImpl implementacion(NodoEstructura clase, NodoFuncion metodo) {
        for (NodoEstructura actual : ascendencia(clase)) {
            for (NodoFuncion candidato : actual.getMetodos()) {
                if (candidato.getNombre().equals(metodo.getNombre())
                        && mismaFirma(candidato.getParametros(), metodo.getParametros())) {
                    return new MetodoImpl(actual, candidato);
                }
            }
        }
        return null;
    }

    @Override
    public String visitar(NodoNuevoObjeto nodo) {
        NodoEstructura definicion = definiciones.get(nodo.getNombreClase());
        if (definicion == null) {
            return "0";
        }
        if (!esClase(definicion)) {
            return generarInstanciacionEstructura(nodo, nuevoTemp());
        }

        MapaDeClase mapa = obtenerMapaDeClase(nodo.getNombreClase());
        int celdas = mapa != null ? mapa.tamanoEnCeldas() : 1;
        NodoTipo tipoObjeto = new NodoTipo(nodo.getLinea(), nodo.getColumna(), nodo.getNombreClase(), 0);
        String objeto = nuevoTemp(tipoObjeto);
        emitir("alloc", String.valueOf(celdas), null, objeto);
        emitir("store", "0", String.valueOf(idsDeClase.get(nodo.getNombreClase())), objeto);

        List<String> argumentos = new ArrayList<>();
        for (NodoAST argumento : nodo.getArgumentos()) {
            argumentos.add(generarExpresion(argumento));
        }
        NodoFuncion constructor = resolverConstructor(definicion, nodo.getArgumentos());
        emitirLlamadaDirecta(etiquetaConstructor(definicion, constructor), objeto, argumentos, nuevoTemp(TIPO_ENTERO));
        return objeto;
    }

    private String generarInstanciacionEstructura(NodoNuevoObjeto nodo, String nombreObjeto) {
        MapaDeClase mapa = obtenerMapaDeClase(nodo.getNombreClase());
        int cantidadDeCeldas = mapa != null ? mapa.tamanoEnCeldas() : 1;
        registrarTipo(nombreObjeto, new NodoTipo(nodo.getLinea(), nodo.getColumna(), nodo.getNombreClase(), 0),
                cantidadDeCeldas);
        emitirReserva(nombreObjeto, cantidadDeCeldas);
        return nombreObjeto;
    }

    @Override
    public String visitar(NodoNuevoArreglo nodo) {
        String nombre = nuevoTemp();
        List<String> tamanos = new ArrayList<>();
        for (NodoAST dimension : nodo.getDimensiones()) {
            tamanos.add(generarExpresion(dimension));
        }
        registrarDimensiones(nombre, tamanos);
        NodoTipo tipoArreglo = new NodoTipo(nodo.getLinea(), nodo.getColumna(), nodo.getTipoBase(), tamanos.size());
        int total = calcularTamanoTotal(tamanos, celdasPorElemento(nodo.getTipoBase()));
        registrarTipo(nombre, tipoArreglo, total);
        emitir("alloc", String.valueOf(total), null, nombre);
        inicializarArregloEnCero(nombre, total);
        return nombre;
    }

    @Override
    public String visitar(NodoLecturaEntrada nodo) {
        NodoTipo tipo = tipoDestinoActual.peek();
        String mnemonico = mnemonicoScan(tipo);
        String temp = nuevoTemp(tipo != null ? tipo : TIPO_CADENA);
        emitir(mnemonico, null, null, temp);
        return temp;
    }
}
