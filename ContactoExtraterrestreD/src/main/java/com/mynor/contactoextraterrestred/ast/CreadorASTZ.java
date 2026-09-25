/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mynor.contactoextraterrestred.ast;

import com.mynor.contactoextraterrestred.antlr4.ZBaseVisitor;
import com.mynor.contactoextraterrestred.antlr4.ZParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author mynordma
 */
public class CreadorASTZ extends ZBaseVisitor<NodoAST> {

    @Override
    public NodoAST visitUnidad(ZParser.UnidadContext ctx) {
        NodoEstructura clase = (NodoEstructura) visit(ctx.clase());
        return new NodoPrograma(linea(ctx), columna(ctx),
                Collections.emptyList(), 
                Collections.emptyList(), 
                Collections.emptyList(), 
                Collections.emptyList(), 
                clase,
                Collections.emptyList()); 
    }

    @Override
    public NodoAST visitClase(ZParser.ClaseContext ctx) {
        String nombre = ctx.ID().getText();
        List<NodoAtributo> atributos = new ArrayList<>();
        List<NodoFuncion> constructores = new ArrayList<>();
        List<NodoFuncion> metodos = new ArrayList<>();

        for (ZParser.MiembroContext m : ctx.miembro()) {
            NodoAST nodo = visit(m);
            if (nodo instanceof NodoAtributo nodoAtributo) {
                atributos.add(nodoAtributo);
            } else if (nodo instanceof NodoFuncion && ((NodoFuncion) nodo).isEsConstructor()) {
                constructores.add((NodoFuncion) nodo);
            } else {
                metodos.add((NodoFuncion) nodo);
            }
        }

        return new NodoEstructura(linea(ctx), columna(ctx), nombre, atributos, constructores, metodos);
    }

    @Override
    public NodoAST visitMiembro(ZParser.MiembroContext ctx) {
        return visit(ctx.getChild(0));
    }

    @Override
    public NodoAST visitAtributo(ZParser.AtributoContext ctx) {
        NodoTipo tipo = (NodoTipo) visit(ctx.tipo());
        String nombre = ctx.ID().getText();
        NodoAST valorInicial = ctx.expresion() != null ? visit(ctx.expresion()) : null;
        return new NodoAtributo(linea(ctx), columna(ctx), tipo, nombre, Collections.emptyList(), valorInicial);
    }

    @Override
    public NodoAST visitConstructor(ZParser.ConstructorContext ctx) {
        String nombre = ctx.ID().getText();
        List<NodoParametro> parametros = new ArrayList<>();
        if (ctx.parametros() != null) {
            for (ZParser.ParametroContext p : ctx.parametros().parametro()) {
                parametros.add((NodoParametro) visit(p));
            }
        }
        NodoBloque cuerpo = (NodoBloque) visit(ctx.bloque());
        return new NodoFuncion(linea(ctx), columna(ctx), nombre, parametros, null,
                cuerpo.getSentencias(), true);
    }

    @Override
    public NodoAST visitMetodo(ZParser.MetodoContext ctx) {
        NodoTipo tipoRetorno = (NodoTipo) visit(ctx.tipo());
        String nombre = ctx.ID().getText();
        List<NodoParametro> parametros = new ArrayList<>();
        if (ctx.parametros() != null) {
            for (ZParser.ParametroContext p : ctx.parametros().parametro()) {
                parametros.add((NodoParametro) visit(p));
            }
        }
        NodoBloque cuerpo = (NodoBloque) visit(ctx.bloque());
        return new NodoFuncion(linea(ctx), columna(ctx), nombre, parametros, tipoRetorno,
                cuerpo.getSentencias(), false);
    }

    @Override
    public NodoAST visitParametro(ZParser.ParametroContext ctx) {
        NodoTipo tipo = (NodoTipo) visit(ctx.tipo());
        String nombre = ctx.ID().getText();
        return new NodoParametro(linea(ctx), columna(ctx), tipo, nombre);
    }

    @Override
    public NodoAST visitTipoBase(ZParser.TipoBaseContext ctx) {
        return new NodoTipo(linea(ctx), columna(ctx), ctx.getText(), 0);
    }

    @Override
    public NodoAST visitTipo(ZParser.TipoContext ctx) {
        NodoTipo base = (NodoTipo) visit(ctx.tipoBase());
        int niveles = ctx.CA().size();
        return new NodoTipo(linea(ctx), columna(ctx), base.getNombre(), niveles);
    }

    @Override
    public NodoAST visitBloque(ZParser.BloqueContext ctx) {
        List<NodoAST> sentencias = new ArrayList<>();
        for (ZParser.SentenciaContext s : ctx.sentencia()) {
            sentencias.add(visit(s));
        }
        return new NodoBloque(linea(ctx), columna(ctx), sentencias);
    }

