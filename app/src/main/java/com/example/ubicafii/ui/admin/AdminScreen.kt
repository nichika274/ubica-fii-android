package com.example.ubicafii.ui.admin

import com.example.ubicafii.data.model.Espacio
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
import com.example.ubicafii.ui.theme.*
import com.example.ubicafii.ui.components.getTypeIcon
import com.example.ubicafii.ui.components.getTypeColor
import com.example.ubicafii.ui.components.getTypeBgColor
import com.example.ubicafii.ui.floors.bloquesMap
import com.example.ubicafii.util.copyUriToInternalStorage
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
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
                                    Text("${espacio.id} · Bloque ${espacio.bloque} · Piso ${espacio.piso}", fontSize = 11.sp, color = MutedForeground)
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
    val scope = rememberCoroutineScope() // ◄ El scope correcto de Jetpack Compose

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

    var drawnWidth by remember { mutableStateOf(0f) }
    var drawnHeight by remember { mutableStateOf(0f) }
    var fitOffsetX by remember { mutableStateOf(0f) }
    var fitOffsetY by remember { mutableStateOf(0f) }

    var markerX by remember { mutableStateOf(0f) }
    var markerY by remember { mutableStateOf(0f) }

    val planoResource = when (piso) {
        "Sótano" -> R.drawable.plano_piso1
        "1" -> R.drawable.plano_piso1
        "2" -> R.drawable.plano_piso1
        "3" -> R.drawable.plano_piso1
        else -> R.drawable.plano_piso1
    }

    LaunchedEffect(drawnWidth, drawnHeight, fitOffsetX, fitOffsetY) {
        if (drawnWidth > 0f && drawnHeight > 0f) {
            markerX = fitOffsetX + (coordenadaX * drawnWidth)
            markerY = fitOffsetY + (coordenadaY * drawnHeight)
        }
    }

    // ◄ LAUNCHER CORREGIDO USANDO 'scope' Y 'RetrofitClient.instance' ►
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            scope.launch { // ◄ Usando el CoroutineScope de Compose
                subiendoImagen = true
                try {
                    val api = RetrofitClient.instance // ◄ Utiliza tu Singleton centralizado
                    val inputStream = context.contentResolver.openInputStream(selectedUri)
                    val bytes = inputStream?.readBytes()
                    val requestBody = bytes?.toRequestBody("image/*".toMediaTypeOrNull())
                    val part = MultipartBody.Part.createFormData("foto", "foto.jpg", requestBody!!)

                    val response = api.uploadImage(part)
                    fotoLocalPath = response.url // URL pública devuelta por el servidor
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Fallback local en modo offline si el backend falla
                    val fileName = "espacio_${System.currentTimeMillis()}.jpg"
                    fotoLocalPath = copyUriToInternalStorage(context, selectedUri, fileName)
                } finally {
                    subiendoImagen = false
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Nombre del espacio", fontWeight = FontWeight.Bold, color = Foreground, modifier = Modifier.padding(bottom = 6.dp))
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                placeholder = { Text("Ej: Aula 301", color = Color(0xFFBDBDBD)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BluePrimary, unfocusedBorderColor = Color(0xFFE0E0E0)),
                shape = RoundedCornerShape(24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Código", fontWeight = FontWeight.Bold, color = Foreground, modifier = Modifier.padding(bottom = 6.dp))
            OutlinedTextField(
                value = codigo,
                onValueChange = { codigo = it },
                placeholder = { Text("Ej: FII-A-301", color = Color(0xFFBDBDBD)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = espacio == null,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BluePrimary, unfocusedBorderColor = Color(0xFFE0E0E0)),
                shape = RoundedCornerShape(24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Tipo de espacio", fontWeight = FontWeight.Bold, color = Foreground, modifier = Modifier.padding(bottom = 6.dp))
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
                                Icon(imageVector = getTypeIcon(t), contentDescription = null, modifier = Modifier.size(16.dp), tint = if (selected) getTypeColor(t) else MutedForeground)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = t, fontSize = 12.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, color = if (selected) getTypeColor(t) else MutedForeground)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(containerColor = Muted.copy(alpha = 0.4f), selectedContainerColor = getTypeBgColor(t)),
                        border = if (selected) BorderStroke(1.5.dp, getTypeColor(t)) else BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        shape = RoundedCornerShape(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Bloque", fontWeight = FontWeight.Bold, color = Foreground, modifier = Modifier.padding(bottom = 6.dp))
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
                        colors = FilterChipDefaults.filterChipColors(containerColor = Muted.copy(alpha = 0.4f), selectedContainerColor = BluePrimary, selectedLabelColor = Color.White, labelColor = MutedForeground),
                        border = if (selected) null else BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        shape = RoundedCornerShape(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Piso", fontWeight = FontWeight.Bold, color = Foreground, modifier = Modifier.padding(bottom = 6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..3).forEach { num ->
                    val selected = piso == num.toString()
                    FilterChip(
                        selected = selected,
                        onClick = { piso = num.toString() },
                        label = { Text("Piso $num", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(containerColor = Muted.copy(alpha = 0.4f), selectedContainerColor = BluePrimary, selectedLabelColor = Color.White, labelColor = MutedForeground),
                        border = if (selected) null else BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        shape = RoundedCornerShape(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Descripción", fontWeight = FontWeight.Bold, color = Foreground, modifier = Modifier.padding(bottom = 6.dp))
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                placeholder = { Text("Describe el espacio, capacidad, equipamiento...", color = Color(0xFFBDBDBD)) },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BluePrimary, unfocusedBorderColor = Color(0xFFE0E0E0)),
                shape = RoundedCornerShape(24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Indicaciones para llegar", fontWeight = FontWeight.Bold, color = Foreground, modifier = Modifier.padding(bottom = 6.dp))
            OutlinedTextField(
                value = indicaciones,
                onValueChange = { indicaciones = it },
                placeholder = { Text("Ej: Sube por la escalera principal, segunda puerta...", color = Color(0xFFBDBDBD)) },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BluePrimary, unfocusedBorderColor = Color(0xFFE0E0E0)),
                shape = RoundedCornerShape(24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Foto del espacio", fontWeight = FontWeight.Bold, color = Foreground, modifier = Modifier.padding(bottom = 6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    enabled = !subiendoImagen,
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    if (subiendoImagen) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Subiendo...", color = Color.White)
                    } else {
                        Icon(Icons.Default.Image, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Seleccionar foto", color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))

                if (fotoLocalPath.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.LightGray)
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

            Spacer(modifier = Modifier.height(16.dp))
            Text("Ubicación en el plano", fontWeight = FontWeight.Bold, color = Foreground, modifier = Modifier.padding(bottom = 4.dp))
            Text(
                "Arrastra el marcador azul hasta la posición exacta",
                fontSize = 12.sp,
                color = MutedForeground,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFE8EDF2))
            ) {
                val containerWidthPx = with(density) { maxWidth.toPx() }
                val containerHeightPx = with(density) { maxHeight.toPx() }

                val imagePainter = painterResource(id = planoResource)
                val imageOriginalWidth = imagePainter.intrinsicSize.width
                val imageOriginalHeight = imagePainter.intrinsicSize.height

                val imageAspect = imageOriginalWidth / imageOriginalHeight
                val containerAspect = containerWidthPx / containerHeightPx

                val currentDrawnWidth = if (containerAspect > imageAspect) containerHeightPx * imageAspect else containerWidthPx
                val currentDrawnHeight = if (containerAspect > imageAspect) containerHeightPx else containerWidthPx / imageAspect

                SideEffect {
                    drawnWidth = currentDrawnWidth
                    drawnHeight = currentDrawnHeight
                    fitOffsetX = (containerWidthPx - currentDrawnWidth) / 2f
                    fitOffsetY = (containerHeightPx - currentDrawnHeight) / 2f
                }

                Image(
                    painter = imagePainter,
                    contentDescription = "Plano",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                val markerSizeDp = 24.dp
                val markerRadiusPx = with(density) { markerSizeDp.toPx() / 2f }

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (markerX - markerRadiusPx).toInt(),
                                (markerY - markerRadiusPx).toInt()
                            )
                        }
                        .size(markerSizeDp)
                        .pointerInput(drawnWidth, drawnHeight, fitOffsetX, fitOffsetY) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                if (drawnWidth > 0f && drawnHeight > 0f) {
                                    markerX = (markerX + dragAmount.x).coerceIn(fitOffsetX, fitOffsetX + drawnWidth)
                                    markerY = (markerY + dragAmount.y).coerceIn(fitOffsetY, fitOffsetY + drawnHeight)

                                    coordenadaX = (markerX - fitOffsetX) / drawnWidth
                                    coordenadaY = (markerY - fitOffsetY) / drawnHeight
                                }
                            }
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.Center)
                            .background(BluePrimary, CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "X: ${String.format("%.0f", coordenadaX * 100)}%, Y: ${String.format("%.0f", coordenadaY * 100)}%",
                fontSize = 11.sp,
                color = MutedForeground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    val nuevo = Espacio(
                        id = codigo.toIntOrNull() ?: (espacio?.id ?: 0),
                        nombre = nombre,
                        tipo = tipo,
                        piso = piso.toIntOrNull() ?: 1,
                        descripcion = descripcion,
                        fotoUrl = fotoLocalPath,
                        indicaciones = indicaciones,
                        bloque = bloque,
                        coordenadaX = coordenadaX,
                        coordenadaY = coordenadaY
                    )
                    onGuardar(nuevo)
                },
                enabled = !subiendoImagen,
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