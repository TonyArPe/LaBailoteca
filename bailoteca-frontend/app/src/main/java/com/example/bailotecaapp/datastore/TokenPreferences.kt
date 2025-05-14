package com.example.bailotecaapp.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Utilidad para guardar y recuperar el token JWT de Firebase usando Jetpack DataStore.
 *
 * Uso:
 * - tokenPreferences.saveToken("tu_token")
 * - tokenPreferences.getToken.collect { ... }
 */
class TokenPreferences(private val context: Context) {

    companion object {
        private val Context.dataStore by preferencesDataStore(name = "user_prefs")
        private val TOKEN_KEY = preferencesKey<String>("jwt_token")
    }

    /**
     * Guarda el token JWT en DataStore.
     */
    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    /**
     * Devuelve el token JWT como Flow.
     */
    val getToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    /**
     * Elimina el token JWT del almacenamiento.
     */
    suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
        }
    }
}