package com.example.tfg_indoor_route_planning.api.services

import com.example.tfg_indoor_route_planning.api.dto.MapaResumen
import com.example.tfg_indoor_route_planning.models.Mapa
import retrofit2.http.GET
import retrofit2.http.Path

interface MapApiService {

    @GET("api/mapas")
    suspend fun getTodosLosMapas(): List<MapaResumen>
    @GET("api/mapas/{mapa_id}")
    suspend fun getMapa(@Path("mapa_id") id: String): Mapa

}