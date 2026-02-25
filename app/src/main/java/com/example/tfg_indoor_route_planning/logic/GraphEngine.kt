package com.example.tfg_indoor_route_planning.logic

import com.example.tfg_indoor_route_planning.models.Node
import com.example.tfg_indoor_route_planning.models.PointMeters
import kotlin.math.pow
import kotlin.math.sqrt

class GraphEngine(private val nodesList: List<Node>) {

    // Optimización: Creamos un mapa para buscar vecinos por ID instantáneamente (O(1))
    // en lugar de recorrer la lista cada vez.
    private val nodesMap: Map<String, Node> = nodesList.associateBy { it.id }

    // Estado actual: El nodo al que el usuario está "imantado"
    var currentUserNode: Node? = null
        private set // Solo se puede modificar desde dentro de esta clase

    // Configuración: Umbral de histéresis (en metros)
    // El usuario debe estar 0.5m más cerca del nuevo nodo que del actual para cambiar.
    private val HYSTERESIS_THRESHOLD = 0.5f

    /**
     * Calcula el nodo lógico más coherente basándose en la posición física.
     */
    fun snapToGraph(rawPosition: PointMeters): Node? {
        // CASO 1: Arranque en frío. No tenemos nodo previo.
        // Buscamos el nodo más cercano de TODO el mapa.
        if (currentUserNode == null) {
            val nearest = nodesList.minByOrNull { node ->
                calculateDistance(rawPosition, node.position)
            }
            currentUserNode = nearest
            return nearest
        }

        // CASO 2: Ya estamos en un nodo. Buscamos solo entre VECINOS + ACTUAL.
        val current = currentUserNode!!

        // Obtenemos los objetos Node de los vecinos
        val candidates = current.neighbors.mapNotNull { neighborId ->
            nodesMap[neighborId]
        }.toMutableList()

        // Nos añadimos a nosotros mismos como candidatos (quedarse quieto)
        candidates.add(current)

        // Buscamos cuál es el candidato matemáticamente más cercano
        val bestCandidate = candidates.minByOrNull { node ->
            calculateDistance(rawPosition, node.position)
        } ?: current

        // LÓGICA DE HISTÉRESIS (Anti-Rebote)
        // Si el mejor candidato es diferente al actual, verificamos si merece la pena cambiar.
        if (bestCandidate.id != current.id) {
            val distToCurrent = calculateDistance(rawPosition, current.position)
            val distToBest = calculateDistance(rawPosition, bestCandidate.position)

            // Solo cambiamos si la mejora es sustancial (supera el umbral)
            // O si la distancia es muy grande (salvavidas por si el usuario corrió)
            if (distToBest < (distToCurrent - HYSTERESIS_THRESHOLD)) {
                currentUserNode = bestCandidate
            }
        }

        return currentUserNode
    }

    /**
     * Resetea el estado (por si el usuario reinicia la navegación)
     */
    fun reset() {
        currentUserNode = null
    }

    private fun calculateDistance(p1: PointMeters, p2: PointMeters): Float {
        return sqrt((p1.x - p2.x).pow(2) + (p1.y - p2.y).pow(2))
    }
}