package com.example.bailotecaapp.di

import android.content.Context
import com.example.bailotecaapp.network.ApiService
import com.example.bailotecaapp.network.FirebaseAuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * Módulo de red que provee las instancias de Retrofit, OkHttpClient e interceptores.
 * Usa Hilt para inyección de dependencias.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "http://10.0.2.2:8080/"

    /**
     * Proporciona el interceptor que añade y renueva el token JWT de Firebase.
     *
     * @param context Contexto de aplicación para acceder a DataStore y Firebase.
     */
    @Provides
    @Singleton
    fun provideFirebaseAuthInterceptor(
        @ApplicationContext context: Context
    ): FirebaseAuthInterceptor {
        return FirebaseAuthInterceptor(context)
    }

    /**
     * Proporciona el cliente HTTP con interceptores configurados.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: FirebaseAuthInterceptor
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()
    }

    /**
     * Proporciona una instancia de Retrofit.
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Proporciona una implementación de la interfaz ApiService.
     */
    @Provides
    @Singleton
    fun provideApiService(
        retrofit: Retrofit
    ): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}