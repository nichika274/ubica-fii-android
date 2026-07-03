package com.example.ubicafii.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ubicafii.data.model.Espacio
import com.example.ubicafii.data.repository.EspacioRepository
import com.example.ubicafii.util.PreferencesManager
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = EspacioRepository(application)

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _resultados = MutableStateFlow<List<Espacio>>(emptyList())
    val resultados: StateFlow<List<Espacio>> = _resultados.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        // Configuramos la tubería reactiva de búsqueda
        observarQuery()
    }

    fun actualizarQuery(texto: String) {
        _query.value = texto
    }

    fun guardarBusqueda(query: String) {
        viewModelScope.launch {
            PreferencesManager.addSearchRecent(getApplication(), query)
        }
    }

    @OptIn(FlowPreview::class)
    private fun observarQuery() {
        viewModelScope.launch {
            _query
                .debounce(300)
                .distinctUntilChanged()
                .flatMapLatest { texto ->
                    // Si el texto está vacío, evitamos ir a red/caché y devolvemos flujo vacío
                    if (texto.isBlank()) {
                        _cargando.value = false
                        _error.value = null
                        flowOf(emptyList())
                    } else {
                        ejecutarBusquedaFlow(texto)
                    }
                }
                .collect { listaResultados ->
                    _resultados.value = listaResultados
                    _cargando.value = false
                }
        }
    }

    /**
     * Construye un flujo de búsqueda híbrido (Red -> Fallback Caché) para el término actual.
     */
    private fun ejecutarBusquedaFlow(texto: String): Flow<List<Espacio>> = flow {
        _cargando.value = true
        _error.value = null

        try {
            // 1. Intenta buscar directamente en tu servidor Node.js
            val deServidor = repository.buscarEspacios(texto)
            emit(deServidor)

            if (deServidor.isNotEmpty()) {
                guardarBusqueda(texto)
            }
        } catch (e: Exception) {
            // 2. Fallback local si falla internet o ngrok
            val locales = repository.obtenerEspaciosOffline()
            val filtradosLocales = locales.filter {
                it.nombre.contains(texto, ignoreCase = true) ||
                        it.tipo.contains(texto, ignoreCase = true)
            }

            emit(filtradosLocales)
            _error.value = "📴 Buscando en datos locales (Offline)"

            if (filtradosLocales.isNotEmpty()) {
                guardarBusqueda(texto)
            }
        }
    }

    fun limpiarError() {
        _error.value = null
    }
}