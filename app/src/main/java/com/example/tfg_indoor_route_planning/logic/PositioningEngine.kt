package com.example.tfg_indoor_route_planning.logic

import android.bluetooth.le.ScanResult
import android.util.Log
import com.example.tfg_indoor_route_planning.MainActivity
import com.example.tfg_indoor_route_planning.models.Node
import com.example.tfg_indoor_route_planning.models.PointMeters
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class PositioningEngine(private val knownBeacons: Map<String, PointMeters>) {

    /**
     * Calcula la posición del usuario basándose en los beacons detectados.
     */
    fun calculateUserPosition(activeBeacons: Map<String, MainActivity.BeaconState>): PointMeters? {

        val validBeacons = activeBeacons.entries
            .filter { it.value.smoothedRssi > -90 } // filtro de ruido
            .sortedByDescending { it.value.smoothedRssi }
            .take(5)

        if (validBeacons.size < 3) return null

        val beaconData = validBeacons.mapNotNull { entry ->
            val pos = knownBeacons[entry.key] ?: return@mapNotNull null
            val distRaw = PositionCalculator.calculateDistance(entry.value.smoothedRssi)

            //CLAMP de distancia
            val dist = distRaw.coerceIn(0.5, 8.0)

            Pair(pos, dist)
        }

        if (beaconData.size < 3) return null

        // 1. CENTROIDE PESADO (ROBUSTO)
        val centroid = weightedCentroid(beaconData)

        //2. TRILATERACIÓN (solo con 3 mejores)
        val trilateration = tryTrilateration(beaconData)

        //3. FUSIÓN (70% centroid, 30% trilateración)
        val fused = if (trilateration != null) {
            PointMeters(
                (0.4f * centroid.x + 0.6f * trilateration.x),
                (0.4f * centroid.y + 0.6f * trilateration.y)
            )
        } else {
            centroid
        }

        return fused
    }

    /**
     * Encuentra el nodo más cercano a una posición dada.
     */
    fun findNearestNode(pos: PointMeters, nodes: List<Node>): Node? {
        return nodes.minByOrNull { node ->
            sqrt((node.position.x - pos.x).toDouble().pow(2.0) + (node.position.y - pos.y).toDouble().pow(2.0))
        }
    }

    private fun weightedCentroid(beacons: List<Pair<PointMeters, Double>>): PointMeters {
        var sumX = 0.0
        var sumY = 0.0
        var sumW = 0.0

        for ((pos, dist) in beacons) {
            val weight = 1.0 / (dist * dist + 0.1) // evitar división por 0
            sumX += pos.x * weight
            sumY += pos.y * weight
            sumW += weight
        }

        return PointMeters(
            (sumX / sumW).toFloat(),
            (sumY / sumW).toFloat()
        )
    }

    private fun tryTrilateration(beacons: List<Pair<PointMeters, Double>>): PointMeters? {
        if (beacons.size < 3) return null

        val (p1, d1) = beacons[0]
        val (p2, d2) = beacons[1]
        val (p3, d3) = beacons[2]

        val result = PositionCalculator.trilaterate(p1, d1, p2, d2, p3, d3)

        //validación (dentro del mapa)
        return if (result != null &&
            result.x in 0f..12f &&
            result.y in 0f..11f
        ) result else null
    }
}
