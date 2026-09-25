/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.antlr4.YBaseVisitor;
import com.mynor.contactoextraterrestred.antlr4.YParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author mynordma
 */
public class CreadorASTY extends YBaseVisitor<NodoAST> {
    
    @Override
    public NodoAST visitS(YParser.SContext ctx) {
        return visit(ctx.secciones());
    }

    @Override
    public NodoAST visitSecciones(YParser.SeccionesContext ctx) {
        List<NodoEstructura> estructuras = new ArrayList<>();
        for (YParser.EstructuraContext e : ctx.estructuras().estructura()) {
            estructuras.add((NodoEstructura) visit(e));
        }
        List<NodoFuncion> funciones = new ArrayList<>();
        for (YParser.FuncionContext f : ctx.funciones().funcion()) {
            funciones.add((NodoFuncion) visit(f));
        }
        return new NodoPrograma(linea(ctx), columna(ctx),
                Collections.emptyList(), 
                Collections.emptyList(),
                estructuras,
                funciones,
                null,
                Collections.emptyList());
    }

    @Override
    public NodoAST visitEstructura(YParser.EstructuraContext ctx) {
        String nombre = ctx.ID().getText();
        List<NodoAtributo> atributos = new ArrayList<>();
        for (YParser.AtributoContext a : ctx.atributo()) {
            atributos.add((NodoAtributo) visit(a));
        }
        return new NodoEstructura(linea(ctx), columna(ctx), nombre, atributos,
                Collections.emptyList(), Collections.emptyList());
    }

    @Override
    public NodoAST visitAtributo(YParser.AtributoContext ctx) {
        NodoTipo tipo = (NodoTipo) visit(ctx.tipo());
        String nombre = ctx.ID().getText();
        List<NodoDimension> dimensiones = new ArrayList<>();
        for (YParser.DimensionContext d : ctx.dimension()) {
            dimensiones.add((NodoDimension) visit(d));
        }
        return new NodoAtributo(linea(ctx), columna(ctx), tipo, nombre, dimensiones, null);
    }

    @Override
    public NodoAST visitDimension(YParser.DimensionContext ctx) {
        long valor = Long.parseLong(ctx.NUMERO_ENTERO().getText());
        NodoLiteral tamano = new NodoLiteral(
                linea(ctx.NUMERO_ENTERO()), columna(ctx.NUMERO_ENTERO()), valor, TipoLiteral.ENTERO);
        return new NodoDimension(linea(ctx), columna(ctx), tamano);
    }

    @Override
    public NodoAST visitTipo(YParser.TipoContext ctx) {
        return new NodoTipo(linea(ctx), columna(ctx), ctx.getText(), 0);
    }

    @Override
    public NodoAST visitFuncion(YParser.FuncionContext ctx) {
        String nombre = ctx.ID().getText();

        List<NodoParametro> parametros = new ArrayList<>();
        if (ctx.parametros_funcion() != null) {
            for (YParser.Parametro_funcionContext p : ctx.parametros_funcion().parametro_funcion()) {
                parametros.add((NodoParametro) visit(p));
            }
        }

        NodoTipo tipoRetorno = ctx.tipo_retorno() != null ? (NodoTipo) visit(ctx.tipo_retorno()) : null;

        List<NodoAST> cuerpo = new ArrayList<>();
        for (YParser.Elemento_funcionContext e : ctx.elemento_funcion()) {
            cuerpo.add(visit(e));
        }

        return new NodoFuncion(linea(ctx), columna(ctx), nombre, parametros, tipoRetorno, cuerpo, false);
    }

    @Override
    public NodoAST visitParametro_funcion(YParser.Parametro_funcionContext ctx) {
        YParser.Info_parametroContext info = ctx.info_parametro();
        NodoTipo tipo = (NodoTipo) visit(info);
        boolean aceptaCualquierArreglo = info.dimensiones_parametro() != null;
        boolean aceptaCualquierEstructura = info.estructuraParametro() != null;
        String nombre = ctx.ID().getText();
        return new NodoParametro(linea(ctx), columna(ctx), tipo, nombre,
                aceptaCualquierArreglo, aceptaCualquierEstructura);
    }

