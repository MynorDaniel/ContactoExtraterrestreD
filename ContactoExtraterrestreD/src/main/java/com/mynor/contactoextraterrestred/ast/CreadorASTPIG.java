/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.antlr4.PIGBaseVisitor;
import com.mynor.contactoextraterrestred.antlr4.PIGParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author mynordma
 */
public class CreadorASTPIG extends PIGBaseVisitor<NodoAST> {

    @Override
    public NodoAST visitPrograma(PIGParser.ProgramaContext ctx) {
        List<NodoImportacion> importaciones = new ArrayList<>();
        for (PIGParser.ImportacionContext i : ctx.importacion()) {
            importaciones.add((NodoImportacion) visit(i));
        }

        List<NodoDeclaracion> variablesGlobales = new ArrayList<>();
        if (ctx.seccionVariables() != null) {
            for (PIGParser.DeclaracionGlobalContext d : ctx.seccionVariables().declaracionGlobal()) {
                variablesGlobales.add((NodoDeclaracion) visit(d));
            }
        }

        List<NodoAST> sentenciasPrincipales = new ArrayList<>();
        for (PIGParser.SentenciaContext s : ctx.seccionMaior().sentencia()) {
            sentenciasPrincipales.add(visit(s));
        }

        return new NodoPrograma(linea(ctx), columna(ctx),
                importaciones,
                variablesGlobales,
                Collections.emptyList(), 
                Collections.emptyList(), 
                null,
                sentenciasPrincipales);
    }

    @Override
    public NodoAST visitImportacion(PIGParser.ImportacionContext ctx) {
        return new NodoImportacion(linea(ctx), columna(ctx), ctx.ruta().getText());
    }

    @Override
    public NodoAST visitDeclaracionGlobal(PIGParser.DeclaracionGlobalContext ctx) {
        return visit(ctx.getChild(0));
    }

    @Override
    public NodoAST visitDeclaracionEsto(PIGParser.DeclaracionEstoContext ctx) {
        NodoTipo tipo = (NodoTipo) visit(ctx.tipo());
        String nombre = ctx.ID().getText();
        NodoAST inicializador = visit(ctx.inicializacion());
        return new NodoDeclaracion(linea(ctx), columna(ctx), tipo, nombre, Collections.emptyList(), inicializador);
    }

    @Override
    public NodoAST visitDeclaracionSeries(PIGParser.DeclaracionSeriesContext ctx) {
        NodoTipo tipo = (NodoTipo) visit(ctx.tipo());
        String nombre = ctx.ID().getText();
        List<NodoDimension> dimensiones = new ArrayList<>();
        for (PIGParser.DimensionContext d : ctx.dimension()) {
            dimensiones.add((NodoDimension) visit(d));
        }
        NodoAST inicializador = ctx.inicializacion() != null ? visit(ctx.inicializacion()) : null;
        return new NodoDeclaracion(linea(ctx), columna(ctx), tipo, nombre, dimensiones, inicializador);
    }

    @Override
    public NodoAST visitDimension(PIGParser.DimensionContext ctx) {
        long valor = Long.parseLong(ctx.NUMERO_ENTERO().getText());
        NodoLiteral tamano = new NodoLiteral(
                linea(ctx.NUMERO_ENTERO()), columna(ctx.NUMERO_ENTERO()), valor, TipoLiteral.ENTERO);
        return new NodoDimension(linea(ctx), columna(ctx), tamano);
    }

    @Override
    public NodoAST visitInicializacion(PIGParser.InicializacionContext ctx) {
        return visit(ctx.getChild(0));
    }

    @Override
    public NodoAST visitLista_expresiones(PIGParser.Lista_expresionesContext ctx) {
        List<NodoAST> elementos = new ArrayList<>();
        for (PIGParser.Elemento_listaContext e : ctx.elemento_lista()) {
            elementos.add(visit(e));
        }
        return new NodoListaExpresiones(linea(ctx), columna(ctx), elementos);
    }

