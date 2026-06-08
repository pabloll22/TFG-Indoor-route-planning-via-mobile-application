package com.example.tfg_indoor_route_planning.api.dto

/**
 * DTO para enviar las credenciales de inicio de sesión al servidor.
 */
data class LoginRequest(
    val idUsuario: String,
    val password: String
)

/**
 * DTO para el formulario de registro de un nuevo usuario.
 */
data class RegistroRequest(
    val idUsuario: String,
    val nombre: String,
    val password: String,
    val rol: String
)

/**
 * DTO para empaquetar la solicitud de cambio de clave (Validación de seguridad).
 */
data class CambiarPasswordRequest(
    val passwordAntigua: String,
    val passwordNueva: String
)

/**
 * DTO de respuesta global del servidor tras un Login exitoso.
 * Transporta el token de sesión y la información del perfil empaquetada.
 */
data class AuthResponse(
    val token: String,
    val usuario: UsuarioRespuesta
)