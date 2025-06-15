package com.example.bailotecaapp.network.session

import android.content.Context
import android.util.Log
import com.example.bailotecaapp.datastore.TokenPreferences
import com.example.bailotecaapp.datastore.UsuarioPersistente
import com.example.bailotecaapp.datastore.UsuarioPreferences
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.network.ApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Gestor centralizado de la sesión del usuario, integrado con Hilt.
 * Encargado de:
 * - Guardar/restaurar token y usuario desde DataStore
 * - Inicializar sesión al obtener token de Firebase
 * - Cerrar sesión limpiamente
 * - Exponer estado observable de sesión
 */
class SesionManager @Inject constructor(
    private val apiService: ApiService,
    private val usuarioPreferences: UsuarioPreferences,
    @ApplicationContext private val context: Context
) {

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token

    private val _yaCargado = MutableStateFlow(false)
    val yaCargado: StateFlow<Boolean> = _yaCargado

    /**
     * Guarda el token JWT en preferencias y memoria.
     */
    suspend fun guardarToken(token: String) {
        TokenPreferences.guardarToken(context, token)
        _token.value = token
        Log.d("SesionManager", "🔐 Token guardado: ${token.take(10)}...")
    }

    /**
     * Devuelve el usuario actualmente guardado en DataStore, si existe.
     */
    suspend fun obtenerUsuario(): Usuario? {
        return usuarioPreferences.obtenerUsuario()
    }

    /**
     * Guarda el usuario completo en preferencias y memoria.
     */
    suspend fun guardarUsuario(usuario: Usuario) {
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

        usuarioPreferences.guardarUsuario(persistente)
        _usuario.value = persistente.toUsuario()
        Log.d("SesionManager", "✅ Usuario guardado correctamente: ${usuario.correo}")
    }

    /**
     * Restaura el token y usuario desde DataStore al iniciar la app.
     */
    suspend fun restaurarSesionDesdePreferencias() {
        withContext(Dispatchers.IO) {
            val savedToken = TokenPreferences.obtenerToken(context)
            val savedUsuario = usuarioPreferences.obtenerUsuario()

            if (savedToken != null && savedUsuario != null) {
                Log.d("SesionManager", "🔁 Restaurando sesión desde preferencias con ${savedUsuario.correo}")
                _token.value = savedToken
                _usuario.value = savedUsuario
                _yaCargado.value = true
            } else {
                Log.w("SesionManager", "⚠️ No se encontró token o usuario en preferencias")
                _yaCargado.value = false
            }
        }
    }

    /**
     * Inicia sesión usando el token JWT. Consulta el backend y guarda usuario/token.
     */
    suspend fun iniciarSesionConTokenSuspend(tokenNuevo: String) {
        try {
            guardarToken(tokenNuevo)
            val usuarioApi = apiService.obtenerUsuarioActual("Bearer $tokenNuevo")
            guardarUsuario(usuarioApi)
            _yaCargado.value = true
            Log.d("SesionManager", "✅ Sesión iniciada correctamente con ${usuarioApi.correo}")
        } catch (e: Exception) {
            Log.e("SesionManager", "❌ Error al iniciar sesión con token: ${e.message}", e)
            cerrarSesion()
        }
    }

    /**
     * Cierra la sesión borrando usuario y token.
     */
    suspend fun cerrarSesion() {
        withContext(Dispatchers.IO) {
            TokenPreferences.borrarToken(context)
            usuarioPreferences.borrarUsuario()
            _token.value = null
            _usuario.value = null
            _yaCargado.value = false
            Log.d("SesionManager", "🔒 Sesión cerrada correctamente")
        }
    }

    /**
     * Devuelve true si hay usuario y token en memoria.
     */
    fun estaSesionActiva(): Boolean = _token.value != null && _usuario.value != null

    /**
     * Acceso directo al usuario actual en memoria.
     */
    fun getUsuarioActual(): Usuario? = _usuario.value

    /**
     * Devuelve el token actual desde memoria o desde DataStore si aún no está cargado.
     */
    suspend fun getToken(): String? {
        token.value?.let {
            return it
        }

        return withContext(Dispatchers.IO) {
            TokenPreferences.obtenerToken(context)?.also {
                _token.value = it
                Log.d("SesionManager", "🔄 Token restaurado desde DataStore")
            }
        }
    }
}

/**
 * Convierte un usuario persistente a un objeto de dominio.
 */
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