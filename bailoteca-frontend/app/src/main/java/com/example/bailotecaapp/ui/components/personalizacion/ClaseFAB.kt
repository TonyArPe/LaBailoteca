package com.example.bailotecaapp.ui.components.personalizacion

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun ClaseFAB(
    onClick: () -> Unit,
    expanded: Boolean = true
) {
    val scale = remember { androidx.compose.animation.core.Animatable(1f) }
    val scope = rememberCoroutineScope()

    // Animación de aparición inicial o expansión
    LaunchedEffect(expanded) {
        scale.animateTo(
            if (expanded) 1f else 0.9f,
            animationSpec = androidx.compose.animation.core.spring(
                dampingRatio = 0.6f,
                stiffness = 300f
            )
        )
    }

    FloatingActionButton(
        onClick = {
            // Animación dentro de coroutine scope
            scope.launch {
                scale.snapTo(0.95f)
                scale.animateTo(1f)
                onClick()
            }
        },
        shape = MaterialTheme.shapes.large,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        ),
        modifier = Modifier
            .scale(scale.value)
            .padding(8.dp)
    ) {
        Text("+", style = MaterialTheme.typography.titleMedium)
    }
}
