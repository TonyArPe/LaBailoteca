package com.example.bailotecaapp.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bailotecaapp.datastore.UsuarioPersistente
import com.example.bailotecaapp.datastore.UsuarioPreferences
import com.example.bailotecaapp.model.Inscripcion
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.model.dto.UsuarioUpdateRequest
import com.example.bailotecaapp.model.enums.Rol
import com.example.bailotecaapp.network.ApiService
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * ViewModel que gestiona el estado de la sesión del usuario actual,
 * incluyendo su autenticación, persistencia, restauración y sus inscripciones.
 *
 * @property apiService Cliente HTTP que comunica con el backend.
 * @constructor Inyectado por Hilt con acceso a [Application].
 */
@HiltViewModel
class SesionViewModel @Inject constructor(
    application: Application,
    private val apiService: ApiService
) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario = _usuario.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _logoutEvent = MutableStateFlow(false)
    val logoutEvent = _logoutEvent.asStateFlow()

    private val _inscripciones = MutableStateFlow<List<Inscripcion>>(emptyList())
    val inscripciones = _inscripciones.asStateFlow()

    private var inscripcionesYaCargadas = false
    private var usuarioYaCargado = false
    private var tokenYaUsado = false

    init {
        restaurarDesdePreferencias()
    }

    /**
     * Inicializa la sesión: si ya se ha hecho, ignora la llamada.
     * De lo contrario, intenta obtener el token Firebase y cargar el usuario.
     */
    fun inicializarSesion() {
        if (usuarioYaCargado || tokenYaUsado) {
            Log.d("SesionViewModel", "Sesión ya inicializada, se omite")
            return
        }

        viewModelScope.launch {
            try {
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                val token = firebaseUser?.getIdToken(true)?.await()?.token

                if (!token.isNullOrBlank()) {
                    Log.d("SesionViewModel", "Token válido, obteniendo usuario")
                    tokenYaUsado = true
                    obtenerUsuarioActualConToken(token)
                } else {
                    Log.d("SesionViewModel", "Token nulo o usuario no autenticado, modo invitado")
                    entrarComoInvitado()
                    usuarioYaCargado = true
                }
            } catch (e: Exception) {
                Log.e("SesionViewModel", "Error al inicializar sesión", e)
                entrarComoInvitado()
            }
        }
    }

    /**
     * Solicita al backend el usuario actual autenticado.
     * Si tiene éxito, guarda el usuario y sus inscripciones.
     */
    fun obtenerUsuarioActualConToken(token: String) {
        if (usuarioYaCargado) {
            Log.d("SesionViewModel", "Usuario ya cargado, no se vuelve a solicitar")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val response = apiService.getUsuarioActual("Bearer $token")
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        _usuario.value = user
                        usuarioYaCargado = true
                        inscripcionesYaCargadas = false

                        UsuarioPreferences.guardarUsuario(
                            context,
                            UsuarioPersistente(
                                id = user.id,
                                nombre = user.nombre,
                                apellido = user.apellido,
                                correo = user.correo,
                                rol = user.rol
                            )
                        )

                        obtenerInscripciones()
                    }
                } else {
                    _error.value = "Error al cargar usuario: código ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Excepción al cargar usuario: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Establece el usuario como invitado (sesión anónima).
     */
    fun entrarComoInvitado() {
        _usuario.value = Usuario(
            id = -1,
            nombre = "Invitado",
            apellido = "",
            correo = "invitado@bailoteca.com",
            contrasenna = "",
            rol = Rol.INVITADO,
            telefono = "",
            direccion = "",
            fechaNacimiento = "",
            fechaRegistro = "",
            activo = true,
            pagado = false,
            fotoPerfil = "",
            dni = "",
            genero = ""
        )
    }

    /**
     * Cierra la sesión actual, borra el usuario persistido
     * y emite evento de logout.
     */
    fun cerrarSesion() {
        viewModelScope.launch {
            UsuarioPreferences.borrarUsuario(context)
            _usuario.value = null
            _logoutEvent.value = true
            inscripcionesYaCargadas = false
            usuarioYaCargado = false
            tokenYaUsado = false
        }
    }

    /**
     * Resetea el flag de logout para evitar redirecciones redundantes.
     */
    fun resetLogoutEvent() {
        _logoutEvent.value = false
    }

    /**
     * Restaura la sesión desde preferencias guardadas (usuario persistente).
     */
    private fun restaurarDesdePreferencias() {
        viewModelScope.launch {
            val u = UsuarioPreferences.obtenerUsuario(context)
            if (u != null) {
                _usuario.value = Usuario(
                    id = u.id,
                    nombre = u.nombre,
                    apellido = u.apellido,
                    correo = u.correo,
                    contrasenna = "",
                    rol = u.rol,
                    telefono = "",
                    direccion = "",
                    fechaNacimiento = "",
                    fechaRegistro = "",
                    activo = true,
                    pagado = false,
                    fotoPerfil = "",
                    dni = "",
                    genero = ""
                )

                // Cargar inscripciones restauradas
                obtenerInscripciones()
            }
        }
    }

    fun actualizarPerfil(
        usuarioActualizado: UsuarioUpdateRequest,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val token = com.google.firebase.auth.FirebaseAuth.getInstance()
                    .currentUser?.getIdToken(false)?.await()?.token

                if (token.isNullOrEmpty()) {
                    onError("Token no disponible")
                    return@launch
                }

                val idUsuario = _usuario.value?.id ?: run {
                    onError("Usuario no disponible")
                    return@launch
                }

                val response = apiService.actualizarUsuario(
                    token = "Bearer $token",
                    id = idUsuario,
                    usuario = usuarioActualizado
                )

                if (response.isSuccessful) {
                    val usuarioResponse = response.body()
                    if (usuarioResponse != null) {
                        _usuario.value = usuarioResponse
                        onSuccess()
                    } else {
                        onError("Respuesta vacía del servidor")
                    }
                } else {
                    onError("Error del servidor: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("SesionViewModel", "Error actualizando perfil", e)
                onError("Excepción: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }


    /**
     * Obtiene las inscripciones del usuario actual desde el backend.
     * Protegido contra múltiples llamadas innecesarias.
     */
    fun obtenerInscripciones() {
        // Prevenir múltiples llamadas incluso si usuario cambia
        if (inscripcionesYaCargadas || usuario.value == null || usuario.value?.rol == Rol.INVITADO) {
            Log.d("SesionViewModel", "Inscripciones ya cargadas o no necesarias")
            return
        }

        viewModelScope.launch {
            Log.d("SesionViewModel", "Solicitando inscripciones...")
            try {
                val token = FirebaseAuth.getInstance().currentUser
                    ?.getIdToken(false)?.await()?.token

                if (!token.isNullOrEmpty()) {
                    val response = apiService.getInscripcionesDelUsuario("Bearer $token")

                    if (response.isSuccessful) {
                        _inscripciones.value = response.body() ?: emptyList()
                        inscripcionesYaCargadas = true
                        Log.d("SesionViewModel", "Inscripciones cargadas correctamente.")
                    } else {
                        Log.e("SesionViewModel", "Error HTTP: ${response.code()}")
                    }
                } else {
                    Log.e("SesionViewModel", "Token Firebase nulo")
                }
            } catch (e: Exception) {
                Log.e("SesionViewModel", "Error al obtener inscripciones", e)
            }
        }
    }

    /**
     * Devuelve `true` si el usuario ya ha sido cargado (y no es INVITADO).
     */
    fun usuarioYaCargado(): Boolean {
        return usuario.value != null && usuario.value?.rol != Rol.INVITADO
    }
}