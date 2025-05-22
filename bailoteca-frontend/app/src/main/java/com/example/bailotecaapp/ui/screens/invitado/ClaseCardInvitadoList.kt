package com.example.bailotecaapp.ui.screens.invitado.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bailotecaapp.model.Clase
import com.example.bailotecaapp.ui.screens.invitado.ClaseCardInvitado

/**
 * Muestra una lista vertical de tarjetas de clases públicas para modo invitado.
 *
 * @param clases Lista de clases públicas obtenidas del servidor.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ClaseCardInvitadoList(clases: List<Clase>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        clases.forEach { clase ->
            ClaseCardInvitado(clase = clase)
        }
    }
}