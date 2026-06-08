package com.example.tfg_indoor_route_planning.logic

import android.content.ContentValues.TAG
import android.util.Log
import com.example.tfg_indoor_route_planning.models.Node
import com.example.tfg_indoor_route_planning.models.PointMeters
import java.util.PriorityQueue
import kotlin.math.pow
import kotlin.math.roundToInt
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
    private val nodeMap = nodesList.associateBy { it.id }

    /**
     * Calcula el nodo lógico más coherente basándose en la posición física.
     */
    fun snapToGraph(rawPosition: PointMeters, plantaActualId: String, noHayRutaCalculada: Boolean): Node? {
        // CASO 1: Arranque en frío. No tenemos nodo previo.

        val saltoPermitido = (currentUserNode?.plantaId != plantaActualId) && noHayRutaCalculada
        Log.d(TAG, "SALTO PERMITIDO: $saltoPermitido")

        if (currentUserNode == null || saltoPermitido) {
            val nearest = nodesList
                .filter { it.plantaId == plantaActualId }
                .minByOrNull { node -> calculateDistance(rawPosition, node.position) }
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

            //Si el mejor candidato está en la planta que estamos viendo,
            // y nosotros venimos de otra planta, forzamos el salto sin esperar.
            val esSaltoDePlanta = (bestCandidate.plantaId == plantaActualId) && (current.plantaId != plantaActualId)

            // Solo cambiamos si la mejora es sustancial (supera el umbral)
            // O si la distancia es muy grande (salvavidas por si el usuario corrió)
            if (esSaltoDePlanta || distToBest < (distToCurrent - HYSTERESIS_THRESHOLD)) {
                currentUserNode = bestCandidate
            }
        }

        return currentUserNode
    }

    /**
     * Calcula la ruta más corta entre dos nodos usando el algoritmo A*
     */
    fun findPath(startId: String, targetId: String?): List<Node> {
        val startNode = nodeMap[startId] ?: return emptyList()
        val targetNode = nodeMap[targetId] ?: return emptyList()

        // 1. Reiniciamos los valores de todos los nodos (por si calculamos varias rutas)
        nodesList.forEach {
            it.gScore = Float.POSITIVE_INFINITY
            it.hScore = 0f
            it.parent = null
        }

        // 2. Cola de prioridad que ordena los nodos por su fScore (el menor primero)
        val openSet = PriorityQueue<Node>(compareBy { it.fScore })
        val closedSet = mutableSetOf<String>() // Nodos ya evaluados

        // 3. Inicializamos el nodo de salida
        startNode.gScore = 0f
        startNode.hScore = calculateHeuristic(startNode, targetNode)
        openSet.add(startNode)

        // 4. Bucle principal del algoritmo A*
        while (openSet.isNotEmpty()) {
            val current = openSet.poll() ?: break

            if (current.id == targetId) {
                return reconstructPath(current)
            }

            closedSet.add(current.id)

            // Evaluamos a los vecinos
            for (neighborId in current.neighbors) {
                if (closedSet.contains(neighborId)) continue

                val neighbor = nodeMap[neighborId] ?: continue

                // Distancia real entre el nodo actual y este vecino
                val tentativeGScore = current.gScore + calculateDistance(current, neighbor)

                // Si hemos encontrado un camino mejor hacia este vecino...
                if (tentativeGScore < neighbor.gScore) {
                    neighbor.parent = current
                    neighbor.gScore = tentativeGScore
                    neighbor.hScore = calculateHeuristic(neighbor, targetNode)

                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor)
                    }
                }
            }
        }

        // Si la cola se vacía y no hemos devuelto la ruta, es que no hay camino posible
        return emptyList()
    }

    // Reconstruye el camino yendo hacia atrás desde el destino hasta el inicio
    private fun reconstructPath(endNode: Node): List<Node> {
        val path = mutableListOf<Node>()
        var current: Node? = endNode
        while (current != null) {
            path.add(current)
            current = current.parent
        }
        return path.reversed() // Le damos la vuelta para que vaya de Inicio a Fin
    }

    // Distancia Euclidiana (Línea recta) entre dos nodos
    private fun calculateDistance(nodeA: Node, nodeB: Node): Float {
        if (nodeA.plantaId != nodeB.plantaId) {
            if (UserSession.rutasAccesibles) {

                if (nodeA.tipo == "ESCALERA" || nodeB.tipo == "ESCALERA") {
                    return Float.POSITIVE_INFINITY // Prohibido pasar por aquí
                }
            }
            return 5.0f //subir/bajar escaleras
        }
        val dx = nodeA.position.x - nodeB.position.x
        val dy = nodeA.position.y - nodeB.position.y
        return sqrt(dx.pow(2) + dy.pow(2))
    }

    // La heurística h(n) en A* suele ser la misma que la distancia euclidiana al destino
    private fun calculateHeuristic(node: Node, target: Node): Float {
        return calculateDistance(node, target)
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

    // Función que recibe la ruta de nodos y devuelve los metros redondeados
    fun calcularDistanciaMetros(ruta: List<Node>): Int {
        if (ruta.size < 2) return 0

        var distanciaTotalPixeles = 0f

        // Sumamos la distancia de cada segmento de la ruta
        for (i in 0 until ruta.size - 1) {
            val nodoActual = ruta[i]
            val nodoSiguiente = ruta[i + 1]

            val dx = nodoSiguiente.position.x - nodoActual.position.x
            val dy = nodoSiguiente.position.y - nodoActual.position.y

            // Pitágoras puro y duro
            val distanciaSegmento = sqrt((dx * dx) + (dy * dy))
            distanciaTotalPixeles += distanciaSegmento
        }

        // FACTOR DE CONVERSIÓN
        // Ejemplo: Si 100 píxeles/unidades de tu nodo equivalen a 1 metro real, tu factor es 0.01f
        val factorDeConversion = 1f

        val distanciaEnMetros = distanciaTotalPixeles * factorDeConversion

        // Redondeamos para que no diga "14.532 metros", sino "15 metros"
        return distanciaEnMetros.roundToInt()
    }
}