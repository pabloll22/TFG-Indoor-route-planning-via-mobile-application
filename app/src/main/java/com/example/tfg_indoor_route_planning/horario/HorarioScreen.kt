package com.example.tfg_indoor_route_planning.horario

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tfg_indoor_route_planning.MainActivity
import com.example.tfg_indoor_route_planning.api.RetrofitClient
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorarioScreen(
    usuarioId: String = UserSession.usuarioId,
    esProfesor: Boolean = UserSession.esProfesor,
    onVolver: () -> Unit,
    onVerEnMapa: (String, String) -> Unit
) {
    var horarioCompleto by remember { mutableStateOf<List<SesionRespuesta>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    // LocalDate para manejar fechas reales
    var fechaSeleccionada by remember { mutableStateOf(LocalDate.now()) }

    val formateadorFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val fechaSeleccionadaTexto = fechaSeleccionada.format(formateadorFecha)

    LaunchedEffect(usuarioId, esProfesor) {
        cargarHorario(usuarioId, esProfesor) { horario ->
            horarioCompleto = horario
            cargando = false
        }
    }

    // Calculamos el Lunes de la semana seleccionada para pintar los 5 días
    val inicioDeSemana = fechaSeleccionada.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val diasDeLaSemana = (0..4).map { inicioDeSemana.plusDays(it.toLong()) } // Lunes a Viernes

    // Formateador para el título
    val mesAñoFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale("es", "ES"))
    var mostrarFormularioCrear by remember { mutableStateOf(false) }

    var aulaSeleccionadaNodo by rememberSaveable { mutableStateOf("") }
    var aulaSeleccionadaNombre by rememberSaveable { mutableStateOf("") }

    val context = LocalContext.current

    val lanzadorMapa = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        if (resultado.resultCode == Activity.RESULT_OK) {
            val datos = resultado.data
            aulaSeleccionadaNodo = datos?.getStringExtra("NODO_ID") ?: ""
            aulaSeleccionadaNombre = datos?.getStringExtra("AULA_NOMBRE") ?: ""
            mostrarFormularioCrear = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Horario", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            if (esProfesor) {
                FloatingActionButton(
                    onClick = { mostrarFormularioCrear = true },
                    containerColor = Color(0xFF6200EE),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Crear clase")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(paddingValues)
        ) {

            // CABECERA DEL CALENDARIO
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { fechaSeleccionada = fechaSeleccionada.minusWeeks(1) }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Semana anterior")
                }

                Text(
                    text = fechaSeleccionada.format(mesAñoFormatter).replaceFirstChar { it.uppercase() },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                IconButton(onClick = { fechaSeleccionada = fechaSeleccionada.plusWeeks(1) }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Semana siguiente")
                }
            }

            // SELECTOR DE DÍAS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val nombresDias = listOf("L", "M", "X", "J", "V")

                diasDeLaSemana.forEachIndexed { index, fecha ->
                    val esSeleccionado = fecha == fechaSeleccionada
                    val esHoy = fecha == LocalDate.now()

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { fechaSeleccionada = fecha }
                            .padding(8.dp)
                    ) {
                        Text(
                            text = nombresDias[index],
                            fontWeight = FontWeight.Normal,
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        esSeleccionado -> Color(0xFF6200EE)
                                        esHoy -> Color(0xFFE0E0E0)
                                        else -> Color.Transparent
                                    }
                                )
                        ) {
                            Text(
                                text = fecha.dayOfMonth.toString(),
                                fontWeight = if (esSeleccionado || esHoy) FontWeight.Bold else FontWeight.Normal,
                                color = if (esSeleccionado) Color.White else Color.Black,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // LÍNEA DE TIEMPO
            if (cargando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                // (Ya no declaramos la fecha aquí porque la hemos subido arriba)
                val diaSemanaSeleccionadoInt = fechaSeleccionada.dayOfWeek.value
                val mesActual = fechaSeleccionada.monthValue
                val cuatrimestreActual = when (mesActual) {
                    9, 10, 11, 12, 1 -> 1
                    2, 3, 4, 5, 6, 7 -> 2
                    else -> 0
                }

                val clasesDelDia = horarioCompleto.filter { clase ->
                    if (clase.fechaEspecifica != null) {
                        clase.fechaEspecifica == fechaSeleccionadaTexto
                    } else {
                        val esElDiaCorrecto = clase.diaSemana == diaSemanaSeleccionadoInt
                        val esDelCuatrimestre = clase.asignaturaId.cuatrimestre == cuatrimestreActual
                        esElDiaCorrecto && esDelCuatrimestre
                    }
                }

                if (cuatrimestreActual == 0) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("¡Vacaciones de verano! 🏖️", color = Color.Gray, fontSize = 18.sp)
                    }
                } else if (diaSemanaSeleccionadoInt > 5) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("¡Es fin de semana! A descansar 🛋️", color = Color.Gray)
                    }
                } else if (clasesDelDia.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No tienes clases este día 🎉", color = Color.Gray)
                    }
                } else {
                    val gruposSolapados = agruparClasesSolapadas(clasesDelDia)

                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(gruposSolapados) { grupo ->
                            val minInicioMins = grupo.minOf { timeToMinutes(it.horaInicio) }
                            val dpPorMinuto = 1.3f

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                grupo.forEach { clase ->
                                    val inicioMins = timeToMinutes(clase.horaInicio)
                                    val finMins = timeToMinutes(clase.horaFin)

                                    val duracionMins = finMins - inicioMins
                                    val empujeHaciaAbajoMins = inicioMins - minInicioMins

                                    val modificadorAltura = if (grupo.size == 1) {
                                        Modifier.height(105.dp)
                                    } else {
                                        Modifier.height((duracionMins * dpPorMinuto).dp)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(top = (empujeHaciaAbajoMins * dpPorMinuto).dp)
                                            .then(modificadorAltura)
                                    ) {
                                        ClaseCard(
                                            clase = clase,
                                            esProfesor = esProfesor,
                                            fechaActualTexto = fechaSeleccionadaTexto,
                                            estaSolapada = grupo.size > 1,
                                            onVerEnMapa = onVerEnMapa,
                                            onClaseCancelada = { idClase ->
                                                horarioCompleto = horarioCompleto.map {
                                                    if (it._id == idClase) {
                                                        val nuevasFechas = (it.fechasCanceladas ?: emptyList()) + fechaSeleccionadaTexto
                                                        it.copy(fechasCanceladas = nuevasFechas)
                                                    } else it
                                                }
                                            },
                                            onClaseRestaurada = { idClase ->
                                                horarioCompleto = horarioCompleto.map {
                                                    if (it._id == idClase) {
                                                        val nuevasFechas = (it.fechasCanceladas ?: emptyList()) - fechaSeleccionadaTexto
                                                        it.copy(fechasCanceladas = nuevasFechas)
                                                    } else it
                                                }
                                            },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // FORMULARIO DE CREACIÓN
    if (mostrarFormularioCrear) {
        val asignaturasDelProfesor = horarioCompleto.map { it.asignaturaId }.distinctBy { it._id }
        val idMongoProfesor = horarioCompleto.firstOrNull()?.profesorId?._id ?: ""

        DialogoCrearClase(
            asignaturas = asignaturasDelProfesor,
            profesorIdMongo = idMongoProfesor,
            aulaNombre = aulaSeleccionadaNombre,
            nodoAulaId = aulaSeleccionadaNodo,
            fechaActual = fechaSeleccionadaTexto,
            onSeleccionarMapa = { idFacultad ->

                val intent = Intent(context, MainActivity::class.java).apply {
                    putExtra("MODO_SELECCION_AULA", true)
                    putExtra("FACULTAD_ID", idFacultad)
                }
                lanzadorMapa.launch(intent)
            },
            onDismiss = { mostrarFormularioCrear = false },
            onClaseCreada = { nuevaClase ->
                horarioCompleto = horarioCompleto + nuevaClase
                mostrarFormularioCrear = false
                aulaSeleccionadaNodo = ""
                aulaSeleccionadaNombre = ""
            }
        )
    }
}

suspend fun cargarHorario(usuarioId: String, esProfesor: Boolean, onResult: (List<SesionRespuesta>) -> Unit) {
    try {
        val horario = /*if (esProfesor) {
            RetrofitClient.apiService.getHorarioProfesor(usuarioId)
        } else {
            RetrofitClient.apiService.getHorarioAlumno(usuarioId)
        }*/ RetrofitClient.apiService.getHorario(usuarioId)

        onResult(horario)
    } catch (e: Exception) {
        Log.e("HORARIO", "Error: ${e.message}")
        onResult(emptyList())
    }
}

fun timeToMinutes(time: String): Int {
    val parts = time.split(":")
    return parts[0].toInt() * 60 + parts[1].toInt()
}

fun agruparClasesSolapadas(clases: List<SesionRespuesta>): List<List<SesionRespuesta>> {
    if (clases.isEmpty()) return emptyList()

    val ordenadas = clases.sortedBy { timeToMinutes(it.horaInicio) }
    val grupos = mutableListOf<MutableList<SesionRespuesta>>()

    var grupoActual = mutableListOf(ordenadas[0])
    var finMaximoGrupo = timeToMinutes(ordenadas[0].horaFin)

    for (i in 1 until ordenadas.size) {
        val clase = ordenadas[i]
        val inicio = timeToMinutes(clase.horaInicio)

        // Si la clase empieza ANTES de que termine el grupo actual, se solapan
        if (inicio < finMaximoGrupo) {
            grupoActual.add(clase)
            val finClase = timeToMinutes(clase.horaFin)
            if (finClase > finMaximoGrupo) {
                finMaximoGrupo = finClase
            }
        } else {
            // No se pisan, cerramos el grupo y abrimos uno nuevo
            grupos.add(grupoActual)
            grupoActual = mutableListOf(clase)
            finMaximoGrupo = timeToMinutes(clase.horaFin)
        }
    }
    grupos.add(grupoActual)
    return grupos
}

@Composable
fun ClaseCard(
    clase: SesionRespuesta,
    esProfesor: Boolean,
    fechaActualTexto: String,
    estaSolapada: Boolean = false,
    onVerEnMapa: (String, String) -> Unit,
    onClaseCancelada: (String) -> Unit,
    onClaseRestaurada: (String) -> Unit, // <-- ¡NUEVO PARÁMETRO!
    modifier: Modifier = Modifier
) {
    // 1. Verificamos si esta clase está cancelada hoy
    val estaCancelada = clase.fechasCanceladas?.contains(fechaActualTexto) == true

    // 2. Colores dinámicos: Si está cancelada es  GRIS, si no, usa su color
    val colorOriginal = Color(android.graphics.Color.parseColor(getColorForAsignatura(clase.asignaturaId.nombre)))
    val colorBase = if (estaCancelada) Color.Gray else colorOriginal
    val colorFondo = if (estaCancelada) Color(0xFFF5F5F5) else Color.White

    var mostrarDialogoConfirmacion by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = if (estaCancelada) 0.dp else 2.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.width(8.dp).fillMaxHeight().background(colorBase))

            Column(
                modifier = Modifier.padding(10.dp).fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // BLOQUE SUPERIOR
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (estaCancelada) "CANCELADA" else "${clase.horaInicio} - ${clase.horaFin}",
                            color = if (estaCancelada) Color.Red else colorBase,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(text = "Grupo ${clase.grupo}", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = clase.asignaturaId.nombre,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = if (estaCancelada) Color.Gray else Color.Black,
                        lineHeight = 16.sp, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (estaCancelada) "No hay clase hoy" else "Prof: ${clase.profesorId.nombre}",
                        color = Color.DarkGray, fontSize = 11.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                // BLOQUE INFERIOR: Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // BOTONES DE PROFESOR (Anular o Restaurar)
                    if (esProfesor) {
                        if (estaCancelada) {
                            // BOTÓN DE RESTAURAR (Verde)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        coroutineScope.launch {
                                            try {
                                                RetrofitClient.apiService.restaurarClase(clase._id, CancelarClaseRequest(fechaActualTexto))
                                                onClaseRestaurada(clase._id) // La quitamos del array local
                                            } catch (e: Exception) {
                                                Log.e("API", "Error al restaurar: ${e.message}")
                                            }
                                        }
                                    }
                                    .background(Color(0xFFE8F5E9)) // Verde clarito
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Restore, contentDescription = "Restaurar", tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Restaurar", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        } else {
                            // BOTÓN DE ANULAR (Rojo)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { mostrarDialogoConfirmacion = true }
                                    .background(Color(0xFFFFEBEE))
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = "Cancelar", tint = Color.Red, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Anular hoy", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // BOTÓN DE MAPA (Solo se puede ir al mapa si la clase NO está cancelada)
                    if (!estaCancelada) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onVerEnMapa(clase.nodoAulaId, clase.asignaturaId.facultadId) }
                                .background(colorBase.copy(alpha = 0.1f))
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = "Aula", tint = colorBase, modifier = Modifier.size(12.dp))
                            if (!estaSolapada) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = clase.aulaNombre ?: clase.nodoAulaId, fontWeight = FontWeight.Bold, color = colorBase, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // DIÁLOGO DE CONFIRMACIÓN
    // ==========================================
    if (mostrarDialogoConfirmacion) {
        val coroutineScope = rememberCoroutineScope()
        AlertDialog(
            onDismissRequest = { mostrarDialogoConfirmacion = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red) },
            title = { Text("¿Anular clase?") },
            text = { Text("¿Estás seguro de que quieres cancelar la clase de ${clase.asignaturaId.nombre} para el día $fechaActualTexto? Los alumnos dejarán de verla en su horario de hoy.") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    onClick = {
                        coroutineScope.launch {
                            try {
                                Log.d("FECHA", "FECHA: ${fechaActualTexto}")
                                RetrofitClient.apiService.cancelarClase(clase._id, CancelarClaseRequest(fechaActualTexto))
                                // 2. Ocultamos el diálogo
                                mostrarDialogoConfirmacion = false
                                // 3. Avisamos a la pantalla para que la oculte
                                onClaseCancelada(clase._id)
                            } catch (e: Exception) {
                                Log.e("API", "Error al cancelar: ${e.message}")
                            }
                        }
                    }
                ) { Text("Sí, anular", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoConfirmacion = false }) { Text("Volver", color = Color.Gray) }
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoCrearClase(
    asignaturas: List<AsignaturaInfo>,
    profesorIdMongo: String,
    aulaNombre: String,
    nodoAulaId: String,
    fechaActual: String,
    onSeleccionarMapa: (String) -> Unit,
    onDismiss: () -> Unit,
    onClaseCreada: (SesionRespuesta) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    var asignaturaSeleccionada by remember { mutableStateOf(asignaturas.firstOrNull()) }
    var desplegableExpandido by remember { mutableStateOf(false) }

    var esClaseUnica by remember { mutableStateOf(false) }

    var diaSemana by remember { mutableStateOf("1") }
    var horaInicio by remember { mutableStateOf("16:00") }
    var horaFin by remember { mutableStateOf("18:00") }
    var grupo by remember { mutableStateOf("A") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Programar nueva clase", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // DESPLEGABLE
                ExposedDropdownMenuBox(
                    expanded = desplegableExpandido,
                    onExpandedChange = { desplegableExpandido = !desplegableExpandido }
                ) {
                    OutlinedTextField(
                        value = asignaturaSeleccionada?.nombre ?: "Selecciona asignatura",
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        label = { Text("Asignatura") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = desplegableExpandido) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = desplegableExpandido,
                        onDismissRequest = { desplegableExpandido = false }
                    ) {
                        asignaturas.forEach { asig ->
                            DropdownMenuItem(
                                text = { Text(asig.nombre) },
                                onClick = {
                                    asignaturaSeleccionada = asig
                                    desplegableExpandido = false
                                }
                            )
                        }
                    }
                }

                // INTERRUPTOR CLASE ÚNICA
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)).padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Clase de un solo día", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(if (esClaseUnica) "Se creará solo para el $fechaActual" else "Se repetirá todas las semanas", color = Color.Gray, fontSize = 12.sp)
                    }
                    Switch(
                        checked = esClaseUnica,
                        onCheckedChange = { esClaseUnica = it }
                    )
                }

                // FILA: DÍA Y GRUPO
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!esClaseUnica) {
                        OutlinedTextField(
                            value = diaSemana,
                            onValueChange = { diaSemana = it },
                            label = { Text("Día (1=L)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = grupo,
                        onValueChange = { grupo = it },
                        label = { Text("Grupo") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // FILA: HORAS
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = horaInicio,
                        onValueChange = { horaInicio = it },
                        label = { Text("Inicio") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = horaFin,
                        onValueChange = { horaFin = it },
                        label = { Text("Fin") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // FILA: AULA (Botón del mapa)
                OutlinedButton(
                    enabled = asignaturaSeleccionada != null,
                    onClick = {
                        val idFacultad = asignaturaSeleccionada?.facultadId ?: "1"
                        onSeleccionarMapa(idFacultad)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (nodoAulaId.isEmpty()) Color.Gray else Color(0xFF6200EE)
                    )
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Mapa")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (nodoAulaId.isEmpty()) "📍 Toca para elegir aula"
                        else "✅ Aula: $aulaNombre",
                        maxLines = 1
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isLoading && asignaturaSeleccionada != null && nodoAulaId.isNotEmpty(),
                onClick = {
                    isLoading = true
                    coroutineScope.launch {
                        try {
                            val diaSemanaCalculado = if (esClaseUnica) {
                                java.time.LocalDate.parse(fechaActual).dayOfWeek.value
                            } else {
                                diaSemana.toIntOrNull() ?: 1
                            }

                            val request = CrearClaseRequest(
                                asignaturaId = asignaturaSeleccionada!!._id,
                                profesorId = profesorIdMongo,
                                diaSemana = diaSemanaCalculado,
                                horaInicio = horaInicio,
                                horaFin = horaFin,
                                nodoAulaId = nodoAulaId,
                                aulaNombre = aulaNombre,
                                grupo = grupo,
                                fechaEspecifica = if (esClaseUnica) fechaActual else null
                            )
                            val nuevaClase = RetrofitClient.apiService.crearClase(request)
                            onClaseCreada(nuevaClase)
                        } catch (e: Exception) {
                            android.util.Log.e("API_CREAR", "Error: ${e.message}")
                        } finally {
                            isLoading = false
                        }
                    }
                }
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                else Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) { Text("Cancelar") }
        }
    )
}

fun getColorForAsignatura(nombre: String): String {
    when (nombre) {
        // --- 1º Cuatrimestre ---
        "Organización Empresarial" -> return "#D32F2F" // Rojo fuerte
        "Fundamentos Físicos de la Informática" -> return "#1976D2" // Azul
        "Fundamentos de Electrónica" -> return "#388E3C" // Verde
        "Introducción a la Programación" -> return "#F57C00" // Naranja
        "Matemática Discreta" -> return "#7B1FA2" // Morado

        // --- 2º Cuatrimestre (¡Nuevos colores!) ---
        "Cálculo para la Computación" -> return "#303F9F" // Índigo / Azul oscuro
        "Introducción a la Ingeniería del Software" -> return "#C2185B" // Rosa fuerte / Magenta
        "Tecnología de Computadores" -> return "#00796B" // Verde azulado oscuro (Teal)
        "Programacion Avanzada I" -> return "#E64A19" // Naranja óxido / Caldera
        "Estructuras Algebraicas" -> return "#7B1FA2" // Morado

        // --- Otros ---
        "Sistemas Operativos" -> return "#00838F" // Cian oscuro
    }


    val coloresReserva = listOf(
        "#0288D1", // Azul claro vibrante
        "#D81B60", // Fucsia
        "#00897B", // Esmeralda
        "#FBC02D", // Mostaza brillante
        "#8E24AA", // Violeta
        "#7CB342"  // Verde manzana oscuro
    )

    // Una fórmula diferente: multiplicamos la primera letra, la última y la longitud
    val calculo = (nombre.first().code + nombre.last().code + nombre.length) * 17

    return coloresReserva[kotlin.math.abs(calculo) % coloresReserva.size]
}