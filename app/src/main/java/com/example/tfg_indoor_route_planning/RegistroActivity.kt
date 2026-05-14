package com.example.tfg_indoor_route_planning

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
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
import com.example.tfg_indoor_route_planning.api.RegistroRequest
import com.example.tfg_indoor_route_planning.api.RetrofitClient
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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

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
            Text(text = "Crear Cuenta", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF6200EE))
            Text(text = "Únete a ControlUMA", fontSize = 16.sp, color = Color.Gray, fontWeight = FontWeight.Medium)

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
                    // SELECTOR DE ROL (Premium UI)
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
                                    .clickable { rolSeleccionado = rol }
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

                    // ID DE USUARIO
                    OutlinedTextField(
                        value = idInput,
                        onValueChange = { idInput = it },
                        label = { Text("ID de Usuario") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF6200EE)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF6200EE))
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // NOMBRE COMPLETO
                    OutlinedTextField(
                        value = nombreInput,
                        onValueChange = { nombreInput = it },
                        label = { Text("Nombre Completo") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = Color(0xFF6200EE)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF6200EE))
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // CONTRASEÑA
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
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
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF6200EE))
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // BOTÓN REGISTRARSE
                    Button(
                        onClick = {
                            if (idInput.isBlank() || nombreInput.isBlank() || passwordInput.isBlank()) {
                                Toast.makeText(context, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isLoading = true
                            coroutineScope.launch {
                                try {
                                    val request = RegistroRequest(idInput.trim(), nombreInput.trim(), passwordInput.trim(), rolSeleccionado)
                                    val response = RetrofitClient.apiService.registrarUsuario(request)

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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Text("CREAR CUENTA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 1.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // VOLVER AL LOGIN
            TextButton(onClick = onVolverLogin, enabled = !isLoading) {
                Text(text = "¿Ya tienes cuenta? ", color = Color.Gray)
                Text(text = "Inicia sesión", color = Color(0xFF6200EE), fontWeight = FontWeight.Bold)
            }
        }
    }
}