package com.example.bailotecaapp.di

import android.content.Context
import com.example.bailotecaapp.datastore.TokenPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext

/**
 * Módulo de Hilt que proporciona dependencias relacionadas con las preferencias del usuario,
 * como el almacenamiento de tokens JWT utilizando Jetpack DataStore.
 *
 * Este módulo está instalado en el componente Singleton, lo que significa que las dependencias
 * que provee vivirán durante todo el ciclo de vida de la aplicación.
 *
 * @author
 * @see TokenPreferences Clase utilizada para almacenar y recuperar el token JWT del usuario.
 */
@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {

    /**
     * Provee una instancia única de [TokenPreferences] accesible desde cualquier parte
     * de la aplicación utilizando Hilt.
     *
     * @param context El contexto de aplicación necesario para inicializar DataStore.
     * @return Instancia de [TokenPreferences].
     */
    @Provides
    fun provideTokenPreferences(@ApplicationContext context: Context): TokenPreferences {
        return TokenPreferences(context)
    }
}