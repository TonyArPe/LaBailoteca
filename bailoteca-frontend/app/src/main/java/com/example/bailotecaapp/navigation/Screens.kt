package com.example.bailotecaapp.navigation

/**
 * Representa las rutas de navegación disponibles en la aplicación.
 * Utiliza una sealed class para asegurar tipado seguro en navegación.
 */
sealed class Screens(val route: String) {

    // Autenticación
    object Login : Screens("login")
    object Register : Screens("register")

    // Navegación general
    object Home : Screens("home")
    object EditProfile : Screens("editProfile")
    object Perfil : Screens("perfil")
    object Eventos : Screens("eventos")

    // Gestión de usuarios (solo admins)
    object Usuarios : Screens("usuarios")

    // Gestión de clases
    object Clases : Screens("clases")

    object ClaseDetail : Screens("clase/{claseId}") {
        fun createRoute(claseId: Long) = "clase/$claseId"
    }

    // Rutas para el modo invitado
    object InvitadoHome : Screens("invitado_home")

    // Puedes añadir más rutas si implementas eventos o redes sociales
    // object EventoDetail : Screens("evento/{eventoId}")
    // object RedesSociales : Screens("redes")
}