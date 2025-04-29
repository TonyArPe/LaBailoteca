package com.example.bailotecaapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.bailotecaapp.navigation.Screens
import com.example.bailotecaapp.viewmodel.LoginViewModel
import com.example.bailotecaapp.viewmodel.SesionViewModel
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.model.enums.Rol
import com.example.bailotecaapp.network.RetrofitInstance
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun RegisterScreen(
    navController: NavHostController,
    loginViewModel: LoginViewModel = viewModel(),
    sesionViewModel: SesionViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Crear cuenta",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre completo") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    isLoading = true
                    errorMessage = null

                    Firebase.auth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener { result ->
                            val user = result.user
                            user?.getIdToken(true)?.addOnSuccessListener { idTokenResult ->
                                val token = idTokenResult.token
                                if (token != null) {
                                    val usuario = Usuario(
                                        nombre = name,
                                        correo = email,
                                        contrasenna = password,
                                        rol = Rol.USUARIO,
                                        activo = true
                                    )
                                    coroutineScope.launch {
                                        try {
                                            val response = RetrofitInstance.api.crearUsuario("Bearer $token", usuario)
                                            if (response.isSuccessful) {
                                                sesionViewModel.cargarUsuarioActual() // carga el perfil
                                                navController.navigate(Screens.Home.route)
                                            } else {
                                                errorMessage = "Error backend: ${response.code()}"
                                            }
                                        } catch (e: Exception) {
                                            errorMessage = "Error al crear usuario: ${'$'}{e.message}"
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                } else {
                                    errorMessage = "No se pudo obtener el token."
                                    isLoading = false
                                }
                            }?.addOnFailureListener {
                                errorMessage = "Error al obtener token: ${'$'}{it.message}"
                                isLoading = false
                            }
                        }
                        .addOnFailureListener {
                            errorMessage = "Error en Firebase: ${'$'}{it.message}"
                            isLoading = false
                        }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text(text = if (isLoading) "Creando..." else "Crear cuenta")
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = it, color = Color.Red, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}