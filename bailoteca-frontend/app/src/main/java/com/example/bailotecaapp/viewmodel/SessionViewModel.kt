package com.example.bailotecaapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bailotecaapp.model.Inscripcion
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.model.dto.UsuarioUpdateRequest
import com.example.bailotecaapp.network.RetrofitInstance
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel que gestiona la sesión del usuario autenticado, así como sus inscripciones y perfil.
 */
class SesionViewModel : ViewModel() {

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _inscripciones = MutableStateFlow<List<Inscripcion>>(emptyList())
    val inscripciones: StateFlow<List<Inscripcion>> = _inscripciones

    init {
        cargarUsuarioActual()
    }

    /**
     * Obtiene el usuario autenticado desde el backend y lo guarda en el estado observable.
     */
    fun cargarUsuarioActual() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = Firebase.auth.currentUser?.getIdToken(true)?.await()?.token
                if (token != null) {
                    val response = RetrofitInstance.api.getUsuarioActual("Bearer $token")
                    if (response.isSuccessful) {
                        _usuario.value = response.body()
                        Log.d("SesionViewModel", "Usuario cargado correctamente.")
                    } else {
                        _error.value = "Error al obtener perfil: ${response.code()}"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Excepción al cargar usuario: ${e.localizedMessage}"
                Log.e("SesionViewModel", "Error: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Carga un usuario por ID desde el backend (requiere token válido).
     */
    fun cargarUsuarioDesdeBackend(id: Long) {
        viewModelScope.launch {
            try {
                val token = Firebase.auth.currentUser?.getIdToken(true)?.await()?.token
                if (token != null) {
                    val response = RetrofitInstance.api.getUsuarioPorId("Bearer $token", id)
                    if (response.isSuccessful) {
                        _usuario.value = response.body()
                    } else {
                        _error.value = "Error al obtener usuario por ID: ${response.code()}"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Excepción al obtener usuario por ID: ${e.localizedMessage}"
            }
        }
    }

    /**
     * Obtiene todas las inscripciones del usuario actual autenticado.
     */
    fun cargarMisInscripciones() {
        viewModelScope.launch {
            try {
                val token = Firebase.auth.currentUser?.getIdToken(false)?.await()?.token ?: return@launch
                val usuarioId = usuario.value?.id ?: return@launch
                val response = RetrofitInstance.api.getInscripcionesPorUsuario("Bearer $token", usuarioId)
                if (response.isSuccessful) {
                    _inscripciones.value = response.body() ?: emptyList()
                    Log.d("SesionViewModel", "Inscripciones cargadas correctamente.")
                } else {
                    _error.value = "Error al obtener inscripciones: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error al cargar inscripciones: ${e.localizedMessage}"
            }
        }
    }

    /**
     * Borra la sesión actual y limpia los estados.
     */
    fun cerrarSesion() {
        _usuario.value = null
        _inscripciones.value = emptyList()
        _error.value = null
    }

    fun actualizarPerfil(usuarioActualizado: UsuarioUpdateRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val token = Firebase.auth.currentUser?.getIdToken(true)?.await()?.token ?: return@launch
                val userId = usuario.value?.id ?: return@launch

                val response = RetrofitInstance.api.actualizarUsuario("Bearer $token", userId, usuarioActualizado)
                if (response.isSuccessful) {
                    _usuario.value = response.body() // actualizamos el usuario
                    onSuccess()
                } else {
                    onError("Error ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                onError("Excepción: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Establece un usuario manualmente (por ejemplo para pruebas).
     */
    fun setUsuario(usuario: Usuario) {
        _usuario.value = usuario
    }
}