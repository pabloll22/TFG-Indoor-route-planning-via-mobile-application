package com.example.tfg_indoor_route_planning

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tfg_indoor_route_planning.api.RetrofitClient
import com.example.tfg_indoor_route_planning.api.dto.MatriculaRequest
import com.example.tfg_indoor_route_planning.horario.AsignaturaInfo
import com.example.tfg_indoor_route_planning.repositories.HorarioRepository
import com.example.tfg_indoor_route_planning.repositories.UsuarioRepository
import kotlinx.coroutines.launch

class MatriculacionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MatriculacionScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun MatriculacionScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var asignaturas by remember { mutableStateOf<List<AsignaturaInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    val seleccionadas = remember { mutableStateMapOf<String, String>() }

    // NUEVO: Estado para controlar qué titulaciones están desplegadas
    val expandedTitulaciones = remember { mutableStateMapOf<String, Boolean>() }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val pullRefreshState = rememberPullToRefreshState()

    // 1. CARGA DE DATOS AL ENTRAR
    LaunchedEffect(Unit) {
        try {
            val resAsignaturas = HorarioRepository.getAsignaturas().distinctBy { it._id }
            asignaturas = resAsignaturas.sortedWith(compareBy({ it.curso ?: 99 }, { it.cuatrimestre }))

            val perfilUsuario = UsuarioRepository.getUsuario(UserSession.usuarioId)
            perfilUsuario.asignaturasMatriculadas.forEach { matricula ->
                seleccionadas[matricula.asignaturaId] = matricula.grupo
            }

            val titulacionesConSeleccion = seleccionadas.keys.mapNotNull { id ->
                resAsignaturas.find { it._id == id }?.titulacion ?: "Otras Titulaciones"
            }.toSet()

            val titulacionesUnicas = resAsignaturas.map { it.titulacion ?: "Otras Titulaciones" }.distinct()

            titulacionesUnicas.forEach { tit ->
                // Abre la titulación si el usuario tiene asignaturas marcadas ahí
                expandedTitulaciones[tit] = titulacionesConSeleccion.contains(tit)
            }

            // Si es un alumno nuevo y no tiene nada seleccionado, abrimos la primera por defecto
            if (titulacionesConSeleccion.isEmpty() && titulacionesUnicas.isNotEmpty()) {
                expandedTitulaciones[titulacionesUnicas.first()] = true
            }

        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("MATRICULA_ERROR", "Fallo al cargar asignaturas: ${e.message}")
            Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    // 2. FUNCIÓN DE GUARDADO
    val handleGuardar: () -> Unit = {
        coroutineScope.launch {
            isSaving = true
            try {
                val matriculaRequest = seleccionadas.map {
                    MatriculaRequest(asignaturaId = it.key, grupo = it.value)
                }

                val response = RetrofitClient.horarioService.actualizarMatricula(
                    token = UserSession.token,
                    idUsuario = UserSession.usuarioId,
                    matricula = matriculaRequest
                )

                if (response.isSuccessful) {
                    Toast.makeText(context, "Matrícula guardada correctamente", Toast.LENGTH_SHORT).show()
                    UsuarioRepository.invalidarCache()
                    HorarioRepository.limpiarCache()
                    onBack()
                } else {
                    Toast.makeText(context, "Error del servidor al guardar", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error de red al guardar matrícula", Toast.LENGTH_SHORT).show()
            } finally {
                isSaving = false
            }
        }
    }

    // 3. AGRUPACIÓN DOBLE
    val asignaturasPorTitulacionYCurso = asignaturas
        .groupBy { it.titulacion ?: "Otras Titulaciones" }
        .mapValues { (_, listaTitulacion) ->
            listaTitulacion.groupBy { it.curso ?: 99 }
        }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Configurar Matrícula", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    scrolledContainerColor = Color.White
                ),
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 16.dp,
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = handleGuardar,
                        enabled = seleccionadas.isNotEmpty() && !isSaving,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6200EE),
                            disabledContainerColor = Color(0xFFE0E0E0),
                            disabledContentColor = Color.Gray
                        )
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Guardando...", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        } else {
                            Icon(Icons.Default.Save, contentDescription = "Guardar", modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardar Matrícula (${seleccionadas.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        if (isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF6200EE), strokeWidth = 3.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Cargando plan de estudios...", color = Color.Gray, fontSize = 14.sp)
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .nestedScroll(pullRefreshState.nestedScrollConnection)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Selecciona tus asignaturas e indica a qué grupo asistes para generar tu horario personalizado.",
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp, end = 4.dp)
                        )
                    }

                    asignaturasPorTitulacionYCurso.forEach { (titulacion, asignaturasPorCurso) ->

                        val isExpanded = expandedTitulaciones[titulacion] ?: false

                        // CABECERA STICKY CLICKABLE (DESPLEGABLE)
                        stickyHeader {
                            Surface(
                                color = Color(0xFFF8F9FA).copy(alpha = 0.95f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedTitulaciones[titulacion] = !isExpanded }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = titulacion.uppercase(),
                                        fontWeight = FontWeight.Black,
                                        color = Color.Black,
                                        fontSize = 16.sp,
                                        letterSpacing = 1.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Desplegar",
                                        tint = Color.Gray
                                    )
                                }
                            }
                        }

