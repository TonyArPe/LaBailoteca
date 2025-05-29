package com.example.bailotecaapp.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "token_prefs")
private val TOKEN_KEY = stringPreferencesKey("jwt_token")
private val TEMA_OSCURO_KEY = booleanPreferencesKey("tema_oscuro")

object TokenPreferences {

    /**
     * Guarda el token JWT en DataStore.
     */
    suspend fun guardarToken(context: Context, token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    /**
     * Recupera el token JWT guardado en DataStore.
     */
    suspend fun obtenerToken(context: Context): String? {
        val preferences = context.dataStore.data.first()
        return preferences[TOKEN_KEY]
    }

    /**
     * Borra el token JWT de DataStore.
     */
    suspend fun borrarToken(context: Context) {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
        }
    }

    suspend fun guardarTemaOscuro(context: Context, dark: Boolean) {
        context.dataStore.edit { it[TEMA_OSCURO_KEY] = dark }
    }

    suspend fun obtenerTemaOscuro(context: Context): Boolean? {
        val prefs = context.dataStore.data.first()
        return prefs[TEMA_OSCURO_KEY]
    }
}