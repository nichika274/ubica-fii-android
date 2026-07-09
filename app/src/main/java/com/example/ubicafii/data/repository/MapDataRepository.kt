package com.example.ubicafii.data.repository

import android.content.Context
import com.example.ubicafii.data.api.ApiService // Ajusta según tu paquete de API
import com.example.ubicafii.data.model.BloqueMapa // Ajusta tus paquetes de modelo
import com.example.ubicafii.data.model.PuntoInteres
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MapDataRepository(context: Context) {

    private val prefs = context.getSharedPreferences("map_cache", Context.MODE_PRIVATE)
    private val gson = Gson()

    // ==========================================
    // GESTIÓN DE BLOQUES
    // ==========================================

    fun getBloques(): List<BloqueMapa> {
        val json = prefs.getString("bloques_cache", null)
        return if (json != null) {
            try {
                val type = object : TypeToken<List<BloqueMapa>>() {}.type
                gson.fromJson(json, type)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    suspend fun fetchAndCacheBloques(api: ApiService): Boolean = withContext(Dispatchers.IO) {
        try {
            val bloques = api.getBloques() // Asumiendo que tu ApiService tiene esta función
            val json = gson.toJson(bloques)
            prefs.edit().putString("bloques_cache", json).apply()
            true
        } catch (e: Exception) {
            false // Si falla (offline), retornamos false pero la caché se mantiene intacta
        }
    }

    // ==========================================
    // GESTIÓN DE PUNTOS DE INTERÉS
    // ==========================================

    fun getPuntosInteres(): List<PuntoInteres> {
        val json = prefs.getString("puntos_cache", null)
        return if (json != null) {
            try {
                val type = object : TypeToken<List<PuntoInteres>>() {}.type
                gson.fromJson(json, type)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    suspend fun fetchAndCachePuntosInteres(api: ApiService): Boolean = withContext(Dispatchers.IO) {
        try {
            val puntos = api.getPuntosInteres() // Asumiendo que tu ApiService tiene esta función
            val json = gson.toJson(puntos)
            prefs.edit().putString("puntos_cache", json).apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    // ==========================================
    // LIMPIEZA DE CACHÉ (Opcional, útil para Logout)
    // ==========================================
    fun clearMapCache() {
        prefs.edit().clear().apply()
    }
}