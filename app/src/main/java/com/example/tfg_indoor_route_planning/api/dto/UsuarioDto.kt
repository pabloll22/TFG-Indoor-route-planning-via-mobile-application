package com.example.tfg_indoor_route_planning.api.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO que representa el perfil completo del usuario recuperado de MongoDB.
 */
data class UsuarioRespuesta(
    val idUsuario: String,
    val nombre: String,
    val rol: String,
    val poisFavoritos: List<PoiFavorito> = emptyList(),
    val asignaturasMatriculadas: List<MatriculaRequest> = emptyList(),
    val rutasAccesibles: Boolean = false,

    @SerializedName("foto_url")
    val foto_url: String? = ""
)

/**
 * DTO para enviar la lista actualizada de los lugares favoritos del usuario.
 */
data class FavoritosRequest(
    val favoritos: List<PoiFavorito>
)

/**
 * DTO para actualizar el estado del interruptor de rutas accesibles en la BD.
 */
data class AccesibilidadRequest(
    val rutasAccesibles: Boolean
)

/**
 * DTO que devuelve el servidor con la URL pública tras procesar la foto de perfil.
 */
data class FotoResponse(
    val mensaje: String,
    val url: String
)