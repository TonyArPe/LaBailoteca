package com.example.bailotecaapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bailotecaapp.model.Evento
import com.example.bailotecaapp.model.dto.AsistenciaEventoRequest
import com.example.bailotecaapp.network.ApiService
import com.example.bailotecaapp.network.session.SesionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    /**
     * Carga eventos públicos (para invitados).
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
     * Carga eventos autenticados (admin o profesor).
     */
    fun obtenerEventosPrivados(token: String) {
        viewModelScope.launch {
            try {
                val res = api.getEventosPrivados("Bearer $token")
                if (res.isSuccessful) {
                    _eventos.value = res.body() ?: emptyList()
                } else {
                    Log.w("EventoViewModel", "Eventos no disponibles: ${res.code()}")
                }
            } catch (e: Exception) {
                Log.e("EventoViewModel", "Error cargando eventos privados", e)
            }
        }
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

    fun registrarAsistencia(
        eventoId: Long,
        asistira: Boolean,
        pagado: Boolean
    ) {
        viewModelScope.launch {
            try {
                val token = sesionManager.getToken()
                if (token == null) {
                    Log.e("EventoViewModel", "Token no disponible.")
                    return@launch
                }

                val request = AsistenciaEventoRequest(asistira, pagado)
                val response = api.registrarAsistenciaEvento("Bearer $token", eventoId, request)

                if (response.isSuccessful) {
                    val asistencia = response.body()
                    Log.d("EventoViewModel", "Asistencia registrada: $asistencia")
                    // Aquí podrías actualizar estado interno o emitir evento UI
                } else {
                    Log.e("EventoViewModel", "Error al registrar asistencia: ${response.code()} - ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("EventoViewModel", "Excepción al registrar asistencia", e)
            }
        }
    }

}