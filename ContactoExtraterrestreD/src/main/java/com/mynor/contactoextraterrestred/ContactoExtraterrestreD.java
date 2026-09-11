/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mynor.contactoextraterrestred;

import com.mynor.contactoextraterrestred.antlr4.*;
import com.mynor.contactoextraterrestred.errorlistener.ErrorLexico;
import com.mynor.contactoextraterrestred.errorlistener.ErrorLexicoListener;
import java.io.IOException;
import java.util.List;
import org.antlr.v4.runtime.*;

/**
 *
 * @author mynordma
 */
public class ContactoExtraterrestreD {

    public static void main(String[] args) {
        try {
            test();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

    static void test() throws IOException {

        // .y
        String archivoY
                = "/home/mynordma/ContactoExtraterrestreD/doc/entrada.y";

        CharStream inputY
                = CharStreams.fromFileName(archivoY);

        YLexer lexerY = new YLexer(inputY);

        List<ErrorLexico> erroresY
                = imprimirTokens(lexerY, "Y");

        // .z
        String archivoZ
                = "/home/mynordma/ContactoExtraterrestreD/doc/entrada.z";

        CharStream inputZ
                = CharStreams.fromFileName(archivoZ);

        ZLexer lexerZ = new ZLexer(inputZ);

        List<ErrorLexico> erroresZ
                = imprimirTokens(lexerZ, "Z");

        // .pig
        String archivoPIG
                = "/home/mynordma/ContactoExtraterrestreD/doc/entrada.pig";

        CharStream inputPIG
                = CharStreams.fromFileName(archivoPIG);

        PIGLexer lexerPIG = new PIGLexer(inputPIG);

        List<ErrorLexico> erroresPIG
                = imprimirTokens(lexerPIG, "PIG");

        System.out.println("\n--- Errores ---");

        System.out.println("\nY:");
        erroresY.forEach(System.out::println);

        System.out.println("\nZ:");
        erroresZ.forEach(System.out::println);

        System.out.println("\nPIG:");
        erroresPIG.forEach(System.out::println);
    }

    static List<ErrorLexico> imprimirTokens(Lexer lexer, String titulo) {

        ErrorLexicoListener listener = new ErrorLexicoListener();

        lexer.removeErrorListeners();
        lexer.addErrorListener(listener);

        Token token;

        System.out.println("----- TOKENS " + titulo + " -----");

        while ((token = lexer.nextToken()).getType() != Token.EOF) {

            String nombreToken = "";

            if (lexer instanceof YLexer) {
                nombreToken
                        = YParser.VOCABULARY.getSymbolicName(token.getType());
            } else if (lexer instanceof ZLexer) {
                nombreToken
                        = ZParser.VOCABULARY.getSymbolicName(token.getType());
            } else if (lexer instanceof PIGLexer) {
                nombreToken
                        = PIGParser.VOCABULARY.getSymbolicName(token.getType());
            }

            System.out.printf(
                    "Línea %-3d Columna %-3d | %-20s | %s%n",
                    token.getLine(),
                    token.getCharPositionInLine() + 1,
                    nombreToken,
                    token.getText()
            );
        }

        return listener.getErrores();
    }
}
