package com.example.bailotecaapp.ui.components

/**
 * Representa las rutas y etiquetas disponibles en el menú lateral.
 */
sealed class DrawerDestination(val route: String, val label: String) {
    object Home : DrawerDestination("home", "Inicio")
    object Clases : DrawerDestination("clases", "Clases")
    object Usuarios : DrawerDestination("usuarios", "Usuarios")
    object Perfil : DrawerDestination("perfil", "Mi perfil")
    object Logout : DrawerDestination("logout", "Cerrar sesión")
}
