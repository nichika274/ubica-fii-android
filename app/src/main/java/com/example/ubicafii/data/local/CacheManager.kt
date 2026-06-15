package com.example.ubicafii.data.local

import android.content.Context
import com.example.ubicafii.data.model.Espacio
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class CacheManager(private val context: Context) {
    private val gson = Gson()
    private val cacheFile: File
        get() = File(context.filesDir, "espacios_cache.json")

    suspend fun guardarEspacios(espacios: List<Espacio>) {
        withContext(Dispatchers.IO) {
            val json = gson.toJson(espacios)
            cacheFile.writeText(json)
        }
    }

    suspend fun cargarEspacios(): List<Espacio> {
        return withContext(Dispatchers.IO) {
            if (cacheFile.exists()) {
                val json = cacheFile.readText()
                val type = object : TypeToken<List<Espacio>>() {}.type
                gson.fromJson(json, type)
            } else {
                emptyList()
            }
        }
    }
    suspend fun limpiarCache() {
        withContext(Dispatchers.IO) {
            cacheFile.delete()
        }
    }
}