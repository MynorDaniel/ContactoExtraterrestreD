/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mynor.contactoextraterrestred.ast.visitantes.semantico;

import com.mynor.contactoextraterrestred.ast.*;
import com.mynor.contactoextraterrestred.ast.visitantes.VisitanteAST;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private NodoEstructura claseActual = null;
    private NodoAST llamadaConstructorPermitida = null; 
    private final Map<String, String> paquetes = new HashMap<>(); 

    public record ArchivoPrograma(String nombre, NodoPrograma programa) {

    }

    private record MetodoEncontrado(NodoFuncion metodo, NodoEstructura propietario) {

    }

    private static final String ENTERO = "ENTERO";
    private static final String DECIMAL = "DECIMAL";
    private static final String CADENA = "CADENA";
    private static final String CARACTER = "CARACTER";
    private static final String BOOLEANO = "BOOLEANO";
    private static final String VOID = "VOID";
    private static final String NOMBRE_NULO = "null";

    private static final NodoTipo TIPO_BOOLEANO = new NodoTipo(0, 0, "bool", 0);
    private static final NodoTipo TIPO_CADENA = new NodoTipo(0, 0, "cadena", 0);

    public List<String> getErrores() {
        return errores;
    }

    public boolean tieneErrores() {
        return !errores.isEmpty();
    }

    public List<Simbolo> getSimbolosDeclarados() {
        return entorno.getSimbolosDeclarados();
    }

    private void error(NodoAST nodo, String mensaje) {
        String prefijo = archivoActual.isEmpty() ? "" : "[" + archivoActual + "] ";
        errores.add(prefijo + "Línea " + nodo.getLinea() + ", columna " + nodo.getColumna() + ": " + mensaje);
    }

    private void errorTipoDesconocido(NodoAST nodo, String nombreTipo) {
        error(nodo, "Tipo desconocido: " + nombreTipo
                + " (si se define en otro archivo, verifique que ese archivo esté importado)");
    }

    @Override
    public Object visitar(NodoPrograma nodo) {
        analizarProgramas(new ArchivoPrograma("", nodo));
        return null;
    }

    private List<NodoEstructura> clasesDe(NodoPrograma programa) {
        List<NodoEstructura> clases = programa.getClases();
        if (clases != null && !clases.isEmpty()) {
            return clases;
        }
        return programa.getClaseUnica() != null ? List.of(programa.getClaseUnica()) : List.of();
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
            String paquete = nodo.getPaquete() == null ? "" : nodo.getPaquete();
            for (NodoEstructura clase : clasesDe(nodo)) {
                paquetes.put(clase.getNombre(), paquete);
                if (!entorno.declarar(Simbolo.estructura(clase.getNombre(), clase))) {
                    error(clase, "Clase ya declarada: " + clase.getNombre());
                }
            }
        }

        for (ArchivoPrograma archivo : archivos) {
            archivoActual = archivo.nombre();
            for (NodoFuncion funcion : archivo.programa().getFunciones()) {
                List<NodoTipo> parametros = tiposParametros(funcion);
                boolean repetida = false;
                for (Simbolo existente : entorno.funcionesEnAmbitoActual(funcion.getNombre())) {
                    if (mismaFirma(existente.parametros, parametros)) {
                        repetida = true;
                        break;
                    }
                }
                if (repetida) {
                    error(funcion, "Función ya declarada: " + firma(funcion.getNombre(), parametros));
                } else {
                    entorno.declarar(Simbolo.funcion(funcion.getNombre(), funcion.getTipoRetorno(), parametros));
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
            for (NodoEstructura clase : clasesDe(nodo)) {
                clase.aceptar(this);
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
        NodoEstructura claseAnterior = claseActual;
        claseActual = nodo;

        validarHerencia(nodo);

        entorno.entrarAmbito();

        for (NodoAtributo atributo : nodo.getAtributos()) {
            atributo.aceptar(this);
        }
        declararAtributosHeredados(nodo);

        validarFirmasRepetidas(nodo);
        validarSobrescritura(nodo);
        validarConstructorPorDefecto(nodo);

        for (NodoFuncion constructor : nodo.getConstructores()) {
            constructor.aceptar(this);
        }
        for (NodoFuncion metodo : nodo.getMetodos()) {
            metodo.aceptar(this);
        }

        entorno.salirAmbito();
        claseActual = claseAnterior;
        return null;
    }

    private void validarHerencia(NodoEstructura clase) {
        String padre = clase.getClasePadre();
        if (padre == null) {
            return;
        }
        if (padre.equals(clase.getNombre())) {
            error(clase, "La clase " + clase.getNombre() + " no puede heredar de sí misma");
            return;
        }
        Simbolo simbolo = entorno.buscar(padre);
        if (simbolo == null || !simbolo.esEstructura) {
            error(clase, "Clase padre no declarada: " + padre
                    + " (si se define en otro archivo, verifique que ese archivo esté importado)");
            return;
        }
        Set<String> vistos = new HashSet<>();
        NodoEstructura actual = clase;
        while (actual != null) {
            if (!vistos.add(actual.getNombre())) {
                error(clase, "Herencia cíclica detectada en la clase " + clase.getNombre());
                return;
            }
            actual = clasePadreDe(actual);
        }
    }

    private void declararAtributosHeredados(NodoEstructura clase) {
        List<NodoEstructura> ascendencia = ascendencia(clase);
        for (int i = 1; i < ascendencia.size(); i++) {
            NodoEstructura ancestro = ascendencia.get(i);
            for (NodoAtributo atributo : ancestro.getAtributos()) {
                boolean accesible = puedeAcceder(atributo.getModificador(), ancestro);
                entorno.declarar(Simbolo.variableHeredada(atributo.getNombre(),
                        tipoEfectivo(atributo.getTipo(), atributo.getDimensiones()), ancestro, !accesible));
            }
        }
    }

    private void validarFirmasRepetidas(NodoEstructura clase) {
        List<NodoFuncion> metodos = clase.getMetodos();
        for (int i = 0; i < metodos.size(); i++) {
            for (int j = i + 1; j < metodos.size(); j++) {
                NodoFuncion a = metodos.get(i);
                NodoFuncion b = metodos.get(j);
                if (a.getNombre().equals(b.getNombre()) && mismaFirma(tiposParametros(a), tiposParametros(b))) {
                    error(b, "Método ya declarado: " + firma(b.getNombre(), tiposParametros(b)));
                }
            }
        }
        List<NodoFuncion> constructores = clase.getConstructores();
        for (int i = 0; i < constructores.size(); i++) {
            for (int j = i + 1; j < constructores.size(); j++) {
                NodoFuncion b = constructores.get(j);
                if (mismaFirma(tiposParametros(constructores.get(i)), tiposParametros(b))) {
                    error(b, "Constructor ya declarado: " + firma(b.getNombre(), tiposParametros(b)));
                }
            }
        }
    }

    private void validarSobrescritura(NodoEstructura clase) {
        for (NodoFuncion metodo : clase.getMetodos()) {
            MetodoEncontrado superior = buscarSobrescrito(clase, metodo);
            boolean marcado = metodo.isEsOverride();
            String nombreFirma = firma(metodo.getNombre(), tiposParametros(metodo));

            if (marcado && superior == null) {
                error(metodo, "@Override en '" + nombreFirma + "': ninguna superclase tiene un método con esa firma");
            } else if (!marcado && superior != null) {
                error(metodo, "El método '" + nombreFirma + "' sobrescribe a "
                        + superior.propietario().getNombre() + "." + nombreFirma + " y debe llevar @Override");
            }

            if (superior != null) {
                if (rango(metodo.getModificador()) < rango(superior.metodo().getModificador())) {
                    error(metodo, "No se puede reducir la visibilidad de '" + nombreFirma + "' (es "
                            + nombreModificador(superior.metodo().getModificador()) + " en "
                            + superior.propietario().getNombre() + ")");
                }
                if (!retornoCompatible(superior.metodo().getTipoRetorno(), metodo.getTipoRetorno())) {
                    error(metodo, "El tipo de retorno de '" + nombreFirma + "' no es compatible con el de "
                            + superior.propietario().getNombre());
                }
            }
        }
    }

    private MetodoEncontrado buscarSobrescrito(NodoEstructura clase, NodoFuncion metodo) {
        List<NodoEstructura> ascendencia = ascendencia(clase);
        List<NodoTipo> parametros = tiposParametros(metodo);
        for (int i = 1; i < ascendencia.size(); i++) {
            NodoEstructura ancestro = ascendencia.get(i);
            for (NodoFuncion candidato : ancestro.getMetodos()) {
                if (!candidato.getNombre().equals(metodo.getNombre())
                        || !mismaFirma(tiposParametros(candidato), parametros)) {
                    continue;
                }
                String mod = candidato.getModificador();
                if ("private".equals(mod)) {
                    continue; 
                }
                if ("default".equals(mod) && !paqueteDe(ancestro).equals(paqueteDe(clase))) {
                    continue;
                }
                return new MetodoEncontrado(candidato, ancestro);
            }
        }
        return null;
    }

    private void validarConstructorPorDefecto(NodoEstructura clase) {
        if (!clase.getConstructores().isEmpty() || !esClase(clase)) {
            return;
        }
        NodoEstructura padre = clasePadreDe(clase);
        if (padre != null && !permiteConstructorSinArgumentos(padre)) {
            error(clase, "La clase " + clase.getNombre() + " no declara constructores y la clase padre "
                    + padre.getNombre() + " no tiene un constructor sin parámetros");
        }
    }

    private boolean permiteConstructorSinArgumentos(NodoEstructura clase) {
        if (clase.getConstructores().isEmpty()) {
            return true;
        }
        for (NodoFuncion constructor : clase.getConstructores()) {
            if (constructor.getParametros().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object visitar(NodoAtributo nodo) {
        if (!tipoConocido(nodo.getTipo())) {
            errorTipoDesconocido(nodo, nodo.getTipo().getNombre());
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
            errorTipoDesconocido(nodo, nodo.getTipoRetorno().getNombre());
        }

        boolean dentroDeFuncionAnterior = dentroDeFuncion;
        NodoTipo tipoRetornoAnterior = tipoRetornoActual;
        NodoAST llamadaPermitidaAnterior = llamadaConstructorPermitida;
        dentroDeFuncion = true;
        NodoTipo tr = nodo.getTipoRetorno();
        tipoRetornoActual = (tr != null && VOID.equals(categoria(tr.getNombre()))) ? null : tr;
        llamadaConstructorPermitida = null;

        if (nodo.isEsConstructor() && claseActual != null) {
            if (!nodo.getNombre().equals(claseActual.getNombre())) {
                error(nodo, "El constructor '" + nodo.getNombre() + "' debe llamarse igual que la clase "
                        + claseActual.getNombre());
            }
            NodoLlamada explicita = llamadaAConstructor(nodo.getCuerpo().isEmpty() ? null : nodo.getCuerpo().get(0));
            llamadaConstructorPermitida = explicita;
            if (explicita == null) {
                NodoEstructura padre = clasePadreDe(claseActual);
                if (padre != null && !permiteConstructorSinArgumentos(padre)) {
                    error(nodo, "El constructor debe invocar a super(...) explícitamente: la clase padre "
                            + padre.getNombre() + " no tiene un constructor sin parámetros");
                }
            }
        }

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
        llamadaConstructorPermitida = llamadaPermitidaAnterior;
        return null;
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

    @Override
    public Object visitar(NodoParametro nodo) {
        if (!tipoConocido(nodo.getTipo())) {
            errorTipoDesconocido(nodo, nodo.getTipo().getNombre());
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
            errorTipoDesconocido(nodo, nodo.getTipo().getNombre());
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
        if (!esDestinoAsignable(nodo.getDestino())) {
            error(nodo, "'" + nodo.getOperador() + "' requiere una variable, atributo o elemento de arreglo");
            return null;
        }
        NodoTipo tipo = (NodoTipo) nodo.getDestino().aceptar(this);
        if (tipo != null && !esNumerico(tipo)) {
            error(nodo, "'" + nodo.getOperador() + "' solo aplica a valores numéricos");
        }
        return tipo;
    }

    private boolean esDestinoAsignable(NodoAST nodo) {
        return nodo instanceof NodoIdentificador
                || nodo instanceof NodoAccesoCampo
                || nodo instanceof NodoAccesoArreglo;
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
        boolean hayValor = nodo.getValor() != null;
        NodoTipo tipoValor = hayValor ? (NodoTipo) nodo.getValor().aceptar(this) : null;
        if (!dentroDeFuncion) {
            error(nodo, "'retornar'/'return' usado fuera de una función");
        } else if (tipoRetornoActual == null) {
            if (hayValor) {
                error(nodo, "Esta función/método/constructor no debe devolver un valor");
            }
        } else if (!hayValor) {
            error(nodo, "Se esperaba un valor de retorno de tipo " + describir(tipoRetornoActual));
        } else if (tipoValor != null && !asignable(tipoRetornoActual, tipoValor)) {
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
            case NULO ->
                new NodoTipo(nodo.getLinea(), nodo.getColumna(), NOMBRE_NULO, 0);
            default ->
                null;
        };
    }

    @Override
    public Object visitar(NodoIdentificador nodo) {
        String nombre = nodo.getNombre();

        if ("this".equals(nombre)) {
            if (claseActual == null) {
                error(nodo, "'this' solo puede usarse dentro de una clase");
                return null;
            }
            return new NodoTipo(nodo.getLinea(), nodo.getColumna(), claseActual.getNombre(), 0);
        }
        if ("super".equals(nombre)) {
            error(nodo, "'super' solo puede usarse como super(...), super.atributo o super.metodo(...)");
            return null;
        }

        Simbolo simbolo = entorno.buscar(nombre);
        if (simbolo == null) {
            if (!entorno.buscarFunciones(nombre).isEmpty()) {
                error(nodo, nombre + " es una función; debe invocarse con ()");
            } else {
                error(nodo, "Variable no declarada: " + nombre);
            }
            return null;
        }
        if (simbolo.esEstructura) {
            error(nodo, nombre + " es un tipo, no una variable");
            return null;
        }
        if (simbolo.inaccesible) {
            error(nodo, "El atributo '" + nombre + "' es private en " + simbolo.propietario.getNombre()
                    + " y no es accesible desde " + (claseActual != null ? claseActual.getNombre() : "este contexto"));
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
        NodoTipo tipoBase = tipoDeObjeto(nodo.getBase());
        if (tipoBase == null) {
            return null;
        }
        if (esNulo(tipoBase)) {
            error(nodo, "No se puede acceder a un campo de null");
            return null;
        }
        if (tipoBase.getNivelesArreglo() > 0 || categoria(tipoBase.getNombre()) != null) {
            error(nodo, "Solo se puede acceder a un campo de una estructura/clase, no de " + describir(tipoBase));
            return null;
        }
        Simbolo simboloTipo = entorno.buscar(tipoBase.getNombre());
        if (simboloTipo == null || !simboloTipo.esEstructura) {
            errorTipoDesconocido(nodo, tipoBase.getNombre());
            return null;
        }
        for (NodoEstructura clase : ascendencia(simboloTipo.estructura)) {
            for (NodoAtributo atributo : clase.getAtributos()) {
                if (atributo.getNombre().equals(nodo.getCampo())) {
                    if (!puedeAcceder(atributo.getModificador(), clase)) {
                        error(nodo, "El atributo '" + nodo.getCampo() + "' es "
                                + nombreModificador(atributo.getModificador()) + " en " + clase.getNombre()
                                + " y no es accesible desde "
                                + (claseActual != null ? claseActual.getNombre() : "este contexto"));
                    }
                    return tipoEfectivo(atributo.getTipo(), atributo.getDimensiones());
                }
            }
        }
        error(nodo, tipoBase.getNombre() + " no tiene el campo " + nodo.getCampo());
        return null;
    }

    private NodoTipo tipoDeObjeto(NodoAST base) {
        if (esSuper(base)) {
            if (claseActual == null) {
                error(base, "'super' solo puede usarse dentro de una clase");
                return null;
            }
            NodoEstructura padre = clasePadreDe(claseActual);
            if (padre == null) {
                error(base, "La clase " + claseActual.getNombre() + " no tiene superclase");
                return null;
            }
            return new NodoTipo(base.getLinea(), base.getColumna(), padre.getNombre(), 0);
        }
        return (NodoTipo) base.aceptar(this);
    }

    @Override
    public Object visitar(NodoLlamada nodo) {
        NodoAST base = nodo.getBase();

        if (base instanceof NodoIdentificador id
                && ("super".equals(id.getNombre()) || "this".equals(id.getNombre()))) {
            return analizarLlamadaConstructor(nodo, id.getNombre());
        }

        List<NodoTipo> tiposArgumentos = evaluarArgumentos(nodo.getArgumentos());
        List<Simbolo> candidatos;
        String descripcion;

        if (base instanceof NodoIdentificador id) {
            String nombre = id.getNombre();
            candidatos = claseActual != null ? metodosDe(claseActual, nombre) : new ArrayList<>();
            if (!candidatos.isEmpty()) {
                descripcion = "método '" + nombre + "'";
            } else {
                candidatos = entorno.buscarFunciones(nombre);
                descripcion = "función '" + nombre + "'";
            }
            if (candidatos.isEmpty()) {
                if (entorno.buscar(nombre) != null) {
                    error(nodo, nombre + " no es invocable");
                } else {
                    error(nodo, "Función no declarada: " + nombre);
                }
                return null;
            }
        } else if (base instanceof NodoAccesoCampo campo) {
            String nombre = campo.getCampo();
            NodoTipo tipoObjeto = tipoDeObjeto(campo.getBase());
            if (tipoObjeto == null) {
                return null;
            }
            if (esNulo(tipoObjeto)) {
                error(nodo, "No se puede invocar un método sobre null");
                return null;
            }
            Simbolo simboloTipo = entorno.buscar(tipoObjeto.getNombre());
            if (tipoObjeto.getNivelesArreglo() > 0 || simboloTipo == null || !simboloTipo.esEstructura) {
                error(nodo, "No se puede invocar un método sobre " + describir(tipoObjeto));
                return null;
            }
            candidatos = metodosDe(simboloTipo.estructura, nombre);
            if (candidatos.isEmpty()) {
                error(nodo, tipoObjeto.getNombre() + " no tiene el método " + nombre);
                return null;
            }
            descripcion = "método '" + nombre + "' de " + tipoObjeto.getNombre();
        } else {
            base.aceptar(this);
            error(nodo, "Expresión no invocable");
            return null;
        }

        Simbolo elegido = resolver(nodo, descripcion, candidatos, tiposArgumentos);
        return elegido == null ? null : elegido.tipo;
    }

    private Object analizarLlamadaConstructor(NodoLlamada nodo, String palabra) {
        List<NodoTipo> tiposArgumentos = evaluarArgumentos(nodo.getArgumentos());
        if (nodo != llamadaConstructorPermitida || claseActual == null) {
            error(nodo, "'" + palabra + "(...)' solo puede ser la primera instrucción de un constructor");
            return null;
        }
        NodoEstructura destino = "this".equals(palabra) ? claseActual : clasePadreDe(claseActual);
        if (destino == null) {
            error(nodo, "La clase " + claseActual.getNombre() + " no tiene superclase para invocar super(...)");
            return null;
        }
        resolverConstructor(nodo, destino, tiposArgumentos);
        return null;
    }

    @Override
    public Object visitar(NodoNuevoObjeto nodo) {
        List<NodoTipo> tiposArgumentos = evaluarArgumentos(nodo.getArgumentos());

        Simbolo simboloTipo = entorno.buscar(nodo.getNombreClase());
        if (simboloTipo == null || !simboloTipo.esEstructura) {
            error(nodo, "Clase no declarada: " + nodo.getNombreClase()
                    + " (si se define en otro archivo, verifique que ese archivo esté importado)");
            return new NodoTipo(nodo.getLinea(), nodo.getColumna(), nodo.getNombreClase(), 0);
        }

        resolverConstructor(nodo, simboloTipo.estructura, tiposArgumentos);
        return new NodoTipo(nodo.getLinea(), nodo.getColumna(), nodo.getNombreClase(), 0);
    }

    private Simbolo resolverConstructor(NodoAST nodo, NodoEstructura clase, List<NodoTipo> argumentos) {
        List<Simbolo> candidatos = new ArrayList<>();
        for (NodoFuncion constructor : clase.getConstructores()) {
            candidatos.add(Simbolo.metodo(clase.getNombre(), null, tiposParametros(constructor), constructor, clase));
        }
        if (candidatos.isEmpty()) {
            if (esClase(clase) && !argumentos.isEmpty()) {
                error(nodo, "La clase " + clase.getNombre()
                        + " no declara constructores; solo existe el constructor por defecto sin argumentos");
            }
            return null;
        }
        return resolver(nodo, "constructor de " + clase.getNombre(), candidatos, argumentos);
    }

    private Simbolo resolver(NodoAST nodo, String descripcion, List<Simbolo> candidatos, List<NodoTipo> argumentos) {
        List<Simbolo> accesibles = new ArrayList<>();
        for (Simbolo candidato : candidatos) {
            if (candidato.propietario == null || puedeAcceder(candidato.modificador, candidato.propietario)) {
                accesibles.add(candidato);
            }
        }
        if (accesibles.isEmpty()) {
            Simbolo c = candidatos.get(0);
            error(nodo, "El " + descripcion + " es " + nombreModificador(c.modificador) + " en "
                    + c.propietario.getNombre() + " y no es accesible desde "
                    + (claseActual != null ? claseActual.getNombre() : "este contexto"));
            return null;
        }

        List<Simbolo> porAridad = new ArrayList<>();
        for (Simbolo candidato : accesibles) {
            if (candidato.parametros.size() == argumentos.size()) {
                porAridad.add(candidato);
            }
        }
        if (porAridad.isEmpty()) {
            if (accesibles.size() == 1) {
                error(nodo, "Se esperaban " + accesibles.get(0).parametros.size() + " argumento(s) para "
                        + descripcion + ", se recibieron " + argumentos.size());
            } else {
                error(nodo, "No existe una versión del " + descripcion + " con " + argumentos.size() + " argumento(s)");
            }
            return null;
        }

        if (argumentos.contains(null)) {
            return porAridad.get(0);
        }

        List<Simbolo> aplicables = new ArrayList<>();
        for (Simbolo candidato : porAridad) {
            boolean aplica = true;
            for (int i = 0; i < argumentos.size(); i++) {
                if (!asignable(candidato.parametros.get(i), argumentos.get(i))) {
                    aplica = false;
                    break;
                }
            }
            if (aplica) {
                aplicables.add(candidato);
            }
        }
        if (aplicables.isEmpty()) {
            if (porAridad.size() == 1) {
                Simbolo unico = porAridad.get(0);
                for (int i = 0; i < argumentos.size(); i++) {
                    if (!asignable(unico.parametros.get(i), argumentos.get(i))) {
                        error(nodo, "El argumento " + (i + 1) + " del " + descripcion + " no coincide en tipo: se esperaba "
                                + describir(unico.parametros.get(i)) + " y se recibió " + describir(argumentos.get(i)));
                        break;
                    }
                }
            } else {
                error(nodo, "Ninguna versión del " + descripcion + " es aplicable a (" + describirLista(argumentos) + ")");
            }
            return null;
        }
        if (aplicables.size() == 1) {
            return aplicables.get(0);
        }

        List<Simbolo> exactos = new ArrayList<>();
        for (Simbolo candidato : aplicables) {
            boolean exacto = true;
            for (int i = 0; i < argumentos.size(); i++) {
                if (!mismoTipoExacto(candidato.parametros.get(i), argumentos.get(i))) {
                    exacto = false;
                    break;
                }
            }
            if (exacto) {
                exactos.add(candidato);
            }
        }
        if (exactos.size() == 1) {
            return exactos.get(0);
        }

        List<Simbolo> masEspecificos = new ArrayList<>();
        for (Simbolo candidato : aplicables) {
            boolean masEspecifico = true;
            for (Simbolo otro : aplicables) {
                if (otro == candidato) {
                    continue;
                }
                for (int i = 0; i < argumentos.size(); i++) {
                    if (!asignable(otro.parametros.get(i), candidato.parametros.get(i))) {
                        masEspecifico = false;
                        break;
                    }
                }
                if (!masEspecifico) {
                    break;
                }
            }
            if (masEspecifico) {
                masEspecificos.add(candidato);
            }
        }
        if (masEspecificos.size() == 1) {
            return masEspecificos.get(0);
        }

        error(nodo, "Llamada ambigua al " + descripcion + " con (" + describirLista(argumentos) + ")");
        return null;
    }

    private List<NodoTipo> evaluarArgumentos(List<NodoAST> argumentos) {
        List<NodoTipo> tipos = new ArrayList<>();
        for (NodoAST argumento : argumentos) {
            tipos.add((NodoTipo) argumento.aceptar(this));
        }
        return tipos;
    }

    private List<Simbolo> metodosDe(NodoEstructura clase, String nombre) {
        List<Simbolo> resultado = new ArrayList<>();
        for (NodoEstructura actual : ascendencia(clase)) {
            for (NodoFuncion metodo : actual.getMetodos()) {
                if (!metodo.getNombre().equals(nombre)) {
                    continue;
                }
                List<NodoTipo> parametros = tiposParametros(metodo);
                boolean yaEsta = false;
                for (Simbolo existente : resultado) {
                    if (mismaFirma(existente.parametros, parametros)) {
                        yaEsta = true;
                        break;
                    }
                }
                if (!yaEsta) {
                    resultado.add(Simbolo.metodo(nombre, metodo.getTipoRetorno(), parametros, metodo, actual));
                }
            }
        }
        return resultado;
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
            errorTipoDesconocido(nodo, nodo.getTipoBase());
        }
        return new NodoTipo(nodo.getLinea(), nodo.getColumna(), nodo.getTipoBase(), nodo.getDimensiones().size());
    }

    @Override
    public Object visitar(NodoLecturaEntrada nodo) {
        return null;
    }

    private boolean esSuper(NodoAST nodo) {
        return nodo instanceof NodoIdentificador id && "super".equals(id.getNombre());
    }

    private boolean esClase(NodoEstructura estructura) {
        return estructura.getModificador() != null;
    }

    private NodoEstructura buscarClase(String nombre) {
        Simbolo simbolo = entorno.buscar(nombre);
        return simbolo != null && simbolo.esEstructura ? simbolo.estructura : null;
    }

    private NodoEstructura clasePadreDe(NodoEstructura clase) {
        return clase.getClasePadre() == null ? null : buscarClase(clase.getClasePadre());
    }

    private List<NodoEstructura> ascendencia(NodoEstructura clase) {
        List<NodoEstructura> lista = new ArrayList<>();
        Set<String> vistos = new HashSet<>();
        NodoEstructura actual = clase;
        while (actual != null && vistos.add(actual.getNombre())) {
            lista.add(actual);
            actual = clasePadreDe(actual);
        }
        return lista;
    }

    private boolean esSubtipo(String sub, String sup) {
        if (sub.equals(sup)) {
            return true;
        }
        NodoEstructura clase = buscarClase(sub);
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

    private String paqueteDe(NodoEstructura clase) {
        return clase == null ? "" : paquetes.getOrDefault(clase.getNombre(), "");
    }

    private boolean puedeAcceder(String modificador, NodoEstructura propietario) {
        if (propietario == null || modificador == null) {
            return true;
        }
        if (claseActual != null && claseActual.getNombre().equals(propietario.getNombre())) {
            return true;
        }
        boolean mismoPaquete = paqueteDe(claseActual).equals(paqueteDe(propietario));
        return switch (modificador) {
            case "public" ->
                true;
            case "private" ->
                false;
            case "protected" ->
                mismoPaquete || (claseActual != null && esSubtipo(claseActual.getNombre(), propietario.getNombre()));
            default ->
                mismoPaquete;
        };
    }

    private int rango(String modificador) {
        if (modificador == null) {
            return 3;
        }
        return switch (modificador) {
            case "public" ->
                3;
            case "protected" ->
                2;
            case "default" ->
                1;
            default ->
                0;
        };
    }

    private String nombreModificador(String modificador) {
        return modificador == null ? "public" : ("default".equals(modificador) ? "package-private" : modificador);
    }

    private boolean retornoCompatible(NodoTipo padre, NodoTipo hijo) {
        if (padre == null || hijo == null || mismoTipoExacto(padre, hijo)) {
            return true;
        }
        return esReferencia(padre) && esReferencia(hijo) && asignable(padre, hijo);
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

    private boolean esNulo(NodoTipo tipo) {
        return tipo != null && tipo.getNivelesArreglo() == 0 && NOMBRE_NULO.equals(tipo.getNombre());
    }

    private boolean esReferencia(NodoTipo tipo) {
        if (tipo == null || esNulo(tipo)) {
            return false;
        }
        if (tipo.getNivelesArreglo() > 0) {
            return true;
        }
        String cat = categoria(tipo.getNombre());
        return cat == null || CADENA.equals(cat);
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
        if (esNulo(a) || esNulo(b)) {
            return (esNulo(a) && esNulo(b)) || esReferencia(esNulo(a) ? b : a);
        }
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

    private boolean mismoTipoExacto(NodoTipo a, NodoTipo b) {
        if (a == null || b == null) {
            return a == b;
        }
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

    private boolean mismaFirma(List<NodoTipo> a, List<NodoTipo> b) {
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            if (!mismoTipoExacto(a.get(i), b.get(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean asignable(NodoTipo destino, NodoTipo origen) {
        if (destino == null || origen == null) {
            return true;
        }
        if (esNulo(origen)) {
            return esReferencia(destino);
        }
        if (esNulo(destino)) {
            return false;
        }
        if (destino.getNivelesArreglo() != origen.getNivelesArreglo()) {
            return false;
        }
        String catD = categoria(destino.getNombre());
        String catO = categoria(origen.getNombre());
        if (catD == null && catO == null) {
            // misma clase o subclase (polimorfismo)
            return destino.getNombre().equals(origen.getNombre())
                    || esSubtipo(origen.getNombre(), destino.getNombre());
        }
        if (catD == null || catO == null) {
            return false;
        }
        if (catD.equals(catO)) {
            return true;
        }
        return DECIMAL.equals(catD) && ENTERO.equals(catO);
    }

    private boolean relacionadosPorHerencia(NodoTipo a, NodoTipo b) {
        if (a.getNivelesArreglo() != 0 || b.getNivelesArreglo() != 0
                || categoria(a.getNombre()) != null || categoria(b.getNombre()) != null) {
            return false;
        }
        return esSubtipo(a.getNombre(), b.getNombre()) || esSubtipo(b.getNombre(), a.getNombre());
    }

    private boolean tipoConocido(NodoTipo tipo) {
        if (categoria(tipo.getNombre()) != null) {
            return true;
        }
        Simbolo simbolo = entorno.buscar(tipo.getNombre());
        return simbolo != null && simbolo.esEstructura;
    }

    private String describir(NodoTipo tipo) {
        if (tipo == null) {
            return "?";
        }
        StringBuilder texto = new StringBuilder(tipo.getNombre());
        for (int i = 0; i < tipo.getNivelesArreglo(); i++) {
            texto.append("[]");
        }
        return texto.toString();
    }

    private String describirLista(List<NodoTipo> tipos) {
        List<String> partes = new ArrayList<>();
        for (NodoTipo tipo : tipos) {
            partes.add(describir(tipo));
        }
        return String.join(", ", partes);
    }

    private String firma(String nombre, List<NodoTipo> parametros) {
        return nombre + "(" + describirLista(parametros) + ")";
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
                boolean ambosCaracteres = CARACTER.equals(categoria(izquierda.getNombre()))
                        && CARACTER.equals(categoria(derecha.getNombre()));
                if ((!esNumerico(izquierda) || !esNumerico(derecha)) && !ambosCaracteres) {
                    error(nodo, "Los operandos de '" + operador + "' deben ser numéricos o caracteres");
                }
                return TIPO_BOOLEANO;
            }
            case "==", "!=", "=" -> {
                if (!mismaCategoria(izquierda, derecha)
                        && !(esNumerico(izquierda) && esNumerico(derecha))
                        && !relacionadosPorHerencia(izquierda, derecha)) {
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
