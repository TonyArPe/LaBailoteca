package com.example.bailotecaapp.network

import android.content.Context
import android.util.Log
import com.example.bailotecaapp.network.session.SesionManagerSingleton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor de autenticación que añade el token JWT de Firebase a cada petición HTTP.
 * Si el token está próximo a expirar, lo renueva automáticamente.
 *
 * @param context Contexto de aplicación necesario para acceder a DataStore y preferencias.
 */
class FirebaseAuthInterceptor(
    private val context: Context
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response = runBlocking {
        try {
            val token = SesionManagerSingleton.token.value
            if (!token.isNullOrBlank()) {
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                return@runBlocking chain.proceed(request)
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuthInterceptor", "❌ Error al insertar token en headers: ${e.message}", e)
        }

        chain.proceed(chain.request())
    }
}