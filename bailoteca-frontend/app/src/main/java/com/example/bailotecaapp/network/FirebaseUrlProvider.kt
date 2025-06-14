package com.example.bailotecaapp.network

import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.coroutines.tasks.await
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
        Log.d("FirebaseUrlProvider", "🌐 URL obtenida de Firebase: $url")
        return if (url.isNotBlank()) {
            if (url.endsWith("/")) url else "$url/"
        } else {
            "https://default-fallback.ngrok-free.app/"
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