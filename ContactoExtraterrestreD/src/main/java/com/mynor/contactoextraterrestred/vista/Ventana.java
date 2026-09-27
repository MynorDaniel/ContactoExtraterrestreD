/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mynor.contactoextraterrestred.vista;

import com.mynor.contactoextraterrestred.antlr4.PIGLexer;
import com.mynor.contactoextraterrestred.antlr4.PIGParser;
import com.mynor.contactoextraterrestred.antlr4.YLexer;
import com.mynor.contactoextraterrestred.antlr4.YParser;
import com.mynor.contactoextraterrestred.antlr4.ZLexer;
import com.mynor.contactoextraterrestred.antlr4.ZParser;
import com.mynor.contactoextraterrestred.ast.CreadorASTPIG;
import com.mynor.contactoextraterrestred.ast.CreadorASTY;
import com.mynor.contactoextraterrestred.ast.CreadorASTZ;
import com.mynor.contactoextraterrestred.ast.NodoAST;
import com.mynor.contactoextraterrestred.ast.NodoPrograma;
import com.mynor.contactoextraterrestred.ast.visitantes.codigointermedio.GeneradorC;
import com.mynor.contactoextraterrestred.ast.visitantes.codigointermedio.GeneradorCuartetas;
import com.mynor.contactoextraterrestred.ast.visitantes.semantico.AnalizadorSemantico;
import com.mynor.contactoextraterrestred.errorlistener.ErrorLexico;
import com.mynor.contactoextraterrestred.errorlistener.ErrorLexicoListener;
import com.mynor.contactoextraterrestred.errorlistener.ErrorSintacticoListener;
import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 *
 * @author mynordma
 */
public class Ventana extends javax.swing.JFrame {

