package com.example.ubicafii.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.ubicafii.data.model.Espacio
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

class CacheManager(private val context: Context) {
    private val gson = Gson()
    private val cacheFile: File
        get() = File(context.filesDir, "espacios_cache.json")

    companion object {
        private const val PREFS_NAME = "cache_prefs"
        private const val KEY_LAST_UPDATE = "last_update"
    }

    suspend fun guardarEspacios(espacios: List<Espacio>) {
        withContext(Dispatchers.IO) {
            val json = gson.toJson(espacios)
            cacheFile.writeText(json)
            actualizarTimestamp()
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

    fun actualizarTimestamp() {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_UPDATE, System.currentTimeMillis()).apply()
    }

    fun isCacheFresh(maxAgeMinutes: Long = 5): Boolean {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastUpdate = prefs.getLong(KEY_LAST_UPDATE, 0L)
        val maxAgeMillis = TimeUnit.MINUTES.toMillis(maxAgeMinutes)
        return (System.currentTimeMillis() - lastUpdate) < maxAgeMillis
    }
}