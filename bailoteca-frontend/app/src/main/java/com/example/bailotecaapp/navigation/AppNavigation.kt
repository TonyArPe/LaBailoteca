package com.example.bailotecaapp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.bailotecaapp.ui.screens.*

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        modifier = modifier,
        startDestination = Screens.Login.route
    ) {
        composable(Screens.Login.route) {
            LoginScreen(navController)
        }
        composable(Screens.Home.route) {
            HomeScreen(navController)
        }
        composable(Screens.Clases.route) {
            ClaseListScreen(navController)
        }
        composable(Screens.Usuarios.route) {
            UserListScreen(navController)
        }
        composable(Screens.Register.route) {
            RegisterScreen(navController)
        }
        composable(
            route = Screens.ClaseDetail.route,
            arguments = listOf(navArgument("claseId") { defaultValue = -1L })
        ) { backStackEntry ->
            val claseId = backStackEntry.arguments?.getLong("claseId") ?: -1L
            ClaseDetailScreen(navController, claseId)
        }
        composable(Screens.Perfil.route) {
            ProfileScreen(navController)
        }
        composable(Screens.EditProfile.route) {
            EditProfileScreen(navController)
        }
    }
}