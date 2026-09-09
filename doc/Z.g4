grammar Z;

unidad: clase EOF;

clase: PUBLIC CLASS ID LLA miembro* LLC;

miembro
    : atributo
    | constructor
    | metodo
    ;

atributo: tipo ID (ASIG expresion)? PC;

constructor: PUBLIC ID PAREN_A parametros? PAREN_C bloque;

metodo: PUBLIC tipo ID PAREN_A parametros? PAREN_C bloque;

parametros: parametro (COMA parametro)*;

parametro: tipo ID;

tipoBase: INT | DOUBLE | CHAR_T | BOOLEAN | STRING_T | VOID | ID;

tipo: tipoBase (CA CC)*;

bloque: LLA sentencia* LLC;

sentencia
    : bloque
    | declaracionLocal
    | asignacion
    | incrementoDecremento
    | sIf
    | sSwitch
    | sFor
    | sWhile
    | sDoWhile
    | sBreak
    | sContinue
    | sReturn
    | sPrint
    | sPrintln
    | sExpresion
    ;

declaracionLocal: tipo ID (ASIG inicializacion)? PC;

inicializacion
    : expresion
    | listaExpresiones
    ;

listaExpresiones: LLA (expresion (COMA expresion)*)? LLC;

asignacion
    : destino (ASIG | ASIG_SUMA | ASIG_RESTA | ASIG_MULT) expresion PC
    ;

incrementoDecremento: destino (INCREMENTO | DECREMENTO) PC;

destino: ID acceso*;

acceso: acceso_arreglo | acceso_struct;

acceso_arreglo: CA expresion CC;

acceso_struct: PUNTO ID;

sIf: IF PAREN_A expresion PAREN_C sentencia (ELSE sentencia)?;

sSwitch: SWITCH PAREN_A expresion PAREN_C LLA casoSwitch* defaultSwitch? LLC;

casoSwitch: CASE literal DP sentencia*;

defaultSwitch: DEFAULT DP sentencia*;

sFor
    : FOR PAREN_A forInit? PC expresion? PC forActualizacion? PAREN_C sentencia
    ;

forInit
    : tipo ID (ASIG expresion)?
    | destino (ASIG | ASIG_SUMA | ASIG_RESTA | ASIG_MULT) expresion
    ;

forActualizacion
    : destino (INCREMENTO | DECREMENTO)
    | destino (ASIG | ASIG_SUMA | ASIG_RESTA | ASIG_MULT) expresion
    ;

sWhile: WHILE PAREN_A expresion PAREN_C sentencia;

sDoWhile: DO sentencia WHILE PAREN_A expresion PAREN_C PC;

sBreak: BREAK PC;

sContinue: CONTINUE PC;

sReturn: RETURN expresion? PC;

sPrint: PRINT PAREN_A expresion PAREN_C PC;

sPrintln: PRINTLN PAREN_A expresion PAREN_C PC;

sExpresion: expresion PC;

expresion: expresionTernario;

expresionTernario
    : expresionOr (INTERROGACION expresion DP expresion)?
    ;

expresionOr: expresionAnd (OR expresionAnd)*;

expresionAnd: expresionIgualdad (AND expresionIgualdad)*;

expresionIgualdad
    : expresionRelacional ((IGUAL | DIFERENTE) expresionRelacional)*
    ;

expresionRelacional
    : expresionAditiva
      ((MENOR_QUE | MAYOR_QUE | MENOR_IGUAL | MAYOR_IGUAL) expresionAditiva)*
    ;

expresionAditiva
    : expresionMultiplicativa ((MAS | MENOS) expresionMultiplicativa)*
    ;

expresionMultiplicativa
    : expresionUnaria ((POR | DIVISION | MODULO) expresionUnaria)*
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
    | creacionObjeto
    | creacionArreglo
    | READLN PAREN_A PAREN_C
    | PAREN_A expresion PAREN_C
    ;

creacionObjeto: NEW ID PAREN_A argumentos? PAREN_C;

creacionArreglo: NEW tipoBase (CA expresion CC)+;

argumentos: expresion (COMA expresion)*;

literal
    : NUMERO_ENTERO
    | DECIMAL
    | STRING
    | CHAR
    | VERDADERO
    | FALSO
    | NULL
    ;

PUBLIC: 'public';
CLASS: 'class';

INT: 'int';
DOUBLE: 'double';
CHAR_T: 'char';
BOOLEAN: 'boolean';
STRING_T: 'String';
VOID: 'void';

NEW: 'new';

IF: 'if';
ELSE: 'else';

SWITCH: 'switch';
CASE: 'case';
DEFAULT: 'default';

FOR: 'for';
WHILE: 'while';
DO: 'do';

BREAK: 'break';
CONTINUE: 'continue';
RETURN: 'return';

PRINT: 'print';
PRINTLN: 'println';
READLN: 'readln';

VERDADERO: 'true';
FALSO: 'false';
NULL: 'null';

ASIG: '=';
ASIG_SUMA: '+=';
ASIG_RESTA: '-=';
ASIG_MULT: '*=';

POR: '*';
MAS: '+';
MENOS: '-';
DIVISION: '/';
MODULO: '%';

INCREMENTO: '++';
DECREMENTO: '--';

IGUAL: '==';
DIFERENTE: '!=';
MENOR_QUE: '<';
MAYOR_QUE: '>';
MENOR_IGUAL: '<=';
MAYOR_IGUAL: '>=';

AND: '&&';
OR: '||';
NEGACION: '!';

INTERROGACION: '?';

PAREN_A: '(';
PAREN_C: ')';

LLA: '{';
LLC: '}';

CA: '[';
CC: ']';

PUNTO: '.';
COMA: ',';
PC: ';';
DP: ':';

NUMERO_ENTERO: [0-9]+;
DECIMAL: [0-9]+ '.' [0-9]*;
CHAR: '\'' . '\'';
STRING: '"' (~["\r\n])* '"';

ID: [A-Za-z_$][A-Za-z0-9_$]*;

COMENTARIO_LINEA: '//' ~[\r\n]* -> skip;
COMENTARIO_BLOQUE: '/*' .*? '*/' -> skip;

NL: '\r'? '\n' -> skip;
ESPACIOS: [ \t]+ -> skip;
