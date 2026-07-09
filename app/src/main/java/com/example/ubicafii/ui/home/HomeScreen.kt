package com.example.ubicafii.ui.home

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.ubicafii.R

// Datos de bloques simulados
data class Bloque(
    val id: String,
    val nombre: String,
    val imagen: String,
    val pisos: List<String>
)

val bloques = listOf(
    Bloque("A", "Bloque A", "https://cdn.phototourl.com/member/2026-06-17-7475f8ec-7748-4107-94dd-236552ce29c9.png", listOf("Planta baja", "Piso 1", "Piso 2")),
    Bloque("B", "Bloque B", "https://cdn.phototourl.com/member/2026-06-17-b6e99229-89b5-4fc9-8c18-25e028562473.png", listOf("Planta baja", "Piso 1", "Piso 2")),
    Bloque("C", "Bloque C", "https://cdn.phototourl.com/member/2026-06-17-552c0410-63d0-465a-8eab-0089c0cd6e65.png", listOf("Planta baja", "Piso 1", "Piso 2")),
    Bloque("D", "Bloque D", "https://cdn.phototourl.com/member/2026-07-02-53d0580a-fed3-4904-bc0a-87b25208e09f.jpg", listOf("Planta baja", "Piso 1")),
    Bloque("E", "Bloque E", "https://cdn.phototourl.com/member/2026-07-02-87b04a35-f2c7-4ce3-a2f3-8e197bd55039.jpg", listOf("Planta baja")),
    Bloque("F", "Bloque F", "https://cdn.phototourl.com/member/2026-07-02-02f8fea9-34c2-45b9-9151-7b8d21e18790.jpg", listOf("Planta baja")),
    Bloque("G", "Bloque G", "https://cdn.phototourl.com/member/2026-06-17-898757d9-9bea-4f1c-8ae8-ae81c2a0ab76.jpg", listOf("Planta baja", "Piso 1"))
)

val frequentNames = listOf(
    "Secretaría",
    "Fueiist",
    "Decanato"
)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToBlocks: (String) -> Unit,
    onNavigateToAllBlocks: () -> Unit,
    onNavigateToDetail: (Int) -> Unit
) {
    val context = LocalContext.current
    val espacios by viewModel.espacios.collectAsState()
    val error by viewModel.error.collectAsState()
    val cargando by viewModel.cargando.collectAsState()

    Log.d("HomeScreen", "Recomposición de HomeScreen, espacios.size: ${espacios.size}, cargando: $cargando")

    LaunchedEffect(Unit) {
        if (viewModel.espacios.value.isEmpty()) {
            viewModel.cargarEspacios()
        }
    }

    LaunchedEffect(espacios) {
        if (espacios.isNotEmpty()) {
            Toast.makeText(context, "Cargados ${espacios.size} espacios", Toast.LENGTH_SHORT).show()
        }
    }

    val frecuentes = remember(espacios) {
        frequentNames.mapNotNull { nombre ->
            espacios.find { it.nombre.equals(nombre, ignoreCase = true) }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header con gradiente adaptativo del tema
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Blue900, Blue700)
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp) // Un pequeño respiro abajo antes de la barra de búsqueda
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(0.dp))

                        Column(
                            modifier = Modifier.offset(
                                x = (-90).dp,
                                y = 10.dp
                            )
                        ) {
                            Text(
                                "Bienvenido a",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 18.sp
                            )

                            Text(
                                "Ubica-FII",
                                color = Color.White,
                                fontSize = 23.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(R.drawable.logo_ubicafii),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(100.dp)
                                    .offset(
                                        x = (-25).dp,
                                        y = 10.dp
                                    )
                                    .graphicsLayer {
                                        scaleX = 1.4f
                                        scaleY = 1.4f
                                    }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Barra de búsqueda simulada fija
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onNavigateToSearch() },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Buscar aula, laboratorio, oficina…",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
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
                    Text(
                        "Bloques",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "${bloques.size} bloques",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
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
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (frecuentes.isEmpty()) {
                    Text(
                        "No hay espacios frecuentes disponibles.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    frecuentes.forEach { espacio ->
                        TarjetaEspacioFrecuente(
                            espacio = espacio,
                            onClick = { onNavigateToDetail(espacio.id) }
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
                    color = MaterialTheme.colorScheme.onBackground
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
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                MaterialTheme.colorScheme.primaryContainer,
                                                RoundedCornerShape(12.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            bloque.id,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            bloque.nombre,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            "${bloque.pisos.size} pisos",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.Transparent
            )
        }

        // --- BANNER DE ERROR ---
        error?.let { mensajeError ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 76.dp),
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
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
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Bloque ${espacio.bloque} · Piso ${espacio.piso}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}