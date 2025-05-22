package com.example.bailotecaapp.ui.screens.invitado.components

import android.content.Intent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import lucide.compose.LucideIcons
import lucide.compose.icons.Facebook
import lucide.compose.icons.Twitter
import lucide.compose.icons.Instagram

/**
 * Muestra los botones para acceder a las redes sociales oficiales.
 * Utiliza iconos de la biblioteca Lucide para una apariencia moderna.
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
            icono = LucideIcons.Instagram,
            url = "https://instagram.com/labailoteca"
        )
        RedSocialButton(
            nombre = "X / Twitter",
            icono = LucideIcons.Twitter,
            url = "https://twitter.com/labailoteca"
        )
        RedSocialButton(
            nombre = "Facebook",
            icono = LucideIcons.Facebook,
            url = "https://facebook.com/labailoteca"
        )
    }
}

/**
 * Botón individual de red social con ícono e intent implícito.
 *
 * @param nombre Nombre de la red.
 * @param icono Icono de Lucide correspondiente.
 * @param url URL a abrir en navegador.
 */
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
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icono,
            contentDescription = nombre,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(nombre)
    }
}