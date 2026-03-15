package com.example.tfg_indoor_route_planning.models

/**
 * Representa un punto de interés o un cruce en el mapa para la planificación de rutas.
 */
data class Node(
    val id: String,
    val position: PointMeters,
    val name: String? = null,
    val plantaId: String,
    val neighbors: List<String> = emptyList()
) {
    // Lista de nodos adyacentes (conectados directamente)


    // Propiedades útiles para algoritmos de búsqueda (como A*)
    var gScore: Float = Float.POSITIVE_INFINITY
    var hScore: Float = 0f
    val fScore: Float get() = gScore + hScore
    var parent: Node? = null
}
