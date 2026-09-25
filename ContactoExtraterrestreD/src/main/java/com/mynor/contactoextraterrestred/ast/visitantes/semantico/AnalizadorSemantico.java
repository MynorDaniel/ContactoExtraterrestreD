/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mynor.contactoextraterrestred.ast.visitantes.semantico;

import com.mynor.contactoextraterrestred.ast.*;
import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mynordma
 */
public class AnalizadorSemantico implements VisitanteAST<Object> {

    private final Entorno entorno = new Entorno();
    private final List<String> errores = new ArrayList<>();

    private int profundidadBucle = 0;
    private int profundidadSwitch = 0;
    private boolean dentroDeFuncion = false;
    private NodoTipo tipoRetornoActual = null;
    private String archivoActual = "";

    public record ArchivoPrograma(String nombre, NodoPrograma programa) {

    }

    private static final String ENTERO = "ENTERO";
    private static final String DECIMAL = "DECIMAL";
    private static final String CADENA = "CADENA";
    private static final String CARACTER = "CARACTER";
    private static final String BOOLEANO = "BOOLEANO";
    private static final String VOID = "VOID";

    private static final NodoTipo TIPO_BOOLEANO = new NodoTipo(0, 0, "bool", 0);
    private static final NodoTipo TIPO_CADENA = new NodoTipo(0, 0, "cadena", 0);

    public List<String> getErrores() {
        return errores;
    }

    public boolean tieneErrores() {
        return !errores.isEmpty();
    }

    private void error(NodoAST nodo, String mensaje) {
        String prefijo = archivoActual.isEmpty() ? "" : "[" + archivoActual + "] ";
        errores.add(prefijo + "Línea " + nodo.getLinea() + ", columna " + nodo.getColumna() + ": " + mensaje);
    }

    @Override
    public Object visitar(NodoPrograma nodo) {
        analizarProgramas(new ArchivoPrograma("", nodo));
        return null;
    }

    public void analizarProgramas(ArchivoPrograma... archivos) {
        for (ArchivoPrograma archivo : archivos) {
            archivoActual = archivo.nombre();
            NodoPrograma nodo = archivo.programa();
            for (NodoEstructura estructura : nodo.getEstructuras()) {
                if (!entorno.declarar(Simbolo.estructura(estructura.getNombre(), estructura))) {
                    error(estructura, "Estructura ya declarada: " + estructura.getNombre());
                }
            }
            if (nodo.getClaseUnica() != null) {
                NodoEstructura clase = nodo.getClaseUnica();
                if (!entorno.declarar(Simbolo.estructura(clase.getNombre(), clase))) {
                    error(clase, "Clase ya declarada: " + clase.getNombre());
                }
            }
        }

        for (ArchivoPrograma archivo : archivos) {
            archivoActual = archivo.nombre();
            for (NodoFuncion funcion : archivo.programa().getFunciones()) {
                if (!entorno.declarar(Simbolo.funcion(funcion.getNombre(), funcion.getTipoRetorno(), tiposParametros(funcion)))) {
                    error(funcion, "Función ya declarada: " + funcion.getNombre());
                }
            }
        }

        for (ArchivoPrograma archivo : archivos) {
            archivoActual = archivo.nombre();
            for (NodoDeclaracion variable : archivo.programa().getVariablesGlobales()) {
                variable.aceptar(this);
            }
        }

        for (ArchivoPrograma archivo : archivos) {
            archivoActual = archivo.nombre();
            NodoPrograma nodo = archivo.programa();
            for (NodoEstructura estructura : nodo.getEstructuras()) {
                estructura.aceptar(this);
            }
            if (nodo.getClaseUnica() != null) {
                nodo.getClaseUnica().aceptar(this);
            }
        }

        for (ArchivoPrograma archivo : archivos) {
            archivoActual = archivo.nombre();
            for (NodoFuncion funcion : archivo.programa().getFunciones()) {
                funcion.aceptar(this);
            }
        }

        for (ArchivoPrograma archivo : archivos) {
            archivoActual = archivo.nombre();
            entorno.entrarAmbito();
            for (NodoAST sentencia : archivo.programa().getSentenciasPrincipales()) {
                sentencia.aceptar(this);
            }
            entorno.salirAmbito();
        }

        archivoActual = "";
    }

