package com.example.tfg_indoor_route_planning.ui


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tfg_indoor_route_planning.models.Planta


@Composable
fun BoxScope.SelectorDePlantas(
    plantas: List<Planta>,
    plantaActivaId: String?,
    onPlantaSeleccionada: (String) -> Unit
) {
    // Ordenamos de mayor a menor nivel
    val plantasOrdenadas = plantas.sortedByDescending { it.nivel }

    Card(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .padding(end = 16.dp)
            .width(50.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp), // Bordes bien redondeados, coherentes con tu UI
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(8.dp), // Un poco de aire por dentro
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp) // Pequeña separación entre botones
        ) {
            plantasOrdenadas.forEach { planta ->
                val estaSeleccionada = (planta.plantaId == plantaActivaId)

                // Texto de la planta
                val textoPlanta = when {
                    planta.nivel > 0 -> "L${planta.nivel}"
                    planta.nivel < 0 -> "B${-planta.nivel}"
                    else -> "0"
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        // Si está seleccionada, fondo azul con bordes redondeados. Si no, transparente.
                        .background(
                            color = if (estaSeleccionada) Color(0xFF1E88E5) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onPlantaSeleccionada(planta.plantaId) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = textoPlanta,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        // Texto blanco si está seleccionado, gris oscuro si no
                        color = if (estaSeleccionada) Color.White else Color.DarkGray
                    )
                }
            }
        }
    }
}