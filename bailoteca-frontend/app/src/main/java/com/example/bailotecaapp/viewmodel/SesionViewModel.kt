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
import com.google.firebase.auth.FirebaseAuth
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

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _inscripciones = MutableStateFlow<List<Inscripcion>>(emptyList())
    val inscripciones: StateFlow<List<Inscripcion>> = _inscripciones

    private var _inscripcionesCargadas = false
    private var usuarioYaCargadoFlag = false // ← NUEVO FLAG

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
            val context = getApplication<Application>().applicationContext
            val usuarioGuardado = UsuarioPreferences.obtenerUsuario(context)
            val tokenGuardado = TokenPreferences.obtenerToken(context)

            if (usuarioGuardado == null || tokenGuardado == null) {
                _modoInvitado.value = false
                _modoInvitadoForzado.value = false
                _usuario.value = null
                return@launch
            }

            if (usuarioGuardado.rol != Rol.INVITADO) {
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

    fun obtenerUsuarioActualConToken(token: String) {
        if (usuarioYaCargadoFlag) {
            Log.d("SesionViewModel", "🔁 Usuario ya cargado, se omite llamada a obtenerUsuarioActualConToken()")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val context = getApplication<Application>().applicationContext
                val response = api.getUsuarioActual("Bearer $token")

                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        usuarioYaCargadoFlag = true
                        propagarCambioSesion(user, token)
                        cargarMisInscripciones()
                    }
                } else if (response.code() == 403) {
                    entrarComoInvitado()
                } else {
                    _error.value = "Error al obtener perfil: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Excepción: ${e.message}"
            } finally {
                _isLoading.value = false
                _usuarioCargado.value = true
            }
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

    private suspend fun propagarCambioSesion(user: Usuario, token: String) {
        val context = getApplication<Application>().applicationContext
        _usuario.value = user
        UsuarioPreferences.guardarUsuario(context, UsuarioPersistente(user.id!!, user.nombre, user.correo, user.rol))
        TokenPreferences.guardarToken(context, token)
        _modoInvitado.value = false
        _modoInvitadoForzado.value = false
    }

    fun cargarMisInscripciones() {
        if (_inscripcionesCargadas) return

        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val token = Firebase.auth.currentUser?.getIdToken(false)?.await()?.token ?: return@launch
                val usuarioId = _usuario.value?.id ?: return@launch
                val response = api.getInscripcionesPorUsuario("Bearer $token", usuarioId)

                if (response.isSuccessful) {
                    _inscripciones.value = response.body() ?: emptyList()
                    _inscripcionesCargadas = true
                } else {
                    _error.value = "Error inscripciones: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Excepción al cargar inscripciones: ${e.message}"
            }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            Firebase.auth.signOut()
            UsuarioPreferences.borrarUsuario(context)
            TokenPreferences.borrarToken(context)

            _usuario.value = null
            _inscripciones.value = emptyList()
            _inscripcionesCargadas = false
            _sesionCerrada.value = true
            usuarioYaCargadoFlag = false
        }
    }

    fun entrarComoInvitado(onPropagado: (() -> Unit)? = null) {
        viewModelScope.launch {
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
            delay(50)
            _modoInvitadoForzado.value = true
            delay(100)
            onPropagado?.invoke()
        }
    }

    fun marcarClasesComoActualizadas() {
        _versionClases.value++
    }

    fun usuarioYaCargado(): Boolean {
        return usuarioYaCargadoFlag
    }

    fun actualizarEstadoCargado(valor: Boolean = true) {
        _usuarioCargado.value = valor
    }

    fun obtenerUsuarioActual() {
        viewModelScope.launch {
            try {
                val token = FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.await()?.token
                if (!token.isNullOrBlank()) {
                    val response = api.getUsuarioActual("Bearer $token")

                    if (response.isSuccessful) {
                        response.body()?.let { user ->
                            setUsuario(user)
                            actualizarEstadoCargado(true)
                        }
                    } else {
                        _error.value = "Error al obtener usuario actual: ${response.code()}"
                    }
                }
            } catch (e: Exception) {
                Log.e("SesionViewModel", "❌ Error al obtener usuario actual: ${e.message}")
            }
        }
    }

    fun setUsuario(usuario: Usuario) {
        _usuario.value = usuario
    }

    val sesionActiva: Boolean
        get() = _usuario.value != null && _usuario.value?.rol != Rol.INVITADO

    fun reiniciarEstadoSesion() {
        _sesionCerrada.value = false
        _modoInvitadoForzado.value = false
        _usuarioCargado.value = false
        usuarioYaCargadoFlag = false
        _inscripcionesCargadas = false
    }

    /**
     * Intenta recuperar sesión desde DataStore si hay usuario y token persistidos.
     */
    fun recuperarSesionDesdePreferencias() {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            val usuarioPersistente = UsuarioPreferences.obtenerUsuario(context)
            val token = TokenPreferences.obtenerToken(context)

            if (usuarioPersistente != null && token != null) {
                Log.d("SesionViewModel", "🧠 Restaurando sesión desde preferencias: ${usuarioPersistente.correo}")
                try {
                    val response = api.getUsuarioActual("Bearer $token")
                    if (response.isSuccessful) {
                        response.body()?.let { user ->
                            propagarCambioSesion(user, token)
                            cargarMisInscripciones()
                            _usuarioCargado.value = true
                            usuarioYaCargadoFlag = true
                        }
                    } else {
                        Log.e("SesionViewModel", "⚠️ Error al restaurar sesión: ${response.code()}")
                        cerrarSesion()
                    }
                } catch (e: Exception) {
                    Log.e("SesionViewModel", "❌ Excepción al restaurar sesión: ${e.message}")
                    cerrarSesion()
                }
            } else {
                Log.d("SesionViewModel", "ℹ️ No hay usuario persistido")
            }
        }
    }

}