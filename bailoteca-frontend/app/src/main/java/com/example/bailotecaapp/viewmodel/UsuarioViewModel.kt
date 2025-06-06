package com.example.bailotecaapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bailotecaapp.model.Inscripcion
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.model.dto.UsuarioUpdateRequest
import com.example.bailotecaapp.network.ApiService
import com.example.bailotecaapp.network.session.SesionManager
import com.example.bailotecaapp.model.dto.UsuarioEstadoUpdateRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel encargado de gestionar los usuarios para profesores y administradores.
 * Permite alternar estados de "activo" y "pagado", cargar datos, y actualizar al backend.
 */
@HiltViewModel
class UsuarioViewModel @Inject constructor(
    private val api: ApiService,
    internal val sesionManager: SesionManager
) : ViewModel() {

    private val _usuarioDetalle = MutableStateFlow<Usuario?>(null)
    val usuarioDetalle: StateFlow<Usuario?> = _usuarioDetalle

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios

    private val _inscripcionesUsuario = MutableStateFlow<List<Inscripcion>>(emptyList())
    val inscripcionesUsuario: StateFlow<List<Inscripcion>> = _inscripcionesUsuario

    private val _inscripcionesDelUsuario = MutableStateFlow<List<Inscripcion>>(emptyList())
    val inscripcionesDelUsuario: StateFlow<List<Inscripcion>> = _inscripcionesDelUsuario

    /**
     * Alterna el estado activo del usuario (solo profesores).
     */
    fun toggleActivo(usuario: Usuario) {
        viewModelScope.launch {
            try {
                val token = sesionManager.getToken() ?: return@launch

                val request = UsuarioUpdateRequest(
                    nombre = usuario.nombre,
                    apellido = usuario.apellido ?: "",
                    correo = usuario.correo,
                    contrasenna = usuario.contrasenna,
                    rol = usuario.rol,
                    telefono = usuario.telefono ?: "",
                    direccion = usuario.direccion ?: "",
                    activo = !usuario.activo,
                    pagado = usuario.pagado
                )

                val response = api.actualizarUsuario("Bearer $token", usuario.id!!, request)
                if (response.isSuccessful) {
                    val actualizado = response.body()
                    if (actualizado != null) {
                        _usuarioDetalle.value = actualizado
                        Log.d("UsuarioViewModel", "✅ Usuario actualizado (activo): ${actualizado.correo}")
                    } else {
                        Log.e("UsuarioViewModel", "❌ Respuesta sin cuerpo al actualizar activo")
                    }
                } else {
                    Log.e("UsuarioViewModel", "❌ Error al actualizar activo: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("UsuarioViewModel", "❌ Excepción al actualizar activo", e)
            }
        }
    }

    /**
     * Alterna el estado de pago del usuario (solo profesores).
     * Utiliza endpoint parcial PUT /usuarios/{id}/estado.
     */
    fun togglePagado(usuario: Usuario) {
        viewModelScope.launch {
            try {
                val token = sesionManager.getToken() ?: return@launch
                val nuevoEstado = !usuario.pagado

                val request = UsuarioEstadoUpdateRequest(pagado = nuevoEstado)
                val response = api.actualizarEstadoUsuario("Bearer $token", usuario.id!!, request)

                if (response.isSuccessful) {
                    val actualizado = response.body()
                    if (actualizado != null) {
                        _usuarioDetalle.value = actualizado
                        _usuarios.value = _usuarios.value.map {
                            if (it.id == actualizado.id) actualizado else it
                        }
                        Log.d("UsuarioViewModel", "✅ Pagado actualizado: ${actualizado.correo}")
                    } else {
                        Log.e("UsuarioViewModel", "❌ Respuesta sin cuerpo al actualizar pagado")
                    }
                } else {
                    Log.e("UsuarioViewModel", "❌ Error HTTP al actualizar pagado: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("UsuarioViewModel", "❌ Excepción al actualizar pagado", e)
            }
        }
    }

    /**
     * Elimina un usuario por su ID (solo ADMIN).
     */
    fun eliminarUsuario(id: Long) {
        viewModelScope.launch {
            try {
                val token = sesionManager.getToken() ?: return@launch
                val response = api.eliminarUsuario("Bearer $token", id)
                if (response.isSuccessful) {
                    _usuarios.value = _usuarios.value.filterNot { it.id == id }
                    Log.d("UsuarioViewModel", "🗑️ Usuario eliminado: $id")
                } else {
                    Log.e("UsuarioViewModel", "❌ Error al eliminar usuario: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("UsuarioViewModel", "❌ Excepción al eliminar usuario", e)
            }
        }
    }

    /**
     * Carga el detalle de un usuario por ID.
     */
    fun cargarUsuarioPorId(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val token = sesionManager.getToken() ?: return@launch
                val response = api.getUsuarioPorId("Bearer $token", id)
                if (response.isSuccessful) {
                    _usuarioDetalle.value = response.body()
                    Log.d("UsuarioViewModel", "👤 Usuario cargado: ${_usuarioDetalle.value?.correo}")
                } else {
                    _errorMessage.value = "Error ${response.code()}"
                    Log.e("UsuarioViewModel", "❌ Código: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "❌ Excepción: ${e.message}"
                Log.e("UsuarioViewModel", "❌ Error al cargar usuario", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Obtiene todas las inscripciones de un usuario.
     */
    fun obtenerInscripcionesDelUsuario(usuarioId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = sesionManager.getToken() ?: return@launch
                val response = api.getInscripcionesPorUsuario("Bearer $token", usuarioId)
                if (response.isSuccessful) {
                    _inscripcionesDelUsuario.value = response.body() ?: emptyList()
                    Log.d("UsuarioViewModel", "📚 Inscripciones cargadas: ${_inscripcionesDelUsuario.value.size}")
                }
            } catch (e: Exception) {
                Log.e("UsuarioViewModel", "❌ Error al cargar inscripciones", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Filtra las inscripciones por clases del profesor.
     */
    fun obtenerInscripcionesFiltradasPorProfesor(profesorId: Long): List<Inscripcion> {
        return _inscripcionesDelUsuario.value.filter {
            it.clase.profesor.id == profesorId
        }
    }

    /**
     * Carga todos los usuarios visibles por un profesor (usuarios con clases impartidas por él).
     */
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

                    _usuarios.value = usuarios.filter { it.id in idsUsuariosConClases }

                    Log.d("UsuarioViewModel", "✅ Usuarios visibles cargados: ${_usuarios.value.size}")
                } else {
                    _errorMessage.value = "Error al cargar usuarios visibles"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
                Log.e("UsuarioViewModel", "❌ Error cargando usuarios visibles", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Carga las inscripciones del usuario y actualiza el flujo correspondiente.
     */
    fun cargarInscripcionesDelUsuario(usuarioId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = sesionManager.getToken() ?: return@launch
                val response = api.getInscripcionesPorUsuario("Bearer $token", usuarioId)
                if (response.isSuccessful) {
                    _inscripcionesUsuario.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("UsuarioViewModel", "❌ Error al cargar inscripciones", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Elimina una inscripción por ID.
     */
    fun eliminarInscripcion(inscripcionId: Long) {
        viewModelScope.launch {
            try {
                val token = sesionManager.getToken() ?: return@launch
                val response = api.eliminarInscripcion("Bearer $token", inscripcionId)
                if (response.isSuccessful) {
                    _inscripcionesUsuario.value =
                        _inscripcionesUsuario.value.filterNot { it.id == inscripcionId }
                }
            } catch (e: Exception) {
                Log.e("UsuarioViewModel", "❌ Error al eliminar inscripción", e)
            }
        }
    }

    /**
     * Actualiza un usuario desde la pantalla de edición (usado por Admin).
     *
     * @param token Token JWT.
     * @param id ID del usuario a actualizar.
     * @param request Objeto con los nuevos datos.
     * @return true si fue exitoso, false en caso contrario.
     */
    suspend fun actualizarUsuario(token: String, id: Long, request: UsuarioUpdateRequest): Boolean {
        return try {
            val response = api.actualizarUsuario("Bearer $token", id, request)
            if (response.isSuccessful) {
                val actualizado = response.body()
                if (actualizado != null) {
                    _usuarioDetalle.value = actualizado
                    Log.d("UsuarioViewModel", "✅ Usuario actualizado correctamente: ${actualizado.correo}")
                    true
                } else {
                    Log.e("UsuarioViewModel", "❌ Cuerpo vacío al actualizar usuario")
                    false
                }
            } else {
                Log.e("UsuarioViewModel", "❌ Error HTTP al actualizar usuario: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e("UsuarioViewModel", "❌ Excepción al actualizar usuario", e)
            false
        }
    }
}