package com.example.tfg_indoor_route_planning.api.services

import com.example.tfg_indoor_route_planning.api.dto.AuthResponse
import com.example.tfg_indoor_route_planning.api.dto.CambiarPasswordRequest
import com.example.tfg_indoor_route_planning.api.dto.LoginRequest
import com.example.tfg_indoor_route_planning.api.dto.RegistroRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AuthApiService {
    @POST("api/auth/login")
    suspend fun loginUsuario(@Body request: LoginRequest): AuthResponse

    @POST("api/auth/registro")
    suspend fun registrarUsuario(@Body request: RegistroRequest): Response<Void>

    @PUT("api/auth/cambiar-password/{idUsuario}")
    suspend fun cambiarPassword(
        @Header("Authorization") token: String,
        @Path("idUsuario") idUsuario: String,
        @Body request: CambiarPasswordRequest
    ): Response<Void>
}