    @Override
    public NodoAST visitSentencia(ZParser.SentenciaContext ctx) {
        return visit(ctx.getChild(0));
    }

    @Override
    public NodoAST visitDeclaracionLocal(ZParser.DeclaracionLocalContext ctx) {
        NodoTipo tipo = (NodoTipo) visit(ctx.tipo());
        String nombre = ctx.ID().getText();
        NodoAST inicializador = ctx.inicializacion() != null ? visit(ctx.inicializacion()) : null;
        return new NodoDeclaracion(linea(ctx), columna(ctx), tipo, nombre, Collections.emptyList(), inicializador);
    }

    @Override
    public NodoAST visitInicializacion(ZParser.InicializacionContext ctx) {
        return visit(ctx.getChild(0));
    }

    @Override
    public NodoAST visitListaExpresiones(ZParser.ListaExpresionesContext ctx) {
        List<NodoAST> elementos = new ArrayList<>();
        for (ZParser.ExpresionContext e : ctx.expresion()) {
            elementos.add(visit(e));
        }
        return new NodoListaExpresiones(linea(ctx), columna(ctx), elementos);
    }

    @Override
    public NodoAST visitAsignacion(ZParser.AsignacionContext ctx) {
        NodoAST destino = visit(ctx.destino());
        String operador = ctx.getChild(1).getText(); // "=", "+=", "-=" o "*="
        NodoAST valor = visit(ctx.expresion());
        return new NodoAsignacion(linea(ctx), columna(ctx), destino, operador, valor);
    }

    @Override
    public NodoAST visitIncrementoDecremento(ZParser.IncrementoDecrementoContext ctx) {
        NodoAST destino = visit(ctx.destino());
        String operador = ctx.INCREMENTO() != null ? "++" : "--";
        return new NodoIncrementoDecremento(linea(ctx), columna(ctx), destino, operador);
    }

    @Override
    public NodoAST visitDestino(ZParser.DestinoContext ctx) {
        NodoAST resultado = new NodoIdentificador(linea(ctx.ID()), columna(ctx.ID()), ctx.ID().getText());
        for (ZParser.AccesoContext acceso : ctx.acceso()) {
            resultado = aplicarAcceso(resultado, acceso);
        }
        return resultado;
    }

    @Override
    public NodoAST visitSIf(ZParser.SIfContext ctx) {
        NodoAST condicion = visit(ctx.expresion());
        List<NodoAST> cuerpoSi = comoLista(visit(ctx.sentencia(0)));
        List<NodoAST> cuerpoSino = ctx.sentencia().size() > 1
                ? comoLista(visit(ctx.sentencia(1)))
                : null;
       
        return new NodoIf(linea(ctx), columna(ctx), condicion, cuerpoSi, Collections.emptyList(), cuerpoSino);
    }

    @Override
    public NodoAST visitSSwitch(ZParser.SSwitchContext ctx) {
        NodoAST expresion = visit(ctx.expresion());

        List<NodoCaso> casos = new ArrayList<>();
        for (ZParser.CasoSwitchContext c : ctx.casoSwitch()) {
            casos.add((NodoCaso) visit(c));
        }

        List<NodoAST> casoPorDefecto = null;
        if (ctx.defaultSwitch() != null) {
            casoPorDefecto = new ArrayList<>();
            for (ZParser.SentenciaContext s : ctx.defaultSwitch().sentencia()) {
                casoPorDefecto.add(visit(s));
            }
        }

        return new NodoSwitch(linea(ctx), columna(ctx), expresion, casos, casoPorDefecto);
    }

    @Override
    public NodoAST visitCasoSwitch(ZParser.CasoSwitchContext ctx) {
        NodoLiteral valor = (NodoLiteral) visit(ctx.literal());
        List<NodoAST> cuerpo = new ArrayList<>();
        for (ZParser.SentenciaContext s : ctx.sentencia()) {
            cuerpo.add(visit(s));
        }
        return new NodoCaso(linea(ctx), columna(ctx), valor, cuerpo);
    }

    @Override
    public NodoAST visitSFor(ZParser.SForContext ctx) {
        NodoAST inicializacion = ctx.forInit() != null ? visit(ctx.forInit()) : null;
        NodoAST condicion = ctx.expresion() != null ? visit(ctx.expresion()) : null;
        NodoAST actualizacion = ctx.forActualizacion() != null ? visit(ctx.forActualizacion()) : null;
        List<NodoAST> cuerpo = comoLista(visit(ctx.sentencia()));
        return new NodoFor(linea(ctx), columna(ctx), inicializacion, condicion, actualizacion, cuerpo);
    }

