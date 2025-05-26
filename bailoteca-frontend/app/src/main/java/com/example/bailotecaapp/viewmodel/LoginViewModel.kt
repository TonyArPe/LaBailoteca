package com.example.bailotecaapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

/**
 * ViewModel encargado de manejar la lógica de login con FirebaseAuth.
 * Este ViewModel **ya no obtiene el token JWT**: solo valida las credenciales.
 */
class LoginViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Inicia sesión con email y contraseña usando Firebase.
     *
     * @param email Correo introducido por el usuario.
     * @param password Contraseña introducida.
     * @param onSuccess Acción a ejecutar si el login es exitoso.
     * @param onError Acción a ejecutar si ocurre un error.
     */
    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (email.isBlank() || password.isBlank()) {
            onError("Correo y contraseña no pueden estar vacíos.")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("LoginViewModel", "✅ Login correcto con Firebase")
                    onSuccess()
                } else {
                    val error = task.exception?.localizedMessage ?: "Error desconocido"
                    Log.e("LoginViewModel", "Login fallido: $error")
                    onError(error)
                }
            }
    }

    /**
     * Registra un nuevo usuario con Firebase usando correo y contraseña.
     *
     * @param email Correo del nuevo usuario.
     * @param password Contraseña segura.
     * @param onSuccess Acción a ejecutar si el registro es exitoso.
     * @param onError Acción a ejecutar si ocurre un error.
     */
    fun register(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (email.isBlank() || password.isBlank()) {
            onError("Correo y contraseña no pueden estar vacíos.")
            return
        }

        if (password.length < 6) {
            onError("La contraseña debe tener al menos 6 caracteres.")
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    val error = task.exception?.localizedMessage ?: "Error desconocido"
                    Log.e("LoginViewModel", "Registro fallido: $error")
                    onError(error)
                }
            }
    }
}