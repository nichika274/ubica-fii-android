package com.example.ubicafii.ui.detail

import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.ubicafii.R

import com.example.ubicafii.ui.theme.*
import com.example.ubicafii.ui.components.getTypeBgColor
import com.example.ubicafii.ui.components.getTypeColor
import com.example.ubicafii.ui.components.getTypeIcon
import com.example.ubicafii.util.getFloorLabel
import com.example.ubicafii.util.getFloorPlanResource
import java.io.File

@Composable
fun DetailScreen(
    espacioId: Int,
    viewModel: DetailViewModel = viewModel(),
    onBack: () -> Unit
) {
    val espacio by viewModel.espacio.collectAsState()
    val cargando by viewModel.cargando.collectAsState()
    val context = LocalContext.current
    var copiado by remember { mutableStateOf(false) }

    var showPlanoDialog by remember { mutableStateOf(false) }

    LaunchedEffect(espacioId) {
        viewModel.cargarEspacio(espacioId)
    }

    if (cargando) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BluePrimary)
        }
        return
    }

    val esp = espacio ?: return

    // dynamic matching usando tu función utilitaria con los DOS parámetros
    val planoResource = getFloorPlanResource(esp.bloque, esp.piso.toString())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Imagen Principal (Hero Image)
        Box(modifier = Modifier.fillMaxWidth().height(240.dp)) {
            val imageModel = if (esp.fotoUrl.startsWith("/")) {
                File(esp.fotoUrl)
            } else {
                esp.fotoUrl.ifEmpty { "https://via.placeholder.com/400?text=${esp.nombre}" }
            }

            AsyncImage(
                model = imageModel,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.38f),
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.22f)
                            )
                        )
                    )
            )

            // Botones Flotantes Superiores
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.92f), CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.DarkGray)
                }
                IconButton(
                    onClick = {
                        copiado = true
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Mira este espacio en UbicaFII: ${esp.nombre} - Bloque ${esp.bloque}\n\nAbri en la App: ubicafii://espacio?id=${esp.id}"
                            )
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Compartir"))
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.92f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (copiado) Icons.Default.CheckCircle else Icons.Default.Share,
                        contentDescription = "Compartir",
                        tint = if (copiado) Color(0xFF2E7D32) else Color.DarkGray
                    )
                }
            }

            // Badge del Tipo de Espacio
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .background(getTypeBgColor(esp.tipo), RoundedCornerShape(24.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(getTypeIcon(esp.tipo), contentDescription = null, modifier = Modifier.size(13.dp), tint = getTypeColor(esp.tipo))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(esp.tipo, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = getTypeColor(esp.tipo))
                }
            }
        }

        // Contenido Informativo
        Column(modifier = Modifier.padding(16.dp)) {

            // Tarjeta de Información General
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(esp.nombre, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Foreground)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = BluePrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bloque ${esp.bloque} · ${getFloorLabel(esp.piso.toString())}", fontSize = 13.sp, color = MutedForeground)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.background(BlueLight, RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                        Text(esp.id.toString(), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = BluePrimary, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(esp.descripcion, fontSize = 13.5.sp, color = Color(0xFF374151), lineHeight = 22.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cómo llegar (Tarjeta con Miniatura de Plano)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Directions, contentDescription = null, tint = BluePrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cómo llegar", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Foreground)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(esp.indicaciones, fontSize = 13.sp, color = MutedForeground, lineHeight = 20.sp)

                    Spacer(modifier = Modifier.height(16.dp))

                    // MINI MAPA ESTÁTICO
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFE8EDF2))
                    ) {
                        val density = LocalDensity.current
                        val containerWidthPx = with(density) { maxWidth.toPx() }
                        val containerHeightPx = with(density) { maxHeight.toPx() }

                        val imagePainter = painterResource(id = planoResource)
                        val imageOriginalWidth = imagePainter.intrinsicSize.width
                        val imageOriginalHeight = imagePainter.intrinsicSize.height

                        val imageAspect = imageOriginalWidth / imageOriginalHeight
                        val containerAspect = containerWidthPx / containerHeightPx

                        val drawnWidth = if (containerAspect > imageAspect) {
                            containerHeightPx * imageAspect
                        } else {
                            containerWidthPx
                        }

                        val drawnHeight = if (containerAspect > imageAspect) {
                            containerHeightPx
                        } else {
                            containerWidthPx / imageAspect
                        }

                        val fitOffsetX = (containerWidthPx - drawnWidth) / 2f
                        val fitOffsetY = (containerHeightPx - drawnHeight) / 2f

                        Image(
                            painter = imagePainter,
                            contentDescription = "Plano del piso",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )

                        val markerRadiusPx = with(density) { 12.dp.toPx() }
                        val markerX = fitOffsetX + (esp.coordenadaX * drawnWidth) - markerRadiusPx
                        val markerY = fitOffsetY + (esp.coordenadaY * drawnHeight) - markerRadiusPx

                        Box(
                            modifier = Modifier
                                .offset { IntOffset(markerX.toInt(), markerY.toInt()) }
                                .size(24.dp)
                                .background(BluePrimary, CircleShape)
                                .border(3.dp, Color.White, CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ubicación del espacio en el plano (${getFloorLabel(esp.piso.toString())})",
                        fontSize = 11.sp,
                        color = MutedForeground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "UbicaFII: ${esp.nombre} - Bloque ${esp.bloque}\n${getFloorLabel(esp.piso.toString())} · ${esp.indicaciones}\n\nUbicación exacta: ubicafii://espacio?id=${esp.id}"
                            )
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Compartir"))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Compartir", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { showPlanoDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BlueLight),
                    border = BorderStroke(1.5.dp, BluePrimary)
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, tint = BluePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ver en plano", color = BluePrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showPlanoDialog) {
        PlanoDialog(
            planoResource = planoResource,
            coordenadaX = esp.coordenadaX,
            coordenadaY = esp.coordenadaY,
            onDismiss = { showPlanoDialog = false }
        )
    }
}

