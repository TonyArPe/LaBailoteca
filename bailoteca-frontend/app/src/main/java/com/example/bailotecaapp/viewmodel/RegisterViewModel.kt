package com.example.bailotecaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import retrofit2.Response

/**
 * ViewModel que gestiona el registro de usuarios, inyectado con Hilt.
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {

    /**
     * Registra el usuario en el backend tras el registro en Firebase.
     */
    fun registrarUsuarioBackend(usuario: Usuario, onResult: (Response<Usuario>) -> Unit) {
        viewModelScope.launch {
            try {
                val token = Firebase.auth.currentUser?.getIdToken(true)?.await()?.token ?: return@launch
                val response = api.crearUsuario("Bearer $token", usuario)
                onResult(response)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(Response.error(500, okhttp3.ResponseBody.create(null, "Excepción: ${e.message}")))
            }
        }
    }
}