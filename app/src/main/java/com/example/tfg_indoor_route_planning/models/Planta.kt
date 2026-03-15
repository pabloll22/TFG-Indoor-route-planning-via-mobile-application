package com.example.tfg_indoor_route_planning.models

data class Planta(
    val plantaId: String,
    val nombre: String,
    val nivel: Int,
    val imagenBase64: String,       // Cada planta tiene su imagen
    val widthMeters: Float,         // Cada planta tiene sus medidas
    val heightMeters: Float,
    val knownBeacons: Map<String, PointMeters>,
    val nodos: List<Node>,
    val pois: List<POI>
)
