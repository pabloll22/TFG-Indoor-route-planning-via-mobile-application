package com.example.tfg_indoor_route_planning.logic

import com.example.tfg_indoor_route_planning.models.Node

data class RutaDividida(
    val tramoPlantaOrigen: List<Node>,
    val tramoPlantaDestino: List<Node>,
    val hayCambioDePlanta: Boolean
)

fun dividirRutaPorPlantas(rutaCompleta: List<Node>): RutaDividida {
    if (rutaCompleta.isEmpty()) return RutaDividida(emptyList(), emptyList(), false)

    val plantaDelInicio = rutaCompleta.first().plantaId
    val tramo1 = mutableListOf<Node>()
    val tramo2 = mutableListOf<Node>()

    var haCambiadoDePlanta = false

    for (nodo in rutaCompleta) {
        if (nodo.plantaId == plantaDelInicio && !haCambiadoDePlanta) {
            // Mientras sigamos en la planta donde empezamos, lo guardamos en el Tramo 1
            tramo1.add(nodo)
        } else {
            // En cuanto detectamos un nodo de otra planta, metemos el resto en el Tramo 2
            haCambiadoDePlanta = true
            tramo2.add(nodo)
        }
    }

    return RutaDividida(tramo1, tramo2, haCambiadoDePlanta)
}
