package com.example.bailotecaapp.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


/**
 * Utilidad para guardar y recuperar el token JWT de Firebase usando Jetpack DataStore.
 *
 * Uso:
 * - tokenPreferences.saveToken("tu_token")
 * - tokenPreferences.getToken.collect { ... }
 */
class TokenPreferences(
    @ApplicationContext private val context: Context
) {

    companion object {
        private val Context.dataStore by preferencesDataStore(name = "user_prefs")
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
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
     * Borra el token JWT del DataStore.
     */
    suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
        }
    }

    suspend fun getToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[TOKEN_KEY]
        }
    }
}