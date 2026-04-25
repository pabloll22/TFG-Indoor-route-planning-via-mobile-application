package com.example.tfg_indoor_route_planning.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.ui.text.style.TextOverflow
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
    modoSimulacionActiva: Boolean,
    pasoActual: Int,
    totalPasos: Int,
    onSimularClick: (POI) -> Unit,
    onAvanzarPaso: () -> Unit,
    onRetrocederPaso: () -> Unit,
    onDetenerRutaClick: () -> Unit,
    onCerrarTarjetaClick: () -> Unit,
    onComoLlegarClick: (POI) -> Unit,
    onIniciarRutaClick: (POI) -> Unit,
    listaFavoritosIds: Set<String>,
    toggleFavorito: (String) -> Unit,
    origenEsUbicacionUsuario: Boolean
) {
    val textoTiempo = if (distanciaMetros != null && distanciaMetros > 0) {
        " " + calcularTiempoEstimado(distanciaMetros)
    } else {
        ""
    }

    // -----------------------------------------------------------------
    // NAVEGACIÓN ACTIVA
    // -----------------------------------------------------------------
    AnimatedVisibility(
        visible = (hayRutaActiva || modoSimulacionActiva) && poiDestinoActivo != null,
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
                // INFO DE LA RUTA (Textos e Iconos)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = poiDestinoActivo?.nombre ?: "Destino",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, "Distancia", tint = Color(0xFF1E88E5), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("$distanciaMetros metros", color = Color(0xFF1E88E5), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, "Tiempo estimado", tint = Color(0xFF1E88E5), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(textoTiempo, color = Color(0xFF1E88E5), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // ZONA DE BOTONES (FLECHAS O INICIAR/SIMULAR)
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp) //
                ) {

                    // MODO SIMULACIÓN -> Flechas
                    if (modoSimulacionActiva) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onRetrocederPaso, enabled = pasoActual > 0) {
                                Icon(Icons.Default.ArrowBackIosNew, "Anterior", tint = if (pasoActual > 0) Color(0xFF1E88E5) else Color.LightGray)
                            }
                            Text(
                                text = "${pasoActual + 1} / ${if (totalPasos > 0) totalPasos else 1}",
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            IconButton(onClick = onAvanzarPaso, enabled = pasoActual < totalPasos - 1) {
                                Icon(Icons.Default.ArrowForwardIos, "Siguiente", tint = if (pasoActual < totalPasos - 1) Color(0xFF1E88E5) else Color.LightGray)
                            }
                        }
                    } else if (!modoNavegacionActiva && !esVistaPrevia) {
                        Button(
                            onClick = {
                                if (origenEsUbicacionUsuario) onIniciarRutaClick(poiDestinoActivo!!)
                                else onSimularClick(poiDestinoActivo!!)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (origenEsUbicacionUsuario) Color(0xFF4CAF50) else Color(0xFF1E88E5)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = if (origenEsUbicacionUsuario) Icons.Default.PlayArrow else Icons.Default.FastForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (origenEsUbicacionUsuario) "Iniciar" else "Simular",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // BOTÓN DETENER
                    Button(
                        onClick = onDetenerRutaClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (modoNavegacionActiva || modoSimulacionActiva) Color(0xFFD32F2F) else Color.DarkGray
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = if (modoNavegacionActiva || modoSimulacionActiva) Icons.Default.Close else Icons.Default.Delete,
                            contentDescription = "Detener",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        if (modoNavegacionActiva || modoSimulacionActiva || esVistaPrevia) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (modoNavegacionActiva || modoSimulacionActiva) "Detener" else "Limpiar",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
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

                    val esFavorito = listaFavoritosIds.contains(poi.id)

                    IconButton(onClick = { toggleFavorito(poi.id) }) {
                        Icon(
                            imageVector = if (esFavorito) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Guardar",
                            tint = if (esFavorito) Color(0xFF4CAF50) else Color.Gray
                        )
                    }

                    IconButton(onClick = onCerrarTarjetaClick) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 2. BOTÓN PRINCIPAL (Cómo llegar / Vista previa)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // BOTÓN 1: CÓMO LLEGAR
                    Button(
                        onClick = { onComoLlegarClick(poi) },
                        modifier = Modifier.weight(1.6f).fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = if (esVistaPrevia) Icons.Default.Visibility else Icons.Default.Directions,
                            contentDescription = null,
                            tint = Color.DarkGray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Cómo llegar",
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    // BOTÓN 2: INICIAR / SIMULAR (En el ModalBottomSheet)
                    Button(
                        onClick = {
                            if (origenEsUbicacionUsuario) onIniciarRutaClick(poi)
                            else onSimularClick(poi) // Lanzamos simulación
                        },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (origenEsUbicacionUsuario) Color(0xFF4CAF50) else Color(0xFF1E88E5)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = if (origenEsUbicacionUsuario) Icons.Default.PlayArrow else Icons.Default.FastForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (origenEsUbicacionUsuario) "Iniciar" else "Simular",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
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

fun calcularTiempoEstimado(distanciaMetros: Int?): String {
    if (distanciaMetros != null) {
        if (distanciaMetros <= 0) return "0 seg"
    }

    val velocidadMetrosPorSegundo = 1.4f
    val tiempoTotalSegundos = (distanciaMetros?.div(velocidadMetrosPorSegundo))?.toInt()

    val minutos = tiempoTotalSegundos?.div(60)
    val segundosRestantes = tiempoTotalSegundos?.rem(60)

    if (segundosRestantes != null) {
        return when {
            // Si es menos de un minuto, enseñamos solo los segundos
            minutos == 0 -> "aprox. $segundosRestantes seg"
            // Si los segundos son muy poquitos, redondeamos a minutos
            segundosRestantes < 10 -> "aprox. $minutos min"
            // Formato completo para distancias medias
            else -> "aprox. $minutos min y $segundosRestantes seg"
        }
    }else{
        return ""
    }
}

@Composable
fun NavigationItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = color,
            style = MaterialTheme.typography.labelMedium
        )
    }
}