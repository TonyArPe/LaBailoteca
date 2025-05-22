package com.example.bailotecaapp.ui.screens.invitado.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri

@Composable
fun RedesSocialesSection() {
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = {
            context.startActivity(Intent(Intent.ACTION_VIEW,
                "https://instagram.com/labailoteca".toUri()))
        }) {
            Text("Instagram")
        }

        Button(onClick = {
            context.startActivity(Intent(Intent.ACTION_VIEW, "https://x.com/labailoteca".toUri()))
        }) {
            Text("X / Twitter")
        }

        Button(onClick = {
            context.startActivity(Intent(Intent.ACTION_VIEW,
                "https://facebook.com/labailoteca".toUri()))
        }) {
            Text("Facebook")
        }
    }
}