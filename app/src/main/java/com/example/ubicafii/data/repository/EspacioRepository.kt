package com.example.ubicafii.data.repository

import com.example.ubicafii.data.api.RetrofitClient
import com.example.ubicafii.data.model.Espacio

class EspacioRepository {
    // Conectamos el repositorio directamente para que consuma los datos de tu Node.js
    suspend fun obtenerEspacios(piso: Int? = null, tipo: String? = null): List<Espacio> {
        return RetrofitClient.instance.getEspacios(piso = piso, tipo = tipo)
    }
    suspend fun buscarEspacios(query: String): List<Espacio> {
        return RetrofitClient.instance.getEspacios(q = query)
    }
    suspend fun agregarEspacio(espacio: Espacio): Espacio {
        return RetrofitClient.instance.crearEspacio(espacio)
    }

    suspend fun editarEspacio(id: Int, espacio: Espacio): Espacio {
        return RetrofitClient.instance.actualizarEspacio(id, espacio)
    }

    suspend fun borrarEspacio(id: Int) {
        RetrofitClient.instance.eliminarEspacio(id)
    }
    suspend fun obtenerEspacio(id: Int): Espacio {
        return RetrofitClient.instance.getEspacio(id)
    }
}