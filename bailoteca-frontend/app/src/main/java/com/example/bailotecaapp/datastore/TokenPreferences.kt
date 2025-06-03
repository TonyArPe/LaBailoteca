package com.example.bailotecaapp.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "token_prefs")
private val TOKEN_KEY = stringPreferencesKey("jwt_token")
private val EXPIRATION_KEY = longPreferencesKey("jwt_expiration")
private val TEMA_OSCURO_KEY = booleanPreferencesKey("tema_oscuro")
private val EXPIRACION_KEY = longPreferencesKey("jwt_expiration")

object TokenPreferences {

    /**
     * Guarda el token JWT y su tiempo de expiración en DataStore.
     * @param token Token JWT.
     * @param expirationTimeMs Fecha de expiración en milisegundos desde epoch.
     */
    suspend fun guardarToken(context: Context, token: String, expirationTimeMs: Long? = null) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            expirationTimeMs?.let {
                preferences[EXPIRATION_KEY] = it
            }
        }
    }

    suspend fun guardarExpiracion(context: Context, expirationTime: Long) {
        context.dataStore.edit { prefs ->
            prefs[EXPIRACION_KEY] = expirationTime
        }
    }

    suspend fun obtenerExpiracion(context: Context): Long? {
        val prefs = context.dataStore.data.first()
        return prefs[EXPIRACION_KEY]
    }

    /**
     * Recupera el token JWT guardado en DataStore.
     * @return Token actual o null.
     */
    suspend fun obtenerToken(context: Context): String? {
        val preferences = context.dataStore.data.first()
        return preferences[TOKEN_KEY]
    }

    /**
     * Recupera la fecha de expiración del token (en ms).
     * @return Tiempo en milisegundos desde epoch o null si no existe.
     */
    suspend fun obtenerExpiracionToken(context: Context): Long? {
        val preferences = context.dataStore.data.first()
        return preferences[EXPIRATION_KEY]
    }

    /**
     * Borra el token JWT y su expiración del DataStore.
     */
    suspend fun borrarToken(context: Context) {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(EXPIRATION_KEY)
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