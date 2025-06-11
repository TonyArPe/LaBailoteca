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
import okhttp3.MultipartBody
import javax.inject.Inject

/**
 * ViewModel que gestiona los eventos en la aplicación.
 * Permite obtener, crear, actualizar y eliminar eventos,
 * así como gestionar la asistencia del usuario.
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
     * Carga los eventos públicos, disponibles para invitados.
     */
    fun obtenerEventosPublicos() {
        viewModelScope.launch {
            try {
                val resultado = api.obtenerEventos()
                _eventos.value = resultado
                Log.d("EventoViewModel", "✅ Eventos públicos cargados correctamente")
            } catch (e: Exception) {
                Log.e("EventoViewModel", "❌ Error cargando eventos públicos", e)
            }
        }
    }

    /**
     * Carga eventos privados según el rol (admin/profesor).
     */
    fun obtenerEventosPrivados(token: String) {
        viewModelScope.launch {
            try {
                val res = api.getEventosPrivados("Bearer $token")
                if (res.isSuccessful) {
                    _eventos.value = res.body() ?: emptyList()
                    Log.d("EventoViewModel", "✅ Eventos privados cargados: ${_eventos.value.size}")
                } else {
                    Log.w("EventoViewModel", "⚠️ Fallo al obtener eventos: ${res.code()}")
                }
            } catch (e: Exception) {
                Log.e("EventoViewModel", "❌ Error cargando eventos privados", e)
            }
        }
    }

    fun subirImagenEvento(archivo: MultipartBody.Part, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val respuesta = api.subirArchivo(archivo)
                if (respuesta.isSuccessful) {
                    val nombre = respuesta.body()
                    Log.d("EventoViewModel", "✅ Imagen evento subida: $nombre")
                    nombre?.let { onSuccess(it) }
                } else {
                    Log.e("EventoViewModel", "❌ Error al subir imagen evento: ${respuesta.errorBody()}")
                }
            } catch (e: Exception) {
                Log.e("EventoViewModel", "❌ Excepción al subir imagen evento", e)
            }
        }
    }

    /**
     * Marca un evento como seleccionado en el estado.
     */
    fun seleccionarEvento(evento: Evento) {
        _eventoSeleccionado.value = evento
        Log.d("EventoViewModel", "📌 Evento seleccionado: ${evento.nombre}")
    }

    /**
     * Limpia el evento seleccionado.
     */
    fun limpiarEventoSeleccionado() {
        _eventoSeleccionado.value = null
        Log.d("EventoViewModel", "🧹 Evento deseleccionado")
    }

    /**
     * Crea un nuevo evento en el sistema.
     * @param token Token JWT del usuario autenticado.
     * @param request Objeto EventoRequest con los datos del nuevo evento.
     * @return true si fue creado correctamente, false si hubo error.
     */
    suspend fun crearEvento(token: String, request: EventoRequest): Boolean {
        return try {
            Log.d("EventoViewModel", "🛠️ Creando evento con nombre: ${request.nombre}")
            val res = api.crearEvento("Bearer $token", request)
            if (res.isSuccessful) {
                Log.d("EventoViewModel", "✅ Evento creado correctamente")

                // 🆕 Actualizar eventos si fue exitoso
                obtenerEventosPrivados(token)

                true
            } else {
                Log.e("EventoViewModel", "❌ Error en respuesta: ${res.code()} - ${res.message()}")
                false
            }
        } catch (e: Exception) {
            Log.e("EventoViewModel", "❌ Error al crear evento", e)
            false
        }
    }

    /**
     * Actualiza un evento ya existente.
     * @param token Token JWT.
     * @param evento Objeto Evento (incluye el ID y datos nuevos).
     */
    suspend fun actualizarEvento(token: String, eventoId: Long, evento: EventoRequest): Boolean {
        return try {
            Log.d("EventoViewModel", "📝 Actualizando evento con ID: $eventoId")
            val res = api.actualizarEvento("Bearer $token", eventoId, evento)
            res.isSuccessful
        } catch (e: Exception) {
            Log.e("EventoViewModel", "❌ Error al actualizar evento", e)
            false
        }
    }

    /**
     * Carga todos los eventos como administrador.
     */
    fun getEventosAdmin() {
        viewModelScope.launch {
            try {
                val token = sesionManager.getToken()
                if (token != null) {
                    obtenerEventosPrivados(token)
                }
            } catch (e: Exception) {
                Log.e("EventoViewModel", "❌ Error al cargar eventos para ADMIN", e)
            }
        }
    }

    /**
     * Carga eventos creados por un profesor.
     */
    fun getEventosProfesor(profesorId: Long) {
        viewModelScope.launch {
            try {
                val token = sesionManager.getToken()
                if (token != null) {
                    val res = api.getEventosProfesor("Bearer $token", profesorId)
                    if (res.isSuccessful) {
                        _eventos.value = res.body() ?: emptyList()
                        Log.d("EventoViewModel", "✅ Eventos del profesor $profesorId cargados")
                    } else {
                        Log.e("EventoViewModel", "❌ Fallo en eventos profesor: ${res.code()}")
                    }
                } else {
                    Log.e("EventoViewModel", "❌ Token nulo al cargar eventos del profesor")
                }
            } catch (e: Exception) {
                Log.e("EventoViewModel", "❌ Error al obtener eventos del profesor", e)
            }
        }
    }

    fun getEventosVisibles(usuarioId: Long) {
        viewModelScope.launch {
            try {
                val token = sesionManager.getToken()
                if (token != null) {
                    val res = api.getEventosUsuario("Bearer $token", usuarioId)
                    if (res.isSuccessful) {
                        _eventos.value = res.body() ?: emptyList()
                        Log.d("EventoViewModel", "✅ Eventos visibles cargados para usuario $usuarioId")
                    } else {
                        Log.w("EventoViewModel", "⚠️ Error eventos visibles: ${res.code()}")
                    }
                } else {
                    Log.e("EventoViewModel", "❌ Token nulo al cargar eventos visibles")
                }
            } catch (e: Exception) {
                Log.e("EventoViewModel", "❌ Error cargando eventos visibles", e)
            }
        }
    }

    /**
     * Elimina un evento por su ID.
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
     * Registra la asistencia del usuario actual a un evento.
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
                    Log.e("EventoViewModel", "❌ Token no disponible.")
                    return@launch
                }

                val request = AsistenciaEventoRequest(asistira, pagado)
                val response = api.registrarAsistenciaEvento("Bearer $token", eventoId, request)

                if (response.isSuccessful) {
                    val asistencia = response.body()
                    Log.d("EventoViewModel", "✅ Asistencia registrada: $asistencia")
                } else {
                    Log.e("EventoViewModel", "❌ Error al registrar asistencia: ${response.code()} - ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("EventoViewModel", "❌ Excepción registrando asistencia", e)
            }
        }
    }
}