                        if (isExpanded) {
                            asignaturasPorCurso.forEach { (curso, listaAsignaturas) ->

                                item {
                                    val tituloCurso = if (curso == 99) "Asignaturas optativas / Sin curso" else "Curso $curso º"
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                                    ) {
                                        HorizontalDivider(modifier = Modifier.width(24.dp), color = Color(0xFF6200EE), thickness = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = tituloCurso,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF6200EE),
                                            fontSize = 14.sp
                                        )
                                    }
                                }

                                items(items = listaAsignaturas, key = { it._id }) { asig ->
                                    val isSelected = seleccionadas.containsKey(asig._id)
                                    val grupoSeleccionado = seleccionadas[asig._id] ?: ""

                                    val gruposDeEstaAsig = if (!asig.grupos.isNullOrEmpty()) asig.grupos else listOf("Sin grupos programados")

                                    val borderColor by animateColorAsState(
                                        targetValue = if (isSelected) Color(0xFF6200EE).copy(alpha = 0.5f) else Color(0xFFE0E0E0),
                                        animationSpec = tween(300)
                                    )
                                    val backgroundColor by animateColorAsState(
                                        targetValue = if (isSelected) Color(0xFFF4F0FF) else Color.White,
                                        animationSpec = tween(300)
                                    )

                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = backgroundColor),
                                        border = BorderStroke(1.dp, borderColor),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable {
                                                if (isSelected) seleccionadas.remove(asig._id)
                                                else seleccionadas[asig._id] = gruposDeEstaAsig.first()
                                            }
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .background(if (isSelected) Color(0xFF6200EE) else Color(0xFFF0F0F0), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Book,
                                                        contentDescription = null,
                                                        tint = if (isSelected) Color.White else Color.Gray,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }

                                                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                                                    Text(
                                                        text = asig.nombre,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp,
                                                        color = if (isSelected) Color.Black else Color.DarkGray,
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = "Cuatrimestre ${asig.cuatrimestre}",
                                                        color = Color.Gray,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }

                                            AnimatedVisibility(
                                                visible = isSelected,
                                                enter = expandVertically() + fadeIn(),
                                                exit = shrinkVertically() + fadeOut()
                                            ) {
                                                Column(modifier = Modifier.padding(top = 16.dp)) {
                                                    HorizontalDivider(color = Color(0xFFE0E0E0).copy(alpha = 0.5f))
                                                    Spacer(modifier = Modifier.height(12.dp))
                                                    Text(
                                                        text = "Selecciona tu grupo de asistencia:",
                                                        fontSize = 12.sp,
                                                        color = Color.Gray,
                                                        fontWeight = FontWeight.Bold,
                                                        letterSpacing = 0.5.sp
                                                    )
                                                    Spacer(modifier = Modifier.height(10.dp))

                                                    FlowRow(
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        gruposDeEstaAsig.forEach { grupo ->
                                                            val isActive = grupo == grupoSeleccionado

                                                            val chipBg by animateColorAsState(targetValue = if (isActive) Color(0xFF6200EE) else Color.White)
                                                            val chipText by animateColorAsState(targetValue = if (isActive) Color.White else Color.DarkGray)
                                                            val chipBorder by animateColorAsState(targetValue = if (isActive) Color(0xFF6200EE) else Color(0xFFE0E0E0))

                                                            Surface(
                                                                shape = RoundedCornerShape(20.dp),
                                                                color = chipBg,
                                                                border = BorderStroke(1.dp, chipBorder),
                                                                modifier = Modifier.clickable {
                                                                    if (grupo != "Sin grupos programados") {
                                                                        seleccionadas[asig._id] = grupo
                                                                    }
                                                                }
                                                            ) {
                                                                Row(
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                                                ) {
                                                                    if (isActive && grupo != "Sin grupos programados") {
                                                                        Icon(
                                                                            imageVector = Icons.Default.Check,
                                                                            contentDescription = null,
                                                                            tint = Color.White,
                                                                            modifier = Modifier.size(16.dp)
                                                                        )
                                                                        Spacer(modifier = Modifier.width(6.dp))
                                                                    }
                                                                    Text(
                                                                        text = grupo,
                                                                        color = chipText,
                                                                        fontSize = 13.sp,
                                                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
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
                            }
                        }
                    }
                }

                PullToRefreshContainer(
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    containerColor = Color.White,
                    contentColor = Color(0xFF6200EE)
                )
            }

            if (pullRefreshState.isRefreshing) {
                LaunchedEffect(true) {
                    try {
                        HorarioRepository.limpiarCache()

                        val resAsignaturas = HorarioRepository.getAsignaturas(forzarRecarga = true).distinctBy { it._id }
                        asignaturas = resAsignaturas.sortedWith(compareBy({ it.curso ?: 99 }, { it.cuatrimestre }))

                        val perfilUsuario = UsuarioRepository.getUsuario(UserSession.usuarioId, forzarRecarga = true)

                        seleccionadas.clear()
                        perfilUsuario.asignaturasMatriculadas.forEach { matricula ->
                            seleccionadas[matricula.asignaturaId] = matricula.grupo
                        }

                        // Reevaluar expansiones al refrescar
                        val titulacionesConSeleccion = seleccionadas.keys.mapNotNull { id ->
                            resAsignaturas.find { it._id == id }?.titulacion ?: "Otras Titulaciones"
                        }.toSet()

                        resAsignaturas.map { it.titulacion ?: "Otras Titulaciones" }.distinct().forEach { tit ->
                            expandedTitulaciones[tit] = titulacionesConSeleccion.contains(tit)
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, "Error al actualizar asignaturas", Toast.LENGTH_SHORT).show()
                    } finally {
                        pullRefreshState.endRefresh()
                    }
                }
            }
        }
    }
}