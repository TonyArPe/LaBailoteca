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
        viewModelScope.launch {
            try {
                val token = Firebase.auth.currentUser?.getIdToken(true)?.await()?.token
                if (token != null) {
                    val response = RetrofitInstance.api.getUsuarioActual("Bearer $token")
                    if (response.isSuccessful) {
                        _usuario.value = response.body()
                        Log.d("SesionViewModel", "Usuario cargado: ${_usuario.value}")
                    } else {
                        Log.e("SesionViewModel", "Error al obtener perfil: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                Log.e("SesionViewModel", "Excepción al cargar usuario: ${e.message}")
            }
        }
    }

    fun cargarMisInscripciones() {
        viewModelScope.launch {
            try {
                val token = Firebase.auth.currentUser?.getIdToken(false)?.await()?.token ?: return@launch
                val usuarioId = usuario.value?.id ?: return@launch
                val response = RetrofitInstance.api.getInscripcionesPorUsuario("Bearer $token", usuarioId)
                if (response.isSuccessful) {
                    _inscripciones.value = response.body() ?: emptyList()
                    Log.d("SesionViewModel", "Inscripciones cargadas: ${_inscripciones.value}")
                } else {
                    Log.e("SesionViewModel", "Error al obtener inscripciones: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("SesionViewModel", "Error al cargar inscripciones: ${e.message}")
            }
        }
    }
}
