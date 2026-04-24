package com.example.tfg_indoor_route_planning.models
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PedalBike
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SettingsInputComponent
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Wc
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class POI(
    val id: String,
    val nombre: String,
    val nodoId: String,
    val plantaId: String
)

data class EstiloPoi(val icono: ImageVector, val color: Color)

// 2. La función que decide el aspecto según el nombre
fun obtenerEstiloPoi(nombre: String): EstiloPoi {
    val n = nombre.lowercase()
    return when {
        n.contains("aseo") || n.contains("baño") -> EstiloPoi(Icons.Default.Wc, Color(0xFF2196F3)) // Azul
        n.contains("clase") || n.contains("aula") -> EstiloPoi(Icons.Default.School, Color(0xFF4CAF50)) // Verde
        n.contains("consejería") || n.contains("conserje") -> EstiloPoi(Icons.Default.Info, Color(0xFFFF9800)) // Naranja
        n.contains("secretaría") -> EstiloPoi(Icons.Default.BusinessCenter, Color(0xFF9C27B0)) // Morado
        n.contains("reprografía") || n.contains("copistería") -> EstiloPoi(Icons.Default.Print, Color(0xFF795548)) // Marrón
        n.contains("salón de actos") || n.contains("grados") -> EstiloPoi(Icons.Default.Groups, Color(0xFFE91E63)) // Rosa
        n.contains("bici") || n.contains("aparcabicis") -> EstiloPoi(Icons.Default.PedalBike, Color(0xFF4CAF50)) // Verde Eco
        n.contains("laboratorio")  -> EstiloPoi(Icons.Default.Engineering, Color(0xFF455A64)) // Gris (Tecnológico)
        n.contains("biblioteca") -> EstiloPoi(Icons.Default.MenuBook, Color(0xFF795548)) // Marrón biblioteca
        n.contains("informática") -> EstiloPoi(Icons.Default.Terminal, Color(0xFF009688))
        n.contains("computadores") -> EstiloPoi(Icons.Default.Memory, Color(0xFF009688))
        n.contains("electrónica") -> EstiloPoi(Icons.Default.SettingsInputComponent, Color(0xFF009688))
        n.contains("teleco") -> EstiloPoi(Icons.Default.SatelliteAlt, Color(0xFF009688))
        else -> EstiloPoi(Icons.Default.Place, Color.Gray) // Icono de chincheta gris por defecto
    }
}

data class Dimensiones(
    val ancho: Float,
    val largo: Float
)
data class Mapa (
    val mapaId: String,
    val nombre: String,
    val dimensiones: Dimensiones,
    val plantas: List<Planta>
)
