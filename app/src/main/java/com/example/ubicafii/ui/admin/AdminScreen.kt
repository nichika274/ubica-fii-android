package com.example.ubicafii.ui.admin

import android.net.Uri
import android.util.Log
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

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
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar espacio")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header con Gradiente de la App
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Blue900, Blue700)   // ← Gradiente original
                            )
                        )
                        .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = MaterialTheme.colorScheme.onPrimary)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Administración", color = MaterialTheme.colorScheme.onPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("${espacios.size} espacios registrados", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f), fontSize = 13.sp, modifier = Modifier.padding(start = 16.dp))
                        Spacer(modifier = Modifier.height(16.dp))

                        // Fila de Filtros (Chips adaptados)
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val isAllSelected = filtroBloque == "all"
                            FilterChip(
                                selected = isAllSelected,
                                onClick = { filtroBloque = "all" },
                                label = { Text("Todos", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = if (isAllSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
                                    labelColor = if (isAllSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary,
                                    selectedContainerColor = MaterialTheme.colorScheme.surface,
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                ),
                                border = null,
                                shape = RoundedCornerShape(24.dp)
                            )
                            bloquesMap.keys.forEach { id ->
                                val isIdSelected = filtroBloque == id
                                FilterChip(
                                    selected = isIdSelected,
                                    onClick = { filtroBloque = id },
                                    label = { Text(id, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = if (isIdSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
                                        labelColor = if (isIdSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary,
                                        selectedContainerColor = MaterialTheme.colorScheme.surface,
                                        selectedLabelColor = MaterialTheme.colorScheme.primary
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
                            border = if (isSystemInDarkTheme()) BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)) else null,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                                        imageVector = getTypeIcon(espacio.tipo),
                                        contentDescription = null,
                                        tint = getTypeColor(espacio.tipo),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(espacio.nombre, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("${espacio.id} · Bloque ${espacio.bloque} · ${getFloorLabel(espacio.piso.toString())}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = {
                                    editando = espacio
                                    mostrarFormulario = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
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
    val scope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf(espacio?.nombre ?: "") }
    var tipo by remember { mutableStateOf(espacio?.tipo ?: "Aula") }
    var piso by remember { mutableStateOf(espacio?.piso?.toString() ?: "1") }
    var bloque by remember { mutableStateOf(espacio?.bloque ?: "A") }
    var codigo by remember { mutableStateOf(espacio?.id?.toString() ?: "") }
    var descripcion by remember { mutableStateOf(espacio?.descripcion ?: "") }
    var indicaciones by remember { mutableStateOf(espacio?.indicaciones ?: "") }
    var fotoLocalPath by remember { mutableStateOf(espacio?.fotoUrl ?: "") }
    var subiendoImagen by remember { mutableStateOf(false) }

    var coordenadaX by remember { mutableStateOf(espacio?.coordenadaX ?: 0.5f) }
    var coordenadaY by remember { mutableStateOf(espacio?.coordenadaY ?: 0.5f) }

    val planoResource = getFloorPlanResource(bloque, piso)

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            scope.launch {
                subiendoImagen = true
                try {
                    val api = RetrofitClient.instance
                    val inputStream = context.contentResolver.openInputStream(selectedUri)
                    val bytes = inputStream?.readBytes()
                    val requestBody = bytes?.toRequestBody("image/*".toMediaTypeOrNull())
                    val part = MultipartBody.Part.createFormData("foto", "foto.jpg", requestBody!!)

                    val response = api.uploadImage(part)
                    fotoLocalPath = response.url
                } catch (e: Exception) {
                    e.printStackTrace()
                    val fileName = "espacio_${System.currentTimeMillis()}.jpg"
                    fotoLocalPath = copyUriToInternalStorage(context, selectedUri, fileName)
                } finally {
                    subiendoImagen = false
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Barra superior azul adaptada
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Blue900, Blue700)   // ← Gradiente original
                    )
                )
                .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onCancelar) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = MaterialTheme.colorScheme.onPrimary)
                }
                Text(
                    text = if (espacio != null) "Editar espacio" else "Nuevo espacio",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onCancelar) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = MaterialTheme.colorScheme.onPrimary)
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
            Text("Nombre del espacio", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 6.dp))
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                placeholder = { Text("Ej: Aula 301") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ───── Código ─────
            Text("Código", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 6.dp))
            OutlinedTextField(
                value = codigo,
                onValueChange = { codigo = it },
                placeholder = { Text("Ej: FII-A-301") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = espacio == null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ───── Tipo de espacio ─────
            Text("Tipo de espacio", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 6.dp))
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
                                    tint = if (selected) getTypeColor(t) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = t,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selected) getTypeColor(t) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            selectedContainerColor = getTypeBgColor(t)
                        ),
                        border = if (selected) BorderStroke(1.5.dp, getTypeColor(t)) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ───── Bloque ─────
            Text("Bloque", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 6.dp))
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
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ───── Piso ─────
            Text("Piso", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val floorOptions = listOf(
                    "Planta baja" to "0",
                    "Primer piso" to "1",
                    "Segundo piso" to "2"
                )
                floorOptions.forEach { (label, value) ->
                    val selected = piso == value
                    FilterChip(
                        selected = selected,
                        onClick = { piso = value },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ───── Descripción ─────
            Text("Descripción", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 6.dp))
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                placeholder = { Text("Describe el espacio, capacidad, equipamiento...") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ───── Indicaciones para llegar ─────
            Text("Indicaciones para llegar", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 6.dp))
            OutlinedTextField(
                value = indicaciones,
                onValueChange = { indicaciones = it },
                placeholder = { Text("Ej: Sube por la escalera principal, segunda puerta...") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ───── Foto del espacio ─────
            Text("Foto del espacio", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    enabled = !subiendoImagen,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    if (subiendoImagen) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Subiendo...", color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Seleccionar foto", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))

                if (fotoLocalPath.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        val imageModel = if (fotoLocalPath.startsWith("/") || fotoLocalPath.contains("filesDir")) {
                            File(fotoLocalPath)
                        } else {
                            fotoLocalPath
                        }
                        AsyncImage(
                            model = imageModel,
                            contentDescription = "Vista previa",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // ───── Ubicación en el plano ─────
            Spacer(modifier = Modifier.height(16.dp))
            Text("Ubicación en el plano", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 4.dp))
            Text(
                "Arrastra el marcador azul hasta la posición exacta",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val imagePainter = painterResource(id = planoResource)

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()

                            val containerWidthPx = size.width.toFloat()
                            val containerHeightPx = size.height.toFloat()

                            val imageAspect = imagePainter.intrinsicSize.width / imagePainter.intrinsicSize.height
                            val containerAspect = containerWidthPx / containerHeightPx

                            val drawnWidth = if (containerAspect > imageAspect) containerHeightPx * imageAspect else containerWidthPx
                            val drawnHeight = if (containerAspect > imageAspect) containerHeightPx else containerWidthPx / imageAspect

                            val fitOffsetX = (containerWidthPx - drawnWidth) / 2f
                            val fitOffsetY = (containerHeightPx - drawnHeight) / 2f

                            val currentXPx = fitOffsetX + (coordenadaX * drawnWidth) + dragAmount.x
                            val currentYPx = fitOffsetY + (coordenadaY * drawnHeight) + dragAmount.y

                            coordenadaX = ((currentXPx - fitOffsetX) / drawnWidth).coerceIn(0f, 1f)
                            coordenadaY = ((currentYPx - fitOffsetY) / drawnHeight).coerceIn(0f, 1f)
                        }
                    }
            ) {
                val containerWidthPx = with(density) { maxWidth.toPx() }
                val containerHeightPx = with(density) { maxHeight.toPx() }

                val imageAspect = imagePainter.intrinsicSize.width / imagePainter.intrinsicSize.height
                val containerAspect = containerWidthPx / containerHeightPx

                val drawnWidth = if (containerAspect > imageAspect) containerHeightPx * imageAspect else containerWidthPx
                val drawnHeight = if (containerAspect > imageAspect) containerHeightPx else containerWidthPx / imageAspect

                val fitOffsetX = (containerWidthPx - drawnWidth) / 2f
                val fitOffsetY = (containerHeightPx - drawnHeight) / 2f

                Image(
                    painter = imagePainter,
                    contentDescription = "Plano",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                val markerX = fitOffsetX + (coordenadaX * drawnWidth) - with(density) { 12.dp.toPx() }
                val markerY = fitOffsetY + (coordenadaY * drawnHeight) - with(density) { 12.dp.toPx() }

                Box(
                    modifier = Modifier
                        .offset { IntOffset(markerX.toInt(), markerY.toInt()) }
                        .size(24.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de guardar
            Button(
                onClick = {
                    val nuevoEspacio = Espacio(
                        id = if (codigo.isNotEmpty()) codigo.toIntOrNull() ?: 0 else 0,
                        nombre = nombre,
                        tipo = tipo,
                        piso = piso.toIntOrNull() ?: 0,
                        bloque = bloque,
                        descripcion = descripcion,
                        indicaciones = indicaciones,
                        fotoUrl = fotoLocalPath,
                        coordenadaX = coordenadaX,
                        coordenadaY = coordenadaY
                    )
                    onGuardar(nuevoEspacio)
                },
                enabled = nombre.isNotEmpty() && (espacio != null || codigo.isNotEmpty()),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Guardar Espacio", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}