package com.example.ubicafii.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ubicafii.data.model.Espacio
import com.example.ubicafii.data.repository.EspacioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DetailViewModel : ViewModel() {
    private val repository = EspacioRepository()
    private val _espacio = MutableStateFlow<Espacio?>(null)
    val espacio: StateFlow<Espacio?> = _espacio
    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    fun cargarEspacio(id: Int) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                _espacio.value = repository.obtenerEspacio(id)
            } catch (e: Exception) {
                _espacio.value = null
            } finally {
                _cargando.value = false
            }
        }
    }
}