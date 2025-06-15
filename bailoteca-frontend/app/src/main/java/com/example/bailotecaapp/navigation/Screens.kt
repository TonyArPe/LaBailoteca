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
     // Gestión de eventos
     object CrearEvento : Screens("crear_evento")

    object EventoDetalle : Screens("evento/{eventoId}") {
        fun createRoute(eventoId: Long) = "evento/$eventoId"
    }

    object UsuarioDetalle : Screens("usuario_detalle") {
        fun routeWithArgs(id: Long) = "usuario_detalle/$id"
    }

    // Rutas para el modo invitado
    object InvitadoHome : Screens("invitado_home")

    object EventoList : Screens("eventos")
    object EventoDetail : Screens("evento/{id}") {
        fun createRoute(id: Long) = "evento/$id"
    }
    object CrearEvento : Screens("crear_evento")
    object EditarEvento : Screens("editar_evento/{eventoId}") {
        fun create(id: Long) = "editar_evento/$id"
    }
}