package com.example.tfg_indoor_route_planning.models

/**
 * Representa un punto de interés o un cruce en el mapa para la planificación de rutas.
 */
data class Node(
    val id: String,
    val position: PointMeters,
    val name: String? = null
) {
    // Lista de nodos adyacentes (conectados directamente)
    val neighbors = mutableListOf<Node>()

    // Propiedades útiles para algoritmos de búsqueda (como A*)
    var gScore: Float = Float.POSITIVE_INFINITY
    var hScore: Float = 0f
    val fScore: Float get() = gScore + hScore
    var parent: Node? = null

    fun addNeighbor(node: Node) {
        if (!neighbors.contains(node)) {
            neighbors.add(node)
        }
    }
}
