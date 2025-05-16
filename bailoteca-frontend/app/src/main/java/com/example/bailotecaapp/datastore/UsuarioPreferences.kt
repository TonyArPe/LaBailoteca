package com.example.bailotecaapp.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.example.bailotecaapp.model.enums.Rol
import kotlinx.serialization.decodeFromString


@Serializable
data class UsuarioPersistente(
    val id: Long,
    val nombre: String,
    val apellido: String,
    val correo: String,
    val rol: Rol
)

private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}


private val Context.dataStore by preferencesDataStore("usuario_prefs")
private val USUARIO_KEY = stringPreferencesKey("usuario_serializado")

object UsuarioPreferences {

    suspend fun guardarUsuario(context: Context, usuario: UsuarioPersistente) {
        val json = json.encodeToString(usuario)
        context.dataStore.edit { prefs ->
            prefs[USUARIO_KEY] = json
        }
    }

    suspend fun obtenerUsuario(context: Context): UsuarioPersistente? {
        val prefs = context.dataStore.data.first()
        return prefs[USUARIO_KEY]?.let {
            json.decodeFromString(it)
        }
    }

    suspend fun borrarUsuario(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.remove(USUARIO_KEY)
        }
    }
}