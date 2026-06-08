package com.example.tfg_indoor_route_planning.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tfg_indoor_route_planning.api.dto.MapaResumen
import com.example.tfg_indoor_route_planning.api.services.MapApiService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaListaFacultades(
    lista: List<MapaResumen>,
    onFacultadClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // CABECERA SUPERIOR
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color(0xFF1E88E5),
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                )
                .padding(start = 8.dp, end = 24.dp, top = 40.dp, bottom = 32.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. BOTÓN DE VOLVER
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver al Dashboard",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // 2. TEXTOS DE LA CABECERA
                Column {
                    Text(
                        text = "Campus UMA",
                        color = Color(0xFFBBDEFB),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Selecciona tu Facultad",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- LISTA DESLIZABLE ---
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(lista) { mapaInfo ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onFacultadClick(mapaInfo.mapaId) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. CONTENEDOR DEL ICONO
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE3F2FD)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = obtenerIconoFacultad(mapaInfo.nombre),
                                contentDescription = "Icono Edificio",
                                tint = Color(0xFF1E88E5),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // 2. TEXTOS PRINCIPALES
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = mapaInfo.nombre,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF212121)
                            )
                        }

                        // 3. BOTÓN / FLECHITA DE ACCIÓN
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF5F5F5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Entrar al mapa",
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
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
        nombreMinusculas.contains("derecho") -> Icons.Default.AccountBalance
        nombreMinusculas.contains("letras") || nombreMinusculas.contains("educación") -> Icons.Default.MenuBook
        nombreMinusculas.contains("ingeniería") || nombreMinusculas.contains("industrial") -> Icons.Default.Engineering
        else -> Icons.Default.Business
    }
}