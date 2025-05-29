package com.example.bailotecaapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bailotecaapp.datastore.TokenPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tokenPreferences: TokenPreferences
) : ViewModel() {
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    init {
        viewModelScope.launch {
            _isDarkTheme.value = tokenPreferences.obtenerTemaOscuro(context) ?: false
        }
    }

    fun toggleDarkTheme(enabled: Boolean) {
        _isDarkTheme.value = enabled
        viewModelScope.launch {
            tokenPreferences.guardarTemaOscuro(context, enabled)
        }
    }
}
