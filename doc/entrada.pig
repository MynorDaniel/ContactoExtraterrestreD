import contacto.Contacto
import funciones.Y

VARIABILES>
esto edad : numerus 24;
esto nombre : textum "Comandante Zeta";
esto activo : bool verum;

esto persona1 : Persona novus Persona();
esto curso1 : Curso novus Curso();
esto punto1 : Punto novus Punto();

esto contacto1 : Contacto novus Contacto("Ana Zeta", 30);
esto contacto2 : Contacto novus Contacto();

series notasContacto[3] : numerus {90, 80, 70};

esto sumaY : numerus sumar(5, 10);
esto restaY : numerus restar(sumaY, 3);
series arregloY[3] : numerus obtenerArreglo();
esto personaY : Persona obtenerPersona();

MAIOR>
>> "Ingresa tu edad:";
edad << ;

>> "=== Persona (definida en el .y) ===";
persona1.nombre = "Comandante X";
persona1.edad = 150;
persona1.calificaciones[0] = 100;
>> persona1.nombre;
>> persona1.edad;
>> persona1.calificaciones[0];

>> "=== Curso y Punto (definidas en el .y) ===";
curso1.titulo = "Xenolinguistica";
curso1.creditos = 4;
curso1.instructor = persona1;
>> curso1.titulo;
>> curso1.instructor.nombre;

punto1.x = 3;
punto1.y = 7;
>> punto1.x + punto1.y;

>> "=== Funciones sueltas del .y ===";
saludar(nombre);
sinParametrosNiRetorno();
>> sumaY;
>> restaY;
>> arregloY[0];
>> personaY.nombre;

>> "=== Contacto (definida en el .z) ===";
contacto1.saludar();
contacto2.saludar();
>> contacto1.esMayorDeEdad();
>> contacto1.calcularAnioNacimiento(2024);
>> contacto1.promedioNotas(notasContacto, 3);
>> contacto1.demoTernario(edad);

si (contacto1.esMayorDeEdad()) {
    >> "El contacto es mayor de edad";
} aliter {
    >> "El contacto es menor de edad";
} finis;

esto total : numerus sumar(persona1.edad, contacto1.calcularAnioNacimiento(2024));
>> "Total combinado:";
>> total;

per (esto i : numerus 0; i < 3; i++) {
    >> notasContacto[i];
}

FINIS;
