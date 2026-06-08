package com.example.tfg_indoor_route_planning.logic

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Elevator
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Stairs
import androidx.compose.material.icons.filled.Straight
import androidx.compose.material.icons.filled.TurnLeft
import androidx.compose.material.icons.filled.TurnRight
import androidx.compose.material.icons.filled.TurnSlightLeft
import androidx.compose.material.icons.filled.TurnSlightRight
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

            val dist = sqrt(
                (next.position.x - current.position.x).toDouble().pow(2) +
                        (next.position.y - current.position.y).toDouble().pow(2)
            ).toInt()

            if (current.plantaId != next.plantaId) {
                // Averiguamos si sube o baja comparando el último número del ID de la planta
                val direccion = if ((next.plantaId.lastOrNull()?.digitToIntOrNull() ?: 0) >
                    (current.plantaId.lastOrNull()?.digitToIntOrNull() ?: 0))
                    "Sube" else "Baja"

                // Comprobamos si el origen o el destino es un ascensor
                val esAscensor = current.tipo == "ASCENSOR" || next.tipo == "ASCENSOR"

                val medio = if (esAscensor) "el ascensor" else "las escaleras"
                val iconInterplanta = if (esAscensor) Icons.Default.Elevator else Icons.Default.Stairs

                instructions.add(
                    NavInstruction(
                        text = "$direccion por $medio a la planta ${next.plantaId.replace("planta_", "")}",
                        icon = iconInterplanta,
                        distance = dist
                    )
                )
                continue
            }

            if (i < ruta.size - 2) {
                val afterNext = ruta[i + 2]

                // Cálculo de ángulos
                val angle1 = atan2((next.position.y - current.position.y).toDouble(), (next.position.x - current.position.x).toDouble())
                val angle2 = atan2((afterNext.position.y - next.position.y).toDouble(), (afterNext.position.x - next.position.x).toDouble())

                var deltaAngle = Math.toDegrees(angle2 - angle1)
                if (deltaAngle > 180) deltaAngle -= 360
                if (deltaAngle < -180) deltaAngle += 360

                // ASIGNACIÓN DINÁMICA DE TEXTO E ICONO
                val (turnText, icon) = when {
                    deltaAngle > 60 -> {
                        "Gira a la derecha hacia ${afterNext.name}" to Icons.Default.TurnRight
                    }
                    deltaAngle < -60 -> {
                        "Gira a la izquierda hacia ${afterNext.name}" to Icons.Default.TurnLeft
                    }
                    // Giros suaves
                    deltaAngle in 20.0..60.0 -> {
                        "Giro suave a la derecha" to Icons.Default.TurnSlightRight
                    }
                    deltaAngle in -60.0..-20.0 -> {
                        "Giro suave a la izquierda" to Icons.Default.TurnSlightLeft
                    }
                    else -> {
                        "Continúa recto por el pasillo" to Icons.Default.Straight
                    }
                }

                instructions.add(NavInstruction(turnText, icon, dist))
            } else {
                // Destino
                instructions.add(NavInstruction("Llegarás a tu destino: ${next.name}", Icons.Default.LocationOn, dist))
            }
        }

        val finalInstructions = instructions.toMutableList()
        var accumulatedDistance = 0

        for (i in finalInstructions.indices.reversed()) {
            val inst = finalInstructions[i]

            if (inst.icon != Icons.Default.Straight) {
                // Si hay un giro, escaleras o destino, reseteamos el acumulador
                // guardando solo la distancia de este tramo antes del giro.
                accumulatedDistance = inst.distance
            } else {
                // Si es un nodo recto, sumamos su distancia a la mochila del pasillo
                accumulatedDistance += inst.distance

                // Actualizamos la instrucción para que muestre la cuenta atrás sumada
                finalInstructions[i] = NavInstruction(
                    text = "Continúa recto por el pasillo durante ${accumulatedDistance}m",
                    icon = inst.icon,
                    distance = accumulatedDistance
                )
            }
        }
        return finalInstructions
    }
}