    @Override
    public Object visitar(NodoImportacion nodo) {
        return null;
    }

    @Override
    public Object visitar(NodoEstructura nodo) {
        entorno.entrarAmbito();

        for (NodoAtributo atributo : nodo.getAtributos()) {
            atributo.aceptar(this);
        }

        for (NodoFuncion metodo : nodo.getMetodos()) {
            if (!entorno.declarar(Simbolo.funcion(metodo.getNombre(), metodo.getTipoRetorno(), tiposParametros(metodo)))) {
                error(metodo, "Método ya declarado: " + metodo.getNombre());
            }
        }

        for (NodoFuncion constructor : nodo.getConstructores()) {
            constructor.aceptar(this);
        }
        for (NodoFuncion metodo : nodo.getMetodos()) {
            metodo.aceptar(this);
        }

        entorno.salirAmbito();
        return null;
    }

    @Override
    public Object visitar(NodoAtributo nodo) {
        if (!tipoConocido(nodo.getTipo())) {
            error(nodo, "Tipo desconocido: " + nodo.getTipo().getNombre());
        }
        for (NodoDimension dimension : nodo.getDimensiones()) {
            dimension.aceptar(this);
        }
        NodoTipo tipoDeclarado = tipoEfectivo(nodo.getTipo(), nodo.getDimensiones());
        if (nodo.getValorInicial() != null) {
            if (nodo.getValorInicial() instanceof NodoListaExpresiones) {
                nodo.getValorInicial().aceptar(this);
            } else {
                NodoTipo tipoValor = (NodoTipo) nodo.getValorInicial().aceptar(this);
                if (!asignable(tipoDeclarado, tipoValor)) {
                    error(nodo, "No se puede asignar " + describir(tipoValor) + " a " + describir(tipoDeclarado));
                }
            }
        }
        if (!entorno.declarar(Simbolo.variable(nodo.getNombre(), tipoDeclarado))) {
            error(nodo, "Atributo ya declarado: " + nodo.getNombre());
        }
        return null;
    }

    @Override
    public Object visitar(NodoFuncion nodo) {
        if (nodo.getTipoRetorno() != null && !tipoConocido(nodo.getTipoRetorno())) {
            error(nodo, "Tipo de retorno desconocido: " + nodo.getTipoRetorno().getNombre());
        }

        boolean dentroDeFuncionAnterior = dentroDeFuncion;
        NodoTipo tipoRetornoAnterior = tipoRetornoActual;
        dentroDeFuncion = true;
        NodoTipo tr = nodo.getTipoRetorno();
        tipoRetornoActual = (tr != null && VOID.equals(categoria(tr.getNombre()))) ? null : tr;

        entorno.entrarAmbito();
        for (NodoParametro parametro : nodo.getParametros()) {
            parametro.aceptar(this);
        }
        for (NodoAST sentencia : nodo.getCuerpo()) {
            sentencia.aceptar(this);
        }
        entorno.salirAmbito();

        dentroDeFuncion = dentroDeFuncionAnterior;
        tipoRetornoActual = tipoRetornoAnterior;
        return null;
    }

    @Override
    public Object visitar(NodoParametro nodo) {
        if (!tipoConocido(nodo.getTipo())) {
            error(nodo, "Tipo desconocido: " + nodo.getTipo().getNombre());
        }
        if (!entorno.declarar(Simbolo.variable(nodo.getNombre(), nodo.getTipo()))) {
            error(nodo, "Parámetro ya declarado: " + nodo.getNombre());
        }
        return null;
    }

    @Override
    public Object visitar(NodoTipo nodo) {
        return nodo;
    }

    @Override
    public Object visitar(NodoDimension nodo) {
        NodoTipo tipoTamano = (NodoTipo) nodo.getTamano().aceptar(this);
        if (tipoTamano != null && !ENTERO.equals(categoria(tipoTamano.getNombre()))) {
            error(nodo, "El tamaño de una dimensión debe ser un entero");
        }
        return null;
    }

