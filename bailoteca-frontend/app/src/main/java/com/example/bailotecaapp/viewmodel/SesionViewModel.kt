package com.example.bailotecaapp.viewmodel

import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bailotecaapp.datastore.TokenPreferences
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
import kotlinx.coroutines.flow.firstOrNull
import com.example.bailotecaapp.model.enums.Rol
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    private val api: ApiService,
    private val tokenPreferences: TokenPreferences
) : ViewModel() {

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _inscripciones = MutableStateFlow<List<Inscripcion>>(emptyList())
    val inscripciones: StateFlow<List<Inscripcion>> = _inscripciones

    private val _logoutEvent = MutableStateFlow(false)
    val logoutEvent: StateFlow<Boolean> = _logoutEvent


    /**
     * Inicializa la carga del usuario si existe una sesión en Firebase.
     */
    init {
        if (_usuario.value == null && Firebase.auth.currentUser != null) {
            obtenerUsuarioActual()
        }
    }

    fun inicializarSesion() {
        viewModelScope.launch {
            val token = tokenPreferences.getToken().firstOrNull()
            Log.d("SesionViewModel", "Token leído de DataStore: $token")

            if (!token.isNullOrBlank()) {
                Log.d("SesionViewModel", "Token válido, lanzando obtenerUsuarioActual()")
                obtenerUsuarioActual()
            } else {
                Log.d("SesionViewModel", "Token no encontrado, entrando como invitado.")
                entrarComoInvitado()
            }
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
                    Log.e("SesionViewModel", "Token vacío al obtener usuario.")
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
                Log.d("SesionViewModel", "Finalizó la carga del usuario.")
            }
        }
    }

    /**
     * Carga las inscripciones del usuario actual.
     */
    fun cargarMisInscripciones() {
        viewModelScope.launch {
            try {
                val token =
                    Firebase.auth.currentUser?.getIdToken(false)?.await()?.token ?: return@launch
                val usuarioId = _usuario.value?.id ?: return@launch

                val response = api.getInscripcionesPorUsuario("Bearer $token", usuarioId)
                if (response.isSuccessful) {
                    _inscripciones.value = response.body() ?: emptyList()
                    Log.d("SesionViewModel", "Inscripciones cargadas correctamente.")
                } else {
                    _error.value = "Error al obtener inscripciones: ${response.code()}"
                    Log.e(
                        "SesionViewModel",
                        "Error HTTP al obtener inscripciones: ${response.code()}"
                    )
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
    suspend fun cerrarSesion() {
        // Firebase fuera primero
        Firebase.auth.signOut()

        // Resetear ViewModel
        _usuario.value = null
        _inscripciones.value = emptyList()
        _error.value = null
        _isLoading.value = false

        // Borrar token de DataStore
        tokenPreferences.clearToken()

        // Nos lleva de vuelta al login
        _logoutEvent.value = true
        Log.d("SesionViewModel", "Sesión cerrada correctamente y token eliminado.")
    }

    fun resetLogoutEvent() {
        _logoutEvent.value = false
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
                val token =
                    Firebase.auth.currentUser?.getIdToken(true)?.await()?.token ?: return@launch
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
        val fechaRegistro = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            java.time.LocalDate.now().toString()
        } else {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }

        val invitado = Usuario(
            id = -1L,
            nombre = "Invitado",
            apellido = "Invitado",
            correo = "invitado@bailoteca.com",
            contrasenna = "",
            rol = Rol.INVITADO,
            direccion = "",
            telefono = "",
            dni = "",
            fotoPerfil = "",
            genero = "",
            fechaNacimiento = "",
            fechaRegistro = fechaRegistro,
            activo = false,
            pagado = false
        )

        Log.d("SesionViewModel", "Usuario invitado cargado correctamente: $invitado")
        _usuario.value = invitado
    }

    /**
         * Permite establecer el usuario desde fuera.
         */
        fun setUsuario(usuario: Usuario) {
            _usuario.value = usuario
        }
    }