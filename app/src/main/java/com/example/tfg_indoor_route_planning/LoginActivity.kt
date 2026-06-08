package com.example.tfg_indoor_route_planning

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.tfg_indoor_route_planning.api.dto.LoginRequest
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (UserSession.init(this)) {
            irAlDashboard()
            return
        }
        setContent {
            MaterialTheme {
                LoginScreen(onLoginSuccess = { irAlDashboard() })
            }
        }
    }

    private fun irAlDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var idInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    var mostrarDialogoRecuperacion by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Fondo general de la pantalla
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
            // CABECERA
            Text(text = "Bienvenido a", fontSize = 18.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            Text(text = "ControlUMA", fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF6200EE), letterSpacing = (-1).sp)

            Spacer(modifier = Modifier.height(40.dp))

            // TARJETA DEL FORMULARIO
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
                    // CAMPO: ID DE USUARIO
                    OutlinedTextField(
                        value = idInput,
                        onValueChange = { idInput = it },
                        label = { Text("ID de Usuario") },
                        placeholder = { Text("ej: alum_101", color = Color.LightGray) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = "User Icon", tint = Color(0xFF6200EE)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6200EE),
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // CAMPO: CONTRASEÑA CON OJO
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock Icon", tint = Color(0xFF6200EE)) },
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = "Toggle password visibility", tint = Color.Gray)
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6200EE),
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        )
                    )

                    // BOTÓN DE OLVIDÉ MI CONTRASEÑA
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        TextButton(
                            onClick = { mostrarDialogoRecuperacion = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        ) {
                            Text(
                                text = "¿Has olvidado tu contraseña?",
                                color = Color(0xFF6200EE),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // BOTÓN PRINCIPAL
                    Button(
                        onClick = {
                            if (idInput.isBlank() || passwordInput.isBlank()) return@Button
                            isLoading = true
                            coroutineScope.launch {
                                try {
                                    val request = LoginRequest(
                                        idUsuario = idInput.trim(),
                                        password = passwordInput.trim()
                                    )
                                    val authResponse = RetrofitClient.authService.loginUsuario(request)
                                    android.util.Log.d("TFG_LOGIN", "URL de la foto recibida: '${authResponse.usuario.foto_url}'")
                                    UserSession.iniciarSesion(
                                        context = context,
                                        id = authResponse.usuario.idUsuario,
                                        nombreUsuario = authResponse.usuario.nombre,
                                        rolUsuario = authResponse.usuario.rol,
                                        jwtToken = authResponse.token,
                                        urlFoto = authResponse.usuario.foto_url ?: "",
                                        accesible = authResponse.usuario.rutasAccesibles
                                    )
                                    isLoading = false
                                    onLoginSuccess()
                                } catch (e: Exception) {
                                    isLoading = false
                                    Toast.makeText(context, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
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
                            Text("INICIAR SESIÓN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 1.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Divider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
                        Text(text = "O", modifier = Modifier.padding(horizontal = 8.dp), color = Color.Gray, fontSize = 12.sp)
                        Divider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // BOTÓN SECUNDARIO: INVITADO
                    OutlinedButton(
                        onClick = {
                            UserSession.iniciarSesion(context, "guest_000", "Invitado", "INVITADO", "")
                            onLoginSuccess()
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        enabled = !isLoading
                    ) {
                        Text("Continuar sin registrarse", color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // REGISTRO
            TextButton(
                onClick = { context.startActivity(Intent(context, RegistroActivity::class.java)) },
                enabled = !isLoading
            ) {
                Text(text = "¿No tienes cuenta? ", color = Color.Gray)
                Text(text = "Regístrate aquí", color = Color(0xFF6200EE), fontWeight = FontWeight.Bold)
            }
        }
    }

    if (mostrarDialogoRecuperacion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoRecuperacion = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Información",
                    tint = Color(0xFF6200EE)
                )
            },
            title = {
                Text(text = "Recuperación de cuenta")
            },
            text = {
                Text(
                    text = "Por motivos de seguridad institucional de la UMA, el restablecimiento automático de contraseñas está desactivado.\n\nPor favor, contacta con la secretaría de tu facultad o el administrador del sistema para generar unas nuevas credenciales de acceso.",
                    color = Color.DarkGray,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { mostrarDialogoRecuperacion = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                ) {
                    Text("Entendido", color = Color.White)
                }
            }
        )
    }
}