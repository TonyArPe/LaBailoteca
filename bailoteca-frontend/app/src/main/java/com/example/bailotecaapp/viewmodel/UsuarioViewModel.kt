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
 * ViewModel encargado de gestionar las operaciones relacionadas con los usuarios.
 * Solo accesible por usuarios autenticados, típicamente con rol ADMIN o PROFESOR.
 */
@HiltViewModel
class UsuarioViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {

    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    /**
     * Obtiene todos los usuarios desde el backend.
     * Requiere autenticación previa con token JWT de Firebase.
     */
    fun obtenerUsuarios() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val token = Firebase.auth.currentUser?.getIdToken(true)?.await()?.token
                    ?: throw Exception("Token nulo")
                val response = api.getUsuarios("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    _usuarios.value = response.body()!!
                    Log.d("UsuarioViewModel", "Usuarios cargados correctamente")
                } else {
                    _errorMessage.value = "Error ${response.code()}: ${response.message()}"
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

    /**
     * Elimina un usuario dado su ID.
     */
    fun eliminarUsuario(id: Long) {
        viewModelScope.launch {
            try {
                val token = Firebase.auth.currentUser?.getIdToken(true)?.await()?.token ?: return@launch
                val response = api.eliminarUsuario("Bearer $token", id)
                if (response.isSuccessful) {
                    _usuarios.value = _usuarios.value.filterNot { it.id == id }
                } else {
                    Log.e("UsuarioViewModel", "Error al eliminar: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("UsuarioViewModel", "Error al eliminar usuario", e)
            }
        }
    }

    /**
     * Alterna el estado de pago de un usuario si es profesor o administrador.
     */
    fun togglePagado(usuario: Usuario) {
        usuario.id?.let { id ->
            viewModelScope.launch {
                try {
                    val nuevoEstado = !usuario.pagado
                    val actualizado = usuario.copy(pagado = nuevoEstado)
                    val token = Firebase.auth.currentUser?.getIdToken(true)?.await()?.token ?: return@launch
                    val response = api.actualizarUsuario("Bearer $token", id, actualizado)
                    if (response.isSuccessful) {
                        _usuarios.value = _usuarios.value.map {
                            if (it.id == id) actualizado else it
                        }
                    }
                } catch (e: Exception) {
                    Log.e("UsuarioViewModel", "Error al actualizar pagado", e)
                }
            }
        } ?: Log.e("UsuarioViewModel", "ID de usuario es null en togglePagado")
    }

    /**
     * Obtiene un usuario por su ID desde el backend.
     *
     * @param id Identificador del usuario a buscar.
     * @param token Token JWT de autenticación.
     * @return El usuario si se encuentra, o null si no.
     */
    suspend fun obtenerUsuarioPorId(id: Long, token: String): Usuario? {
        return try {
            val response = api.getUsuarioPorId("Bearer $token", id)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("UsuarioViewModel", "Error al obtener usuario: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("UsuarioViewModel", "Excepción al obtener usuario por ID", e)
            null
        }
    }

    /**
     * Actualiza un usuario con nuevos datos.
     */
    suspend fun actualizarUsuario(token: String, id: Long, usuario: Usuario): Boolean {
        return try {
            val response = api.actualizarUsuario("Bearer $token", id, usuario)
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("UsuarioViewModel", "Error al actualizar usuario", e)
            false
        }
    }
}