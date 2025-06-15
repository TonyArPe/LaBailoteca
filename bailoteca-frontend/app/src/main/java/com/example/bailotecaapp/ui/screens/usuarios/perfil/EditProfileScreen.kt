package com.example.bailotecaapp.ui.screens.usuarios.perfil

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.bailotecaapp.model.dto.UsuarioUpdateRequest
import com.example.bailotecaapp.ui.theme.Lima
import com.example.bailotecaapp.ui.theme.Magenta
import com.example.bailotecaapp.utils.construirUrlMedia
import com.example.bailotecaapp.utils.crearMultipartDesdeUri
import com.example.bailotecaapp.viewmodel.ApiInitViewModel
import com.example.bailotecaapp.viewmodel.SesionViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavHostController,
    sesionViewModel: SesionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val apiInitViewModel: ApiInitViewModel = hiltViewModel()
    val baseUrl by apiInitViewModel.baseUrl.collectAsState()

    val usuario by sesionViewModel.usuario.collectAsState()
    val usuarioCargado by sesionViewModel.usuarioCargado.collectAsState()
    val isLoading by sesionViewModel.isLoading.collectAsState()

    if (usuario == null || !usuarioCargado || isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val usuarioActual = usuario!!

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var nombre by remember { mutableStateOf(usuarioActual.nombre) }
    var telefono by remember { mutableStateOf(usuarioActual.telefono ?: "") }
    var direccion by remember { mutableStateOf(usuarioActual.direccion ?: "") }
    var fechaNacimiento by remember { mutableStateOf(usuarioActual.fechaNacimiento ?: "") }
    var genero by remember { mutableStateOf(usuarioActual.genero ?: "") }

    var imagenUri by remember { mutableStateOf<Uri?>(null) }
    val imagenSubidaNombre = remember { mutableStateOf<String?>(usuarioActual.fotoPerfil) }

    val imagenFinal = construirUrlMedia(imagenSubidaNombre.value, baseUrl)

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imagenUri = uri
        uri?.let {
            val archivoPart = crearMultipartDesdeUri(context, it)
            sesionViewModel.subirImagenPerfil(archivoPart) { nombreArchivo ->
                imagenSubidaNombre.value = nombreArchivo

                scope.launch {
                    // ⚠️ Nueva lógica para actualizar perfil con imagen subida
                    val token = Firebase.auth.currentUser?.getIdToken(true)?.await()?.token
                    if (token != null) {
                        val actualizado = UsuarioUpdateRequest(
                            nombre = usuarioActual.nombre,
                            apellido = usuarioActual.apellido ?: "",
                            correo = usuarioActual.correo,
                            contrasenna = usuarioActual.contrasenna,
                            rol = usuarioActual.rol,
                            telefono = usuarioActual.telefono,
                            direccion = usuarioActual.direccion,
                            fechaNacimiento = usuarioActual.fechaNacimiento,
                            genero = usuarioActual.genero,
                            fotoPerfil = nombreArchivo,
                            activo = usuarioActual.activo,
                            pagado = usuarioActual.pagado
                        )

                        Log.d("EditProfileScreen", "🖼️ Actualizando usuario con nueva imagen: $actualizado")

                        sesionViewModel.actualizarPerfil(
                            usuarioActualizado = actualizado,
                            onSuccess = {
                                sesionViewModel.sincronizarDesdeSesionManager()
                            },
                            onError = { msg ->
                                Log.e("EditProfileScreen", "❌ Error al actualizar imagen: $msg")
                            }
                        )
                    }
                }
            }
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Editar perfil") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Edita tu información", style = MaterialTheme.typography.titleMedium)

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imagenFinal)
                    .crossfade(true)
                    .build(),
                contentDescription = "Foto de perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
            )

            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Magenta),
            ) {
                Text("Seleccionar imagen", color = Color.White)
            }

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Dirección") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = fechaNacimiento,
                onValueChange = { fechaNacimiento = it },
                label = { Text("Fecha de nacimiento (AAAA-MM-DD)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = genero,
                onValueChange = { genero = it },
                label = { Text("Género") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val actualizado = UsuarioUpdateRequest(
                        nombre = nombre,
                        apellido = usuarioActual.apellido ?: "",
                        correo = usuarioActual.correo,
                        contrasenna = usuarioActual.contrasenna,
                        rol = usuarioActual.rol,
                        telefono = telefono,
                        direccion = direccion,
                        fechaNacimiento = fechaNacimiento,
                        genero = genero,
                        fotoPerfil = imagenSubidaNombre.value,
                        activo = usuarioActual.activo,
                        pagado = usuarioActual.pagado
                    )

                    Log.d("EditProfileScreen", "📤 Enviando actualización: $actualizado")

                    sesionViewModel.actualizarPerfil(
                        usuarioActualizado = actualizado,
                        onSuccess = {
                            scope.launch {
                                sesionViewModel.sincronizarDesdeSesionManager()
                                navController.popBackStack()
                            }
                        },
                        onError = { errorMsg ->
                            errorMessage = errorMsg
                            showErrorDialog = true
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Lima)
            ) {
                Text("Guardar cambios", style = MaterialTheme.typography.labelLarge)
            }

            if (showErrorDialog) {
                AlertDialog(
                    onDismissRequest = { showErrorDialog = false },
                    confirmButton = {
                        TextButton(onClick = { showErrorDialog = false }) {
                            Text("Aceptar")
                        }
                    },
                    title = { Text("Error al guardar perfil") },
                    text = { Text(errorMessage) }
                )
            }
        }
    }
}