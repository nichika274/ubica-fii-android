package com.example.ubicafii.ui.detail

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ubicafii.data.model.BloqueMapa
import com.example.ubicafii.data.model.PuntoInteres
import com.example.ubicafii.ui.theme.BluePrimary
import com.example.ubicafii.util.Nodo
import kotlinx.coroutines.launch

@Composable
fun CampusMapView(
    imagePainter: Painter,
    imageWidth: Float,
    imageHeight: Float,
    bloques: List<BloqueMapa>,
    puntosInteres: List<PuntoInteres>,
    ruta: List<Nodo>?,
    bloqueActual: BloqueMapa?,
    espacioNombre: String = "",
    centrarTrigger: Int = 0,
    origenId: String = "entrada", // Recibimos el ID del origen para rastrear su pulso
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var containerWidthPx by remember { mutableStateOf(0f) }
    var containerHeightPx by remember { mutableStateOf(0f) }

    val scope = rememberCoroutineScope()

    val animatedScale = remember { Animatable(1f) }
    val animatedOffsetX = remember { Animatable(0f) }
    val animatedOffsetY = remember { Animatable(0f) }

    val pulseAnim = remember { Animatable(1f) }
    val routeProgress = remember { Animatable(0f) }

    // 📍 FASE 4.2: Animación de pulso infinito tipo Radar para el origen
    val infiniteTransition = rememberInfiniteTransition(label = "RadarOrigen")
    val radarScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "EscalaRadar"
    )
    val radarAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AlphaRadar"
    )

    val textMeasurer = rememberTextMeasurer()
    val labelTextStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BluePrimary)

    val imageAspect = imageWidth / imageHeight
    val containerAspect = if (containerWidthPx > 0 && containerHeightPx > 0) containerWidthPx / containerHeightPx else 1f

    val drawnWidth = if (containerAspect > imageAspect) containerHeightPx * imageAspect else containerWidthPx
    val drawnHeight = if (containerAspect > imageAspect) containerHeightPx else containerWidthPx / imageAspect

    val fitOffsetX = (containerWidthPx - drawnWidth) / 2f
    val fitOffsetY = (containerHeightPx - drawnHeight) / 2f

    // Forzar el redibujado progresivo de la ruta cuando cambie el trayecto original
    LaunchedEffect(ruta) {
        routeProgress.snapTo(0f)
        routeProgress.animateTo(1f, animationSpec = tween(1000, easing = FastOutSlowInEasing))
    }

    LaunchedEffect(centrarTrigger, containerWidthPx, containerHeightPx) {
        if (centrarTrigger > 0 && bloqueActual != null && containerWidthPx > 0 && containerHeightPx > 0) {
            val targetScale = 2.5f

            val blockCenterX = fitOffsetX + (bloqueActual.mapaX + bloqueActual.mapaWidth / 2f) * drawnWidth
            val blockCenterY = fitOffsetY + (bloqueActual.mapaY + bloqueActual.mapaHeight / 2f) * drawnHeight

            val targetOffsetX = (containerWidthPx / 2f - blockCenterX) * targetScale
            val targetOffsetY = (containerHeightPx / 2f - blockCenterY) * targetScale

            animatedScale.snapTo(scale)
            animatedOffsetX.snapTo(offsetX)
            animatedOffsetY.snapTo(offsetY)

            scope.launch { animatedScale.animateTo(targetScale, animationSpec = tween(600)) }
            scope.launch { animatedOffsetX.animateTo(targetOffsetX, animationSpec = tween(600)) }
            scope.launch { animatedOffsetY.animateTo(targetOffsetY, animationSpec = tween(600)) }

            scope.launch {
                pulseAnim.animateTo(1.12f, animationSpec = tween(300, easing = FastOutSlowInEasing))
                pulseAnim.animateTo(1.0f, animationSpec = tween(300, easing = FastOutLinearInEasing))
            }

            scale = targetScale
            offsetX = targetOffsetX
            offsetY = targetOffsetY
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scope.launch {
                        animatedScale.snapTo(scale)
                        animatedOffsetX.snapTo(offsetX)
                        animatedOffsetY.snapTo(offsetY)
                    }

                    val newScale = (scale * zoom).coerceIn(1f, 5f)
                    scale = newScale
                    offsetX += pan.x
                    offsetY += pan.y

                    scope.launch {
                        animatedScale.snapTo(scale)
                        animatedOffsetX.snapTo(offsetX)
                        animatedOffsetY.snapTo(offsetY)
                    }
                }
            }
            .onSizeChanged { size ->
                containerWidthPx = size.width.toFloat()
                containerHeightPx = size.height.toFloat()
            }
    ) {
        val currentScale = animatedScale.value
        val maxHorizontalDrag = maxOf(0f, (drawnWidth * currentScale - containerWidthPx) / 2f)
        val maxVerticalDrag = maxOf(0f, (drawnHeight * currentScale - containerHeightPx) / 2f)

        val boundedX = if (currentScale > 1f) animatedOffsetX.value.coerceIn(-maxHorizontalDrag, maxHorizontalDrag) else 0f
        val boundedY = if (currentScale > 1f) animatedOffsetY.value.coerceIn(-maxVerticalDrag, maxVerticalDrag) else 0f

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = currentScale,
                    scaleY = currentScale,
                    translationX = boundedX,
                    translationY = boundedY,
                    transformOrigin = TransformOrigin.Center
                )
        ) {
            Image(
                painter = imagePainter,
                contentDescription = "Mapa del campus",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            if (containerWidthPx > 0 && containerHeightPx > 0) {
                Canvas(modifier = Modifier.fillMaxSize()) {

                    // 1. BLOQUE SELECCIONADO Y ETIQUETA
                    if (bloqueActual != null) {
                        val left = fitOffsetX + bloqueActual.mapaX * drawnWidth
                        val top = fitOffsetY + bloqueActual.mapaY * drawnHeight
                        val w = bloqueActual.mapaWidth * drawnWidth
                        val h = bloqueActual.mapaHeight * drawnHeight

                        val centerX = left + w / 2f
                        val centerY = top + h / 2f
                        val pScale = pulseAnim.value

                        val pW = w * pScale
                        val pH = h * pScale
                        val pLeft = centerX - pW / 2f
                        val pTop = centerY - pH / 2f

                        drawRect(color = Color(0xFF1565C0).copy(alpha = 0.25f), topLeft = Offset(pLeft, pTop), size = Size(pW, pH))
                        drawRect(color = Color(0xFF1565C0), topLeft = Offset(pLeft, pTop), size = Size(pW, pH), style = Stroke(width = 3f))

                        if (espacioNombre.isNotEmpty()) {
                            val textLayoutResult = textMeasurer.measure(text = AnnotatedString(espacioNombre), style = labelTextStyle)
                            val padding = 8f
                            val labelX = centerX - (textLayoutResult.size.width + padding * 2) / 2
                            val labelY = pTop - (textLayoutResult.size.height + padding * 2) - 8f

                            drawRoundRect(color = Color.White, topLeft = Offset(labelX, labelY), size = Size(textLayoutResult.size.width + padding * 2, textLayoutResult.size.height + padding * 2), cornerRadius = CornerRadius(8f, 8f))
                            drawRoundRect(color = Color(0xFF1565C0), topLeft = Offset(labelX, labelY), size = Size(textLayoutResult.size.width + padding * 2, textLayoutResult.size.height + padding * 2), cornerRadius = CornerRadius(8f, 8f), style = Stroke(width = 1f))
                            drawText(textLayoutResult = textLayoutResult, topLeft = Offset(labelX + padding, labelY + padding))
                        }
                    }

                    // 2. PUNTOS DE INTERÉS Y MARCADOR PULSANTE DE ORIGEN
                    for (punto in puntosInteres) {
                        val x = fitOffsetX + punto.mapaX * drawnWidth
                        val y = fitOffsetY + punto.mapaY * drawnHeight
                        val isOrigen = punto.tipo == origenId

                        val color = when (punto.tipo) {
                            "entrada" -> Color.Green
                            "cafeteria" -> Color(0xFFFFA000)
                            "biblioteca" -> Color(0xFF7C3AED)
                            else -> Color.Gray
                        }

                        // Si este punto es el origen activo, dibujamos el efecto radar de fondo
                        if (isOrigen) {
                            drawCircle(
                                color = color.copy(alpha = radarAlpha),
                                radius = 10f * radarScale,
                                center = Offset(x, y)
                            )
                        }

                        drawCircle(color = color, radius = 8f, center = Offset(x, y))
                        drawCircle(color = Color.White, radius = 4f, center = Offset(x, y))
                    }

                    // 3. TRAZADO DE RUTA
                    if (ruta != null && ruta.size >= 2) {
                        val fullPath = Path().apply {
                            moveTo(fitOffsetX + ruta[0].x * drawnWidth, fitOffsetY + ruta[0].y * drawnHeight)
                            for (i in 1 until ruta.size) {
                                lineTo(fitOffsetX + ruta[i].x * drawnWidth, fitOffsetY + ruta[i].y * drawnHeight)
                            }
                        }

                        val pathMeasure = PathMeasure()
                        pathMeasure.setPath(fullPath, false)
                        val currentLength = pathMeasure.length * routeProgress.value

                        val animatedPath = Path()
                        pathMeasure.getSegment(0f, currentLength, animatedPath, true)

                        drawPath(
                            path = animatedPath,
                            color = Color.Yellow,
                            style = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                        )

                        var accumulatedLength = 0f
                        for (i in 0 until ruta.size) {
                            val nodeX = fitOffsetX + ruta[i].x * drawnWidth
                            val nodeY = fitOffsetY + ruta[i].y * drawnHeight
                            if (i > 0) {
                                accumulatedLength += kotlin.math.hypot(nodeX - (fitOffsetX + ruta[i-1].x * drawnWidth), nodeY - (fitOffsetY + ruta[i-1].y * drawnHeight))
                            }
                            if (currentLength >= accumulatedLength) {
                                drawCircle(color = Color.Yellow, radius = 4f, center = Offset(nodeX, nodeY))
                            }
                        }
                    }
                }
            }
        }

        // BRÚJULA NORTE ESTÁTICA
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(36.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), CircleShape)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(text = "N", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = BluePrimary, modifier = Modifier.offset(y = 2.dp))
                Icon(imageVector = Icons.Default.Navigation, contentDescription = "Norte", tint = BluePrimary, modifier = Modifier.size(14.dp).offset(y = (-2).dp))
            }
        }
    }
}