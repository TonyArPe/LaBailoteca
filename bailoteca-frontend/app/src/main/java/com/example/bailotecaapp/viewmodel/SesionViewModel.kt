package com.example.bailotecaapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bailotecaapp.model.Inscripcion
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.model.dto.UsuarioUpdateRequest
import com.example.bailotecaapp.model.enums.Rol
import com.example.bailotecaapp.network.ApiService
import com.example.bailotecaapp.network.session.SesionManager
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.MultipartBody
import javax.inject.Inject

/**
 * ViewModel que centraliza la gestión de sesión del usuario.
 */
@HiltViewModel
class SesionViewModel @Inject constructor(
    application: Application,
    private val api: ApiService,
    private val sesionManager: SesionManager
) : AndroidViewModel(application) {

    val usuario = sesionManager.usuario
    val token = sesionManager.token
    val usuarioYaCargado = sesionManager.yaCargado

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _inscripciones = MutableStateFlow<List<Inscripcion>>(emptyList())
    val inscripciones = _inscripciones.asStateFlow()
    private var _inscripcionesCargadas = false

    private val _usuarioCargado = MutableStateFlow(false)
    val usuarioCargado: StateFlow<Boolean> = _usuarioCargado

    private val _versionClases = MutableStateFlow(0)
    val versionClases: StateFlow<Int> = _versionClases

    private val _sesionCerrada = MutableStateFlow(false)
    val sesionCerrada: StateFlow<Boolean> = _sesionCerrada

    private val _modoInvitado = MutableStateFlow(false)
    val modoInvitado: StateFlow<Boolean> = _modoInvitado

    private val _modoInvitadoForzado = MutableStateFlow(false)
    val modoInvitadoForzado: StateFlow<Boolean> = _modoInvitadoForzado

    fun obtenerUsuarioActualConToken(token: String) {
        if (usuarioYaCargado.value) {
            Log.d("SesionViewModel", "⛔ Usuario ya cargado, omitiendo nueva llamada a /me")
            return
        }
        _isLoading.value = true
        sesionManager.iniciarSesionConToken(token)
        viewModelScope.launch {
            sesionManager.usuario.filterNotNull().first {
                cargarMisInscripciones()
                _usuarioCargado.value = true
                _isLoading.value = false
                true
            }
        }
    }

    fun recuperarSesionDesdePreferencias() {
        _isLoading.value = true
        sesionManager.restaurarSesionDesdePreferencias()
        viewModelScope.launch {
            sesionManager.usuario.filterNotNull().first {
                if (sesionManager.estaSesionActiva()) {
                    cargarMisInscripciones()
                }
                _usuarioCargado.value = true
                _isLoading.value = false
                true
            }
        }
    }

    fun iniciarSesionConTokenYSincronizar(token: String) {
        _isLoading.value = true
        _error.value = null
        Log.d("SesionViewModel", "🚀 Iniciando sesión con token...")

        viewModelScope.launch {
            try {
                cerrarSesion()
                sesionManager.iniciarSesionConToken(token)

                sesionManager.usuario.filterNotNull().first()

                cargarMisInscripciones()
                sincronizarDesdeSesionManager()
                _usuarioCargado.value = true
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = "❌ Excepción al iniciar sesión: ${e.localizedMessage}"
                Log.e("SesionViewModel", "❌ Error al sincronizar sesión", e)
            }
        }
    }

    fun subirImagenPerfil(archivo: MultipartBody.Part, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val respuesta = api.subirArchivo(archivo)
                if (respuesta.isSuccessful) {
                    val nombre = respuesta.body()?.string()
                    Log.d("SesionViewModel", "✅ Imagen subida correctamente: $nombre")
                    nombre?.let { onSuccess(it) }
                } else {
                    Log.e("SesionViewModel", "❌ Error al subir imagen: ${respuesta.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("SesionViewModel", "❌ Excepción al subir imagen", e)
            }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            FirebaseAuth.getInstance().signOut()
            sesionManager.cerrarSesion()
            _inscripciones.value = emptyList()
            _inscripcionesCargadas = false
            _usuarioCargado.value = false
            _sesionCerrada.value = true
        }
    }

    fun entrarComoInvitado(onPropagado: (() -> Unit)? = null) {
        viewModelScope.launch {
            sesionManager.cerrarSesion()
            _modoInvitado.value = true
            _modoInvitadoForzado.value = true
            _usuarioCargado.value = true
            _inscripciones.value = emptyList()
            delay(100)
            onPropagado?.invoke()
        }
    }

    /**
     * Actualiza el perfil del usuario autenticado.
     */
    fun actualizarPerfil(
        usuarioActualizado: UsuarioUpdateRequest,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val tokenPersistido = sesionManager.token.value
                val userId = usuario.value?.id

                if (tokenPersistido == null || userId == null) {
                    val mensaje = "❌ Token o usuario nulo. ¿Se ha perdido la sesión?"
                    _error.value = mensaje
                    onError(mensaje)
                    return@launch
                }

                val response = api.actualizarUsuario(
                    token = "Bearer $tokenPersistido",
                    id = userId,
                    usuario = usuarioActualizado
                )

                if (response.isSuccessful) {
                    Log.d("SesionViewModel", "✅ Perfil actualizado correctamente")
                    response.body()?.let { actualizarUsuarioEnSesion(it) }
                    onSuccess()
                } else {
                    val mensaje = "❌ Error al actualizar perfil: ${response.code()}"
                    _error.value = mensaje
                    onError(mensaje)
                }

            } catch (e: Exception) {
                val mensaje = "⚠️ Excepción al actualizar perfil: ${e.localizedMessage}"
                _error.value = mensaje
                Log.e("SesionViewModel", mensaje, e)
                onError(mensaje)
            }
        }
    }

    private fun actualizarUsuarioEnSesion(usuario: Usuario) {
        viewModelScope.launch {
            sesionManager.guardarUsuario(usuario)
        }
    }

    fun cargarMisInscripciones() {
        if (_inscripcionesCargadas) return
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser ?: return@launch
                val token = currentUser.getIdToken(false).await().token ?: return@launch
                val usuarioId = usuario.value?.id ?: return@launch

                val response = api.getInscripcionesPorUsuario("Bearer $token", usuarioId)
                if (response.isSuccessful) {
                    _inscripciones.value = response.body() ?: emptyList()
                    _inscripcionesCargadas = true
                } else {
                    _error.value = "Error al cargar inscripciones: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Excepción al cargar inscripciones: ${e.message}"
            }
        }
    }

    fun sincronizarDesdeSesionManager() {
        viewModelScope.launch {
            val usuarioSesion = sesionManager.usuario.value
            val tokenSesion = sesionManager.token.value

            if (usuarioSesion != null && tokenSesion != null) {
                Log.d("SesionViewModel", "🔁 Sincronizando estado desde SesionManager: ${usuarioSesion.correo}")
                _usuarioCargado.value = true
                sesionManager.marcarUsuarioComoCargado()
            } else {
                Log.w("SesionViewModel", "⚠️ SesionManager sin usuario o token. No se puede sincronizar.")
            }
        }
    }

    fun marcarClasesComoActualizadas() {
        _versionClases.value++
    }

    fun actualizarEstadoCargado(valor: Boolean = true) {
        _usuarioCargado.value = valor
    }

    val sesionActiva: Boolean
        get() = usuario.value != null && usuario.value?.rol != Rol.INVITADO

    fun reiniciarEstadoSesion() {
        _sesionCerrada.value = false
        _modoInvitado.value = false
        _modoInvitadoForzado.value = false
        _usuarioCargado.value = false
        _inscripcionesCargadas = false
    }
}