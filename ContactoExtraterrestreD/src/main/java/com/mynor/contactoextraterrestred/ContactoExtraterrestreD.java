/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mynor.contactoextraterrestred;

import com.mynor.contactoextraterrestred.antlr4.*;
import com.mynor.contactoextraterrestred.ast.CreadorASTPIG;
import com.mynor.contactoextraterrestred.ast.CreadorASTY;
import com.mynor.contactoextraterrestred.ast.CreadorASTZ;
import com.mynor.contactoextraterrestred.ast.NodoAST;
import com.mynor.contactoextraterrestred.ast.NodoPrograma;
import com.mynor.contactoextraterrestred.ast.Programa;
import com.mynor.contactoextraterrestred.ast.visitantes.codigointermedio.GeneradorCuartetas;
import com.mynor.contactoextraterrestred.ast.visitantes.semantico.AnalizadorSemantico;
import com.mynor.contactoextraterrestred.errorlistener.ErrorLexico;
import com.mynor.contactoextraterrestred.errorlistener.ErrorLexicoListener;
import com.mynor.contactoextraterrestred.errorlistener.ErrorSintacticoListener;
import com.mynor.contactoextraterrestred.utils.ImpresorAST;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.function.Function;
import org.antlr.v4.runtime.*;

/**
 *
 * @author mynordma
 */
public class ContactoExtraterrestreD {
    
    static String rutaSalida = "/home/mynordma/ContactoExtraterrestreD/doc/salida.txt";

    public static void main(String[] args) {
        try {
            test();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    static void test() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(rutaSalida))) {

            ResultadoAnalisis resultadoY = analizar(
                    "/home/mynordma/ContactoExtraterrestreD/doc/entrada.y",
                    "Y",
                    YLexer::new,
                    YParser::new,
                    YParser::s,
                    writer
            );

            ResultadoAnalisis resultadoZ = analizar(
                    "/home/mynordma/ContactoExtraterrestreD/doc/entrada.z",
                    "Z",
                    ZLexer::new,
                    ZParser::new,
                    ZParser::unidad,
                    writer
            );

            ResultadoAnalisis resultadoPIG = analizar(
                    "/home/mynordma/ContactoExtraterrestreD/doc/entrada.pig",
                    "PIG",
                    PIGLexer::new,
                    PIGParser::new,
                    PIGParser::programa,
                    writer
            );

            writer.println("\n--- Errores ---");

            imprimirErrores("Y", resultadoY, writer);
            imprimirErrores("Z", resultadoZ, writer);
            imprimirErrores("PIG", resultadoPIG, writer);
            
            // Creacion de los arboles de sintaxis abstracta
            
            Programa programa = new Programa();
            
            CreadorASTY creadorASTY = new CreadorASTY();
            NodoAST raizY = creadorASTY.visit(resultadoY.arbol);
            programa.agregarArchivo(raizY);
            
            CreadorASTZ creadorASTZ = new CreadorASTZ();
            NodoAST raizZ = creadorASTZ.visit(resultadoZ.arbol);
            programa.agregarArchivo(raizZ);
            
            CreadorASTPIG creadorASTPIG = new CreadorASTPIG();
            NodoAST raizPIG = creadorASTPIG.visit(resultadoPIG.arbol);
            programa.agregarArchivo(raizPIG);
            
            writer.print(ImpresorAST.imprimirAST(programa));
            
            AnalizadorSemantico analizadorSemantico = new AnalizadorSemantico();
            AnalizadorSemantico.ArchivoPrograma archivoY = new AnalizadorSemantico.ArchivoPrograma("Y", (NodoPrograma) raizY);
            AnalizadorSemantico.ArchivoPrograma archivoZ = new AnalizadorSemantico.ArchivoPrograma("Z", (NodoPrograma) raizZ);
            AnalizadorSemantico.ArchivoPrograma archivoPIG = new AnalizadorSemantico.ArchivoPrograma("PIG", (NodoPrograma) raizPIG);

            analizadorSemantico.analizarProgramas(archivoY, archivoZ, archivoPIG);
            writer.println("\n--- Errores semánticos ---");

            imprimirErroresSemanticos("",analizadorSemantico, writer);
            
            GeneradorCuartetas generadorCuartetas = new GeneradorCuartetas();
            //generadorCuartetas.generarProgramas((NodoPrograma) raizY, (NodoPrograma) raizZ, (NodoPrograma) raizPIG);
            //List<Cuarteto> cuartetas = generadorCuartetas.getCodigo();
            writer.append(generadorCuartetas.imprimirCodigo());
        }
    }

    static <L extends Lexer, P extends Parser> ResultadoAnalisis analizar(
            String archivo,
            String titulo,
            Function<CharStream, L> crearLexer,
            Function<TokenStream, P> crearParser,
            Function<P, ParserRuleContext> reglaInicial,
            PrintWriter writer) throws IOException {

        CharStream inputLexico = CharStreams.fromFileName(archivo);
        L lexerLexico = crearLexer.apply(inputLexico);
        List<ErrorLexico> erroresLexicos = imprimirTokens(lexerLexico, titulo, writer);

        CharStream inputSintactico = CharStreams.fromFileName(archivo);
        L lexerSintactico = crearLexer.apply(inputSintactico);
        CommonTokenStream tokens = new CommonTokenStream(lexerSintactico);

        P parser = crearParser.apply(tokens);
        parser.removeErrorListeners();

        ErrorSintacticoListener errorListener = new ErrorSintacticoListener();
        parser.addErrorListener(errorListener);

        ParserRuleContext arbol = reglaInicial.apply(parser);

        return new ResultadoAnalisis(arbol, erroresLexicos, errorListener.getErrores());
    }

    static List<ErrorLexico> imprimirTokens(Lexer lexer, String titulo, PrintWriter writer) {

        ErrorLexicoListener listener = new ErrorLexicoListener();

        lexer.removeErrorListeners();
        lexer.addErrorListener(listener);

        writer.println("----- TOKENS " + titulo + " -----");

        Token token;

        while ((token = lexer.nextToken()).getType() != Token.EOF) {

            String nombreToken = lexer.getVocabulary().getSymbolicName(token.getType());

            writer.printf(
                    "Línea %-3d Columna %-3d | %-20s | %s%n",
                    token.getLine(),
                    token.getCharPositionInLine() + 1,
                    nombreToken,
                    token.getText()
            );
        }

        return listener.getErrores();
    }

    static void imprimirErrores(String titulo, ResultadoAnalisis resultado, PrintWriter writer) {
        writer.println("\n" + titulo + ":");
        resultado.erroresLexicos().forEach(writer::println);
        resultado.erroresSintacticos().forEach(writer::println);
    }

    static void imprimirErroresSemanticos(String titulo, AnalizadorSemantico analizador, PrintWriter writer) {
        writer.println("\n" + titulo + ":");
        analizador.getErrores().forEach(writer::println);
    }

    record ResultadoAnalisis(
            ParserRuleContext arbol,
            List<ErrorLexico> erroresLexicos,
            List<String> erroresSintacticos) {
    }
}