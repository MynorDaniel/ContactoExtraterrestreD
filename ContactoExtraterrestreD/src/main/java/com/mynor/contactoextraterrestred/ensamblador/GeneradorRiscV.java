/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mynor.contactoextraterrestred.ensamblador;

import com.mynor.contactoextraterrestred.ast.NodoTipo;
import com.mynor.contactoextraterrestred.ast.visitantes.codigointermedio.GeneradorCuartetas;
import com.mynor.contactoextraterrestred.ast.visitantes.codigointermedio.GeneradorCuartetas.Cuarteto;
import com.mynor.contactoextraterrestred.ast.visitantes.codigointermedio.GeneradorCuartetas.InfoNombre;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 *
 * @author mynordma
 */
public class GeneradorRiscV {

    private static final String TEXTO_VERDADERO = "true";
    private static final String TEXTO_FALSO = "false";

    private enum Cat {
        INT, FLOAT, STR, CHAR, BOOL, PTR
    }

    private enum Clase {
        FRAME, PALABRA, BLOQUE
    }

    private record Sym(Clase clase, int desplazamiento, String etiqueta) {

    }

    private record Lit(Cat cat, long entero, float flotante, String cadena) {

    }

    private static final class Region {

        String funcion;   
        int ini;
        int fin;
        Map<String, Sym> locales = new LinkedHashMap<>();
        int celdasLocales = 0;
    }

    private static final Pattern ENTERO = Pattern.compile("-?\\d+");
    private static final Pattern DECIMAL = Pattern.compile("-?\\d+\\.\\d*");

    private final List<Cuarteto> codigo;
    private final Map<String, InfoNombre> tabla;
    private final Set<String> funciones;

    private final StringBuilder texto = new StringBuilder();
    private final Map<String, Sym> globales = new LinkedHashMap<>();
    private final Map<String, Integer> bloques = new LinkedHashMap<>();
    private final Map<String, String> cadenas = new LinkedHashMap<>(); 

    private Map<String, Sym> locales = null;
    private final List<String> parametrosPendientes = new ArrayList<>();
    private int contadorSalto = 0;

    public GeneradorRiscV(List<Cuarteto> codigo, Map<String, InfoNombre> tabla, List<String> nombresDeFunciones) {
        this.codigo = codigo;
        this.tabla = tabla;
        this.funciones = new HashSet<>(nombresDeFunciones);
    }

    public static String generar(GeneradorCuartetas generador) {
        return new GeneradorRiscV(generador.getCodigo(), generador.getTablaNombres(),
                generador.getNombresDeFunciones()).generar();
    }

    public String generar() {
        List<Region> regiones = dividirRegiones();

        for (Region region : regiones) {
            if (region.funcion == null) {
                analizarGlobal(region);
            }
        }
        for (Region region : regiones) {
            if (region.funcion != null) {
                analizarFuncion(region);
            }
        }

        texto.append(".text\n.globl main\nmain:\n");
        boolean principalEmitido = false;
        for (Region region : regiones) {
            if (region.funcion == null && !principalEmitido) {
                traducirRegion(region);
                emitirSalida();
                principalEmitido = true;
            } else if (region.funcion == null) {
                traducirRegion(region);
                emitirSalida();
            } else {
                traducirFuncion(region);
            }
        }
        if (regiones.isEmpty()) {
            emitirSalida();
        }

        StringBuilder salida = new StringBuilder();
        salida.append("# Generado automaticamente RISC-V\n");
        emitirDatos(salida);
        salida.append(texto);
        salida.append(RUNTIME);
        return salida.toString();
    }

    private List<Region> dividirRegiones() {
        List<Region> regiones = new ArrayList<>();
        int primeraFuncion = -1;
        for (int i = 0; i < codigo.size(); i++) {
            Cuarteto q = codigo.get(i);
            if ("etq".equals(q.operador()) && funciones.contains(q.destino())) {
                primeraFuncion = i;
                break;
            }
        }
        if (primeraFuncion < 0) {
            Region principal = new Region();
            principal.ini = 0;
            principal.fin = codigo.size();
            regiones.add(principal);
            return regiones;
        }

        String etiquetaFin = null;
        if (primeraFuncion > 0 && "goto".equals(codigo.get(primeraFuncion - 1).operador())) {
            etiquetaFin = codigo.get(primeraFuncion - 1).destino();
        }

        Region principal = new Region();
        principal.ini = 0;
        principal.fin = primeraFuncion;
        regiones.add(principal);

        Region actual = null;
        for (int i = primeraFuncion; i < codigo.size(); i++) {
            Cuarteto q = codigo.get(i);
            boolean esEtiqueta = "etq".equals(q.operador());
            if (esEtiqueta && funciones.contains(q.destino())) {
                if (actual != null) {
                    actual.fin = i;
                }
                actual = new Region();
                actual.funcion = q.destino();
                actual.ini = i;
                regiones.add(actual);
            } else if (esEtiqueta && q.destino().equals(etiquetaFin)) {
                if (actual != null) {
                    actual.fin = i;
                }
                actual = new Region();
                actual.ini = i;
                regiones.add(actual);
            }
        }
        if (actual != null) {
            actual.fin = codigo.size();
        }
        return regiones;
    }