    @Override
    public Object visitar(NodoDeclaracion nodo) {
        if (!tipoConocido(nodo.getTipo())) {
            error(nodo, "Tipo desconocido: " + nodo.getTipo().getNombre());
        }
        for (NodoDimension dimension : nodo.getDimensiones()) {
            dimension.aceptar(this);
        }
        NodoTipo tipoDeclarado = tipoEfectivo(nodo.getTipo(), nodo.getDimensiones());
        if (nodo.getInicializador() != null) {
            if (nodo.getInicializador() instanceof NodoListaExpresiones) {
                nodo.getInicializador().aceptar(this);
            } else {
                NodoTipo tipoValor = (NodoTipo) nodo.getInicializador().aceptar(this);
                if (!asignable(tipoDeclarado, tipoValor)) {
                    error(nodo, "No se puede asignar " + describir(tipoValor) + " a " + describir(tipoDeclarado));
                }
            }
        }
        if (!entorno.declarar(Simbolo.variable(nodo.getNombre(), tipoDeclarado))) {
            error(nodo, "Variable ya declarada: " + nodo.getNombre());
        }
        return null;
    }

    @Override
    public Object visitar(NodoListaExpresiones nodo) {
        for (NodoAST elemento : nodo.getElementos()) {
            elemento.aceptar(this);
        }
        return null;
    }

    @Override
    public Object visitar(NodoBloque nodo) {
        entorno.entrarAmbito();
        for (NodoAST sentencia : nodo.getSentencias()) {
            sentencia.aceptar(this);
        }
        entorno.salirAmbito();
        return null;
    }

    @Override
    public Object visitar(NodoAsignacion nodo) {
        NodoTipo tipoDestino = (NodoTipo) nodo.getDestino().aceptar(this);
        NodoTipo tipoValor = (NodoTipo) nodo.getValor().aceptar(this);
        if (tipoDestino == null || tipoValor == null) {
            return null;
        }
        if ("=".equals(nodo.getOperador())) {
            if (!asignable(tipoDestino, tipoValor)) {
                error(nodo, "No se puede asignar " + describir(tipoValor) + " a " + describir(tipoDestino));
            }
        } else {
            String operadorBase = nodo.getOperador().substring(0, 1);
            NodoTipo resultado = tipoOperacionBinaria(operadorBase, tipoDestino, tipoValor, nodo);
            if (!asignable(tipoDestino, resultado)) {
                error(nodo, "El resultado de '" + nodo.getOperador() + "' no es asignable a " + describir(tipoDestino));
            }
        }
        return null;
    }

    @Override
    public Object visitar(NodoIncrementoDecremento nodo) {
        NodoTipo tipo = (NodoTipo) nodo.getDestino().aceptar(this);
        if (tipo != null && !esNumerico(tipo)) {
            error(nodo, "'" + nodo.getOperador() + "' solo aplica a valores numéricos");
        }
        return null;
    }

    @Override
    public Object visitar(NodoIf nodo) {
        NodoTipo tipoCondicion = (NodoTipo) nodo.getCondicion().aceptar(this);
        if (tipoCondicion != null && !esBooleano(tipoCondicion)) {
            error(nodo, "La condición del if debe ser booleana");
        }
        entorno.entrarAmbito();
        for (NodoAST sentencia : nodo.getCuerpoSi()) {
            sentencia.aceptar(this);
        }
        entorno.salirAmbito();

        for (NodoSinoSi rama : nodo.getRamasSinoSi()) {
            rama.aceptar(this);
        }

        if (nodo.getCuerpoSino() != null) {
            entorno.entrarAmbito();
            for (NodoAST sentencia : nodo.getCuerpoSino()) {
                sentencia.aceptar(this);
            }
            entorno.salirAmbito();
        }
        return null;
    }

    @Override
    public Object visitar(NodoSinoSi nodo) {
        NodoTipo tipoCondicion = (NodoTipo) nodo.getCondicion().aceptar(this);
        if (tipoCondicion != null && !esBooleano(tipoCondicion)) {
            error(nodo, "La condición del sino-si debe ser booleana");
        }
        entorno.entrarAmbito();
        for (NodoAST sentencia : nodo.getCuerpo()) {
            sentencia.aceptar(this);
        }
        entorno.salirAmbito();
        return null;
    }

