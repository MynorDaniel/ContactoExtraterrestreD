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
import com.mynor.contactoextraterrestred.ast.visitantes.codigointermedio.GeneradorCuartetas;
import com.mynor.contactoextraterrestred.ast.visitantes.semantico.AnalizadorSemantico;
import com.mynor.contactoextraterrestred.ast.visitantes.semantico.Simbolo;
import com.mynor.contactoextraterrestred.ensamblador.EjecutorRars;
import com.mynor.contactoextraterrestred.ensamblador.GeneradorRiscV;
import com.mynor.contactoextraterrestred.errorlistener.ErrorLexico;
import com.mynor.contactoextraterrestred.errorlistener.ErrorLexicoListener;
import com.mynor.contactoextraterrestred.errorlistener.ErrorSintacticoListener;
import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 *
 * @author mynordma
 */
public class Ventana extends javax.swing.JFrame {

    private AnalizadorSemantico ultimoAnalisis;
    private GeneradorCuartetas ultimasCuartetas;
    private JMenuItem verTablaSimbolos;
    private JMenuItem verCuartetas;
    private EjecutorRars ejecutorActivo;

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

        crearMenuReportes();
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
        abrirCarpetaBtn = new javax.swing.JButton();
        entradaCampo = new javax.swing.JTextField();
        enviarEntradaBtn = new javax.swing.JButton();
        detenerBtn = new javax.swing.JButton();

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

