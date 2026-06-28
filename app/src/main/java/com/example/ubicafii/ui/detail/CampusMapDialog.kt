package com.example.ubicafii.ui.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var origenSeleccionado by remember { mutableStateOf("entrada") }

    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                bloques = RetrofitClient.instance.getBloques()
                puntosInteres = RetrofitClient.instance.getPuntosInteres()
            } catch (_: Exception) { }
        }
    }

    val bloqueActual = bloques.find { it.bloque == espacio.bloque }

    val bloqueANodo = mapOf(
        "A" to "pasilloA",
        "B" to "pasilloB",
        "C" to "pasilloC",
        "G" to "bloqueG"
    )

    val ruta = remember(origenSeleccionado, bloqueActual) {
        val destinoId = bloqueANodo[bloqueActual?.bloque]
        if (destinoId != null) encontrarRuta(origenSeleccionado, destinoId) else null
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            containerColor = Color.Black.copy(alpha = 0.92f),
            topBar = {
                TopAppBar(
                    title = { Text("Mapa del Campus", color = Color.White, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Cerrar", tint = Color.White)
                        }
                    },
                    actions = {
                        // Chips de origen
                        Row(
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .padding(end = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            GrafoNavegacion.nodos.forEach { nodo ->
                                val selected = origenSeleccionado == nodo.id
                                FilterChip(
                                    selected = selected,
                                    onClick = { origenSeleccionado = nodo.id },
                                    label = {
                                        Text(
                                            nodo.nombre,
                                            fontSize = 11.sp,
                                            color = if (selected) Color.White else Color.White.copy(alpha = 0.8f)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = if (selected) BluePrimary else Color.White.copy(alpha = 0.15f),
                                        selectedContainerColor = BluePrimary
                                    ),
                                    border = null
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                // Leyenda de colores
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LeyendaItem(color = Color(0xFF1565C0), texto = "Bloque")
                    LeyendaItem(color = Color.Green, texto = "Entrada")
                    LeyendaItem(color = Color(0xFFFFA000), texto = "Cafetería")
                    LeyendaItem(color = Color.Yellow, texto = "Ruta")
                }
            }
        ) { innerPadding ->
            // Contenido del mapa (imagen, rectángulo, puntos, ruta)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .pointerInput(Unit) {
                        detectTransformGestures { centroid, pan, zoom, _ ->
                            val oldScale = scale
                            val newScale = (scale * zoom).coerceIn(1f, 5f)
                            offsetX = centroid.x - (centroid.x - offsetX) * (newScale / oldScale)
                            offsetY = centroid.y - (centroid.y - offsetY) * (newScale / oldScale)
                            offsetX += pan.x
                            offsetY += pan.y
                            scale = newScale
                        }
                    }
            ) {
                val imagePainter = painterResource(id = R.drawable.mapa_campus)
                val imageWidth = imagePainter.intrinsicSize.width
                val imageHeight = imagePainter.intrinsicSize.height

                var containerWidthPx by remember { mutableStateOf(0f) }
                var containerHeightPx by remember { mutableStateOf(0f) }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { size ->
                            containerWidthPx = size.width.toFloat()
                            containerHeightPx = size.height.toFloat()
                        }
                ) {
                    Image(
                        painter = imagePainter,
                        contentDescription = "Mapa del campus",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offsetX,
                                translationY = offsetY,
                                transformOrigin = TransformOrigin(0f, 0f)
                            ),
                        contentScale = ContentScale.Fit
                    )

                    if (containerWidthPx > 0 && containerHeightPx > 0) {
                        val imageAspect = imageWidth / imageHeight
                        val containerAspect = containerWidthPx / containerHeightPx
                        val drawnWidth: Float
                        val drawnHeight: Float
                        val fitOffsetX: Float
                        val fitOffsetY: Float

                        if (containerAspect > imageAspect) {
                            drawnHeight = containerHeightPx
                            drawnWidth = containerHeightPx * imageAspect
                            fitOffsetX = (containerWidthPx - drawnWidth) / 2f
                            fitOffsetY = 0f
                        } else {
                            drawnWidth = containerWidthPx
                            drawnHeight = containerWidthPx / imageAspect
                            fitOffsetX = 0f
                            fitOffsetY = (containerHeightPx - drawnHeight) / 2f
                        }

                        // Rectángulo del bloque
                        if (bloqueActual != null) {
                            val left = (fitOffsetX + bloqueActual.mapaX * drawnWidth) * scale + offsetX
                            val top = (fitOffsetY + bloqueActual.mapaY * drawnHeight) * scale + offsetY
                            val width = bloqueActual.mapaWidth * drawnWidth * scale
                            val height = bloqueActual.mapaHeight * drawnHeight * scale

                            Box(
                                modifier = Modifier
                                    .offset { IntOffset(left.toInt(), top.toInt()) }
                                    .size(
                                        width = with(density) { width.toDp() },
                                        height = with(density) { height.toDp() }
                                    )
                                    .border(3.dp, Color(0xFF1565C0), RoundedCornerShape(4.dp))
                                    .background(Color(0xFF1565C0).copy(alpha = 0.25f))
                            )
                        }

                        // Canvas para puntos de interés y ruta
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Puntos de interés
                            for (punto in puntosInteres) {
                                val baseX = (fitOffsetX + punto.mapaX * drawnWidth) * scale + offsetX
                                val baseY = (fitOffsetY + punto.mapaY * drawnHeight) * scale + offsetY
                                val color = when (punto.tipo) {
                                    "entrada" -> Color.Green
                                    "cafeteria" -> Color(0xFFFFA000)
                                    "biblioteca" -> Color(0xFF7C3AED)
                                    else -> Color.Gray
                                }
                                drawCircle(color = color, radius = 8f, center = Offset(baseX, baseY))
                                drawCircle(color = Color.White, radius = 4f, center = Offset(baseX, baseY))
                            }

                            // Ruta
                            if (ruta != null && ruta.size >= 2) {
                                for (i in 0 until ruta.size - 1) {
                                    val p1 = Offset(
                                        (fitOffsetX + ruta[i].x * drawnWidth) * scale + offsetX,
                                        (fitOffsetY + ruta[i].y * drawnHeight) * scale + offsetY
                                    )
                                    val p2 = Offset(
                                        (fitOffsetX + ruta[i + 1].x * drawnWidth) * scale + offsetX,
                                        (fitOffsetY + ruta[i + 1].y * drawnHeight) * scale + offsetY
                                    )
                                    drawLine(
                                        color = Color.Yellow,
                                        start = p1,
                                        end = p2,
                                        strokeWidth = 3f,
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                    )
                                    drawCircle(color = Color.Yellow, radius = 4f, center = p1)
                                    drawCircle(color = Color.Yellow, radius = 4f, center = p2)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LeyendaItem(color: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(texto, color = Color.White, fontSize = 12.sp)
    }
}