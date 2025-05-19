package com.example.bailotecaapp.ui.screens

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
import com.example.bailotecaapp.navigation.Screens
import com.example.bailotecaapp.ui.components.ClaseCard
import com.example.bailotecaapp.viewmodel.ClaseViewModel
import com.example.bailotecaapp.viewmodel.SesionViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Pantalla principal que muestra el listado de clases disponibles para el usuario.
 * Permite inscribirse, ver detalles y muestra el estado actual de las inscripciones.
 *
 * @param navController controlador de navegación.
 * @param viewModel ViewModel encargado de las clases.
 * @param sesionViewModel ViewModel encargado de la sesión.
 */
@Composable
fun ClaseListScreen(
    navController: NavHostController,
    viewModel: ClaseViewModel = hiltViewModel(),
    sesionViewModel: SesionViewModel = hiltViewModel()
) {
    val clases by viewModel.clases.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorMessage.collectAsState()
    val usuario by sesionViewModel.usuario.collectAsState()
    val inscripciones by sesionViewModel.inscripciones.collectAsState()
    val versionClases by sesionViewModel.versionClases.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    /**
     * Efecto que recarga las clases cuando cambia la versión.
     */
    LaunchedEffect(versionClases) {
        viewModel.obtenerClases()
    }

    /**
     * Efecto inicial que asegura que se haya cargado el usuario y sus inscripciones.
     */
    LaunchedEffect(usuario) {
        if (usuario == null) {
            sesionViewModel.obtenerUsuarioActual()
        } else {
            viewModel.obtenerClases()
            sesionViewModel.cargarMisInscripciones()
        }
    }

    /**
     * Función que gestiona la inscripción a una clase concreta por ID.
     * Recupera el token, llama al ViewModel, muestra mensaje y recarga datos.
     */
    fun inscribirseAClase(claseId: Long) {
        val userId = usuario?.id ?: return
        val request = InscripcionRequest(usuarioId = userId, claseId = claseId)

        coroutineScope.launch {
            try {
                val token = Firebase.auth.currentUser?.getIdToken(false)?.await()?.token ?: return@launch
                val response = viewModel.inscribirseAClase(token, request)

                if (response.isSuccessful) {
                    Toast.makeText(context, "Inscripción realizada con éxito", Toast.LENGTH_SHORT).show()
                    sesionViewModel.cargarMisInscripciones()
                    sesionViewModel.marcarClasesComoActualizadas()
                    Log.d("ClaseListScreen", "Inscripciones y clases recargadas tras inscribirse")
                } else {
                    Toast.makeText(context, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    Log.e("ClaseListScreen", "Error HTTP: ${response.code()}")
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al inscribirse: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("ClaseListScreen", "Excepción: ${e.localizedMessage}")
            }
        }
    }

    /**
     * UI principal
     */
    Scaffold { padding ->
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

                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(
                        clases,
                        key = { clase -> "${clase.id}-${inscripciones.any { it.clase?.id == clase.id }}" }
                    ) { clase ->
                        val yaInscrito = inscripciones.any { it.clase?.id == clase.id }

                        ClaseCard(
                            clase = clase,
                            usuarioActual = usuario,
                            inscripciones = inscripciones,
                            yaInscrito = yaInscrito,
                            onInscribirse = { claseId -> inscribirseAClase(claseId) },
                            onVerDetalle = { claseId -> navController.navigate(Screens.ClaseDetail.createRoute(claseId)) }
                        )
                    }
                }
            }
        }
    }
}