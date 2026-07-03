package com.example.ubicafii.ui.explore

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ubicafii.data.model.Espacio
import com.example.ubicafii.data.repository.EspacioRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class ExploreViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = EspacioRepository(application)

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _filteredSpaces = MutableStateFlow<List<Espacio>>(emptyList())
    val filteredSpaces: StateFlow<List<Espacio>> = _filteredSpaces.asStateFlow()

    // Estados para controlar la carga y sincronización en la UI
    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private val _actualizando = MutableStateFlow(false)
    val actualizando: StateFlow<Boolean> = _actualizando.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Job para cancelar la recolección anterior si el usuario salta rápido entre categorías
    private var categoryJob: Job? = null

    fun selectCategory(categoria: String) {
        _selectedCategory.value = categoria

        // Cancelamos la recolección de la categoría anterior si aún estaba procesándose
        categoryJob?.cancel()

        categoryJob = viewModelScope.launch {
            repository.obtenerEspaciosFlow(tipo = categoria)
                .onStart {
                    _cargando.value = true
                    _error.value = null
                }
                .catch { _ ->
                    _cargando.value = false
                    _actualizando.value = false
                    if (_filteredSpaces.value.isEmpty()) {
                        _error.value = "❌ Error al cargar los espacios de esta categoría."
                    }
                }
                .collect { listaFiltrada ->
                    _filteredSpaces.value = listaFiltrada

                    // Control de loaders (Caché vs Red)
                    if (_cargando.value) {
                        _cargando.value = false
                        // Si la caché ya tenía elementos, avisamos que se está validando con el servidor
                        if (listaFiltrada.isNotEmpty()) {
                            _actualizando.value = true
                        }
                    } else {
                        // Llegó la respuesta fresca del backend
                        _actualizando.value = false
                    }

                    // Si al final del flujo no hay nada ni en caché ni en red
                    if (_filteredSpaces.value.isEmpty()) {
                        _actualizando.value = false
                        _error.value = "📴 No se encontraron espacios para la categoría: $categoria"
                    }
                }
        }
    }

    fun limpiarError() {
        _error.value = null
    }
}