    @Override
    public Object visitar(NodoSwitch nodo) {
        NodoTipo tipoExpresion = (NodoTipo) nodo.getExpresion().aceptar(this);
        profundidadSwitch++;
        entorno.entrarAmbito();

        for (NodoCaso caso : nodo.getCasos()) {
            NodoTipo tipoCaso = (NodoTipo) caso.aceptar(this);
            if (tipoExpresion != null && tipoCaso != null && !mismaCategoria(tipoExpresion, tipoCaso)) {
                error(caso, "El tipo del caso no coincide con el tipo del switch");
            }
        }
        if (nodo.getCasoPorDefecto() != null) {
            for (NodoAST sentencia : nodo.getCasoPorDefecto()) {
                sentencia.aceptar(this);
            }
        }

        entorno.salirAmbito();
        profundidadSwitch--;
        return null;
    }

    @Override
    public Object visitar(NodoCaso nodo) {
        NodoTipo tipoValor = (NodoTipo) nodo.getValor().aceptar(this);
        for (NodoAST sentencia : nodo.getCuerpo()) {
            sentencia.aceptar(this);
        }
        return tipoValor;
    }

    @Override
    public Object visitar(NodoFor nodo) {
        entorno.entrarAmbito();
        if (nodo.getInicializacion() != null) {
            nodo.getInicializacion().aceptar(this);
        }
        if (nodo.getCondicion() != null) {
            NodoTipo tipoCondicion = (NodoTipo) nodo.getCondicion().aceptar(this);
            if (tipoCondicion != null && !esBooleano(tipoCondicion)) {
                error(nodo, "La condición del for debe ser booleana");
            }
        }
        if (nodo.getActualizacion() != null) {
            nodo.getActualizacion().aceptar(this);
        }
        profundidadBucle++;
        for (NodoAST sentencia : nodo.getCuerpo()) {
            sentencia.aceptar(this);
        }
        profundidadBucle--;
        entorno.salirAmbito();
        return null;
    }

    @Override
    public Object visitar(NodoWhile nodo) {
        NodoTipo tipoCondicion = (NodoTipo) nodo.getCondicion().aceptar(this);
        if (tipoCondicion != null && !esBooleano(tipoCondicion)) {
            error(nodo, "La condición del while debe ser booleana");
        }
        entorno.entrarAmbito();
        profundidadBucle++;
        for (NodoAST sentencia : nodo.getCuerpo()) {
            sentencia.aceptar(this);
        }
        profundidadBucle--;
        entorno.salirAmbito();
        return null;
    }

    @Override
    public Object visitar(NodoDoWhile nodo) {
        entorno.entrarAmbito();
        profundidadBucle++;
        for (NodoAST sentencia : nodo.getCuerpo()) {
            sentencia.aceptar(this);
        }
        profundidadBucle--;
        entorno.salirAmbito();
        NodoTipo tipoCondicion = (NodoTipo) nodo.getCondicion().aceptar(this);
        if (tipoCondicion != null && !esBooleano(tipoCondicion)) {
            error(nodo, "La condición del do-while debe ser booleana");
        }
        return null;
    }

    @Override
    public Object visitar(NodoBreak nodo) {
        if (profundidadBucle == 0 && profundidadSwitch == 0) {
            error(nodo, "'romper'/'break' usado fuera de un bucle o switch");
        }
        return null;
    }

    @Override
    public Object visitar(NodoContinue nodo) {
        if (profundidadBucle == 0) {
            error(nodo, "'continuar'/'continue' usado fuera de un bucle");
        }
        return null;
    }