    private void emitirSalida() {
        ins("li a7, 10");
        ins("ecall");
    }

    private boolean esNombre(String s) {
        return s != null && parseLiteral(s) == null;
    }

    private void clasificar(Cuarteto q, List<String> usos, List<String> defs) {
        switch (q.operador()) {
            case "etq", "goto", "reservar" -> {
            }
            case "recibir", "SCAN_INT", "SCAN_FLOAT", "SCAN_CHAR", "SCAN_TEXT" ->
                defs.add(q.destino());
            case "=", "alloc" -> {
                usos.add(q.operando1());
                defs.add(q.destino());
            }
            case "load", "=[]" -> {
                usos.add(q.operando1());
                usos.add(q.operando2());
                defs.add(q.destino());
            }
            case "[]=", "store" -> {
                usos.add(q.operando1());
                usos.add(q.operando2());
                usos.add(q.destino());
            }
            case "param", "print", "return", "if" ->
                usos.add(q.operando1());
            case "call" -> {
                if (q.destino() != null) {
                    defs.add(q.destino());
                }
            }
            case "if==", "if!=", "if<", "if>", "if<=", "if>=" -> {
                usos.add(q.operando1());
                usos.add(q.operando2());
            }
            default -> {
                usos.add(q.operando1());
                usos.add(q.operando2());
                defs.add(q.destino());
            }
        }
    }

    private String etiquetaGlobal(String nombre) {
        return "g_" + limpiar(nombre);
    }

    private void analizarGlobal(Region region) {
        for (int i = region.ini; i < region.fin; i++) {
            Cuarteto q = codigo.get(i);
            if ("reservar".equals(q.operador())) {
                String etiqueta = etiquetaGlobal(q.destino());
                globales.put(q.destino(), new Sym(Clase.BLOQUE, 0, etiqueta));
                bloques.merge(etiqueta, celdasDe(q.operando1()), Math::max);
            }
        }
        for (int i = region.ini; i < region.fin; i++) {
            List<String> usos = new ArrayList<>();
            List<String> defs = new ArrayList<>();
            clasificar(codigo.get(i), usos, defs);
            for (String nombre : concat(usos, defs)) {
                if (esNombre(nombre) && !globales.containsKey(nombre)) {
                    globales.put(nombre, new Sym(Clase.PALABRA, 0, etiquetaGlobal(nombre)));
                }
            }
        }
    }

    private void analizarFuncion(Region region) {
        int indiceParametro = 0;
        for (int i = region.ini; i < region.fin; i++) {
            Cuarteto q = codigo.get(i);
            if ("recibir".equals(q.operador()) && !region.locales.containsKey(q.destino())) {
                region.locales.put(q.destino(), new Sym(Clase.FRAME, 4 * indiceParametro++, null));
            } else if ("reservar".equals(q.operador())) {
                String etiqueta = "s_" + limpiar(region.funcion) + "_" + limpiar(q.destino());
                region.locales.put(q.destino(), new Sym(Clase.BLOQUE, 0, etiqueta));
                bloques.merge(etiqueta, celdasDe(q.operando1()), Math::max);
            }
        }
        for (int i = region.ini; i < region.fin; i++) {
            List<String> usos = new ArrayList<>();
            List<String> defs = new ArrayList<>();
            clasificar(codigo.get(i), usos, defs);
            for (String nombre : defs) {
                agregarLocal(region, nombre);
            }
            for (String nombre : usos) {
                if (esNombre(nombre) && !globales.containsKey(nombre)) {
                    agregarLocal(region, nombre); 
                }
            }
        }
    }

    private void agregarLocal(Region region, String nombre) {
        if (!esNombre(nombre) || region.locales.containsKey(nombre)) {
            return;
        }
        region.locales.put(nombre, new Sym(Clase.FRAME, -12 - 4 * region.celdasLocales, null));
        region.celdasLocales++;
    }

