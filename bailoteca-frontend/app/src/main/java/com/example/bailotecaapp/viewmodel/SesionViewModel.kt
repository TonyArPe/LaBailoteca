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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * ViewModel responsable de manejar la sesión del usuario, ya sea autenticado o invitado.
 * Gestiona el estado actual del usuario, la carga de inscripciones y el control de navegación.
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

    private val _versionClases = MutableStateFlow(0)
    val versionClases: StateFlow<Int> = _versionClases

    private val _sesionCerrada = MutableStateFlow(false)
    val sesionCerrada: StateFlow<Boolean> = _sesionCerrada

    private val _modoInvitadoForzado = MutableStateFlow(false)
    val modoInvitadoForzado: StateFlow<Boolean> = _modoInvitadoForzado

    private val _modoInvitado = MutableStateFlow(false)
    val modoInvitado: StateFlow<Boolean> = _modoInvitado

    private val _usuarioCargado = MutableStateFlow(false)
    val usuarioCargado: StateFlow<Boolean> = _usuarioCargado

    init {
        viewModelScope.launch {
            val usuarioGuardado = UsuarioPreferences.obtenerUsuario(context)
            val tokenGuardado = TokenPreferences.obtenerToken(context)

            if (usuarioGuardado != null && tokenGuardado != null && usuarioGuardado.rol != Rol.INVITADO) {
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
                obtenerUsuarioActual()
            }
        }
    }

    fun obtenerUsuarioActual() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = Firebase.auth.currentUser?.getIdToken(true)?.await()?.token
                if (token.isNullOrEmpty()) {
                    _error.value = "Token de autenticación vacío"
                    _usuarioCargado.value = true
                    return@launch
                }

                val response = api.getUsuarioActual("Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        _usuario.value = user
                        UsuarioPreferences.guardarUsuario(context, UsuarioPersistente(user.id!!, user.nombre, user.correo, user.rol))
                        TokenPreferences.guardarToken(context, token)
                        cargarMisInscripciones()
                    }
                } else {
                    _error.value = "Error al obtener perfil: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Excepción al obtener usuario: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
                _usuarioCargado.value = true
            }
        }
    }

    fun obtenerUsuarioActualConToken(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.getUsuarioActual("Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        _usuario.value = user
                        UsuarioPreferences.guardarUsuario(context, UsuarioPersistente(user.id!!, user.nombre, user.correo, user.rol))
                        TokenPreferences.guardarToken(context, token)
                        cargarMisInscripciones()
                        Log.d("SesionViewModel", "Usuario y token guardados correctamente")
                    }
                } else if (response.code() == 403) {
                    Log.w("SesionViewModel", "Usuario no encontrado en backend, iniciando como invitado")
                    entrarComoInvitado()
                } else {
                    _error.value = "Error al obtener perfil: ${response.code()}"
                    Log.e("SesionViewModel", "Error HTTP: ${response.code()}")
                }
            } catch (e: Exception) {
                _error.value = "Excepción al obtener usuario: ${e.localizedMessage}"
                Log.e("SesionViewModel", "Excepción al obtener usuario", e)
            } finally {
                _isLoading.value = false
                _usuarioCargado.value = true
            }
        }
    }

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
            _sesionCerrada.value = true

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

    /**
     * Establece un usuario invitado genérico y activa el modo invitado para navegación limitada.
     */
    fun entrarComoInvitado(onPropagado: (() -> Unit)? = null) {
        viewModelScope.launch {
            Log.d("SesionViewModel", "🌐 Estableciendo modo invitado...")

            _usuario.value = Usuario(
                id = -1,
                nombre = "Invitado",
                apellido = "",
                correo = "invitado@bailoteca.com",
                contrasenna = "",
                rol = Rol.INVITADO,
                fotoPerfil = null,
                telefono = null,
                direccion = null,
                fechaNacimiento = null,
                genero = null,
                dni = null,
                fechaRegistro = null,
                activo = false,
                pagado = false
            )

            delay(50)
            _modoInvitado.value = true
            _modoInvitadoForzado.value = true

            delay(200) // asegurar propagación Compose
            Log.d("SesionViewModel", "🚀 Modo invitado activado completamente.")
            onPropagado?.invoke()
        }
    }

    fun marcarClasesComoActualizadas() {
        _versionClases.value++
    }

    fun usuarioYaCargado(): Boolean {
        return usuario.value != null && usuario.value?.rol != Rol.INVITADO
    }

    fun setUsuario(usuario: Usuario) {
        _usuario.value = usuario
    }

    val sesionActiva: Boolean
        get() = _usuario.value != null && _usuario.value?.rol != Rol.INVITADO

    fun reiniciarEstadoSesion() {
        _sesionCerrada.value = false
        _modoInvitadoForzado.value = false
    }
}