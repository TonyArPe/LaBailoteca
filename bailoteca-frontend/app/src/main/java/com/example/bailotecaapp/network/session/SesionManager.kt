package com.example.bailotecaapp.network.session

import android.content.Context
import android.util.Log
import com.example.bailotecaapp.datastore.TokenPreferences
import com.example.bailotecaapp.datastore.UsuarioPersistente
import com.example.bailotecaapp.datastore.UsuarioPreferences
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.model.enums.Rol
import com.example.bailotecaapp.network.ApiService
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Clase encargada de gestionar el estado de sesión del usuario.
 *
 * Maneja el token JWT, el usuario actual y su persistencia utilizando DataStore. Además, permite:
 * - Restaurar sesión desde preferencias,
 * - Iniciar sesión con un token de Firebase,
 * - Cerrar sesión y eliminar los datos persistidos,
 * - Comprobar si hay una sesión activa.
 *
 * Esta clase se utiliza principalmente desde el ViewModel `SesionViewModel`.
 */
class SesionManager @Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext private val context: Context
) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> get() {
        if (_usuario.value == null) {
            restaurarSesionDesdePreferencias()
        }
        return _usuario
    }

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token

    private val _yaCargado = MutableStateFlow(false)
    val yaCargado: StateFlow<Boolean> = _yaCargado

    suspend fun getToken(): String? {
        token.value?.let {
            return it
        }
        return TokenPreferences.obtenerToken(context)?.also {
            _token.value = it
        }
    }

    suspend fun guardarToken(token: String) {
        TokenPreferences.guardarToken(context, token)
        _token.value = token
    }

    suspend fun guardarUsuario(usuario: Usuario) {
        try {
            val persistente = UsuarioPersistente(
                id = usuario.id ?: -1,
                nombre = usuario.nombre,
                apellido = usuario.apellido,
                correo = usuario.correo,
                rol = usuario.rol,
                fotoPerfil = usuario.fotoPerfil,
                telefono = usuario.telefono,
                direccion = usuario.direccion,
                fechaNacimiento = usuario.fechaNacimiento,
                genero = usuario.genero,
                dni = usuario.dni,
                fechaRegistro = usuario.fechaRegistro,
                activo = usuario.activo,
                pagado = usuario.pagado
            )

            UsuarioPreferences.guardarUsuario(context, persistente)
            _usuario.value = usuario

            Log.d("SesionManager", "✅ Usuario guardado correctamente: ${usuario.correo}")
        } catch (e: Exception) {
            Log.e("SesionManager", "❌ Error al guardar usuario: ${e.message}", e)
        }
    }

    suspend fun borrarToken() {
        TokenPreferences.borrarToken(context)
        _token.value = null
    }

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
                    Log.d("SesionManager", "📦 Usuario recuperado de preferencias: $savedUsuario")
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
     */
    fun iniciarSesionConToken(tokenNuevo: String) {
        coroutineScope.launch {
            try {
                TokenPreferences.guardarToken(context, tokenNuevo)
                _token.value = tokenNuevo

                val usuarioApi = apiService.obtenerUsuarioActual("Bearer $tokenNuevo")

                val persistente = UsuarioPersistente(
                    id = usuarioApi.id ?: -1,
                    nombre = usuarioApi.nombre ?: "",
                    apellido = usuarioApi.apellido ?: "",
                    correo = usuarioApi.correo ?: "",
                    rol = usuarioApi.rol,
                    fotoPerfil = usuarioApi.fotoPerfil ?: "",
                    telefono = usuarioApi.telefono ?: "",
                    direccion = usuarioApi.direccion ?: "",
                    fechaNacimiento = usuarioApi.fechaNacimiento,
                    genero = usuarioApi.genero ?: "",
                    dni = usuarioApi.dni ?: "",
                    fechaRegistro = usuarioApi.fechaRegistro,
                    activo = usuarioApi.activo,
                    pagado = usuarioApi.pagado
                )
                UsuarioPreferences.guardarUsuario(context, persistente)
                _usuario.value = persistente.toUsuario()

                Log.d("SesionManager", "✅ Sesión iniciada con ${usuarioApi.correo}")

                _yaCargado.value = true
            } catch (e: Exception) {
                Log.e("SesionManager", "❌ Error al iniciar sesión: ${e.message}", e)
                _yaCargado.value = true
            }
        }
    }

    suspend fun renovarTokenFirebaseSiHaCambiado() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            try {
                val tokenNuevo = user.getIdToken(true).await().token
                if (tokenNuevo != null && tokenNuevo != token.value) {
                    Log.d("SesionManager", "🔁 Token actualizado desde Firebase")
                    guardarToken(tokenNuevo)
                }
            } catch (e: Exception) {
                Log.e("SesionManager", "❌ Error al renovar token Firebase: ${e.message}", e)
            }
        }
    }

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

    fun marcarUsuarioComoCargado() {
        _yaCargado.value = true
    }

    fun estaSesionActiva(): Boolean {
        return _usuario.value != null && _token.value != null
    }

    fun getUsuarioActual(): Usuario? {
        return _usuario.value
    }

}

fun UsuarioPersistente.toUsuario(): Usuario {
    return Usuario(
        id = this.id,
        nombre = this.nombre,
        apellido = this.apellido ?: "",
        correo = this.correo,
        contrasenna = "",
        rol = this.rol,
        fotoPerfil = this.fotoPerfil,
        telefono = this.telefono,
        direccion = this.direccion,
        fechaNacimiento = this.fechaNacimiento,
        genero = this.genero,
        dni = this.dni,
        fechaRegistro = this.fechaRegistro,
        activo = this.activo,
        pagado = this.pagado
    )
}