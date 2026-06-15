package com.example.ubicafii.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ubicafii.ui.admin.AdminScreen
import com.example.ubicafii.ui.detail.DetailScreen
import com.example.ubicafii.ui.floors.FloorsScreen
import com.example.ubicafii.ui.home.HomeScreen
import com.example.ubicafii.ui.search.SearchScreen
import com.example.ubicafii.ui.splash.SplashScreen
import com.example.ubicafii.ui.theme.BluePrimary
import com.example.ubicafii.ui.theme.MutedForeground
import com.example.ubicafii.ui.theme.home.HomeViewModel

class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()
    private var deepLinkEspacioId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Capturar deep link al iniciar la app en frío
        intent?.data?.getQueryParameter("id")?.toIntOrNull()?.let {
            deepLinkEspacioId = it
        }

        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                var mostrarDialogoAdmin by remember { mutableStateOf(false) }
                var pinIngresado by remember { mutableStateOf("") }
                val context = LocalContext.current

                // Escuchar deep links en caliente (cuando la app ya está abierta)
                LaunchedEffect(intent) {
                    intent?.data?.getQueryParameter("id")?.toIntOrNull()?.let { id ->
                        navController.navigate("detail/$id") {
                            launchSingleTop = true
                        }
                    }
                }

                Scaffold(
                    bottomBar = {
                        if (currentRoute in listOf("home", "search", "admin")) {
                            NavigationBar {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                                    label = { Text("Inicio") },
                                    selected = currentRoute == "home",
                                    onClick = {
                                        // MEJORA: Evita duplicar pantallas al navegar a Home
                                        navController.navigate("home") {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Search, contentDescription = null) },
                                    label = { Text("Buscar") },
                                    selected = currentRoute == "search",
                                    onClick = {
                                        // MEJORA: Evita duplicar pantallas al navegar a Buscar
                                        navController.navigate("search") {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null) },
                                    label = { Text("Admin") },
                                    selected = currentRoute == "admin",
                                    onClick = { mostrarDialogoAdmin = true }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "splash",
                        modifier = Modifier.padding(innerPadding),
                        enterTransition = {
                            slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(400)) +
                                    fadeIn(animationSpec = tween(400))
                        },
                        exitTransition = {
                            slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(400)) +
                                    fadeOut(animationSpec = tween(400))
                        },
                        popEnterTransition = {
                            slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(400)) +
                                    fadeIn(animationSpec = tween(400))
                        },
                        popExitTransition = {
                            slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(400)) +
                                    fadeOut(animationSpec = tween(400))
                        }
                    ) {
                        composable("splash") {
                            SplashScreen {
                                val destinoId = deepLinkEspacioId
                                if (destinoId != null) {
                                    deepLinkEspacioId = null
                                    navController.navigate("detail/$destinoId") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                } else {
                                    navController.navigate("home") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            }
                        }

                        composable("home") {
                            HomeScreen(
                                viewModel = homeViewModel,
                                onNavigateToSearch = { navController.navigate("search") },
                                onNavigateToBlocks = { bloqueId -> navController.navigate("floors/$bloqueId") },
                                onNavigateToAllBlocks = { },
                                onNavigateToDetail = { id -> navController.navigate("detail/$id") }
                            )
                        }

                        composable("search") {
                            SearchScreen(
                                onBack = { navController.popBackStack() },
                                onSpaceClick = { id -> navController.navigate("detail/$id") }
                            )
                        }

                        composable(
                            "floors/{bloqueId}",
                            arguments = listOf(navArgument("bloqueId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val bloqueId = backStackEntry.arguments?.getString("bloqueId") ?: "A"
                            FloorsScreen(
                                bloqueId = bloqueId,
                                onBack = { navController.popBackStack() },
                                onSpaceClick = { id -> navController.navigate("detail/$id") }
                            )
                        }

                        composable(
                            "detail/{espacioId}",
                            arguments = listOf(navArgument("espacioId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val espacioId = backStackEntry.arguments?.getString("espacioId")?.toIntOrNull() ?: 0
                            DetailScreen(
                                espacioId = espacioId,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("admin") {
                            AdminScreen(onBack = { navController.popBackStack() })
                        }
                    }
                }

                // Diálogo de PIN
                if (mostrarDialogoAdmin) {
                    AlertDialog(
                        onDismissRequest = {
                            mostrarDialogoAdmin = false
                            pinIngresado = ""
                        },
                        title = {
                            Text("Acceso de Administrador", fontWeight = FontWeight.Bold, color = BluePrimary)
                        },
                        text = {
                            OutlinedTextField(
                                value = pinIngresado,
                                onValueChange = { pinIngresado = it },
                                label = { Text("PIN") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BluePrimary,
                                    focusedLabelColor = BluePrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                if (pinIngresado == "1234") {
                                    mostrarDialogoAdmin = false
                                    pinIngresado = ""
                                    navController.navigate("admin") {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                } else {
                                    android.widget.Toast.makeText(context, "PIN incorrecto", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Text("Entrar", color = BluePrimary, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                mostrarDialogoAdmin = false
                                pinIngresado = ""
                            }) {
                                Text("Cancelar", color = MutedForeground)
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}