package com.example.bailotecaapp.ui.screens.invitado.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri

@Composable
fun RedesSocialesSection() {
    val redes = listOf(
        "Instagram" to "https://instagram.com/labailoteca",
        "Facebook" to "https://facebook.com/labailoteca",
        "X (Twitter)" to "https://twitter.com/labailoteca"
    )

    val context = LocalContext.current

    Column {
        redes.forEach { (nombre, url) ->
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text("Ir a $nombre")
            }
        }
    }
}