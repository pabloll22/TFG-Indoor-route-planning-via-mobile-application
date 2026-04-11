package com.example.tfg_indoor_route_planning.logic

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.tfg_indoor_route_planning.models.Node
import kotlin.math.*

data class NavInstruction(
    val text: String,
    val icon: ImageVector,
    val distance: Int
)

object NavigationHelper {

    fun generateInstructions(ruta: List<Node>): List<NavInstruction> {
        if (ruta.size < 2) return emptyList()

        val instructions = mutableListOf<NavInstruction>()

        for (i in 0 until ruta.size - 1) {
            val current = ruta[i]
            val next = ruta[i + 1]

            // Calculamos distancia entre estos dos nodos
            val dist = sqrt(
                (next.position.x - current.position.x).toDouble().pow(2) +
                        (next.position.y - current.position.y).toDouble().pow(2)
            ).toInt()

            // Si hay un tercer nodo, calculamos el giro
            if (i < ruta.size - 2) {
                val afterNext = ruta[i + 2]

                val angle1 = atan2((next.position.y - current.position.y).toDouble(), (next.position.x - current.position.x).toDouble())
                val angle2 = atan2((afterNext.position.y - next.position.y).toDouble(), (afterNext.position.x - next.position.x).toDouble())

                var deltaAngle = Math.toDegrees(angle2 - angle1)
                if (deltaAngle > 180) deltaAngle -= 360
                if (deltaAngle < -180) deltaAngle += 360

                val turnText = when {
                    deltaAngle > 45 -> "Gira a la derecha hacia ${afterNext.name}"
                    deltaAngle < -45 -> "Gira a la izquierda hacia ${afterNext.name}"
                    else -> "Continúa recto hacia ${afterNext.name}"
                }

                instructions.add(NavInstruction(turnText, Icons.Default.ArrowForward, dist))
            } else {
                instructions.add(NavInstruction("Llegarás a tu destino: ${next.name}", Icons.Default.LocationOn, dist))
            }
        }
        return instructions
    }
}