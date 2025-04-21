package com.example.bailotecaapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * ViewModel encargado de manejar la lógica de login con FirebaseAuth.
 */
class LoginViewModel : ViewModel() {

    // Instancia de Firebase Authentication
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Inicia sesión con email y contraseña usando Firebase.
     *
     * @param email Correo introducido por el usuario
     * @param password Contraseña introducida
     * @param onSuccess Acción a ejecutar si el login es exitoso (por ejemplo, navegar)
     * @param onError Acción a ejecutar si ocurre un error (muestra mensaje)
     */
    fun login(
        email: String,
        password: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        // Ejecutamos en una corrutina para no bloquear la UI
        viewModelScope.launch {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Si el login es correcto, obtenemos el idToken (JWT)
                        auth.currentUser?.getIdToken(true)?.addOnSuccessListener { result ->
                            val idToken = result.token
                            idToken?.let {
                                onSuccess(it) // Llamamos al callback con el token
                            } ?: onError("Token no disponible")
                        }?.addOnFailureListener {
                            onError("Error al obtener token")
                        }
                    } else {
                        // Mostramos mensaje de error si el login falla
                        val error = task.exception?.localizedMessage ?: "Error desconocido"
                        Log.e("LoginViewModel", "Login fallido: $error")
                        onError(error)
                    }
                }
        }
    }

    /**
     * Registra un nuevo usuario con Firebase usando correo y contraseña.
     *
     * @param email Correo del nuevo usuario
     * @param password Contraseña segura (mínimo 6 caracteres)
     * @param onSuccess Acción a ejecutar si el registro es exitoso
     * @param onError Acción a ejecutar si ocurre un error durante el proceso
     */
    fun register(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (email.isBlank() || password.isBlank()) {
            onError("Correo y contraseña no pueden estar vacíos")
            return
        }

        if (password.length < 6) {
            onError("La contraseña debe tener al menos 6 caracteres")
            return
        }

        viewModelScope.launch {
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
}
