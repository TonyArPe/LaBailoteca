package com.example.bailotecaapp.ui.screens.invitado

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.bailotecaapp.navigation.Screens
import com.example.bailotecaapp.ui.screens.invitado.components.ClaseCardInvitadoList
import com.example.bailotecaapp.ui.screens.invitado.components.EventoCardInvitadoList
import com.example.bailotecaapp.ui.screens.invitado.components.ExpandibleCard
import com.example.bailotecaapp.ui.screens.invitado.components.RedesSocialesSection
import com.example.bailotecaapp.ui.screens.invitado.components.SeccionExpandable
import com.example.bailotecaapp.viewmodel.ClaseViewModel
import com.example.bailotecaapp.viewmodel.EventoViewModel
import com.example.bailotecaapp.viewmodel.SesionViewModel
import kotlinx.coroutines.launch

/**
 * Pantalla de inicio para el usuario invitado.
 * Muestra la lista de eventos públicos y clases disponibles
 * sin posibilidad de interacción (más allá de ver detalles o contactar al profesor).
 *
 * @param claseViewModel ViewModel que gestiona las clases públicas.
 * @param eventoViewModel ViewModel que gestiona los eventos públicos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun InvitadoHomeScreen(
    navController: NavHostController,
    sesionViewModel: SesionViewModel = hiltViewModel(),
    claseViewModel: ClaseViewModel = hiltViewModel(),
    eventoViewModel: EventoViewModel = hiltViewModel()
) {
    val clases by claseViewModel.clases.collectAsState()
    val eventos by eventoViewModel.eventos.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var isDarkMode by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        eventoViewModel.obtenerEventosPublicos()
    }

    @Composable
    fun ModoOscuroToggle(
        isDark: Boolean,
        onToggle: (Boolean) -> Unit
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Modo oscuro")
            Switch(checked = isDark, onCheckedChange = onToggle)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContentInvitado(
                onInicioClick = {},
                onPerfilClick = {},
                onCerrarSesion = {
                    sesionViewModel.cerrarSesion()
                    navController.navigate("login") {
                        popUpTo("invitado_home") { inclusive = true }
                    }
                }
            )
        },
        scrimColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 1f)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Modo Invitado") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    }
                )
            },
            content = { padding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    item {
                        val texto = buildAnnotatedString {
                            append("Estás en modo invitado. ")
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append("Regístrate")
                            }
                            append(" para inscribirte a clases y eventos.")
                        }

                        ClickableText(
                            text = texto,
                            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            onClick = { offset ->
                                if (offset in 27..36) {
                                    navController.navigate(Screens.Register.route)
                                }
                            }
                        )
                    }

                    item {
                        ExpandibleCard(seccion = SeccionExpandable("Clases públicas")) {
                            ClaseCardInvitadoList(clases = clases)
                        }
                    }

                    item {
                        ExpandibleCard(seccion = SeccionExpandable("Eventos públicos")) {
                            EventoCardInvitadoList(eventos = eventos, navController = navController)
                        }
                    }

                    item {
                        ExpandibleCard(seccion = SeccionExpandable("Redes sociales")) {
                            RedesSocialesSection()
                        }
                    }
                }
            }
        )
    }
}