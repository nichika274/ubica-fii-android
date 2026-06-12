package com.example.ubicafii.data.repository

import com.example.ubicafii.data.api.RetrofitClient
import com.example.ubicafii.data.model.Espacio

class EspacioRepository {
    // Conectamos el repositorio directamente para que consuma los datos de tu Node.js
    suspend fun obtenerEspacios(piso: Int? = null, tipo: String? = null): List<Espacio> {
        return RetrofitClient.instance.getEspacios(piso = piso, tipo = tipo)
    }
}