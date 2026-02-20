package com.example.tfg_indoor_route_planning.logic

import android.bluetooth.le.ScanResult
import com.example.tfg_indoor_route_planning.models.Node
import com.example.tfg_indoor_route_planning.models.PointMeters
import kotlin.math.pow
import kotlin.math.sqrt

class PositioningEngine(private val knownBeacons: Map<String, PointMeters>) {

    /**
     * Calcula la posición del usuario basándose en los beacons detectados.
     */
    fun calculateUserPosition(devices: List<ScanResult>): PointMeters? {
        val nearbyBeacons = devices
            .filter { knownBeacons.containsKey(it.device.address) }
            .sortedByDescending { it.rssi }
            .take(3)

        if (nearbyBeacons.size < 3) return null

        val p1 = knownBeacons[nearbyBeacons[0].device.address]!!
        val p2 = knownBeacons[nearbyBeacons[1].device.address]!!
        val p3 = knownBeacons[nearbyBeacons[2].device.address]!!

        val d1 = PositionCalculator.calculateDistance(nearbyBeacons[0].rssi)
        val d2 = PositionCalculator.calculateDistance(nearbyBeacons[1].rssi)
        val d3 = PositionCalculator.calculateDistance(nearbyBeacons[2].rssi)

        return PositionCalculator.trilaterate(p1, d1, p2, d2, p3, d3)
    }

    /**
     * Encuentra el nodo más cercano a una posición dada.
     */
    fun findNearestNode(pos: PointMeters, nodes: List<Node>): Node? {
        return nodes.minByOrNull { node ->
            sqrt((node.position.x - pos.x).toDouble().pow(2.0) + (node.position.y - pos.y).toDouble().pow(2.0))
        }
    }
}
