package com.example.tfg_indoor_route_planning.horario

data class AsignaturaInfo(
    val _id: String,
    val nombre: String,
    val curso: Int? = null,
    val cuatrimestre: Int,
    val facultadId: String,
    val grupos: List<String>? = emptyList()
)
