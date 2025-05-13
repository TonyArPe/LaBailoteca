package com.example.bailotecaapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bailotecaapp.model.Inscripcion
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.model.dto.UsuarioUpdateRequest
import com.example.bailotecaapp.network.ApiService
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.example.bailotecaapp.model.enums.Rol

/**
 * ViewModel que gestiona el estado de sesión de la aplicación, incluyendo:
 * - Usuario autenticado y cargado desde el backend.
 * - Lista de inscripciones del usuario.
 * - Estado de carga y errores.
 *
 * Utilizado por componentes como SesionGuard y tal
 */
@HiltViewModel
class SesionViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _inscripciones = MutableStateFlow<List<Inscripcion>>(emptyList())
    val inscripciones: StateFlow<List<Inscripcion>> = _inscripciones

    /**
     * Inicializa la carga del usuario si existe una sesión en Firebase.
     */
    init {
        if (_usuario.value == null && Firebase.auth.currentUser != null) {
            obtenerUsuarioActual()
        }
    }

    /**
     * Obtiene el perfil del usuario autenticado desde el backend usando el token de Firebase.
     */
    fun obtenerUsuarioActual() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = Firebase.auth.currentUser?.getIdToken(true)?.await()?.token
                if (token.isNullOrEmpty()) {
                    _error.value = "Token de autenticación vacío"
                    return@launch
                }

                val response = api.getUsuarioActual("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    _usuario.value = response.body()
                    Log.d("SesionViewModel", "Usuario cargado correctamente: ${_usuario.value?.correo}")
                } else {
                    val mensaje = "Error HTTP ${response.code()}: ${response.message()}"
                    _error.value = mensaje
                    Log.e("SesionViewModel", mensaje)
                }
            } catch (e: Exception) {
                _error.value = "Error de red al obtener usuario: ${e.localizedMessage}"
                Log.e("SesionViewModel", "Excepción al obtener usuario", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Carga las inscripciones del usuario actual.
     */
    fun cargarMisInscripciones() {
        viewModelScope.launch {
            try {
                val token = Firebase.auth.currentUser?.getIdToken(false)?.await()?.token ?: return@launch
                val usuarioId = _usuario.value?.id ?: return@launch

                val response = api.getInscripcionesPorUsuario("Bearer $token", usuarioId)
                if (response.isSuccessful) {
                    _inscripciones.value = response.body() ?: emptyList()
                    Log.d("SesionViewModel", "Inscripciones cargadas correctamente.")
                } else {
                    _error.value = "Error al obtener inscripciones: ${response.code()}"
                    Log.e("SesionViewModel", "Error HTTP al obtener inscripciones: ${response.code()}")
                }
            } catch (e: Exception) {
                _error.value = "Error al cargar inscripciones: ${e.localizedMessage}"
                Log.e("SesionViewModel", "Excepción al cargar inscripciones", e)
            }
        }
    }

    /**
     * Cierra la sesión actual y limpia todos los estados del ViewModel.
     */
    fun cerrarSesion() {
        Firebase.auth.signOut()
        _usuario.value = null
        _inscripciones.value = emptyList()
        _error.value = null
        _isLoading.value = false
        Log.d("SesionViewModel", "Sesión cerrada y estado reiniciado.")
    }

    /**
     * Actualiza el perfil del usuario autenticado.
     *
     * @param usuarioActualizado DTO con los datos actualizados.
     * @param onSuccess Callback si la operación fue exitosa.
     * @param onError Callback con mensaje si hubo error.
     */
    fun actualizarPerfil(
        usuarioActualizado: UsuarioUpdateRequest,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val token = Firebase.auth.currentUser?.getIdToken(true)?.await()?.token ?: return@launch
                val userId = _usuario.value?.id ?: return@launch

                val response = api.actualizarUsuario("Bearer $token", userId, usuarioActualizado)
                if (response.isSuccessful) {
                    obtenerUsuarioActual() // refrescar
                    onSuccess()
                } else {
                    val mensaje = "Error ${response.code()}: ${response.message()}"
                    _error.value = mensaje
                    onError(mensaje)
                    Log.e("SesionViewModel", mensaje)
                }
            } catch (e: Exception) {
                val mensaje = "Excepción: ${e.localizedMessage}"
                _error.value = mensaje
                onError(mensaje)
                Log.e("SesionViewModel", "Excepción al actualizar perfil", e)
            }
        }
    }

    fun entrarComoInvitado() {
        _usuario.value = Usuario(
            id = -1,
            nombre = "Invitado",
            correo = "invitado@bailoteca.com",
            rol = Rol.INVITADO,
            activo = false,
            pagado = false,
            contrasenna = "",
            direccion = null,
            telefono = null,
            dni = null,
            fotoPerfil = null,
            genero = null,
            fechaNacimiento = null,
            fechaRegistro = null.toString()
        )
    }


    /**
     * Permite establecer el usuario desde fuera (por ejemplo, tras login).
     */
    fun setUsuario(usuario: Usuario) {
        _usuario.value = usuario
    }
}