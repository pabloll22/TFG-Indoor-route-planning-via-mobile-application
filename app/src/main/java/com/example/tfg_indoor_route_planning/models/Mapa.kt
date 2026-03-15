package com.example.tfg_indoor_route_planning.models

data class POI(
    val id: String,
    val nombre: String,
    val nodoId: String,
    val plantaId: String
)
data class Mapa (
    val mapaId: String,
    val nombre: String,
    val plantas: List<Planta>
)