@Composable
fun PlanoDialog(
    planoResource: Int,
    coordenadaX: Float,
    coordenadaY: Float,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
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
            val density = LocalDensity.current
            val dialogWidthPx = with(density) { maxWidth.toPx() }
            val dialogHeightPx = with(density) { maxHeight.toPx() }

            val imagePainter = painterResource(id = planoResource)
            val imageOriginalWidth = imagePainter.intrinsicSize.width
            val imageOriginalHeight = imagePainter.intrinsicSize.height

            val imageAspect = imageOriginalWidth / imageOriginalHeight
            val dialogAspect = dialogWidthPx / dialogHeightPx

            val drawnWidth = if (dialogAspect > imageAspect) {
                dialogHeightPx * imageAspect
            } else {
                dialogWidthPx
            }

            val drawnHeight = if (dialogAspect > imageAspect) {
                dialogHeightPx
            } else {
                dialogWidthPx / imageAspect
            }

            val fitOffsetX = (dialogWidthPx - drawnWidth) / 2f
            val fitOffsetY = (dialogHeightPx - drawnHeight) / 2f

            Image(
                painter = imagePainter,
                contentDescription = "Plano Interactivo",
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

            val markerRadiusPx = with(density) { 15.dp.toPx() }

            val markerX = (fitOffsetX + (coordenadaX * drawnWidth)) * scale + offsetX - markerRadiusPx
            val markerY = (fitOffsetY + (coordenadaY * drawnHeight)) * scale + offsetY - markerRadiusPx

            Box(
                modifier = Modifier
                    .offset { IntOffset(markerX.toInt(), markerY.toInt()) }
                    .size(30.dp)
                    .background(BluePrimary, CircleShape)
                    .border(4.dp, Color.White, CircleShape)
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
            }
        }
    }
}