    @Override
    public Object visitar(NodoReturn nodo) {
        NodoTipo tipoValor = nodo.getValor() != null ? (NodoTipo) nodo.getValor().aceptar(this) : null;
        if (!dentroDeFuncion) {
            error(nodo, "'retornar'/'return' usado fuera de una función");
        } else if (tipoRetornoActual == null) {
            if (tipoValor != null) {
                error(nodo, "Esta función/método/constructor no debe devolver un valor");
            }
        } else if (tipoValor == null) {
            error(nodo, "Se esperaba un valor de retorno de tipo " + describir(tipoRetornoActual));
        } else if (!asignable(tipoRetornoActual, tipoValor)) {
            error(nodo, "El valor devuelto (" + describir(tipoValor) + ") no coincide con el tipo de retorno "
                    + describir(tipoRetornoActual));
        }
        return null;
    }

    @Override
    public Object visitar(NodoImprimir nodo) {
        for (NodoAST expresion : nodo.getExpresiones()) {
            expresion.aceptar(this);
        }
        return null;
    }

    @Override
    public Object visitar(NodoLeer nodo) {
        if (nodo.getDestino() != null) {
            nodo.getDestino().aceptar(this);
        }
        return null;
    }

    @Override
    public Object visitar(NodoExpresionSentencia nodo) {
        nodo.getExpresion().aceptar(this);
        return null;
    }

    @Override
    public Object visitar(NodoBinaria nodo) {
        NodoTipo izquierda = (NodoTipo) nodo.getIzquierda().aceptar(this);
        NodoTipo derecha = (NodoTipo) nodo.getDerecha().aceptar(this);
        return tipoOperacionBinaria(nodo.getOperador(), izquierda, derecha, nodo);
    }

    @Override
    public Object visitar(NodoUnaria nodo) {
        NodoTipo tipo = (NodoTipo) nodo.getOperando().aceptar(this);
        if (tipo == null) {
            return null;
        }
        if ("!".equals(nodo.getOperador())) {
            if (!esBooleano(tipo)) {
                error(nodo, "'!' solo aplica a valores booleanos");
            }
            return TIPO_BOOLEANO;
        }
        if (!esNumerico(tipo)) {
            error(nodo, "'" + nodo.getOperador() + "' unario solo aplica a valores numéricos");
            return null;
        }
        return tipo;
    }

    @Override
    public Object visitar(NodoTernaria nodo) {
        NodoTipo tipoCondicion = (NodoTipo) nodo.getCondicion().aceptar(this);
        if (tipoCondicion != null && !esBooleano(tipoCondicion)) {
            error(nodo, "La condición del operador ternario debe ser booleana");
        }
        NodoTipo tipoVerdadero = (NodoTipo) nodo.getSiVerdadero().aceptar(this);
        NodoTipo tipoFalso = (NodoTipo) nodo.getSiFalso().aceptar(this);
        if (tipoVerdadero == null || tipoFalso == null) {
            return null;
        }
        if (asignable(tipoVerdadero, tipoFalso)) {
            return tipoVerdadero;
        }
        if (asignable(tipoFalso, tipoVerdadero)) {
            return tipoFalso;
        }
        error(nodo, "Las dos ramas del operador ternario tienen tipos incompatibles");
        return null;
    }

    @Override
    public Object visitar(NodoLiteral nodo) {
        return switch (nodo.getTipo()) {
            case ENTERO ->
                new NodoTipo(nodo.getLinea(), nodo.getColumna(), "entero", 0);
            case DECIMAL ->
                new NodoTipo(nodo.getLinea(), nodo.getColumna(), "flotante", 0);
            case CADENA ->
                new NodoTipo(nodo.getLinea(), nodo.getColumna(), "cadena", 0);
            case CARACTER ->
                new NodoTipo(nodo.getLinea(), nodo.getColumna(), "caracter", 0);
            case BOOLEANO ->
                new NodoTipo(nodo.getLinea(), nodo.getColumna(), "bool", 0);
            default ->
                null;
        };
    }

    @Override
    public Object visitar(NodoIdentificador nodo) {
        Simbolo simbolo = entorno.buscar(nodo.getNombre());
        if (simbolo == null) {
            error(nodo, "Variable no declarada: " + nodo.getNombre());
            return null;
        }
        if (simbolo.esEstructura) {
            error(nodo, nodo.getNombre() + " es un tipo, no una variable");
            return null;
        }
        if (simbolo.esFuncion) {
            error(nodo, nodo.getNombre() + " es una función; debe invocarse con ()");
            return null;
        }
        return simbolo.tipo;
    }

