package com.example.bailotecaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bailotecaapp.network.FirebaseUrlProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel encargado de exponer la URL base del backend obtenida desde Firebase Remote Config.
 * Esta URL es utilizada por Retrofit y por componentes de UI como carga de imágenes.
 */
@HiltViewModel
class ApiInitViewModel @Inject constructor(
    private val firebaseUrlProvider: FirebaseUrlProvider
) : ViewModel() {

    private val _baseUrl = MutableStateFlow("")
    val baseUrl: StateFlow<String> = _baseUrl

    init {
        viewModelScope.launch {
            // Fetch URL desde el proveedor y expónla como flujo
            val url = firebaseUrlProvider.getBaseUrl()
            _baseUrl.value = url
        }
    }
}