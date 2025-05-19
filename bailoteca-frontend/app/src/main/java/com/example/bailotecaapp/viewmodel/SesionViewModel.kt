package com.example.bailotecaapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bailotecaapp.datastore.TokenPreferences
import com.example.bailotecaapp.datastore.UsuarioPersistente
import com.example.bailotecaapp.datastore.UsuarioPreferences
import com.example.bailotecaapp.model.Inscripcion
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.model.dto.UsuarioUpdateRequest
import com.example.bailotecaapp.model.enums.Rol
import com.example.bailotecaapp.network.ApiService
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * ViewModel que gestiona el estado de sesión de la aplicación,
 * incluyendo usuario autenticado, inscripciones, errores y persistencia.
 */
@HiltViewModel
class SesionViewModel @Inject constructor(
    application: Application,
    private val api: ApiService
) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _inscripciones = MutableStateFlow<List<Inscripcion>>(emptyList())
    val inscripciones: StateFlow<List<Inscripcion>> = _inscripciones

    private var _inscripcionesCargadas = false
    fun inscripcionesYaCargadas(): Boolean = _inscripcionesCargadas

    /**
     * Inicializa la sesión restaurando usuario/token si existen en DataStore o usa Firebase.
     */
    init {
        viewModelScope.launch {
            val usuarioGuardado = UsuarioPreferences.obtenerUsuario(context)
            val tokenGuardado = TokenPreferences.obtenerToken(context)

            if (usuarioGuardado != null && tokenGuardado != null) {
                Log.d("SesionViewModel", "Restaurando sesión desde preferencias")
                _usuario.value = Usuario(
                    id = usuarioGuardado.id,
                    nombre = usuarioGuardado.nombre,
                    correo = usuarioGuardado.correo,
                    rol = usuarioGuardado.rol,
                    activo = true,
                    pagado = false,
                    contrasenna = "",
                    direccion = null,
                    telefono = null,
                    dni = null,
                    fotoPerfil = null,
                    genero = null,
                    fechaNacimiento = null,
                    fechaRegistro = null
                )
                cargarMisInscripciones()
            } else if (Firebase.auth.currentUser != null) {
                obtenerUsuarioActual()
            }
        }
    }

    /**
     * Obtiene el usuario autenticado desde el backend usando Firebase Auth.
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
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        _usuario.value = user

                        UsuarioPreferences.guardarUsuario(
                            context,
                            UsuarioPersistente(
                                id = user.id!!,
                                nombre = user.nombre,
                                correo = user.correo,
                                rol = user.rol
                            )
                        )
                        TokenPreferences.guardarToken(context, token)
                        cargarMisInscripciones()
                        Log.d("SesionViewModel", "Usuario y token guardados correctamente")
                    }
                } else {
                    _error.value = "Error al obtener perfil: ${response.code()}"
                    Log.e("SesionViewModel", "Error HTTP: ${response.code()}")
                }
            } catch (e: Exception) {
                _error.value = "Excepción al obtener usuario: ${e.localizedMessage}"
                Log.e("SesionViewModel", "Excepción al obtener usuario", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Carga las inscripciones del usuario actual (una vez por sesión).
     */
    fun cargarMisInscripciones() {
        if (_inscripcionesCargadas) return

        viewModelScope.launch {
            try {
                val token = Firebase.auth.currentUser?.getIdToken(false)?.await()?.token ?: return@launch
                val usuarioId = _usuario.value?.id ?: run {
                    _error.value = "ID del usuario es nulo"
                    return@launch
                }

                val response = api.getInscripcionesPorUsuario("Bearer $token", usuarioId)
                if (response.isSuccessful) {
                    _inscripciones.value = response.body() ?: emptyList()
                    _inscripcionesCargadas = true
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

    fun cerrarSesion() {
        viewModelScope.launch {
            Firebase.auth.signOut()
            UsuarioPreferences.borrarUsuario(context)
            TokenPreferences.borrarToken(context)

            _usuario.value = null
            _inscripciones.value = emptyList()
            _error.value = null
            _isLoading.value = false
            _inscripcionesCargadas = false

            Log.d("SesionViewModel", "Sesión cerrada y datos persistentes borrados.")
        }
    }

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
                    obtenerUsuarioActual()
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
            fechaRegistro = null
        )
    }

    fun usuarioYaCargado(): Boolean {
        return usuario.value != null && usuario.value?.rol != Rol.INVITADO
    }

    fun setUsuario(usuario: Usuario) {
        _usuario.value = usuario
    }
}