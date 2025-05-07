package com.example.bailotecaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bailotecaapp.model.Clase
import com.example.bailotecaapp.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await

@HiltViewModel
class ClaseDetailViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {

    var claseSeleccionada by mutableStateOf<Clase?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun cargarClase(claseId: Long) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Obtener token
                val token = Firebase.auth.currentUser?.getIdToken(false)?.await()?.token
                if (token == null) {
                    errorMessage = "Token no disponible"
                    return@launch
                }

                val response = api.getClasePorId("Bearer $token", claseId)
                if (response.isSuccessful) {
                    claseSeleccionada = response.body()
                } else {
                    errorMessage = "Error al cargar clase: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Excepción: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}