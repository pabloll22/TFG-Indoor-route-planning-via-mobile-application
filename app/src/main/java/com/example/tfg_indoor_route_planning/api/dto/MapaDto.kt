package com.example.tfg_indoor_route_planning.api.dto

/**
 * DTO ligero utilizado para listar los edificios disponibles en el selector principal.
 */
data class MapaResumen(
    val mapaId: String,
    val nombre: String
)

/**
 * DTO que modela la información básica de un punto de interés guardado en favoritos.
 */
data class PoiFavorito(
    val idPoi: String,
    val nombrePoi: String,
    val facultad: String
)