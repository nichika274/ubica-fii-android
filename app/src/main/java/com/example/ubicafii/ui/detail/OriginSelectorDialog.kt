package com.example.ubicafii.ui.detail

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ubicafii.ui.theme.BluePrimary
import com.example.ubicafii.ui.theme.MutedForeground
import com.example.ubicafii.util.GrafoNavegacion

// Mapeo de nodos a iconos sugeridos (puedes personalizarlo)
private val iconosPorNodo = mapOf(
    "entrada"      to Icons.Default.MeetingRoom,
    "cafeteria"    to Icons.Default.LocalCafe,
    "pasilloA"     to Icons.Default.ArrowForward,
    "pasilloB"     to Icons.Default.ArrowForward,
    "pasilloC"     to Icons.Default.ArrowForward,
    "escalerasAB"  to Icons.Default.Stairs,
    "escalerasBC"  to Icons.Default.Stairs,
    "bloqueG"      to Icons.Default.Business,
    "logo"         to Icons.Default.School
)

@Composable
fun OriginSelectorDialog(
    origenSeleccionado: String,
    onOrigenCambiado: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                "¿Desde dónde quieres ir?",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 400.dp)
            ) {
                items(GrafoNavegacion.nodos.filter { it.mostrarEnSelector }) { nodo ->
                    val selected = origenSeleccionado == nodo.id
                    val icono = iconosPorNodo[nodo.id] ?: Icons.Default.Place

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOrigenCambiado(nodo.id) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected)
                                BluePrimary.copy(alpha = 0.1f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = if (selected)
                            androidx.compose.foundation.BorderStroke(1.5.dp, BluePrimary)
                        else null
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icono,
                                contentDescription = null,
                                tint = if (selected) BluePrimary else MutedForeground,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = nodo.nombre,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) BluePrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = BluePrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}