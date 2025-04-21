package com.example.bailotecaapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bailotecaapp.model.Clase
import com.example.bailotecaapp.network.RetrofitInstance
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ClaseViewModel : ViewModel() {

    // Lista observable de clases
    private val _clases = MutableStateFlow<List<Clase>>(emptyList())
    val clases: StateFlow<List<Clase>> = _clases

    // Cargando
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Error
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    /**
     * Llama al backend para obtener las clases disponibles
     * Incluye autenticación con el token de Firebase
     */
    fun obtenerClases() {
        _isLoading.value = true
        _errorMessage.value = null

        val user = Firebase.auth.currentUser

        user?.getIdToken(true)?.addOnSuccessListener { result ->
            val token = result.token ?: ""
            val authHeader = "Bearer $token"

            viewModelScope.launch {
                try {
                    val response = RetrofitInstance.api.getClasesDisponibles(authHeader)
                    if (response.isSuccessful && response.body() != null) {
                        _clases.value = response.body()!!
                    } else {
                        _errorMessage.value = "Error: ${response.code()}"
                        Log.e("ClaseViewModel", "Error: ${response.errorBody()?.string()}")
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Excepción: ${e.message}"
                    Log.e("ClaseViewModel", "Excepción: ${e.localizedMessage}")
                } finally {
                    _isLoading.value = false
                }
            }
        }?.addOnFailureListener {
            _isLoading.value = false
            _errorMessage.value = "Error al obtener token de Firebase"
        }
    }
}
