package com.example.bailotecaapp.network

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private const val BASE_URL = "http://10.0.2.2:8080/" // Cambiar en producción

    /**
     * Interceptor que añade el token de Firebase a cada petición.
     */
    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()

        val token = runBlocking {
            Firebase.auth.currentUser?.getIdToken(false)?.await()?.token
        }

        val newRequest = token?.let {
            original.newBuilder()
                .addHeader("Authorization", "Bearer $it")
                .build()
        } ?: original

        chain.proceed(newRequest)
    }

    /**
     * Interceptor para mostrar los logs de cada petición.
     */
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * Cliente HTTP con interceptores configurados.
     */
    private val client = OkHttpClient.Builder()
        // Token de Firebase
        .addInterceptor(authInterceptor)
        // Logs de petición/respuesta
        .addInterceptor(logging)
        .build()

    /**
     * Instancia de Retrofit para convertir a JSON.
     */
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /**
     * Servicio principal de la API (endpoints ApiService.kt).
     */
    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
