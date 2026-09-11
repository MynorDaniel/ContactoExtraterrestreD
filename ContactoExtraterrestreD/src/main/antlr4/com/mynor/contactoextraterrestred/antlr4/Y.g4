grammar Y;

tokens { INDENTACION, DESINDENTACION }
    
@lexer::header {
    import java.util.ArrayDeque;
    }
    
@lexer::members {

    private ArrayDeque<Integer> indentStack = new ArrayDeque<>();
    private ArrayDeque<Token> pendingTokens = new ArrayDeque<>();

    {
        indentStack.push(0);
    }

    private boolean comentarioAdelante() {
        int c1 = _input.LA(1);
        int c2 = _input.LA(2);
        return c1 == '/' && (c2 == '/' || c2 == '*');
    }

    private Token nuevoToken(int tipo, Token base) {
        CommonToken t = new CommonToken(base);
        t.setType(tipo);
        t.setText(tipo == YParser.INDENTACION ? "<INDENTACION>" : "<DESINDENTACION>");
        return t;
    }

    @Override
    public Token nextToken() {
        if (!pendingTokens.isEmpty()) {
            return pendingTokens.poll();
        }

        Token t = super.nextToken();

        if (t.getType() == Token.EOF) {
            while (indentStack.peek() > 0) {
                indentStack.pop();
                pendingTokens.offer(nuevoToken(YParser.DESINDENTACION, t));
            }
            pendingTokens.offer(t);
            return pendingTokens.poll();
        }

        if (t.getType() != NL) {
            return t;
        }

        String texto = t.getText();
        int ultimoSalto = Math.max(texto.lastIndexOf('\n'), texto.lastIndexOf('\r'));
        int anchoIndentacion = texto.length() - (ultimoSalto + 1);

        int siguiente = _input.LA(1);

        if (siguiente == -1) {
            pendingTokens.offer(t);
            return pendingTokens.poll();
        }

        if (siguiente == '\r' || siguiente == '\n' || comentarioAdelante()) {
            return nextToken();
        }

        pendingTokens.offer(t);

        int actual = indentStack.peek();
        if (anchoIndentacion > actual) {
            indentStack.push(anchoIndentacion);
            pendingTokens.offer(nuevoToken(YParser.INDENTACION, t));
        } else if (anchoIndentacion < actual) {
            while (indentStack.peek() > anchoIndentacion) {
                indentStack.pop();
                pendingTokens.offer(nuevoToken(YParser.DESINDENTACION, t));
            }
            if (indentStack.peek() != anchoIndentacion) {
                throw new RuntimeException(
                    "Indentacion inconsistente en la linea " + t.getLine());
            }
        }

        return pendingTokens.poll();
    }
    }
    
    s: secciones EOF;

secciones: estructuras funciones;

estructuras: (ESTRUCTURAS NL estructura*)?;

funciones: FUNCIONES NL funcion*;

estructura: ESTRUCTURA ID DP NL INDENTACION atributo+ DESINDENTACION;

atributo: tipo ID dimension* NL;

dimension: CA NUMERO_ENTERO CC;

tipo: CADENA | ENTERO | FLOTANTE | CARACTER | BOOL | ID;

funcion
    : DEFINIR ID PAREN_A parametros_funcion? PAREN_C tipo_retorno? DP NL
    INDENTACION elemento_funcion+ DESINDENTACION
    ;

parametros_funcion: parametro_funcion (COMA parametro_funcion)*;

parametro_funcion: info_parametro ID;

info_parametro
    : dimensiones_parametro tipo
    | estructuraParametro tipo
    | tipo
    ;

dimensiones_parametro: (CA CC)+;

estructuraParametro: LLA LLC;

tipo_retorno: MENOS MAYOR_QUE info_parametro;

elemento_funcion: sentencia | declaracion;

declaracion
    : estructura
    | tipo ID dimension* (ASIG inicializacion)? NL
    ;

inicializacion
    : expresion
    | lista_expresiones
    ;

lista_expresiones: LLA (elemento_lista (COMA elemento_lista)*)? LLC;

elemento_lista: expresion | lista_expresiones;

sentencia
    : asignacion
    | incrementoDecremento
    | sIf
    | sSwitch
    | sFor
    | sWhile
    | sDoWhile
    | sContinue
    | sBreak
    | sReturn
    | sImprimir
    | sExpresion
    ;

asignacion: destino ASIG expresion NL;

incrementoDecremento: destino (INCREMENTO | DECREMENTO) NL;

destino: ID acceso*;

acceso: acceso_arreglo | acceso_struct;

acceso_arreglo: CA expresion CC;

acceso_struct: PUNTO ID;

sIf
    : SI PAREN_A expresion PAREN_C ENTONCES NL
    INDENTACION sentencia+ DESINDENTACION
    sSino*
    sContrario?
    ;

