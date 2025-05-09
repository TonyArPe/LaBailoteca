package com.example.bailotecaapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.network.ApiService
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * ViewModel que obtiene la lista de usuarios desde el backend.
 * Solo accesible por usuarios con rol ADMIN.
 */
@HiltViewModel
class UsuarioViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {

    // Lista de usuarios observable
    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios

    // Estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Mensaje de error
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    /**
     * Función para obtener usuarios desde el backend.
     * Utiliza el token de Firebase para autenticación segura.
     */
    fun obtenerUsuarios() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val token = Firebase.auth.currentUser?.getIdToken(true)?.await()?.token ?: throw Exception("Token nulo")
                val response = api.getUsuarios("Bearer $token")

                if (response.isSuccessful && response.body() != null) {
                    _usuarios.value = response.body()!!
                    Log.d("UsuarioViewModel", "Usuarios cargados correctamente")
                } else {
                    _errorMessage.value = when (response.code()) {
                        403 -> "Acceso denegado. No tienes permisos para ver los usuarios."
                        401 -> "Sesión inválida. Inicia sesión nuevamente."
                        else -> "Error ${response.code()}: ${response.message() ?: "Respuesta no válida"}"
                    }
                    Log.e("UsuarioViewModel", "Error: ${response.errorBody()?.string()}")
                }

            } catch (e: Exception) {
                _errorMessage.value = "Excepción: ${e.localizedMessage}"
                Log.e("UsuarioViewModel", "Excepción: ${e.message}", e)

            } finally {
                _isLoading.value = false
            }
        }
    }
}