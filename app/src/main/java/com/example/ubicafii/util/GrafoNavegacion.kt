package com.example.ubicafii.util

data class Nodo(
    val id: String,
    val nombre: String,
    val x: Float,
    val y: Float
)

data class Conexion(val desde: String, val hasta: String)

object GrafoNavegacion {
    val nodos = listOf(
        Nodo("entrada",      "Entrada Principal",       0.3895f, 0.6109f),
        Nodo("cafeteria",    "Cafetería Central",       0.2595f, 0.3851f),
        Nodo("pasilloA",     "Pasillo Bloque A",        0.3396f, 0.3704f),
        Nodo("pasilloB",     "Pasillo Bloque B",        0.3989f, 0.3581f),
        Nodo("pasilloC",     "Pasillo Bloque C",        0.4641f, 0.3719f),
        Nodo("escalerasAB",  "Escaleras entre A y B",   0.3532f, 0.3458f),
        Nodo("escalerasBC",  "Escaleras entre B y C",   0.4458f, 0.3478f),
        Nodo("bloqueG",      "Bloque G",                0.2235f, 0.2277f),
        Nodo("logo",         "Logo Universidad",        0.4231f, 0.4152f)   // opcional
    )

    val conexiones = listOf(
        // La entrada se conecta directamente a los pasillos A y B (caminos rectos)
        Conexion("entrada", "pasilloA"),
        Conexion("entrada", "pasilloB"),

        // Cafetería está cerca del pasillo A
        Conexion("cafeteria", "pasilloA"),

        // Los pasillos están conectados entre sí (forman un eje central)
        Conexion("pasilloA", "pasilloB"),
        Conexion("pasilloB", "pasilloC"),

        // Las escaleras se conectan solo a los pasillos que las rodean
        Conexion("pasilloA", "escalerasAB"),
        Conexion("pasilloB", "escalerasAB"),
        Conexion("pasilloB", "escalerasBC"),
        Conexion("pasilloC", "escalerasBC"),

        // Bloque G se conecta al pasillo A (es el más cercano)
        Conexion("pasilloA", "bloqueG"),

        // Logo conectado al pasillo B (está cerca)
        Conexion("logo", "pasilloB")
    )
}