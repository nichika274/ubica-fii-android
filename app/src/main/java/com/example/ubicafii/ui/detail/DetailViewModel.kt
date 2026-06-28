package com.example.ubicafii.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ubicafii.data.model.Espacio
import com.example.ubicafii.data.repository.EspacioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Cambiado a AndroidViewModel para recibir la aplicación
class DetailViewModel(application: Application) : AndroidViewModel(application) {

    // Pasamos el contexto al repositorio
    private val repository = EspacioRepository(application)

    private val _espacio = MutableStateFlow<Espacio?>(null)
    val espacio: StateFlow<Espacio?> = _espacio

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    fun cargarEspacio(id: Int) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                println("ID QUE LLEGA AL DETALLE: $id")

                val resultado = repository.obtenerEspacio(id)

                println("ESPACIO RECIBIDO: ${resultado.nombre}")
                println("===== DATOS ANDROID =====")
                println("ID: ${resultado.id}")
                println("NOMBRE: ${resultado.nombre}")
                println("DESC: ${resultado.descripcion}")
                println("FOTO: ${resultado.fotoUrl}")
                println("=========================")

                _espacio.value = resultado

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _cargando.value = false
            }
        }
    }
}