sSino
    : SINO PAREN_A expresion PAREN_C ENTONCES NL
    INDENTACION sentencia+ DESINDENTACION
    ;

sContrario
    : CONTRARIO NL
    INDENTACION sentencia+ DESINDENTACION
    ;

sSwitch
    : ELEGIR PAREN_A expresion PAREN_C DP NL
    INDENTACION caso+ siempre? DESINDENTACION
    ;

caso: CASO literal DP NL INDENTACION sentencia+ DESINDENTACION;

siempre: SIEMPRE DP NL INDENTACION sentencia+ DESINDENTACION;

sFor
    : PARA PAREN_A forInit? PC expresion? PC forActualizacion? PAREN_C DP NL
    INDENTACION sentencia+ DESINDENTACION
    ;

forInit
    : tipo ID (ASIG expresion)?
    | destino ASIG expresion
    ;

forActualizacion
    : destino (INCREMENTO | DECREMENTO)
    | destino ASIG expresion
    ;

sWhile
    : MIENTRAS PAREN_A expresion PAREN_C HACER NL
    INDENTACION sentencia+ DESINDENTACION
    ;

sDoWhile
    : HACER DP NL
    INDENTACION sentencia+ DESINDENTACION
    NL* MIENTRAS PAREN_A expresion PAREN_C NL
    ;

sContinue: CONTINUAR NL;

sBreak: ROMPER NL;

sReturn: RETORNAR expresion? NL;

sImprimir: IMPRIMIR PAREN_A expresion PAREN_C NL;

sExpresion: expresion NL;

expresion: expresionOr;

expresionOr: expresionAnd (OR expresionAnd)*;

expresionAnd: expresionIgualdad (AND expresionIgualdad)*;

expresionIgualdad
    : expresionRelacional ((IGUAL | DIFERENTE) expresionRelacional)*
    ;

expresionRelacional
    : expresionAditiva ((MENOR_QUE | MAYOR_QUE) expresionAditiva)*
    ;

expresionAditiva
    : expresionMultiplicativa ((MAS | MENOS) expresionMultiplicativa)*
    ;

expresionMultiplicativa
    : expresionUnaria ((POR | DIVISION) expresionUnaria)*
    ;

expresionUnaria
    : (NEGACION | MENOS | MAS) expresionUnaria
    | expresionPostfija
    ;

expresionPostfija: expresionPrimaria sufijo*;

sufijo
    : CA expresion CC
    | PUNTO ID
    | PAREN_A argumentos? PAREN_C
    ;

expresionPrimaria
    : literal
    | ID
    | LEER PAREN_A PAREN_C
    | PAREN_A expresion PAREN_C
    ;

argumentos: expresion (COMA expresion)*;

literal
    : NUMERO_ENTERO
    | DECIMAL
    | STRING
    | CHAR
    | VERDADERO
    | FALSO
    ;

ESTRUCTURAS: '%estructuras';
FUNCIONES: '%funciones';

ESTRUCTURA: 'estructura';
DEFINIR: 'definir';
RETORNAR: 'retornar';

CADENA: 'cadena';
ENTERO: 'entero';
FLOTANTE: 'flotante';
CARACTER: 'caracter';
BOOL: 'bool';

VERDADERO: 'verdadero';
FALSO: 'falso';

SI: 'si';
ENTONCES: 'entonces';
SINO: 'sino';
CONTRARIO: 'contrario';

ELEGIR: 'elegir';
CASO: 'caso';
ROMPER: 'romper';
SIEMPRE: 'siempre';
 
PARA: 'para';
CONTINUAR: 'continuar';

MIENTRAS: 'mientras';
HACER: 'hacer';

IMPRIMIR: 'imprimir';
LEER: 'leer';

DP: ':';
CA: '[';
CC: ']';
PAREN_A: '(';
PAREN_C: ')';
LLA: '{';
LLC: '}';
ASIG: '=';
PUNTO: '.';
COMA: ',';
PC: ';';

POR: '*';
MAS: '+';
MENOS: '-';
DIVISION: '/';

IGUAL: '==';
DIFERENTE: '!=';
MENOR_QUE: '<';
MAYOR_QUE: '>';
AND: '&&';
OR: '||';
NEGACION: '!';

INCREMENTO: '++';
DECREMENTO: '--';

NUMERO_ENTERO: [0-9]+;
DECIMAL: [0-9]+ '.' [0-9]*;
CHAR: '\'' . '\'';
STRING: '"' (~["\r\n])* '"';

ID: [A-Za-z_$][A-Za-z0-9_$]*;

COMENTARIO_LINEA: '//' ~[\r\n]* -> skip;
COMENTARIO_BLOQUE: '/*' .*? '*/' -> skip;

NL: '\r'? '\n' [ \t]*;

WS: [ \t]+ -> skip;