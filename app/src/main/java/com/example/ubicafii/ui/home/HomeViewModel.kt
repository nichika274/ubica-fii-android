package com.example.ubicafii.ui.theme.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ubicafii.data.model.Espacio
import com.example.ubicafii.data.repository.EspacioRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = EspacioRepository(application)

    private val _espacios = MutableStateFlow<List<Espacio>>(emptyList())
    val espacios: StateFlow<List<Espacio>> = _espacios

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    // NUEVO: Indica si se está sincronizando con el servidor en segundo plano
    private val _actualizando = MutableStateFlow(false)
    val actualizando: StateFlow<Boolean> = _actualizando

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Para evitar que múltiples llamadas rápidas dupliquen recolecciones concurrentes
    private var searchJob: Job? = null

    init {
        cargarEspacios()
    }

    fun cargarEspacios(piso: Int? = null, tipo: String? = null) {
        // Cancelamos el trabajo anterior si el usuario cambia de filtros rápidamente
        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            repository.obtenerEspaciosFlow(piso, tipo)
                .onStart {
                    // Al iniciar el flujo limpiamos errores y encendemos el loader principal
                    _cargando.value = true
                    _error.value = null
                }
                .catch { _ ->
                    // Captura errores inesperados del flujo
                    _cargando.value = false
                    _actualizando.value = false
                    if (_espacios.value.isEmpty()) {
                        _error.value = "❌ Ocurrió un error al cargar los datos."
                    }
                }
                .collect { lista ->
                    // Actualizamos la lista con la emisión que llegue (caché o remoto)
                    _espacios.value = lista

                    if (_cargando.value) {
                        // Si es la PRIMERA emisión (normalmente la caché)
                        _cargando.value = false
                        // Si hay datos locales, asumimos que se inicia la búsqueda remota silenciosa
                        if (lista.isNotEmpty()) {
                            _actualizando.value = true
                        }
                    } else {
                        // Si es la SEGUNDA emisión (datos frescos de red exitosos)
                        _actualizando.value = false
                        _error.value = null
                    }

                    // Validación de estados vacíos al terminar de procesar la emisión actual
                    evaluarEstadoVacio()
                }
        }
    }

    private fun evaluarEstadoVacio() {
        if (_espacios.value.isEmpty()) {
            _actualizando.value = false
            _error.value = "📴 Sin conexión - No hay datos guardados localmente todavía."
        }
    }

    fun limpiarError() {
        _error.value = null
    }
}