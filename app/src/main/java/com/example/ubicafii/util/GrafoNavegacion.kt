package com.example.ubicafii.util

data class Nodo(
    val id: String,
    val nombre: String,
    val x: Float,
    val y: Float,
    val mostrarEnSelector: Boolean = true
)

data class Conexion(
    val desde: String,
    val hasta: String,
    val peso: Float = 1f
)
object GrafoNavegacion {
    val nodos = listOf(
        // ─── Puntos visibles en el selector ───
        Nodo("entrada",      "Entrada Principal",       0.3895f, 0.6109f),
        Nodo("cafeteria",    "Cafetería Central",       0.2595f, 0.3851f),
        Nodo("pasilloA",     "Pasillo Bloque A",        0.3396f, 0.3704f),
        Nodo("pasilloB",     "Pasillo Bloque B",        0.3989f, 0.3581f),
        Nodo("pasilloC",     "Pasillo Bloque C",        0.4641f, 0.3719f),
        Nodo("bloqueG",      "Bloque G",                0.2235f, 0.2277f),
        Nodo("entrada_DFE",  "Bloques D/E/F",           0.1624f, 0.5627f, true), // 523, 1144
        Nodo("logo",         "Logo Universidad",        0.4231f, 0.4152f, true), // visible de nuevo

        // ─── Giros y caminos intermedios (ocultos) ───
        Nodo("decision",     "Bifurcación central",     0.3898f, 0.5189f, false), // 1256,1055
        Nodo("giroA",        "Giro hacia Bloque A",     0.3457f, 0.5076f, false), // 1114,1032
        Nodo("giroB",        "Giro hacia Bloque B",     0.3985f, 0.5140f, false), // 1284,1045
        Nodo("giroC",        "Giro hacia Bloque C",     0.4500f, 0.5140f, false), // 1450,1045

        // Giro hacia los bloques de la izquierda (D/E/F y G)
        Nodo("giro_izquierda", "Giro hacia D/E/F y G",  0.2452f, 0.5179f, false), // 790, 1053

        // Nodo intermedio para el Bloque G (783, 459)
        Nodo("bloqueG_intermedio", "Intermedio Bloque G", 0.2430f, 0.2257f, false),

        // Nodo previo al Bloque C (1448, 752) ← CORREGIDO
        Nodo("previoC",     "Previo Bloque C",         0.4495f, 0.3699f, false),

        // Caminos hacia D/E/F desde entrada
        Nodo("nodo_DFE_1",  "Entrada D/E/F (1)",       0.3883f, 0.6005f, false), // 1251, 1221
        Nodo("nodo_DFE_2",  "Entrada D/E/F (2)",       0.1624f, 0.6005f, false), // 523, 1221

        // Cafetería a giro central
        Nodo("cafe_giro1",   "Giro cafetería (1)",      0.2619f, 0.5214f, false), // 844, 1060
        Nodo("cafe_DFE1",    "Cafetería a D/E/F (1)",   0.2605f, 0.5917f, false), // 839, 1203
        Nodo("cafe_G1",      "Cafetería a Bloque G (1)",0.2446f, 0.3851f, false), // 788, 783

        // Escaleras (ocultas)
        Nodo("escalerasAB",  "Escaleras entre A y B",   0.3532f, 0.3458f, false),
        Nodo("escalerasBC",  "Escaleras entre B y C",   0.4458f, 0.3478f, false)
    )

    val conexiones = listOf(
        // === RUTAS ORIGINALES QUE FUNCIONABAN (A, B, C) ===
        Conexion("entrada", "decision"),

        Conexion("decision", "giroA"),
        Conexion("decision", "giroB"),
        Conexion("decision", "giroC"),

        Conexion("giroA", "pasilloA"),
        Conexion("giroB", "pasilloB"),
        Conexion("giroC", "previoC"),        // ahora pasa por el nodo previo
        Conexion("previoC", "pasilloC"),

        // Conexiones entre pasillos (por si se quiere ir de un bloque a otro)
        Conexion("pasilloA", "pasilloB"),
        Conexion("pasilloB", "pasilloC", 10f),

        // === NUEVAS RUTAS A D/E/F Y G (INDEPENDIENTES) ===
        // Desde la entrada, ir hacia D/E/F
        Conexion("entrada", "nodo_DFE_1"),
        Conexion("nodo_DFE_1", "nodo_DFE_2"),
        Conexion("nodo_DFE_2", "entrada_DFE"),

        // Desde la bifurcación, ir hacia el Bloque G
        Conexion("decision", "giro_izquierda"),
        Conexion("giro_izquierda", "bloqueG_intermedio"),
        Conexion("bloqueG_intermedio", "bloqueG"),

        // === RUTAS DESDE CAFETERÍA ===
        // Cafetería al giro central (para ir a A,B,C)
        Conexion("cafeteria", "cafe_giro1"),
        Conexion("cafe_giro1", "giroA"),

        // Cafetería a D/E/F
        Conexion("cafeteria", "cafe_DFE1"),
        Conexion("cafe_DFE1", "nodo_DFE_2"),

        // Cafetería a Bloque G
        Conexion("cafeteria", "cafe_G1"),
        Conexion("cafe_G1", "bloqueG"),

        // === LOGO ===
        Conexion("logo", "pasilloB"),

        // === ESCALERAS (para futuras rutas entre pisos) ===
        Conexion("pasilloA", "escalerasAB"),
        Conexion("pasilloB", "escalerasAB"),
        Conexion("pasilloB", "escalerasBC"),
        Conexion("pasilloC", "escalerasBC")
    )
}