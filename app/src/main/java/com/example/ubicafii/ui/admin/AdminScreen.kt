package com.example.ubicafii.ui.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.ubicafii.data.model.Espacio
import com.example.ubicafii.ui.theme.*
import com.example.ubicafii.ui.components.getTypeIcon
import com.example.ubicafii.ui.components.getTypeColor
import com.example.ubicafii.ui.components.getTypeBgColor
import com.example.ubicafii.ui.floors.bloquesMap

val tiposEspacio = listOf("Aula", "Laboratorio", "Oficina", "Baño", "Biblioteca", "Cafetería", "Otro")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: AdminViewModel = viewModel(),
    onBack: () -> Unit
) {
    val espacios by viewModel.espacios.collectAsState()
    var filtroBloque by remember { mutableStateOf("all") }
    var mostrarFormulario by remember { mutableStateOf(false) }
    var editando by remember { mutableStateOf<Espacio?>(null) }

    val filtrados = if (filtroBloque == "all") espacios else espacios.filter { it.bloque == filtroBloque }

    if (mostrarFormulario) {
        SpaceFormScreen(
            espacio = editando,
            onGuardar = { espacio ->
                if (editando != null) viewModel.actualizar(editando!!.id, espacio)
                else viewModel.agregar(espacio)
                mostrarFormulario = false
                editando = null
            },
            onCancelar = {
                mostrarFormulario = false
                editando = null
            }
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(brush = Brush.verticalGradient(listOf(Blue900, Blue700)))
                    .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Administración", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("${espacios.size} espacios registrados", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp, modifier = Modifier.padding(start = 16.dp))
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = filtroBloque == "all",
                            onClick = { filtroBloque = "all" },
                            label = { Text("Todos", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = if (filtroBloque == "all") Color.White else Color.White.copy(alpha = 0.18f),
                                labelColor = if (filtroBloque == "all") BluePrimary else Color.White,
                                selectedContainerColor = Color.White,
                                selectedLabelColor = BluePrimary
                            ),
                            border = null,
                            shape = RoundedCornerShape(24.dp)
                        )
                        bloquesMap.keys.forEach { id ->
                            FilterChip(
                                selected = filtroBloque == id,
                                onClick = { filtroBloque = id },
                                label = { Text(id, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = if (filtroBloque == id) Color.White else Color.White.copy(alpha = 0.18f),
                                    labelColor = if (filtroBloque == id) BluePrimary else Color.White,
                                    selectedContainerColor = Color.White,
                                    selectedLabelColor = BluePrimary
                                ),
                                border = null,
                                shape = RoundedCornerShape(24.dp)
                            )
                        }
                    }
                }
            }

            // Lista de espacios
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtrados) { espacio ->
                    Card(
                        shape = RoundedCornerShape(24.dp), // Cambiado a 24.dp para consistencia visual
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(getTypeBgColor(espacio.tipo), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    getTypeIcon(espacio.tipo),
                                    contentDescription = null,
                                    tint = getTypeColor(espacio.tipo),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(espacio.nombre, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Foreground)
                                Text("${espacio.id} · Bloque ${espacio.bloque} · Piso ${espacio.piso}", fontSize = 11.sp, color = MutedForeground)
                            }
                            IconButton(onClick = {
                                editando = espacio
                                mostrarFormulario = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = BluePrimary)
                            }
                            IconButton(onClick = { viewModel.eliminar(espacio.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFC62828))
                            }
                        }
                    }
                }
            }
        }

        // FAB circular moderno
        FloatingActionButton(
            onClick = {
                editando = null
                mostrarFormulario = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = BluePrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Agregar espacio")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpaceFormScreen(
    espacio: Espacio?,
    onGuardar: (Espacio) -> Unit,
    onCancelar: () -> Unit
) {
    var nombre by remember { mutableStateOf(espacio?.nombre ?: "") }
    var tipo by remember { mutableStateOf(espacio?.tipo ?: "Aula") }
    var piso by remember { mutableStateOf(espacio?.piso?.toString() ?: "1") }
    var bloque by remember { mutableStateOf(espacio?.bloque ?: "A") }
    var codigo by remember { mutableStateOf(espacio?.id?.toString() ?: "") }
    var descripcion by remember { mutableStateOf(espacio?.descripcion ?: "") }
    var indicaciones by remember { mutableStateOf(espacio?.indicaciones ?: "") }
    var fotoUrl by remember { mutableStateOf(espacio?.fotoUrl ?: "") }
    var fotoUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            fotoUri = it
            fotoUrl = it.toString()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Barra superior azul
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = Brush.verticalGradient(listOf(Blue900, Blue700)))
                .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onCancelar) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
                Text(
                    text = if (espacio != null) "Editar espacio" else "Nuevo espacio",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onCancelar) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                }
            }
        }

        // Contenido desplazable del formulario
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ───── Nombre ─────
            Text(
                "Nombre del espacio",
                fontWeight = FontWeight.Bold,
                color = Foreground,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                placeholder = { Text("Ej: Aula 301", color = Color(0xFFBDBDBD)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BluePrimary,
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                ),
                shape = RoundedCornerShape(24.dp) // Curvatura estilizada tipo píldora
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ───── Código ─────
            Text(
                "Código",
                fontWeight = FontWeight.Bold,
                color = Foreground,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            OutlinedTextField(
                value = codigo,
                onValueChange = { codigo = it },
                placeholder = { Text("Ej: FII-A-301", color = Color(0xFFBDBDBD)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BluePrimary,
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                ),
                shape = RoundedCornerShape(24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ───── SOLUCIÓN 2: Tipo de espacio con Iconos y Colores dinámicos ─────
            Text(
                "Tipo de espacio",
                fontWeight = FontWeight.Bold,
                color = Foreground,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 4.dp)
            ) {
                tiposEspacio.forEach { t ->
                    val selected = tipo == t
                    FilterChip(
                        selected = selected,
                        onClick = { tipo = t },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = getTypeIcon(t),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (selected) getTypeColor(t) else MutedForeground
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = t,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selected) getTypeColor(t) else MutedForeground
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Muted.copy(alpha = 0.4f),
                            selectedContainerColor = getTypeBgColor(t)
                        ),
                        border = if (selected) {
                            BorderStroke(1.5.dp, getTypeColor(t))
                        } else {
                            BorderStroke(1.dp, Color(0xFFE0E0E0))
                        },
                        shape = RoundedCornerShape(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ───── Bloque ─────
            Text(
                "Bloque",
                fontWeight = FontWeight.Bold,
                color = Foreground,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                bloquesMap.keys.forEach { id ->
                    val selected = bloque == id
                    FilterChip(
                        selected = selected,
                        onClick = { bloque = id },
                        label = { Text(id, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Muted.copy(alpha = 0.4f),
                            selectedContainerColor = BluePrimary,
                            selectedLabelColor = Color.White,
                            labelColor = MutedForeground
                        ),
                        border = if (selected) null else BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        shape = RoundedCornerShape(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ───── Piso ─────
            Text(
                "Piso",
                fontWeight = FontWeight.Bold,
                color = Foreground,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..3).forEach { num ->
                    val selected = piso == num.toString()
                    FilterChip(
                        selected = selected,
                        onClick = { piso = num.toString() },
                        label = { Text("Piso $num", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Muted.copy(alpha = 0.4f),
                            selectedContainerColor = BluePrimary,
                            selectedLabelColor = Color.White,
                            labelColor = MutedForeground
                        ),
                        border = if (selected) null else BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        shape = RoundedCornerShape(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ───── Descripción ─────
            Text(
                "Descripción",
                fontWeight = FontWeight.Bold,
                color = Foreground,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                placeholder = { Text("Describe el espacio, capacidad, equipamiento...", color = Color(0xFFBDBDBD)) },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BluePrimary,
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                ),
                shape = RoundedCornerShape(24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ───── Indicaciones para llegar ─────
            Text(
                "Indicaciones para llegar",
                fontWeight = FontWeight.Bold,
                color = Foreground,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            OutlinedTextField(
                value = indicaciones,
                onValueChange = { indicaciones = it },
                placeholder = { Text("Ej: Sube por la escalera principal, segunda puerta...", color = Color(0xFFBDBDBD)) },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BluePrimary,
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                ),
                shape = RoundedCornerShape(24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ───── Foto del espacio ─────
            Text(
                "Foto del espacio",
                fontWeight = FontWeight.Bold,
                color = Foreground,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Seleccionar foto", color = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                if (fotoUri != null || fotoUrl.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.LightGray)
                    ) {
                        AsyncImage(
                            model = fotoUri ?: fotoUrl,
                            contentDescription = "Vista previa",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Botón Guardar
            Button(
                onClick = {
                    val nuevo = Espacio(
                        id = espacio?.id ?: 0,
                        nombre = nombre,
                        tipo = tipo,
                        piso = piso.toIntOrNull() ?: 1,
                        descripcion = descripcion,
                        fotoUrl = fotoUrl.ifBlank { fotoUri?.toString() ?: "" },
                        indicaciones = indicaciones,
                        bloque = bloque
                    )
                    onGuardar(nuevo)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Guardar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}