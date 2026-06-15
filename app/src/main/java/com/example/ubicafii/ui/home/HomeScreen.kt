package com.example.ubicafii.ui.home

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ubicafii.data.model.Espacio
import com.example.ubicafii.ui.theme.*
import com.example.ubicafii.ui.components.getTypeBgColor
import com.example.ubicafii.ui.components.getTypeColor
import com.example.ubicafii.ui.components.getTypeIcon
import com.example.ubicafii.ui.theme.home.HomeViewModel

// Datos de bloques simulados
data class Bloque(
    val id: String,
    val nombre: String,
    val imagen: String,
    val pisos: List<String>
)

val bloques = listOf(
    Bloque("A", "Bloque A", "https://images.unsplash.com/photo-1770146605141-cd08750b3b4c?w=400&h=250&fit=crop&auto=format", listOf("Sótano", "1", "2", "3")),
    Bloque("B", "Bloque B", "https://images.unsplash.com/photo-1762972922113-878e5223711f?w=400&h=250&fit=crop&auto=format", listOf("1", "2", "3")),
    Bloque("C", "Bloque C", "https://images.unsplash.com/photo-1777378543333-b4fb4f96fdd3?w=400&h=250&fit=crop&auto=format", listOf("1", "2")),
    Bloque("D", "Bloque D", "https://images.unsplash.com/photo-1774131231781-62ac008585bf?w=400&h=250&fit=crop&auto=format", listOf("Sótano", "1", "2", "3", "4"))
)

val frequentIds = listOf("D-1-CAF", "D-2-BIB", "D-1-ENT")

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToBlocks: (String) -> Unit,
    onNavigateToAllBlocks: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val context = LocalContext.current
    val espacios by viewModel.espacios.collectAsState()
    val error by viewModel.error.collectAsState()
    val cargando by viewModel.cargando.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarEspacios()
    }

    LaunchedEffect(espacios) {
        if (espacios.isNotEmpty()) {
            Toast.makeText(context, "Cargados ${espacios.size} espacios", Toast.LENGTH_SHORT).show()
        }
    }

    val frecuentes = remember(espacios) {
        frequentIds.mapNotNull { id -> espacios.find { it.nombre == id || it.id.toString() == id } }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header azul con gradiente
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Blue900, Blue700)
                        )
                    )
                    .padding(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Bienvenido a",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                            Text(
                                "Ubica-FII",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(
                            onClick = { /* notificaciones */ },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Barra de búsqueda circular usando OutlinedTextField simulado
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onNavigateToSearch() }
                    ) {
                        OutlinedTextField(
                            value = "",
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            placeholder = {
                                Text(
                                    text = "Buscar aula, laboratorio, oficina…",
                                    color = Color(0xFFBDBDBD),
                                    fontSize = 14.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Buscar",
                                    tint = Color(0xFF9E9E9E),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledContainerColor = Color.White,
                                disabledBorderColor = Color.Transparent,
                                disabledTextColor = Color.White,
                                disabledPlaceholderColor = Color(0xFFBDBDBD),
                                disabledLeadingIconColor = Color(0xFF9E9E9E)
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                    }
                }
            }

            // Contenido Principal
            Column(modifier = Modifier.padding(20.dp)) {
                // Sección Bloques horizontales
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Bloques", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Foreground)
                    Text("${bloques.size} bloques", fontSize = 12.sp, color = BluePrimary, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(bloques.size) { index ->
                        val bloque = bloques[index]
                        Box(
                            modifier = Modifier
                                .width(158.dp)
                                .height(115.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .clickable { onNavigateToBlocks(bloque.id) }
                        ) {
                            AsyncImage(
                                model = bloque.imagen,
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
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.1f),
                                                Color.Black.copy(alpha = 0.72f)
                                            )
                                        )
                                    )
                                    .padding(12.dp),
                                contentAlignment = Alignment.BottomStart
                            ) {
                                Column {
                                    Text(
                                        bloque.nombre,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "${bloque.pisos.size} piso${if (bloque.pisos.size != 1) "s" else ""}",
                                        color = Color.White.copy(alpha = 0.72f),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Espacios frecuentes
                Text(
                    "Espacios frecuentes",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Foreground
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (frecuentes.isEmpty()) {
                    Text(
                        "No hay espacios frecuentes disponibles.",
                        fontSize = 13.sp,
                        color = MutedForeground,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    frecuentes.forEach { espacio ->
                        TarjetaEspacioFrecuente(
                            espacio = espacio,
                            onClick = { onNavigateToDetail(espacio.id.toString()) }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Todos los bloques (Sección inferior)
                Text(
                    "Todos los bloques",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Foreground
                )
                Spacer(modifier = Modifier.height(12.dp))

                bloques.chunked(2).forEach { parDeBloques ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        parDeBloques.forEach { bloque ->
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigateToBlocks(bloque.id) },
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(BlueLight, RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            bloque.id,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = BluePrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            bloque.nombre,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Foreground
                                        )
                                        Text(
                                            "${bloque.pisos.size} pisos",
                                            fontSize = 11.sp,
                                            color = MutedForeground
                                        )
                                    }
                                }
                            }
                        }
                        if (parDeBloques.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        // INDICADOR DE CARGA
        if (cargando) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                color = BluePrimary,
                trackColor = Color.Transparent
            )
        }

        // --- BANNER DE ERROR EN UN SNACKBAR INFERIOR FLOATING RECONFIGURADO ---
        error?.let { mensajeError ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 76.dp), // Ajustado el margen inferior para no tapar barras de navegación si usas scaffold
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                action = {
                    TextButton(
                        onClick = { viewModel.cargarEspacios() }
                    ) {
                        Text(
                            "Reintentar",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = mensajeError,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun TarjetaEspacioFrecuente(espacio: Espacio, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(getTypeBgColor(espacio.tipo), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getTypeIcon(espacio.tipo),
                    contentDescription = null,
                    tint = getTypeColor(espacio.tipo),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    espacio.nombre,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Foreground
                )
                Text(
                    "Bloque ${espacio.bloque} · Piso ${espacio.piso}",
                    fontSize = 12.sp,
                    color = MutedForeground
                )
            }
            AsyncImage(
                model = espacio.fotoUrl.ifEmpty { "https://via.placeholder.com/48" },
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFBDBDBD))
        }
    }
}