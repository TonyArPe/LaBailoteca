package com.example.bailotecaapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bailotecaapp.model.Evento
import com.example.bailotecaapp.network.ApiService
import com.example.bailotecaapp.network.session.SesionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel encargado de gestionar toda la lógica de eventos:
 * - Listado de eventos públicos/privados.
 * - Selección y edición de eventos.
 * - Asistencia a eventos.
 */
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
     * Si la respuesta es válida, además carga las asistencias del usuario.
     *
     * @param token JWT del usuario autenticado.
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
     * Carga los eventos a los que el usuario ya está apuntado como asistente.
     *
     * @param token JWT del usuario autenticado.
     */
    private fun cargarAsistencias(token: String) {
        viewModelScope.launch {
            try {
                val lista = api.getEventosAsistidos("Bearer $token")
                _asistencias.value = lista.map { it.id }.toSet()
                Log.i("EventoViewModel", "📌 Asistencias cargadas (${_asistencias.value.size} eventos)")
            } catch (e: Exception) {
                Log.e("EventoViewModel", "❌ Error cargando asistencias", e)
            }
        }
    }

    /**
     * Marca asistencia del usuario a un evento determinado.
     *
     * @param eventoId ID del evento.
     * @param token JWT del usuario autenticado.
     */
    fun asistirEvento(eventoId: Long, token: String) {
        viewModelScope.launch {
            try {
                api.asistirEvento("Bearer $token", eventoId)
                _asistencias.value = _asistencias.value + eventoId
                Log.i("EventoViewModel", "✅ Asistencia registrada al evento $eventoId")
            } catch (e: Exception) {
                Log.e("EventoViewModel", "❌ Error al asistir a evento $eventoId", e)
            }
        }
    }

    /**
     * Cancela la asistencia del usuario al evento.
     *
     * @param eventoId ID del evento.
     * @param token JWT del usuario autenticado.
     */
    fun cancelarAsistencia(eventoId: Long, token: String) {
        viewModelScope.launch {
            try {
                api.cancelarAsistencia("Bearer $token", eventoId)
                _asistencias.value = _asistencias.value - eventoId
                Log.i("EventoViewModel", "🟡 Asistencia cancelada para evento $eventoId")
            } catch (e: Exception) {
                Log.e("EventoViewModel", "❌ Error al cancelar asistencia", e)
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

    /**
     * Marca un evento como seleccionado para edición.
     */
    fun seleccionarEvento(evento: Evento) {
        _eventoSeleccionado.value = evento
        Log.d("EventoViewModel", "📝 Evento seleccionado: ${evento.nombre}")
    }

    /**
     * Limpia el evento seleccionado.
     */
    fun limpiarEventoSeleccionado() {
        _eventoSeleccionado.value = null
        Log.d("EventoViewModel", "🧹 Evento seleccionado limpiado")
    }

    /**
     * Crea un nuevo evento en el servidor.
     *
     * @param token JWT del usuario autenticado.
     * @param evento Evento a crear.
     * @return true si fue exitoso, false si falló.
     */
    suspend fun crearEvento(token: String, evento: Evento): Boolean {
        return try {
            val res = api.crearEvento("Bearer $token", evento)
            val exito = res.isSuccessful
            Log.i("EventoViewModel", "✅ Evento creado: ${evento.nombre}")
            exito
        } catch (e: Exception) {
            Log.e("EventoViewModel", "❌ Error al crear evento", e)
            false
        }
    }

    /**
     * Actualiza un evento existente en el servidor.
     *
     * @param token JWT del usuario autenticado.
     * @param evento Evento actualizado.
     * @return true si fue exitoso, false si falló.
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
     * Elimina un evento existente.
     *
     * @param token JWT del usuario autenticado.
     * @param eventoId ID del evento a eliminar.
     * @return true si fue exitoso, false si falló.
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
}