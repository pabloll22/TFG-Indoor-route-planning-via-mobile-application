package com.example.tfg_indoor_route_planning.api.dto

import com.example.tfg_indoor_route_planning.horario.AsignaturaInfo
import com.example.tfg_indoor_route_planning.horario.ProfesorInfo

/**
 * DTO que vincula una asignatura seleccionada con su respectivo grupo horario de asistencia.
 */
data class MatriculaRequest(
    val asignaturaId: String,
    val grupo: String
)

data class CancelarClaseRequest(val fecha: String)

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