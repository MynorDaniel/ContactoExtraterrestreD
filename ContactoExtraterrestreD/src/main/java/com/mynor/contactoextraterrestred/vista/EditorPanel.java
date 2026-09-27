/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.mynor.contactoextraterrestred.vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

/**
 *
 * @author mynordma
 */
public class EditorPanel extends javax.swing.JPanel {

    private final RSyntaxTextArea editor;
    private final RTextScrollPane scrollPane;

    private String rutaAbsolutaActual = "";
    private boolean cambiosSinGuardar = false;

    private String rutaArchivo;
    private boolean modificado;

    private final JComboBox comboLenguaje;

    public EditorPanel() {
        initComponents();

        setLayout(new BorderLayout());

        editor = new RSyntaxTextArea(800, 600);

        editor.setCodeFoldingEnabled(true);
        editor.setAntiAliasingEnabled(true);
        editor.setTabSize(4);
        editor.setBackground(new Color(30, 30, 30));
        editor.setForeground(new Color(220, 220, 220));
        editor.setCaretColor(Color.WHITE);
        editor.setHighlightCurrentLine(false);

        editor.setFont(editor.getFont().deriveFont(16f));

        scrollPane = new RTextScrollPane(editor);
        scrollPane.setLineNumbersEnabled(true);

        add(jMenuBar1, BorderLayout.NORTH);

        comboLenguaje = new JComboBox<>(new String[]{
            "Y",
            "Z",
            "PIG",
            "C"
        });

        comboLenguaje.setBackground(new Color(45, 45, 45));
        comboLenguaje.setForeground(new Color(220, 220, 220));

        add(scrollPane, BorderLayout.CENTER);
        add(comboLenguaje, BorderLayout.SOUTH);

        editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                textoModificado();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                textoModificado();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                textoModificado();
            }
        });

    }

    public EditorPanel(Lenguaje lenguaje, String codigo) {
        initComponents();

        setLayout(new BorderLayout());

        editor = new RSyntaxTextArea(800, 600);

        editor.setCodeFoldingEnabled(true);
        editor.setAntiAliasingEnabled(true);
        editor.setTabSize(4);
        editor.setBackground(new Color(30, 30, 30));
        editor.setForeground(new Color(220, 220, 220));
        editor.setCaretColor(Color.WHITE);
        editor.setHighlightCurrentLine(false);

        editor.setFont(editor.getFont().deriveFont(16f));

        scrollPane = new RTextScrollPane(editor);
        scrollPane.setLineNumbersEnabled(true);

        add(jMenuBar1, BorderLayout.NORTH);

        comboLenguaje = new JComboBox<>(new String[]{
            "Y",
            "Z",
            "PIG",
            "C"
        });

        comboLenguaje.setBackground(new Color(45, 45, 45));
        comboLenguaje.setForeground(new Color(220, 220, 220));

        add(scrollPane, BorderLayout.CENTER);
        add(comboLenguaje, BorderLayout.SOUTH);

        editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                textoModificado();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                textoModificado();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                textoModificado();
            }
        });

        comboLenguaje.setSelectedItem(lenguaje.name());
        editor.setText(codigo);
    }

    public RSyntaxTextArea getEditor() {
        return editor;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public boolean isModificado() {
        return modificado;
    }

    public void setModificado(boolean modificado) {
        this.modificado = modificado;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuBar1 = new javax.swing.JMenuBar();
        abrir = new javax.swing.JMenu();
        guardar = new javax.swing.JMenu();
        guardarComo = new javax.swing.JMenu();
        nuevo = new javax.swing.JMenu();

        jMenuBar1.setBackground(new java.awt.Color(51, 51, 51));
        jMenuBar1.setForeground(new java.awt.Color(255, 255, 255));

        abrir.setForeground(new java.awt.Color(255, 255, 255));
        abrir.setText("Abrir");
        abrir.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                abrirMouseClicked(evt);
            }
        });
        jMenuBar1.add(abrir);

        guardar.setForeground(new java.awt.Color(255, 255, 255));
        guardar.setText("Guardar");
        guardar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                guardarMouseClicked(evt);
            }
        });
        jMenuBar1.add(guardar);

        guardarComo.setForeground(new java.awt.Color(255, 255, 255));
        guardarComo.setText("Guardar como");
        guardarComo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                guardarComoMouseClicked(evt);
            }
        });
        jMenuBar1.add(guardarComo);

        nuevo.setForeground(new java.awt.Color(255, 255, 255));
        nuevo.setText("Nuevo");
        nuevo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                nuevoMouseClicked(evt);
            }
        });
        jMenuBar1.add(nuevo);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 837, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 655, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void abrirMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_abrirMouseClicked
        if (cambiosSinGuardar) {
            int opcion = JOptionPane.showConfirmDialog(
                    this,
                    "¿Guardar cambios?",
                    "Cambios no guardados",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (opcion == JOptionPane.CANCEL_OPTION || opcion == JOptionPane.CLOSED_OPTION) {
                return;
            }

            if (opcion == JOptionPane.YES_OPTION) {
                guardarMouseClicked(evt);
            }
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecciona un archivo");

        FileNameExtensionFilter filtro = new FileNameExtensionFilter(
                "Archivos de código (*.y, *.z, *.pig, *.c)",
                "y",
                "z",
                "c",
                "pig"
        );
        fileChooser.setFileFilter(filtro);
        fileChooser.setAcceptAllFileFilterUsed(false);

        int resultado = fileChooser.showOpenDialog(this);

        if (resultado == JFileChooser.APPROVE_OPTION) {
            File archivoSeleccionado = fileChooser.getSelectedFile();
            rutaAbsolutaActual = archivoSeleccionado.getAbsolutePath();
            archivoSeleccionado.getPath();

            try (BufferedReader br = new BufferedReader(new FileReader(archivoSeleccionado))) {
                StringBuilder contenido = new StringBuilder();
                String linea;
                while ((linea = br.readLine()) != null) {
                    contenido.append(linea).append("\n");
                }
                editor.setText(contenido.toString());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error al leer el archivo: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_abrirMouseClicked

    private void guardarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_guardarMouseClicked
        if (!rutaAbsolutaActual.isEmpty()) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(rutaAbsolutaActual))) {
                writer.write(editor.getText());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error al guardar el archivo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            cambiosSinGuardar = false;
        } else {
            guardarComoMouseClicked(evt);
        }
    }//GEN-LAST:event_guardarMouseClicked

    private void guardarComoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_guardarComoMouseClicked
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar archivo como...");

        FileNameExtensionFilter filtro = new FileNameExtensionFilter(
                "Archivos de código (*.y, *.z, *.pig, *.c)",
                "y",
                "z",
                "c",
                "pig"
        );
        fileChooser.setFileFilter(filtro);
        fileChooser.setAcceptAllFileFilterUsed(false);

        int resultado = fileChooser.showSaveDialog(this);

        if (resultado == JFileChooser.APPROVE_OPTION) {
            File archivoSeleccionado = fileChooser.getSelectedFile();
            String rutaNuevoArchivo = archivoSeleccionado.getAbsolutePath();

            archivoSeleccionado = new File(rutaNuevoArchivo);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivoSeleccionado))) {
                writer.write(editor.getText());
                if (rutaAbsolutaActual.isEmpty()) {
                    rutaAbsolutaActual = archivoSeleccionado.getAbsolutePath();
                    archivoSeleccionado.getPath();
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error al guardar el archivo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_guardarComoMouseClicked

    private void nuevoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_nuevoMouseClicked
        if (cambiosSinGuardar) {
            int opcion = JOptionPane.showConfirmDialog(
                    this,
                    "¿Guardar cambios?",
                    "Cambios no guardados",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (opcion == JOptionPane.CANCEL_OPTION || opcion == JOptionPane.CLOSED_OPTION) {
                return;
            }

            if (opcion == JOptionPane.YES_OPTION) {
                guardarMouseClicked(evt);
            }
        }

        editor.setText("");
        rutaAbsolutaActual = "";
        cambiosSinGuardar = false;
    }//GEN-LAST:event_nuevoMouseClicked

    private void textoModificado() {
        cambiosSinGuardar = true;
    }

    public String getCodigo() {
        return editor.getText();
    }

    public Lenguaje getLenguaje() {
        String lenguajeText = comboLenguaje.getSelectedItem().toString();
        return Lenguaje.valueOf(lenguajeText);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu abrir;
    private javax.swing.JMenu guardar;
    private javax.swing.JMenu guardarComo;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu nuevo;
    // End of variables declaration//GEN-END:variables
}