    @Override
    public Object visitar(NodoAccesoArreglo nodo) {
        NodoTipo tipoBase = (NodoTipo) nodo.getBase().aceptar(this);
        NodoTipo tipoIndice = (NodoTipo) nodo.getIndice().aceptar(this);
        if (tipoIndice != null && !esNumerico(tipoIndice)) {
            error(nodo, "El índice de un arreglo debe ser numérico");
        }
        if (tipoBase == null) {
            return null;
        }
        if (tipoBase.getNivelesArreglo() == 0) {
            error(nodo, describir(tipoBase) + " no es un arreglo");
            return null;
        }
        return new NodoTipo(tipoBase.getLinea(), tipoBase.getColumna(),
                tipoBase.getNombre(), tipoBase.getNivelesArreglo() - 1);
    }

    @Override
    public Object visitar(NodoAccesoCampo nodo) {
        NodoTipo tipoBase = (NodoTipo) nodo.getBase().aceptar(this);
        if (tipoBase == null) {
            return null;
        }
        if (tipoBase.getNivelesArreglo() > 0 || categoria(tipoBase.getNombre()) != null) {
            error(nodo, "Solo se puede acceder a un campo de una estructura/clase, no de " + describir(tipoBase));
            return null;
        }
        Simbolo simboloTipo = entorno.buscar(tipoBase.getNombre());
        if (simboloTipo == null || !simboloTipo.esEstructura) {
            error(nodo, "Tipo desconocido: " + tipoBase.getNombre());
            return null;
        }
        for (NodoAtributo atributo : simboloTipo.estructura.getAtributos()) {
            if (atributo.getNombre().equals(nodo.getCampo())) {
                return tipoEfectivo(atributo.getTipo(), atributo.getDimensiones());
            }
        }
        error(nodo, tipoBase.getNombre() + " no tiene el campo " + nodo.getCampo());
        return null;
    }

    @Override
    public Object visitar(NodoLlamada nodo) {
        List<NodoTipo> tiposArgumentos = new ArrayList<>();
        for (NodoAST argumento : nodo.getArgumentos()) {
            tiposArgumentos.add((NodoTipo) argumento.aceptar(this));
        }

        Simbolo funcion;
        String nombreLlamada;

        switch (nodo.getBase()) {
            case NodoIdentificador nodoIdentificador -> {
                nombreLlamada = nodoIdentificador.getNombre();
                funcion = entorno.buscar(nombreLlamada);
            }
            case NodoAccesoCampo campo -> {
                nombreLlamada = campo.getCampo();
                funcion = null;
                NodoTipo tipoObjeto = (NodoTipo) campo.getBase().aceptar(this);
                if (tipoObjeto != null) {
                    Simbolo simboloTipo = entorno.buscar(tipoObjeto.getNombre());
                    if (simboloTipo != null && simboloTipo.esEstructura) {
                        for (NodoFuncion metodo : simboloTipo.estructura.getMetodos()) {
                            if (metodo.getNombre().equals(nombreLlamada)) {
                                funcion = Simbolo.funcion(metodo.getNombre(), metodo.getTipoRetorno(), tiposParametros(metodo));
                                break;
                            }
                        }
                        if (funcion == null) {
                            error(nodo, tipoObjeto.getNombre() + " no tiene el método " + nombreLlamada);
                        }
                    } else {
                        error(nodo, "No se puede invocar un método sobre " + describir(tipoObjeto));
                    }
                }
            }
            default -> {
                nodo.getBase().aceptar(this);
                error(nodo, "Expresión no invocable");
                return null;
            }
        }

        if (funcion == null) {
            if (nodo.getBase() instanceof NodoIdentificador) {
                error(nodo, "Función no declarada: " + nombreLlamada);
            }
            return null;
        }
        if (!funcion.esFuncion) {
            error(nodo, nombreLlamada + " no es invocable");
            return null;
        }
        if (funcion.parametros.size() != tiposArgumentos.size()) {
            error(nodo, "Se esperaban " + funcion.parametros.size() + " argumento(s) para " + nombreLlamada
                    + ", se recibieron " + tiposArgumentos.size());
            return funcion.tipo;
        }
        for (int i = 0; i < funcion.parametros.size(); i++) {
            if (!asignable(funcion.parametros.get(i), tiposArgumentos.get(i))) {
                error(nodo, "El argumento " + (i + 1) + " de " + nombreLlamada + " no coincide en tipo");
            }
        }
        return funcion.tipo;
    }