    @Override
    public NodoAST visitInfo_parametro(YParser.Info_parametroContext ctx) {
        NodoTipo tipoBase = (NodoTipo) visit(ctx.tipo());
        int niveles = ctx.dimensiones_parametro() != null
                ? ctx.dimensiones_parametro().CA().size()
                : 0;
        return new NodoTipo(linea(ctx), columna(ctx), tipoBase.getNombre(), niveles);
    }

    @Override
    public NodoAST visitTipo_retorno(YParser.Tipo_retornoContext ctx) {
        return visit(ctx.info_parametro());
    }

    @Override
    public NodoAST visitElemento_funcion(YParser.Elemento_funcionContext ctx) {
        return visit(ctx.getChild(0));
    }

    @Override
    public NodoAST visitDeclaracion(YParser.DeclaracionContext ctx) {
        if (ctx.estructura() != null) {
            return visit(ctx.estructura());
        }
        NodoTipo tipo = (NodoTipo) visit(ctx.tipo());
        String nombre = ctx.ID().getText();
        List<NodoDimension> dimensiones = new ArrayList<>();
        for (YParser.DimensionContext d : ctx.dimension()) {
            dimensiones.add((NodoDimension) visit(d));
        }
        NodoAST inicializador = ctx.inicializacion() != null ? visit(ctx.inicializacion()) : null;
        return new NodoDeclaracion(linea(ctx), columna(ctx), tipo, nombre, dimensiones, inicializador);
    }

    @Override
    public NodoAST visitInicializacion(YParser.InicializacionContext ctx) {
        return visit(ctx.getChild(0));
    }

    @Override
    public NodoAST visitLista_expresiones(YParser.Lista_expresionesContext ctx) {
        List<NodoAST> elementos = new ArrayList<>();
        for (YParser.Elemento_listaContext e : ctx.elemento_lista()) {
            elementos.add(visit(e));
        }
        return new NodoListaExpresiones(linea(ctx), columna(ctx), elementos);
    }

    @Override
    public NodoAST visitElemento_lista(YParser.Elemento_listaContext ctx) {
        return visit(ctx.getChild(0));
    }

    @Override
    public NodoAST visitSentencia(YParser.SentenciaContext ctx) {
        return visit(ctx.getChild(0));
    }

    @Override
    public NodoAST visitAsignacion(YParser.AsignacionContext ctx) {
        NodoAST destino = visit(ctx.destino());
        String operador = ctx.ASIG().getText();
        NodoAST valor = visit(ctx.expresion());
        return new NodoAsignacion(linea(ctx), columna(ctx), destino, operador, valor);
    }

    @Override
    public NodoAST visitIncrementoDecremento(YParser.IncrementoDecrementoContext ctx) {
        NodoAST destino = visit(ctx.destino());
        String operador = ctx.INCREMENTO() != null ? "++" : "--";
        return new NodoIncrementoDecremento(linea(ctx), columna(ctx), destino, operador);
    }

    @Override
    public NodoAST visitDestino(YParser.DestinoContext ctx) {
        NodoAST resultado = new NodoIdentificador(linea(ctx.ID()), columna(ctx.ID()), ctx.ID().getText());
        for (YParser.AccesoContext acceso : ctx.acceso()) {
            resultado = aplicarAcceso(resultado, acceso);
        }
        return resultado;
    }

    @Override
    public NodoAST visitSIf(YParser.SIfContext ctx) {
        NodoAST condicion = visit(ctx.expresion());

        List<NodoAST> cuerpoSi = new ArrayList<>();
        for (YParser.SentenciaContext s : ctx.sentencia()) {
            cuerpoSi.add(visit(s));
        }

        List<NodoSinoSi> ramasSinoSi = new ArrayList<>();
        for (YParser.SSinoContext ss : ctx.sSino()) {
            ramasSinoSi.add((NodoSinoSi) visit(ss));
        }

        List<NodoAST> cuerpoSino = null;
        if (ctx.sContrario() != null) {
            cuerpoSino = new ArrayList<>();
            for (YParser.SentenciaContext s : ctx.sContrario().sentencia()) {
                cuerpoSino.add(visit(s));
            }
        }

        return new NodoIf(linea(ctx), columna(ctx), condicion, cuerpoSi, ramasSinoSi, cuerpoSino);
    }

