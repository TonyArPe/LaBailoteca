package com.example.bailotecaapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bailotecaapp.model.Inscripcion
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.model.dto.UsuarioUpdateRequest
import com.example.bailotecaapp.network.BailotecaApi
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * ViewModel que gestiona la sesión del usuario autenticado, así como sus inscripciones y perfil.
 * Se inyecta con Hilt y gestiona la lógica de autenticación + sincronización de perfil.
 */
@HiltViewModel
class SesionViewModel @Inject constructor(
    private val api: BailotecaApi
) : ViewModel() {

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _inscripciones = MutableStateFlow<List<Inscripcion>>(emptyList())
    val inscripciones: StateFlow<List<Inscripcion>> = _inscripciones

    /**
     * Obtiene el usuario autenticado desde el backend y lo guarda en el estado observable.
     */
    fun obtenerUsuarioActual() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = Firebase.auth.currentUser?.getIdToken(true)?.await()?.token
                if (token != null) {
                    val response = api.getUsuarioActual("Bearer $token")
                    if (response.isSuccessful) {
                        _usuario.value = response.body()
                        Log.d("SesionViewModel", "Usuario actualizado correctamente.")
                    } else {
                        _error.value = "Error al obtener perfil: ${response.code()}"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Excepción al obtener usuario: ${e.localizedMessage}"
                Log.e("SesionViewModel", "Error: ${e.message}", e)
            } finally {
                _isLoading.value = false
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
                val response = api.getInscripcionesPorUsuario("Bearer $token", usuarioId)
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

                val response = api.actualizarUsuario("Bearer $token", userId, usuarioActualizado)
                if (response.isSuccessful) {
                    obtenerUsuarioActual()
                    onSuccess()
                } else {
                    onError("Error ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                onError("Excepción: ${e.localizedMessage}")
            }
        }
    }

    fun setUsuario(usuario: Usuario) {
        _usuario.value = usuario
    }
}