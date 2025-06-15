package com.example.bailotecaapp.network

import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Proveedor de la URL base obtenida desde Firebase Remote Config.
 */
@Singleton
class FirebaseUrlProvider @Inject constructor() {

    private val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig.apply {
        setConfigSettingsAsync(remoteConfigSettings {
            minimumFetchIntervalInSeconds = 60
        })
        setDefaultsAsync(mapOf("base_url" to "https://9e2f-84-122-0-141.ngrok-free.app/"))
    }

    /**
     * Devuelve la URL base actualmente obtenida desde Remote Config.
     */
    // ✅ Así
    fun getBaseUrl(): String {
        val url = remoteConfig.getString("base_url")
        Log.d("FirebaseUrlProvider", "🌐 URL obtenida de Firebase RemoteConfig: '$url'")

        val finalUrl = if (url.isNotBlank()) {
            if (url.endsWith("/")) url else "$url/"
        } else {
            Log.w("FirebaseUrlProvider", "⚠️ URL vacía, usando fallback.")
            "https://default-fallback.ngrok-free.app/"
        }

        Log.d("FirebaseUrlProvider", "🌐 URL final utilizada por Retrofit: $finalUrl")
        return finalUrl
    }

    /**
     * Espera de forma segura a que fetchAndActivate termine y devuelva una URL válida.
     * Bloquea hasta 5 segundos como máximo para prevenir bloqueos eternos.
     */
    suspend fun fetchAndAwaitValidUrl(timeoutMillis: Long = 5000): Boolean {
        return try {
            withTimeout(timeoutMillis) {
                val resultado = remoteConfig.fetchAndActivate().await()
                Log.i("FirebaseUrlProvider", "✅ Configuración Firebase actualizada: $resultado")
                true
            }
        } catch (e: Exception) {
            Log.e("FirebaseUrlProvider", "❌ Timeout o error al obtener URL remota", e)
            false
        }
    }

    /**
     * Versión suspend de fetchAndActivate para integrarse mejor en coroutines.
     */
    suspend fun fetchAndActivateSuspend(): Boolean {
        return try {
            remoteConfig.fetchAndActivate().await().also {
                Log.i("FirebaseUrlProvider", "✅ Configuración Firebase actualizada.")
            }
        } catch (e: Exception) {
            Log.e("FirebaseUrlProvider", "❌ Error al actualizar Remote Config", e)
            false
        }
    }


    /**
     * Ejecuta `fetchAndActivate` para sincronizar la URL con Firebase.
     */
    fun fetchAndActivate() {
        remoteConfig.fetchAndActivate()
            .addOnSuccessListener {
                Log.i("FirebaseUrlProvider", "✅ Configuración Firebase actualizada.")
            }
            .addOnFailureListener {
                Log.e("FirebaseUrlProvider", "❌ Error al actualizar Remote Config", it)
            }
    }
}