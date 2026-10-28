package com.mynor.contactoextraterrestred.ensamblador;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class EjecutorRars {

    private static Path rarsEmpaquetado;
    private final Path jarRars;
    private volatile Process proceso;
    private BufferedWriter entrada;

    public EjecutorRars(Path jarRars) {
        this.jarRars = jarRars;
    }

    public static synchronized Path rutaRarsEmpaquetado() throws IOException {
        if (rarsEmpaquetado != null && Files.isRegularFile(rarsEmpaquetado)) {
            return rarsEmpaquetado;
        }
        try (InputStream recurso = EjecutorRars.class.getResourceAsStream("/rars/rars1_6.jar")) {
            if (recurso == null) {
                return null;
            }
            Path archivo = Files.createTempFile("rars1_6_", ".jar");
            Files.copy(recurso, archivo, StandardCopyOption.REPLACE_EXISTING);
            archivo.toFile().deleteOnExit();
            rarsEmpaquetado = archivo;
            return archivo;
        }
    }

    public boolean estaEjecutando() {
        Process p = proceso;
        return p != null && p.isAlive();
    }

    public void ejecutar(String asm, Consumer<String> salida, Consumer<String> errores, IntConsumer alTerminar)
            throws IOException {
        detener();

        Path archivo = Files.createTempFile("programa_", ".asm");
        Files.writeString(archivo, asm, StandardCharsets.UTF_8);

        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        ProcessBuilder pb = new ProcessBuilder(
                java,
                "-Dfile.encoding=ISO-8859-1",
                "-Dstdout.encoding=ISO-8859-1",
                "-Dstderr.encoding=ISO-8859-1",
                "-jar", jarRars.toAbsolutePath().toString(),
                "nc",
                "sm",
                "me",
                archivo.toAbsolutePath().toString());

        Process p = pb.start();
        proceso = p;
        entrada = new BufferedWriter(new OutputStreamWriter(p.getOutputStream(), StandardCharsets.ISO_8859_1));

        Thread hiloSalida = leer(p.getInputStream(), salida);
        Thread hiloErrores = leer(p.getErrorStream(), errores);

        Thread esperar = new Thread(() -> {
            int codigo = -1;
            try {
                codigo = p.waitFor();
                hiloSalida.join(1000);
                hiloErrores.join(1000);
            } catch (InterruptedException ignorada) {
                Thread.currentThread().interrupt();
            } finally {
                try {
                    Files.deleteIfExists(archivo);
                } catch (IOException ignorada) {
                }
                alTerminar.accept(codigo);
            }
        }, "rars-espera");
        esperar.setDaemon(true);
        esperar.start();
    }

    public synchronized void enviar(String linea) throws IOException {
        if (!estaEjecutando() || entrada == null) {
            return;
        }
        entrada.write(linea);
        entrada.write('\n');
        entrada.flush();
    }

    public void detener() {
        Process p = proceso;
        if (p != null && p.isAlive()) {
            p.destroyForcibly();
        }
    }

    private Thread leer(InputStream flujo, Consumer<String> destino) {
        Thread hilo = new Thread(() -> {
            try (Reader lector = new InputStreamReader(flujo, StandardCharsets.UTF_8)) {
                char[] buffer = new char[512];
                int n;
                while ((n = lector.read(buffer)) != -1) {
                    destino.accept(new String(buffer, 0, n));
                }
            } catch (IOException ignorada) {
            }
        }, "rars-lectura");
        hilo.setDaemon(true);
        hilo.start();
        return hilo;
    }
}