    @Override
    public NodoAST visitForInit(ZParser.ForInitContext ctx) {
        if (ctx.tipo() != null) {
            NodoTipo tipo = (NodoTipo) visit(ctx.tipo());
            String nombre = ctx.ID().getText();
            NodoAST inicial = ctx.expresion() != null ? visit(ctx.expresion()) : null;
            return new NodoDeclaracion(linea(ctx), columna(ctx), tipo, nombre, Collections.emptyList(), inicial);
        }
        NodoAST destino = visit(ctx.destino());
        String operador = ctx.getChild(1).getText();
        NodoAST valor = visit(ctx.expresion());
        return new NodoAsignacion(linea(ctx), columna(ctx), destino, operador, valor);
    }

    @Override
    public NodoAST visitForActualizacion(ZParser.ForActualizacionContext ctx) {
        NodoAST destino = visit(ctx.destino());
        if (ctx.INCREMENTO() != null || ctx.DECREMENTO() != null) {
            String operador = ctx.INCREMENTO() != null ? "++" : "--";
            return new NodoIncrementoDecremento(linea(ctx), columna(ctx), destino, operador);
        }
        String operador = ctx.getChild(1).getText();
        NodoAST valor = visit(ctx.expresion());
        return new NodoAsignacion(linea(ctx), columna(ctx), destino, operador, valor);
    }

    @Override
    public NodoAST visitSWhile(ZParser.SWhileContext ctx) {
        NodoAST condicion = visit(ctx.expresion());
        List<NodoAST> cuerpo = comoLista(visit(ctx.sentencia()));
        return new NodoWhile(linea(ctx), columna(ctx), condicion, cuerpo);
    }

    @Override
    public NodoAST visitSDoWhile(ZParser.SDoWhileContext ctx) {
        List<NodoAST> cuerpo = comoLista(visit(ctx.sentencia()));
        NodoAST condicion = visit(ctx.expresion());
        return new NodoDoWhile(linea(ctx), columna(ctx), cuerpo, condicion);
    }

    @Override
    public NodoAST visitSBreak(ZParser.SBreakContext ctx) {
        return new NodoBreak(linea(ctx), columna(ctx));
    }

    @Override
    public NodoAST visitSContinue(ZParser.SContinueContext ctx) {
        return new NodoContinue(linea(ctx), columna(ctx));
    }

    @Override
    public NodoAST visitSReturn(ZParser.SReturnContext ctx) {
        NodoAST valor = ctx.expresion() != null ? visit(ctx.expresion()) : null;
        return new NodoReturn(linea(ctx), columna(ctx), valor);
    }

    @Override
    public NodoAST visitSPrint(ZParser.SPrintContext ctx) {
        NodoAST expresion = visit(ctx.expresion());
        return new NodoImprimir(linea(ctx), columna(ctx), Collections.singletonList(expresion), false);
    }

    @Override
    public NodoAST visitSPrintln(ZParser.SPrintlnContext ctx) {
        NodoAST expresion = visit(ctx.expresion());
        return new NodoImprimir(linea(ctx), columna(ctx), Collections.singletonList(expresion), true);
    }

    @Override
    public NodoAST visitSExpresion(ZParser.SExpresionContext ctx) {
        return new NodoExpresionSentencia(linea(ctx), columna(ctx), visit(ctx.expresion()));
    }

    @Override
    public NodoAST visitExpresion(ZParser.ExpresionContext ctx) {
        return visit(ctx.expresionTernario());
    }

    @Override
    public NodoAST visitExpresionTernario(ZParser.ExpresionTernarioContext ctx) {
        NodoAST condicion = visit(ctx.expresionOr());
        if (ctx.INTERROGACION() == null) {
            return condicion;
        }
        NodoAST siVerdadero = visit(ctx.expresion(0));
        NodoAST siFalso = visit(ctx.expresion(1));
        return new NodoTernaria(linea(ctx), columna(ctx), condicion, siVerdadero, siFalso);
    }

    @Override
    public NodoAST visitExpresionOr(ZParser.ExpresionOrContext ctx) {
        return plegarIzquierda(ctx, ctx.expresionAnd());
    }

    @Override
    public NodoAST visitExpresionAnd(ZParser.ExpresionAndContext ctx) {
        return plegarIzquierda(ctx, ctx.expresionIgualdad());
    }

    @Override
    public NodoAST visitExpresionIgualdad(ZParser.ExpresionIgualdadContext ctx) {
        return plegarIzquierda(ctx, ctx.expresionRelacional());
    }

    @Override
    public NodoAST visitExpresionRelacional(ZParser.ExpresionRelacionalContext ctx) {
        return plegarIzquierda(ctx, ctx.expresionAditiva());
    }

    @Override
    public NodoAST visitExpresionAditiva(ZParser.ExpresionAditivaContext ctx) {
        return plegarIzquierda(ctx, ctx.expresionMultiplicativa());
    }

