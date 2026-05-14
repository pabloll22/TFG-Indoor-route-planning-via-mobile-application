package com.example.tfg_indoor_route_planning

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
                    }
                )
            }
        }
    }
}

@Composable
fun DashboardScreen(
    onNavigateToMapa: () -> Unit,
    onNavigateToHorario: () -> Unit,
    onNavigateToNoticias: () -> Unit
) {
    val context = LocalContext.current
    val nombreUsuario = UserSession.nombre.ifBlank { "Usuario" }
    val rolUsuario = UserSession.rol

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // ==========================================
        // CABECERA
        // ==========================================
        Surface(
            color = Color(0xFF6200EE).copy(alpha = 0.1f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = rolUsuario.uppercase(),
                color = Color(0xFF6200EE),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "¡Hola, $nombreUsuario!",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "¿Qué necesitas hacer hoy?",
            fontSize = 16.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ==========================================
        // LISTA DE TARJETAS HORIZONTALES (Diseño Limpio)
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

            DashboardCardWide(
                title = "Noticias UMA",
                subtitle = "Últimas novedades del campus",
                imageResId = R.drawable.news,
                onClick = onNavigateToNoticias
            )
        }

        // ==========================================
        // BOTÓN INFERIOR DE SALIR
        // ==========================================
        OutlinedButton(
            onClick = {
                UserSession.cerrarSesion(context)
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFD32F2F).copy(alpha = 0.5f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "Cerrar sesión",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("CERRAR SESIÓN", fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 1.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

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
                modifier = Modifier
                    .size(42.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = title,
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    alpha = if (enabled) 1f else 0.3f
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Textos
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) Color.Black else Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Icono de acción
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