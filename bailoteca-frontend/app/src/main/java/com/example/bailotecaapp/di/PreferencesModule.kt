package com.example.bailotecaapp.di

import com.example.bailotecaapp.datastore.TokenPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Módulo de Hilt que proporciona instancias singleton relacionadas con preferencias
 * como TokenPreferences o UsuarioPreferences si se necesitara.
 *
 * Estas instancias se inyectan como objetos únicos (object) y no requieren contexto.
 */
@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {

    /**
     * Provee el singleton [TokenPreferences] como dependencia inyectable.
     */
    @Provides
    fun provideTokenPreferences(): TokenPreferences = TokenPreferences
}