        abrirCarpetaBtn.setText("Abrir carpeta");
        abrirCarpetaBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                abrirCarpetaBtnActionPerformed(evt);
            }
        });

        entradaCampo.setEnabled(false);
        entradaCampo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enviarEntrada();
            }
        });

        enviarEntradaBtn.setText("Enviar entrada");
        enviarEntradaBtn.setEnabled(false);
        enviarEntradaBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enviarEntrada();
            }
        });

        detenerBtn.setText("Detener");
        detenerBtn.setEnabled(false);
        detenerBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                detenerEjecucion();
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
                                 .addComponent(entradaCampo, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(enviarEntradaBtn)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(detenerBtn)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addGap(0, 0, Short.MAX_VALUE)
                                 .addComponent(jButton2)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(jButton1)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(abrirCarpetaBtn)
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
                     .addComponent(entradaCampo)
                     .addComponent(enviarEntradaBtn)
                     .addComponent(detenerBtn)
                     .addComponent(execBtn)
                     .addComponent(jButton1)
                     .addComponent(jButton2)
                     .addComponent(abrirCarpetaBtn))
                .addGap(9, 9, 9))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void execBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_execBtnActionPerformed

        try {
            outputTextArea.setText("");
            ultimoAnalisis = null;
            ultimasCuartetas = null;
            verTablaSimbolos.setEnabled(false);
            verCuartetas.setEnabled(false);
            ejecutorActivo = null;
            entradaCampo.setEnabled(false);
            enviarEntradaBtn.setEnabled(false);
            detenerBtn.setEnabled(false);

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

                        CharStream input = CharStreams.fromString(codigo + "\n");
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

            if (!errores.isEmpty()) {
                log(errores.toString());
                return;
            }

            GeneradorCuartetas generadorCuartetas = new GeneradorCuartetas();
            generadorCuartetas.generarProgramas(arboles.toArray(NodoPrograma[]::new));
            ultimoAnalisis = analizadorSemantico;
            ultimasCuartetas = generadorCuartetas;
            verTablaSimbolos.setEnabled(true);
            verCuartetas.setEnabled(true);

            log(generadorCuartetas.imprimirCodigo());

            String codigoRiscV = GeneradorRiscV.generar(generadorCuartetas);
            Path rars = rutaRars();
            if (rars == null || !Files.isRegularFile(rars)) {
                log("No se encontro RARS externo ni empaquetado.");
                return;
            }

            try {
                execBtn.setEnabled(false);
                log("Salida:");
                EjecutorRars ejecutor = new EjecutorRars(rars);
                ejecutorActivo = ejecutor;
                entradaCampo.setEnabled(true);
                enviarEntradaBtn.setEnabled(true);
                detenerBtn.setEnabled(true);
                entradaCampo.requestFocusInWindow();
                ejecutor.ejecutar(codigoRiscV, this::logDesdeHilo, this::logDesdeHilo,
                        codigoSalida -> SwingUtilities.invokeLater(() -> {
                            log("RARS finalizo con codigo " + codigoSalida);
                            if (ejecutorActivo == ejecutor) {
                                ejecutorActivo = null;
                                entradaCampo.setEnabled(false);
                                enviarEntradaBtn.setEnabled(false);
                                detenerBtn.setEnabled(false);
                                execBtn.setEnabled(true);
                            }
                        }));
            } catch (IOException ex) {
                log("Error al ejecutar RARS: " + ex.getMessage());
                ejecutorActivo = null;
                entradaCampo.setEnabled(false);
                enviarEntradaBtn.setEnabled(false);
                detenerBtn.setEnabled(false);
                execBtn.setEnabled(true);
            }
        } catch (Exception e) {
            log("Error grave durante el analisis: " + e.getMessage());
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

    private void abrirCarpetaBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_abrirCarpetaBtnActionPerformed
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Selecciona una carpeta");
        selector.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        selector.setAcceptAllFileFilterUsed(false);

        if (selector.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path carpeta = selector.getSelectedFile().toPath();
        List<Path> archivos;
        try (Stream<Path> rutas = Files.walk(carpeta)) {
            archivos = rutas
                    .filter(Files::isRegularFile)
                    .filter(this::esArchivoDeLenguaje)
                    .sorted(Comparator.comparing(Path::toString))
                    .toList();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo leer la carpeta: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        StringBuilder errores = new StringBuilder();
        for (Path archivo : archivos) {
            try {
                nuevaVentana(Lenguaje.desdeNombreArchivo(archivo.getFileName().toString()),
                        Files.readString(archivo), archivo, carpeta);
            } catch (IOException ex) {
                errores.append(archivo).append(": ").append(ex.getMessage()).append("\n");
            }
        }

        if (archivos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "La carpeta no contiene archivos .y, .z o .pig.",
                    "Sin archivos compatibles", JOptionPane.INFORMATION_MESSAGE);
        } else if (!errores.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No se pudieron abrir algunos archivos:\n" + errores,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_abrirCarpetaBtnActionPerformed

    private boolean esArchivoDeLenguaje(Path archivo) {
        String nombre = archivo.getFileName().toString().toLowerCase();
        return nombre.endsWith(".y") || nombre.endsWith(".z") || nombre.endsWith(".pig");
    }

    private void log(String s) {
        outputTextArea.append("\n");
        outputTextArea.append(s);
    }

    private void logDesdeHilo(String texto) {
        SwingUtilities.invokeLater(() -> outputTextArea.append(texto));
    }

    private void enviarEntrada() {
        if (ejecutorActivo == null || !ejecutorActivo.estaEjecutando()) {
            return;
        }
        String entrada = entradaCampo.getText();
        try {
            ejecutorActivo.enviar(entrada);
            log("> " + entrada);
            entradaCampo.setText("");
        } catch (IOException ex) {
            log("No se pudo enviar la entrada: " + ex.getMessage());
        }
    }

    private void detenerEjecucion() {
        if (ejecutorActivo == null) {
            return;
        }
        ejecutorActivo.detener();
        ejecutorActivo = null;
        entradaCampo.setEnabled(false);
        enviarEntradaBtn.setEnabled(false);
        detenerBtn.setEnabled(false);
        execBtn.setEnabled(true);
        log("Ejecucion detenida por el usuario.");
    }

    private void crearMenuReportes() {
        JMenuBar barra = new JMenuBar();
        JMenu menuVer = new JMenu("Ver");
        verTablaSimbolos = new JMenuItem("Tabla de simbolos");
        verTablaSimbolos.addActionListener(e -> mostrarTablaSimbolos());
        verTablaSimbolos.setEnabled(false);
        verCuartetas = new JMenuItem("Cuartetas");
        verCuartetas.addActionListener(e -> mostrarCuartetas());
        verCuartetas.setEnabled(false);
        menuVer.add(verTablaSimbolos);
        menuVer.add(verCuartetas);
        barra.add(menuVer);
        setJMenuBar(barra);
    }

    private void mostrarTablaSimbolos() {
        DefaultTableModel modelo = modeloNoEditable(new String[]{"Nombre", "Categoria", "Tipo", "Parametros", "Propietario", "Visibilidad"});
        for (Simbolo simbolo : ultimoAnalisis.getSimbolosDeclarados()) {
            modelo.addRow(new Object[]{
                simbolo.nombre,
                categoriaSimbolo(simbolo),
                textoTipo(simbolo.tipo),
                textoParametros(simbolo),
                simbolo.propietario == null ? "" : simbolo.propietario.getNombre(),
                simbolo.modificador == null ? "" : simbolo.modificador
            });
        }
        mostrarTabla("Tabla de simbolos", modelo);
    }

    private void mostrarCuartetas() {
        DefaultTableModel modelo = modeloNoEditable(new String[]{"#", "Operador", "Operando 1", "Operando 2", "Destino"});
        List<GeneradorCuartetas.Cuarteto> codigo = ultimasCuartetas.getCodigo();
        for (int i = 0; i < codigo.size(); i++) {
            GeneradorCuartetas.Cuarteto cuarteto = codigo.get(i);
            modelo.addRow(new Object[]{i, cuarteto.operador(), valorCuarteto(cuarteto.operando1()),
                valorCuarteto(cuarteto.operando2()), valorCuarteto(cuarteto.destino())});
        }
        mostrarTabla("Cuartetas", modelo);
    }

    private DefaultTableModel modeloNoEditable(String[] columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    private void mostrarTabla(String titulo, DefaultTableModel modelo) {
        JTable tabla = new JTable(modelo);
        tabla.setAutoCreateRowSorter(true);
        JScrollPane panel = new JScrollPane(tabla);
        panel.setPreferredSize(new java.awt.Dimension(850, 420));
        JOptionPane.showMessageDialog(this, panel, titulo, JOptionPane.PLAIN_MESSAGE);
    }

    private String categoriaSimbolo(Simbolo simbolo) {
        if (simbolo.esEstructura) {
            return "Estructura";
        }
        return simbolo.esFuncion ? "Funcion" : "Variable";
    }

    private String textoTipo(com.mynor.contactoextraterrestred.ast.NodoTipo tipo) {
        if (tipo == null) {
            return "";
        }
        return tipo.getNombre() + "[]".repeat(tipo.getNivelesArreglo());
    }

    private String textoParametros(Simbolo simbolo) {
        if (simbolo.parametros == null) {
            return "";
        }
        return simbolo.parametros.stream().map(this::textoTipo).reduce((a, b) -> a + ", " + b).orElse("");
    }

    private String valorCuarteto(String valor) {
        return valor == null ? "_" : valor;
    }

    private Path rutaRars() throws IOException {
        Path directorioActual = Path.of(System.getProperty("user.dir"));
        Path rars = directorioActual.resolve("lib").resolve("rars1_6.jar");
        if (Files.isRegularFile(rars)) {
            return rars;
        }
        rars = directorioActual.resolve("ContactoExtraterrestreD").resolve("lib").resolve("rars1_6.jar");
        if (Files.isRegularFile(rars)) {
            return rars;
        }
        return EjecutorRars.rutaRarsEmpaquetado();
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

    private void nuevaVentana(Lenguaje lenguaje, String codigo, Path archivo, Path carpetaBase) {
        EditorPanel panel = new EditorPanel(lenguaje, codigo);
        panel.setRutaArchivo(archivo.toAbsolutePath().toString());

        ventanas.addTab(carpetaBase.relativize(archivo).toString(), panel);
        ventanas.setSelectedComponent(panel);
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton abrirCarpetaBtn;
    private javax.swing.JButton detenerBtn;
    private javax.swing.JTextField entradaCampo;
    private javax.swing.JButton enviarEntradaBtn;
    private javax.swing.JButton execBtn;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea outputTextArea;
    private javax.swing.JTabbedPane ventanas;
    // End of variables declaration//GEN-END:variables

}
