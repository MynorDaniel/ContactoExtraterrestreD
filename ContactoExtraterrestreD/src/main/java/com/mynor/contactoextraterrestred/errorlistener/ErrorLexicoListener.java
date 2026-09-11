/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mynor.contactoextraterrestred.errorlistener;

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 *
 * @author mynordma
 */
public class ErrorLexicoListener extends BaseErrorListener {

    private final List<ErrorLexico> errores = new ArrayList<>();

    @Override
    public void syntaxError(
            Recognizer<?, ?> recognizer,
            Object offendingSymbol,
            int line,
            int charPositionInLine,
            String msg,
            RecognitionException e) {

        errores.add(
                new ErrorLexico(
                        line,
                        charPositionInLine + 1,
                        msg
                )
        );
    }

    public List<ErrorLexico> getErrores() {
        return errores;
    }
}
