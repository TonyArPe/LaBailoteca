package com.example.bailotecaapp.network

import android.util.Log
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Interceptor que añade el token JWT (Firebase) a cada petición HTTP.
 */
class FirebaseAuthInterceptor @Inject constructor(
    private val tokenProvider: FirebaseAuthTokenProvider
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response = runBlocking {
        try {
            val token = tokenProvider.obtenerToken()
            if (!token.isNullOrBlank()) {
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                return@runBlocking chain.proceed(request)
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuthInterceptor", "❌ Error al añadir token: ${e.message}", e)
        }

        chain.proceed(chain.request())
    }
}