    @Override
    public NodoAST visitSSino(YParser.SSinoContext ctx) {
        NodoAST condicion = visit(ctx.expresion());
        List<NodoAST> cuerpo = new ArrayList<>();
        for (YParser.SentenciaContext s : ctx.sentencia()) {
            cuerpo.add(visit(s));
        }
        return new NodoSinoSi(linea(ctx), columna(ctx), condicion, cuerpo);
    }

    @Override
    public NodoAST visitSSwitch(YParser.SSwitchContext ctx) {
        NodoAST expresion = visit(ctx.expresion());

        List<NodoCaso> casos = new ArrayList<>();
        for (YParser.CasoContext c : ctx.caso()) {
            casos.add((NodoCaso) visit(c));
        }

        List<NodoAST> casoPorDefecto = null;
        if (ctx.siempre() != null) {
            casoPorDefecto = new ArrayList<>();
            for (YParser.SentenciaContext s : ctx.siempre().sentencia()) {
                casoPorDefecto.add(visit(s));
            }
        }

        return new NodoSwitch(linea(ctx), columna(ctx), expresion, casos, casoPorDefecto);
    }

    @Override
    public NodoAST visitCaso(YParser.CasoContext ctx) {
        NodoLiteral valor = (NodoLiteral) visit(ctx.literal());
        List<NodoAST> cuerpo = new ArrayList<>();
        for (YParser.SentenciaContext s : ctx.sentencia()) {
            cuerpo.add(visit(s));
        }
        return new NodoCaso(linea(ctx), columna(ctx), valor, cuerpo);
    }

    @Override
    public NodoAST visitSFor(YParser.SForContext ctx) {
        NodoAST inicializacion = ctx.forInit() != null ? visit(ctx.forInit()) : null;
        NodoAST condicion = ctx.expresion() != null ? visit(ctx.expresion()) : null;
        NodoAST actualizacion = ctx.forActualizacion() != null ? visit(ctx.forActualizacion()) : null;

        List<NodoAST> cuerpo = new ArrayList<>();
        for (YParser.SentenciaContext s : ctx.sentencia()) {
            cuerpo.add(visit(s));
        }

        return new NodoFor(linea(ctx), columna(ctx), inicializacion, condicion, actualizacion, cuerpo);
    }

    @Override
    public NodoAST visitForInit(YParser.ForInitContext ctx) {
        if (ctx.tipo() != null) {
            NodoTipo tipo = (NodoTipo) visit(ctx.tipo());
            String nombre = ctx.ID().getText();
            NodoAST inicial = ctx.expresion() != null ? visit(ctx.expresion()) : null;
            return new NodoDeclaracion(linea(ctx), columna(ctx), tipo, nombre, Collections.emptyList(), inicial);
        }
        NodoAST destino = visit(ctx.destino());
        String operador = ctx.ASIG().getText();
        NodoAST valor = visit(ctx.expresion());
        return new NodoAsignacion(linea(ctx), columna(ctx), destino, operador, valor);
    }

    @Override
    public NodoAST visitForActualizacion(YParser.ForActualizacionContext ctx) {
        NodoAST destino = visit(ctx.destino());
        if (ctx.INCREMENTO() != null || ctx.DECREMENTO() != null) {
            String operador = ctx.INCREMENTO() != null ? "++" : "--";
            return new NodoIncrementoDecremento(linea(ctx), columna(ctx), destino, operador);
        }
        String operador = ctx.ASIG().getText();
        NodoAST valor = visit(ctx.expresion());
        return new NodoAsignacion(linea(ctx), columna(ctx), destino, operador, valor);
    }

