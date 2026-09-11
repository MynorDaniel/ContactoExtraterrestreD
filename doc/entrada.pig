import carpeta.Objeto1.z
import carpeta.Funciones.y

##
    Sección opcional de variables, puede no existir
    En esta sección solo se definen variables, arreglos
    o estructuras globales
##
VARIABILES>
esto edad : numerus 20;
esto cifrado : bool falsus;
esto comandante : textum "Estudiante X";
esto fuerza : numerus 10;
esto poder : numerus 0;
esto gravedad : decimalis 9.81;
esto inicial : littera 'a';
esto activo : bool verum;
series mis_enteros[3] : numerus {1, 1, 1};
series mis_enteros_[3] : numerus;
series nombres[2] : textum {"Hola", "Adios"};
series matriz[2][2] : numerus {{1, 2}, {3, 4}};
series misObjetos[10] : Persona;
esto miObjeto : Persona novus Persona(12, "Profesor");
esto otroObjeto : Persona novus Persona(12, miObjeto, novus Persona());

MAIOR>
>> "Hola comandante!" ;
>> "Ingresa tu nombre por favor" ;
comandante << ;
>> "Bienvenido" >> comandante ;
>> "Ingresa tu edad" ;
edad << ;

si (edad >= 18) {
    cifrado = verum;
    fuerza = 12;
} finis ;

si (edad >= 18) {
    cifrado = verum;
} aliter {
    cifrado = falsus;
} finis ;

si (edad > 10 && edad < 20) {
    poder = 1;
} aliter (edad = 18) {
    poder = 2;
} aliter (edad > 20) {
    poder = 3;
} aliter {
    poder = 0;
} finis ;

esto poderCalculado : numerus fuerza * 2;
poderCalculado = poderCalculado + 1;
poderCalculado = poderCalculado - 1;

// ciclo dum con interrupcion
dum (poder < 100) {
    poder = poder + 1;
    si (poder = 50) {
        interrumpe;
    } finis;
} finis ;

facere {
    fuerza = fuerza + 1;
    si (fuerza = 20) {
        interrumpe;
    } finis;
} dum (fuerza < 30);

per (esto i : numerus 0; i < 10; i++) {
    si (i = 5) {
        perge;
    } finis;
    si (i = 8) {
        interrumpe;
    } finis;
}

per (poder = 0; poder < 5; poder = poder + 1) {
    >> poder;
}

miObjeto.nombre = "Yennifer";
misObjetos[9].hablar(miObjeto.getNombre());
comandante = miObjeto.apellidos[0].getNombre();

esto resultadoLogico : bool verum || 1 = 1;
esto comparacion : bool (edad > 10) && (poder <= 5) || (fuerza >= 1);
esto expresionCompleja : numerus (fuerza + poder) * 2 - (edad / 2);
esto expresionUnaria : numerus -fuerza + +poder;

>> "Tu poder es: " >> calcularPoder(fuerza);
>> "La puerta esta cifrada?" >> cifrado ;

FINIS;
