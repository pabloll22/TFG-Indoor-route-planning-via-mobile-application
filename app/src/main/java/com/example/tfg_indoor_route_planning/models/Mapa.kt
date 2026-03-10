package com.example.tfg_indoor_route_planning.models

data class POI(
    val id: String,
    val nombre: String,
    val nodoId: String
)
data class Mapa (
    val mapaId: String,
    val nombre: String,
    val imagenBase64: String,
    val widthMeters: Float,
    val heightMeters: Float,
    val knownBeacons: Map<String, PointMeters>,
    val nodos: List<Node>,
    val pois: List<POI>
)
