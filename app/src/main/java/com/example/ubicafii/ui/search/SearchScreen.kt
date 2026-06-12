package com.example.ubicafii.ui.search

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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ubicafii.data.model.Espacio
import com.example.ubicafii.ui.theme.*
import com.example.ubicafii.ui.components.getTypeBgColor
import com.example.ubicafii.ui.components.getTypeColor
import com.example.ubicafii.ui.components.getTypeIcon

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = viewModel(),
    onBack: () -> Unit,
    onSpaceClick: (String) -> Unit
) {
    val query by viewModel.query.collectAsState()
    val resultados by viewModel.resultados.collectAsState()
    val cargando by viewModel.cargando.collectAsState()
    val focusRequester = remember { FocusRequester() }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = Foreground
                )
            }
            Spacer(modifier = Modifier.width(4.dp))

            // --- CORREGIDO: Barra de búsqueda totalmente circular ---
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::actualizarQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp) // Altura estandarizada con el Home
                    .focusRequester(focusRequester),
                placeholder = { Text("Buscar por nombre o código…", color = Color(0xFFBDBDBD), fontSize = 14.sp) },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = Color(0xFF9E9E9E),
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.actualizarQuery("") }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Limpiar",
                                tint = Color(0xFF9E9E9E),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Foreground,
                    unfocusedTextColor = Foreground,
                    focusedBorderColor = Color.Transparent, // Oculta bordes duros de enfoque
                    unfocusedBorderColor = Color.Transparent // Oculta bordes duros por defecto
                ),
                shape = RoundedCornerShape(24.dp) // Curvatura tipo píldora idéntica al Home
            )
        }

        if (cargando) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        if (query.isBlank()) {
            Text(
                "Sugerencias",
                modifier = Modifier.padding(16.dp),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MutedForeground
            )
        } else if (resultados.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.SearchOff,
                        contentDescription = "Sin resultados",
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFFCBD5E1)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Sin resultados", fontWeight = FontWeight.SemiBold)
                    Text("No se encontró \"$query\"", color = MutedForeground)
                }
            }
        } else {
            Text(
                "${resultados.size} resultado(s)",
                modifier = Modifier.padding(16.dp),
                fontSize = 13.sp,
                color = MutedForeground
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(resultados) { espacio ->
                    TarjetaResultadoBusqueda(espacio, query, onClick = { onSpaceClick(espacio.id.toString()) })
                }
            }
        }
    }
}

@Composable
fun TarjetaResultadoBusqueda(espacio: Espacio, query: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp), // Cambiado a 24.dp para consistencia visual
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(getTypeBgColor(espacio.tipo), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    getTypeIcon(espacio.tipo),
                    contentDescription = espacio.tipo,
                    tint = getTypeColor(espacio.tipo),
                    modifier = Modifier.size(21.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = buildAnnotatedString {
                        val nombre = espacio.nombre
                        val idx = nombre.lowercase().indexOf(query.lowercase())
                        if (idx >= 0) {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Foreground)) {
                                append(nombre.substring(0, idx))
                            }
                            withStyle(
                                SpanStyle(
                                    background = BlueLight,
                                    color = BluePrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append(nombre.substring(idx, idx + query.length))
                            }
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Foreground)) {
                                append(nombre.substring(idx + query.length))
                            }
                        } else {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Foreground)) {
                                append(nombre)
                            }
                        }
                    },
                    fontSize = 14.sp
                )
                Text(
                    "${espacio.id} · Bloque ${espacio.bloque} · ${espacio.tipo}",
                    fontSize = 11.5.sp,
                    color = MutedForeground
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Ver detalle",
                tint = Color(0xFFBDBDBD)
            )
        }
    }
}