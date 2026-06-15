package com.example.ubicafii.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ubicafii.data.model.Espacio
import com.example.ubicafii.data.repository.EspacioRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// 1. Cambiamos a AndroidViewModel y pasamos 'application'
class SearchViewModel(application: Application) : AndroidViewModel(application) {

    // 2. Le pasamos el contexto al repositorio
    private val repository = EspacioRepository(application)

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _resultados = MutableStateFlow<List<Espacio>>(emptyList())
    val resultados: StateFlow<List<Espacio>> = _resultados

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    // Nuevo estado opcional por si quieres mostrar un aviso de "Modo Offline" en la barra de búsqueda
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        @OptIn(FlowPreview::class) // Requerido por Kotlin para usar debounce de forma segura
        viewModelScope.launch {
            _query
                .debounce(300)
                .distinctUntilChanged()
                .collect { buscar(it) }
        }
    }

    fun actualizarQuery(texto: String) {
        _query.value = texto
    }

    private suspend fun buscar(texto: String) {
        _cargando.value = true
        _error.value = null // Limpiamos errores al iniciar una nueva búsqueda

        try {
            if (texto.isBlank()) {
                _resultados.value = emptyList()
            } else {
                // 1. Intenta buscar en tiempo real en tu servidor Node.js
                _resultados.value = repository.buscarEspacios(texto)
            }
        } catch (e: Exception) {
            // 2. Si falla ngrok/Internet, busca de inmediato en el archivo JSON local de la caché
            if (texto.isNotBlank()) {
                val locales = repository.obtenerEspaciosOffline()
                // Filtramos localmente usando el query que ingresó el usuario
                _resultados.value = locales.filter {
                    it.nombre.contains(texto, ignoreCase = true) ||
                            it.tipo.contains(texto, ignoreCase = true)
                }
                _error.value = "📴 Buscando en datos locales (Offline)"
            } else {
                _resultados.value = emptyList()
            }
        } finally {
            _cargando.value = false
        }
    }
}