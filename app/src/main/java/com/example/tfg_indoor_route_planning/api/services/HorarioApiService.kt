package com.example.tfg_indoor_route_planning.api.services

import com.example.tfg_indoor_route_planning.api.dto.CancelarClaseRequest
import com.example.tfg_indoor_route_planning.api.dto.CrearClaseRequest
import com.example.tfg_indoor_route_planning.api.dto.MatriculaRequest
import com.example.tfg_indoor_route_planning.api.dto.SesionRespuesta
import com.example.tfg_indoor_route_planning.horario.AsignaturaInfo
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface HorarioApiService {
    @GET("/api/horarios/usuario/{usuarioId}/horario")
    suspend fun getHorario(@Path("usuarioId") usuarioId: String): List<SesionRespuesta>

    @GET("/api/horarios/aula/{nodoId}")
    suspend fun getHorarioAulaHoy(@Path("nodoId") nodoId: String): List<SesionRespuesta>

    @POST("/api/horarios/sesion/{sesionId}/cancelar")
    suspend fun cancelarClase(@Path("sesionId") sesionId: String, @Body request: CancelarClaseRequest)

    @POST("/api/horarios/sesion/{sesionId}/restaurar")
    suspend fun restaurarClase(@Path("sesionId") sesionId: String, @Body request: CancelarClaseRequest)

    @POST("/api/horarios/sesion/crear")
    suspend fun crearClase(@Body request: CrearClaseRequest): SesionRespuesta

    @GET("api/horarios/asignaturas")
    suspend fun getAsignaturas(): List<AsignaturaInfo>

    @PUT("api/horarios/usuarios/{idUsuario}/matricula")
    suspend fun actualizarMatricula(
        @Header("Authorization") token: String,
        @Path("idUsuario") idUsuario: String,
        @Body matricula: List<MatriculaRequest>
    ): Response<Unit>
}