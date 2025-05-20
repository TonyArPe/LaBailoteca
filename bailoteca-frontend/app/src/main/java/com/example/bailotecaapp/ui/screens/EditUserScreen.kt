package com.example.bailotecaapp.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.viewmodel.UsuarioViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Pantalla de edición de usuario, usada por el propio usuario o por ADMIN.
 *
 * @param usuarioId ID del usuario a editar.
 * @param navController Controlador de navegación para volver atrás tras guardar.
 * @param viewModel ViewModel con lógica de carga y actualización de usuario.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserScreen(
    usuarioId: Long,
    navController: NavController,
    viewModel: UsuarioViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var usuario by remember { mutableStateOf<Usuario?>(null) }
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }

    val isLoading by viewModel.isLoading.collectAsState()

    /**
     * Efecto de carga al inicio de pantalla.
     * Llama a la API para obtener el usuario por ID.
     */
    LaunchedEffect(usuarioId) {
        try {
            val token = Firebase.auth.currentUser?.getIdToken(true)?.await()?.token ?: return@LaunchedEffect
            val resultado = viewModel.obtenerUsuarioPorId(usuarioId, token)

            resultado?.let {
                val limpio = sanearUsuarioParaFormulario(it)
                usuario = limpio
                nombre = limpio.nombre
                apellido = limpio.apellido
                telefono = limpio.telefono ?: ""
                direccion = limpio.direccion ?: ""
            }
        } catch (e: Exception) {
            Log.e("EditUserScreen", "Error cargando usuario", e)
        }
    }

    usuario?.let {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Editar Usuario") })
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = apellido,
                    onValueChange = { apellido = it },
                    label = { Text("Apellido") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = direccion,
                    onValueChange = { direccion = it },
                    label = { Text("Dirección") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val token = Firebase.auth.currentUser?.getIdToken(true)?.await()?.token ?: return@launch
                                val actualizado = it.copy(
                                    nombre = nombre,
                                    apellido = apellido,
                                    telefono = telefono.ifBlank { null },
                                    direccion = direccion.ifBlank { null }
                                )
                                val success = viewModel.actualizarUsuario(token, usuarioId, actualizado)
                                if (success) {
                                    navController.navigate("lista_usuarios") {
                                        popUpTo("lista_usuarios") { inclusive = true }
                                    }
                                } else {
                                    Log.e("EditUserScreen", "Error al guardar cambios")
                                }
                            } catch (e: Exception) {
                                Log.e("EditUserScreen", "Error en la actualización", e)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar Cambios")
                }
            }
        }
    } ?: run {
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

/**
 * Sanea un objeto Usuario para asegurar que no haya campos nulos al cargar en el formulario.
 *
 * @param usuario Usuario original (con posibles nulos).
 * @return Usuario con campos transformados en cadenas vacías para evitar errores en TextField.
 */
fun sanearUsuarioParaFormulario(usuario: Usuario): Usuario {
    return usuario.copy(
        nombre = usuario.nombre ?: "",
        apellido = usuario.apellido ?: "",
        telefono = usuario.telefono ?: "",
        direccion = usuario.direccion ?: ""
    )
}