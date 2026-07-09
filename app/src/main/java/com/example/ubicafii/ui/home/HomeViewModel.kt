package com.example.ubicafii.ui.theme.home

import android.app.Application
import android.util.Log
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
            // [MODIFICADO]: Evaluamos si ya tenemos datos guardados en memoria
            val tieneDatosPrevios = _espacios.value.isNotEmpty()

            Log.d("HomeViewModel", "cargarEspacios llamado, tieneDatosPrevios: $tieneDatosPrevios, _espacios.size: ${_espacios.value.size}")

            repository.obtenerEspaciosFlow(piso, tipo)
                .onStart {
                    _error.value = null
                    // Solo activamos el esqueleto/loader principal si la app está totalmente vacía
                    if (!tieneDatosPrevios) {
                        _cargando.value = true
                    } else {
                        // Si ya hay datos, mostramos una actualización silenciosa
                        _actualizando.value = true
                    }
                }
                .catch { _ ->
                    _cargando.value = false
                    _actualizando.value = false
                    if (_espacios.value.isEmpty()) {
                        _error.value = "❌ Ocurrió un error al cargar los datos."
                    }
                }
                .collect { lista ->
                    // Actualizamos la lista de inmediato sin vaciar la pantalla
                    _espacios.value = lista

                    if (_cargando.value) {
                        // Si es la PRIMERA emisión absoluta sin datos previos
                        _cargando.value = false
                        if (lista.isNotEmpty()) {
                            _actualizando.value = true
                        }
                    } else {
                        // Si ya terminó de actualizarse por completo desde la red
                        _actualizando.value = false
                        _error.value = null
                    }

                    // Validación inteligente de estados vacíos
                    evaluarEstadoVacio(tieneDatosPrevios)
                }
        }
    }

    private fun evaluarEstadoVacio(tieneDatosPrevios: Boolean) {
        // Solo disparamos el error de "Sin conexión" si realmente no hay nada en memoria
        // Y tampoco se logró traer nada en este intento.
        if (_espacios.value.isEmpty() && !_cargando.value && !_actualizando.value) {
            _error.value = "📴 Sin conexión - No hay datos guardados localmente todavía."
        }
    }
}