    @Override
    public NodoAST visitElemento_lista(PIGParser.Elemento_listaContext ctx) {
        return visit(ctx.getChild(0));
    }

    @Override
    public NodoAST visitTipo(PIGParser.TipoContext ctx) {
        if (ctx.tipoBase() != null) {
            return visit(ctx.tipoBase());
        }
        return new NodoTipo(linea(ctx.ID()), columna(ctx.ID()), ctx.ID().getText(), 0);
    }

    @Override
    public NodoAST visitTipoBase(PIGParser.TipoBaseContext ctx) {
        return new NodoTipo(linea(ctx), columna(ctx), ctx.getText(), 0);
    }

    @Override
    public NodoAST visitBloque(PIGParser.BloqueContext ctx) {
        List<NodoAST> sentencias = new ArrayList<>();
        for (PIGParser.SentenciaContext s : ctx.sentencia()) {
            sentencias.add(visit(s));
        }
        return new NodoBloque(linea(ctx), columna(ctx), sentencias);
    }

    @Override
    public NodoAST visitSentencia(PIGParser.SentenciaContext ctx) {
        return visit(ctx.getChild(0));
    }

    @Override
    public NodoAST visitAsignacion(PIGParser.AsignacionContext ctx) {
        NodoAST destino = visit(ctx.destino());
        NodoAST valor = visit(ctx.expresion());
        return new NodoAsignacion(linea(ctx), columna(ctx), destino, "=", valor);
    }

    @Override
    public NodoAST visitIncrementoDecremento(PIGParser.IncrementoDecrementoContext ctx) {
        NodoAST destino = visit(ctx.destino());
        String operador = ctx.INCREMENTO() != null ? "++" : "--";
        return new NodoIncrementoDecremento(linea(ctx), columna(ctx), destino, operador);
    }

    @Override
    public NodoAST visitDestino(PIGParser.DestinoContext ctx) {
        NodoAST resultado = new NodoIdentificador(linea(ctx.ID()), columna(ctx.ID()), ctx.ID().getText());
        for (PIGParser.AccesoContext acceso : ctx.acceso()) {
            resultado = aplicarAcceso(resultado, acceso);
        }
        return resultado;
    }

    @Override
    public NodoAST visitSIf(PIGParser.SIfContext ctx) {
        int n = ctx.expresion().size() - 1;
        boolean tieneSinoFinal = ctx.ALITER().size() > n;

        NodoAST condicion = visit(ctx.expresion(0));
        NodoBloque cuerpoPrincipal = (NodoBloque) visit(ctx.bloque(0));

        List<NodoSinoSi> ramasSinoSi = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            NodoAST condRama = visit(ctx.expresion(i));
            NodoBloque cuerpoRama = (NodoBloque) visit(ctx.bloque(i));
            ramasSinoSi.add(new NodoSinoSi(
                    cuerpoRama.getLinea(), cuerpoRama.getColumna(), condRama, cuerpoRama.getSentencias()));
        }

        List<NodoAST> cuerpoSino = null;
        if (tieneSinoFinal) {
            NodoBloque ultimoBloque = (NodoBloque) visit(ctx.bloque(ctx.bloque().size() - 1));
            cuerpoSino = ultimoBloque.getSentencias();
        }

