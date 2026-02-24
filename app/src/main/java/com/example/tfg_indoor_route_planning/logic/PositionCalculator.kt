package com.example.tfg_indoor_route_planning.logic

import com.example.tfg_indoor_route_planning.models.PointMeters
import kotlin.math.abs
import kotlin.math.pow

object PositionCalculator {
    /**
     * Convierte RSSI a distancia estimada en metros.
     */
    fun calculateDistance(rssi: Int, txPower: Int = -50, n: Double = 4.0): Double {
        return 10.0.pow((txPower - rssi) / (10 * n))
    }

    /**
     * Algoritmo de trilateración para 3 puntos.
     */
    fun trilaterate(
        p1: PointMeters, d1: Double,
        p2: PointMeters, d2: Double,
        p3: PointMeters, d3: Double
    ): PointMeters? {
        val a = 2 * (p2.x - p1.x)
        val b = 2 * (p2.y - p1.y)
        val c = d1.pow(2) - d2.pow(2) - p1.x.pow(2) + p2.x.pow(2) - p1.y.pow(2) + p2.y.pow(2)
        val d = 2 * (p3.x - p2.x)
        val e = 2 * (p3.y - p2.y)
        val f = d2.pow(2) - d3.pow(2) - p2.x.pow(2) + p3.x.pow(2) - p2.y.pow(2) + p3.y.pow(2)

        val det = (a * e) - (b * d)
        if (abs(det) < 1e-6) return null // Evita división por cero si son colineales

        val x = ((c * e) - (f * b)) / det
        val y = ((a * f) - (c * d)) / det

        return PointMeters(x.toFloat(), y.toFloat())
    }
}