    @Override
    public NodoAST visitSWhile(YParser.SWhileContext ctx) {
        NodoAST condicion = visit(ctx.expresion());
        List<NodoAST> cuerpo = new ArrayList<>();
        for (YParser.SentenciaContext s : ctx.sentencia()) {
            cuerpo.add(visit(s));
        }
        return new NodoWhile(linea(ctx), columna(ctx), condicion, cuerpo);
    }

    @Override
    public NodoAST visitSDoWhile(YParser.SDoWhileContext ctx) {
        List<NodoAST> cuerpo = new ArrayList<>();
        for (YParser.SentenciaContext s : ctx.sentencia()) {
            cuerpo.add(visit(s));
        }
        NodoAST condicion = visit(ctx.expresion());
        return new NodoDoWhile(linea(ctx), columna(ctx), cuerpo, condicion);
    }

    @Override
    public NodoAST visitSContinue(YParser.SContinueContext ctx) {
        return new NodoContinue(linea(ctx), columna(ctx));
    }

    @Override
    public NodoAST visitSBreak(YParser.SBreakContext ctx) {
        return new NodoBreak(linea(ctx), columna(ctx));
    }

    @Override
    public NodoAST visitSReturn(YParser.SReturnContext ctx) {
        NodoAST valor = ctx.expresion() != null ? visit(ctx.expresion()) : null;
        return new NodoReturn(linea(ctx), columna(ctx), valor);
    }

    @Override
    public NodoAST visitSImprimir(YParser.SImprimirContext ctx) {
        NodoAST expresion = visit(ctx.expresion());
        return new NodoImprimir(linea(ctx), columna(ctx), Collections.singletonList(expresion), false);
    }

    @Override
    public NodoAST visitSExpresion(YParser.SExpresionContext ctx) {
        return new NodoExpresionSentencia(linea(ctx), columna(ctx), visit(ctx.expresion()));
    }

    @Override
    public NodoAST visitExpresion(YParser.ExpresionContext ctx) {
        return visit(ctx.expresionOr());
    }

    @Override
    public NodoAST visitExpresionOr(YParser.ExpresionOrContext ctx) {
        return plegarIzquierda(ctx, ctx.expresionAnd());
    }

    @Override
    public NodoAST visitExpresionAnd(YParser.ExpresionAndContext ctx) {
        return plegarIzquierda(ctx, ctx.expresionIgualdad());
    }

    @Override
    public NodoAST visitExpresionIgualdad(YParser.ExpresionIgualdadContext ctx) {
        return plegarIzquierda(ctx, ctx.expresionRelacional());
    }

    @Override
    public NodoAST visitExpresionRelacional(YParser.ExpresionRelacionalContext ctx) {
        return plegarIzquierda(ctx, ctx.expresionAditiva());
    }

    @Override
    public NodoAST visitExpresionAditiva(YParser.ExpresionAditivaContext ctx) {
        return plegarIzquierda(ctx, ctx.expresionMultiplicativa());
    }

    @Override
    public NodoAST visitExpresionMultiplicativa(YParser.ExpresionMultiplicativaContext ctx) {
        return plegarIzquierda(ctx, ctx.expresionUnaria());
    }

    @Override
    public NodoAST visitExpresionUnaria(YParser.ExpresionUnariaContext ctx) {
        if (ctx.expresionUnaria() != null) {
            String operador = ctx.getChild(0).getText();
            NodoAST operando = visit(ctx.expresionUnaria());
            return new NodoUnaria(linea(ctx), columna(ctx), operador, operando);
        }
        return visit(ctx.expresionPostfija());
    }

    @Override
    public NodoAST visitExpresionPostfija(YParser.ExpresionPostfijaContext ctx) {
        NodoAST resultado = visit(ctx.expresionPrimaria());
        for (YParser.SufijoContext sufijo : ctx.sufijo()) {
            resultado = aplicarSufijo(resultado, sufijo);
        }
        return resultado;
    }