    @Override
    public Object visitar(NodoNuevoObjeto nodo) {
        List<NodoTipo> tiposArgumentos = new ArrayList<>();
        for (NodoAST argumento : nodo.getArgumentos()) {
            tiposArgumentos.add((NodoTipo) argumento.aceptar(this));
        }

        Simbolo simboloTipo = entorno.buscar(nodo.getNombreClase());
        if (simboloTipo == null || !simboloTipo.esEstructura) {
            error(nodo, "Clase no declarada: " + nodo.getNombreClase());
            return new NodoTipo(nodo.getLinea(), nodo.getColumna(), nodo.getNombreClase(), 0);
        }

        List<NodoFuncion> constructores = simboloTipo.estructura.getConstructores();
        if (!constructores.isEmpty()) {
            NodoFuncion elegido = null;
            for (NodoFuncion constructor : constructores) {
                if (constructor.getParametros().size() == tiposArgumentos.size()) {
                    elegido = constructor;
                    break;
                }
            }
            if (elegido == null) {
                error(nodo, "No hay un constructor de " + nodo.getNombreClase() + " con "
                        + tiposArgumentos.size() + " argumento(s)");
            } else {
                for (int i = 0; i < elegido.getParametros().size(); i++) {
                    if (!asignable(elegido.getParametros().get(i).getTipo(), tiposArgumentos.get(i))) {
                        error(nodo, "El argumento " + (i + 1) + " del constructor de " + nodo.getNombreClase()
                                + " no coincide en tipo");
                    }
                }
            }
        }

        return new NodoTipo(nodo.getLinea(), nodo.getColumna(), nodo.getNombreClase(), 0);
    }

    @Override
    public Object visitar(NodoNuevoArreglo nodo) {
        for (NodoAST dimension : nodo.getDimensiones()) {
            NodoTipo tipoDimension = (NodoTipo) dimension.aceptar(this);
            if (tipoDimension != null && !esNumerico(tipoDimension)) {
                error(nodo, "El tamaño de un arreglo debe ser numérico");
            }
        }
        if (!tipoConocido(new NodoTipo(nodo.getLinea(), nodo.getColumna(), nodo.getTipoBase(), 0))) {
            error(nodo, "Tipo desconocido: " + nodo.getTipoBase());
        }
        return new NodoTipo(nodo.getLinea(), nodo.getColumna(), nodo.getTipoBase(), nodo.getDimensiones().size());
    }

    @Override
    public Object visitar(NodoLecturaEntrada nodo) {
        return null;
    }

    private String categoria(String nombre) {
        return switch (nombre) {
            case "entero", "int", "numerus" ->
                ENTERO;
            case "flotante", "double", "decimalis" ->
                DECIMAL;
            case "cadena", "String", "textum" ->
                CADENA;
            case "caracter", "char", "littera" ->
                CARACTER;
            case "bool", "boolean" ->
                BOOLEANO;
            case "void" ->
                VOID;
            default ->
                null;
        };
    }

    private boolean esNumerico(NodoTipo tipo) {
        return tipo.getNivelesArreglo() == 0
                && (ENTERO.equals(categoria(tipo.getNombre())) || DECIMAL.equals(categoria(tipo.getNombre())));
    }

    private boolean esBooleano(NodoTipo tipo) {
        return tipo.getNivelesArreglo() == 0 && BOOLEANO.equals(categoria(tipo.getNombre()));
    }

    private boolean esCadena(NodoTipo tipo) {
        return tipo.getNivelesArreglo() == 0 && CADENA.equals(categoria(tipo.getNombre()));
    }