    public Ventana() {

        initComponents();

        setTitle("Editor - ContactoExtraterrestr3D");
        setLocationRelativeTo(null);

        this.getContentPane().setBackground(new Color(30, 30, 30));

        outputTextArea.setEditable(false);

        outputTextArea.setBackground(new Color(37, 37, 38));
        outputTextArea.setForeground(new Color(212, 212, 212));
        outputTextArea.setCaretColor(Color.WHITE);
        outputTextArea.setSelectionColor(new Color(38, 79, 120));
        outputTextArea.setDragEnabled(true);

        nuevaVentana();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        outputTextArea = new javax.swing.JTextArea();
        execBtn = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        ventanas = new javax.swing.JTabbedPane();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setForeground(java.awt.Color.black);

        outputTextArea.setBackground(new java.awt.Color(43, 43, 43));
        outputTextArea.setColumns(20);
        outputTextArea.setFont(new java.awt.Font("Monospaced", 0, 15)); // NOI18N
        outputTextArea.setForeground(new java.awt.Color(169, 183, 198));
        outputTextArea.setRows(5);
        outputTextArea.setDragEnabled(true);
        jScrollPane2.setViewportView(outputTextArea);

        execBtn.setBackground(new java.awt.Color(51, 51, 51));
        execBtn.setForeground(new java.awt.Color(255, 255, 255));
        execBtn.setText("Ejecutar");
        execBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                execBtnActionPerformed(evt);
            }
        });

        jButton1.setText("Nueva pestaña");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Eliminar pestaña actual");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 868, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jButton2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(execBtn))))
                    .addComponent(ventanas))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(ventanas, javax.swing.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
                .addGap(28, 28, 28)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 344, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(execBtn)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addGap(9, 9, 9))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void execBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_execBtnActionPerformed

        try {
            outputTextArea.setText("");

            List<NodoAST> arboles = new ArrayList<>();
            AnalizadorSemantico analizadorSemantico = new AnalizadorSemantico();
            List<AnalizadorSemantico.ArchivoPrograma> programas = new ArrayList<>();
            StringBuilder errores = new StringBuilder();

            for (int i = 0; i < ventanas.getTabCount(); i++) {

                EditorPanel ventanaEditor = (EditorPanel) ventanas.getComponentAt(i);

                String codigo = ventanaEditor.getCodigo();
                Lenguaje lenguaje = ventanaEditor.getLenguaje();

                switch (lenguaje) {
                    case Lenguaje.Y -> {

                        CharStream input = CharStreams.fromString(codigo);
                        YLexer lexer = new YLexer(input);
                        ErrorLexicoListener listener = new ErrorLexicoListener();
                        lexer.removeErrorListeners();
                        lexer.addErrorListener(listener);
                        CommonTokenStream tokens = new CommonTokenStream(lexer);
                        for (ErrorLexico error : listener.getErrores()) {
                            errores.append(error.toString()).append("\n");
                        }

                        YParser parser = new YParser(tokens);
                        parser.removeErrorListeners();
                        ErrorSintacticoListener errorListener = new ErrorSintacticoListener();
                        parser.addErrorListener(errorListener);
                        YParser.SContext tree = parser.s();
                        CreadorASTY creadorAST = new CreadorASTY();
                        NodoAST raiz = creadorAST.visit(tree);
                        for (String error : errorListener.getErrores()) {
                            errores.append(error).append("\n");
                        }

                        AnalizadorSemantico.ArchivoPrograma archivoY = new AnalizadorSemantico.ArchivoPrograma("Y", (NodoPrograma) raiz);
                        programas.add(archivoY);

                        arboles.add(raiz);
                    }
                    case Lenguaje.Z -> {
                        CharStream input = CharStreams.fromString(codigo);
                        ZLexer lexer = new ZLexer(input);
                        ErrorLexicoListener listener = new ErrorLexicoListener();
                        lexer.removeErrorListeners();
                        lexer.addErrorListener(listener);
                        CommonTokenStream tokens = new CommonTokenStream(lexer);
                        for (ErrorLexico error : listener.getErrores()) {
                            errores.append(error.toString()).append("\n");
                        }

                        ZParser parser = new ZParser(tokens);
                        parser.removeErrorListeners();
                        ErrorSintacticoListener errorListener = new ErrorSintacticoListener();
                        parser.addErrorListener(errorListener);
                        ZParser.UnidadContext tree = parser.unidad();
                        CreadorASTZ creadorAST = new CreadorASTZ();
                        NodoAST raiz = creadorAST.visit(tree);
                        for (String error : errorListener.getErrores()) {
                            errores.append(error).append("\n");
                        }

                        AnalizadorSemantico.ArchivoPrograma archivoZ = new AnalizadorSemantico.ArchivoPrograma("Z", (NodoPrograma) raiz);
                        programas.add(archivoZ);

                        arboles.add(raiz);
                    }
                    case Lenguaje.PIG -> {
                        CharStream input = CharStreams.fromString(codigo);
                        PIGLexer lexer = new PIGLexer(input);
                        ErrorLexicoListener listener = new ErrorLexicoListener();
                        lexer.removeErrorListeners();
                        lexer.addErrorListener(listener);
                        CommonTokenStream tokens = new CommonTokenStream(lexer);
                        for (ErrorLexico error : listener.getErrores()) {
                            errores.append(error.toString()).append("\n");
                        }

                        PIGParser parser = new PIGParser(tokens);
                        parser.removeErrorListeners();
                        ErrorSintacticoListener errorListener = new ErrorSintacticoListener();
                        parser.addErrorListener(errorListener);
                        PIGParser.ProgramaContext tree = parser.programa();
                        CreadorASTPIG creadorAST = new CreadorASTPIG();
                        NodoAST raiz = creadorAST.visit(tree);
                        for (String error : errorListener.getErrores()) {
                            errores.append(error).append("\n");
                        }

                        AnalizadorSemantico.ArchivoPrograma archivoPIG = new AnalizadorSemantico.ArchivoPrograma("PIG", (NodoPrograma) raiz);
                        programas.add(archivoPIG);

                        arboles.add(raiz);
                    }
                    case Lenguaje.C -> {

                    }
                }

            }

            analizadorSemantico.analizarProgramas(programas.toArray(AnalizadorSemantico.ArchivoPrograma[]::new));
            for (String errorSemantico : analizadorSemantico.getErrores()) {
                errores.append(errorSemantico).append("\n");
            }

            log(errores.toString());

            GeneradorCuartetas generadorCuartetas = new GeneradorCuartetas();
            generadorCuartetas.generarProgramas(arboles.toArray(NodoPrograma[]::new));

            log(generadorCuartetas.imprimirCodigo());

            GeneradorC generadorC = new GeneradorC();
            String codigoC = generadorC.generar(generadorCuartetas);

            nuevaVentana(Lenguaje.C, codigoC);

            Path archivoC = Paths.get(System.getProperty("user.dir")).resolve("programa.c");

            try {

                Files.writeString(archivoC, codigoC);

                log("Archivo C creado en: " + archivoC.toAbsolutePath());

            } catch (IOException ex) {

                log("Error al crear el archivo C: " + ex.getMessage());
            }
        } catch (Exception e) {
            log("Error grave irrecuperable durante el analisis, revisa la estructura de los archivos");
        }
    }//GEN-LAST:event_execBtnActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        nuevaVentana();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        int indice = ventanas.getSelectedIndex();

        if (indice != -1) {
            ventanas.removeTabAt(indice);
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void log(String s) {
        outputTextArea.append("\n");
        outputTextArea.append(s);
    }

    private void nuevaVentana() {

        EditorPanel panel = new EditorPanel();

        int numero = ventanas.getTabCount() + 1;

        ventanas.addTab("Editor " + numero, panel);
        ventanas.setSelectedComponent(panel);
    }

    private void nuevaVentana(Lenguaje lenguaje, String codigo) {

        EditorPanel panel = new EditorPanel(lenguaje, codigo);

        int numero = ventanas.getTabCount() + 1;

        ventanas.addTab("Editor " + numero, panel);
        ventanas.setSelectedComponent(panel);
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton execBtn;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea outputTextArea;
    private javax.swing.JTabbedPane ventanas;
    // End of variables declaration//GEN-END:variables

}
