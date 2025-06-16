package com.example.bailotecaapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bailotecaapp.model.Inscripcion
import com.example.bailotecaapp.model.Usuario
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
 * ViewModel encargado de gestionar la lógica de negocio relacionada con usuarios.
 * Coordina las operaciones CRUD y visibilidad en función del rol (ADMIN o PROFESOR).
 *
 * @author ChatGPT
 */
@HiltViewModel
class UsuarioViewModel @Inject constructor(
    private val api: ApiService,
    private val sesionManager: SesionManager
) : ViewModel() {

    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios

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
            Log.d("UsuarioViewModel", "⏳ Esperando a que usuario y token estén disponibles...")
            esperarUsuarioYToken()?.let { (usuario, token) ->
                Log.d("UsuarioViewModel", "✅ Sesión sincronizada: ${usuario.correo}, Rol: ${usuario.rol}")
                obtenerUsuariosSegunRol(usuario, token)
            } ?: Log.e("UsuarioViewModel", "❌ No se pudo recuperar el usuario o el token")
        }
    }

    fun obtenerUsuariosSegunRol(usuario: Usuario, token: String) {
        viewModelScope.launch {
            Log.d("UsuarioViewModel", "👤 Rol detectado: ${usuario.rol}")
            when (usuario.rol) {
                Rol.ADMIN -> {
                    Log.d("UsuarioViewModel", "📤 ADMIN: cargando todos los usuarios")
                    obtenerTodosLosUsuarios(token)
                }
                Rol.PROFESOR -> {
                    Log.d("UsuarioViewModel", "📤 PROFESOR: cargando usuarios visibles por profesor ID=${usuario.id}")
                    obtenerUsuariosVisiblesParaProfesor(token, usuario.id ?: -1L)
                }
                else -> {
                    Log.w("UsuarioViewModel", "⚠️ Rol sin permisos: ${usuario.rol}")
                    _usuarios.value = emptyList()
                    _errorMessage.value = "No tienes permisos para ver esta información"
                }
            }
        }
    }

    private suspend fun obtenerTodosLosUsuarios(token: String) {
        _isLoading.value = true
        _errorMessage.value = null
        try {
            Log.d("UsuarioViewModel", "🌐 GET /api/usuarios")
            val response = api.getUsuarios("Bearer $token")
            if (response.isSuccessful) {
                _usuarios.value = response.body().orEmpty()
                Log.d("UsuarioViewModel", "✅ Usuarios cargados correctamente: ${_usuarios.value.size}")
            } else {
                manejarErrorHttp(response.code(), "al cargar usuarios")
            }
        } catch (e: Exception) {
            manejarExcepcion(e, "obtenerTodosLosUsuarios")
        } finally {
            _isLoading.value = false
        }
    }

    private suspend fun obtenerUsuariosVisiblesParaProfesor(token: String, profesorId: Long) {
        _isLoading.value = true
        _errorMessage.value = null
        try {
            Log.d("UsuarioViewModel", "🌐 GET usuarios + inscripciones para profesor $profesorId")
            val usuariosResponse = api.getUsuarios("Bearer $token")
            val inscripcionesResponse = api.getInscripcionesProfesor("Bearer $token", profesorId)

            if (usuariosResponse.isSuccessful && inscripcionesResponse.isSuccessful) {
                val todosLosUsuarios = usuariosResponse.body().orEmpty()
                val inscripciones = inscripcionesResponse.body().orEmpty()
                val idsUsuarios = inscripciones.mapNotNull { it.usuario.id }.toSet()
                _usuarios.value = todosLosUsuarios.filter { it.id in idsUsuarios && it.id != profesorId }
                Log.d("UsuarioViewModel", "✅ Usuarios visibles filtrados: ${_usuarios.value.size}")
            } else {
                _errorMessage.value = "Error HTTP usuarios=${usuariosResponse.code()}, inscripciones=${inscripcionesResponse.code()}"
            }
        } catch (e: Exception) {
            manejarExcepcion(e, "obtenerUsuariosVisiblesParaProfesor")
        } finally {
            _isLoading.value = false
        }
    }

    fun togglePagado(usuario: Usuario) {
        viewModelScope.launch {
            val (user, token) = esperarUsuarioYToken() ?: return@launch
            try {
                val request = UsuarioEstadoUpdateRequest(pagado = !usuario.pagado)
                val response = usuario.id?.let { api.actualizarEstadoUsuario("Bearer $token", it, request) }
                if (response?.isSuccessful == true) {
                    Log.d("UsuarioViewModel", "✅ Estado de pago actualizado: ${usuario.correo}")
                    obtenerUsuariosSegunRol(user, token)
                }
            } catch (e: Exception) {
                manejarExcepcion(e, "togglePagado")
            }
        }
    }

    fun toggleActivo(usuario: Usuario) {
        viewModelScope.launch {
            val (user, token) = esperarUsuarioYToken() ?: return@launch
            try {
                val request = UsuarioEstadoUpdateRequest(activo = !usuario.activo)
                val response = usuario.id?.let { api.actualizarEstadoUsuario("Bearer $token", it, request) }
                if (response?.isSuccessful == true) {
                    Log.d("UsuarioViewModel", "✅ Estado de actividad actualizado: ${usuario.correo}")
                    obtenerUsuariosSegunRol(user, token)
                }
            } catch (e: Exception) {
                manejarExcepcion(e, "toggleActivo")
            }
        }
    }

    fun cargarUsuarioPorId(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            val (_, token) = esperarUsuarioYToken() ?: return@launch
            try {
                Log.d("UsuarioViewModel", "🔍 Cargando usuario con ID $id")
                val response = api.getUsuarioPorId("Bearer $token", id)
                _usuarioDetalle.value = response.body()
            } catch (e: Exception) {
                manejarExcepcion(e, "cargarUsuarioPorId")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun eliminarUsuario(usuarioId: Long) {
        viewModelScope.launch {
            val (user, token) = esperarUsuarioYToken() ?: return@launch
            try {
                Log.d("UsuarioViewModel", "🗑️ Eliminando usuario con ID $usuarioId")
                val response = api.eliminarUsuario("Bearer $token", usuarioId)
                if (response.isSuccessful) {
                    Log.d("UsuarioViewModel", "🗑️ Usuario eliminado correctamente")
                    obtenerUsuariosSegunRol(user, token)
                }
            } catch (e: Exception) {
                manejarExcepcion(e, "eliminarUsuario")
            }
        }
    }

    fun obtenerInscripcionesDelUsuario(userId: Long) {
        viewModelScope.launch {
            val (_, token) = esperarUsuarioYToken() ?: return@launch
            try {
                Log.d("UsuarioViewModel", "📄 Cargando inscripciones del usuario $userId")
                val response = api.getInscripcionesPorUsuario("Bearer $token", userId)
                _inscripcionesUsuario.value = response.body().orEmpty()
                Log.d("UsuarioViewModel", "📄 Inscripciones cargadas: ${_inscripcionesUsuario.value.size}")
            } catch (e: Exception) {
                manejarExcepcion(e, "obtenerInscripcionesDelUsuario")
            }
        }
    }

    suspend fun actualizarUsuario(token: String, usuarioId: Long, actualizado: UsuarioUpdateRequest): Boolean {
        return try {
            Log.d("UsuarioViewModel", "✏️ Actualizando usuario $usuarioId...")
            val response = api.actualizarUsuario("Bearer $token", usuarioId, actualizado)
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

    fun sincronizarUsuarioDesdeSesion(usuario: Usuario) {
        viewModelScope.launch {
            Log.d("UsuarioViewModel", "💾 Sincronizando usuario en sesión: ${usuario.correo}")
            sesionManager.guardarUsuario(usuario)
        }
    }

    fun sincronizarDesdeSesionViewModel(sesionViewModel: SesionViewModel) {
        viewModelScope.launch {
            val user = sesionViewModel.usuario.value
            val token = sesionViewModel.token.value
            if (user != null && token != null) {
                Log.d("UsuarioViewModel", "🔁 Sincronizando sesión desde SesionViewModel")
                sesionManager.guardarUsuario(user)
                sesionManager.guardarToken(token)
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