    private List<String> concat(List<String> a, List<String> b) {
        List<String> r = new ArrayList<>(a);
        r.addAll(b);
        return r;
    }

    private int celdasDe(String literal) {
        Lit lit = literal == null ? null : parseLiteral(literal);
        return lit == null ? 1 : (int) Math.max(lit.entero(), 1);
    }

    private Sym resolver(String nombre) {
        Sym sym = locales != null ? locales.get(nombre) : null;
        if (sym == null) {
            sym = globales.get(nombre);
        }
        if (sym == null) {
            sym = new Sym(Clase.PALABRA, 0, etiquetaGlobal(nombre));
            globales.put(nombre, sym);
        }
        return sym;
    }

    private Cat catDe(NodoTipo tipo) {
        if (tipo == null) {
            return Cat.INT;
        }
        if (tipo.getNivelesArreglo() > 0) {
            return Cat.PTR;
        }
        String n = tipo.getNombre();
        if ("entero".equals(n) || "int".equals(n) || "numerus".equals(n) || "void".equals(n)) {
            return Cat.INT;
        }
        if ("flotante".equals(n) || "double".equals(n) || "decimalis".equals(n)) {
            return Cat.FLOAT;
        }
        if ("caracter".equals(n) || "char".equals(n) || "littera".equals(n)) {
            return Cat.CHAR;
        }
        if ("cadena".equals(n) || "String".equals(n) || "textum".equals(n)) {
            return Cat.STR;
        }
        if ("bool".equals(n) || "boolean".equals(n)) {
            return Cat.BOOL;
        }
        return Cat.PTR;
    }

    private Cat cat(String operando) {
        if (operando == null) {
            return Cat.INT;
        }
        Lit lit = parseLiteral(operando);
        if (lit != null) {
            return lit.cat();
        }
        InfoNombre info = tabla.get(operando);
        return info == null ? Cat.INT : catDe(info.tipo());
    }

    private Lit parseLiteral(String op) {
        if (op == null || op.isEmpty()) {
            return null;
        }
        char c = op.charAt(0);
        if (c == '"' && op.length() >= 2 && op.endsWith("\"")) {
            return new Lit(Cat.STR, 0, 0, desescapar(op.substring(1, op.length() - 1)));
        }
        if (c == '\'' && op.length() >= 3 && op.endsWith("'")) {
            return new Lit(Cat.CHAR, op.codePointAt(1), 0, null);
        }
        if (ENTERO.matcher(op).matches()) {
            return new Lit(Cat.INT, Long.parseLong(op), 0, null);
        }
        if (DECIMAL.matcher(op).matches()) {
            return new Lit(Cat.FLOAT, 0, Float.parseFloat(op), null);
        }
        return null;
    }

