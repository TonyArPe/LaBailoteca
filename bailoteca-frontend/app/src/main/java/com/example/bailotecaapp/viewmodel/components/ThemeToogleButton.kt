import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.bailotecaapp.viewmodel.ThemeViewModel
import androidx.compose.runtime.collectAsState

/**
 * Botón de cambio de tema que alterna entre modo claro y oscuro.
 *
 * @param themeViewModel ViewModel que gestiona el tema actual.
 */
@Composable
fun ThemeToggleButton(
    themeViewModel: ThemeViewModel,
    modifier: Modifier = Modifier
) {
    val isDark by themeViewModel.isDarkTheme.collectAsState()

    IconButton(
        onClick = {
            themeViewModel.toggleDarkTheme(!isDark)
        },
        modifier = modifier
    ) {
        Icon(
            imageVector = if (isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
            contentDescription = if (isDark) "Modo claro" else "Modo oscuro",
            tint = MaterialTheme.colorScheme.onBackground
        )
    }
}