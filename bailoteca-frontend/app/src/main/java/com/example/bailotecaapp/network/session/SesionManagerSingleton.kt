package com.example.bailotecaapp.network.session

import android.content.Context
import android.util.Log
import com.example.bailotecaapp.datastore.TokenPreferences
import com.example.bailotecaapp.datastore.UsuarioPersistente
import com.example.bailotecaapp.datastore.UsuarioPreferences
import com.example.bailotecaapp.model.Usuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Singleton que gestiona la sesión del usuario en memoria y sincroniza con DataStore.
 *
 * No guarda contexto como propiedad global. Todas las funciones lo reciben como argumento.
 */
object SesionManagerSingleton {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario

    private val _yaCargado = MutableStateFlow(false)
    val yaCargado: StateFlow<Boolean> = _yaCargado

    private val _tokenExpirationTime = MutableStateFlow<Long?>(null)
    val tokenExpirationTime: StateFlow<Long?> = _tokenExpirationTime

    /**
     * Restaura token y usuario desde DataStore. Llamar al iniciar la app.
     */
    fun restaurarSesionDesdePreferencias(context: Context) {
        coroutineScope.launch {
            try {
                val savedToken = TokenPreferences.obtenerToken(context)
                val expirationTime = TokenPreferences.obtenerExpiracion(context)
                val savedUsuario = UsuarioPreferences.obtenerUsuario(context)

                if (savedToken != null && savedUsuario != null) {
                    Log.d("SesionManager", "✅ Sesión restaurada desde preferencias: ${savedUsuario.correo}")
                    _token.value = savedToken
                    _usuario.value = savedUsuario.toUsuarioCompat()
                    _tokenExpirationTime.value = expirationTime
                    _yaCargado.value = true
                } else {
                    Log.w("SesionManager", "⚠️ No se encontró token o usuario persistido")
                    _yaCargado.value = false
                }
            } catch (e: Exception) {
                Log.e("SesionManager", "❌ Error al restaurar sesión: ${e.message}", e)
                _yaCargado.value = false
            }
        }
    }

    /**
     * Guarda un token nuevo con tiempo de expiración.
     */
    fun guardarTokenConExpiracion(context: Context, nuevoToken: String, expirationTimeMs: Long) {
        coroutineScope.launch {
            TokenPreferences.guardarToken(context, nuevoToken)
            TokenPreferences.guardarExpiracion(context, expirationTimeMs)
            _token.value = nuevoToken
            _tokenExpirationTime.value = expirationTimeMs
            Log.d("SesionManager", "🔐 Token y expiración actualizados en memoria y preferencias")
        }
    }

    /**
     * Determina si el token está próximo a expirar (menos de 5 minutos).
     */
    fun deberiaRenovarToken(): Boolean {
        val now = System.currentTimeMillis()
        val expiration = _tokenExpirationTime.value
        return expiration != null && expiration - now < 5 * 60 * 1000
    }

    /**
     * Guarda el nuevo token en memoria y preferencias (sin expiración).
     */
    fun guardarToken(context: Context, nuevoToken: String) {
        coroutineScope.launch {
            TokenPreferences.guardarToken(context, nuevoToken)
            _token.value = nuevoToken
            Log.d("SesionManager", "🔐 Token actualizado en memoria y preferencias")
        }
    }

    /**
     * Verifica si el nuevo token es diferente y lo guarda en memoria.
     */
    fun actualizarTokenSiHaCambiado(nuevoToken: String) {
        if (_token.value != nuevoToken) {
            Log.d("SesionManager", "🔄 Token ha cambiado. Actualizando...")
            _token.value = nuevoToken
        } else {
            Log.d("SesionManager", "✅ Token sin cambios")
        }
    }

    /**
     * Cierra la sesión limpiando memoria y preferencias.
     */
    fun cerrarSesion(context: Context) {
        coroutineScope.launch {
            TokenPreferences.borrarToken(context)
            UsuarioPreferences.borrarUsuario(context)
            _token.value = null
            _usuario.value = null
            _yaCargado.value = false
            Log.d("SesionManager", "🔒 Sesión cerrada correctamente")
        }
    }

    fun estaSesionActiva(): Boolean {
        return _token.value != null && _usuario.value != null
    }

    fun marcarUsuarioComoCargado() {
        _yaCargado.value = true
    }
}

/**
 * Conversión segura para evitar ambigüedades por sobrecarga.
 */
fun UsuarioPersistente.toUsuarioCompat(): Usuario {
    return Usuario(
        id = this.id,
        nombre = this.nombre,
        apellido = this.apellido ?: "",
        correo = this.correo,
        contrasenna = "",
        rol = this.rol,
        activo = this.activo,
        pagado = this.pagado
    )
}