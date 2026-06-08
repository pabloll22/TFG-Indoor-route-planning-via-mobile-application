package com.example.tfg_indoor_route_planning.repositories

import com.example.tfg_indoor_route_planning.api.RetrofitClient
import com.example.tfg_indoor_route_planning.api.dto.MapaResumen
import com.example.tfg_indoor_route_planning.models.Mapa

object MapaRepository {

    private var mapasResumenCache: List<MapaResumen>? = null
    private var mapasResumenCacheTime: Long = 0L

    private var mapasDetalleCache: MutableMap<String, Mapa> = mutableMapOf()
    private var mapasDetalleCacheTime: MutableMap<String, Long> = mutableMapOf()

    // 3 horas en milisegundos (3 * 60 * 60 * 1000)
    private const val TIEMPO_CADUCIDAD_MAPA = 3_600_000L // 3 horas

    suspend fun getTodosLosMapas(forzarRecarga: Boolean = false): List<MapaResumen> {
        val tiempoActual = System.currentTimeMillis()
        val estaVigente = (tiempoActual - mapasResumenCacheTime) < TIEMPO_CADUCIDAD_MAPA

        if (!forzarRecarga && estaVigente && mapasResumenCache != null) {
            return mapasResumenCache!!
        }

        val response = RetrofitClient.mapaService.getTodosLosMapas()
        mapasResumenCache = response
        mapasResumenCacheTime = tiempoActual
        return response
    }

    suspend fun getMapa(id: String, forzarRecarga: Boolean = false): Mapa {
        val tiempoGuardado = mapasDetalleCacheTime[id] ?: 0L
        val tiempoActual = System.currentTimeMillis()
        val estaVigente = (tiempoActual - tiempoGuardado) < TIEMPO_CADUCIDAD_MAPA

        if (!forzarRecarga && estaVigente && mapasDetalleCache.containsKey(id)) {
            return mapasDetalleCache[id]!!
        }

        val response = RetrofitClient.mapaService.getMapa(id)
        mapasDetalleCache[id] = response
        mapasDetalleCacheTime[id] = tiempoActual
        return response
    }

    fun limpiarCache() {
        mapasResumenCache = null
        mapasDetalleCache.clear()
        mapasDetalleCacheTime.clear()
    }
}