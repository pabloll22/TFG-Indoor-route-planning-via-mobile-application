package com.example.tfg_indoor_route_planning.repository

import com.example.tfg_indoor_route_planning.api.MapaResumen
import com.example.tfg_indoor_route_planning.api.RetrofitClient
import com.example.tfg_indoor_route_planning.models.Mapa

object MapaRepository {

    private var mapasResumenCache: List<MapaResumen>? = null

    private var mapasDetalleCache: MutableMap<String, Mapa> = mutableMapOf()
    private var mapasDetalleCacheTime: MutableMap<String, Long> = mutableMapOf()
    private const val TIEMPO_CADUCIDAD_MAPA = 300_000L // 5 minutos

    suspend fun getTodosLosMapas(forzarRecarga: Boolean = false): List<MapaResumen> {
        if (!forzarRecarga && mapasResumenCache != null) {
            return mapasResumenCache!!
        }
        val response = RetrofitClient.apiService.getTodosLosMapas()
        mapasResumenCache = response
        return response
    }

    suspend fun getMapa(id: String, forzarRecarga: Boolean = false): Mapa {
        val tiempoGuardado = mapasDetalleCacheTime[id] ?: 0L
        val tiempoActual = System.currentTimeMillis()
        val estaVigente = (tiempoActual - tiempoGuardado) < TIEMPO_CADUCIDAD_MAPA

        if (!forzarRecarga && estaVigente && mapasDetalleCache.containsKey(id)) {
            return mapasDetalleCache[id]!!
        }

        val response = RetrofitClient.apiService.getMapa(id)
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