    @Override
    public NodoAST visitExpresionPrimaria(YParser.ExpresionPrimariaContext ctx) {
        if (ctx.literal() != null) {
            return visit(ctx.literal());
        }
        if (ctx.ID() != null) {
            return new NodoIdentificador(linea(ctx.ID()), columna(ctx.ID()), ctx.ID().getText());
        }
        if (ctx.LEER() != null) {
            return new NodoLecturaEntrada(linea(ctx), columna(ctx));
        }
        return visit(ctx.expresion());
    }

    @Override
    public NodoAST visitLiteral(YParser.LiteralContext ctx) {
        if (ctx.NUMERO_ENTERO() != null) {
            long valor = Long.parseLong(ctx.NUMERO_ENTERO().getText());
            return new NodoLiteral(linea(ctx), columna(ctx), valor, TipoLiteral.ENTERO);
        }
        if (ctx.DECIMAL() != null) {
            double valor = Double.parseDouble(ctx.DECIMAL().getText());
            return new NodoLiteral(linea(ctx), columna(ctx), valor, TipoLiteral.DECIMAL);
        }
        if (ctx.STRING() != null) {
            String texto = ctx.STRING().getText();
            String valor = texto.substring(1, texto.length() - 1);
            return new NodoLiteral(linea(ctx), columna(ctx), valor, TipoLiteral.CADENA);
        }
        if (ctx.CHAR() != null) {
            String texto = ctx.CHAR().getText();
            char valor = texto.charAt(1);
            return new NodoLiteral(linea(ctx), columna(ctx), valor, TipoLiteral.CARACTER);
        }
        if (ctx.VERDADERO() != null) {
            return new NodoLiteral(linea(ctx), columna(ctx), Boolean.TRUE, TipoLiteral.BOOLEANO);
        }
        // FALSO
        return new NodoLiteral(linea(ctx), columna(ctx), Boolean.FALSE, TipoLiteral.BOOLEANO);
    }

    private int linea(ParserRuleContext ctx) {
        return ctx.getStart().getLine();
    }

    private int columna(ParserRuleContext ctx) {
        return ctx.getStart().getCharPositionInLine();
    }

    private int linea(TerminalNode t) {
        return t.getSymbol().getLine();
    }

    private int columna(TerminalNode t) {
        return t.getSymbol().getCharPositionInLine();
    }

    private NodoAST plegarIzquierda(ParserRuleContext ctx, List<? extends ParserRuleContext> operandos) {
        NodoAST resultado = visit(operandos.get(0));
        int siguienteOperando = 1;
        for (int i = 1; i < ctx.getChildCount(); i += 2) {
            String operador = ctx.getChild(i).getText();
            NodoAST derecha = visit(operandos.get(siguienteOperando++));
            resultado = new NodoBinaria(linea(ctx), columna(ctx), resultado, operador, derecha);
        }
        return resultado;
    }

    private NodoAST aplicarAcceso(NodoAST base, YParser.AccesoContext ctx) {
        if (ctx.acceso_arreglo() != null) {
            YParser.Acceso_arregloContext a = ctx.acceso_arreglo();
            NodoAST indice = visit(a.expresion());
            return new NodoAccesoArreglo(linea(a), columna(a), base, indice);
        }
        YParser.Acceso_structContext a = ctx.acceso_struct();
        return new NodoAccesoCampo(linea(a), columna(a), base, a.ID().getText());
    }

    private NodoAST aplicarSufijo(NodoAST base, YParser.SufijoContext ctx) {
        if (ctx.CA() != null) {
            NodoAST indice = visit(ctx.expresion());
            return new NodoAccesoArreglo(linea(ctx), columna(ctx), base, indice);
        }
        if (ctx.PUNTO() != null) {
            return new NodoAccesoCampo(linea(ctx), columna(ctx), base, ctx.ID().getText());
        }
        List<NodoAST> argumentos = new ArrayList<>();
        if (ctx.argumentos() != null) {
            for (YParser.ExpresionContext e : ctx.argumentos().expresion()) {
                argumentos.add(visit(e));
            }
        }
        return new NodoLlamada(linea(ctx), columna(ctx), base, argumentos);
    }
}
