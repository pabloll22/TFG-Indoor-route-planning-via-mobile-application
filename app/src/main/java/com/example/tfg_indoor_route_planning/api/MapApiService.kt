package com.example.tfg_indoor_route_planning.api

import com.example.tfg_indoor_route_planning.horario.AsignaturaInfo
import com.example.tfg_indoor_route_planning.horario.CancelarClaseRequest
import com.example.tfg_indoor_route_planning.horario.CrearClaseRequest
import com.example.tfg_indoor_route_planning.horario.SesionRespuesta
import com.example.tfg_indoor_route_planning.models.Mapa
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Header

data class MapaResumen(
    val mapaId: String,
    val nombre: String
)

data class PoiFavorito(
    val idPoi: String,
    val nombrePoi: String,
    val facultad: String
)
data class FavoritosRequest(
    val favoritos: List<PoiFavorito>
)

data class UsuarioRespuesta(
    val idUsuario: String,
    val nombre: String,
    val rol: String,
    val poisFavoritos: List<PoiFavorito> = emptyList(),
    @com.google.gson.annotations.SerializedName("foto_url")
    val foto_url: String? = "",
    val asignaturasMatriculadas: List<MatriculaRequest> = emptyList()
)

data class LoginRequest(val idUsuario: String, val password: String)
data class RegistroRequest(val idUsuario: String, val nombre: String, val password: String, val rol: String)

// (Token + Datos)
data class AuthResponse(
    val token: String,
    val usuario: UsuarioRespuesta
)

data class FotoResponse(
    val mensaje: String,
    val url: String
)

data class MatriculaRequest(
    val asignaturaId: String,
    val grupo: String
)
interface MapApiService {

    @GET("api/mapas")
    suspend fun getTodosLosMapas(): List<MapaResumen>
    @GET("api/mapas/{mapa_id}")
    suspend fun getMapa(@Path("mapa_id") id: String): Mapa

    @GET("/api/horarios/aula/{nodoId}")
    suspend fun getHorarioAulaHoy(
        @Path("nodoId") nodoId: String
    ): List<SesionRespuesta>

    @GET("/api/horarios/usuario/{usuarioId}/horario")
    suspend fun getHorario(@Path("usuarioId") usuarioId: String): List<SesionRespuesta>

    // Cancelar una clase
    @POST("/api/horarios/sesion/{sesionId}/cancelar")
    suspend fun cancelarClase(
        @Path("sesionId") sesionId: String,
        @Body request: CancelarClaseRequest
    )

    @POST("/api/horarios/sesion/{sesionId}/restaurar")
    suspend fun restaurarClase(
        @Path("sesionId") sesionId: String,
        @Body request: CancelarClaseRequest
    )

    @POST("/api/horarios/sesion/crear")
    suspend fun crearClase(
        @Body request: CrearClaseRequest
    ): SesionRespuesta

    @PUT("/api/horarios/usuarios/{idUsuario}/favoritos")
    suspend fun actualizarFavoritos(
        @Path("idUsuario") idUsuario: String,
        @Body request: FavoritosRequest
    ): retrofit2.Response<Void> // Usamos Void si no nos importa leer la respuesta del servidor, solo que dé OK

    @GET("/api/horarios/usuarios/{idUsuario}") // Fíjate en el prefijo que estés usando
    suspend fun getUsuario(@Path("idUsuario") idUsuario: String): UsuarioRespuesta

    @POST("api/auth/login")
    suspend fun loginUsuario(@Body request: LoginRequest): AuthResponse

    @POST("api/auth/registro")
    suspend fun registrarUsuario(@Body request: RegistroRequest): retrofit2.Response<Void>

    @Multipart // Indica que vamos a enviar un archivo binario
    @POST("api/usuario/foto")
    suspend fun subirFotoPerfil(
        @Header("Authorization") token: String,
        @Part foto: MultipartBody.Part //  archivo de la imagen
    ): Response<FotoResponse>

    // 1. Obtener la lista de todas las asignaturas disponibles para matricularse
    @GET("api/horarios/asignaturas") // <-- Ajusta la ruta si tu Base URL es distinta
    suspend fun getAsignaturas(): List<AsignaturaInfo>

    // 2. Enviar las asignaturas elegidas y sus grupos
    @PUT("api/horarios/usuarios/{idUsuario}/matricula")
    suspend fun actualizarMatricula(
        @Header("Authorization") token: String,
        @Path("idUsuario") idUsuario: String,
        @Body matricula: List<MatriculaRequest>
    ): retrofit2.Response<Unit>

    companion object {
        /*const val BASE_URL = "http://192.168.137.1:3000"*/
        const val BASE_URL = "http://192.168.1.49:3000/"
    }
}