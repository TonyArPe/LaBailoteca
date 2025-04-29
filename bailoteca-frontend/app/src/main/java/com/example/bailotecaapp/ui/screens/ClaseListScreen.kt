package com.example.bailotecaapp.ui.screens

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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.bailotecaapp.model.InscripcionRequest
import com.example.bailotecaapp.navigation.Screens
import com.example.bailotecaapp.ui.components.ClaseCard
import com.example.bailotecaapp.viewmodel.ClaseViewModel
import com.example.bailotecaapp.viewmodel.SesionViewModel
import com.example.bailotecaapp.network.RetrofitInstance
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun ClaseListScreen(
    navController: NavHostController,
    viewModel: ClaseViewModel = viewModel()
) {
    val clases by viewModel.clases.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorMessage.collectAsState()
    val sesionViewModel: SesionViewModel = viewModel()
    val usuario by sesionViewModel.usuario.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    fun inscribirseAClase(claseId: Long) {
        val userId = usuario?.id ?: return
        val request = InscripcionRequest(usuarioId = userId, claseId = claseId)

        coroutineScope.launch {
            try {
                val token =
                    Firebase.auth.currentUser?.getIdToken(false)?.await()?.token ?: return@launch
                val response = RetrofitInstance.api.inscribirseClase("Bearer $token", request)

                if (response.isSuccessful) {
                    Toast.makeText(context, "Inscripción realizada con éxito", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(context, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al inscribirse: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.obtenerClases()
    }

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

                else -> LazyColumn {
                    items(clases) { clase ->
                        ClaseCard(
                            clase = clase,
                            usuarioActual = usuario,
                            onInscribirse = { claseId -> inscribirseAClase(claseId) },
                            onVerDetalle = { claseId ->
                                navController.navigate(Screens.ClaseDetail.createRoute(claseId))
                            }
                        )
                    }
                }
            }
        }
    }
}