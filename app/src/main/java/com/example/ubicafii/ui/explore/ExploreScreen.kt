package com.example.ubicafii.ui.explore

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ubicafii.data.model.Espacio
import com.example.ubicafii.ui.theme.*
import com.example.ubicafii.ui.components.getTypeBgColor
import com.example.ubicafii.ui.components.getTypeColor
import com.example.ubicafii.ui.components.getTypeIcon

@Composable
fun ExploreScreen(
    navigateToDetail: (Int) -> Unit,
    viewModel: ExploreViewModel = viewModel()
) {
    val categorias = listOf(
        "Aula" to Icons.Default.School,
        "Laboratorio" to Icons.Default.Science,
        "Oficina" to Icons.Default.BusinessCenter,
        "Baño" to Icons.Default.Wc,
        "Biblioteca" to Icons.Default.LibraryBooks,
        "Cafetería" to Icons.Default.Coffee
    )

    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val espaciosFiltrados by viewModel.filteredSpaces.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header azul (igual que en HomeScreen y FloorsScreen)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = Brush.verticalGradient(listOf(Blue900, Blue700)))
                .padding(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 24.dp)
        ) {
            Column {
                Text(
                    "Explorar",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Descubre espacios por categoría",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cuadrícula de categorías (filas manuales para evitar scroll anidado)
            item {
                Text(
                    "Categorías",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))
                val rows = categorias.chunked(3)
                for (rowItems in rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for ((tipo, icon) in rowItems) {
                            val isSelected = selectedCategory == tipo
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.selectCategory(tipo) },
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) getTypeBgColor(tipo) else MaterialTheme.colorScheme.surface
                                ),
                                border = if (isSelected) BorderStroke(1.5.dp, getTypeColor(tipo)) else null
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp, horizontal = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = tipo,
                                        tint = if (isSelected) getTypeColor(tipo) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = tipo,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) getTypeColor(tipo) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        if (rowItems.size < 3) {
                            repeat(3 - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Resultados
            if (selectedCategory != null) {
                item {
                    Text(
                        text = "Resultados para \"$selectedCategory\"",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                if (espaciosFiltrados.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No se encontraron espacios de este tipo.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    items(espaciosFiltrados) { espacio ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navigateToDetail(espacio.id) },
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
                                        .size(48.dp)
                                        .background(getTypeBgColor(espacio.tipo), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        getTypeIcon(espacio.tipo),
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
                                        "Bloque ${espacio.bloque} · Piso ${espacio.piso}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}