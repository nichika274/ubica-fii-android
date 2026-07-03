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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
import com.example.ubicafii.util.PreferencesManager
import com.example.ubicafii.util.programarSincronizacion
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()
    private var deepLinkEspacioId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔥 PASO NUEVO: Inicializar WorkManager en segundo plano de forma segura
        programarSincronizacion(this)

        intent?.data?.getQueryParameter("id")?.toIntOrNull()?.let {
            deepLinkEspacioId = it
        }

        // Leer la preferencia guardada antes de definir el estado del tema
        val savedDarkMode = PreferencesManager.isDarkMode(this)

        setContent {
            // RECUPERADO: Estado del tema persistente inicializado con el valor guardado
            var isDarkTheme by rememberSaveable { mutableStateOf(savedDarkMode) }

            UbicaFIITheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()

                // Escuchar deep links en caliente
                LaunchedEffect(intent) {
                    intent?.data?.getQueryParameter("id")?.toIntOrNull()?.let { id ->
                        navController.navigate("detail/$id") {
                            launchSingleTop = true
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = "splash",
                    modifier = Modifier,
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
                                navController.navigate("main") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        }
                    }

                    composable("main") {
                        MainScreenWithPager(
                            homeViewModel = homeViewModel,
                            isDarkTheme = isDarkTheme,
                            onToggleDarkTheme = {
                                isDarkTheme = !isDarkTheme
                                PreferencesManager.setDarkMode(this@MainActivity, isDarkTheme)
                            },
                            navigateToDetail = { id: Int -> navController.navigate("detail/${id.toString()}") },
                            navigateToBlock = { bloqueId -> navController.navigate("floors/$bloqueId") },
                            navigateToAdmin = { navController.navigate("admin") }
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

                    composable("admin") {
                        AdminScreen(onBack = { navController.popBackStack() })
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreenWithPager(
    homeViewModel: HomeViewModel,
    isDarkTheme: Boolean,
    onToggleDarkTheme: () -> Unit,
    navigateToDetail: (Int) -> Unit,
    navigateToBlock: (String) -> Unit,
    navigateToAdmin: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()

    val selectedTab by remember { derivedStateOf { pagerState.currentPage } }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    selected = selectedTab == 0,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    label = { Text("Buscar") },
                    selected = selectedTab == 1,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Explore, contentDescription = "Explorar") },
                    label = { Text("Explorar") },
                    selected = selectedTab == 2,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(2) } }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    selected = selectedTab == 3,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(3) } }
                )
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(innerPadding),
            beyondViewportPageCount = 1
        ) { page ->
            when (page) {
                0 -> HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToSearch = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                    onNavigateToBlocks = navigateToBlock,
                    onNavigateToAllBlocks = { },
                    onNavigateToDetail = navigateToDetail
                )
                1 -> SearchScreen(
                    onBack = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                    onSpaceClick = navigateToDetail
                )
                2 -> ExploreScreen(
                    navigateToDetail = navigateToDetail
                )
                3 -> ProfileScreen(
                    navigateToAdmin = navigateToAdmin,
                    onBack = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                    onNavigateToDetail = navigateToDetail,
                    isDarkTheme = isDarkTheme,
                    onToggleDarkTheme = onToggleDarkTheme
                )
            }
        }
    }
}