package com.example.bailotecaapp.network

import com.example.bailotecaapp.datastore.TokenPreferences
import javax.inject.Inject
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Proveedor de token JWT desde DataStore, usado por Interceptors.
 */
class FirebaseAuthTokenProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Obtiene el token JWT guardado en DataStore, si existe.
     */
    suspend fun obtenerToken(): String? = withContext(Dispatchers.IO) {
        TokenPreferences.obtenerToken(context)
    }
}