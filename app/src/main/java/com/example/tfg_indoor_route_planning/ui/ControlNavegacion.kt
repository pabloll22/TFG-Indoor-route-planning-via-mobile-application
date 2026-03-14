package com.example.tfg_indoor_route_planning.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tfg_indoor_route_planning.models.POI

@Composable
fun BoxScope.ControlesNavegacion(
    hayRutaActiva: Boolean,
    modoNavegacionActiva: Boolean,
    esVistaPrevia: Boolean,
    poiParaConfirmar: POI?,
    poiDestinoActivo: POI?,
    distanciaMetros: Int?,
    onVolverClick: () -> Unit,
    onDetenerRutaClick: () -> Unit,
    onCerrarTarjetaClick: () -> Unit,
    onComoLlegarClick: (POI) -> Unit,
) {
    // -----------------------------------------------------------------
    // BOTÓN DE VOLVER A LA LISTA
    // -----------------------------------------------------------------
    FloatingActionButton(
        onClick = onVolverClick,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(
                end = 16.dp,
                // Si la tarjeta está visible, subimos el botón para que no lo tape
                bottom = if (poiParaConfirmar != null) 180.dp else 16.dp
            ),
        containerColor = Color(0xFF1E88E5)
    ) {
        Text("Volver", color = Color.White, modifier = Modifier.padding(horizontal = 16.dp))
    }

    // -----------------------------------------------------------------
    // NAVEGACIÓN ACTIVA
    // -----------------------------------------------------------------
    AnimatedVisibility(
        visible = hayRutaActiva && poiDestinoActivo != null,
        modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = poiDestinoActivo?.nombre ?: "Destino",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📍 A $distanciaMetros metros",
                        color = Color(0xFF1E88E5),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = onDetenerRutaClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (modoNavegacionActiva) Color(0xFFD32F2F) else Color.DarkGray
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = if (modoNavegacionActiva) Icons.Default.Close else Icons.Default.Delete,
                        contentDescription = "Detener",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (modoNavegacionActiva) "Detener" else "Limpiar",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
    // -----------------------------------------------------------------
    // TARJETA INFERIOR CON INFORMACIÓN
    // -----------------------------------------------------------------
    AnimatedVisibility(
        visible = poiParaConfirmar != null,
        modifier = Modifier.align(Alignment.BottomCenter),
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        poiParaConfirmar?.let { poi ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = poi.nombre,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(text = "Punto de interés", color = Color.Gray, fontSize = 14.sp)
                        }
                        IconButton(onClick = onCerrarTarjetaClick) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { onComoLlegarClick(poi) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        //Cambiamos icono y texto dependiendo de si es vista previa o ruta real
                        Icon(
                            imageVector = if (esVistaPrevia) Icons.Default.Visibility else Icons.Default.Directions,
                            contentDescription = if (esVistaPrevia) "Vista previa" else "Cómo llegar",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (esVistaPrevia) "Vista previa" else "Cómo llegar",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                      )
                    }
                }
            }
        }
    }
}