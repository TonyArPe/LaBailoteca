import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.bailotecaapp.model.enums.Rol
import com.example.bailotecaapp.navigation.Screens
import com.example.bailotecaapp.viewmodel.LoginViewModel
import com.example.bailotecaapp.viewmodel.SesionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: LoginViewModel = viewModel(),
    sesionViewModel: SesionViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val usuario by sesionViewModel.usuario.collectAsState()
    val sesionCargando by sesionViewModel.isLoading.collectAsState()
    val modoInvitado by sesionViewModel.modoInvitadoForzado.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val invitado by sesionViewModel.modoInvitado.collectAsState()
    val modoInvitadoForzado by sesionViewModel.modoInvitadoForzado.collectAsState()

    LaunchedEffect(usuario, sesionCargando) {
        if (usuario != null && usuario!!.rol != Rol.INVITADO && !sesionCargando) {
            Log.d("LoginScreen", "Usuario autenticado normal, navegando a Home")
            navController.navigate(Screens.Home.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(invitado, usuario) {
        if (invitado && usuario?.rol == Rol.INVITADO) {
            Log.d("LoginScreen", "Modo invitado detectado. Navegando a invitado_home")
            navController.navigate("invitado_home") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "La Bailoteca",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    isLoading = true
                    errorMessage = null
                    viewModel.login(
                        email,
                        password,
                        onSuccess = {
                            if (!sesionViewModel.usuarioYaCargado()) {
                                sesionViewModel.obtenerUsuarioActual()
                                sesionViewModel.cargarMisInscripciones()
                            }
                            isLoading = false
                        },
                        onError = { error ->
                            isLoading = false
                            errorMessage = error
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && !sesionCargando
            ) {
                Text(text = if (isLoading || sesionCargando) "Cargando..." else "Iniciar sesión")
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = it, color = Color.Red, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { navController.navigate(Screens.Register.route) }) {
                Text("¿No tienes cuenta? Regístrate aquí")
            }

            /**
             * Entrar como invitado
             */
            TextButton(
                onClick = {
                    Log.d("LoginScreen", "Botón invitado pulsado")
                    sesionViewModel.entrarComoInvitado {
                        Log.d("LoginScreen", "Modo invitado propagado. Navegando a invitado_home")
                        navController.navigate("invitado_home") {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Entrar como invitado")
            }
        }
    }
}