package com.example.bailotecaapp.network.session

import android.content.Context
import android.util.Log
import androidx.compose.runtime.remember
import com.example.bailotecaapp.datastore.TokenPreferences
import com.example.bailotecaapp.datastore.UsuarioPersistente
import com.example.bailotecaapp.datastore.UsuarioPreferences
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.navigation.Screens
import com.example.bailotecaapp.network.ApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Clase centralizada para gestionar el estado de sesión del usuario.
 *
 * Se encarga de manejar el token JWT, el usuario actual y su persistencia
 * usando DataStore. Además, permite restaurar sesión, cerrarla y marcar
 * que ya fue cargada correctamente.
 *
 * Esta clase es utilizada por el ViewModel `SesionViewModel`.
 */
class SesionManager @Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext private val context: Context
) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token

    private val _yaCargado = MutableStateFlow(false)
    val yaCargado: StateFlow<Boolean> = _yaCargado

    /**
     * Devuelve el token JWT actualmente almacenado en memoria.
     * Si no está en memoria, lo intenta cargar desde DataStore.
     *
     * @return Token JWT o null si no existe.
     */
    suspend fun getToken(): String? {
        // Devuelve token en memoria si existe
        token.value?.let {
            return it
        }

        // Si no hay token en memoria, lo intenta restaurar desde preferencias
        return TokenPreferences.obtenerToken(context)?.also {
            _token.value = it
        }
    }

    suspend fun guardarToken(token: String) {
        TokenPreferences.guardarToken(context, token)
        _token.value = token
    }

    suspend fun borrarToken() {
        TokenPreferences.borrarToken(context)
        _token.value = null
    }

    /**
     * Restaura la sesión desde DataStore si existen token y usuario guardados.
     *
     * Se utiliza normalmente al iniciar la app para intentar restablecer el estado
     * previo de la sesión sin depender de Firebase.
     */
    fun restaurarSesionDesdePreferencias() {
        coroutineScope.launch {
            try {
                val savedToken = TokenPreferences.obtenerToken(context)
                val savedUsuario = UsuarioPreferences.obtenerUsuario(context)

                if (savedToken != null && savedUsuario != null) {
                    Log.d("SesionManager", "🔁 Restaurando sesión desde preferencias con ${savedUsuario.correo}")
                    _token.value = savedToken
                    _usuario.value = savedUsuario.toUsuario()
                    _yaCargado.value = true
                } else {
                    Log.d("SesionManager", "⚠️ No se encontró usuario/token en preferencias")
                    _yaCargado.value = false
                }
            } catch (e: Exception) {
                Log.e("SesionManager", "❌ Error al restaurar sesión: ${e.message}", e)
                _yaCargado.value = false
            }
        }
    }

    /**
     * Realiza login con token (JWT), obtiene el usuario desde el backend
     * y guarda sus datos en DataStore.
     *
     * @param tokenNuevo Token JWT recibido desde Firebase.
     */
    fun iniciarSesionConToken(tokenNuevo: String) {
        coroutineScope.launch {
            try {
                _token.value = tokenNuevo
                TokenPreferences.guardarToken(context, tokenNuevo)

                val response = apiService.getUsuarioActual("Bearer $tokenNuevo")
                if (response.isSuccessful) {
                    val usuarioApi = response.body()
                    if (usuarioApi != null) {
                        _usuario.value = usuarioApi

                        UsuarioPreferences.guardarUsuario(
                            context,
                            UsuarioPersistente(
                                id = usuarioApi.id ?: -1,
                                nombre = usuarioApi.nombre,
                                apellido = usuarioApi.apellido,
                                correo = usuarioApi.correo,
                                rol = usuarioApi.rol,
                                fotoPerfil = usuarioApi.fotoPerfil,
                                telefono = usuarioApi.telefono,
                                direccion = usuarioApi.direccion,
                                fechaNacimiento = usuarioApi.fechaNacimiento,
                                genero = usuarioApi.genero,
                                dni = usuarioApi.dni,
                                fechaRegistro = usuarioApi.fechaRegistro,
                                activo = usuarioApi.activo,
                                pagado = usuarioApi.pagado
                            )
                        )

                        _yaCargado.value = true
                        Log.d("SesionManager", "✅ Sesión iniciada con ${usuarioApi.correo}")
                    } else {
                        Log.e("SesionManager", "⚠️ Respuesta sin cuerpo al iniciar sesión")
                        _yaCargado.value = false
                    }
                } else {
                    Log.e("SesionManager", "❌ Error al obtener usuario: ${response.code()}")
                    _yaCargado.value = false
                }
            } catch (e: Exception) {
                Log.e("SesionManager", "❌ Error al iniciar sesión: ${e.message}", e)
                _yaCargado.value = false
            }
        }
    }

    /**
     * Cierra la sesión actual, eliminando token y usuario tanto de memoria
     * como de DataStore.
     */
    fun cerrarSesion() {
        coroutineScope.launch {
            TokenPreferences.borrarToken(context)
            UsuarioPreferences.borrarUsuario(context)
            _usuario.value = null
            _token.value = null
            _yaCargado.value = false
            Log.d("SesionManager", "🔒 Sesión cerrada correctamente")
        }
    }

    /**
     * Marca que el usuario ha sido correctamente cargado (usado por el ViewModel).
     * Esto permite evitar múltiples llamadas innecesarias al backend si ya se tiene
     * un estado válido en memoria.
     */
    fun marcarUsuarioComoCargado() {
        _yaCargado.value = true
    }

    /**
     * Comprueba si actualmente hay una sesión activa en memoria.
     *
     * @return true si tanto el usuario como el token están presentes.
     */
    fun estaSesionActiva(): Boolean {
        return _usuario.value != null && _token.value != null
    }
}

/**
 * Extensión para convertir un `UsuarioPersistente` guardado en DataStore
 * en un `Usuario` completo, útil al restaurar sesión.
 */
fun UsuarioPersistente.toUsuario(): Usuario {
    return Usuario(
        id = this.id,
        nombre = this.nombre,
        apellido = this.apellido.toString(),
        correo = this.correo,
        contrasenna = "",
        rol = this.rol,
        activo = true,
        pagado = false
    )
}