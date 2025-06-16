package com.example.bailotecaapp.ui.screens.usuarios.perfil

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.bailotecaapp.model.dto.UsuarioUpdateRequest
import com.example.bailotecaapp.viewmodel.UsuarioViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.example.bailotecaapp.viewmodel.ApiInitViewModel
import com.example.bailotecaapp.utils.construirUrlMedia


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
    val scope = rememberCoroutineScope()
    val usuario by viewModel.usuarioDetalle.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }

    /**
     * Llama al ViewModel para cargar el usuario desde el backend.
     */
    LaunchedEffect(usuarioId) {
        viewModel.cargarUsuarioPorId(usuarioId)
    }

    usuario?.let {
        LaunchedEffect(it) {
            nombre = it.nombre ?: ""
            apellido = it.apellido ?: ""
            telefono = it.telefono ?: ""
            direccion = it.direccion ?: ""
        }
        val apiInitViewModel: ApiInitViewModel = hiltViewModel()
        val baseUrl by apiInitViewModel.baseUrl.collectAsState()
        val imagenUrl = construirUrlMedia(it.fotoPerfil, baseUrl)

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Editar Usuario") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!imagenUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imagenUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(140.dp)
                            .align(Alignment.CenterHorizontally)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                    )
                }

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
                                val actualizado = usuario!!.contrasenna?.let { it1 ->
                                    UsuarioUpdateRequest(
                                        nombre = usuario!!.nombre,
                                        apellido = usuario!!.apellido,
                                        correo = usuario!!.correo,
                                        contrasenna = it1,
                                        rol = usuario!!.rol,
                                        telefono = usuario!!.telefono,
                                        direccion = usuario!!.direccion,
                                        fechaNacimiento = usuario!!.fechaNacimiento,
                                        genero = usuario!!.genero,
                                        fotoPerfil = usuario!!.fotoPerfil,
                                        activo = usuario!!.activo,
                                        pagado = usuario!!.pagado
                                    )
                                }
                                val success = actualizado?.let { it1 ->
                                    viewModel.actualizarUsuario(token, usuarioId,
                                        it1
                                    )
                                }
                                if (success == true) {
                                    navController.navigate("usuarios") {
                                        popUpTo("editar_usuario/{$usuarioId}") { inclusive = true }
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
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: $errorMessage")
            }
        }
    }
}