package com.example.bailotecaapp.network

import com.example.bailotecaapp.network.session.SesionManagerSingleton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import kotlinx.coroutines.tasks.await
import android.util.Log

/**
 * Interceptor que añade el token JWT a cada petición y lo renueva si ha cambiado.
 */
class FirebaseAuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return runBlocking {
            val user = FirebaseAuth.getInstance().currentUser

            if (user != null) {
                try {
                    // Siempre obtiene el token más reciente
                    val newToken = user.getIdToken(true).await().token
                    if (!newToken.isNullOrEmpty()) {
                        // Compara con el token actual y actualiza si ha cambiado
                        SesionManagerSingleton.actualizarTokenSiHaCambiado(newToken)

                        Log.d("FirebaseAuthInterceptor", "🔐 Token renovado exitosamente")

                        val newRequest = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer $newToken")
                            .build()

                        return@runBlocking chain.proceed(newRequest)
                    }
                } catch (e: Exception) {
                    Log.e("FirebaseAuthInterceptor", "❌ Error renovando token: ${e.message}", e)
                }
            }

            // En caso de no haber token, sigue con la petición original
            chain.proceed(chain.request())
        }
    }
}