package com.example.ubicafii.ui.detail

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ubicafii.R
import com.example.ubicafii.data.api.RetrofitClient
import com.example.ubicafii.data.model.BloqueMapa
import com.example.ubicafii.data.model.Espacio
import com.example.ubicafii.data.model.PuntoInteres
import com.example.ubicafii.ui.theme.BluePrimary
import com.example.ubicafii.util.GrafoNavegacion
import com.example.ubicafii.util.encontrarRuta
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusMapDialog(
    espacio: Espacio,
    onDismiss: () -> Unit
) {
    var bloques by remember { mutableStateOf<List<BloqueMapa>>(emptyList()) }
    var puntosInteres by remember { mutableStateOf<List<PuntoInteres>>(emptyList()) }
    var origenSeleccionado by remember { mutableStateOf("entrada") }
    var mostrarSelectorOrigen by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var mostrarAyuda by remember { mutableStateOf(false) }
    var centrarTrigger by remember { mutableStateOf(0) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // ✨ Animación de escala para la entrada del Diálogo Principal
    val dialogScale = remember { Animatable(0.85f) }
    val dialogAlpha = remember { Animatable(0f) }

    val imagePainter = painterResource(id = R.drawable.mapa_campus)
    val imgWidth = imagePainter.intrinsicSize.width.toFloat()
    val imgHeight = imagePainter.intrinsicSize.height.toFloat()

    LaunchedEffect(Unit) {
        // Ejecutar animación de entrada de forma paralela a la carga
        scope.launch {
            dialogScale.animateTo(1f, animationSpec = tween(400))
        }
        scope.launch {
            dialogAlpha.animateTo(1f, animationSpec = tween(300))
        }

        scope.launch {
            try {
                bloques = RetrofitClient.instance.getBloques()
                puntosInteres = RetrofitClient.instance.getPuntosInteres()
            } catch (_: Exception) { }
            isLoading = false
            centrarTrigger = 1
        }
    }

    val bloqueActual = bloques.find { it.bloque == espacio.bloque }
    val bloqueANodo = mapOf(
        "A" to "pasilloA",
        "B" to "pasilloB",
        "C" to "pasilloC",
        "D" to "entrada_DFE",
        "E" to "entrada_DFE",
        "F" to "entrada_DFE",
        "G" to "bloqueG"
    )

    val ruta = remember(origenSeleccionado, bloqueActual) {
        val destinoId = bloqueANodo[bloqueActual?.bloque]

        if (destinoId != null) {
            val r = encontrarRuta(origenSeleccionado, destinoId)

            println("===================================")
            println("Origen: $origenSeleccionado")
            println("Bloque: ${bloqueActual?.bloque}")
            println("Destino: $destinoId")
            println("Ruta:")
            r?.forEach {
                println("${it.id} -> ${it.nombre}")
            }
            println("===================================")

            r
        } else null
    }

    val nombreOrigen = GrafoNavegacion.nodos.find { it.id == origenSeleccionado }?.nombre ?: "Entrada"

    // 🗣️ Lanzar confirmación en Snackbar al cambiar de origen
    LaunchedEffect(origenSeleccionado) {
        if (!isLoading) {
            scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(
                    message = "Ruta calculada desde: $nombreOrigen",
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    if (mostrarSelectorOrigen) {
        OriginSelectorDialog(
            origenSeleccionado = origenSeleccionado,
            onOrigenCambiado = { origenSeleccionado = it },
            onDismiss = { mostrarSelectorOrigen = false }
        )
    }

    if (mostrarAyuda) {
        AlertDialog(
            onDismissRequest = { mostrarAyuda = false },
            title = { Text("Leyenda del mapa") },
            text = {
                Column {
                    LeyendaItem(BluePrimary, "Bloque seleccionado")
                    LeyendaItem(Color.Yellow, "Ruta sugerida")
                    LeyendaItem(Color.Green, "Entrada principal")
                    LeyendaItem(Color(0xFFFFA000), "Cafetería")
                }
            },
            confirmButton = { TextButton(onClick = { mostrarAyuda = false }) { Text("Cerrar") } }
        )
    }

    Dialog(
        onDismissRequest = {
            // Animación de salida antes de cerrar
            scope.launch {
                dialogScale.animateTo(0.85f, animationSpec = tween(250))
            }
            scope.launch {
                dialogAlpha.animateTo(0f, animationSpec = tween(200))
                onDismiss()
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            // Aplicamos las propiedades animadas de escala y fundido al contenedor raíz
            modifier = Modifier.graphicsLayer(
                scaleX = dialogScale.value,
                scaleY = dialogScale.value,
                alpha = dialogAlpha.value
            ),
            containerColor = MaterialTheme.colorScheme.surface,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("🗺 Mapa del Campus", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Cerrar", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                if (isLoading) {
                    Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = BluePrimary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Ubicando ${espacio.nombre}...", style = MaterialTheme.typography.titleMedium, color = BluePrimary, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1f)) {
                            CampusMapView(
                                imagePainter = imagePainter,
                                imageWidth = imgWidth,
                                imageHeight = imgHeight,
                                bloques = bloques,
                                puntosInteres = puntosInteres,
                                ruta = ruta,
                                bloqueActual = bloqueActual,
                                espacioNombre = espacio.nombre,
                                centrarTrigger = centrarTrigger,
                                origenId = origenSeleccionado, // Pasamos el ID para animar el origen
                                modifier = Modifier.fillMaxSize()
                            )

                            SmallFloatingActionButton(
                                onClick = { centrarTrigger++ },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp),
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = BluePrimary
                            ) {
                                Icon(Icons.Default.MyLocation, contentDescription = "Centrar en el bloque")
                            }
                        }

                        // 🏁 Animación de entrada de la tarjeta inferior (Slide + Fade)
                        AnimatedVisibility(
                            visible = !isLoading,
                            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
                        ) {
                            CampusBottomCard(
                                espacio = espacio,
                                origenActual = nombreOrigen,
                                onClickCambiarOrigen = { mostrarSelectorOrigen = true },
                                onClickAyuda = { mostrarAyuda = true },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun LeyendaItem(color: Color, texto: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(color, androidx.compose.foundation.shape.RoundedCornerShape(7.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(texto, style = MaterialTheme.typography.bodyMedium)
    }
}