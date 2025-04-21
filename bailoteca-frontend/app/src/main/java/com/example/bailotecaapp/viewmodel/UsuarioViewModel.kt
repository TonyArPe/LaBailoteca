package com.example.bailotecaapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.network.RetrofitInstance
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UsuarioViewModel : ViewModel() {

    // Lista de usuarios observable
    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios

    // Estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Error (opcional)
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    /**
     * Función para obtener usuarios desde el backend
     * Se encarga de obtener el token Firebase y hacer la petición segura
     */
    fun obtenerUsuarios() {
        _isLoading.value = true
        _errorMessage.value = null

        val user = Firebase.auth.currentUser

        user?.getIdToken(true)?.addOnSuccessListener { result ->
            val token = result.token ?: ""
            val authHeader = "Bearer $token"

            viewModelScope.launch {
                try {
                    val response = RetrofitInstance.api.getUsuarios(authHeader)
                    if (response.isSuccessful && response.body() != null) {
                        _usuarios.value = response.body()!!
                    } else {
                        _errorMessage.value = "Error al obtener usuarios: ${response.code()}"
                        Log.e("UsuarioViewModel", "Error: ${response.errorBody()?.string()}")
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Error: ${e.message}"
                    Log.e("UsuarioViewModel", "Excepción: ${e.localizedMessage}")
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
