package com.example.ubicafii.ui.detail

import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.ubicafii.ui.theme.*
import com.example.ubicafii.ui.components.getTypeBgColor
import com.example.ubicafii.ui.components.getTypeColor
import com.example.ubicafii.ui.components.getTypeIcon

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

    LaunchedEffect(espacioId) {
        viewModel.cargarEspacio(espacioId)
    }

    if (cargando) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val esp = espacio ?: return

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Hero image
        Box(modifier = Modifier.fillMaxWidth().height(240.dp)) {
            AsyncImage(
                model = esp.fotoUrl.ifEmpty { "https://via.placeholder.com/400?text=${esp.nombre}" },
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.38f), Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.22f))
                    )
                )
            )
            // Botones superiores
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.92f), RoundedCornerShape(20.dp))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.DarkGray)
                }
                IconButton(
                    onClick = {
                        copiado = true
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Mira este espacio: ${esp.nombre} - UbicaFII")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Compartir"))
                    },
                    modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.92f), RoundedCornerShape(20.dp))
                ) {
                    Icon(if (copiado) Icons.Default.CheckCircle else Icons.Default.Share, contentDescription = null, tint = if (copiado) Color(0xFF2E7D32) else Color.DarkGray)
                }
            }
            // Badge tipo
            Box(
                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
                    .background(getTypeBgColor(esp.tipo), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(getTypeIcon(esp.tipo), contentDescription = null, modifier = Modifier.size(13.dp), tint = getTypeColor(esp.tipo))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(esp.tipo, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = getTypeColor(esp.tipo))
                }
            }
        }

        // Contenido
        Column(modifier = Modifier.padding(16.dp)) {
            // Info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(esp.nombre, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Foreground)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = BluePrimary)
                        Text("Bloque ${esp.bloque} · Piso ${esp.piso}", fontSize = 13.sp, color = MutedForeground)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.background(BlueLight, RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                        Text(esp.id.toString(), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = BluePrimary, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(esp.descripcion, fontSize = 13.5.sp, color = Color(0xFF374151), lineHeight = 22.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cómo llegar
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
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

                    // Plano simplificado (canvas)
                    FloorPlanMini(floor = esp.piso.toString(), blockId = esp.bloque)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botones de acción
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "UbicaFII: ${esp.nombre} - ${esp.bloque} Piso ${esp.piso}\n${esp.indicaciones}")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Compartir"))
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Compartir", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { /* mostrar plano interactivo */ },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(16.dp),
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
}

@Composable
fun FloorPlanMini(floor: String, blockId: String) {
    val floorLabel = if (floor == "Sótano") "Sótano" else "Piso $floor"
    val posiciones = mapOf(
        "Sótano" to Offset(40f, 137f), "1" to Offset(40f, 137f), "2" to Offset(210f, 137f),
        "3" to Offset(150f, 137f), "4" to Offset(280f, 50f)
    )
    val (cx, cy) = posiciones[floor] ?: Offset(40f, 137f)

    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFE8EDF2))
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            // Corredor
            drawRect(color = Color(0xFFD4DCE8), topLeft = Offset(0f, 80f), size = androidx.compose.ui.geometry.Size(size.width, 20f))

            // Salones superiores
            listOf(10f, 80f, 150f, 220f, 280f).forEachIndexed { i, x ->
                drawRoundRect(color = Color.White, topLeft = Offset(x, 10f), size = androidx.compose.ui.geometry.Size(if (i == 4) 30f else 60f, 65f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f))
                drawRoundRect(color = Color(0xFFB0BEC5), topLeft = Offset(x, 10f), size = androidx.compose.ui.geometry.Size(if (i == 4) 30f else 60f, 65f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f), style = Stroke(width = 1.5f))
            }

            // Salones inferiores
            listOf(10f, 80f, 150f, 220f, 280f).forEachIndexed { i, x ->
                val resaltado = (cx == x && cy == 137f)
                drawRoundRect(color = if (resaltado) BlueLight else Color.White, topLeft = Offset(x, 105f), size = androidx.compose.ui.geometry.Size(if (i == 4) 30f else 60f, 65f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f))
                drawRoundRect(color = if (resaltado) BluePrimary else Color(0xFFB0BEC5), topLeft = Offset(x, 105f), size = androidx.compose.ui.geometry.Size(if (i == 4) 30f else 60f, 65f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f), style = Stroke(width = if (resaltado) 2f else 1.5f))
            }

            // Estrella / Marcador
            drawCircle(color = BluePrimary, radius = 11f, center = Offset(cx, cy))

            // Flecha entrada
            drawLine(color = Color(0xFF64748B), start = Offset(160f, 175f), end = Offset(160f, 168f), strokeWidth = 1.5f)
        }
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).background(Color.White).padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Ubicación del espacio en el plano ($floorLabel - $blockId)", fontSize = 11.sp, color = MutedForeground)
        }
    }
}