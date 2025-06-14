package com.example.bailotecaapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bailotecaapp.model.Inscripcion
import com.example.bailotecaapp.model.dto.UsuarioUpdateRequest
import com.example.bailotecaapp.model.enums.Rol
import com.example.bailotecaapp.network.ApiService
import com.example.bailotecaapp.network.session.SesionManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.MultipartBody
import javax.inject.Inject

/**
 * ViewModel principal que gestiona la sesión del usuario en la aplicación Bailoteca.
 * Encargado de iniciar sesión, cerrar sesión, restaurar sesiones previas y gestionar inscripciones.
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

    val yaCargado = sesionManager.yaCargado

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
                Log.d("SesionViewModel", "📦 usuario restaurado: ${it.correo}")
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
                // 🔁 IMPORTANTE: Limpiar sesión anterior antes de iniciar una nueva
                cerrarSesion() // <--- esta línea es clave

                // Guardar token localmente
                sesionManager.iniciarSesionConToken(token)
                Log.d("SesionViewModel", "🔐 Token inyectado en SesionManager")

                // Esperar a que se propague y recuperar usuario
                val usuarioRecuperado = sesionManager.usuario.filterNotNull().first()

                Log.d("SesionViewModel", "✅ Usuario recuperado: ${usuarioRecuperado.correo}")

                // Cargar inscripciones del usuario si procede
                cargarMisInscripciones()

                // Confirmar sincronización
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
                    val nombre = respuesta.body()
                    Log.d("SesionViewModel", "✅ Imagen subida correctamente: $nombre")
                    nombre?.let { onSuccess(it) }
                } else {
                    Log.e("SesionViewModel", "❌ Error al subir imagen de perfil: ${respuesta.errorBody()}")
                }
            } catch (e: Exception) {
                Log.e("SesionViewModel", "❌ Excepción al subir imagen", e)
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

    fun cerrarSesion() {
        viewModelScope.launch {
            Firebase.auth.signOut()
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
     *
     * @param usuarioActualizado Objeto con los datos modificados del usuario.
     * @param onSuccess Callback si la actualización fue exitosa.
     * @param onError Callback con mensaje si hubo error.
     */
    fun actualizarPerfil(
        usuarioActualizado: UsuarioUpdateRequest,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val currentUser = Firebase.auth.currentUser
                val token = currentUser?.getIdToken(true)?.await()?.token
                if (token == null) {
                    val mensaje = "❌ Token Firebase nulo, no se puede continuar"
                    _error.value = mensaje
                    Log.e("SesionViewModel", mensaje)
                    onError(mensaje)
                    return@launch
                }
                val userId = usuario.value?.id ?: return@launch

                val response = api.actualizarUsuario("Bearer $token", userId, usuarioActualizado)

                if (response.isSuccessful) {
                    sesionManager.iniciarSesionConToken(token)
                    onSuccess()
                } else {
                    val mensaje = "❌ Error ${response.code()}: ${response.message()}"
                    Log.e("SesionViewModel", mensaje)
                    _error.value = mensaje
                    onError(mensaje)
                }

            } catch (e: Exception) {
                val mensaje = "⚠️ Excepción al actualizar perfil: ${e.localizedMessage}"
                Log.e("SesionViewModel", mensaje, e)
                _error.value = mensaje
                onError(mensaje)
            }
        }
    }

    fun cargarMisInscripciones() {
        if (_inscripcionesCargadas) return
        viewModelScope.launch {
            try {
                val currentUser = Firebase.auth.currentUser ?: return@launch
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