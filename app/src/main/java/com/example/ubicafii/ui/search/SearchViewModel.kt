package com.example.ubicafii.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ubicafii.data.model.Espacio
import com.example.ubicafii.data.repository.EspacioRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    private val repository = EspacioRepository()
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query
    private val _resultados = MutableStateFlow<List<Espacio>>(emptyList())
    val resultados: StateFlow<List<Espacio>> = _resultados
    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    init {
        viewModelScope.launch {
            _query
                .debounce(300)
                .distinctUntilChanged()
                .collect { buscar(it) }
        }
    }

    fun actualizarQuery(texto: String) { _query.value = texto }

    private suspend fun buscar(texto: String) {
        _cargando.value = true
        try {
            _resultados.value = if (texto.isBlank()) emptyList() else repository.buscarEspacios(texto)
        } catch (e: Exception) {
            _resultados.value = emptyList()
        } finally {
            _cargando.value = false
        }
    }
}