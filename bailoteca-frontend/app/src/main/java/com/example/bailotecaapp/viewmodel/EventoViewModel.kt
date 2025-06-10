package com.example.bailotecaapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bailotecaapp.model.Evento
import com.example.bailotecaapp.model.dto.AsistenciaEventoRequest
import com.example.bailotecaapp.model.dto.EventoRequest
import com.example.bailotecaapp.network.ApiService
import com.example.bailotecaapp.network.session.SesionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel encargado de gestionar la lógica de presentación de eventos en la app.
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

    /**
     * Carga eventos públicos visibles para cualquier usuario.
     */
    fun obtenerEventosPublicos() {
        viewModelScope.launch {
            try {
                val resultado = api.obtenerEventos()
                _eventos.value = resultado
                Log.d("EventoViewModel", "🎯 Eventos públicos cargados: ${resultado.size}")
            } catch (e: Exception) {
                Log.e("EventoViewModel", "❌ Error al cargar eventos públicos", e)
            }
        }
    }

    /**
     * Carga eventos accesibles para usuarios autenticados (ADMIN o PROFESOR).
     */
    fun obtenerEventosPrivados(token: String) {
        viewModelScope.launch {
            try {
                val res = api.getEventosPrivados("Bearer $token")
                if (res.isSuccessful) {
                    _eventos.value = res.body() ?: emptyList()
                    Log.d("EventoViewModel", "✅ Eventos privados cargados")
                } else {
                    Log.w("EventoViewModel", "⚠️ Error al cargar eventos privados: ${res.code()}")
                }
            } catch (e: Exception) {
                Log.e("EventoViewModel", "❌ Excepción al cargar eventos privados", e)
            }
        }
    }

    /**
     * Selecciona un evento de la lista para su detalle o edición.
     */
    fun seleccionarEvento(evento: Evento) {
        _eventoSeleccionado.value = evento
        Log.d("EventoViewModel", "📌 Evento seleccionado: ${evento.nombre}")
    }

    /**
     * Limpia la selección del evento actual.
     */
    fun limpiarEventoSeleccionado() {
        _eventoSeleccionado.value = null
        Log.d("EventoViewModel", "🧹 Evento deseleccionado")
    }

    /**
     * Crea un nuevo evento en el sistema a partir de los datos introducidos por el usuario.
     * @param token Token JWT del usuario.
     * @param request Objeto DTO con los datos del nuevo evento.
     * @return true si se crea correctamente, false si hay error.
     */
    suspend fun crearEvento(token: String, request: EventoRequest): Boolean {
        return try {
            Log.d("EventoViewModel", "🛠️ Creando evento con nombre: ${request.nombre}")
            val res = api.crearEvento("Bearer $token", request)
            res.isSuccessful
        } catch (e: Exception) {
            Log.e("EventoViewModel", "❌ Error al crear evento", e)
            false
        }
    }

    /**
     * Actualiza un evento existente en el sistema.
     * @param token Token JWT de autenticación.
     * @param evento Evento con datos actualizados (incluye el ID).
     * @return true si se actualiza correctamente, false si hay error.
     */
    suspend fun actualizarEvento(token: String, evento: Evento): Boolean {
        return try {
            Log.d("EventoViewModel", "✏️ Actualizando evento con ID: ${evento.id}")
            val res = api.actualizarEvento("Bearer $token", evento.id, evento)
            res.isSuccessful
        } catch (e: Exception) {
            Log.e("EventoViewModel", "❌ Error al actualizar evento", e)
            false
        }
    }

    /**
     * Elimina un evento específico.
     * @param token Token JWT.
     * @param eventoId ID del evento a eliminar.
     * @return true si se elimina correctamente.
     */
    suspend fun eliminarEvento(token: String, eventoId: Long): Boolean {
        return try {
            Log.d("EventoViewModel", "🗑️ Eliminando evento con ID: $eventoId")
            val res = api.eliminarEvento("Bearer $token", eventoId)
            res.isSuccessful
        } catch (e: Exception) {
            Log.e("EventoViewModel", "❌ Error al eliminar evento", e)
            false
        }
    }

    /**
     * Registra asistencia del usuario autenticado a un evento.
     */
    fun registrarAsistencia(eventoId: Long, asistira: Boolean, pagado: Boolean) {
        viewModelScope.launch {
            try {
                val token = sesionManager.getToken()
                if (token == null) {
                    Log.e("EventoViewModel", "⚠️ Token no disponible")
                    return@launch
                }

                val request = AsistenciaEventoRequest(asistira, pagado)
                val response = api.registrarAsistenciaEvento("Bearer $token", eventoId, request)

                if (response.isSuccessful) {
                    Log.d("EventoViewModel", "✅ Asistencia registrada: ${response.body()}")
                } else {
                    Log.e("EventoViewModel", "❌ Error HTTP al registrar asistencia: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("EventoViewModel", "❌ Excepción al registrar asistencia", e)
            }
        }
    }
}