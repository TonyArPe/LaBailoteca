package com.example.bailotecaapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bailotecaapp.model.Evento
import com.example.bailotecaapp.model.enums.EstadoEvento
import com.example.bailotecaapp.network.ApiService
import com.example.bailotecaapp.network.session.SesionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventoViewModel @Inject constructor(
    private val api: ApiService,
    private val sesionManager: SesionManager
) : ViewModel() {

    private val _eventos = MutableStateFlow<List<Evento>>(emptyList())
    val eventos: StateFlow<List<Evento>> = _eventos

    private val _eventoSeleccionado = MutableStateFlow<Evento?>(null)
    val eventoSeleccionado: StateFlow<Evento?> = _eventoSeleccionado

    private val _asistencias = MutableStateFlow<Set<Long>>(emptySet())
    val asistencias: StateFlow<Set<Long>> = _asistencias

    fun seleccionarEvento(id: Long) {
        val evento = _eventos.value.find { it.id == id }
        _eventoSeleccionado.value = evento
    }

    fun limpiarEventoSeleccionado() {
        _eventoSeleccionado.value = null
    }

    /**
     * Obtiene todos los eventos públicos visibles por invitados.
     */
    fun obtenerEventosPublicos() {
        viewModelScope.launch {
            try {
                val resultado = api.obtenerEventos()
                _eventos.value = resultado
                Log.i("EventoViewModel", "✅ Eventos públicos cargados correctamente (${resultado.size})")
            } catch (e: Exception) {
                Log.e("EventoViewModel", "❌ Error cargando eventos públicos", e)
            }
        }
    }

    /**
     * Obtiene los eventos visibles para un usuario autenticado.
     * Los administradores y profesores pueden ver todos los eventos.
     * Los usuarios comunes solo pueden ver los eventos públicos o aquellos a los que están asociados.
     */
    fun obtenerEventosPrivados(token: String) {
        viewModelScope.launch {
            try {
                val res = api.getEventosPrivados("Bearer $token")
                if (res.isSuccessful) {
                    val lista = res.body() ?: emptyList()
                    _eventos.value = lista
                    Log.i("EventoViewModel", "✅ Eventos privados cargados: ${lista.size}")
                    cargarAsistencias(token)
                } else {
                    Log.w("EventoViewModel", "⚠️ Eventos no disponibles: código ${res.code()}")
                }
            } catch (e: Exception) {
                Log.e("EventoViewModel", "❌ Error cargando eventos privados", e)
            }
        }
    }

    /**
     * Lógica para crear un evento solo si el usuario es ADMIN o PROFESOR.
     */
    fun crearEvento(token: String, evento: Evento): Boolean {
        viewModelScope.launch {
            try {
                val res = api.crearEvento("Bearer $token", evento)
                val exito = res.isSuccessful
                if (exito) {
                    Log.i("EventoViewModel", "✅ Evento creado: ${evento.nombre}")
                } else {
                    Log.e("EventoViewModel", "❌ Error al crear evento")
                }
            } catch (e: Exception) {
                Log.e("EventoViewModel", "❌ Error al crear evento", e)
            }
        }
        return true
    }

    /**
     * Lógica para editar un evento solo si el usuario es ADMIN o el organizador del evento.
     */
    suspend fun actualizarEvento(token: String, evento: Evento): Boolean {
        return try {
            val res = api.actualizarEvento("Bearer $token", evento.id, evento)
            val exito = res.isSuccessful
            Log.i("EventoViewModel", "🛠️ Evento actualizado: ${evento.nombre}")
            exito
        } catch (e: Exception) {
            Log.e("EventoViewModel", "❌ Error al actualizar evento", e)
            false
        }
    }

    /**
     * Elimina un evento solo si el usuario es ADMIN o el organizador del evento.
     */
    suspend fun eliminarEvento(token: String, eventoId: Long): Boolean {
        return try {
            val res = api.eliminarEvento("Bearer $token", eventoId)
            val exito = res.isSuccessful
            Log.i("EventoViewModel", "🗑️ Evento eliminado: ID $eventoId")
            exito
        } catch (e: Exception) {
            Log.e("EventoViewModel", "❌ Error al eliminar evento", e)
            false
        }
    }

    /**
     * Carga las asistencias para un usuario autenticado.
     */
    private fun cargarAsistencias(token: String) {
        viewModelScope.launch {
            try {
                val lista = api.getEventosAsistidos("Bearer $token")
                _asistencias.value = lista.body()?.map { it.id }?.toSet() ?: emptySet()
                Log.i("EventoViewModel", "📌 Asistencias cargadas (${_asistencias.value.size} eventos)")
            } catch (e: Exception) {
                Log.e("EventoViewModel", "❌ Error cargando asistencias", e)
            }
        }
    }

    /**
     * Verifica si el usuario ya está apuntado a un evento.
     *
     * @param eventoId ID del evento.
     * @return true si ya está inscrito, false en caso contrario.
     */
    fun yaAsiste(eventoId: Long): Boolean {
        return _asistencias.value.contains(eventoId)
    }
}