#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef union Celda {
    int entero;
    double decimal;
    char caracter;
    const char* texto;
    union Celda* ref;
} Celda;

static char* rt_concat(const char* a, const char* b) {
    char* resultado = malloc(strlen(a) + strlen(b) + 1);
    strcpy(resultado, a);
    strcat(resultado, b);
    return resultado;
}

static char* rt_entero_a_texto(int v) {
    char* buffer = malloc(32);
    snprintf(buffer, 32, "%d", v);
    return buffer;
}

static char* rt_decimal_a_texto(double v) {
    char* buffer = malloc(64);
    snprintf(buffer, 64, "%g", v);
    return buffer;
}

static char* rt_caracter_a_texto(char v) {
    char* buffer = malloc(2);
    buffer[0] = v;
    buffer[1] = '\0';
    return buffer;
}


int main(void) {
    printf("%s", "Hola");
    return 0;
}

