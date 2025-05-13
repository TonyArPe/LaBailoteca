package com.example.bailotecaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bailotecaapp.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint
import com.example.bailotecaapp.navigation.MainScaffold
import com.example.bailotecaapp.navigation.Screens
import com.example.bailotecaapp.ui.theme.BailotecaAppTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BailotecaAppTheme {
                val navController = rememberNavController()
                MainScaffold(navController = navController)
            }
        }
    }
}