package com.example.ubicafii.ui.profile

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ubicafii.data.repository.EspacioRepository
import com.example.ubicafii.util.PreferencesManager
import com.example.ubicafii.ui.theme.*

@Composable
fun ProfileScreen(
    navigateToAdmin: () -> Unit,
    onBack: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    isDarkTheme: Boolean,
    onToggleDarkTheme: () -> Unit
) {
    val context = LocalContext.current
    var mostrarFavoritos by remember { mutableStateOf(false) }
    var mostrarRecientes by remember { mutableStateOf(false) }
    var mostrarDialogoReporte by remember { mutableStateOf(false) }
    var mostrarDialogoAdmin by remember { mutableStateOf(false) }

    var listaFavoritos by remember { mutableStateOf<List<Pair<Int, String>>>(emptyList()) }
    var listaRecientes by remember { mutableStateOf<List<Pair<Int, String>>>(emptyList()) }

    var pinIngresado by remember { mutableStateOf("") }
    var textoReporte by remember { mutableStateOf("") }

    // Carga de nombres reales para los diálogos
    LaunchedEffect(mostrarFavoritos) {
        if (mostrarFavoritos) {
            val ids = PreferencesManager.getFavorites(context)
            val repo = EspacioRepository(context)
            val data = mutableListOf<Pair<Int, String>>()
            for (id in ids) {
                try { data.add(id to repo.obtenerEspacio(id).nombre) }
                catch (_: Exception) { data.add(id to "Espacio $id") }
            }
            listaFavoritos = data
        }
    }
    LaunchedEffect(mostrarRecientes) {
        if (mostrarRecientes) {
            val ids = PreferencesManager.getRecents(context)
            val repo = EspacioRepository(context)
            val data = mutableListOf<Pair<Int, String>>()
            for (id in ids) {
                try { data.add(id to repo.obtenerEspacio(id).nombre) }
                catch (_: Exception) { data.add(id to "Espacio $id") }
            }
            listaRecientes = data
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header azul
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = Brush.verticalGradient(listOf(Blue900, Blue700)))
                .padding(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 24.dp)
        ) {
            Text("Perfil", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Tarjeta de usuario
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Invitado", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("Modo visitante", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Opciones principales
            item { ProfileOptionCard(Icons.Default.Star, "Favoritos", "Espacios que has guardado") { mostrarFavoritos = true } }
            item { ProfileOptionCard(Icons.Default.History, "Recientes", "Últimos espacios visitados") { mostrarRecientes = true } }
            item { ProfileOptionCard(Icons.Default.BugReport, "Reportar errores", "Ayúdanos a mejorar la app") { mostrarDialogoReporte = true } }

            // Tema oscuro con Switch
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tema oscuro", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                            Text("Activa el modo oscuro", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = isDarkTheme, onCheckedChange = { onToggleDarkTheme() })
                    }
                }
            }

            item { ProfileOptionCard(Icons.Default.Info, "Acerca de", "UbicaFII v2.8") { Toast.makeText(context, "Facultad de Ingeniería Industrial", Toast.LENGTH_SHORT).show() } }

            // Separador y acceso a Admin
            item { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant) }
            item {
                ProfileOptionCard(
                    icon = Icons.Default.AdminPanelSettings,
                    title = "Administración",
                    subtitle = "Acceso restringido",
                    iconColor = MaterialTheme.colorScheme.primary
                ) { mostrarDialogoAdmin = true }
            }
        }
    }

    // ==================== DIÁLOGOS DE INTERFAZ ====================

    if (mostrarFavoritos) {
        AlertDialog(
            onDismissRequest = { mostrarFavoritos = false },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = { Text("Favoritos", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
            text = {
                if (listaFavoritos.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No tienes espacios favoritos.", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 280.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(listaFavoritos, key = { it.first }) { item ->
                            TextButton(
                                onClick = { mostrarFavoritos = false; onNavigateToDetail(item.first) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = item.second,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Start
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { mostrarFavoritos = false }) { Text("Cerrar", color = MaterialTheme.colorScheme.primary) } }
        )
    }

    if (mostrarRecientes) {
        AlertDialog(
            onDismissRequest = { mostrarRecientes = false },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = { Text("Recientes", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
            text = {
                if (listaRecientes.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No hay espacios recientes.", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 280.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(listaRecientes, key = { it.first }) { item ->
                            TextButton(
                                onClick = { mostrarRecientes = false; onNavigateToDetail(item.first) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = item.second,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Start
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { mostrarRecientes = false }) { Text("Cerrar", color = MaterialTheme.colorScheme.primary) } }
        )
    }

    if (mostrarDialogoReporte) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoReporte = false; textoReporte = "" },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = { Text("Reportar un error", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
            text = {
                OutlinedTextField(
                    value = textoReporte,
                    onValueChange = { textoReporte = it },
                    placeholder = { Text("Describe el problema detalladamente...") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                )
            },
            confirmButton = {
                TextButton(
                    enabled = textoReporte.isNotBlank(),
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:")
                            putExtra(Intent.EXTRA_EMAIL, arrayOf("soporte_ubicafii@ejemplo.com"))
                            putExtra(Intent.EXTRA_SUBJECT, "Reporte de Error - UbicaFII")
                            putExtra(Intent.EXTRA_TEXT, textoReporte)
                        }
                        context.startActivity(Intent.createChooser(intent, "Enviar reporte vía..."))
                        mostrarDialogoReporte = false; textoReporte = ""
                    }
                ) { Text("Enviar", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { mostrarDialogoReporte = false; textoReporte = "" }) { Text("Cancelar", color = MaterialTheme.colorScheme.primary) } }
        )
    }

    if (mostrarDialogoAdmin) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoAdmin = false; pinIngresado = "" },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = { Text("Acceso de Administrador", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
            text = {
                OutlinedTextField(
                    value = pinIngresado,
                    onValueChange = { pinIngresado = it },
                    label = { Text("PIN de seguridad") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (pinIngresado == "7726") { mostrarDialogoAdmin = false; pinIngresado = ""; navigateToAdmin() }
                    else Toast.makeText(context, "PIN incorrecto", Toast.LENGTH_SHORT).show()
                }) { Text("Entrar", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { mostrarDialogoAdmin = false; pinIngresado = "" }) { Text("Cancelar", color = MaterialTheme.colorScheme.primary) } }
        )
    }
}

@Composable
fun ProfileOptionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        }
    }
}