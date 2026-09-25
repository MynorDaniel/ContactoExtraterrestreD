%estructuras
estructura Persona:
    entero edad
    cadena nombre
    flotante promedio
    caracter inicial
    bool activo
    entero calificaciones[5]

estructura Curso:
    cadena titulo
    entero creditos
    Persona instructor

estructura Punto:
    entero x
    entero y

estructura Local:
    entero valor

%funciones
definir sumar(entero a, entero b) -> entero:
    entero resultado
    resultado = a + b
    retornar resultado

definir restar(entero a, entero b) -> entero:
    retornar a - b

definir saludar(cadena nombre):
    imprimir("Hola " + nombre)

definir sinParametrosNiRetorno():
    imprimir("Sin parametros ni retorno")

definir procesarArreglo([] entero numeros, entero tam) -> entero:
    entero suma = 0
    para(entero i = 0; i < tam; i++):
        suma = suma + numeros[i]
    retornar suma

definir procesarPersona({} Persona p) -> cadena:
    retornar p.nombre

definir obtenerArreglo() -> [] entero:
    entero salida[3] = {1, 1, 1}
    retornar salida

definir obtenerPersona() -> {} Persona:
    Persona resultado
    resultado.nombre = "Alien"
    resultado.edad = 100
    retornar resultado

definir combos(entero x, [] entero arreglo, {} Persona p) -> bool:
    retornar verdadero

definir demoTipos():
    entero sinInicializacion
    entero edadUsuario = 25
    flotante temperatura = 36.6
    caracter inicial = 'A'
    bool bandera = verdadero
    bool bandera2 = falso
    cadena saludos = "Saludos zetarianos"
    entero numeros[5] = {10, 20, 30, 40, 50}
    entero matriz[2][2] = {{1, 2}, {3, 4}}
    Persona alumno1
    alumno1.nombre = "Yennifer"
    alumno1.calificaciones[0] = 100

definir demoCondicionales(entero x, entero y):
    bool resultado
    si (x > 0 && y > 0) entonces
        resultado = verdadero
    sino (x == 0 || y == 0) entonces
        resultado = falso
    contrario
        resultado = !(x < y)
    si (x != y) entonces
        imprimir("Son diferentes")

definir demoSwitch(entero opcion) -> entero:
    entero x
    elegir(opcion):
        caso 1:
            x = 10
            romper
        caso 2:
            x = 20
            romper
        siempre:
            x = 30
            romper
    retornar x

definir demoCiclos():
    entero contador = 0
    mientras(contador < 5) hacer
        contador++
        si (contador == 2) entonces
            continuar
    entero intentos = 0
    hacer:
        intentos++
        si (intentos == 4) entonces
            romper
    mientras(intentos < 10)
    para(entero i = 0; i < 10; i++):
        si (i == 3) entonces
            continuar
        si (i == 8) entonces
            romper

definir demoUnario(entero x, entero y):
    entero z
    z = -x + +y
    entero cociente
    cociente = x / y
    entero producto
    producto = x * y
    bool r
    r = !(x == y)
    z--
    z++

definir demoEstructuraLocal():
    Local l1 = {99}
    Punto p1 = {10, 20}
    Punto p2 = {5, 15}
    entero sumaX
    sumaX = p1.x + p2.x

definir demoLectura():
    cadena entrada = leer()
    entero valorLeido
    valorLeido = leer()
    imprimir(entrada)

definir demoAcceso({} Curso c) -> cadena:
    retornar c.instructor.nombre

definir principal():
    entero r1
    r1 = sumar(5, 10)
    entero r2
    r2 = restar(r1, 3)
    saludar("Zetarianos")
    imprimir(r2)
    entero arr[3] = {1, 2, 3}
    entero total
    total = procesarArreglo(arr, 3)
    imprimir(total)
    sinParametrosNiRetorno()



