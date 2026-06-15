package com.example.ubicafii.data.repository

import android.content.Context
import com.example.ubicafii.data.api.RetrofitClient
import com.example.ubicafii.data.local.CacheManager
import com.example.ubicafii.data.model.Espacio

class EspacioRepository(private val context: Context) {

    private val api = RetrofitClient.instance
    private val cache = CacheManager(context)

    // 1. OBTENER TODOS LOS ESPACIOS (Optimizando el guardado de Caché)
    suspend fun obtenerEspacios(piso: Int? = null, tipo: String? = null): List<Espacio> {
        return try {
            // Traemos siempre TODOS los espacios para mantener la caché local completa y sana
            val todosLosEspacios = api.getEspacios(piso = null, tipo = null)
            cache.guardarEspacios(todosLosEspacios)

            // Filtramos en memoria para devolverle al ViewModel lo que pidió
            todosLosEspacios.filter {
                (piso == null || it.piso == piso) &&
                        (tipo == null || it.tipo.equals(tipo, ignoreCase = true))
            }
        } catch (e: Exception) {
            // Si falla la red, recurrimos al método offline de forma segura
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
            }
            actualizado
        } catch (e: Exception) {
            throw Exception("No se pudo editar el espacio. Verifica tu conexión a internet.")
        }
    }

    // 6. OBTENER ESPACIOS OFFLINE (Aseguramos consistencia con ignoreCase)
    suspend fun obtenerEspaciosOffline(piso: Int? = null, tipo: String? = null): List<Espacio> {
        val todos = cache.cargarEspacios()
        return todos.filter {
            (piso == null || it.piso == piso) &&
                    (tipo == null || it.tipo.equals(tipo, ignoreCase = true))
        }
    }

    // 7. BORRAR UN ESPACIO
    suspend fun borrarEspacio(id: Int) {
        try {
            api.eliminarEspacio(id)
            val lista = cache.cargarEspacios().toMutableList()
            lista.removeAll { it.id == id }
            cache.guardarEspacios(lista)
        } catch (e: Exception) {
            throw Exception("No se pudo eliminar el espacio. Verifica tu conexión a internet.")
        }
    }
}