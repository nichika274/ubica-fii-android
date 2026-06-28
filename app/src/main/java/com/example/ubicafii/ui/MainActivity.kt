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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ubicafii.ui.admin.AdminScreen
import com.example.ubicafii.ui.detail.DetailScreen
import com.example.ubicafii.ui.explore.ExploreScreen
import com.example.ubicafii.ui.floors.FloorsScreen
import com.example.ubicafii.ui.home.HomeScreen
import com.example.ubicafii.ui.profile.ProfileScreen
import com.example.ubicafii.ui.search.SearchScreen
import com.example.ubicafii.ui.splash.SplashScreen
import com.example.ubicafii.ui.theme.home.HomeViewModel
import com.example.ubicafii.ui.theme.UbicaFIITheme

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
            // Usamos rememberSaveable para que el estado del tema persista al rotar la pantalla
            var isDarkTheme by rememberSaveable { mutableStateOf(false) }

            UbicaFIITheme(
                darkTheme = isDarkTheme
            ) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

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
                        if (currentRoute in listOf("home", "search", "explore", "profile")) {
                            NavigationBar {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                                    label = { Text("Inicio") },
                                    selected = currentRoute == "home",
                                    onClick = {
                                        navController.navigate("home") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                                    label = { Text("Buscar") },
                                    selected = currentRoute == "search",
                                    onClick = {
                                        navController.navigate("search") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Explore, contentDescription = "Explorar") },
                                    label = { Text("Explorar") },
                                    selected = currentRoute == "explore",
                                    onClick = {
                                        navController.navigate("explore") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                                    label = { Text("Perfil") },
                                    selected = currentRoute == "profile",
                                    onClick = {
                                        navController.navigate("profile") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
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
                            slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(400)) + fadeIn(tween(400))
                        },
                        exitTransition = {
                            slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(400)) + fadeOut(tween(400))
                        },
                        popEnterTransition = {
                            slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(400)) + fadeIn(tween(400))
                        },
                        popExitTransition = {
                            slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(400)) + fadeOut(tween(400))
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

                        composable("explore") {
                            ExploreScreen(
                                navigateToDetail = { id -> navController.navigate("detail/$id") }
                            )
                        }

                        composable("profile") {
                            ProfileScreen(
                                navigateToAdmin = {
                                    navController.navigate("admin") { launchSingleTop = true }
                                },
                                onBack = { navController.popBackStack() },
                                onNavigateToDetail = { id ->
                                    navController.navigate("detail/$id") { launchSingleTop = true }
                                },
                                isDarkTheme = isDarkTheme,
                                onToggleDarkTheme = { isDarkTheme = !isDarkTheme }
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

                        // Corregido "arguments" por "espacioId" para evitar errores al parsear el id de la pantalla
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
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}