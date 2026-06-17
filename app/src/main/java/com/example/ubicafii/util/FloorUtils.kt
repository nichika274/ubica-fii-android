package com.example.ubicafii.util

import com.example.ubicafii.R

/**
 * Devuelve el nombre legible del piso.
 */
fun getFloorLabel(piso: String): String = when (piso) {
    "0" -> "Planta baja"
    "1" -> "Primer piso"
    "2" -> "Segundo piso"
    else -> "Piso $piso"
}

/**
 * Devuelve el recurso de imagen del plano según el bloque y el piso.
 * Si no existe un plano específico para (bloque, piso), devuelve el genérico.
 */
fun getFloorPlanResource(bloque: String, piso: String): Int {
    // --- Planos específicos por bloque y piso ---
    when (bloque.uppercase()) {
        "D" -> {
            if (piso == "0") return R.drawable.plano_bloque_d_planta_baja
            if (piso == "1") return R.drawable.plano_bloque_d_primera_planta
        }
    }

    // --- Planos genéricos por piso (fallback) ---
    return when (piso) {
        "0" -> R.drawable.plano_piso1   // Planta baja genérico
        "1" -> R.drawable.plano_piso2   // Primer piso
        "2" -> R.drawable.plano_piso3   // Segundo piso
        else -> R.drawable.plano_piso1  // fallback
    }
}