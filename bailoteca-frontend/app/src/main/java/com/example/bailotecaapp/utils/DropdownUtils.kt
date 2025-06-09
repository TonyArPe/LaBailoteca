package com.example.bailotecaapp.utils

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

/**
 * Componente reutilizable que muestra un menú desplegable para seleccionar un valor de un Enum.
 *
 * Ideal para enums como EstadoEvento, Dificultad, etc.
 *
 * @param T Tipo del enum que se desea mostrar.
 * @param selected Valor actualmente seleccionado.
 * @param onSelected Callback que se ejecuta cuando el usuario selecciona un valor.
 * @param opciones Lista de opciones a mostrar en el menú.
 */
@Composable
fun <T : Enum<T>> DropdownMenuBox(
    selected: T,
    onSelected: (T) -> Unit,
    opciones: List<T>
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = selected.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Seleccionar estado") },
            modifier = Modifier,
            trailingIcon = {
                IconButton(onClick = {
                    expanded = true
                    Log.d("DropdownMenuBox", "📂 Menú desplegado para ${selected::class.simpleName}")
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Abrir menú"
                    )
                }
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                Log.d("DropdownMenuBox", "📁 Menú cerrado sin selección")
            }
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion.name.replaceFirstChar { it.uppercase() }) },
                    onClick = {
                        Log.d("DropdownMenuBox", "✅ Opción seleccionada: ${opcion.name}")
                        onSelected(opcion)
                        expanded = false
                    }
                )
            }
        }
    }
}