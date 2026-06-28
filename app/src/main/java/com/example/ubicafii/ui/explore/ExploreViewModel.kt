package com.example.ubicafii.ui.explore

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ubicafii.data.model.Espacio
import com.example.ubicafii.data.repository.EspacioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExploreViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = EspacioRepository(application)

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _filteredSpaces = MutableStateFlow<List<Espacio>>(emptyList())
    val filteredSpaces: StateFlow<List<Espacio>> = _filteredSpaces.asStateFlow()

    fun selectCategory(categoria: String) {
        _selectedCategory.value = categoria
        viewModelScope.launch {
            // Llamada real al repositorio filtrando por tipo
            _filteredSpaces.value = repository.obtenerEspacios(tipo = categoria)
        }
    }
}