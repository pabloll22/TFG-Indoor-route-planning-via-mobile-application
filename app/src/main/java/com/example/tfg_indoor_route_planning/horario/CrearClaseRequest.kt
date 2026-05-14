package com.example.tfg_indoor_route_planning.horario

data class CrearClaseRequest(
    val asignaturaId: String,
    val profesorId: String,
    val diaSemana: Int,
    val horaInicio: String,
    val horaFin: String,
    val nodoAulaId: String,
    val aulaNombre: String,
    val grupo: String,
    val fechaEspecifica: String? = null
)