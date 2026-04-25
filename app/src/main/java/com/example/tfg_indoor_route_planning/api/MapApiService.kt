package com.example.tfg_indoor_route_planning.api

import com.example.tfg_indoor_route_planning.models.Mapa
import retrofit2.http.GET
import retrofit2.http.Path


interface MapApiService {

    data class MapaResumen(
        val mapaId: String,
        val nombre: String
    )

    @GET("api/mapas")
    suspend fun getTodosLosMapas(): List<MapaResumen>
    @GET("api/mapas/{mapa_id}")
    suspend fun getMapa(@Path("mapa_id") id: String): Mapa

    companion object {
        /*const val BASE_URL = "http://172.16.142.119:3000"*/
        const val BASE_URL = "http://192.168.1.49:3000/"
    }
}