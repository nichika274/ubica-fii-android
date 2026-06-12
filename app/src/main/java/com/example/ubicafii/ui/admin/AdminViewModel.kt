package com.example.ubicafii.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ubicafii.data.model.Espacio
import com.example.ubicafii.data.repository.EspacioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {
    private val repository = EspacioRepository()
    private val _espacios = MutableStateFlow<List<Espacio>>(emptyList())
    val espacios: StateFlow<List<Espacio>> = _espacios
    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _cargando.value = true
            try { _espacios.value = repository.obtenerEspacios() }
            catch (_: Exception) {}
            finally { _cargando.value = false }
        }
    }

    fun agregar(espacio: Espacio) {
        viewModelScope.launch {
            repository.agregarEspacio(espacio)
            cargar()
        }
    }

    fun actualizar(id: Int, espacio: Espacio) {
        viewModelScope.launch {
            repository.editarEspacio(id, espacio)
            cargar()
        }
    }

    fun eliminar(id: Int) {
        viewModelScope.launch {
            repository.borrarEspacio(id)
            cargar()
        }
    }
}