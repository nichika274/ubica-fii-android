package com.example.ubicafii.ui.theme.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ubicafii.data.model.Espacio
import com.example.ubicafii.data.repository.EspacioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val repository = EspacioRepository()

    private val _espacios = MutableStateFlow<List<Espacio>>(emptyList())
    val espacios: StateFlow<List<Espacio>> = _espacios

    fun cargarEspacios(piso: Int? = null, tipo: String? = null) {
        viewModelScope.launch {
            try {
                val lista = repository.obtenerEspacios(piso, tipo)
                _espacios.value = lista
            } catch (e: Exception) {
                _espacios.value = emptyList()
            }
        }
    }
}