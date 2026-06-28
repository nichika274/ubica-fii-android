package com.example.ubicafii.data.model

data class Espacio(
    val id: Int,
    val nombre: String,
    val tipo: String,
    val piso: Int,

    val descripcion: String = "",
    val fotoUrl: String = "",
    val indicaciones: String = "",

    val bloque: String,

    val coordenadaX: Float = 0.5f,
    val coordenadaY: Float = 0.5f,

    // mapa general campus
    val mapaX: Float = 0f,
    val mapaY: Float = 0f,
    val mapaWidth: Float = 0f,
    val mapaHeight: Float = 0f
)