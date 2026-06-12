package com.example.ubicafii.ui.floors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ubicafii.data.model.Espacio
import com.example.ubicafii.data.repository.EspacioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FloorsViewModel : ViewModel() {
    private val repository = EspacioRepository()

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
                val todos = repository.obtenerEspacios()
                val filtrados = todos.filter { it.bloque.equals(bloqueId, ignoreCase = true) }
                _espacios.value = filtrados
                // Extraer pisos únicos ordenados
                _pisos.value = filtrados.map { it.piso.toString() }.distinct().sorted()
            } catch (e: Exception) {
                _espacios.value = emptyList()
                _pisos.value = emptyList()
            } finally {
                _cargando.value = false
            }
        }
    }
}