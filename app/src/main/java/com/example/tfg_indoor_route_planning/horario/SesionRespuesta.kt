package com.example.tfg_indoor_route_planning.horario

data class SesionRespuesta(
    val _id: String,
    val asignaturaId: AsignaturaInfo,
    val profesorId: ProfesorInfo,
    val diaSemana: Int, // 1 = Lunes, 5 = Viernes
    val horaInicio: String,
    val horaFin: String,
    val nodoAulaId: String,
    val fechasCanceladas: List<String>? = null,
    val grupo: String,
    val aulaNombre: String? = null,
    val fechaEspecifica: String? = null
)

data class CancelarClaseRequest(val fecha: String)
