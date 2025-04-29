package com.example.bailotecaapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bailotecaapp.model.Inscripcion
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.network.RetrofitInstance
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SesionViewModel : ViewModel() {

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _inscripciones = MutableStateFlow<List<Inscripcion>>(emptyList())
    val inscripciones: StateFlow<List<Inscripcion>> = _inscripciones

    /**
     * Obtiene el usuario actual desde el backend usando el token Firebase
     */
    fun cargarUsuarioActual() {
        _isLoading.value = true
        _error.value = null

        val user = Firebase.auth.currentUser
        user?.getIdToken(true)?.addOnSuccessListener { result ->
            val token = result.token ?: ""
            val authHeader = "Bearer $token"

            viewModelScope.launch {
                try {
                    val response = RetrofitInstance.api.getUsuarioActual(authHeader)
                    if (response.isSuccessful && response.body() != null) {
                        _usuario.value = response.body()
                    } else {
                        _error.value = "Error: ${response.code()}"
                    }
                } catch (e: Exception) {
                    _error.value = "Excepción: ${e.message}"
                    Log.e("SesionViewModel", "Error al obtener usuario: ${e.localizedMessage}")
                } finally {
                    _isLoading.value = false
                }
            }
        }?.addOnFailureListener {
            _isLoading.value = false
            _error.value = "Error al obtener token de Firebase"
        }
    }

    fun cargarMisInscripciones() {
        viewModelScope.launch {
            try {
                val token = Firebase.auth.currentUser?.getIdToken(false)?.await()?.token ?: return@launch
                val response = RetrofitInstance.api.getMisInscripciones("Bearer $token")
                if (response.isSuccessful) {
                    _inscripciones.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("SesionViewModel", "Error cargando inscripciones: ${e.message}")
            }
        }
    }
}
