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
 * ViewModel que gestiona los eventos dentro de la aplicación.
 * Controla la carga de eventos públicos, privados y el CRUD completo.
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
     * Carga los eventos públicos disponibles.
     */
    fun obtenerEventosPublicos() {
        viewModelScope.launch {
            try {
                val resultado = api.obtenerEventos()
                _eventos.value = resultado
                Log.d("EventoViewModel", "✅ Eventos públicos cargados: ${resultado.size}")
            } catch (e: Exception) {
                Log.e("EventoViewModel", "❌ Error cargando eventos públicos", e)
            }
        }
    }

    /**
     * Carga los eventos disponibles para un usuario autenticado.
     */
    fun obtenerEventosPrivados(token: String) {
        viewModelScope.launch {
            try {
                val res = api.getEventosPrivados("Bearer $token")
                if (res.isSuccessful) {
                    _eventos.value = res.body() ?: emptyList()
                    Log.d("EventoViewModel", "✅ Eventos privados cargados: ${_eventos.value.size}")
                } else {
                    Log.w("EventoViewModel", "⚠️ Fallo al obtener eventos privados: ${res.code()}")
                }
            } catch (e: Exception) {
                Log.e("EventoViewModel", "❌ Error cargando eventos privados", e)
            }
        }
    }

    fun seleccionarEvento(evento: Evento) {
        Log.d("EventoViewModel", "📌 Evento seleccionado: ${evento.id} - ${evento.nombre}")
        _eventoSeleccionado.value = evento
    }

    fun limpiarEventoSeleccionado() {
        Log.d("EventoViewModel", "🧹 Limpiando evento seleccionado")
        _eventoSeleccionado.value = null
    }

    /**
     * Envía una solicitud para crear un nuevo evento.
     */
    suspend fun crearEvento(token: String, eventoRequest: EventoRequest): Boolean {
        return try {
            val res = api.crearEvento("Bearer $token", eventoRequest)
            val success = res.isSuccessful
            Log.d("EventoViewModel", if (success) "✅ Evento creado" else "❌ Error al crear evento: ${res.code()}")
            success
        } catch (e: Exception) {
            Log.e("EventoViewModel", "❌ Excepción al crear evento", e)
            false
        }
    }

    suspend fun actualizarEvento(token: String, eventoId: Long, eventoRequest: EventoRequest): Boolean {
        return try {
            val res = api.actualizarEvento("Bearer $token", eventoId, eventoRequest)
            Log.d("EventoViewModel", "📝 Actualizando evento ${eventoId}: ${res.code()}")
            res.isSuccessful
        } catch (e: Exception) {
            Log.e("EventoViewModel", "❌ Error al actualizar evento", e)
            false
        }
    }

    suspend fun eliminarEvento(token: String, eventoId: Long): Boolean {
        return try {
            val res = api.eliminarEvento("Bearer $token", eventoId)
            Log.d("EventoViewModel", "🗑️ Evento eliminado: $eventoId")
            res.isSuccessful
        } catch (e: Exception) {
            Log.e("EventoViewModel", "❌ Error al eliminar evento", e)
            false
        }
    }

    /**
     * Registra la asistencia de un usuario a un evento.
     */
    fun registrarAsistencia(
        eventoId: Long,
        asistira: Boolean,
        pagado: Boolean
    ) {
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
                    Log.d("EventoViewModel", "✅ Asistencia registrada para evento $eventoId")
                } else {
                    Log.e("EventoViewModel", "❌ Error al registrar asistencia: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("EventoViewModel", "❌ Excepción al registrar asistencia", e)
            }
        }
    }
}