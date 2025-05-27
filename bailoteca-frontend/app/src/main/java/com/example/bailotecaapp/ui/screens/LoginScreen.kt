package com.example.bailotecaapp.ui.screens

import android.util.Log
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.bailotecaapp.model.enums.Rol
import com.example.bailotecaapp.navigation.Screens
import com.example.bailotecaapp.ui.screens.invitado.components.personalizacion.FondoGradiente
import com.example.bailotecaapp.ui.screens.invitado.components.personalizacion.LoginAnimatedHeader
import com.example.bailotecaapp.ui.theme.VerdeBailoteca
import com.example.bailotecaapp.viewmodel.LoginViewModel
import com.example.bailotecaapp.viewmodel.SesionViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: LoginViewModel = viewModel(),
    sesionViewModel: SesionViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val usuario by sesionViewModel.usuario.collectAsState()
    val usuarioCargado by sesionViewModel.usuarioCargado.collectAsState()
    val sesionCargando by sesionViewModel.isLoading.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(usuarioCargado, usuario) {
        if (usuario != null && usuario!!.rol != Rol.INVITADO && usuarioCargado) {
            navController.navigate(Screens.Home.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    FondoGradiente {
        Scaffold(containerColor = Color.Transparent) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LoginAnimatedHeader()

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electrónico", color = Color.White) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VerdeBailoteca,
                        unfocusedBorderColor = Color.White,
                        focusedLabelColor = VerdeBailoteca,
                        unfocusedLabelColor = Color.White,
                        cursorColor = VerdeBailoteca
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(color = Color.White),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = VerdeBailoteca,
                        cursorColor = VerdeBailoteca,
                        focusedLabelColor = VerdeBailoteca,
                        unfocusedLabelColor = Color.White,
                        unfocusedBorderColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        viewModel.login(
                            email,
                            password,
                            onSuccess = {
                                coroutineScope.launch {
                                    try {
                                        val token = Firebase.auth.currentUser?.getIdToken(false)?.await()?.token
                                        if (!token.isNullOrEmpty()) {
                                            sesionViewModel.obtenerUsuarioActualConToken(token)
                                        } else {
                                            errorMessage = "Token nulo o vacío."
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Error obteniendo token: ${e.message}"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            onError = { error ->
                                isLoading = false
                                errorMessage = error
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && !sesionCargando
                ) {
                    Text(text = if (isLoading || sesionCargando) "Cargando..." else "Iniciar sesión")
                }

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = it, color = Color.Red, style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = { navController.navigate(Screens.Register.route) }) {
                    Text("¿No tienes cuenta? Regístrate aquí")
                }

                TextButton(
                    onClick = {
                        sesionViewModel.entrarComoInvitado {
                            navController.navigate(Screens.InvitadoHome.route) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Entrar como invitado")
                }
            }

            TextButton(
                onClick = {
                    sesionViewModel.entrarComoInvitado()
                    navController.navigate(Screens.Home.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Entrar como invitado")
            }
        }
    }
}