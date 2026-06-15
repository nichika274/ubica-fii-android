package com.example.ubicafii.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ubicafii.data.model.Espacio
import com.example.ubicafii.data.repository.EspacioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = EspacioRepository(application)

    private val _espacios = MutableStateFlow<List<Espacio>>(emptyList())
    val espacios: StateFlow<List<Espacio>> = _espacios

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje

    init {
        cargarEspacios()
    }

    // Carga los espacios desde el servidor (o caché si el servidor falla)
    fun cargarEspacios() {
        viewModelScope.launch {
            _cargando.value = true
            try {
                _espacios.value = repository.obtenerEspacios()
            } catch (e: Exception) {
                _mensaje.value = "Error al cargar: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    // Agrega un espacio y refresca la lista sincronizada
    fun agregar(espacio: Espacio) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                repository.agregarEspacio(espacio)
                cargarEspacios() // Sincroniza la vista con los datos reales guardados
                _mensaje.value = "Espacio agregado exitosamente"
            } catch (e: Exception) {
                _mensaje.value = "Error al agregar: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    // Actualiza un espacio por ID y refresca la lista sincronizada
    fun actualizar(id: Int, espacio: Espacio) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                repository.editarEspacio(id, espacio)
                cargarEspacios() // Sincroniza la vista con los datos reales guardados
                _mensaje.value = "Espacio actualizado exitosamente"
            } catch (e: Exception) {
                _mensaje.value = "Error al actualizar: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    // Elimina un espacio por ID y refresca la lista sincronizada
    fun eliminar(id: Int) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                repository.borrarEspacio(id)
                cargarEspacios() // Sincroniza la vista con los datos reales guardados
                _mensaje.value = "Espacio eliminado exitosamente"
            } catch (e: Exception) {
                _mensaje.value = "Error al eliminar: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    // Limpia el estado del mensaje para evitar que se repitan los Toasts o Snackbars
    fun limpiarMensaje() {
        _mensaje.value = null
    }
}