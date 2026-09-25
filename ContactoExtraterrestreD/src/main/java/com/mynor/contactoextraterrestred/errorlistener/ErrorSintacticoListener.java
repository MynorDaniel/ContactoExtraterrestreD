/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mynor.contactoextraterrestred.errorlistener;

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.*;

/**
 *
 * @author mynordma
 */
public class ErrorSintacticoListener extends BaseErrorListener {

    private final List<String> errores = new ArrayList<>();

    @Override
    public void syntaxError(
            Recognizer<?, ?> recognizer,
            Object offendingSymbol,
            int line,
            int charPositionInLine,
            String msg,
            RecognitionException e) {

        errores.add(
            "Línea " + line +
            ", columna " + charPositionInLine +
            ": " + msg
        );
    }

    public List<String> getErrores() {
        return errores;
    }
}
