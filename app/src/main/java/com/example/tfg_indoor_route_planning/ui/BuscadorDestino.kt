package com.example.tfg_indoor_route_planning.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.tfg_indoor_route_planning.models.POI

@Composable
fun BuscadorDestino(
    pois: List<POI>, // Recibimos la lista de destinos posibles
    onPoiSelected: (POI) -> Unit // Avisamos al MainActivity cuando elijan uno
) {
    var textoBuscador by remember { mutableStateOf("") }
    var buscadorActivo by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .zIndex(1f) // Esto asegura que flote por encima del mapa
    ) {
        // 1. LA BARRA DE BÚSQUEDA
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            TextField(
                value = textoBuscador,
                onValueChange = {
                    textoBuscador = it
                    buscadorActivo = it.isNotEmpty()
                },
                placeholder = { Text("Buscar destino", color = Color.Gray) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Color.Gray)
                },
                trailingIcon = {
                    if (textoBuscador.isNotEmpty()) {
                        IconButton(onClick = {
                            textoBuscador = ""
                            buscadorActivo = false
                            focusManager.clearFocus()
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Borrar")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )
        }

        // 2. LA LISTA DESPLEGABLE DE RESULTADOS
        if (buscadorActivo && textoBuscador.isNotEmpty()) {
            val poisFiltrados = pois.filter {
                it.nombre.contains(textoBuscador, ignoreCase = true)
            }

            if (poisFiltrados.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 250.dp)
                    ) {
                        items(poisFiltrados) { poi ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        textoBuscador = poi.nombre
                                        buscadorActivo = false
                                        onPoiSelected(poi) // ¡Avisamos al padre!
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(text = poi.nombre, color = Color.Black, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }
}