package com.example.ubicafii.ui.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ubicafii.data.model.Espacio
import com.example.ubicafii.ui.components.getTypeIcon
import com.example.ubicafii.ui.theme.BluePrimary
import com.example.ubicafii.ui.theme.MutedForeground
import com.example.ubicafii.util.getFloorLabel

@Composable
fun CampusBottomCard(
    espacio: Espacio,
    origenActual: String,
    onClickCambiarOrigen: () -> Unit,
    onClickAyuda: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Fila superior con nombre y botón de ayuda
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    getTypeIcon(espacio.tipo),
                    contentDescription = null,
                    tint = BluePrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        espacio.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary
                    )
                    Text(
                        "${getFloorLabel(espacio.piso.toString())} · Bloque ${espacio.bloque}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedForeground
                    )
                }
                IconButton(
                    onClick = onClickAyuda,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.HelpOutline,
                        contentDescription = "Ayuda",
                        tint = MutedForeground,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Selector de origen
            Text(
                "Ruta desde",
                style = MaterialTheme.typography.labelMedium,
                color = MutedForeground,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            OutlinedButton(
                onClick = onClickCambiarOrigen,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BluePrimary)
            ) {
                Icon(Icons.Default.NearMe, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    origenActual,
                    color = BluePrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = BluePrimary)
            }
        }
    }
}