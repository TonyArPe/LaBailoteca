package com.example.bailotecaapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bailotecaapp.model.Evento
import com.example.bailotecaapp.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel que gestiona la lógica de eventos (ver, crear, asistir, etc.).
 */
@HiltViewModel
class EventoViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {

    private val _eventos = MutableStateFlow<List<Evento>>(emptyList())
    val eventos: StateFlow<List<Evento>> = _eventos

    private val _eventoSeleccionado = MutableStateFlow<Evento?>(null)
    val eventoSeleccionado: StateFlow<Evento?> = _eventoSeleccionado

    private val _asistencias = MutableStateFlow<Set<Long>>(emptySet())
    val asistencias: StateFlow<Set<Long>> = _asistencias

    /**
     * Carga los eventos públicos (para invitados).
     */
    fun obtenerEventosPublicos() {
        viewModelScope.launch {
            try {
                val resultado = api.obtenerEventos()
                _eventos.value = resultado
            } catch (e: Exception) {
                Log.e("EventoViewModel", "Error cargando eventos públicos", e)
            }
        }
    }

    /**
     * Carga eventos privados para usuarios autenticados.
     */
    fun obtenerEventosPrivados(token: String) {
        viewModelScope.launch {
            try {
                val res = api.getEventosPrivados("Bearer $token")
                if (res.isSuccessful) {
                    val lista = res.body() ?: emptyList()
                    _eventos.value = lista
                    cargarAsistencias(token)
                } else {
                    Log.w("EventoViewModel", "Eventos no disponibles: ${res.code()}")
                }
            } catch (e: Exception) {
                Log.e("EventoViewModel", "Error cargando eventos privados", e)
            }
        }
    }

    /**
     * Carga los IDs de eventos a los que el usuario ya asiste.
     */
    private fun cargarAsistencias(token: String) {
        viewModelScope.launch {
            try {
                val lista = api.getEventosAsistidos("Bearer $token")
                _asistencias.value = lista.map { it.id }.toSet()
            } catch (e: Exception) {
                Log.e("EventoViewModel", "Error cargando asistencias", e)
            }
        }
    }

    /**
     * Marca asistencia a un evento.
     */
    fun asistirEvento(eventoId: Long, token: String) {
        viewModelScope.launch {
            try {
                api.asistirEvento("Bearer $token", eventoId)
                _asistencias.value = _asistencias.value + eventoId
                Log.i("EventoViewModel", "✅ Asistencia marcada a evento $eventoId")
            } catch (e: Exception) {
                Log.e("EventoViewModel", "Error al asistir a evento $eventoId", e)
            }
        }
    }

    /**
     * Cancela asistencia a un evento.
     */
    fun cancelarAsistencia(eventoId: Long, token: String) {
        viewModelScope.launch {
            try {
                api.cancelarAsistencia("Bearer $token", eventoId)
                _asistencias.value = _asistencias.value - eventoId
                Log.i("EventoViewModel", "🟡 Asistencia cancelada a evento $eventoId")
            } catch (e: Exception) {
                Log.e("EventoViewModel", "Error al cancelar asistencia", e)
            }
        }
    }

    /**
     * Verifica si el usuario ya está apuntado a un evento.
     */
    fun yaAsiste(eventoId: Long): Boolean {
        return _asistencias.value.contains(eventoId)
    }

    fun seleccionarEvento(evento: Evento) {
        _eventoSeleccionado.value = evento
    }

    fun limpiarEventoSeleccionado() {
        _eventoSeleccionado.value = null
    }

    suspend fun crearEvento(token: String, evento: Evento): Boolean {
        return try {
            val res = api.crearEvento("Bearer $token", evento)
            res.isSuccessful
        } catch (e: Exception) {
            Log.e("EventoViewModel", "Error al crear evento", e)
            false
        }
    }

    suspend fun actualizarEvento(token: String, evento: Evento): Boolean {
        return try {
            val res = api.actualizarEvento("Bearer $token", evento.id, evento)
            res.isSuccessful
        } catch (e: Exception) {
            Log.e("EventoViewModel", "Error al actualizar evento", e)
            false
        }
    }

    suspend fun eliminarEvento(token: String, eventoId: Long): Boolean {
        return try {
            val res = api.eliminarEvento("Bearer $token", eventoId)
            res.isSuccessful
        } catch (e: Exception) {
            Log.e("EventoViewModel", "Error al eliminar evento", e)
            false
        }
    }
}