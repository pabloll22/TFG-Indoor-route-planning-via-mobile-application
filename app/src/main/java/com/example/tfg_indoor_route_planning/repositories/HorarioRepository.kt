package com.example.tfg_indoor_route_planning.repositories

import com.example.tfg_indoor_route_planning.api.RetrofitClient
import com.example.tfg_indoor_route_planning.api.dto.SesionRespuesta
import com.example.tfg_indoor_route_planning.horario.AsignaturaInfo

object HorarioRepository {

    private var asignaturasCache: List<AsignaturaInfo>? = null

     //VARIABLES DE CACHÉ PARA HORARIO (Caducidad 1 min)
    private var horarioCache: List<SesionRespuesta>? = null
    private var horarioCacheTime: Long = 0L
    private const val TIEMPO_CADUCIDAD_HORARIO = 60_000L


    suspend fun getAsignaturas(forzarRecarga: Boolean = false): List<AsignaturaInfo> {
        if (!forzarRecarga && asignaturasCache != null) {
            return asignaturasCache!!
        }
        val response = RetrofitClient.horarioService.getAsignaturas()
        asignaturasCache = response
        return response
    }

    //OBTENER HORARIO CON CACHÉ
    suspend fun getHorario(usuarioId: String, forzarRecarga: Boolean = false): List<SesionRespuesta> {
        val tiempoActual = System.currentTimeMillis()
        val estaVigente = (tiempoActual - horarioCacheTime) < TIEMPO_CADUCIDAD_HORARIO

        if (!forzarRecarga && estaVigente && horarioCache != null) {
            return horarioCache!!
        }

        val response = RetrofitClient.horarioService.getHorario(usuarioId)
        horarioCache = response
        horarioCacheTime = tiempoActual
        return response
    }

    fun marcarClaseCanceladaLocalmente(sesionId: String, fechaCancelada: String) {
        horarioCache = horarioCache?.map { sesion ->
            if (sesion._id == sesionId) {
                // Añadimos la fecha a la lista de canceladas de esa sesión concreta
                val nuevasFechas = sesion.fechasCanceladas?.toMutableList()
                nuevasFechas?.contains(fechaCancelada)?.let { if (!it) nuevasFechas?.add(fechaCancelada) }
                sesion.copy(fechasCanceladas = nuevasFechas)
            } else {
                sesion
            }
        }
    }

    // Para cuando restaura la clase
    fun restaurarClaseLocalmente(sesionId: String, fechaRestaurada: String) {
        horarioCache = horarioCache?.map { sesion ->
            if (sesion._id == sesionId) {
                val nuevasFechas = sesion.fechasCanceladas?.toMutableList() ?: mutableListOf()
                nuevasFechas.remove(fechaRestaurada) // La quitamos de la lista negra
                sesion.copy(fechasCanceladas = nuevasFechas)
            } else {
                sesion
            }
        }
    }

    fun limpiarCache() {
        asignaturasCache = null
        horarioCache = null
        horarioCacheTime = 0L
    }
}