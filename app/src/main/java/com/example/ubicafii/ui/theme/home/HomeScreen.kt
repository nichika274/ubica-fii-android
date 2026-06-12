package com.example.ubicafii.ui.theme.home

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ubicafii.data.model.Espacio

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val context = LocalContext.current
    val listaEspacios by viewModel.espacios.collectAsState()
    var textoBusqueda by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.cargarEspacios()
    }

    LaunchedEffect(listaEspacios) {
        if (listaEspacios.isNotEmpty()) {
            Toast.makeText(context, "Cargados ${listaEspacios.size} espacios", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "¡Bienvenido a UbicaFII!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = textoBusqueda,
            onValueChange = { textoBusqueda = it },
            label = { Text("Buscar espacios...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        Text(
            text = "Explorar por bloques",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Resultados:",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listaEspacios) { espacio ->
                CardEspacio(espacio = espacio)
            }
        }
    }
}

@Composable
fun CardEspacio(espacio: Espacio) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = espacio.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = "${espacio.tipo.replaceFirstChar { it.uppercase() }} • ${espacio.bloque}", fontSize = 14.sp, color = Color.Gray)
            Text(text = "Indicación: ${espacio.indicaciones}", fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
        }
    }
}