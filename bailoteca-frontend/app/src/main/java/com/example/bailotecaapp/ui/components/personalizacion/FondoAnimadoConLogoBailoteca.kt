package com.example.bailotecaapp.ui.components.personalizacion

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.bailotecaapp.R
import kotlinx.coroutines.delay

@Composable
fun FondoAnimadoConLogoBailoteca(content: @Composable () -> Unit) {
    val visible = remember { mutableStateOf(false) }

    // Disparar animación al iniciar
    LaunchedEffect(Unit) {
        delay(300)
        visible.value = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Imagen animada
        AnimatedVisibility(
            visible = visible.value,
            enter = fadeIn() + scaleIn(initialScale = 0.7f),
            exit = fadeOut()
        ) {
            Image(
                painter = painterResource(id = R.drawable.bailoteca_background),
                contentDescription = "Logo Bailoteca",
                modifier = Modifier
                    .size(180.dp)
                    .align(Alignment.Center)
            )
        }

        // Capa opcional para contenido
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 280.dp) // contenido debajo del logo
        ) {
            content()
        }
    }
}