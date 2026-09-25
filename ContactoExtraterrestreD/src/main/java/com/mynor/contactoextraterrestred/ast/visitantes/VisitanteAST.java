package com.mynor.contactoextraterrestred.ast.visitantes;

import com.mynor.contactoextraterrestred.ast.*;

public interface VisitanteAST<T> {

    T visitar(NodoPrograma nodo);

    T visitar(NodoImportacion nodo);

    T visitar(NodoEstructura nodo);

    T visitar(NodoAtributo nodo);

    T visitar(NodoFuncion nodo);

    T visitar(NodoParametro nodo);

    T visitar(NodoTipo nodo);

    T visitar(NodoDimension nodo);

    T visitar(NodoDeclaracion nodo);

    T visitar(NodoListaExpresiones nodo);

    T visitar(NodoBloque nodo);

    T visitar(NodoAsignacion nodo);

    T visitar(NodoIncrementoDecremento nodo);

    T visitar(NodoIf nodo);

    T visitar(NodoSinoSi nodo);

    T visitar(NodoSwitch nodo);

    T visitar(NodoCaso nodo);

    T visitar(NodoFor nodo);

    T visitar(NodoWhile nodo);

    T visitar(NodoDoWhile nodo);

    T visitar(NodoBreak nodo);

    T visitar(NodoContinue nodo);

    T visitar(NodoReturn nodo);

    T visitar(NodoImprimir nodo);

    T visitar(NodoLeer nodo);

    T visitar(NodoExpresionSentencia nodo);

    T visitar(NodoBinaria nodo);

    T visitar(NodoUnaria nodo);

    T visitar(NodoTernaria nodo);

    T visitar(NodoLiteral nodo);

    T visitar(NodoIdentificador nodo);

    T visitar(NodoAccesoArreglo nodo);

    T visitar(NodoAccesoCampo nodo);

    T visitar(NodoLlamada nodo);

    T visitar(NodoNuevoObjeto nodo);

    T visitar(NodoNuevoArreglo nodo);

    T visitar(NodoLecturaEntrada nodo);
}
