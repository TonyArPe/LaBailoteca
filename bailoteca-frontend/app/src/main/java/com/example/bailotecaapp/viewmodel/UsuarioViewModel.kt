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
 * Es usado por roles con permisos de gestión como ADMIN o PROFESOR.
 */
@HiltViewModel
class UsuarioViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {

    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios

    private val _usuarioDetalle = MutableStateFlow<Usuario?>(null)
    val usuarioDetalle: StateFlow<Usuario?> = _usuarioDetalle

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    /**
     * Carga la lista completa de usuarios desde el backend.
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
                    Log.d("UsuarioViewModel", "✅ Usuarios cargados correctamente")
                } else {
                    _errorMessage.value = "Error ${response.code()}: ${response.message()}"
                    Log.e("UsuarioViewModel", "❌ Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Excepción: ${e.localizedMessage}"
                Log.e("UsuarioViewModel", "❌ Excepción: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Carga el detalle de un usuario por su ID.
     * Actualiza el flujo `usuarioDetalle` si se encuentra.
     *
     * @param id ID del usuario a mostrar.
     */
    fun cargarUsuarioPorId(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val token = Firebase.auth.currentUser?.getIdToken(true)?.await()?.token
                    ?: throw Exception("Token nulo")
                val response = api.getUsuarioPorId("Bearer $token", id)
                if (response.isSuccessful && response.body() != null) {
                    _usuarioDetalle.value = response.body()
                    Log.d("UsuarioViewModel", "👤 Usuario detalle cargado: ${_usuarioDetalle.value?.correo}")
                } else {
                    _errorMessage.value = "Error ${response.code()}"
                    Log.e("UsuarioViewModel", "❌ Error al cargar usuario: ${response.message()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Excepción: ${e.message}"
                Log.e("UsuarioViewModel", "❌ Excepción al obtener usuario por ID", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Elimina un usuario por ID.
     */
    fun eliminarUsuario(id: Long) {
        viewModelScope.launch {
            try {
                val token = Firebase.auth.currentUser?.getIdToken(true)?.await()?.token ?: return@launch
                val response = api.eliminarUsuario("Bearer $token", id)
                if (response.isSuccessful) {
                    _usuarios.value = _usuarios.value.filterNot { it.id == id }
                    Log.d("UsuarioViewModel", "🗑️ Usuario eliminado: $id")
                } else {
                    Log.e("UsuarioViewModel", "❌ Error al eliminar: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("UsuarioViewModel", "❌ Error al eliminar usuario", e)
            }
        }
    }

    /**
     * Alterna el estado de pago del usuario (visible para profesores).
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
                        Log.d("UsuarioViewModel", "💳 Pagado actualizado: $nuevoEstado")
                    }
                } catch (e: Exception) {
                    Log.e("UsuarioViewModel", "❌ Error al actualizar pagado", e)
                }
            }
        } ?: Log.e("UsuarioViewModel", "⚠️ ID de usuario nulo en togglePagado")
    }

    /**
     * Actualiza un usuario desde la pantalla de edición (admin).
     */
    suspend fun actualizarUsuario(token: String, id: Long, usuario: Usuario): Boolean {
        return try {
            val response = api.actualizarUsuario("Bearer $token", id, usuario)
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("UsuarioViewModel", "❌ Error al actualizar usuario", e)
            false
        }
    }
}