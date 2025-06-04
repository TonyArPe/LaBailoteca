package com.example.bailotecaapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.network.ApiService
import com.example.bailotecaapp.network.session.SesionManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject

/**
 * ViewModel encargado del registro de usuarios en Firebase y en el backend,
 * y de guardar su sesión en local con token + usuario.
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val api: ApiService,
    private val sesionManager: SesionManager
) : ViewModel() {

    /**
     * Registra un nuevo usuario en el backend y guarda sesión local.
     *
     * @param usuario Usuario a registrar
     * @param onResult Callback con la respuesta HTTP (éxito o error)
     */
    fun registrarUsuarioBackend(usuario: Usuario, onResult: (Response<Usuario>) -> Unit) {
        viewModelScope.launch {
            try {
                val token = Firebase.auth.currentUser?.getIdToken(true)?.await()?.token ?: run {
                    Log.e("RegisterViewModel", "❌ No se pudo obtener el token de Firebase.")
                    return@launch
                }

                Log.d("RegisterViewModel", "📝 Registrando usuario en backend con correo ${usuario.correo}")
                val response = api.crearUsuario("Bearer $token", usuario)

                if (response.isSuccessful && response.body() != null) {
                    val backendUsuario = response.body()!!
                    Log.d("RegisterViewModel", "✅ Usuario registrado en backend: ${backendUsuario.correo}")

                    sesionManager.guardarToken(token)
                    sesionManager.guardarUsuario(backendUsuario)

                    Log.d("RegisterViewModel", "📦 Usuario y token guardados en preferencias")
                } else {
                    Log.w("RegisterViewModel", "⚠️ Error backend: ${response.code()}")
                }

                onResult(response)
            } catch (e: Exception) {
                Log.e("RegisterViewModel", "❌ Excepción durante registro: ${e.localizedMessage}")
                val errorBody = ResponseBody.create("application/json".toMediaTypeOrNull(), "Excepción: ${e.message}")
                onResult(Response.error(500, errorBody))
            }
        }
    }
}