package com.example.bailotecaapp.ui.screens.invitado

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.bailotecaapp.model.Clase
import androidx.core.net.toUri

@Composable
fun ClaseCardInvitado(clase: Clase) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(clase.titulo, style = MaterialTheme.typography.titleLarge)
            Text("Profesor: ${clase.profesor.nombre}")

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val email = clase.profesor?.correo ?: return@Button
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = "mailto:$email".toUri()
                        putExtra(Intent.EXTRA_SUBJECT, "Consulta sobre la clase ${clase.titulo}")
                        putExtra(Intent.EXTRA_TEXT, "Hola profesor/a, me gustaría saber más sobre la clase '${clase.titulo}'. Gracias.")
                    }
                    context.startActivity(intent)
                }
            ) {
                Text("Contactar profesor")
            }
        }
    }
}