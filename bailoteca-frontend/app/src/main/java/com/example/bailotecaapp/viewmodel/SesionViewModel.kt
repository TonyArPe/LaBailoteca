package com.example.bailotecaapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import com.example.bailotecaapp.model.Inscripcion
import com.example.bailotecaapp.model.dto.UsuarioUpdateRequest
import com.example.bailotecaapp.model.enums.Rol
import com.example.bailotecaapp.network.ApiService
import com.example.bailotecaapp.network.session.SesionManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * ViewModel principal que gestiona la sesión del usuario.
 * Se apoya en SesionManager para centralizar token/usuario.
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
            delay(500)
            cargarMisInscripciones()
            _usuarioCargado.value = true
            _isLoading.value = false
        }
    }

    fun recuperarSesionDesdePreferencias() {
        sesionManager.restaurarSesionDesdePreferencias()
        viewModelScope.launch {
            delay(500)
            if (sesionManager.estaSesionActiva()) {
                cargarMisInscripciones()
                _usuarioCargado.value = true
            }
        }
    }

    fun iniciarSesionConTokenYSincronizar(token: String) {
        viewModelScope.launch {
            sesionManager.iniciarSesionConToken(token)

            sesionManager.usuario
                .filterNotNull()
                .first {
                    sincronizarDesdeSesionManager()
                    true
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
            sesionManager.cerrarSesion() // limpiamos
            _modoInvitado.value = true
            _modoInvitadoForzado.value = true
            _usuarioCargado.value = true
            _inscripciones.value = emptyList()
            delay(100)
            onPropagado?.invoke()
        }
    }

    fun actualizarPerfil(
        usuarioActualizado: UsuarioUpdateRequest,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val currentUser = Firebase.auth.currentUser
                val token = currentUser?.getIdToken(true)?.await()?.token ?: return@launch
                val userId = usuario.value?.id ?: return@launch

                val response = api.actualizarUsuario("Bearer $token", userId, usuarioActualizado)
                if (response.isSuccessful) {
                    sesionManager.iniciarSesionConToken(token)
                    onSuccess()
                } else {
                    val mensaje = "Error ${response.code()}: ${response.message()}"
                    _error.value = mensaje
                    onError(mensaje)
                }
            } catch (e: Exception) {
                val mensaje = "Excepción: ${e.localizedMessage}"
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