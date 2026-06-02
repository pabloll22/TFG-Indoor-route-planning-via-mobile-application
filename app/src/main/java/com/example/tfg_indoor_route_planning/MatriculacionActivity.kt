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
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
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
import com.example.tfg_indoor_route_planning.api.MatriculaRequest
import com.example.tfg_indoor_route_planning.api.RetrofitClient
import com.example.tfg_indoor_route_planning.horario.AsignaturaInfo
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

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    // 1. CARGA DE DATOS AL ENTRAR
    LaunchedEffect(Unit) {
        try {
            // Descargamos las asignaturas y garantizamos que no haya repetidas por ID
            val resAsignaturas = RetrofitClient.apiService.getAsignaturas().distinctBy { it._id }
            asignaturas = resAsignaturas.sortedWith(compareBy({ it.curso ?: 99 }, { it.cuatrimestre }))

            // Descargamos el perfil para pre-marcar las que ya tiene
            val perfilUsuario = RetrofitClient.apiService.getUsuario(UserSession.usuarioId)
            perfilUsuario.asignaturasMatriculadas.forEach { matricula ->
                seleccionadas[matricula.asignaturaId] = matricula.grupo
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

                val response = RetrofitClient.apiService.actualizarMatricula(
                    token = UserSession.token,
                    idUsuario = UserSession.usuarioId,
                    matricula = matriculaRequest
                )

                if (response.isSuccessful) {
                    Toast.makeText(context, "Matrícula guardada correctamente", Toast.LENGTH_SHORT).show()
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

    // Agrupamos para los Sticky Headers
    val asignaturasAgrupadas = asignaturas.groupBy { it.curso ?: 99 }

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
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
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

                // BUCLE PRINCIPAL DE DIBUJADO
                asignaturasAgrupadas.forEach { (curso, listaAsignaturas) ->

                    stickyHeader {
                        val tituloCurso = if (curso == 99) "Asignaturas sin curso" else "Curso $curso º"
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8F9FA).copy(alpha = 0.95f))
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = tituloCurso.uppercase(),
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF6200EE),
                                fontSize = 13.sp,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }

                    // Previene duplicados visuales en Compose
                    items(items = listaAsignaturas, key = { it._id }) { asig ->
                        val isSelected = seleccionadas.containsKey(asig._id)
                        val grupoSeleccionado = seleccionadas[asig._id] ?: ""

                        // Lista real de grupos que provienen del servidor
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
                                        Divider(color = Color(0xFFE0E0E0).copy(alpha = 0.5f))
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
}