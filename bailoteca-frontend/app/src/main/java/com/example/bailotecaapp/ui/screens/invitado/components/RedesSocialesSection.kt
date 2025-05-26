package com.example.bailotecaapp.ui.screens.invitado.components

import android.content.Intent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri


/**
 * Muestra botones de redes sociales usando Material Icons.
 */
@Composable
fun RedesSocialesSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RedSocialButton(
            nombre = "Instagram",
            icono = Icons.Filled.CameraAlt,
            url = "https://instagram.com/labailoteca"
        )

        RedSocialButton(
            nombre = "X / Twitter",
            icono = Icons.Filled.Close,
            url = "https://twitter.com/labailoteca"
        )

        RedSocialButton(
            nombre = "Facebook",
            icono = Icons.Filled.ThumbUp,
            url = "https://facebook.com/labailoteca"
        )
    }
}

@Composable
fun RedSocialButton(
    nombre: String,
    icono: ImageVector,
    url: String
) {
    val context = LocalContext.current

    Button(
        onClick = {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            context.startActivity(intent)
        },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Icon(icono, contentDescription = nombre, modifier = Modifier.padding(end = 8.dp))
        Text(
            text = nombre,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}