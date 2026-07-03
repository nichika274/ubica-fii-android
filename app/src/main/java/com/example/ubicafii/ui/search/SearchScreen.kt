package com.example.ubicafii.ui.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ubicafii.data.model.Espacio
import com.example.ubicafii.ui.theme.*
import com.example.ubicafii.ui.components.getTypeBgColor
import com.example.ubicafii.ui.components.getTypeColor
import com.example.ubicafii.ui.components.getTypeIcon
import com.example.ubicafii.util.PreferencesManager

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = viewModel(),
    onBack: () -> Unit,
    onSpaceClick: (Int) -> Unit
) {
    val query by viewModel.query.collectAsState()
    val resultados by viewModel.resultados.collectAsState()
    val cargando by viewModel.cargando.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val context = LocalContext.current

    val searchRecents = remember { mutableStateListOf<String>() }
    val populares = listOf("Aula", "Laboratorio", "Biblioteca", "Cafetería", "Baño")

    LaunchedEffect(Unit) {
        searchRecents.clear()
        searchRecents.addAll(PreferencesManager.getSearchRecents(context))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header con barra de búsqueda circular
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
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.width(4.dp))

            // REEMPLAZO SEGURO CON DECORATION BOX PARA MANEJAR CONTENT PADDING
            val interactionSource = remember { MutableInteractionSource() }
            val colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            BasicTextField(
                value = query,
                onValueChange = viewModel::actualizarQuery,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .focusRequester(focusRequester),
                interactionSource = interactionSource,
                singleLine = true,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    OutlinedTextFieldDefaults.DecorationBox(
                        value = query,
                        innerTextField = innerTextField,
                        enabled = true,
                        singleLine = true,
                        visualTransformation = VisualTransformation.None,
                        interactionSource = interactionSource,
                        placeholder = {
                            Text(
                                "Buscar por nombre o código…",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Buscar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { viewModel.actualizarQuery("") }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Limpiar",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        },
                        colors = colors,
                        container = {
                            OutlinedTextFieldDefaults.ContainerBox(
                                enabled = true,
                                isError = false,
                                interactionSource = interactionSource,
                                colors = colors,
                                shape = RoundedCornerShape(24.dp)
                            )
                        },
                        // Aquí modificamos el padding vertical internamente sin romper firmas de métodos
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    )
                }
            )
        }

        if (cargando) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary)
        }

        if (query.isBlank()) {
            // Sugerencias: Recientes + Populares
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Recientes
                if (searchRecents.isNotEmpty()) {
                    item {
                        Text(
                            "Búsquedas recientes",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    item {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            searchRecents.forEach { termino ->
                                SuggestionChip(
                                    onClick = {
                                        viewModel.actualizarQuery(termino)
                                        viewModel.guardarBusqueda(termino)
                                    },
                                    label = { Text(termino) },
                                    icon = {
                                        Icon(
                                            Icons.Default.History,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    shape = RoundedCornerShape(24.dp),
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        iconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }
                }

                // Populares
                item {
                    Text(
                        "Populares",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        populares.forEach { termino ->
                            FilterChip(
                                selected = false,
                                onClick = {
                                    viewModel.actualizarQuery(termino)
                                    viewModel.guardarBusqueda(termino)
                                },
                                label = { Text(termino) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                shape = RoundedCornerShape(24.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.onSurface,
                                    iconColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }
        } else if (resultados.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.SearchOff,
                        contentDescription = "Sin resultados",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Sin resultados", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Text("No se encontró \"$query\"", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            Text(
                "${resultados.size} resultado(s)",
                modifier = Modifier.padding(16.dp),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(resultados) { espacio ->
                    TarjetaResultadoBusqueda(espacio, query, onClick = { onSpaceClick(espacio.id) })
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
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)) {
                                append(nombre.substring(0, idx))
                            }
                            withStyle(
                                SpanStyle(
                                    background = MaterialTheme.colorScheme.primaryContainer,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append(nombre.substring(idx, idx + query.length))
                            }
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)) {
                                append(nombre.substring(idx + query.length))
                            }
                        } else {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)) {
                                append(nombre)
                            }
                        }
                    },
                    fontSize = 14.sp
                )
                Text(
                    "${espacio.id} · Bloque ${espacio.bloque} · ${espacio.tipo}",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Ver detalle",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}