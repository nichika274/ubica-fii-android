package com.example.ubicafii.ui.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.ubicafii.R
import com.example.ubicafii.data.api.RetrofitClient
import com.example.ubicafii.data.model.Espacio
import com.example.ubicafii.ui.theme.*
import com.example.ubicafii.ui.components.getTypeIcon
import com.example.ubicafii.ui.components.getTypeColor
import com.example.ubicafii.ui.components.getTypeBgColor
import com.example.ubicafii.ui.floors.bloquesMap
import com.example.ubicafii.util.copyUriToInternalStorage
import com.example.ubicafii.util.getFloorLabel
import com.example.ubicafii.util.getFloorPlanResource
import java.io.File
import kotlinx.coroutines.launch

val tiposEspacio = listOf("Aula", "Laboratorio", "Oficina", "Baño", "Biblioteca", "Cafetería", "Otro")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: AdminViewModel = viewModel(),
    onBack: () -> Unit
) {
    var espacioAEliminar by remember { mutableStateOf<Int?>(null) }
    val espacios by viewModel.espacios.collectAsState()
    var filtroBloque by remember { mutableStateOf("all") }
    var mostrarFormulario by remember { mutableStateOf(false) }
    var editando by remember { mutableStateOf<Espacio?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val mensaje by viewModel.mensaje.collectAsState()

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensaje()
        }
    }

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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editando = null
                    mostrarFormulario = true
                },
                containerColor = BluePrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar espacio")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtrados) { espacio ->
                        Card(
                            shape = RoundedCornerShape(24.dp),
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
                                    Text("${espacio.id} · Bloque ${espacio.bloque} · ${getFloorLabel(espacio.piso.toString())}", fontSize = 11.sp, color = MutedForeground)
                                }
                                IconButton(onClick = {
                                    editando = espacio
                                    mostrarFormulario = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = BluePrimary)
                                }
                                IconButton(onClick = {
                                    espacioAEliminar = espacio.id
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFC62828))
                                }
                            }
                        }
                    }
                }
            }

            if (espacioAEliminar != null) {
                AlertDialog(
                    onDismissRequest = { espacioAEliminar = null },
                    title = { Text("Eliminar espacio") },
                    text = { Text("¿Estás seguro de que deseas eliminar este espacio?") },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.eliminar(espacioAEliminar!!)
                            espacioAEliminar = null
                        }) {
                            Text("Eliminar", color = Color(0xFFC62828))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { espacioAEliminar = null }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
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
    val context = LocalContext.current
    val density = LocalDensity.current

    var nombre by remember { mutableStateOf(espacio?.nombre ?: "") }
    var tipo by remember { mutableStateOf(espacio?.tipo ?: "Aula") }
    var piso by remember { mutableStateOf(espacio?.piso?.toString() ?: "1") }
    var bloque by remember { mutableStateOf(espacio?.bloque ?: "A") }
    var codigo by remember { mutableStateOf(espacio?.id?.toString() ?: "") }
    var descripcion by remember { mutableStateOf(espacio?.descripcion ?: "") }
    var indicaciones by remember { mutableStateOf(espacio?.indicaciones ?: "") }

    var coordenadaX by remember { mutableStateOf(espacio?.coordenadaX ?: 0.5f) }
    var coordenadaY by remember { mutableStateOf(espacio?.coordenadaY ?: 0.5f) }
    var fotoUrl by remember { mutableStateOf(espacio?.fotoUrl ?: "") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val path = copyUriToInternalStorage(context, it, "temp_photo.jpg")
            if (path != null) {
                fotoUrl = path
            }
        }
    }

    // Solución del error: Pasamos tanto bloque como piso al llamar a la utilidad global
    val planoResource = getFloorPlanResource(bloque, piso)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (espacio == null) "Nuevo Espacio" else "Editar Espacio",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Blue900
        )

        OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre del espacio") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = codigo, onValueChange = { codigo = it }, label = { Text("Código / ID") }, modifier = Modifier.fillMaxWidth(), enabled = espacio == null)

        // Selección de Bloque
        OutlinedTextField(value = bloque, onValueChange = { bloque = it.uppercase() }, label = { Text("Bloque (Ej: A, B, D)") }, modifier = Modifier.fillMaxWidth())

        // Selección de Piso
        OutlinedTextField(value = piso, onValueChange = { piso = it }, label = { Text("Piso (0 para Planta Baja, 1, 2)") }, modifier = Modifier.fillMaxWidth())

        OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
        OutlinedTextField(value = indicaciones, onValueChange = { indicaciones = it }, label = { Text("Indicaciones de cómo llegar") }, modifier = Modifier.fillMaxWidth(), minLines = 2)

        // Selección de Foto
        Button(
            onClick = { launcher.launch("image/*") },
            colors = ButtonDefaults.buttonColors(containerColor = BlueLight, contentColor = BluePrimary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Image, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Seleccionar Imagen")
        }

        if (fotoUrl.isNotEmpty()) {
            Text("Imagen seleccionada: $fotoUrl", fontSize = 11.sp, color = MutedForeground)
        }

        // Selección Interactiva de Coordenadas
        Text("Ubicación en el Mapa (Arrastra el marcador rojo):", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF1F5F9))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val currentWidth = size.width
                        val currentHeight = size.height

                        val newXPx = (coordenadaX * currentWidth) + dragAmount.x
                        val newYPx = (coordenadaY * currentHeight) + dragAmount.y

                        coordenadaX = (newXPx / currentWidth).coerceIn(0f, 1f)
                        coordenadaY = (newYPx / currentHeight).coerceIn(0f, 1f)
                    }
                }
        ) {
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
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            val markerX = fitOffsetX + (coordenadaX * drawnWidth) - with(density) { 12.dp.toPx() }
            val markerY = fitOffsetY + (coordenadaY * drawnHeight) - with(density) { 12.dp.toPx() }

            Box(
                modifier = Modifier
                    .offset { IntOffset(markerX.toInt(), markerY.toInt()) }
                    .size(24.dp)
                    .background(Color.Red, CircleShape)
                    .border(2.dp, Color.White, CircleShape)
            )
        }

        Text(text = "Coordenadas: X: ${(coordenadaX * 100).toInt()}% | Y: ${(coordenadaY * 100).toInt()}%", fontSize = 12.sp, color = MutedForeground, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

        // Botones de Acción del Formulario
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onCancelar, modifier = Modifier.weight(1f)) {
                Text("Cancelar")
            }
            Button(
                onClick = {
                    if (nombre.isNotBlank() && codigo.isNotBlank()) {
                        val nuevoEspacio = Espacio(
                            id = codigo.toIntOrNull() ?: 0,
                            nombre = nombre,
                            tipo = tipo,
                            piso = piso.toIntOrNull() ?: 1,
                            bloque = bloque,
                            descripcion = descripcion,
                            indicaciones = indicaciones,
                            coordenadaX = coordenadaX,
                            coordenadaY = coordenadaY,
                            fotoUrl = fotoUrl
                        )
                        onGuardar(nuevoEspacio)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                modifier = Modifier.weight(1f)
            ) {
                Text("Guardar")
            }
        }
    }
}