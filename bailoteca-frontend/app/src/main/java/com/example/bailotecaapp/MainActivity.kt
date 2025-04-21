package com.example.bailotecaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.bailotecaapp.navigation.AppNavigation
import com.example.bailotecaapp.ui.theme.BailotecaAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BailotecaAppTheme {
                val navController = rememberNavController()
                AppNavigation(navController)
            }
        }
    }
}
