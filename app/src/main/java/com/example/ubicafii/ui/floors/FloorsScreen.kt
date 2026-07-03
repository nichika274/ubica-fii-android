package com.example.ubicafii.ui.floors

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.ubicafii.data.model.Espacio
import com.example.ubicafii.ui.theme.*
import com.example.ubicafii.ui.components.getTypeBgColor
import com.example.ubicafii.ui.components.getTypeColor
import com.example.ubicafii.ui.components.getTypeIcon
import androidx.compose.foundation.isSystemInDarkTheme

data class BloqueData(
    val id: String,
    val nombre: String,
    val imagen: String,
    val pisos: List<String>,
    val descripcion: String
)

val bloquesMap = mapOf(
    "A" to BloqueData(
        "A",
        "Bloque A",
        "https://cdn.phototourl.com/member/2026-06-17-7475f8ec-7748-4107-94dd-236552ce29c9.png",
        listOf("0", "1", "2"),
        "Bloque principal con aulas de pregrado y laboratorios de cómputo."
    ),
    "B" to BloqueData(
        "B",
        "Bloque B",
        "https://cdn.phototourl.com/member/2026-06-17-b6e99229-89b5-4fc9-8c18-25e028562473.png",
        listOf("0", "1", "2"),
        "Bloque administrativo con oficinas de docentes y decanato."
    ),
    "C" to BloqueData(
        "C",
        "Bloque C",
        "https://cdn.phototourl.com/member/2026-06-17-552c0410-63d0-465a-8eab-0089c0cd6e65.png",
        listOf("0", "1", "2"),
        "Bloque de laboratorios especializados y aulas."
    ),
    "D" to BloqueData(
        "D",
        "Bloque D",
        "https://cdn.phototourl.com/member/2026-07-02-53d0580a-fed3-4904-bc0a-87b25208e09f.jpg",
        listOf("0", "1"),
        "Bloque de laboratorios especializados y talleres."
    ),
    "E" to BloqueData(
        "E",
        "Bloque E",
        "https://cdn.phototourl.com/member/2026-07-02-87b04a35-f2c7-4ce3-a2f3-8e197bd55039.jpg",
        listOf("0", "1"),
        "Bloque E de Taller de mecánica."
    ),
    "F" to BloqueData(
        "F",
        "Bloque F",
        "https://images.unsplash.com/photo-1774131231781-62ac008585bf?w=400&h=250&fit=crop&auto=format",
        listOf("0", "1"),
        "Bloque F de Taller Industrial"
    ),
    "G" to BloqueData(
        "G",
        "Bloque G",
        "https://cdn.phototourl.com/member/2026-07-02-02f8fea9-34c2-45b9-9151-7b8d21e18790.jpg",
        listOf("0", "1"),
        "Bloque de aulas especializadas."
    )
)
val filterTypes = listOf("Aula", "Laboratorio", "Oficina", "Baño")

@Composable
fun FloorsScreen(
    bloqueId: String,
    viewModel: FloorsViewModel = viewModel(),
    onBack: () -> Unit,
    onSpaceClick: (String) -> Unit
) {
    val espacios by viewModel.espacios.collectAsState()
    val pisos by viewModel.pisos.collectAsState()
    val cargando by viewModel.cargando.collectAsState()

    val bloque = bloquesMap[bloqueId] ?: bloquesMap["A"]!!
    var pisoSeleccionado by remember(bloqueId) {
        mutableStateOf(bloque.pisos.firstOrNull() ?: "1")
    }
    var filtroTipo by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(bloqueId) {
        viewModel.cargarBloque(bloqueId)
    }

    val espaciosFiltrados = remember(espacios, pisoSeleccionado, filtroTipo) {
        espacios.filter {
            it.piso.toString() == pisoSeleccionado &&
                    (filtroTipo == null || it.tipo.equals(filtroTipo, ignoreCase = true))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        // --- HEADER CON GRADIENTE ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Blue900, Blue700)
                    )
                )
                .padding(start = 16.dp, end = 16.dp, top = 48.dp, bottom = 16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                    Text(
                        "Inicio",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    bloque.nombre,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    bloque.descripcion,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Chips de pisos dinámicos adaptados
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val listaPisos = pisos.ifEmpty { bloque.pisos }
                    listaPisos.forEach { pisoStr ->
                        val isSelected = pisoStr == pisoSeleccionado
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                pisoSeleccionado = pisoStr
                                filtroTipo = null
                            },
                            label = {
                                Text(
                                    when (pisoStr) {
                                        "0" -> "Planta Baja"
                                        "Sótano" -> "Sótano"
                                        else -> "Piso $pisoStr"
                                    }
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
                                labelColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- FILTROS POR TIPO ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filtroTipo == null,
                onClick = { filtroTipo = null },
                label = { Text("Todos", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = if (filtroTipo == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = if (filtroTipo == null) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            filterTypes.forEach { tipo ->
                val active = filtroTipo == tipo
                FilterChip(
                    selected = active,
                    onClick = { filtroTipo = if (active) null else tipo },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                getTypeIcon(tipo),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (active) getTypeColor(tipo) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(tipo, fontSize = 12.sp)
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = if (active) getTypeBgColor(tipo) else MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = if (active) getTypeColor(tipo) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        // --- LISTA DE ESPACIOS ---
        if (cargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (espaciosFiltrados.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Room,
                        contentDescription = null,
                        modifier = Modifier.size(52.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        "No hay espacios en este piso",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(espaciosFiltrados) { espacio ->
                    TarjetaEspacioPiso(espacio, onClick = { onSpaceClick(espacio.id.toString()) })
                }
            }
        }
    }
}
@Composable
fun TarjetaEspacioPiso(espacio: Espacio, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
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
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    espacio.id.toString(),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .background(getTypeBgColor(espacio.tipo), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        espacio.tipo,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = getTypeColor(espacio.tipo)
                    )
                }
            }
            AsyncImage(
                model = espacio.fotoUrl.ifEmpty { "https://via.placeholder.com/56" },
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}