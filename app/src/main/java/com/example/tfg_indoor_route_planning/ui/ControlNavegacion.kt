package com.example.tfg_indoor_route_planning.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tfg_indoor_route_planning.models.POI

@OptIn(ExperimentalMaterial3Api::class)
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
    if (poiParaConfirmar != null) {
        // IMPORTANTE: Ponemos skipPartiallyExpanded = true para que la tarjeta
        // ocupe exactamente el tamaño de su contenido y no se quede atascada a la mitad.
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = onCerrarTarjetaClick,
            sheetState = sheetState,
            containerColor = Color.White,
            scrimColor = Color.Transparent,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            val poi = poiParaConfirmar!!

            // VARIABLE QUE CONTROLA EL DESPLEGABLE INTERNO
            var mostrarInfoExtra by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp)
                    // Esto hace que cuando pulsemos "Ver más", la tarjeta crezca con una animación suave
                    .animateContentSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. CABECERA (Título y botón cerrar)
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

                // 2. BOTÓN PRINCIPAL (Cómo llegar / Vista previa)
                Button(
                    onClick = { onComoLlegarClick(poi) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                    shape = RoundedCornerShape(12.dp)
                ) {
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

                Spacer(modifier = Modifier.height(8.dp))

                // 3. BOTÓN DE "VER MÁS / VER MENOS"
                TextButton(
                    onClick = { mostrarInfoExtra = !mostrarInfoExtra },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = if (mostrarInfoExtra) "Ocultar información" else "Ver información del lugar",
                        color = Color(0xFF1E88E5),
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = if (mostrarInfoExtra) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = Color(0xFF1E88E5)
                    )
                }

                // 4. LA INFORMACIÓN EXTRA OCULTA (Solo aparece si pulsar el botón anterior)
                AnimatedVisibility(visible = mostrarInfoExtra) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Información",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Aquí irá la descripción detallada sobre ${poi.nombre}. En el futuro, podrás añadir horarios de apertura, el aforo actual, o si hay profesores en este despacho.",
                            fontSize = 15.sp,
                            color = Color.Gray,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("📍 Planta: ${poi.plantaId}", color = Color.Gray, fontSize = 14.sp)
                        Text("🔢 Nodo asociado: ${poi.nodoId}", color = Color.Gray, fontSize = 14.sp)

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}