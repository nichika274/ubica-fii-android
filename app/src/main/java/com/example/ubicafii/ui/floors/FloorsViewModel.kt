package com.example.ubicafii.ui.floors

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ubicafii.data.model.Espacio
import com.example.ubicafii.data.repository.EspacioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Cambiado a AndroidViewModel para recibir la aplicación
class FloorsViewModel(application: Application) : AndroidViewModel(application) {

    // Pasamos el contexto al repositorio
    private val repository = EspacioRepository(application)

    private val _espacios = MutableStateFlow<List<Espacio>>(emptyList())
    val espacios: StateFlow<List<Espacio>> = _espacios

    private val _pisos = MutableStateFlow<List<String>>(emptyList())
    val pisos: StateFlow<List<String>> = _pisos

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    fun cargarBloque(bloqueId: String) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                // Intenta traer todos los datos frescos del servidor
                val todos = repository.obtenerEspacios()
                val filtrados = todos.filter { it.bloque.equals(bloqueId, ignoreCase = true) }
                _espacios.value = filtrados
                _pisos.value = filtrados.map { it.piso.toString() }.distinct().sorted()
            } catch (e: Exception) {
                // FALLBACK OFFLINE: Si falla la red, lee la caché JSON localmente
                try {
                    val todosOffline = repository.obtenerEspaciosOffline()
                    val filtradosOffline = todosOffline.filter { it.bloque.equals(bloqueId, ignoreCase = true) }
                    _espacios.value = filtradosOffline
                    _pisos.value = filtradosOffline.map { it.piso.toString() }.distinct().sorted()
                } catch (offlineError: Exception) {
                    _espacios.value = emptyList()
                    _pisos.value = emptyList()
                }
            } finally {
                _cargando.value = false
            }
        }
    }
}