package com.example.tfg_indoor_route_planning.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalHotel
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tfg_indoor_route_planning.api.MapApiService

    @Composable
    fun PantallaListaFacultades(
        lista: List<MapApiService.MapaResumen>,
        onFacultadClick: (String) -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5)) // Un gris muy clarito de fondo
        ) {
            // --- CABECERA SUPERIOR ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1E88E5), // Azul tipo Google Maps
                shadowElevation = 4.dp
            ) {
                Text(
                    text = "Selecciona Facultad",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // --- LISTA DESLIZABLE (DINÁMICA) ---
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(lista) { mapaInfo ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFacultadClick(mapaInfo.mapaId) }, // Avisa al MainActivity del ID tocado
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val iconoDinamico = obtenerIconoFacultad(mapaInfo.nombre)

                            Icon(
                                imageVector = iconoDinamico,
                                contentDescription = "Icono Edificio",
                                tint = Color(0xFF1E88E5), // Lo he puesto azul para que combine con la cabecera, pero puedes dejarlo Color.Gray
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))

                            // Textos (Nombre e ID de Mongo)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mapaInfo.nombre,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                            }

                            // Flechita de navegación a la derecha
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Entrar al mapa",
                                tint = Color(0xFF1E88E5)
                            )
                        }
                    }
                }
            }
        }
    }

fun obtenerIconoFacultad(nombre: String): ImageVector {
    val nombreMinusculas = nombre.lowercase()

    return when {
        nombreMinusculas.contains("etsii") || nombreMinusculas.contains("computadores") -> Icons.Default.Computer
        nombreMinusculas.contains("casa") -> Icons.Default.Home
        nombreMinusculas.contains("telecomunicaci") -> Icons.Default.CellTower
        nombreMinusculas.contains("medicina") || nombreMinusculas.contains("salud") -> Icons.Default.LocalHospital
        nombreMinusculas.contains("ciencia") || nombreMinusculas.contains("química") -> Icons.Default.Science
        nombreMinusculas.contains("derecho") -> Icons.Default.AccountBalance // Parecido a un juzgado/institución
        nombreMinusculas.contains("letras") || nombreMinusculas.contains("educación") -> Icons.Default.MenuBook
        nombreMinusculas.contains("ingeniería") || nombreMinusculas.contains("industrial") -> Icons.Default.Engineering
        else -> Icons.Default.Business // Icono de edificio de oficinas por defecto
    }
}