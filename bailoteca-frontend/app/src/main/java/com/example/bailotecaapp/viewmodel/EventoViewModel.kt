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
 * ViewModel encargado de gestionar la lógica de eventos dentro de la aplicación Bailoteca.
 * Encapsula la obtención de eventos públicos, privados y el control del evento seleccionado.
 * También permite el registro de asistencias y la creación/modificación/eliminación de eventos.
 *
 * Utiliza corrutinas para acceder a la API de forma asincrónica y MutableStateFlow para exponer
 * los datos observables a la UI.
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
     * Obtiene los eventos públicos disponibles para usuarios no autenticados o invitados.
     */
    fun obtenerEventosPublicos() {
        viewModelScope.launch {
            try {
                Log.d("EventoViewModel", "Solicitando eventos públicos...")
                val resultado = api.obtenerEventos()
                _eventos.value = resultado
                Log.d("EventoViewModel", "Eventos públicos obtenidos: ${resultado.size}")
            } catch (e: Exception) {
                Log.e("EventoViewModel", "Error cargando eventos públicos", e)
            }
        }
    }

    /**
     * Obtiene los eventos privados (requiere autenticación) para usuarios con permisos ADMIN o PROFESOR.
     * @param token Token JWT de autenticación.
     */
    fun obtenerEventosPrivados(token: String) {
        viewModelScope.launch {
            try {
                Log.d("EventoViewModel", "Solicitando eventos privados...")
                val res = api.getEventosPrivados("Bearer $token")
                if (res.isSuccessful) {
                    _eventos.value = res.body() ?: emptyList()
                    Log.d("EventoViewModel", "Eventos privados obtenidos: ${_eventos.value.size}")
                } else {
                    Log.w("EventoViewModel", "Eventos no disponibles. Código: ${res.code()}")
                }
            } catch (e: Exception) {
                Log.e("EventoViewModel", "Error cargando eventos privados", e)
            }
        }
    }

    /**
     * Establece el evento seleccionado actual.
     * @param evento Evento a establecer como seleccionado.
     */
    fun seleccionarEvento(evento: Evento) {
        Log.d("EventoViewModel", "Evento seleccionado: ${evento.nombre}")
        _eventoSeleccionado.value = evento
    }

    /**
     * Limpia el evento seleccionado, útil al salir de la vista de detalles o editar.
     */
    fun limpiarEventoSeleccionado() {
        _eventoSeleccionado.value = null
        Log.d("EventoViewModel", "Evento seleccionado limpiado")
    }

    /**
     * Crea un nuevo evento en el sistema a partir de los datos introducidos por el usuario.
     * @param token Token JWT del usuario.
     * @param request Objeto DTO con los datos del nuevo evento.
     * @return true si se crea correctamente, false si hay error.
     */
    suspend fun crearEvento(token: String, request: EventoRequest): Boolean {
        return try {
            Log.d("EventoViewModel", "Creando evento con nombre: ${request.nombre}")
            val res = api.crearEvento("Bearer $token", request)
            val success = res.isSuccessful
            Log.d("EventoViewModel", "Resultado creación evento: $success")
            success
        } catch (e: Exception) {
            Log.e("EventoViewModel", "Error al crear evento", e)
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
            Log.d("EventoViewModel", "Actualizando evento con ID: ${evento.id}")
            val res = api.actualizarEvento("Bearer $token", evento.id, evento)
            res.isSuccessful
        } catch (e: Exception) {
            Log.e("EventoViewModel", "Error al actualizar evento", e)
            false
        }
    }

    /**
     * Elimina un evento por su identificador.
     * @param token Token JWT de autenticación.
     * @param eventoId ID del evento a eliminar.
     * @return true si se elimina correctamente, false si hay error.
     */
    suspend fun eliminarEvento(token: String, eventoId: Long): Boolean {
        return try {
            Log.d("EventoViewModel", "Eliminando evento con ID: $eventoId")
            val res = api.eliminarEvento("Bearer $token", eventoId)
            res.isSuccessful
        } catch (e: Exception) {
            Log.e("EventoViewModel", "Error al eliminar evento", e)
            false
        }
    }

    /**
     * Registra o actualiza la asistencia del usuario autenticado a un evento concreto.
     * @param eventoId ID del evento.
     * @param asistira true si asistirá, false si no.
     * @param pagado true si ya ha pagado, false si no.
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
                    Log.e("EventoViewModel", "Token no disponible para registrar asistencia.")
                    return@launch
                }

                val request = AsistenciaEventoRequest(asistira, pagado)
                val response = api.registrarAsistenciaEvento("Bearer $token", eventoId, request)

                if (response.isSuccessful) {
                    val asistencia = response.body()
                    Log.d("EventoViewModel", "Asistencia registrada correctamente: $asistencia")
                } else {
                    Log.e(
                        "EventoViewModel",
                        "Error al registrar asistencia: ${response.code()} - ${response.errorBody()?.string()}"
                    )
                }
            } catch (e: Exception) {
                Log.e("EventoViewModel", "Excepción al registrar asistencia", e)
            }
        }
    }
}