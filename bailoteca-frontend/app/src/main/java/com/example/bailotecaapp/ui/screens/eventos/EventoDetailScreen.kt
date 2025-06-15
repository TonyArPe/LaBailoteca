package com.example.bailotecaapp.ui.screens.eventos

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.bailotecaapp.model.enums.Rol
import com.example.bailotecaapp.navigation.Screens
import com.example.bailotecaapp.ui.theme.Lima
import com.example.bailotecaapp.ui.theme.Magenta
import com.example.bailotecaapp.viewmodel.ApiInitViewModel
import com.example.bailotecaapp.viewmodel.EventoViewModel
import com.example.bailotecaapp.viewmodel.SesionViewModel

/**
 * Pantalla que muestra los detalles de un evento específico.
 *
 * @param navController controlador de navegación para redirigir a edición
 * @param eventoId identificador del evento a mostrar
 */
@Composable
fun EventoDetailScreen(
    navController: NavHostController,
    eventoId: Long,
    eventoViewModel: EventoViewModel = hiltViewModel(),
    apiInitViewModel: ApiInitViewModel = hiltViewModel(),
    sesionViewModel: SesionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val evento by eventoViewModel.eventoSeleccionado.collectAsState()
    val baseUrl by apiInitViewModel.baseUrl.collectAsState()
    val usuario by sesionViewModel.usuario.collectAsState()

    // 🔄 Carga inicial del evento
    LaunchedEffect(eventoId) {
        val token = eventoViewModel.getTokenSafe()
        Log.d("EventoDetailScreen", "🔍 Cargando detalle del evento con ID: $eventoId")
        eventoViewModel.cargarEventoPorId(token, eventoId)
    }

    if (evento == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Lima)
        }
        return
    }

    val imagenFinal = evento!!.imagen?.let {
        "$baseUrl/media/$it?t=${System.currentTimeMillis()}"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        // 🖼 Imagen del evento
        if (!imagenFinal.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imagenFinal)
                    .crossfade(true)
                    .build(),
                contentDescription = "Imagen del evento",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(Color.DarkGray, RoundedCornerShape(16.dp))
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 📝 Detalles del evento
        Text(
            text = evento!!.nombre,
            style = MaterialTheme.typography.headlineMedium,
            color = Lima
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = evento!!.descripcion, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "📍 Lugar: ${evento!!.lugar}", color = Color.White)
        Text(text = "📅 Fecha: ${evento!!.fecha}", color = Color.White)
        Text(text = "⏳ Estado: ${evento!!.estado}", color = Color.White)
        Text(text = "🌐 Público: ${if (evento!!.publico) "Sí" else "No"}", color = Color.White)

        // ✏️ Botón para editar (solo si tiene permisos)
        val puedeEditar = usuario != null && (
                usuario!!.rol == Rol.ADMIN || usuario!!.nombre == evento!!.nombreOrganizador
                )

        if (puedeEditar) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    Log.i("EventoDetailScreen", "✏️ Editando evento ID=${evento!!.id}")
                    navController.navigate(Screens.EditarEvento.create(evento!!.id))
                },
                colors = ButtonDefaults.buttonColors(containerColor = Magenta),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Editar")
            }
        }
    }
}