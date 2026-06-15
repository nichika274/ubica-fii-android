package com.example.ubicafii.ui.theme.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ubicafii.data.model.Espacio
import com.example.ubicafii.data.repository.EspacioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = EspacioRepository(application)

    private val _espacios = MutableStateFlow<List<Espacio>>(emptyList())
    val espacios: StateFlow<List<Espacio>> = _espacios

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        cargarEspacios()
    }

    fun cargarEspacios(piso: Int? = null, tipo: String? = null) {
        viewModelScope.launch {
            _cargando.value = true

            try {
                // 1. Intentamos traer los datos del repositorio
                val resultado = repository.obtenerEspacios(piso, tipo)
                _espacios.value = resultado

                if (resultado.isEmpty()) {
                    _error.value = "📴 Modo offline - No hay datos guardados localmente todavía."
                } else {

                    _error.value = null // <-- Esto hace que el cartel desaparezca mágicamente
                }
            } catch (e: Exception) {
                // 3. Si Retrofit o la red fallan drásticamente, entramos aquí:
                _espacios.value = repository.obtenerEspaciosOffline(piso, tipo)

                if (_espacios.value.isEmpty()) {
                    _error.value = "📴 Sin conexión - No hay caché disponible."
                } else {
                    // Solo si está verdaderamente offline mostramos el cartel
                    _error.value = "📴 Modo offline - Mostrando datos locales"
                }
            } finally {
                _cargando.value = false
            }
        }
    }

    fun limpiarError() {
        _error.value = null
    }
}