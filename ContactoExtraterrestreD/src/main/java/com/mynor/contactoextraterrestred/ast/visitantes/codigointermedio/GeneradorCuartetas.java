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

    private record Direccion(String variable, String posicion) {

        boolean esVariableCompleta() {
            return posicion == null;
        }
    }

    private static final Set<String> RELACIONALES = Set.of("<", ">", "<=", ">=", "==", "!=", "=");
    private static final int TAMANO_ARREGLO_DESCONOCIDO = 256;

    private static final NodoTipo TIPO_ENTERO = new NodoTipo(0, 0, "entero", 0);
    private static final NodoTipo TIPO_DECIMAL = new NodoTipo(0, 0, "flotante", 0);
    private static final NodoTipo TIPO_CADENA = new NodoTipo(0, 0, "cadena", 0);
    private static final NodoTipo TIPO_CARACTER = new NodoTipo(0, 0, "caracter", 0);
    private static final NodoTipo TIPO_BOOLEANO = new NodoTipo(0, 0, "bool", 0);

    private final List<Cuarteto> codigo = new ArrayList<>();
    private int contadorTemp = 0;
    private int contadorEtiqueta = 0;

    private final Map<String, NodoEstructura> definiciones = new HashMap<>();
    private final Map<String, MapaDeClase> mapasDeClase = new HashMap<>();
    private final Map<String, List<String>> dimensionesArreglo = new HashMap<>();
    private final Map<String, NodoFuncion> funcionesLibres = new HashMap<>();
    private final List<String> nombresDeFunciones = new ArrayList<>();
    private final Map<String, InfoNombre> tablaNombres = new HashMap<>();
    private final Entorno tipos = new Entorno();
    private final Deque<Map<String, Direccion>> sustituciones = new ArrayDeque<>();
    private final Deque<String> etiquetasSalidaBucle = new ArrayDeque<>();
    private final Deque<String> etiquetasContinuarBucle = new ArrayDeque<>();
    private final Deque<String> etiquetaFinInline = new ArrayDeque<>();
    private final Deque<String> tempResultadoInline = new ArrayDeque<>();
    private final Deque<NodoTipo> tipoDestinoActual = new ArrayDeque<>();

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

    private void registrarDimensiones(String nombre, List<String> tamanos) {
        if (!tamanos.isEmpty()) {
            dimensionesArreglo.put(nombre, tamanos);
        }
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

    private boolean esVoid(NodoTipo tipo) {
        return tipo == null || "void".equals(tipo.getNombre());
    }

    private boolean esCompuesto(NodoTipo tipo) {
        return tipo != null && (tipo.getNivelesArreglo() > 0 || esNombreDeClase(tipo.getNombre()));
    }

    private boolean esNombreDeClase(String nombreTipo) {
        return !esCategoriaEntero(nombreTipo) && !esCategoriaDecimal(nombreTipo)
                && !esCategoriaCaracter(nombreTipo) && !esCategoriaCadena(nombreTipo)
                && !"bool".equals(nombreTipo) && !"boolean".equals(nombreTipo) && !"void".equals(nombreTipo);
    }

    private NodoTipo tipoDeExpresionSimple(NodoAST expr) {
        if (expr instanceof NodoIdentificador id) {
            Simbolo simbolo = tipos.buscar(id.getNombre());
            return simbolo == null ? null : simbolo.tipo;
        }
        if (expr instanceof NodoAccesoCampo campo) {
            NodoTipo tipoBase = tipoDeExpresionSimple(campo.getBase());
            if (tipoBase == null) {
                return null;
            }
            NodoEstructura estructura = definiciones.get(tipoBase.getNombre());
            if (estructura == null) {
                return null;
            }
            for (NodoAtributo atributo : estructura.getAtributos()) {
                if (atributo.getNombre().equals(campo.getCampo())) {
                    return tipoEfectivo(atributo.getTipo(), atributo.getDimensiones());
                }
            }
            return null;
        }
        if (expr instanceof NodoAccesoArreglo arreglo) {
            NodoTipo base = tipoDeExpresionSimple(arreglo.getBase());
            if (base == null || base.getNivelesArreglo() == 0) {
                return null;
            }
            return new NodoTipo(base.getLinea(), base.getColumna(), base.getNombre(), base.getNivelesArreglo() - 1);
        }
        return null;
    }

    private NodoFuncion buscarMetodo(NodoEstructura duena, String nombre) {
        if (duena == null) {
            return null;
        }
        for (NodoFuncion metodo : duena.getMetodos()) {
            if (metodo.getNombre().equals(nombre)) {
                return metodo;
            }
        }
        return null;
    }

    private NodoTipo tipoRetornoNormalizado(NodoTipo tipoRetorno) {
        return esVoid(tipoRetorno) ? TIPO_ENTERO : tipoRetorno;
    }

    private NodoTipo tipoDeLlamada(NodoLlamada llamada) {
        NodoFuncion funcion;
        if (llamada.getBase() instanceof NodoAccesoCampo campo) {
            NodoTipo tipoObjeto = tipoDeExpresionSimple(campo.getBase());
            NodoEstructura duena = tipoObjeto != null ? definiciones.get(tipoObjeto.getNombre()) : null;
            funcion = buscarMetodo(duena, campo.getCampo());
        } else {
            funcion = funcionesLibres.get(((NodoIdentificador) llamada.getBase()).getNombre());
        }
        return funcion == null ? TIPO_ENTERO : tipoRetornoNormalizado(funcion.getTipoRetorno());
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
        if (expr instanceof NodoTernaria ternaria) {
            NodoTipo tipo = inferirTipo(ternaria.getSiVerdadero());
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

    private int celdasPorElemento(String nombreTipoBase) {
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

    private Direccion sumarPosicion(Direccion base, String cantidad) {
        if (base.esVariableCompleta()) {
            return new Direccion(base.variable(), cantidad);
        }
        return new Direccion(base.variable(), sumar(base.posicion(), cantidad));
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
            if (!sustituciones.isEmpty()) {
                Direccion sustituida = sustituciones.peek().get(id.getNombre());
                if (sustituida != null) {
                    return sustituida;
                }
            }
            return new Direccion(id.getNombre(), null);
        }
        if (nodo instanceof NodoAccesoCampo campo) {
            Direccion base = resolverDireccion(campo.getBase());
            NodoTipo tipoBase = tipoDeExpresionSimple(campo.getBase());
            int posicionCampo = 0;
            if (tipoBase != null) {
                MapaDeClase mapa = obtenerMapaDeClase(tipoBase.getNombre());
                if (mapa != null) {
                    posicionCampo = mapa.posicionesDeCampos().getOrDefault(campo.getCampo(), 0);
                }
            }
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
            List<String> tamanos = dimensionesArreglo.getOrDefault(base.variable(), List.of());

            String posicion = null;
            for (int i = 0; i < indices.size(); i++) {
                String indice = generarExpresion(indices.get(i));
                if (posicion == null) {
                    posicion = indice;
                } else {
                    String dimensionActual = i < tamanos.size() ? tamanos.get(i) : "1";
                    posicion = sumar(multiplicar(posicion, dimensionActual), indice);
                }
            }
            int celdasElemento = celdasDelElementoFinal(actual, indices.size());
            if (celdasElemento != 1) {
                posicion = multiplicar(posicion, String.valueOf(celdasElemento));
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
        emitir("=[]", direccion.variable(), direccion.posicion(), temp);
        return temp;
    }

    private void guardar(Direccion direccion, String valor) {
        if (direccion.esVariableCompleta()) {
            emitir("=", valor, null, direccion.variable());
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

    public void generarProgramas(NodoPrograma... programas) {
        for (NodoPrograma p : programas) {
            for (NodoEstructura estructura : p.getEstructuras()) {
                definiciones.put(estructura.getNombre(), estructura);
            }
            if (p.getClaseUnica() != null) {
                definiciones.put(p.getClaseUnica().getNombre(), p.getClaseUnica());
            }
        }

        for (NodoPrograma p : programas) {
            for (NodoDeclaracion variable : p.getVariablesGlobales()) {
                variable.aceptar(this);
            }
            for (NodoAST sentencia : p.getSentenciasPrincipales()) {
                sentencia.aceptar(this);
            }
        }

        List<NodoFuncion> todasLasFunciones = new ArrayList<>();
        for (NodoPrograma p : programas) {
            for (NodoFuncion funcion : p.getFunciones()) {
                todasLasFunciones.add(funcion);
                funcionesLibres.put(funcion.getNombre(), funcion);
            }
        }
        if (!todasLasFunciones.isEmpty()) {
            String etFinPrograma = nuevaEtiqueta();
            emitir("goto", null, null, etFinPrograma);
            for (NodoFuncion funcion : todasLasFunciones) {
                funcion.aceptar(this);
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
        nombresDeFunciones.add(nodo.getNombre());
        tipos.entrarAmbito();
        etiqueta(nodo.getNombre());
        for (NodoParametro parametro : nodo.getParametros()) {
            parametro.aceptar(this);
        }
        for (NodoAST sentencia : nodo.getCuerpo()) {
            sentencia.aceptar(this);
        }
        emitir("return", null, null, null);
        tipos.salirAmbito();
        return null;
    }

    @Override
    public String visitar(NodoParametro nodo) {
        tipos.declarar(Simbolo.variable(nodo.getNombre(), nodo.getTipo()));
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
            emitirReserva(nodo.getNombre(), totalNuevo);
            return null;
        }
        if (inicializador instanceof NodoNuevoObjeto nuevoObjeto) {
            generarInstanciacion(nuevoObjeto, nodo.getNombre());
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
        if (nodo.getValor() instanceof NodoNuevoObjeto nuevoObjeto && nodo.getDestino() instanceof NodoIdentificador id) {
            generarInstanciacion(nuevoObjeto, id.getNombre());
            return null;
        }

        Direccion destino = resolverDireccion(nodo.getDestino());
        tipoDestinoActual.push(tipoDeExpresionSimple(nodo.getDestino()));
        try {
            if ("=".equals(nodo.getOperador())) {
                String valor = generarExpresion(nodo.getValor());
                guardar(destino, valor);
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
        String temp = nuevoTemp(tipo != null ? tipo : TIPO_ENTERO);
        emitir("++".equals(nodo.getOperador()) ? "+" : "-", actual, "1", temp);
        guardar(direccion, temp);
        return null;
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
        if (!etiquetaFinInline.isEmpty()) {
            if (valor != null) {
                emitir("=", valor, null, tempResultadoInline.peek());
            }
            emitir("goto", null, null, etiquetaFinInline.peek());
        } else {
            emitir("return", valor, null, null);
        }
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
            emitir("[]=", direccion.posicion(), temp, direccion.variable());
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
                "null";
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
        if (nodo.getBase() instanceof NodoAccesoCampo campo) {
            Direccion receptor = resolverDireccion(campo.getBase());
            NodoTipo tipoObjeto = tipoDeExpresionSimple(campo.getBase());
            NodoEstructura duena = tipoObjeto != null ? definiciones.get(tipoObjeto.getNombre()) : null;
            NodoFuncion metodo = buscarMetodo(duena, campo.getCampo());
            if (metodo == null) {
                return "0";
            }
            return generarInline(metodo, duena, receptor.variable(), nodo.getArgumentos());
        }

        String nombre = ((NodoIdentificador) nodo.getBase()).getNombre();
        List<String> argumentos = new ArrayList<>();
        for (NodoAST argumento : nodo.getArgumentos()) {
            argumentos.add(generarExpresion(argumento));
        }
        for (String argumento : argumentos) {
            emitir("param", argumento, null, null);
        }
        NodoFuncion funcion = funcionesLibres.get(nombre);
        String resultado = nuevoTemp(funcion == null ? TIPO_ENTERO : tipoRetornoNormalizado(funcion.getTipoRetorno()));
        emitir("call", nombre, String.valueOf(argumentos.size()), resultado);
        return resultado;
    }

    private String generarInline(NodoFuncion funcion, NodoEstructura duena, String receptorPlano,
            List<NodoAST> argumentosAST) {
        Map<String, Direccion> sustitucion = new HashMap<>();
        tipos.entrarAmbito();

        List<NodoParametro> parametros = funcion.getParametros();
        for (int i = 0; i < parametros.size(); i++) {
            String valorArgumento = generarExpresion(argumentosAST.get(i));
            NodoParametro parametro = parametros.get(i);
            String temp = nuevoTemp(parametro.getTipo());
            emitir("=", valorArgumento, null, temp);
            sustitucion.put(parametro.getNombre(), new Direccion(temp, null));
            tipos.declarar(Simbolo.variable(parametro.getNombre(), parametro.getTipo()));
        }
        if (duena != null) {
            MapaDeClase mapa = obtenerMapaDeClase(duena.getNombre());
            for (NodoAtributo atributo : duena.getAtributos()) {
                int posicion = mapa != null ? mapa.posicionesDeCampos().getOrDefault(atributo.getNombre(), 0) : 0;
                sustitucion.putIfAbsent(atributo.getNombre(), new Direccion(receptorPlano, String.valueOf(posicion)));
                tipos.declarar(Simbolo.variable(atributo.getNombre(), atributo.getTipo()));
            }
        }

        sustituciones.push(sustitucion);
        String etFin = nuevaEtiqueta();
        String resultado = nuevoTemp(tipoRetornoNormalizado(funcion.getTipoRetorno()));
        etiquetaFinInline.push(etFin);
        tempResultadoInline.push(resultado);

        for (NodoAST sentencia : funcion.getCuerpo()) {
            sentencia.aceptar(this);
        }

        etiqueta(etFin);
        tempResultadoInline.pop();
        etiquetaFinInline.pop();
        sustituciones.pop();
        tipos.salirAmbito();
        return resultado;
    }

    @Override
    public String visitar(NodoNuevoObjeto nodo) {
        return generarInstanciacion(nodo, nuevoTemp());
    }

    private String generarInstanciacion(NodoNuevoObjeto nodo, String nombreObjeto) {
        NodoEstructura definicion = definiciones.get(nodo.getNombreClase());
        MapaDeClase mapa = obtenerMapaDeClase(nodo.getNombreClase());
        int cantidadDeCeldas = mapa != null ? mapa.tamanoEnCeldas() : 1;
        registrarTipo(nombreObjeto, new NodoTipo(nodo.getLinea(), nodo.getColumna(), nodo.getNombreClase(), 0),
                cantidadDeCeldas);
        emitirReserva(nombreObjeto, cantidadDeCeldas);

        List<NodoFuncion> constructores = definicion != null ? definicion.getConstructores() : List.of();
        NodoFuncion elegido = null;
        for (NodoFuncion constructor : constructores) {
            if (constructor.getParametros().size() == nodo.getArgumentos().size()) {
                elegido = constructor;
                break;
            }
        }
        if (elegido != null) {
            generarInline(elegido, definicion, nombreObjeto, nodo.getArgumentos());
        }
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
        emitirReserva(nombre, total);
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