    private boolean mismaCategoria(NodoTipo a, NodoTipo b) {
        if (a.getNivelesArreglo() != b.getNivelesArreglo()) {
            return false;
        }
        String catA = categoria(a.getNombre());
        String catB = categoria(b.getNombre());
        if (catA == null && catB == null) {
            return a.getNombre().equals(b.getNombre());
        }
        return catA != null && catA.equals(catB);
    }

    private boolean asignable(NodoTipo destino, NodoTipo origen) {
        if (destino == null || origen == null) {
            return true;
        }
        if (destino.getNivelesArreglo() != origen.getNivelesArreglo()) {
            return false;
        }
        String catD = categoria(destino.getNombre());
        String catO = categoria(origen.getNombre());
        if (catD == null && catO == null) {
            return destino.getNombre().equals(origen.getNombre());
        }
        if (catD == null || catO == null) {
            return false;
        }
        if (catD.equals(catO)) {
            return true;
        }
        return DECIMAL.equals(catD) && ENTERO.equals(catO);
    }

    private boolean tipoConocido(NodoTipo tipo) {
        if (categoria(tipo.getNombre()) != null) {
            return true;
        }
        Simbolo simbolo = entorno.buscar(tipo.getNombre());
        return simbolo != null && simbolo.esEstructura;
    }

    private String describir(NodoTipo tipo) {
        StringBuilder texto = new StringBuilder(tipo.getNombre());
        for (int i = 0; i < tipo.getNivelesArreglo(); i++) {
            texto.append("[]");
        }
        return texto.toString();
    }

    private List<NodoTipo> tiposParametros(NodoFuncion funcion) {
        List<NodoTipo> tipos = new ArrayList<>();
        for (NodoParametro parametro : funcion.getParametros()) {
            tipos.add(parametro.getTipo());
        }
        return tipos;
    }

    private NodoTipo tipoEfectivo(NodoTipo tipo, List<NodoDimension> dimensiones) {
        if (dimensiones.isEmpty()) {
            return tipo;
        }
        return new NodoTipo(tipo.getLinea(), tipo.getColumna(), tipo.getNombre(),
                tipo.getNivelesArreglo() + dimensiones.size());
    }

    private NodoTipo tipoNumericoResultante(NodoTipo a, NodoTipo b) {
        if (DECIMAL.equals(categoria(a.getNombre()))) {
            return a;
        }
        if (DECIMAL.equals(categoria(b.getNombre()))) {
            return b;
        }
        return a;
    }

    private NodoTipo tipoOperacionBinaria(String operador, NodoTipo izquierda, NodoTipo derecha, NodoAST nodo) {
        if (izquierda == null || derecha == null) {
            return null;
        }
        switch (operador) {
            case "+" -> {
                if (esCadena(izquierda) || esCadena(derecha)) {
                    return TIPO_CADENA;
                }
                if (esNumerico(izquierda) && esNumerico(derecha)) {
                    return tipoNumericoResultante(izquierda, derecha);
                }
                error(nodo, "Operandos inválidos para '+': " + describir(izquierda) + " y " + describir(derecha));
                return null;
            }
            case "-", "*", "/", "%" -> {
                if (esNumerico(izquierda) && esNumerico(derecha)) {
                    return tipoNumericoResultante(izquierda, derecha);
                }
                error(nodo, "Los operandos de '" + operador + "' deben ser numéricos");
                return null;
            }
            case "<", ">", "<=", ">=" -> {
                if (!esNumerico(izquierda) || !esNumerico(derecha)) {
                    error(nodo, "Los operandos de '" + operador + "' deben ser numéricos");
                }
                return TIPO_BOOLEANO;
            }
            case "==", "!=", "=" -> {
                if (!mismaCategoria(izquierda, derecha) && !(esNumerico(izquierda) && esNumerico(derecha))) {
                    error(nodo, "No se pueden comparar " + describir(izquierda) + " y " + describir(derecha));
                }
                return TIPO_BOOLEANO;
            }
            case "&&", "||" -> {
                if (!esBooleano(izquierda) || !esBooleano(derecha)) {
                    error(nodo, "Los operandos de '" + operador + "' deben ser booleanos");
                }
                return TIPO_BOOLEANO;
            }
            default -> {
                return null;
            }
        }
    }
}
