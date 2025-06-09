package com.example.bailotecaapp.ui.screens.eventos

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.bailotecaapp.viewmodel.EventoViewModel
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier

@Composable
fun EventoDetailScreen(
    viewModel: EventoViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val evento by viewModel.eventoSeleccionado.collectAsState()

    if (evento == null) {
        Text("No hay evento seleccionado")
        return
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = evento!!.nombre, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = evento!!.descripcion)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Lugar: ${evento!!.lugar}")
        Text(text = "Fecha: ${evento!!.fecha}")
        Text(text = "Estado: ${evento!!.estado}")
        Text(text = "Público: ${if (evento!!.publico) "Sí" else "No"}")
    }
}