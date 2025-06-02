package com.example.bailotecaapp.ui.screens.clases

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.bailotecaapp.model.dto.InscripcionRequest
import com.example.bailotecaapp.model.enums.Rol
import com.example.bailotecaapp.navigation.Screens
import com.example.bailotecaapp.ui.components.ClaseCard
import com.example.bailotecaapp.viewmodel.ClaseViewModel
import com.example.bailotecaapp.viewmodel.SesionViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.bailotecaapp.ui.components.personalizacion.ClaseFAB

/**
 * Pantalla que muestra el listado de clases disponibles para inscribirse.
 */
@Composable
fun ClaseListScreen(
    navController: NavHostController,
    viewModel: ClaseViewModel = hiltViewModel(),
    sesionViewModel: SesionViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val clases by viewModel.clases.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorMessage.collectAsState()
    val usuario by sesionViewModel.usuario.collectAsState()
    val inscripciones by sesionViewModel.inscripciones.collectAsState()
    val versionClases by sesionViewModel.versionClases.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(versionClases) {
        viewModel.obtenerClases()
        sesionViewModel.cargarMisInscripciones()
    }

    fun inscribirseAClase(claseId: Long) {
        val userId = usuario?.id ?: run {
            Log.w("ClaseListScreen", "⚠️ Usuario nulo al intentar inscribirse")
            return
        }

        val request = InscripcionRequest(usuarioId = userId, claseId = claseId)

        coroutineScope.launch {
            val currentUser = Firebase.auth.currentUser
            if (currentUser == null) {
                Toast.makeText(context, "Usuario Firebase nulo", Toast.LENGTH_SHORT).show()
                Log.w("ClaseListScreen", "⚠️ Firebase user es null")
                return@launch
            }

            try {
                val token = currentUser.getIdToken(false).await().token
                if (token.isNullOrBlank()) {
                    Toast.makeText(context, "Token inválido", Toast.LENGTH_SHORT).show()
                    Log.w("ClaseListScreen", "⚠️ Token JWT nulo o vacío")
                    return@launch
                }

                val response = viewModel.inscribirseAClase(token, request)

                if (response.isSuccessful) {
                    Toast.makeText(context, "Inscripción realizada con éxito", Toast.LENGTH_SHORT).show()
                    sesionViewModel.cargarMisInscripciones()
                    sesionViewModel.marcarClasesComoActualizadas()
                } else {
                    Toast.makeText(context, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    Log.e("ClaseListScreen", "❌ Error HTTP: ${response.code()}")
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al inscribirse: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("ClaseListScreen", "❌ Excepción al inscribirse: ${e.message}")
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            if (usuario?.rol == Rol.PROFESOR) {
                ClaseFAB(onClick = { navController.navigate("crearEditarClase") })
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                error != null -> Text(
                    text = error ?: "Error desconocido",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
                usuario == null -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                else -> LazyColumn(
                    contentPadding = PaddingValues(
                        top = 16.dp,
                        bottom = 100.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(clases, key = { it.id }) { clase ->
                        val yaInscrito = inscripciones.any { it.clase.id == clase.id }

                        if (usuario!!.rol == Rol.PROFESOR && clase.profesor.id == usuario!!.id) {
                            ClaseCardProfesor(
                                clase = clase,
                                usuarioActual = usuario!!,
                                navController = navController,
                                claseViewModel = viewModel,
                                sesionViewModel = sesionViewModel
                            )
                        } else {
                            ClaseCard(
                                clase = clase,
                                usuarioActual = usuario,
                                inscripciones = inscripciones,
                                yaInscrito = yaInscrito,
                                onInscribirse = {
                                    if (usuario!!.rol != Rol.INVITADO) {
                                        inscribirseAClase(it)
                                    } else {
                                        Toast.makeText(context, "Inicia sesión para inscribirte", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onVerDetalle = { navController.navigate(Screens.ClaseDetail.createRoute(it)) }
                            )
                        }
                    }
                }
            }
        }
    }
}