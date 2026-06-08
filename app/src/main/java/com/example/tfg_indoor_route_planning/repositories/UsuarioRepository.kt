package com.example.tfg_indoor_route_planning.repositories

import android.content.Context
import com.example.tfg_indoor_route_planning.api.RetrofitClient
import com.example.tfg_indoor_route_planning.api.dto.AccesibilidadRequest
import com.example.tfg_indoor_route_planning.api.dto.UsuarioRespuesta

object UsuarioRepository {

    private var usuarioCache: UsuarioRespuesta? = null

    suspend fun getUsuario(idUsuario: String, forzarRecarga: Boolean = false): UsuarioRespuesta {
        if (!forzarRecarga && usuarioCache != null && usuarioCache?.idUsuario == idUsuario) {
            return usuarioCache!!
        }
        val response = RetrofitClient.usuarioService.getUsuario(idUsuario)
        usuarioCache = response
        return response
    }

    suspend fun cambiarAccesibilidadServidor(context: Context, idUsuario: String, esAccesible: Boolean): Boolean {
        return try {
            val response = RetrofitClient.usuarioService.actualizarAccesibilidad(
                token = UserSession.token,
                idUsuario = idUsuario,
                request = AccesibilidadRequest(esAccesible)
            )

            if (response.isSuccessful) {
                // 1. Actualizamos SharedPreferences para que persista localmente en esta sesión
                UserSession.setRutasAccesibles(context, esAccesible)

                // 2. Modificamos la caché en memoria del repositorio usando .copy()
                usuarioCache = usuarioCache?.copy(rutasAccesibles = esAccesible)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
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