package com.example.bailotecaapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.bailotecaapp.ui.screens.HomeScreen
import com.example.bailotecaapp.ui.screens.LoginScreen
import com.example.bailotecaapp.ui.screens.RegisterScreen
import com.example.bailotecaapp.ui.screens.UserListScreen
import com.example.bailotecaapp.ui.screens.ClaseListScreen

// Definimos las rutas
sealed class Screens(val route: String) {
    object Login : Screens("login")
    object Home : Screens("home")
    object Register : Screens("register")
    object Usuarios : Screens("usuarios")
    object Clases : Screens("clases")

}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screens.Login.route) {
        composable(Screens.Login.route) {
            LoginScreen(navController)
        }
        composable(Screens.Home.route) {
            HomeScreen(navController)
        }
        composable("register") {
            RegisterScreen(navController)
        }
        composable("usuarios") {
            UserListScreen()
        }
        composable("clases") {
            ClaseListScreen()
        }

    }
}