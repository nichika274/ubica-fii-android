package com.example.ubicafii.ui.floors

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

class FloorsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = EspacioRepository(application)

    private val _espacios = MutableStateFlow<List<Espacio>>(emptyList())
    val espacios: StateFlow<List<Espacio>> = _espacios

    private val _pisos = MutableStateFlow<List<String>>(emptyList())
    val pisos: StateFlow<List<String>> = _pisos

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    // NUEVO: Para mantener la consistencia visual de actualización en segundo plano
    private val _actualizando = MutableStateFlow(false)
    val actualizando: StateFlow<Boolean> = _actualizando

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var loadJob: Job? = null

    fun cargarBloque(bloqueId: String) {
        // Cancelamos cualquier petición previa en caso de re-entrada rápida
        loadJob?.cancel()

        loadJob = viewModelScope.launch {
            // Llamamos a obtenerEspaciosFlow sin filtros de piso/tipo para asegurar la caché total,
            // pero filtramos por bloque directamente en la recolección.
            repository.obtenerEspaciosFlow()
                .onStart {
                    _cargando.value = true
                    _error.value = null
                }
                .catch { _ ->
                    _cargando.value = false
                    _actualizando.value = false
                    if (_espacios.value.isEmpty()) {
                        _error.value = "❌ Error al cargar los pisos del bloque."
                    }
                }
                .collect { todosLosEspacios ->
                    // 1. Filtrar por el bloque solicitado (Ignorando mayúsculas/minúsculas)
                    val filtradosPorBloque = todosLosEspacios.filter {
                        it.bloque.equals(bloqueId, ignoreCase = true)
                    }

                    // 2. Actualizar estados de UI
                    _espacios.value = filtradosPorBloque
                    _pisos.value = filtradosPorBloque.map { it.piso.toString() }.distinct().sorted()

                    // 3. Control de los estados de carga distribuidos (Caché vs Servidor)
                    if (_cargando.value) {
                        _cargando.value = false
                        // Si la caché tenía datos de este bloque, avisamos que buscamos actualización remota
                        if (filtradosPorBloque.isNotEmpty()) {
                            _actualizando.value = true
                        }
                    } else {
                        // Segunda emisión exitosa (Remota)
                        _actualizando.value = false
                    }

                    // 4. Si después de procesar todo sigue vacío, alertamos al usuario
                    if (_espacios.value.isEmpty()) {
                        _actualizando.value = false
                        _error.value = "📴 Sin conexión - No hay datos locales para este bloque."
                    }
                }
        }
    }

    fun limpiarError() {
        _error.value = null
    }
}