    @Override
    public NodoAST visitExpresionMultiplicativa(ZParser.ExpresionMultiplicativaContext ctx) {
        return plegarIzquierda(ctx, ctx.expresionUnaria());
    }

    @Override
    public NodoAST visitExpresionUnaria(ZParser.ExpresionUnariaContext ctx) {
        if (ctx.expresionUnaria() != null) {
            String operador = ctx.getChild(0).getText(); // NEGACION | MENOS | MAS
            NodoAST operando = visit(ctx.expresionUnaria());
            return new NodoUnaria(linea(ctx), columna(ctx), operador, operando);
        }
        return visit(ctx.expresionPostfija());
    }

    @Override
    public NodoAST visitExpresionPostfija(ZParser.ExpresionPostfijaContext ctx) {
        NodoAST resultado = visit(ctx.expresionPrimaria());
        for (ZParser.SufijoContext sufijo : ctx.sufijo()) {
            resultado = aplicarSufijo(resultado, sufijo);
        }
        return resultado;
    }

    @Override
    public NodoAST visitExpresionPrimaria(ZParser.ExpresionPrimariaContext ctx) {
        if (ctx.literal() != null) {
            return visit(ctx.literal());
        }
        if (ctx.ID() != null) {
            return new NodoIdentificador(linea(ctx.ID()), columna(ctx.ID()), ctx.ID().getText());
        }
        if (ctx.creacionObjeto() != null) {
            return visit(ctx.creacionObjeto());
        }
        if (ctx.creacionArreglo() != null) {
            return visit(ctx.creacionArreglo());
        }
        if (ctx.READLN() != null) {
            return new NodoLecturaEntrada(linea(ctx), columna(ctx));
        }
        // PAREN_A expresion PAREN_C
        return visit(ctx.expresion());
    }

    @Override
    public NodoAST visitCreacionObjeto(ZParser.CreacionObjetoContext ctx) {
        String nombreClase = ctx.ID().getText();
        List<NodoAST> argumentos = new ArrayList<>();
        if (ctx.argumentos() != null) {
            for (ZParser.ExpresionContext e : ctx.argumentos().expresion()) {
                argumentos.add(visit(e));
            }
        }
        return new NodoNuevoObjeto(linea(ctx), columna(ctx), nombreClase, argumentos);
    }

    @Override
    public NodoAST visitCreacionArreglo(ZParser.CreacionArregloContext ctx) {
        String tipoBase = ctx.tipoBase().getText();
        List<NodoAST> dimensiones = new ArrayList<>();
        for (ZParser.ExpresionContext e : ctx.expresion()) {
            dimensiones.add(visit(e));
        }
        return new NodoNuevoArreglo(linea(ctx), columna(ctx), tipoBase, dimensiones);
    }

    @Override
    public NodoAST visitLiteral(ZParser.LiteralContext ctx) {
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
            String valor = texto.substring(1, texto.length() - 1); // quitar comillas
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
        if (ctx.FALSO() != null) {
            return new NodoLiteral(linea(ctx), columna(ctx), Boolean.FALSE, TipoLiteral.BOOLEANO);
        }
        // NULL
        return new NodoLiteral(linea(ctx), columna(ctx), null, TipoLiteral.NULO);
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

    private List<NodoAST> comoLista(NodoAST nodo) {
        if (nodo instanceof NodoBloque nodoBloque) {
            return nodoBloque.getSentencias();
        }
        return Collections.singletonList(nodo);
    }

    private NodoAST aplicarAcceso(NodoAST base, ZParser.AccesoContext ctx) {
        if (ctx.acceso_arreglo() != null) {
            ZParser.Acceso_arregloContext a = ctx.acceso_arreglo();
            NodoAST indice = visit(a.expresion());
            return new NodoAccesoArreglo(linea(a), columna(a), base, indice);
        }
        ZParser.Acceso_structContext a = ctx.acceso_struct();
        return new NodoAccesoCampo(linea(a), columna(a), base, a.ID().getText());
    }

    private NodoAST aplicarSufijo(NodoAST base, ZParser.SufijoContext ctx) {
        if (ctx.CA() != null) {
            NodoAST indice = visit(ctx.expresion());
            return new NodoAccesoArreglo(linea(ctx), columna(ctx), base, indice);
        }
        if (ctx.PUNTO() != null) {
            return new NodoAccesoCampo(linea(ctx), columna(ctx), base, ctx.ID().getText());
        }
        List<NodoAST> argumentos = new ArrayList<>();
        if (ctx.argumentos() != null) {
            for (ZParser.ExpresionContext e : ctx.argumentos().expresion()) {
                argumentos.add(visit(e));
            }
        }
        return new NodoLlamada(linea(ctx), columna(ctx), base, argumentos);
    }
}
