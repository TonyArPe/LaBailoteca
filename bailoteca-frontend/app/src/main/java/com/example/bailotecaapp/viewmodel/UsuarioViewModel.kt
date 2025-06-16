package com.example.bailotecaapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bailotecaapp.model.Inscripcion
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.model.dto.UsuarioDTO
import com.example.bailotecaapp.model.dto.UsuarioEstadoUpdateRequest
import com.example.bailotecaapp.model.dto.UsuarioUpdateRequest
import com.example.bailotecaapp.model.enums.Rol
import com.example.bailotecaapp.network.ApiService
import com.example.bailotecaapp.network.session.SesionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel encargado de gestionar los usuarios visibles en la app según rol.
 * Admins pueden ver todos los usuarios, Profesores solo a los inscritos en sus clases.
 * También gestiona los cambios de estado y eliminación.
 */
@HiltViewModel
class UsuarioViewModel @Inject constructor(
    private val api: ApiService,
    private val sesionManager: SesionManager
) : ViewModel() {

    private val _usuarios = MutableStateFlow<List<UsuarioDTO>>(emptyList())
    val usuarios: StateFlow<List<UsuarioDTO>> = _usuarios

    private val _usuarioEstados = MutableStateFlow<Map<Long, Usuario>>(emptyMap())
    val usuarioEstados: StateFlow<Map<Long, Usuario>> = _usuarioEstados

    private val _usuarioDetalle = MutableStateFlow<Usuario?>(null)
    val usuarioDetalle: StateFlow<Usuario?> = _usuarioDetalle

    private val _inscripcionesUsuario = MutableStateFlow<List<Inscripcion>>(emptyList())
    val inscripcionesUsuario: StateFlow<List<Inscripcion>> = _inscripcionesUsuario

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        viewModelScope.launch {
            Log.d("UsuarioViewModel", "⏳ Inicializando ViewModel y esperando sesión...")
            esperarUsuarioYToken()?.let { (usuario, token) ->
                Log.d("UsuarioViewModel", "✅ Sesión lista: ${usuario.correo} (${usuario.rol})")
                obtenerUsuariosSegunRol(usuario, token)
            }
        }
    }

    fun cargarUsuarioPorId(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            val (_, token) = esperarUsuarioYToken() ?: run {
                Log.e("UsuarioViewModel", "⛔ Token o usuario no disponible para cargar usuario por ID")
                _isLoading.value = false
                return@launch
            }
            try {
                Log.d("UsuarioViewModel", "🔍 Cargando usuario con ID $id")
                val response = api.getUsuarioPorId("Bearer $token", id)
                if (response.isSuccessful) {
                    _usuarioDetalle.value = response.body()
                    Log.d("UsuarioViewModel", "✅ Usuario cargado correctamente: ${response.body()?.correo}")
                } else {
                    Log.e("UsuarioViewModel", "❌ Error HTTP ${response.code()} al cargar usuario por ID")
                }
            } catch (e: Exception) {
                Log.e("UsuarioViewModel", "❌ Excepción al cargar usuario por ID", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Obtiene los usuarios visibles dependiendo del rol del usuario autenticado.
     */
    fun obtenerUsuariosSegunRol(usuario: Usuario, token: String) {
        viewModelScope.launch {
            obtenerTodosLosUsuarios(token)
        }
    }

    private suspend fun obtenerTodosLosUsuarios(token: String) {
        _isLoading.value = true
        try {
            val usuariosResponse = api.getUsuarios("Bearer $token")
            if (usuariosResponse.isSuccessful) {
                val usuariosDTO = usuariosResponse.body().orEmpty()
                _usuarios.value = usuariosDTO

                // Ahora carga cada usuario individualmente para tener activo y pagado
                val estados = usuariosDTO.mapNotNull { dto ->
                    dto.id?.let { api.getUsuarioPorId("Bearer $token", it).body() }
                }.associateBy { it.id!! }

                _usuarioEstados.value = estados

                Log.d("UsuarioViewModel", "📋 Usuarios cargados con estados: ${estados.size}")
            } else {
                manejarErrorHttp(usuariosResponse.code(), "al obtener usuarios")
            }
        } catch (e: Exception) {
            manejarExcepcion(e, "obtenerTodosLosUsuarios")
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Obtiene los usuarios asociados a las inscripciones del profesor autenticado.
     */
    private suspend fun obtenerUsuariosVisiblesParaProfesor(token: String, profesorId: Long) {
        _isLoading.value = true
        try {
            val usuariosResponse = api.getUsuarios("Bearer $token")
            val inscripcionesResponse = api.getInscripcionesProfesor("Bearer $token", profesorId)

            if (usuariosResponse.isSuccessful && inscripcionesResponse.isSuccessful) {
                val todosLosUsuarios = usuariosResponse.body().orEmpty()
                val inscripciones = inscripcionesResponse.body().orEmpty()
                val idsVisibles = inscripciones.mapNotNull { it.usuario.id }.toSet()
                _usuarios.value = todosLosUsuarios.filter { it.id in idsVisibles && it.id != profesorId }
                Log.d("UsuarioViewModel", "📋 Usuarios filtrados por profesor: ${_usuarios.value.size}")
            } else {
                manejarErrorHttp(usuariosResponse.code(), "usuarios")
                manejarErrorHttp(inscripcionesResponse.code(), "inscripciones")
            }
        } catch (e: Exception) {
            manejarExcepcion(e, "obtenerUsuariosVisiblesParaProfesor")
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Alterna el estado de 'pagado' de un usuario (solo PROFESOR).
     */
    fun toggleActivo(usuario: Usuario) {
        viewModelScope.launch {
            val (_, token) = esperarUsuarioYToken() ?: return@launch
            val nuevoActivo = !usuario.activo
            Log.d("UsuarioViewModel", "🟢 Toggle ACTIVO a $nuevoActivo para ${usuario.correo}")
            try {
                val response = api.actualizarEstadoUsuario(
                    token = "Bearer $token",
                    id = usuario.id ?: return@launch,
                    request = UsuarioEstadoUpdateRequest(activo = nuevoActivo)
                )
                if (response.isSuccessful) {
                    Log.d("UsuarioViewModel", "✅ Backend actualizó ACTIVO correctamente")
                    val usuarioSesion = sesionManager.getUsuarioActual()
                    if (usuarioSesion != null) {
                        obtenerUsuariosSegunRol(usuarioSesion, token)
                    } else {
                        Log.e("UsuarioViewModel", "⚠️ usuarioSesion es null tras actualizar estado")
                    }
                } else {
                    Log.e("UsuarioViewModel", "❌ Error al actualizar ACTIVO: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("UsuarioViewModel", "❌ Excepción en toggleActivo", e)
            }
        }
    }

    fun togglePagado(usuario: Usuario) {
        viewModelScope.launch {
            val (_, token) = esperarUsuarioYToken() ?: return@launch
            val nuevoPagado = !usuario.pagado
            Log.d("UsuarioViewModel", "💰 Toggle PAGADO a $nuevoPagado para ${usuario.correo}")
            try {
                val response = api.actualizarEstadoUsuario(
                    token = "Bearer $token",
                    id = usuario.id ?: return@launch,
                    request = UsuarioEstadoUpdateRequest(pagado = nuevoPagado)
                )
                if (response.isSuccessful) {
                    Log.d("UsuarioViewModel", "✅ Backend actualizó PAGADO correctamente")
                    val usuarioSesion = sesionManager.getUsuarioActual()
                    if (usuarioSesion != null) {
                        obtenerUsuariosSegunRol(usuarioSesion, token)
                    } else {
                        Log.e("UsuarioViewModel", "⚠️ usuarioSesion es null tras actualizar estado")
                    }
                } else {
                    Log.e("UsuarioViewModel", "❌ Error al actualizar PAGADO: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("UsuarioViewModel", "❌ Excepción en togglePagado", e)
            }
        }
    }

    /**
     * Actualiza los datos de un usuario específico en el backend.
     *
     * @param token Token de autorización JWT con el prefijo "Bearer ".
     * @param usuarioId ID del usuario a actualizar.
     * @param actualizado Objeto UsuarioUpdateRequest con los nuevos datos.
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    suspend fun actualizarUsuario(
        token: String,
        usuarioId: Long,
        actualizado: UsuarioUpdateRequest
    ): Boolean {
        return try {
            Log.d("UsuarioViewModel", "✏️ Actualizando usuario $usuarioId...")
            val response = api.actualizarUsuario(token, usuarioId, actualizado)
            if (response.isSuccessful) {
                Log.d("UsuarioViewModel", "✅ Usuario actualizado correctamente")
                true
            } else {
                manejarErrorHttp(response.code(), "al actualizar usuario")
                false
            }
        } catch (e: Exception) {
            manejarExcepcion(e, "actualizarUsuario")
            false
        }
    }

    /**
     * Elimina un usuario del sistema (solo ADMIN o el mismo usuario).
     */
    fun eliminarUsuario(usuarioId: Long) {
        viewModelScope.launch {
            val (_, token) = esperarUsuarioYToken() ?: return@launch
            val response = api.eliminarUsuario("Bearer $token", usuarioId)
            if (response.isSuccessful) {
                obtenerTodosLosUsuarios(token)
            } else {
                manejarErrorHttp(response.code(), "al eliminar usuario")
            }
        }
    }

    /**
     * Carga las inscripciones de un usuario concreto (pantalla detalle).
     */
    fun obtenerInscripcionesDelUsuario(userId: Long) {
        viewModelScope.launch {
            val (_, token) = esperarUsuarioYToken() ?: return@launch
            try {
                val response = api.getInscripcionesPorUsuario("Bearer $token", userId)
                _inscripcionesUsuario.value = response.body().orEmpty()
                Log.d("UsuarioViewModel", "📄 Inscripciones cargadas: ${_inscripcionesUsuario.value.size}")
            } catch (e: Exception) {
                manejarExcepcion(e, "obtenerInscripcionesDelUsuario")
            }
        }
    }

    private suspend fun esperarUsuarioYToken(): Pair<Usuario, String>? {
        return combine(sesionManager.usuario, sesionManager.token) { usuario, token ->
            usuario to token
        }
            .filter { it.first != null && it.second != null }
            .map { it.first!! to it.second!! }
            .firstOrNull()
    }

    private fun manejarExcepcion(e: Exception, origen: String) {
        Log.e("UsuarioViewModel", "❌ Error en $origen", e)
        _errorMessage.value = "Error en $origen: ${e.message}"
    }

    private fun manejarErrorHttp(codigo: Int, contexto: String) {
        val mensaje = "Error HTTP $codigo $contexto"
        Log.e("UsuarioViewModel", "❌ $mensaje")
        _errorMessage.value = mensaje
    }
}