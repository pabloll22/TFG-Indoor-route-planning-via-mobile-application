package com.example.tfg_indoor_route_planning.ui

import android.app.Activity
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tfg_indoor_route_planning.api.RetrofitClient
import com.example.tfg_indoor_route_planning.horario.SesionRespuesta
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
    origenEsUbicacionUsuario: Boolean,
    modoSeleccionAula: Boolean,
    usuarioId: String = UserSession.usuarioId,
    esProfesor: Boolean = UserSession.esProfesor
) {
    val textoTiempo = if (distanciaMetros != null && distanciaMetros > 0) {
        " " + calcularTiempoEstimado(distanciaMetros)
    } else {
        ""
    }

    var misClasesIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    LaunchedEffect(usuarioId) {
        if (!UserSession.esInvitado) {
            try {
                // Descargamos el horario (Alumno o Profesor)
                val miHorario = RetrofitClient.apiService.getHorario(usuarioId)

                // Extraemos solo los IDs y los guardamos en el Set para buscar rápido
                misClasesIds = miHorario.map { it._id }.toSet()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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
                // INFO DE LA RUTA
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

                // ZONA DE BOTONES
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, "Siguiente", tint = if (pasoActual < totalPasos - 1) Color(0xFF1E88E5) else Color.LightGray)
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
    // TARJETA INFERIOR CON INFORMACIÓN O SELECCIÓN DE AULA
    // -----------------------------------------------------------------
    if (poiParaConfirmar != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = onCerrarTarjetaClick,
            sheetState = sheetState,
            containerColor = Color.White,
            scrimColor = Color.Transparent,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            val poi = poiParaConfirmar!!
            val esAulaOLab = poi.nombre.contains("Aula", ignoreCase = true) ||
                    poi.nombre.contains("Lab", ignoreCase = true)

            var mostrarInfoExtra by remember { mutableStateOf(false) }
            var clasesDelAula by remember { mutableStateOf<List<SesionRespuesta>>(emptyList()) }
            var cargandoClases by remember { mutableStateOf(false) }

            // Solo cargamos el horario si estamos en la interfaz normal (no en selección) y expandimos la info
            LaunchedEffect(mostrarInfoExtra, poi.nodoId) {
                if (!modoSeleccionAula && mostrarInfoExtra && clasesDelAula.isEmpty() && esAulaOLab) {
                    cargandoClases = true
                    try {
                        val todasLasClases = RetrofitClient.apiService.getHorarioAulaHoy(poi.nodoId)
                        val mesActual = java.time.LocalDate.now().monthValue
                        val cuatrimestreActual = when (mesActual) {
                            9, 10, 11, 12, 1 -> 1
                            2, 3, 4, 5, 6, 7 -> 2
                            else -> 0
                        }
                        clasesDelAula = todasLasClases.filter { clase ->
                            clase.asignaturaId.cuatrimestre == cuatrimestreActual
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        cargandoClases = false
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp)
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

                    // Ocultamos el botón de favoritos si solo venimos a seleccionar un aula
                    if (!modoSeleccionAula) {
                        val esFavorito = listaFavoritosIds.contains(poi.id)
                        IconButton(onClick = { toggleFavorito(poi.id) }) {
                            Icon(
                                imageVector = if (esFavorito) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Guardar",
                                tint = if (esFavorito) Color(0xFF4CAF50) else Color.Gray
                            )
                        }
                    }

                    IconButton(onClick = onCerrarTarjetaClick) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // =========================================================
                // BIFURCACIÓN DE INTERFAZ SEGÚN EL MODO
                // =========================================================
                if (modoSeleccionAula) {

                    // MODO 1: SELECCIÓN DE AULA (Interfaz minimalista)
                    if (esAulaOLab) {
                        val context = LocalContext.current as Activity

                        Button(
                            onClick = {
                                val intentRespuesta = Intent().apply {
                                    putExtra("NODO_ID", poi.nodoId)
                                    putExtra("AULA_NOMBRE", poi.nombre)
                                }
                                context.setResult(Activity.RESULT_OK, intentRespuesta)
                                context.finish() // Cierra el mapa y vuelve al horario
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("✅ Usar ${poi.nombre} para la clase", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Aviso para evitar que programen una clase en la cafetería
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "⚠️ Solo puedes seleccionar Aulas o Laboratorios para programar una clase.",
                                color = Color(0xFFD32F2F),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                } else {

                    // MODO 2: INTERFAZ DE NAVEGACIÓN NORMAL (Botones ruta, ver más, etc)

                    Row(
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
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
                            Text("Cómo llegar", color = Color.DarkGray, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Button(
                            onClick = {
                                if (origenEsUbicacionUsuario) onIniciarRutaClick(poi)
                                else onSimularClick(poi)
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
                            Text(if (origenEsUbicacionUsuario) "Iniciar" else "Simular", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

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

                    AnimatedVisibility(visible = mostrarInfoExtra) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(16.dp))

                            // UBICACIÓN (Siempre visible para todos los lugares)
                            /*InfoCard(titulo = "Ubicación técnica", texto = "Planta: ${poi.plantaId}   |   Nodo: ${poi.nodoId}", icono = Icons.Default.Place)
                            Spacer(modifier = Modifier.height(8.dp))*/

                            // BIFURCACIÓN SEGÚN EL TIPO DE LUGAR
                            when (poi.tipo?.uppercase()) {
                                "SECRETARIA" -> {
                                    InfoCard(titulo = "Horario de Atención", texto = poi.horario ?: "Consultar en el centro", icono = Icons.Default.Schedule)
                                    InfoCard(titulo = "Contacto", texto = poi.telefono ?: "No disponible", icono = Icons.Default.Phone)
                                    if (poi.enlaceExtra != null) BotonEnlace(texto = "Pedir Cita Previa", url = poi.enlaceExtra)
                                }
                                "CAFETERIA" -> {
                                    InfoCard(titulo = "Horario", texto = poi.horario ?: "Consultar en el centro", icono = Icons.Default.Schedule)
                                    if (poi.enlaceExtra != null) BotonEnlace(texto = "Ver Menú del Día", url = poi.enlaceExtra)
                                }
                                "BIBLIOTECA" -> {
                                    InfoCard(titulo = "Horario", texto = poi.horario ?: "No disponible", icono = Icons.Default.Schedule)
                                    if (poi.capacidad != null) InfoCard(titulo = "Capacidad", texto = "${poi.capacidad} puestos", icono = Icons.Default.Info)
                                    if (poi.enlaceExtra != null) BotonEnlace(texto = "Biblioteca Electrónica", url = poi.enlaceExtra)
                                }
                                "ASEO" -> {
                                    if (poi.esAccesible == true) {
                                        InfoCard(titulo = "Accesibilidad", texto = "Baño adaptado para movilidad reducida", icono = Icons.Default.Info)
                                    }
                                }
                                "CONSERJERIA" -> {
                                    InfoCard(titulo = "Atención al alumno", texto = "Objetos perdidos y gestión", icono = Icons.Default.Info)
                                    if (poi.telefono != null) InfoCard(titulo = "Contacto", texto = poi.telefono, icono = Icons.Default.Phone)
                                }
                                "SALON_ACTOS" -> {
                                    if (poi.capacidad != null) InfoCard(titulo = "Aforo máximo", texto = "${poi.capacidad} butacas", icono = Icons.Default.Info)
                                }
                                else -> {
                                    // Si es AULA, LABORATORIO, o no tiene tipo guardado pero la palabra lo indica
                                    if (esAulaOLab) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Ocupación de hoy", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                        Spacer(modifier = Modifier.height(12.dp))

                                        if (cargandoClases) {
                                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                                CircularProgressIndicator(color = Color(0xFF1E88E5), modifier = Modifier.size(30.dp))
                                            }
                                        } else if (clasesDelAula.isEmpty()) {
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = "No hay clases programadas para hoy en este lugar.",
                                                    color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(16.dp)
                                                )
                                            }
                                        } else {
                                            clasesDelAula.forEach { clase ->
                                                MiniClaseAulaItem(
                                                    clase = clase,
                                                    esMia = misClasesIds.contains(clase._id)
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoCard(titulo: String, texto: String, icono: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icono, contentDescription = null, tint = Color(0xFF1E88E5), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = titulo, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.DarkGray)
            Text(text = texto, fontSize = 14.sp, color = Color.Black)
        }
    }
}

@Composable
fun BotonEnlace(texto: String, url: String) {
    val context = LocalContext.current
    OutlinedButton(
        onClick = {
            // Esto lanza el navegador del teléfono
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
            context.startActivity(intent)
        },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E88E5))
    ) {
        // Usamos Icono de información como base para no pedir librerías externas
        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(texto)
    }
}

@Composable
fun MiniClaseAulaItem(clase: SesionRespuesta, esMia: Boolean = false) {
    val colorHex = com.example.tfg_indoor_route_planning.horario.getColorForAsignatura(clase.asignaturaId.nombre)
    val colorBase = Color(android.graphics.Color.parseColor(colorHex))
    val hoyStr = remember {
        java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }

    val estaCancelada = clase.fechasCanceladas?.contains(hoyStr) == true

    val colorFondo = when {
        estaCancelada -> Color(0xFFF5F5F5) // Gris muy clarito si está cancelada
        esMia -> Color(0xFFFFF8E1)
        else -> Color.White
    }
    val bordeResalte = if (esMia && !estaCancelada) BorderStroke(1.dp, Color(0xFFFFB300)) else null

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = if (esMia) 4.dp else 2.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        border = bordeResalte,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(105.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .fillMaxHeight()
                    .background(colorBase)
            )

            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "${clase.horaInicio} - ${clase.horaFin}", color = colorBase, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                        if (estaCancelada) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFD32F2F), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("CANCELADA", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                        // ETIQUETA "TU CLASE"
                        else if (esMia) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFFFB300), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("TU CLASE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                    Text(text = "Grupo ${clase.grupo}", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = clase.asignaturaId.nombre,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = Color.Black,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Prof: ${clase.profesorId.nombre}",
                    color = Color.DarkGray,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
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
            minutos == 0 -> "aprox. $segundosRestantes seg"
            segundosRestantes < 10 -> "aprox. $minutos min"
            else -> "aprox. $minutos min y $segundosRestantes seg"
        }
    } else {
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