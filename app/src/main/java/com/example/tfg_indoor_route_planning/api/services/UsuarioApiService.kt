package com.example.tfg_indoor_route_planning.api.services

import com.example.tfg_indoor_route_planning.api.dto.AccesibilidadRequest
import com.example.tfg_indoor_route_planning.api.dto.FavoritosRequest
import com.example.tfg_indoor_route_planning.api.dto.FotoResponse
import com.example.tfg_indoor_route_planning.api.dto.UsuarioRespuesta
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface UsuarioApiService {
    @GET("/api/horarios/usuarios/{idUsuario}")
    suspend fun getUsuario(@Path("idUsuario") idUsuario: String): UsuarioRespuesta

    @Multipart
    @POST("api/usuario/foto")
    suspend fun subirFotoPerfil(
        @Header("Authorization") token: String,
        @Part foto: MultipartBody.Part
    ): Response<FotoResponse>

    @PUT("/api/horarios/usuarios/{idUsuario}/favoritos")
    suspend fun actualizarFavoritos(
        @Path("idUsuario") idUsuario: String,
        @Body request: FavoritosRequest
    ): Response<Void>

    @PUT("api/usuario/{idUsuario}/accesibilidad")
    suspend fun actualizarAccesibilidad(
        @Header("Authorization") token: String,
        @Path("idUsuario") idUsuario: String,
        @Body request: AccesibilidadRequest
    ): Response<Void>
}