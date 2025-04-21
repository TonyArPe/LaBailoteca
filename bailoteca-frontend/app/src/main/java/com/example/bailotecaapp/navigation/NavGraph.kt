package com.example.bailotecaapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.bailotecaapp.ui.screens.HomeScreen
import com.example.bailotecaapp.ui.screens.LoginScreen

// Definimos las rutas
sealed class Screens(val route: String) {
    object Login : Screens("login")
    object Home : Screens("home")
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screens.Login.route) {
        composable(Screens.Login.route) {
            LoginScreen(navController)
        }
        composable(Screens.Home.route) {
            HomeScreen()
        }
    }
}
