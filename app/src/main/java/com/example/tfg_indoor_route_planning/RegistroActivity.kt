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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tfg_indoor_route_planning.api.RetrofitClient
import com.example.tfg_indoor_route_planning.api.dto.RegistroRequest
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch

class RegistroActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                RegistroScreen(
                    onRegistroExitoso = { finish() },
                    onVolverLogin = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    onRegistroExitoso: () -> Unit,
    onVolverLogin: () -> Unit
) {
    var idInput by remember { mutableStateOf("") }
    var nombreInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val opcionesRol = listOf("ALUMNO", "PROFESOR")
    var rolSeleccionado by remember { mutableStateOf(opcionesRol[0]) }

    var isLoading by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(false) }
    var carnetValidado by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Comprobamos si los tres campos están rellenos
    val todosLosCamposLlenos = idInput.isNotBlank() && nombreInput.isNotBlank() && passwordInput.isNotBlank()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imagenSeleccionada ->
            isScanning = true
            CarnetValidator.validarCarnet(context, imagenSeleccionada) { success, niuExtraido, rolDetectado ->
                isScanning = false
                if (success) {
                    // VERIFICACIÓN DE SEGURIDAD: El NIU del carnet debe ser igual al introducido
                    if (niuExtraido != null && niuExtraido.equals(idInput.trim(), ignoreCase = true)) {
                        carnetValidado = true
                        if (rolDetectado != null) {
                            rolSeleccionado = rolDetectado
                        }
                        Toast.makeText(context, "✅ Identidad verificada correctamente", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "❌ El NIU del carnet ($niuExtraido) no coincide con el introducido", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(context, "❌ No se detecta un carnet válido de la UMA", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F6)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(text = "Registro Oficial", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF6200EE))
            Text(text = "Identifícate con tu carnet de la UMA", fontSize = 16.sp, color = Color.Gray, fontWeight = FontWeight.Medium)

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // SELECTOR DE ROL
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        opcionesRol.forEach { rol ->
                            val isSelected = rol == rolSeleccionado
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Color.White else Color.Transparent)
                                    .clickable { if (!carnetValidado) rolSeleccionado = rol }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = rol,
                                    color = if (isSelected) Color(0xFF6200EE) else Color.Gray,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ID DE USUARIO (NIU)
                    OutlinedTextField(
                        value = idInput,
                        onValueChange = { if (!carnetValidado) idInput = it },
                        label = { Text("NIU (ID de Usuario)") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF6200EE)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        readOnly = carnetValidado, // Se bloquea si ya está validado
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (carnetValidado) Color(0xFF4CAF50) else Color(0xFF6200EE)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // NOMBRE COMPLETO
                    OutlinedTextField(
                        value = nombreInput,
                        onValueChange = { if (!carnetValidado) nombreInput = it },
                        label = { Text("Nombre Completo") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = Color(0xFF6200EE)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        readOnly = carnetValidado, // Se bloquea si ya está validado
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (carnetValidado) Color(0xFF4CAF50) else Color(0xFF6200EE)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // CONTRASEÑA
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { if (!carnetValidado) passwordInput = it },
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF6200EE)) },
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = null, tint = Color.Gray)
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        readOnly = carnetValidado, // Se bloquea si ya está validado
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (carnetValidado) Color(0xFF4CAF50) else Color(0xFF6200EE)
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // ==========================================
                    // BOTÓN DE VERIFICACIÓN DE CARNET
                    // ==========================================
                    if (carnetValidado) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
                            Spacer(Modifier.width(8.dp))
                            Text("Identidad Universitaria Verificada", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    } else {
                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            shape = RoundedCornerShape(12.dp),
                            enabled = todosLosCamposLlenos && !isScanning // Solo activo si todo está relleno
                        ) {
                            if (isScanning) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text("Analizando documento...", color = Color.White)
                            } else {
                                Icon(Icons.Default.DocumentScanner, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (todosLosCamposLlenos) "VERIFICAR CARNET UMA" else "RELLENA LOS DATOS PRIMERO",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ==========================================
                    // BOTÓN FINAL DE CREAR CUENTA
                    // ==========================================
                    Button(
                        onClick = {
                            isLoading = true
                            coroutineScope.launch {
                                try {
                                    val request = RegistroRequest(
                                        idInput.trim(),
                                        nombreInput.trim(),
                                        passwordInput.trim(),
                                        rolSeleccionado
                                    )
                                    val response = RetrofitClient.authService.registrarUsuario(request)

                                    if (response.isSuccessful) {
                                        Toast.makeText(context, "¡Cuenta creada con éxito!", Toast.LENGTH_LONG).show()
                                        onRegistroExitoso()
                                    } else {
                                        Toast.makeText(context, "El ID de usuario ya existe", Toast.LENGTH_LONG).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6200EE),
                            disabledContainerColor = Color(0xFFE0E0E0)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isLoading && carnetValidado // Solo habilitado si el carnet validó el NIU
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = "CREAR CUENTA",
                                color = if (carnetValidado) Color.White else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // VOLVER AL LOGIN
            TextButton(onClick = onVolverLogin, enabled = !isLoading && !isScanning) {
                Text(text = "¿Ya tienes cuenta? ", color = Color.Gray)
                Text(text = "Inicia sesión", color = Color(0xFF6200EE), fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==============================================================
// MOTOR DE RECONOCIMIENTO ÓPTICO (OCR) CON GOOGLE ML KIT
// ==============================================================
object CarnetValidator {
    fun validarCarnet(
        context: Context,
        uri: Uri,
        onResult: (isValid: Boolean, niu: String?, rolDetectado: String?) -> Unit
    ) {
        try {
            val image = InputImage.fromFilePath(context, uri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    // Convertimos a minúsculas para que la búsqueda no falle por mayúsculas
                    val textoCompleto = visionText.text.lowercase()

                    Log.d("OCR_TFG", "Texto leído:\n$textoCompleto")

                    // 1. Validar que la imagen pertenece a la UMA
                    val esUMA = textoCompleto.contains("universidad de málaga") ||
                            textoCompleto.contains("uma") ||
                            textoCompleto.contains("universidad de malaga")

                    // 2. Extraer el NIU exacto (Ej: "niu: 061090914x")
                    // Busca "niu:", seguido de espacios opcionales (\s*), y captura números y letras ([0-9a-z]+)
                    val regexNIU = Regex("niu:\\s*([0-9a-z]+)")
                    val matchNIU = regexNIU.find(textoCompleto)

                    // Extraemos el grupo 1 (lo que está entre paréntesis en el Regex), que es el NIU limpio
                    val niuEncontrado = matchNIU?.groupValues?.getOrNull(1)

                    Log.d("OCR_TFG", "NIU extraído: $niuEncontrado")

                    // 3. Buscar indicadores de rol
                    val esEstudiante = textoCompleto.contains("estudiante") ||
                            textoCompleto.contains("alumno") ||
                            textoCompleto.contains("grado")

                    val esProfesor = textoCompleto.contains("pdi") ||
                            textoCompleto.contains("profesor") ||
                            textoCompleto.contains("investigador") ||
                            textoCompleto.contains("docente")

                    // Verificamos si todo es correcto
                    if (esUMA && niuEncontrado != null && (esEstudiante || esProfesor)) {
                        val rol = if (esProfesor) "PROFESOR" else "ALUMNO"
                        onResult(true, niuEncontrado, rol)
                    } else {
                        Log.e("OCR_TFG", "Fallo validación -> UMA: $esUMA, NIU: $niuEncontrado, Estudiante: $esEstudiante")
                        onResult(false, null, null)
                    }
                }
                .addOnFailureListener {
                    onResult(false, null, null)
                }
        } catch (e: Exception) {
            e.printStackTrace()
            onResult(false, null, null)
        }
    }
}