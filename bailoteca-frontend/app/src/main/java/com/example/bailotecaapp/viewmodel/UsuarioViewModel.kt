package com.example.bailotecaapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bailotecaapp.model.Inscripcion
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.model.dto.UsuarioEstadoUpdateRequest
import com.example.bailotecaapp.model.dto.UsuarioUpdateRequest
import com.example.bailotecaapp.network.ApiService
import com.example.bailotecaapp.network.session.SesionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel que gestiona la lógica de los usuarios visibles en la app.
 * Se apoya en el SesionManager para obtener token y usuario actual.
 */
@HiltViewModel
class UsuarioViewModel @Inject constructor(
    private val api: ApiService,
    val sesionManager: SesionManager
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

    fun obtenerTodosLosUsuarios() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val token = sesionManager.getToken() ?: return@launch
                val response = api.getUsuarios("Bearer $token")

                if (response.isSuccessful) {
                    val todos = response.body() ?: emptyList()
                    val yo = sesionManager.usuario.value?.id
                    _usuarios.value = todos.filter { it.id != yo }

                    Log.d("UsuarioViewModel", "✅ Todos los usuarios cargados: ${_usuarios.value.size}")
                } else {
                    _errorMessage.value = "Error al obtener usuarios: ${response.code()}"
                }

            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun obtenerUsuariosVisiblesParaProfesor(profesorId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val token = sesionManager.getToken() ?: return@launch

                val responseUsuarios = api.getUsuarios("Bearer $token")
                val responseInscripciones = api.getInscripcionesProfesor("Bearer $token", profesorId)

                if (responseUsuarios.isSuccessful && responseInscripciones.isSuccessful) {
                    val usuarios = responseUsuarios.body() ?: emptyList()
                    val inscripciones = responseInscripciones.body() ?: emptyList()
                    val idsUsuariosConClases = inscripciones.mapNotNull { it.usuario.id }.toSet()
                    val yo = sesionManager.usuario.value?.id

                    _usuarios.value = usuarios.filter {
                        it.id in idsUsuariosConClases && it.id != yo
                    }

                    Log.d("UsuarioViewModel", "✅ Usuarios visibles cargados: ${_usuarios.value.size}")
                } else {
                    _errorMessage.value = "Error al cargar datos: usuarios=${responseUsuarios.code()}, inscripciones=${responseInscripciones.code()}"
                }

            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun togglePagado(usuario: Usuario) {
        viewModelScope.launch {
            try {
                val token = sesionManager.getToken() ?: return@launch
                val request = UsuarioEstadoUpdateRequest(pagado = !usuario.pagado)
                val response = usuario.id?.let { api.actualizarEstadoUsuario(token = "Bearer $token", id = it, request = request) }
                if (response != null) {
                    if (response.isSuccessful) {
                        Log.d("UsuarioViewModel", "✅ Estado 'pagado' actualizado para ${usuario.correo}")
                        obtenerTodosLosUsuarios()
                    } else {
                        Log.e("UsuarioViewModel", "❌ Error al actualizar estado de pago")
                    }
                }
            } catch (e: Exception) {
                Log.e("UsuarioViewModel", "❌ Excepción al actualizar estado de pago", e)
            }
        }
    }

    fun toggleActivo(usuario: Usuario) {
        viewModelScope.launch {
            try {
                val token = sesionManager.getToken() ?: return@launch
                val request = UsuarioEstadoUpdateRequest(activo = !usuario.activo)
                val response = usuario.id?.let { api.actualizarEstadoUsuario(token = "Bearer $token", id = it, request = request) }
                if (response != null) {
                    if (response.isSuccessful) {
                        Log.d("UsuarioViewModel", "✅ Estado 'activo' actualizado para ${usuario.correo}")
                        obtenerTodosLosUsuarios()
                    } else {
                        Log.e("UsuarioViewModel", "❌ Error al actualizar estado activo")
                    }
                }
            } catch (e: Exception) {
                Log.e("UsuarioViewModel", "❌ Excepción al actualizar estado activo", e)
            }
        }
    }

    suspend fun actualizarUsuario(token: String, usuarioId: Long, actualizado: UsuarioUpdateRequest): Boolean {
        return try {
            val response = api.actualizarUsuario(
                token = "Bearer $token",
                id = usuarioId,
                usuario = actualizado
            )
            if (response.isSuccessful) {
                Log.d("UsuarioViewModel", "✅ Usuario actualizado correctamente: $usuarioId")
                true
            } else {
                Log.e("UsuarioViewModel", "❌ Fallo al actualizar usuario: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e("UsuarioViewModel", "❌ Excepción al actualizar usuario: ${e.message}", e)
            false
        }
    }

    fun eliminarUsuario(usuarioId: Long) {
        viewModelScope.launch {
            try {
                val token = sesionManager.getToken() ?: return@launch
                val response = api.eliminarUsuario("Bearer $token", usuarioId)
                if (response.isSuccessful) {
                    Log.d("UsuarioViewModel", "🗑️ Usuario eliminado: ID $usuarioId")
                    obtenerTodosLosUsuarios()
                } else {
                    Log.e("UsuarioViewModel", "❌ Error al eliminar usuario")
                }
            } catch (e: Exception) {
                Log.e("UsuarioViewModel", "❌ Excepción al eliminar usuario", e)
            }
        }
    }

    fun cargarUsuarioPorId(userId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = sesionManager.getToken() ?: return@launch
                val response = api.getUsuarioPorId("Bearer $token", userId)
                if (response.isSuccessful) {
                    _usuarioDetalle.value = response.body()
                } else {
                    _errorMessage.value = "Error al obtener usuario: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun obtenerInscripcionesDelUsuario(userId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = sesionManager.getToken() ?: return@launch
                val response = api.getInscripcionesPorUsuario("Bearer $token", userId)
                if (response.isSuccessful) {
                    _inscripcionesUsuario.value = response.body() ?: emptyList()
                } else {
                    _errorMessage.value = "Error al obtener inscripciones: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cargarInscripcionesDelUsuario(userId: Long) = obtenerInscripcionesDelUsuario(userId)
}