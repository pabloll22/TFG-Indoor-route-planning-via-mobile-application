package com.example.tfg_indoor_route_planning.repository

import com.example.tfg_indoor_route_planning.api.RetrofitClient
import com.example.tfg_indoor_route_planning.horario.AsignaturaInfo
import com.example.tfg_indoor_route_planning.horario.SesionRespuesta

object HorarioRepository {

    private var asignaturasCache: List<AsignaturaInfo>? = null

    /* VARIABLES DE CACHÉ PARA HORARIO (Caducidad 1 min)
    private var horarioCache: List<SesionRespuesta>? = null
    private var horarioCacheTime: Long = 0L
    private const val TIEMPO_CADUCIDAD_HORARIO = 60_000L
    */

    suspend fun getAsignaturas(forzarRecarga: Boolean = false): List<AsignaturaInfo> {
        if (!forzarRecarga && asignaturasCache != null) {
            return asignaturasCache!!
        }
        val response = RetrofitClient.apiService.getAsignaturas()
        asignaturasCache = response
        return response
    }

    /* OBTENER HORARIO CON CACHÉ
    suspend fun getHorario(usuarioId: String, forzarRecarga: Boolean = false): List<SesionRespuesta> {
        val tiempoActual = System.currentTimeMillis()
        val estaVigente = (tiempoActual - horarioCacheTime) < TIEMPO_CADUCIDAD_HORARIO

        if (!forzarRecarga && estaVigente && horarioCache != null) {
            return horarioCache!!
        }

        val response = RetrofitClient.apiService.getHorario(usuarioId)
        horarioCache = response
        horarioCacheTime = tiempoActual
        return response
    }
    */

    fun limpiarCache() {
        asignaturasCache = null
        /*
        horarioCache = null
        horarioCacheTime = 0L
        */
    }
}