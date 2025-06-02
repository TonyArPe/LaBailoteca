package com.example.bailotecaapp.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.bailotecaapp.model.enums.Rol
import kotlinx.coroutines.flow.first
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Modelo persistente simplificado del usuario para guardar en DataStore.
 * Evita guardar datos innecesarios o estructuras recursivas.
 */
@Serializable
data class UsuarioPersistente(
    val id: Long,
    val nombre: String,
    val apellido: String? = "",
    val correo: String,
    val contrasenna: String = "",
    val rol: Rol,
    val fotoPerfil: String? = null,
    val telefono: String? = null,
    val direccion: String? = null,
    val fechaNacimiento: String? = null,
    val genero: String? = null,
    val dni: String? = null,
    val fechaRegistro: String? = null,
    val activo: Boolean,
    val pagado: Boolean
)



private val Context.dataStore by preferencesDataStore(name = "usuario_prefs")
private val USUARIO_KEY = stringPreferencesKey("usuario_json")

/**
 * Utilidad para guardar, recuperar o eliminar un usuario en DataStore.
 */
object UsuarioPreferences {

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    suspend fun guardarUsuario(context: Context, usuario: UsuarioPersistente) {
        val jsonString = json.encodeToString(usuario)
        context.dataStore.edit { prefs ->
            prefs[USUARIO_KEY] = jsonString
        }
    }

    suspend fun obtenerUsuario(context: Context): UsuarioPersistente? {
        val prefs = context.dataStore.data.first()
        val jsonString = prefs[USUARIO_KEY] ?: return null
        return json.decodeFromString(jsonString)
    }

    suspend fun borrarUsuario(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.remove(USUARIO_KEY)
        }
    }
}