package com.example.tfg_indoor_route_planning

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Accessible
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tfg_indoor_route_planning.api.RetrofitClient
import com.example.tfg_indoor_route_planning.api.dto.CambiarPasswordRequest
import com.example.tfg_indoor_route_planning.api.dto.PoiFavorito
import com.example.tfg_indoor_route_planning.repositories.HorarioRepository
import com.example.tfg_indoor_route_planning.repositories.MapaRepository
import com.example.tfg_indoor_route_planning.repositories.UsuarioRepository
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import kotlin.collections.find

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                DashboardScreen(
                    onNavigateToMapa = {
                        val intent = Intent(this@DashboardActivity, MainActivity::class.java)
                        startActivity(intent)
                    },
                    onNavigateToHorario = {
                        val intent = Intent(this@DashboardActivity, HorarioActivity::class.java)
                        startActivity(intent)
                    },
                    onNavigateToNoticias = {
                        val intent = Intent(this@DashboardActivity, NoticiasActivity::class.java)
                        startActivity(intent)
                    },
                    onNavigateToMatricula = {
                        val intent = Intent(this@DashboardActivity, MatriculacionActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToMapa: () -> Unit,
    onNavigateToHorario: () -> Unit,
    onNavigateToNoticias: () -> Unit,
    onNavigateToMatricula: () -> Unit
) {
    val context = LocalContext.current
    val nombreUsuario = UserSession.nombre.ifBlank { "Usuario" }
    val rolUsuario = UserSession.rol
    val urlFotoActual = UserSession.fotoUrl ?: ""

    var showProfileSheet by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // ==========================================
            // CABECERA CON BOTÓN DE PERFIL
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val (bgColor, textColor, borderColor) = when (rolUsuario.uppercase()) {
                        "PROFESOR" -> Triple(Color(0xFFFAF5FF), Color(0xFF7E22CE), Color(0xFFE9D5FF))
                        "ALUMNO" -> Triple(Color(0xFFECFDF5), Color(0xFF047857), Color(0xFFA7F3D0))
                        "ADMIN" -> Triple(Color(0xFFFEF2F2), Color(0xFFB91C1C), Color(0xFFFECACA))
                        else -> Triple(Color(0xFFF3F4F6), Color(0xFF4B5563), Color(0xFFE5E7EB))
                    }

                    Surface(
                        color = bgColor,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
                    ) {
                        Text(
                            text = rolUsuario.uppercase(),
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), // Un poco más de padding
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "¡Hola, $nombreUsuario!",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        lineHeight = 32.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "¿Qué necesitas hacer hoy?",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(if (urlFotoActual.isBlank()) Color(0xFF6200EE) else Color.Transparent)
                        .clickable { showProfileSheet = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (urlFotoActual.isBlank()) {
                        val inicial = nombreUsuario.firstOrNull()?.uppercase() ?: "U"
                        Text(text = inicial, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    } else {
                        AsyncImage(
                            model = urlFotoActual,
                            contentDescription = "Foto de perfil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // ==========================================
            // LISTA DE TARJETAS HORIZONTALES
            // ==========================================
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashboardCardWide(
                    title = "Navegación y Mapas",
                    subtitle = "Encuentra tu ruta en la facultad",
                    imageResId = R.drawable.location,
                    onClick = onNavigateToMapa
                )

                DashboardCardWide(
                    title = "Mi Horario",
                    subtitle = "Gestiona tus clases de hoy",
                    imageResId = R.drawable.timetable,
                    enabled = !UserSession.esInvitado,
                    onClick = onNavigateToHorario
                )

                if (rolUsuario != "PROFESOR") {
                    DashboardCardWide(
                        title = "Mis Asignaturas",
                        subtitle = "Configura tu matrícula y grupos",
                        imageResId = R.drawable.book,
                        enabled = !UserSession.esInvitado,
                        onClick = onNavigateToMatricula
                    )
                }

                DashboardCardWide(
                    title = "Noticias UMA",
                    subtitle = "Últimas novedades del campus",
                    imageResId = R.drawable.news,
                    onClick = onNavigateToNoticias
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // ==========================================
        // MENÚ DESPLEGABLE DE PERFIL (Bottom Sheet)
        // ==========================================
        if (showProfileSheet) {
            ModalBottomSheet(
                onDismissRequest = { showProfileSheet = false },
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                ProfileMenuContent(
                    onCerrarSesion = {
                        showProfileSheet = false
                        UserSession.cerrarSesion(context)
                        MapaRepository.limpiarCache()
                        UsuarioRepository.limpiarCache()
                        HorarioRepository.limpiarCache()
                        val intent = Intent(context, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun ProfileMenuContent(onCerrarSesion: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val nombreUsuario = UserSession.nombre.ifBlank { "Usuario" }
    val niuUsuario = UserSession.usuarioId.ifBlank { "Sin identificar" }

    val scrollState = rememberScrollState()

    var urlFotoActual by remember { mutableStateOf(UserSession.fotoUrl ?: "") }
    var isLoadingFoto by remember { mutableStateOf(false) }
    var rutasAccesibles by remember { mutableStateOf(UserSession.rutasAccesibles) }

    var expandFavoritos by remember { mutableStateOf(false) }
    var mostrarDialogoPassword by remember { mutableStateOf(false) }

    var listaFavoritos by remember { mutableStateOf<List<PoiFavorito>>(emptyList()) }
    var isLoadingFavoritos by remember { mutableStateOf(false) }

    // Obtenemos los favoritos desde el backend al abrir el menú
    LaunchedEffect(Unit) {
        if (!UserSession.esInvitado) {
            isLoadingFavoritos = true
            try {
                val usuarioInfo = UsuarioRepository.getUsuario(UserSession.usuarioId)
                listaFavoritos = usuarioInfo.poisFavoritos
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoadingFavoritos = false
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imagenSeleccionada ->
            isLoadingFoto = true
            coroutineScope.launch {
                var tempFile: File? = null
                try {
                    tempFile = uriToFile(context, imagenSeleccionada)
                    android.util.Log.d(
                        "TFG_FOTO_DEBUG",
                        "Tamaño archivo = ${tempFile?.length()} bytes"
                    )

                    if (tempFile != null) {
                        val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
                        val body = MultipartBody.Part.createFormData("foto", tempFile.name, requestFile)

                        val response = RetrofitClient.usuarioService.subirFotoPerfil(UserSession.token, body)

                        if (response.isSuccessful) {
                            val nuevaUrl = response.body()?.url ?: ""
                            UserSession.actualizarFotoUrl(context, nuevaUrl)
                            urlFotoActual = nuevaUrl
                            Toast.makeText(context, "✅ Foto actualizada", Toast.LENGTH_SHORT).show()
                            UsuarioRepository.invalidarCache()
                        } else {
                            val codigoError = response.code()
                            val cuerpoError = response.errorBody()?.string() ?: "Sin detalles"
                            android.util.Log.e("TFG_FOTO", "Fallo servidor: Código $codigoError - Detalles: $cuerpoError")
                            Toast.makeText(context, "❌ Error $codigoError. Revisa el Logcat", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(context, "❌ Error al procesar la imagen local", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("TFG_FOTO_APP", "¡CRASH AL SUBIR FOTO!", e)
                    Toast.makeText(context, "Error de conexión al subir la foto", Toast.LENGTH_SHORT).show()
                } finally {
                    isLoadingFoto = false
                    tempFile?.delete()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // AVATAR GRANDE
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(if (urlFotoActual.isBlank()) Color(0xFF6200EE).copy(alpha = 0.1f) else Color.Transparent)
                    .clickable { galleryLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (isLoadingFoto) {
                    CircularProgressIndicator(color = Color(0xFF6200EE))
                } else if (urlFotoActual.isBlank()) {
                    val inicial = nombreUsuario.firstOrNull()?.uppercase() ?: "U"
                    Text(text = inicial, color = Color(0xFF6200EE), fontSize = 40.sp, fontWeight = FontWeight.Bold)
                } else {
                    AsyncImage(
                        model = urlFotoActual,
                        contentDescription = "Foto de perfil grande",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            if (!isLoadingFoto) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color.White, CircleShape)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Cambiar foto", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = nombreUsuario, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "NIU: $niuUsuario", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium)

        Spacer(modifier = Modifier.height(32.dp))

        Divider(color = Color(0xFFEEEEEE))

        // ==========================================
        // OPCIÓN: Favoritos
        // ==========================================
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandFavoritos = !expandFavoritos }
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(40.dp).background(Color(0xFFFFF8E1), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Mis Sitios Favoritos", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Aulas y laboratorios guardados", color = Color.Gray, fontSize = 13.sp)
                }
                Icon(
                    imageVector = if (expandFavoritos) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.LightGray
                )
            }

            // CONTENIDO DESPLEGABLE DE FAVORITOS
            AnimatedVisibility(visible = expandFavoritos) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, bottom = 16.dp, end = 24.dp)
                ) {
                    if (isLoadingFavoritos) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = Color(0xFFFFC107))
                        }
                    } else if (listaFavoritos.isEmpty()) {
                        Text("Aún no tienes sitios favoritos guardados.", color = Color.Gray, fontSize = 13.sp)
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))

                        listaFavoritos.forEach { favorito ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        coroutineScope.launch {
                                            try {
                                                val mapas = MapaRepository.getTodosLosMapas()
                                                val mapaDestino = mapas.find { it.nombre == favorito.facultad }

                                                if (mapaDestino != null) {
                                                    val intent = Intent(context, MainActivity::class.java).apply {
                                                        putExtra("AULA_DESTINO_ID", favorito.idPoi)
                                                        putExtra("FACULTAD_DESTINO_ID", mapaDestino.mapaId)
                                                    }
                                                    context.startActivity(intent)
                                                } else {
                                                    Toast.makeText(context, "No se encontró el mapa", Toast.LENGTH_SHORT).show()
                                                }
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Error de red al abrir el mapa", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0F0F0))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(favorito.nombrePoi, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.LocationCity, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(favorito.facultad, fontSize = 13.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Ir al mapa", tint = Color(0xFFE0E0E0), modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        Divider(color = Color(0xFFEEEEEE))

        // ==========================================
        // Cambiar Contraseña
        // ==========================================
        if (!UserSession.esInvitado) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { mostrarDialogoPassword = true }
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(40.dp).background(Color(0xFFE8F5E9), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF4CAF50))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Seguridad", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Cambiar contraseña de acceso", color = Color.Gray, fontSize = 13.sp)
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.LightGray
                )
            }

            Divider(color = Color(0xFFEEEEEE))
        }

        // ==========================================
        // OPCIÓN: Accesibilidad
        // ==========================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(40.dp).background(Color(0xFFE3F2FD), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Accessible, contentDescription = null, tint = Color(0xFF2196F3))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Rutas Accesibles", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Evitar escaleras en el mapa", color = Color.Gray, fontSize = 13.sp)
            }
            Switch(
                checked = rutasAccesibles,
                onCheckedChange = { nuevoValor ->
                    rutasAccesibles = nuevoValor

                    coroutineScope.launch {
                        val exito = UsuarioRepository.cambiarAccesibilidadServidor(context, UserSession.usuarioId, nuevoValor)
                        if (!exito) {
                            // Si falla internet, revertimos el interruptor y avisamos
                            rutasAccesibles = !nuevoValor
                            Toast.makeText(context, "Error al sincronizar con el servidor", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF6200EE),
                    checkedTrackColor = Color(0xFF6200EE).copy(alpha = 0.5f)
                )
            )
        }

        Divider(color = Color(0xFFEEEEEE))

        Spacer(modifier = Modifier.height(24.dp))

        // BOTÓN SALIR
        TextButton(
            onClick = onCerrarSesion,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFD32F2F), containerColor = Color(0xFFFFEBEE))
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("CERRAR SESIÓN", fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 1.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // ==========================================
    // INVOCACIÓN DEL DIÁLOGO DE CONTRASEÑA
    // ==========================================
    if (mostrarDialogoPassword) {
        DialogoCambiarPassword(
            onDismiss = { mostrarDialogoPassword = false }
        )
    }
}

// ==========================================================
// DIÁLOGO DE CAMBIO DE CONTRASEÑA
// ==========================================================
@Composable
fun DialogoCambiarPassword(onDismiss: () -> Unit) {
    var passAntigua by remember { mutableStateOf("") }
    var passNueva by remember { mutableStateOf("") }
    var passRepetida by remember { mutableStateOf("") }
    var passAntiguaVisible by remember { mutableStateOf(false) }
    var passNuevaVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Cambiar Contraseña", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Por seguridad, introduce tu contraseña actual y la nueva que deseas utilizar.",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Input: Contraseña Actual
                OutlinedTextField(
                    value = passAntigua,
                    onValueChange = { passAntigua = it },
                    label = { Text("Contraseña actual") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (passAntiguaVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                    trailingIcon = {
                        val image = if (passAntiguaVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { passAntiguaVisible = !passAntiguaVisible }) {
                            Icon(imageVector = image, contentDescription = null, tint = Color.Gray)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Input: Nueva Contraseña
                OutlinedTextField(
                    value = passNueva,
                    onValueChange = { passNueva = it },
                    label = { Text("Nueva contraseña") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (passNuevaVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                    trailingIcon = {
                        val image = if (passNuevaVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { passNuevaVisible = !passNuevaVisible }) {
                            Icon(imageVector = image, contentDescription = null, tint = Color.Gray)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Input: Repetir Nueva Contraseña
                OutlinedTextField(
                    value = passRepetida,
                    onValueChange = { passRepetida = it },
                    label = { Text("Repite nueva contraseña") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (passNuevaVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    isError = passNueva.isNotEmpty() && passRepetida.isNotEmpty() && passNueva != passRepetida
                )
                if (passNueva.isNotEmpty() && passRepetida.isNotEmpty() && passNueva != passRepetida) {
                    Text(
                        text = "Las contraseñas no coinciden",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (passAntigua.isBlank() || passNueva.isBlank() || passNueva != passRepetida) return@Button

                    isLoading = true
                    coroutineScope.launch {
                        try {

                            val request = CambiarPasswordRequest(passAntigua, passNueva)
                            val response = RetrofitClient.authService.cambiarPassword(UserSession.token, UserSession.usuarioId, request)
                            if (response.isSuccessful) {
                                Toast.makeText(context, "Contraseña actualizada correctamente", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            } else {
                                Toast.makeText(context, "La contraseña actual es incorrecta", Toast.LENGTH_SHORT).show()
                            }

                            // Simulación temporal para que no falle al compilar:
                            kotlinx.coroutines.delay(1000)
                            Toast.makeText(context, "Conecta esta función a la API", Toast.LENGTH_LONG).show()
                            onDismiss()

                        } catch (e: Exception) {
                            Toast.makeText(context, "Error de red", Toast.LENGTH_SHORT).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading && passAntigua.isNotBlank() && passNueva.isNotBlank() && passNueva == passRepetida,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Actualizar", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Cancelar", color = Color.Gray)
            }
        }
    )
}

// ==========================================================
// FUNCIÓN AUXILIAR PARA LA CÁMARA/GALERÍA
// ==========================================================
private fun uriToFile(context: Context, uri: Uri): File? {
    return try {
        val file = File(context.cacheDir, "temp_perfil_${Date().time}.jpg")
        val inputStream = context.contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(file)

        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()

        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// ==========================================================
// COMPONENTE DE TARJETA
// ==========================================================
@Composable
fun DashboardCardWide(
    title: String,
    subtitle: String,
    imageResId: Int,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = if (enabled) 6.dp else 0.dp),
        colors = CardDefaults.cardColors(containerColor = if (enabled) Color.White else Color(0xFFF0F0F0)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable {
                if (enabled) {
                    onClick()
                } else {
                    Toast.makeText(context, "Regístrate para ver tu horario", Toast.LENGTH_SHORT).show()
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(42.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    alpha = if (enabled) 1f else 0.3f
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) Color.Black else Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            if (enabled) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Ir",
                    tint = Color(0xFFE0E0E0),
                    modifier = Modifier.size(28.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Bloqueado",
                    tint = Color.Gray,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}