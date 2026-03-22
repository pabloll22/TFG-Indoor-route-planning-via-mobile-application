package com.example.tfg_indoor_route_planning.ui


import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    plantaParpadeandoId: String?,
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

                // Solo parpadea si es la planta destino Y NO estamos ya en ella
                val debeParpadear = (planta.plantaId == plantaParpadeandoId) && !estaSeleccionada

                // Texto de la planta
                val textoPlanta = when {
                    planta.nivel > 0 -> "L${planta.nivel}"
                    planta.nivel < 0 -> "B${-planta.nivel}"
                    else -> "0"
                }

                val infiniteTransition = rememberInfiniteTransition(label = "animacion_latido_suave")

                // Definimos los colores para el estado "latido"
                val colorLatidoBase = Color(0xFFEEEEEE) // Gris muy clarito
                val colorLatidoResaltado = Color(0xFFFFCC80) // Naranja pálido (menos saturado)

                // Animamos el color de fondo con una curva orgánica (FastOutSlowInEasing) y ritmo lento
                val colorFondoAnimado by infiniteTransition.animateColor(
                    initialValue = if (estaSeleccionada) Color(0xFF1E88E5) else colorLatidoBase,
                    targetValue = when {
                        estaSeleccionada -> Color(0xFF1E88E5) // Azul fijo si está seleccionada
                        debeParpadear -> colorLatidoResaltado // Oscila hacia el naranja pálido
                        else -> colorLatidoBase // Si no, gris suave fijo
                    },
                    animationSpec = infiniteRepeatable(
                        // 1000ms (1 segundo) por latido, curva orgánica y modo Reverse
                        animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "colorFondo"
                )

                // Animamos el color del texto de forma sutil
                val colorTextoAnimado by infiniteTransition.animateColor(
                    initialValue = if (estaSeleccionada) Color.White else Color.DarkGray,
                    targetValue = when {
                        estaSeleccionada -> Color.White // Blanco fijo
                        debeParpadear -> Color.Black // Negro si parpadea para que contraste
                        else -> Color.DarkGray // Gris oscuro por defecto
                    },
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "colorTexto"
                )

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = colorFondoAnimado, // Usamos la variable animada
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onPlantaSeleccionada(planta.plantaId) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = textoPlanta,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorTextoAnimado // Usamos el texto animado
                    )
                }
            }
        }
    }
}