    private String desescapar(String crudo) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < crudo.length(); i++) {
            char c = crudo.charAt(i);
            if (c == '\\' && i + 1 < crudo.length()) {
                char n = crudo.charAt(++i);
                switch (n) {
                    case 'n' ->
                        sb.append('\n');
                    case 't' ->
                        sb.append('\t');
                    case 'r' ->
                        sb.append('\r');
                    case '0' ->
                        sb.append('\0');
                    default ->
                        sb.append(n);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private String limpiar(String nombre) {
        StringBuilder sb = new StringBuilder();
        for (char c : nombre.toCharArray()) {
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_') {
                sb.append(c);
            } else {
                sb.append(String.format("_%02x_", (int) c));
            }
        }
        return sb.toString();
    }

    private String etiquetaCadena(String contenido) {
        return cadenas.computeIfAbsent(contenido, k -> "str_" + cadenas.size());
    }

    private void ins(String linea) {
        texto.append("    ").append(linea).append('\n');
    }

    private void etiquetaTexto(String etiqueta) {
        texto.append(etiqueta).append(":\n");
    }

    private boolean cabeEn12Bits(long valor) {
        return valor >= -2048 && valor <= 2047;
    }

    private void sumarInmediato(String rd, String rs, int inmediato) {
        if (cabeEn12Bits(inmediato)) {
            ins("addi " + rd + ", " + rs + ", " + inmediato);
        } else {
            ins("li t6, " + inmediato);
            ins("add " + rd + ", " + rs + ", t6");
        }
    }

    private void cargarValor(String reg, String operando) {
        if (operando == null) {
            ins("li " + reg + ", 0");
            return;
        }
        Lit lit = parseLiteral(operando);
        if (lit != null) {
            switch (lit.cat()) {
                case INT, CHAR ->
                    ins("li " + reg + ", " + (int) lit.entero());
                case FLOAT ->
                    ins("li " + reg + ", " + Float.floatToRawIntBits(lit.flotante()));
                default ->
                    ins("la " + reg + ", " + etiquetaCadena(lit.cadena()));
            }
            return;
        }
        Sym sym = resolver(operando);
        switch (sym.clase()) {
            case BLOQUE ->
                ins("la " + reg + ", " + sym.etiqueta());
            case PALABRA -> {
                ins("la t6, " + sym.etiqueta());
                ins("lw " + reg + ", 0(t6)");
            }
            default ->
                accesoFrame("lw", reg, sym.desplazamiento());
        }
    }

    private void guardarEn(String reg, String destino) {
        if (destino == null) {
            return;
        }
        Sym sym = resolver(destino);
        if (sym.clase() == Clase.FRAME) {
            accesoFrame("sw", reg, sym.desplazamiento());
        } else {
            ins("la t6, " + sym.etiqueta());
            ins("sw " + reg + ", 0(t6)");
        }
    }

    private void accesoFrame(String instruccion, String reg, int desplazamiento) {
        if (cabeEn12Bits(desplazamiento)) {
            ins(instruccion + " " + reg + ", " + desplazamiento + "(fp)");
        } else {
            ins("li t6, " + desplazamiento);
            ins("add t6, fp, t6");
            ins(instruccion + " " + reg + ", 0(t6)");
        }
    }

    private void aFlotante(String freg, String ireg, Cat categoria) {
        if (categoria == Cat.FLOAT) {
            ins("fmv.w.x " + freg + ", " + ireg);
        } else {
            ins("fcvt.s.w " + freg + ", " + ireg);
        }
    }

    private void saltarSi(String reg, String destino) {
        String omitir = "__sk" + (contadorSalto++);
        ins("beqz " + reg + ", " + omitir);
        ins("j " + destino);
        etiquetaTexto(omitir);
    }

    private String etiquetaDeCodigo(String nombre) {
        return funciones.contains(nombre) ? "f_" + limpiar(nombre) : nombre;
    }

    private void traducirRegion(Region region) {
        locales = null;
        for (int i = region.ini; i < region.fin; i++) {
            traducir(codigo.get(i));
        }
    }

    private void traducirFuncion(Region region) {
        locales = region.locales;
        int marco = 8 + 4 * region.celdasLocales;
        etiquetaTexto("f_" + limpiar(region.funcion));
        ins("mv t5, fp");
        sumarInmediato("sp", "sp", -marco);
        sumarInmediato("fp", "sp", marco);
        ins("sw ra, -4(fp)");
        ins("sw t5, -8(fp)");
        for (int i = region.ini + 1; i < region.fin; i++) {
            traducir(codigo.get(i));
        }
        epilogo();
        locales = null;
    }

    private void epilogo() {
        ins("lw ra, -4(fp)");
        ins("lw t5, -8(fp)");
        ins("mv sp, fp");
        ins("mv fp, t5");
        ins("jr ra");
    }

    private void traducir(Cuarteto q) {
        texto.append("    # ").append(q).append('\n');
        switch (q.operador()) {
            case "etq" ->
                etiquetaTexto(etiquetaDeCodigo(q.destino()));
            case "goto" ->
                ins("j " + etiquetaDeCodigo(q.destino()));
            case "reservar", "recibir" -> {
            }
            case "=" ->
                asignar(q);
            case "alloc" ->
                reservarHeap(q);
            case "load", "=[]" ->
                leerElemento(q.operando1(), q.operando2(), q.destino());
            case "store", "[]=" ->
                escribirElemento(q.operando1(), q.operando2(), q.destino());
            case "param" ->
                parametrosPendientes.add(q.operando1());
            case "call" ->
                llamar(q);
            case "return" ->
                retornar(q);
            case "print" ->
                imprimir(q);
            case "SCAN_INT" -> {
                ins("li a7, 5");
                ins("ecall");
                guardarEn("a0", q.destino());
            }
            case "SCAN_FLOAT" -> {
                ins("li a7, 6");
                ins("ecall");
                ins("fmv.x.w a0, fa0");
                guardarEn("a0", q.destino());
            }
            case "SCAN_CHAR" -> {
                ins("li a7, 12");
                ins("ecall");
                guardarEn("a0", q.destino());
            }
            case "SCAN_TEXT" -> {
                ins("jal ra, __rt_readline");
                guardarEn("a0", q.destino());
            }
            case "concat" ->
                concatenar(q);
            case "if", "if==", "if!=", "if<", "if>", "if<=", "if>=" ->
                condicional(q);
            case "+", "-", "*", "/", "%" ->
                aritmetica(q);
            default ->
                texto.append("    # operador no soportado: ").append(q.operador()).append('\n');
        }
    }

    private void asignar(Cuarteto q) {
        cargarValor("t0", q.operando1());
        Cat origen = cat(q.operando1());
        Cat destino = cat(q.destino());
        if (destino == Cat.FLOAT && origen != Cat.FLOAT && origen != Cat.PTR) {
            ins("fcvt.s.w ft0, t0");
            ins("fmv.x.w t0, ft0");
        } else if (destino == Cat.INT && origen == Cat.FLOAT) {
            ins("fmv.w.x ft0, t0");
            ins("fcvt.w.s t0, ft0, rtz");
        }
        guardarEn("t0", q.destino());
    }

    private void aritmetica(Cuarteto q) {
        String op = q.operador();
        boolean unario = q.operando2() == null;
        cargarValor("t0", q.operando1());
        if (!unario) {
            cargarValor("t1", q.operando2());
        }
        Cat c1 = cat(q.operando1());
        Cat c2 = unario ? Cat.INT : cat(q.operando2());
        boolean flotante = c1 == Cat.FLOAT || c2 == Cat.FLOAT;

        if (!flotante) {
            if (unario) {
                ins("-".equals(op) ? "sub t2, zero, t0" : "mv t2, t0");
            } else {
                switch (op) {
                    case "+" ->
                        ins("add t2, t0, t1");
                    case "-" ->
                        ins("sub t2, t0, t1");
                    case "*" ->
                        ins("mul t2, t0, t1");
                    case "/" ->
                        ins("div t2, t0, t1");
                    default ->
                        ins("rem t2, t0, t1");
                }
            }
        } else {
            aFlotante("ft0", "t0", c1);
            if (unario) {
                ins("-".equals(op) ? "fneg.s ft2, ft0" : "fmv.s ft2, ft0");
            } else {
                aFlotante("ft1", "t1", c2);
                switch (op) {
                    case "+" ->
                        ins("fadd.s ft2, ft0, ft1");
                    case "-" ->
                        ins("fsub.s ft2, ft0, ft1");
                    case "*" ->
                        ins("fmul.s ft2, ft0, ft1");
                    default ->
                        ins("fdiv.s ft2, ft0, ft1");
                }
            }
            ins("fmv.x.w t2, ft2");
        }
        guardarEn("t2", q.destino());
    }

    private void condicional(Cuarteto q) {
        String op = q.operador();
        if ("if".equals(op)) {
            cargarValor("t2", q.operando1());
            saltarSi("t2", etiquetaDeCodigo(q.destino()));
            return;
        }
        String rel = op.substring(2);
        String a = q.operando1();
        String b = q.operando2();
        Cat c1 = cat(a);
        Cat c2 = cat(b);
        boolean igualdad = "==".equals(rel) || "!=".equals(rel);

        if (igualdad && (c1 == Cat.STR || c2 == Cat.STR)) {
            cargarValor("a0", a);
            cargarValor("a1", b);
            ins("jal ra, __rt_streq");
            ins("mv t2, a0");
            if ("!=".equals(rel)) {
                ins("xori t2, t2, 1");
            }
        } else if (c1 == Cat.FLOAT || c2 == Cat.FLOAT) {
            cargarValor("t0", a);
            cargarValor("t1", b);
            aFlotante("ft0", "t0", c1);
            aFlotante("ft1", "t1", c2);
            switch (rel) {
                case "==" ->
                    ins("feq.s t2, ft0, ft1");
                case "!=" -> {
                    ins("feq.s t2, ft0, ft1");
                    ins("xori t2, t2, 1");
                }
                case "<" ->
                    ins("flt.s t2, ft0, ft1");
                case ">" ->
                    ins("flt.s t2, ft1, ft0");
                case "<=" ->
                    ins("fle.s t2, ft0, ft1");
                default ->
                    ins("fle.s t2, ft1, ft0");
            }
        } else {
            cargarValor("t0", a);
            cargarValor("t1", b);
            switch (rel) {
                case "==" -> {
                    ins("sub t2, t0, t1");
                    ins("seqz t2, t2");
                }
                case "!=" -> {
                    ins("sub t2, t0, t1");
                    ins("snez t2, t2");
                }
                case "<" ->
                    ins("slt t2, t0, t1");
                case ">" ->
                    ins("slt t2, t1, t0");
                case "<=" -> {
                    ins("slt t2, t1, t0");
                    ins("xori t2, t2, 1");
                }
                default -> {
                    ins("slt t2, t0, t1");
                    ins("xori t2, t2, 1");
                }
            }
        }
        saltarSi("t2", etiquetaDeCodigo(q.destino()));
    }

    private void direccionElemento(String base, String posicion) {
        cargarValor("t3", base);
        Lit lit = parseLiteral(posicion);
        if (lit != null && lit.cat() == Cat.INT) {
            long desplazamiento = lit.entero() * 4;
            if (desplazamiento != 0) {
                sumarInmediato("t3", "t3", (int) desplazamiento);
            }
            return;
        }
        cargarValor("t1", posicion);
        ins("slli t1, t1, 2");
        ins("add t3, t3, t1");
    }

    private void leerElemento(String base, String posicion, String destino) {
        direccionElemento(base, posicion);
        ins("lw t2, 0(t3)");
        guardarEn("t2", destino);
    }

    private void escribirElemento(String posicion, String valor, String base) {
        direccionElemento(base, posicion);
        cargarValor("t2", valor);
        ins("sw t2, 0(t3)");
    }

    private void reservarHeap(Cuarteto q) {
        Lit lit = parseLiteral(q.operando1());
        if (lit != null && lit.cat() == Cat.INT) {
            ins("li a0, " + (int) (Math.max(lit.entero(), 1) * 4));
        } else {
            cargarValor("t0", q.operando1());
            ins("slli a0, t0, 2");
        }
        ins("li a7, 9");
        ins("ecall");
        guardarEn("a0", q.destino());
    }

    private void llamar(Cuarteto q) {
        int n = parametrosPendientes.size();
        if (n > 0) {
            sumarInmediato("sp", "sp", -4 * n);
            for (int i = 0; i < n; i++) {
                cargarValor("t0", parametrosPendientes.get(i));
                ins("sw t0, " + (4 * i) + "(sp)");
            }
        }
        ins("jal ra, f_" + limpiar(q.operando1()));
        if (n > 0) {
            sumarInmediato("sp", "sp", 4 * n);
        }
        parametrosPendientes.clear();
        guardarEn("a0", q.destino());
    }

    private void retornar(Cuarteto q) {
        if (q.operando1() != null) {
            cargarValor("a0", q.operando1());
        }
        if (locales == null) {
            emitirSalida();
        } else {
            epilogo();
        }
    }

    private void imprimir(Cuarteto q) {
        String op = q.operando1();
        cargarValor("a0", op);
        switch (cat(op)) {
            case STR ->
                ins("jal ra, __rt_print_str");
            case CHAR -> {
                ins("li a7, 11");
                ins("ecall");
            }
            case BOOL ->
                ins("jal ra, __rt_print_bool");
            case FLOAT -> {
                ins("fmv.w.x fa0, a0");
                ins("li a7, 2");
                ins("ecall");
            }
            default -> {
                ins("li a7, 1");
                ins("ecall");
            }
        }
    }

    private void convertirACadena(Cat categoria) {
        switch (categoria) {
            case STR -> {
            }
            case CHAR ->
                ins("jal ra, __rt_ctoa");
            case BOOL ->
                ins("jal ra, __rt_btoa");
            case FLOAT ->
                ins("jal ra, __rt_ftoa");
            default ->
                ins("jal ra, __rt_itoa");
        }
    }

    private void concatenar(Cuarteto q) {
        cargarValor("a0", q.operando1());
        convertirACadena(cat(q.operando1()));
        ins("addi sp, sp, -4");
        ins("sw a0, 0(sp)");
        cargarValor("a0", q.operando2());
        convertirACadena(cat(q.operando2()));
        ins("mv a1, a0");
        ins("lw a0, 0(sp)");
        ins("addi sp, sp, 4");
        ins("jal ra, __rt_concat");
        guardarEn("a0", q.destino());
    }

    private void emitirDatos(StringBuilder salida) {
        salida.append(".data\n");
        for (Sym sym : globales.values()) {
            if (sym.clase() == Clase.PALABRA) {
                salida.append(sym.etiqueta()).append(": .word 0\n");
            }
        }
        for (Map.Entry<String, Integer> bloque : bloques.entrySet()) {
            salida.append(".align 2\n");
            salida.append(bloque.getKey()).append(": .space ").append(4 * bloque.getValue()).append('\n');
        }
        for (Map.Entry<String, String> cadena : cadenas.entrySet()) {
            salida.append(".align 2\n");
            salida.append(cadena.getValue()).append(":\n");
            emitirBytesDeCadena(salida, cadena.getKey());
        }
        salida.append("__rt_str_null: .asciz \"null\"\n");
        salida.append("__rt_str_true: .asciz \"").append(TEXTO_VERDADERO).append("\"\n");
        salida.append("__rt_str_false: .asciz \"").append(TEXTO_FALSO).append("\"\n");
        salida.append('\n');
    }

    private void emitirBytesDeCadena(StringBuilder salida, String contenido) {
        StringBuilder texto = new StringBuilder();
        StringBuilder numeros = new StringBuilder();
        for (byte b : contenido.getBytes(StandardCharsets.UTF_8)) {
            int v = b & 0xFF;
            if (v >= 32 && v < 127 && v != '"' && v != '\\') {
                if (numeros.length() > 0) {
                    salida.append("    .byte ").append(numeros).append('\n');
                    numeros.setLength(0);
                }
                texto.append((char) v);
            } else {
                if (texto.length() > 0) {
                    salida.append("    .ascii \"").append(texto).append("\"\n");
                    texto.setLength(0);
                }
                if (numeros.length() > 0) {
                    numeros.append(", ");
                }
                numeros.append(v);
            }
        }
        if (texto.length() > 0) {
            salida.append("    .ascii \"").append(texto).append("\"\n");
        }
        if (numeros.length() > 0) {
            salida.append("    .byte ").append(numeros).append('\n');
        }
        salida.append("    .byte 0\n");
    }

    private static final String RUNTIME = """

        # a0 = bytes -> a0 = direccion en el heap (memoria en cero)
        __rt_alloc:
            addi a0, a0, 3
            andi a0, a0, -4
            li a7, 9
            ecall
            jr ra

        # a0 = cadena -> a0 = longitud
        __rt_strlen:
            mv t0, a0
            li a0, 0
        __rt_strlen_l:
            lbu t1, 0(t0)
            beqz t1, __rt_strlen_e
            addi t0, t0, 1
            addi a0, a0, 1
            j __rt_strlen_l
        __rt_strlen_e:
            jr ra

        # a0, a1 = cadenas -> a0 = nueva cadena concatenada
        __rt_concat:
            addi sp, sp, -4
            sw ra, 0(sp)
            bnez a0, __rt_c1
            la a0, __rt_str_null
        __rt_c1:
            bnez a1, __rt_c2
            la a1, __rt_str_null
        __rt_c2:
            mv s1, a0
            mv s2, a1
            mv a0, s1
            jal ra, __rt_strlen
            mv s3, a0
            mv a0, s2
            jal ra, __rt_strlen
            add a0, a0, s3
            addi a0, a0, 1
            jal ra, __rt_alloc
            mv s4, a0
            mv t0, a0
        __rt_c_l1:
            lbu t1, 0(s1)
            beqz t1, __rt_c_l2
            sb t1, 0(t0)
            addi t0, t0, 1
            addi s1, s1, 1
            j __rt_c_l1
        __rt_c_l2:
            lbu t1, 0(s2)
            sb t1, 0(t0)
            beqz t1, __rt_c_end
            addi t0, t0, 1
            addi s2, s2, 1
            j __rt_c_l2
        __rt_c_end:
            mv a0, s4
            lw ra, 0(sp)
            addi sp, sp, 4
            jr ra

        # a0 = entero -> a0 = cadena
        __rt_itoa:
            addi sp, sp, -8
            sw ra, 4(sp)
            sw a0, 0(sp)
            li a0, 12
            jal ra, __rt_alloc
            lw t5, 0(sp)
            lw ra, 4(sp)
            addi sp, sp, 8
            sb zero, 11(a0)
            addi t0, a0, 11
            li t3, 0
            bgez t5, __rt_i_pos
            li t3, 1
            sub t5, zero, t5
        __rt_i_pos:
            li t4, 10
            bnez t5, __rt_i_loop
            addi t0, t0, -1
            li t1, 48
            sb t1, 0(t0)
            j __rt_i_sign
        __rt_i_loop:
            beqz t5, __rt_i_sign
            remu t1, t5, t4
            divu t5, t5, t4
            addi t1, t1, 48
            addi t0, t0, -1
            sb t1, 0(t0)
            j __rt_i_loop
        __rt_i_sign:
            beqz t3, __rt_i_done
            addi t0, t0, -1
            li t1, 45
            sb t1, 0(t0)
        __rt_i_done:
            mv a0, t0
            jr ra

        # a0 = caracter -> a0 = cadena de un caracter
        __rt_ctoa:
            addi sp, sp, -8
            sw ra, 4(sp)
            sw a0, 0(sp)
            li a0, 4
            jal ra, __rt_alloc
            lw t0, 0(sp)
            sb t0, 0(a0)
            sb zero, 1(a0)
            lw ra, 4(sp)
            addi sp, sp, 8
            jr ra

        # a0 = booleano -> a0 = cadena
        __rt_btoa:
            beqz a0, __rt_b_f
            la a0, __rt_str_true
            jr ra
        __rt_b_f:
            la a0, __rt_str_false
            jr ra

        # a0 = bits de un flotante -> a0 = cadena 
        __rt_ftoa:
            addi sp, sp, -4
            sw ra, 0(sp)
            fmv.w.x ft0, a0
            li s1, 0
            fmv.w.x ft1, zero
            flt.s t0, ft0, ft1
            beqz t0, __rt_f_pos
            li s1, 1
            fneg.s ft0, ft0
        __rt_f_pos:
            fcvt.w.s s2, ft0, rtz
            fcvt.s.w ft1, s2
            fsub.s ft2, ft0, ft1
            li t0, 1000000
            fcvt.s.w ft3, t0
            fmul.s ft2, ft2, ft3
            li t0, 1056964608
            fmv.w.x ft4, t0
            fadd.s ft2, ft2, ft4
            fcvt.w.s s3, ft2, rtz
            li t0, 1000000
            blt s3, t0, __rt_f_nocarry
            sub s3, s3, t0
            addi s2, s2, 1
        __rt_f_nocarry:
            li a0, 32
            jal ra, __rt_alloc
            mv s4, a0
            mv s5, a0
            beqz s1, __rt_f_int
            li t0, 45
            sb t0, 0(s5)
            addi s5, s5, 1
        __rt_f_int:
            mv a0, s2
            jal ra, __rt_itoa
        __rt_f_cp:
            lbu t0, 0(a0)
            beqz t0, __rt_f_dot
            sb t0, 0(s5)
            addi s5, s5, 1
            addi a0, a0, 1
            j __rt_f_cp
        __rt_f_dot:
            li t0, 46
            sb t0, 0(s5)
            addi s5, s5, 1
            li t4, 10
            addi t3, s5, 5
            li t5, 6
        __rt_f_dl:
            remu t1, s3, t4
            divu s3, s3, t4
            addi t1, t1, 48
            sb t1, 0(t3)
            addi t3, t3, -1
            addi t5, t5, -1
            bnez t5, __rt_f_dl
            addi t3, s5, 5
            mv t0, s5
        __rt_f_strip:
            beq t3, t0, __rt_f_end
            lbu t1, 0(t3)
            li t2, 48
            bne t1, t2, __rt_f_end
            addi t3, t3, -1
            j __rt_f_strip
        __rt_f_end:
            sb zero, 1(t3)
            mv a0, s4
            lw ra, 0(sp)
            addi sp, sp, 4
            jr ra

        # a0, a1 = cadenas -> a0 = 1 si son iguales (null == null)
        __rt_streq:
            beq a0, a1, __rt_se_yes
            beqz a0, __rt_se_no
            beqz a1, __rt_se_no
        __rt_se_l:
            lbu t0, 0(a0)
            lbu t1, 0(a1)
            bne t0, t1, __rt_se_no
            beqz t0, __rt_se_yes
            addi a0, a0, 1
            addi a1, a1, 1
            j __rt_se_l
        __rt_se_yes:
            li a0, 1
            jr ra
        __rt_se_no:
            li a0, 0
            jr ra

        # a0 = cadena 
        __rt_print_str:
            bnez a0, __rt_ps_go
            la a0, __rt_str_null
        __rt_ps_go:
            li a7, 4
            ecall
            jr ra

        # a0 = booleano
        __rt_print_bool:
            beqz a0, __rt_pb_f
            la a0, __rt_str_true
            j __rt_print_str
        __rt_pb_f:
            la a0, __rt_str_false
            j __rt_print_str

        # lee una linea -> a0 = cadena nueva sin el salto de linea
        __rt_readline:
            addi sp, sp, -4
            sw ra, 0(sp)
            li a0, 256
            jal ra, __rt_alloc
            mv t5, a0
            li a1, 256
            li a7, 8
            ecall
            mv a0, t5
            mv t0, t5
        __rt_rl_l:
            lbu t1, 0(t0)
            beqz t1, __rt_rl_e
            li t2, 10
            beq t1, t2, __rt_rl_n
            addi t0, t0, 1
            j __rt_rl_l
        __rt_rl_n:
            sb zero, 0(t0)
        __rt_rl_e:
            lw ra, 0(sp)
            addi sp, sp, 4
            jr ra
        """;
}