package com.example.ubicafii.data.repository

import android.content.Context
import com.example.ubicafii.data.api.RetrofitClient
import com.example.ubicafii.data.local.CacheManager
import com.example.ubicafii.data.model.Espacio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class EspacioRepository(private val context: Context) {

    private val api = RetrofitClient.instance
    private val cache = CacheManager(context)

    // =================================================================================
    // NUEVO/MODIFICADO: OBTENER ESPACIOS CON FLOW (Estrategia Cache-First Inteligente)
    // =================================================================================
    fun obtenerEspaciosFlow(piso: Int? = null, tipo: String? = null): Flow<List<Espacio>> = flow {
        // 1. Emitir inmediatamente lo que haya en caché (Modo Offline o Carga rápida)
        val cached = cache.cargarEspacios()
        val cachedFiltrados = filtrarEspacios(cached, piso, tipo)

        if (cachedFiltrados.isNotEmpty()) {
            emit(cachedFiltrados)
        }

        // 🔥 MODIFICACIÓN: Si el caché es fresco y ya emitimos datos locales, no tocamos la API
        if (cache.isCacheFresh() && cached.isNotEmpty()) {
            return@flow
        }

        // 2. Intentar buscar datos frescos del backend en un hilo de I/O si el caché expiró
        try {
            val todosLosEspaciosRemotos = withContext(Dispatchers.IO) {
                api.getEspacios(piso = null, tipo = null)
            }

            // Actualizamos la caché global y refrescamos el timestamp automáticamente
            cache.guardarEspacios(todosLosEspaciosRemotos)
            cache.actualizarTimestamp() // Asegúrate de tener este método en tu CacheManager

            // Filtramos en memoria lo que el ViewModel necesita y emitimos
            val remotosFiltrados = filtrarEspacios(todosLosEspaciosRemotos, piso, tipo)
            emit(remotosFiltrados)

        } catch (e: Exception) {
            // Si la red falla y no había nada en caché, emitimos una lista vacía
            if (cachedFiltrados.isEmpty()) {
                emit(emptyList())
            }
        }
    }

    // =================================================================================
    // NUEVO: Método directo para que ejecute el SyncWorker en segundo plano
    // =================================================================================
    suspend fun refrescarEspacios() {
        withContext(Dispatchers.IO) {
            val todosLosEspaciosRemotos = api.getEspacios(piso = null, tipo = null)
            cache.guardarEspacios(todosLosEspaciosRemotos)
            cache.actualizarTimestamp()
        }
    }

    // 1. OBTENER TODOS LOS ESPACIOS (Legacy / Operaciones puntuales)
    suspend fun obtenerEspacios(piso: Int? = null, tipo: String? = null): List<Espacio> {
        return try {
            val todosLosEspacios = api.getEspacios(piso = null, tipo = null)
            cache.guardarEspacios(todosLosEspacios)
            cache.actualizarTimestamp() // También mantenemos al día el tiempo aquí
            filtrarEspacios(todosLosEspacios, piso, tipo)
        } catch (e: Exception) {
            obtenerEspaciosOffline(piso, tipo)
        }
    }

    // 2. OBTENER UN ESPACIO POR ID
    suspend fun obtenerEspacio(id: Int): Espacio {
        return try {
            api.getEspacio(id)
        } catch (e: Exception) {
            cache.cargarEspacios().find { it.id == id }
                ?: throw Exception("Espacio no encontrado en la caché local (Modo Offline)")
        }
    }

    // 3. BUSCAR ESPACIOS POR TEXTO (Query)
    suspend fun buscarEspacios(query: String): List<Espacio> {
        return try {
            api.getEspacios(q = query)
        } catch (e: Exception) {
            cache.cargarEspacios().filter {
                it.nombre.contains(query, ignoreCase = true) ||
                        it.tipo.contains(query, ignoreCase = true)
            }
        }
    }

    // 4. AGREGAR UN NUEVO ESPACIO
    suspend fun agregarEspacio(espacio: Espacio): Espacio {
        return try {
            val nuevo = api.crearEspacio(espacio)
            val lista = cache.cargarEspacios().toMutableList()
            lista.add(nuevo)
            cache.guardarEspacios(lista)
            cache.actualizarTimestamp() // Forzar actualización de marca de tiempo al modificar
            nuevo
        } catch (e: Exception) {
            throw Exception("No se pudo agregar el espacio. Verifica tu conexión a internet.")
        }
    }

    // 5. EDITAR UN ESPACIO EXISTENTE
    suspend fun editarEspacio(id: Int, espacio: Espacio): Espacio {
        return try {
            val actualizado = api.actualizarEspacio(id, espacio)
            val lista = cache.cargarEspacios().toMutableList()
            val index = lista.indexOfFirst { it.id == id }
            if (index != -1) {
                lista[index] = actualizado
                cache.guardarEspacios(lista)
                cache.actualizarTimestamp() // Mantenemos consistencia temporal
            }
            actualizado
        } catch (e: Exception) {
            throw Exception("No se pudo editar el espacio. Verifica tu conexión a internet.")
        }
    }

    // 6. OBTENER ESPACIOS OFFLINE
    suspend fun obtenerEspaciosOffline(piso: Int? = null, tipo: String? = null): List<Espacio> {
        return filtrarEspacios(cache.cargarEspacios(), piso, tipo)
    }

    // 7. BORRAR UN ESPACIO
    suspend fun borrarEspacio(id: Int) {
        try {
            api.eliminarEspacio(id)
            val lista = cache.cargarEspacios().toMutableList()
            lista.removeAll { it.id == id }
            cache.guardarEspacios(lista)
            cache.actualizarTimestamp()
        } catch (e: Exception) {
            throw Exception("No se pudo eliminar el espacio. Verifica tu conexión a internet.")
        }
    }

    // =================================================================================
    // FUNCIÓN AUXILIAR (Para evitar duplicar la lógica de filtrado)
    // =================================================================================
    private fun filtrarEspacios(lista: List<Espacio>, piso: Int?, tipo: String?): List<Espacio> {
        return lista.filter {
            (piso == null || it.piso == piso) &&
                    (tipo == null || it.tipo.equals(tipo, ignoreCase = true))
        }
    }
}