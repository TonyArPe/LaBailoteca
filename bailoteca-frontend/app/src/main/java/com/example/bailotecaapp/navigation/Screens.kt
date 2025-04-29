package com.example.bailotecaapp.navigation

sealed class Screens(val route: String) {
    object Login : Screens("login")
    object Home : Screens("home")
    object Register : Screens("register")
    object Usuarios : Screens("usuarios")
    object Clases : Screens("clases")
    object ClaseDetail : Screens("clase/{claseId}") {
        fun createRoute(claseId: Long) = "clase/$claseId"
    }
    object Perfil : Screens("perfil")
}
