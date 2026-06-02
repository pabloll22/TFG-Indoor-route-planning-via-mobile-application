package com.example.tfg_indoor_route_planning.repository

import com.example.tfg_indoor_route_planning.api.RetrofitClient
import com.example.tfg_indoor_route_planning.api.UsuarioRespuesta

object UsuarioRepository {

    private var usuarioCache: UsuarioRespuesta? = null

    suspend fun getUsuario(idUsuario: String, forzarRecarga: Boolean = false): UsuarioRespuesta {
        if (!forzarRecarga && usuarioCache != null && usuarioCache?.idUsuario == idUsuario) {
            return usuarioCache!!
        }
        val response = RetrofitClient.apiService.getUsuario(idUsuario)
        usuarioCache = response
        return response
    }

    // Si actualizas la foto o los favoritos, puedes llamar a esta función
    // para forzar que el perfil se vuelva a descargar
    fun invalidarCache() {
        usuarioCache = null
    }

    fun limpiarCache() {
        usuarioCache = null
    }
}