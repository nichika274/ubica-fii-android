package com.example.ubicafii.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log // 🛠️ LOGS DE NAVEGACIÓN
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
import androidx.compose.foundation.layout.systemBarsPadding
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
import com.example.ubicafii.data.api.RetrofitClient
import com.example.ubicafii.data.repository.MapDataRepository
import com.example.ubicafii.util.PreferencesManager
import com.example.ubicafii.util.programarSincronizacion
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()
    private var deepLinkEspacioId: Int? = null

    // 🛠️ Estado mutable para controlar los intents entrantes en caliente (onNewIntent)
    private var currentIntentState by mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        programarSincronizacion(this)

        // 🛠️ Captura inicial en frío + Limpieza inmediata del Intent para evitar ejecuciones fantasmas en recreaciones
        intent?.data?.getQueryParameter("id")?.toIntOrNull()?.let {
            deepLinkEspacioId = it
            intent.data = null
        }
        currentIntentState = intent

        val savedDarkMode = PreferencesManager.isDarkMode(this)

        setContent {
            var isDarkTheme by rememberSaveable { mutableStateOf(savedDarkMode) }

            UbicaFIITheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()

                // PRE-CARGA OFFLINE-FIRST: Sincronización silenciosa al arrancar la app
                LaunchedEffect(Unit) {
                    launch(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                            Log.d("MainActivity", "Iniciando pre-carga silenciosa del mapa...")
                            val mapRepo = MapDataRepository(this@MainActivity)
                            val apiService = RetrofitClient.instance

                            val bloquesOk = mapRepo.fetchAndCacheBloques(apiService)
                            val puntosOk = mapRepo.fetchAndCachePuntosInteres(apiService)

                            Log.d("MainActivity", "Pre-carga completada. Bloques: $bloquesOk, Puntos: $puntosOk")
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Error en la sincronización inicial del mapa", e)
                        }
                    }
                }

                // 🛠️ Escucha activa en caliente + Limpieza profunda del Intent de la Activity
                LaunchedEffect(currentIntentState) {
                    currentIntentState?.data?.getQueryParameter("id")?.toIntOrNull()?.let { id ->
                        Log.d("MainActivity", "Deep link en caliente detectado: evaluando ID $id")

                        if (navController.currentDestination?.route != "detail/$id") {
                            Log.d("MainActivity", "Navegando a detalle desde Intent caliente")
                            navController.navigate("detail/$id") {
                                launchSingleTop = true
                            }
                        }

                        // Limpieza a nivel de estado de Compose y a nivel de Activity de Android
                        currentIntentState = null
                        intent?.data = null
                        setIntent(Intent())
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = "splash",
                    modifier = Modifier,
                    enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(400)) + fadeIn(tween(400)) },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(400)) + fadeOut(tween(400)) },
                    popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(400)) + fadeIn(tween(400)) },
                    popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(400)) + fadeOut(tween(400)) }
                ) {
                    // El Splash únicamente limpia la pila y redirige a Main
                    composable("splash") {
                        SplashScreen {
                            navController.navigate("main") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                    }

                    // El Main procesa el Deep Link en frío tras ser montado en el Backstack
                    composable("main") {
                        Log.d("NavHost", "Navegando a main")

                        LaunchedEffect(Unit) {
                            deepLinkEspacioId?.let { id ->
                                Log.d("NavHost", "Procesando deep link en frío diferido para ID: $id")
                                deepLinkEspacioId = null
                                navController.navigate("detail/$id") {
                                    launchSingleTop = true
                                    restoreState = false // 🛠️ Evitamos restaurar estados previos inconsistentes para este destino
                                }
                            }
                        }

                        MainScreenWithPager(
                            homeViewModel = homeViewModel,
                            isDarkTheme = isDarkTheme,
                            onToggleDarkTheme = {
                                isDarkTheme = !isDarkTheme
                                PreferencesManager.setDarkMode(this@MainActivity, isDarkTheme)
                            },
                            navigateToDetail = { id: Int ->
                                navController.navigate("detail/$id") { launchSingleTop = true }
                            },
                            navigateToBlock = { bloqueId ->
                                navController.navigate("floors/$bloqueId") { launchSingleTop = true }
                            },
                            navigateToAdmin = { navController.navigate("admin") }
                        )
                    }

                    composable(
                        "detail/{espacioId}",
                        arguments = listOf(navArgument("espacioId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val espacioId = backStackEntry.arguments?.getString("espacioId")?.toIntOrNull() ?: 0
                        Log.d("NavHost", "Navegando a detail con ID: $espacioId")

                        DetailScreen(
                            espacioId = espacioId,
                            onBack = {
                                Log.d("DetailScreen", "onBack ejecutado: popBackStack()")
                                if (navController.currentBackStackEntry?.lifecycle?.currentState?.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED) == true) {
                                    navController.popBackStack()
                                }
                            }
                        )
                    }

                    composable(
                        "floors/{bloqueId}",
                        arguments = listOf(navArgument("bloqueId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val bloqueId = backStackEntry.arguments?.getString("bloqueId") ?: "A"
                        Log.d("NavHost", "Navegando a floors con bloque: $bloqueId")

                        FloorsScreen(
                            bloqueId = bloqueId,
                            onBack = {
                                Log.d("FloorsScreen", "onBack ejecutado: popBackStack()")
                                if (navController.currentBackStackEntry?.lifecycle?.currentState?.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED) == true) {
                                    navController.popBackStack()
                                }
                            },
                            onSpaceClick = { id ->
                                navController.navigate("detail/$id") { launchSingleTop = true }
                            }
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
        // Actualizamos nuestro estado observable para que el LaunchedEffect de Compose se ejecute de inmediato
        currentIntentState = intent
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
    var currentPage by rememberSaveable { mutableStateOf(0) }

    val pagerState = rememberPagerState(
        initialPage = currentPage,
        pageCount = { 4 }
    )
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            currentPage = page
        }
    }

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    selected = pagerState.currentPage == 0,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    label = { Text("Buscar") },
                    selected = pagerState.currentPage == 1,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Explore, contentDescription = "Explorar") },
                    label = { Text("Explorar") },
                    selected = pagerState.currentPage == 2,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(2) } }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    selected = pagerState.currentPage == 3,
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
            Log.d("MainScreen", "Renderizando página: $page")

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