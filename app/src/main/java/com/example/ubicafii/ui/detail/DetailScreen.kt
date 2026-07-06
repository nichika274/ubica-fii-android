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
import com.example.ubicafii.data.model.Espacio
import com.example.ubicafii.ui.components.getTypeBgColor
import com.example.ubicafii.ui.components.getTypeColor
import com.example.ubicafii.ui.components.getTypeIcon
import com.example.ubicafii.ui.theme.*
import com.example.ubicafii.util.ImageCache
import com.example.ubicafii.util.PreferencesManager
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
    var showCampusMapDialog by remember { mutableStateOf(false) }

    LaunchedEffect(espacioId) {
        viewModel.cargarEspacio(espacioId)
        PreferencesManager.addRecent(context, espacioId)
    }

    if (cargando) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val esp = espacio ?: return
    val planoResource = getFloorPlanResource(esp.bloque, esp.piso.toString())
    var isFav by remember { mutableStateOf(PreferencesManager.isFavorite(context, esp.id)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Imagen Principal (Hero Image)
        Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {

            // ─── CACHÉ DE IMÁGENES OFFLINE ───
            val cachedFile = remember(esp.fotoUrl) {
                if (esp.fotoUrl.isNotBlank()) {
                    ImageCache.getCachedFile(context, esp.fotoUrl.hashCode().toString())
                } else null
            }
            var localImageModel by remember { mutableStateOf<Any?>(cachedFile ?: esp.fotoUrl) }

            // Si no está en caché, descargarla en segundo plano
            LaunchedEffect(esp.fotoUrl) {
                if (esp.fotoUrl.isNotBlank() && cachedFile == null) {
                    val file = ImageCache.cacheImage(
                        context = context,
                        imageUrl = esp.fotoUrl,
                        imageId = esp.fotoUrl.hashCode().toString()
                    )
                    if (file != null) {
                        localImageModel = file
                    }
                }
            }

            // Modelo final que se pasa a AsyncImage
            val imageModel = when {
                localImageModel is File -> localImageModel
                esp.fotoUrl.startsWith("/") || esp.fotoUrl.contains("filesDir") -> File(esp.fotoUrl)
                else -> esp.fotoUrl.ifEmpty { "https://via.placeholder.com/400?text=${esp.nombre}" }
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
                                Color.Black.copy(alpha = 0.4f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.3f)
                            )
                        )
                    )
            )

            // Botones Flotantes Superiores
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.92f), CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.DarkGray)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = {
                            if (isFav) {
                                PreferencesManager.removeFavorite(context, esp.id)
                                isFav = false
                            } else {
                                PreferencesManager.addFavorite(context, esp.id)
                                isFav = true
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.92f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (isFav) Color.Red else Color.DarkGray
                        )
                    }
                    IconButton(
                        onClick = {
                            copiado = true
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Mira este espacio en UbicaFII: ${esp.nombre} - Bloque ${esp.bloque}\n\nAbrir en la App: ubicafii://espacio?id=${esp.id}"
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
                    Icon(getTypeIcon(esp.tipo), contentDescription = null, modifier = Modifier.size(14.dp), tint = getTypeColor(esp.tipo))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(esp.tipo, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = getTypeColor(esp.tipo))
                }
            }
        }

        // Contenido Informativo
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // Tarjeta de Información General
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(esp.nombre, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bloque ${esp.bloque} · ${getFloorLabel(esp.piso.toString())}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                        Text(esp.id.toString(), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(esp.descripcion, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 22.sp)
                }
            }

            // Cómo llegar (Tarjeta con Miniatura de Plano y Botón Integrado)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Directions, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cómo llegar", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(esp.indicaciones, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 21.sp)

                    Spacer(modifier = Modifier.height(16.dp))

                    // MINI MAPA ESTÁTICO
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        val density = LocalDensity.current
                        val containerWidthPx = with(density) { maxWidth.toPx() }
                        val containerHeightPx = with(density) { maxHeight.toPx() }

                        val imagePainter = painterResource(id = planoResource)
                        val imageAspect = imagePainter.intrinsicSize.width / imagePainter.intrinsicSize.height
                        val containerAspect = containerWidthPx / containerHeightPx

                        val drawnWidth = if (containerAspect > imageAspect) containerHeightPx * imageAspect else containerWidthPx
                        val drawnHeight = if (containerAspect > imageAspect) containerHeightPx else containerWidthPx / imageAspect

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
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                .border(3.dp, Color.White, CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Ubicación en plano (${getFloorLabel(esp.piso.toString())})",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // BOTÓN "VER PLANO" DENTRO DE LA TARJETA
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showPlanoDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.ZoomIn, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ver plano en grande", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Botones inferiores de Acción
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
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
                        .height(52.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Compartir", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { showCampusMapDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Public, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ver en mapa", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
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

    if (showCampusMapDialog) {
        CampusMapDialog(
            espacio = esp,
            onDismiss = { showCampusMapDialog = false }
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
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.98f))
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
            val imageAspect = imagePainter.intrinsicSize.width / imagePainter.intrinsicSize.height
            val dialogAspect = dialogWidthPx / dialogHeightPx

            val drawnWidth = if (dialogAspect > imageAspect) dialogHeightPx * imageAspect else dialogWidthPx
            val drawnHeight = if (dialogAspect > imageAspect) dialogHeightPx else dialogWidthPx / imageAspect

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
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
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