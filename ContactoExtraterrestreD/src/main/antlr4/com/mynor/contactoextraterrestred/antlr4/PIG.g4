grammar PIG;

programa: importacion* seccionVariables? seccionMaior EOF;

importacion: IMPORT ruta;

ruta: ID (PUNTO ID)*;

seccionVariables: SECCION_VARIABLES MAYOR_QUE declaracionGlobal*;

declaracionGlobal: declaracionEsto | declaracionSeries;

seccionMaior: SECCION_MAIOR MAYOR_QUE sentencia* FIN_PROGRAMA PC;

bloque: LLA sentencia* LLC;

sentencia
    : declaracionEsto
    | declaracionSeries
    | asignacion
    | incrementoDecremento
    | sIf
    | sWhile
    | sDoWhile
    | sFor
    | sBreak
    | sContinue
    | sImprimir
    | sLeer
    | sExpresion
    ;

declaracionEsto: ESTO ID DP tipo inicializacion PC;

declaracionSeries: SERIES ID dimension+ DP tipo inicializacion? PC;

dimension: CA NUMERO_ENTERO CC;

inicializacion
    : expresion
    | lista_expresiones
    ;

lista_expresiones: LLA (elemento_lista (COMA elemento_lista)*)? LLC;

elemento_lista: expresion | lista_expresiones;

tipo: tipoBase | ID;

tipoBase: NUMERUS | DECIMALIS | TEXTUM | LITTERA | BOOL;

asignacion: destino ASIG expresion PC;

incrementoDecremento: destino (INCREMENTO | DECREMENTO) PC;

destino: ID acceso*;

acceso: acceso_arreglo | acceso_struct;

acceso_arreglo: CA expresion CC;

acceso_struct: PUNTO ID;

sIf
    : SI PAREN_A expresion PAREN_C bloque
      (ALITER PAREN_A expresion PAREN_C bloque)*
      (ALITER bloque)?
      FIN_BLOQUE PC
    ;

sWhile: DUM PAREN_A expresion PAREN_C bloque FIN_BLOQUE PC;

sDoWhile: FACERE bloque DUM PAREN_A expresion PAREN_C PC;

sFor
    : PER PAREN_A forInit? PC expresion? PC forActualizacion? PAREN_C bloque
    ;

forInit
    : ESTO ID DP tipo expresion
    | destino ASIG expresion
    ;

forActualizacion
    : destino (INCREMENTO | DECREMENTO)
    | destino ASIG expresion
    ;

sBreak: INTERRUMPE PC;

sContinue: PERGE PC;

sImprimir: PRINT expresion (PRINT expresion)* PC;

sLeer: destino? LEER PC;

sExpresion: expresion PC;

expresion: expresionOr;

expresionOr: expresionAnd (OR expresionAnd)*;

expresionAnd: expresionIgualdad (AND expresionIgualdad)*;

expresionIgualdad: expresionRelacional (ASIG expresionRelacional)*;

expresionRelacional
    : expresionAditiva
      ((MENOR_QUE | MAYOR_QUE | MENOR_IGUAL | MAYOR_IGUAL) expresionAditiva)*
    ;

expresionAditiva: expresionMultiplicativa ((MAS | MENOS) expresionMultiplicativa)*;

expresionMultiplicativa: expresionUnaria ((POR | DIVISION) expresionUnaria)*;

expresionUnaria
    : (MENOS | MAS) expresionUnaria
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
    | PAREN_A expresion PAREN_C
    ;

creacionObjeto: NOVUS ID PAREN_A argumentos? PAREN_C;

argumentos: expresion (COMA expresion)*;

literal
    : NUMERO_ENTERO
    | DECIMAL
    | STRING
    | CHAR
    | VERUM
    | FALSUS
    ;

IMPORT: 'import';

SECCION_VARIABLES: 'VARIABILES';
SECCION_MAIOR: 'MAIOR';

FIN_PROGRAMA: 'FINIS';   
FIN_BLOQUE: 'finis';     

ESTO: 'esto';
SERIES: 'series';

NUMERUS: 'numerus';
DECIMALIS: 'decimalis';
TEXTUM: 'textum';
LITTERA: 'littera';
BOOL: 'bool';

VERUM: 'verum';
FALSUS: 'falsus';

SI: 'si';
ALITER: 'aliter';

DUM: 'dum';
FACERE: 'facere';
PER: 'per';

INTERRUMPE: 'interrumpe';
PERGE: 'perge';

NOVUS: 'novus';

DP: ':';
PC: ';';
COMA: ',';
PUNTO: '.';
CA: '[';
CC: ']';
PAREN_A: '(';
PAREN_C: ')';
LLA: '{';
LLC: '}';

ASIG: '=';

MAS: '+';
MENOS: '-';
POR: '*';
DIVISION: '/';

MAYOR_QUE: '>';
MENOR_QUE: '<';
MAYOR_IGUAL: '>=';
MENOR_IGUAL: '<=';  

AND: '&&';
OR: '||';

INCREMENTO: '++';
DECREMENTO: '--';    

PRINT: '>>';
LEER: '<<';

NUMERO_ENTERO: [0-9]+;
DECIMAL: [0-9]+ '.' [0-9]*;
CHAR: '\'' . '\'';
STRING: '"' (~["\r\n])* '"';

ID: [A-Za-z_$][A-Za-z0-9_$]*;

COMENTARIO_LINEA: '//' ~[\r\n]* -> skip;
COMENTARIO_BLOQUE: '##' .*? '##' -> skip;

NL: '\r'? '\n' -> skip;
ESPACIOS: [ \t]+ -> skip;