        return new NodoIf(linea(ctx), columna(ctx), condicion, cuerpoPrincipal.getSentencias(),
                ramasSinoSi, cuerpoSino);
    }

    @Override
    public NodoAST visitSWhile(PIGParser.SWhileContext ctx) {
        NodoAST condicion = visit(ctx.expresion());
        NodoBloque cuerpo = (NodoBloque) visit(ctx.bloque());
        return new NodoWhile(linea(ctx), columna(ctx), condicion, cuerpo.getSentencias());
    }

    @Override
    public NodoAST visitSDoWhile(PIGParser.SDoWhileContext ctx) {
        NodoBloque cuerpo = (NodoBloque) visit(ctx.bloque());
        NodoAST condicion = visit(ctx.expresion());
        return new NodoDoWhile(linea(ctx), columna(ctx), cuerpo.getSentencias(), condicion);
    }

    @Override
    public NodoAST visitSFor(PIGParser.SForContext ctx) {
        NodoAST inicializacion = ctx.forInit() != null ? visit(ctx.forInit()) : null;
        NodoAST condicion = ctx.expresion() != null ? visit(ctx.expresion()) : null;
        NodoAST actualizacion = ctx.forActualizacion() != null ? visit(ctx.forActualizacion()) : null;
        NodoBloque cuerpo = (NodoBloque) visit(ctx.bloque());
        return new NodoFor(linea(ctx), columna(ctx), inicializacion, condicion, actualizacion,
                cuerpo.getSentencias());
    }

    @Override
    public NodoAST visitForInit(PIGParser.ForInitContext ctx) {
        if (ctx.ESTO() != null) {
            NodoTipo tipo = (NodoTipo) visit(ctx.tipo());
            String nombre = ctx.ID().getText();
            NodoAST inicial = visit(ctx.expresion());
            return new NodoDeclaracion(linea(ctx), columna(ctx), tipo, nombre, Collections.emptyList(), inicial);
        }
        NodoAST destino = visit(ctx.destino());
        NodoAST valor = visit(ctx.expresion());
        return new NodoAsignacion(linea(ctx), columna(ctx), destino, "=", valor);
    }

    @Override
    public NodoAST visitForActualizacion(PIGParser.ForActualizacionContext ctx) {
        NodoAST destino = visit(ctx.destino());
        if (ctx.INCREMENTO() != null || ctx.DECREMENTO() != null) {
            String operador = ctx.INCREMENTO() != null ? "++" : "--";
            return new NodoIncrementoDecremento(linea(ctx), columna(ctx), destino, operador);
        }
        NodoAST valor = visit(ctx.expresion());
        return new NodoAsignacion(linea(ctx), columna(ctx), destino, "=", valor);
    }

    @Override
    public NodoAST visitSBreak(PIGParser.SBreakContext ctx) {
        return new NodoBreak(linea(ctx), columna(ctx));
    }

    @Override
    public NodoAST visitSContinue(PIGParser.SContinueContext ctx) {
        return new NodoContinue(linea(ctx), columna(ctx));
    }

    @Override
    public NodoAST visitSImprimir(PIGParser.SImprimirContext ctx) {
        List<NodoAST> expresiones = new ArrayList<>();
        for (PIGParser.ExpresionContext e : ctx.expresion()) {
            expresiones.add(visit(e));
        }
        return new NodoImprimir(linea(ctx), columna(ctx), expresiones, false);
    }

    @Override
    public NodoAST visitSLeer(PIGParser.SLeerContext ctx) {
        NodoAST destino = ctx.destino() != null ? visit(ctx.destino()) : null;
        return new NodoLeer(linea(ctx), columna(ctx), destino);
    }

    @Override
    public NodoAST visitSExpresion(PIGParser.SExpresionContext ctx) {
        return new NodoExpresionSentencia(linea(ctx), columna(ctx), visit(ctx.expresion()));
    }

    @Override
    public NodoAST visitExpresion(PIGParser.ExpresionContext ctx) {
        return visit(ctx.expresionOr());
    }

    @Override
    public NodoAST visitExpresionOr(PIGParser.ExpresionOrContext ctx) {
        return plegarIzquierda(ctx, ctx.expresionAnd());
    }

    @Override
    public NodoAST visitExpresionAnd(PIGParser.ExpresionAndContext ctx) {
        return plegarIzquierda(ctx, ctx.expresionIgualdad());
    }

    @Override
    public NodoAST visitExpresionIgualdad(PIGParser.ExpresionIgualdadContext ctx) {
        return plegarIzquierda(ctx, ctx.expresionRelacional());
    }

    @Override
    public NodoAST visitExpresionRelacional(PIGParser.ExpresionRelacionalContext ctx) {
        return plegarIzquierda(ctx, ctx.expresionAditiva());
    }

    @Override
    public NodoAST visitExpresionAditiva(PIGParser.ExpresionAditivaContext ctx) {
        return plegarIzquierda(ctx, ctx.expresionMultiplicativa());
    }

    @Override
    public NodoAST visitExpresionMultiplicativa(PIGParser.ExpresionMultiplicativaContext ctx) {
        return plegarIzquierda(ctx, ctx.expresionUnaria());
    }

    @Override
    public NodoAST visitExpresionUnaria(PIGParser.ExpresionUnariaContext ctx) {
        if (ctx.expresionUnaria() != null) {
            String operador = ctx.getChild(0).getText();
            NodoAST operando = visit(ctx.expresionUnaria());
            return new NodoUnaria(linea(ctx), columna(ctx), operador, operando);
        }
        return visit(ctx.expresionPostfija());
    }

    @Override
    public NodoAST visitExpresionPostfija(PIGParser.ExpresionPostfijaContext ctx) {
        NodoAST resultado = visit(ctx.expresionPrimaria());
        for (PIGParser.SufijoContext sufijo : ctx.sufijo()) {
            resultado = aplicarSufijo(resultado, sufijo);
        }
        return resultado;
    }

    @Override
    public NodoAST visitExpresionPrimaria(PIGParser.ExpresionPrimariaContext ctx) {
        if (ctx.literal() != null) {
            return visit(ctx.literal());
        }
        if (ctx.ID() != null) {
            return new NodoIdentificador(linea(ctx.ID()), columna(ctx.ID()), ctx.ID().getText());
        }
        if (ctx.creacionObjeto() != null) {
            return visit(ctx.creacionObjeto());
        }
        return visit(ctx.expresion());
    }

    @Override
    public NodoAST visitCreacionObjeto(PIGParser.CreacionObjetoContext ctx) {
        String nombreClase = ctx.ID().getText();
        List<NodoAST> argumentos = new ArrayList<>();
        if (ctx.argumentos() != null) {
            for (PIGParser.ExpresionContext e : ctx.argumentos().expresion()) {
                argumentos.add(visit(e));
            }
        }
        return new NodoNuevoObjeto(linea(ctx), columna(ctx), nombreClase, argumentos);
    }

    @Override
    public NodoAST visitLiteral(PIGParser.LiteralContext ctx) {
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
        if (ctx.VERUM() != null) {
            return new NodoLiteral(linea(ctx), columna(ctx), Boolean.TRUE, TipoLiteral.BOOLEANO);
        }
        // FALSUS
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

    private NodoAST aplicarAcceso(NodoAST base, PIGParser.AccesoContext ctx) {
        if (ctx.acceso_arreglo() != null) {
            PIGParser.Acceso_arregloContext a = ctx.acceso_arreglo();
            NodoAST indice = visit(a.expresion());
            return new NodoAccesoArreglo(linea(a), columna(a), base, indice);
        }
        PIGParser.Acceso_structContext a = ctx.acceso_struct();
        return new NodoAccesoCampo(linea(a), columna(a), base, a.ID().getText());
    }

    private NodoAST aplicarSufijo(NodoAST base, PIGParser.SufijoContext ctx) {
        if (ctx.CA() != null) {
            NodoAST indice = visit(ctx.expresion());
            return new NodoAccesoArreglo(linea(ctx), columna(ctx), base, indice);
        }
        if (ctx.PUNTO() != null) {
            return new NodoAccesoCampo(linea(ctx), columna(ctx), base, ctx.ID().getText());
        }
        List<NodoAST> argumentos = new ArrayList<>();
        if (ctx.argumentos() != null) {
            for (PIGParser.ExpresionContext e : ctx.argumentos().expresion()) {
                argumentos.add(visit(e));
            }
        }
        return new NodoLlamada(linea(ctx), columna(ctx), base, argumentos);
    }
}
