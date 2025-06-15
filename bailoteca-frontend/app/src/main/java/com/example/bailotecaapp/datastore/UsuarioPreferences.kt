package com.example.bailotecaapp.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.network.session.toUsuario
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "usuario_prefs")

/**
 * Clase encargada de guardar y recuperar el usuario en DataStore como JSON.
 */
@Singleton
class UsuarioPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val USUARIO_JSON_KEY = stringPreferencesKey("usuario_json")
        private const val TAG = "UsuarioPreferences"
    }

    /**
     * Guarda el usuario serializado como JSON.
     */
    suspend fun guardarUsuario(usuario: UsuarioPersistente) {
        val json = Gson().toJson(usuario)
        context.dataStore.edit { prefs ->
            prefs[USUARIO_JSON_KEY] = json
        }
        Log.d(TAG, "✅ Usuario guardado en preferencias: $json")
    }

    /**
     * Devuelve el usuario guardado, o null si no existe.
     * Realiza la conversión desde UsuarioPersistente a Usuario.
     */
    suspend fun obtenerUsuario(): Usuario? {
        val prefs = context.dataStore.data.first()
        val json = prefs[USUARIO_JSON_KEY]
        return if (json != null) {
            try {
                val persistente = Gson().fromJson(json, UsuarioPersistente::class.java)
                val usuario = persistente.toUsuario()
                Log.d(TAG, "📤 Usuario cargado desde preferencias: $usuario")
                usuario
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al deserializar el usuario persistente: ${e.message}")
                null
            }
        } else {
            Log.w(TAG, "⚠️ No se encontró usuario en preferencias.")
            null
        }
    }

    /**
     * Borra el usuario almacenado.
     */
    suspend fun borrarUsuario() {
        context.dataStore.edit { prefs ->
            prefs.remove(USUARIO_JSON_KEY)
        }
        Log.d(TAG, "🗑️ Usuario eliminado de preferencias")
    }
}