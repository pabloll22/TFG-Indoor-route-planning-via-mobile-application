import android.content.Context
import android.content.SharedPreferences

object UserSession {
    var usuarioId: String = ""
    var nombre: String = ""
    var rol: String = "ALUMNO"
    var token: String = ""
    var fotoUrl: String = ""

    val esProfesor: Boolean
        get() = rol == "PROFESOR"

    val esInvitado: Boolean
        get() = rol == "INVITADO"

    // Inicializa la sesión leyendo de la memoria del móvil
    fun init(context: Context): Boolean {
        val prefs: SharedPreferences = context.getSharedPreferences("ControlUMAPrefs", Context.MODE_PRIVATE)
        usuarioId = prefs.getString("usuarioId", "") ?: ""
        nombre = prefs.getString("nombre", "") ?: ""
        rol = prefs.getString("rol", "ALUMNO") ?: "ALUMNO"
        token = prefs.getString("token", "") ?: ""
        fotoUrl = prefs.getString("fotoUrl", "") ?: ""

        return usuarioId.isNotEmpty() && token.isNotEmpty()// Devuelve true si hay alguien logueado
    }

    // Guarda en memoria y en la sesión actual
    fun iniciarSesion(context: Context, id: String, nombreUsuario: String, rolUsuario: String, jwtToken: String, urlFoto: String = "") {
        usuarioId = id
        nombre = nombreUsuario
        rol = rolUsuario
        token = jwtToken
        fotoUrl = urlFoto

        val prefs = context.getSharedPreferences("ControlUMAPrefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("usuarioId", id)
            putString("nombre", nombreUsuario)
            putString("rol", rolUsuario)
            putString("token", jwtToken)
            putString("fotoUrl", urlFoto)
            apply()
        }
    }

    // Borra de la memoria al salir
    fun cerrarSesion(context: Context) {
        token = ""
        usuarioId = ""
        val prefs = context.getSharedPreferences("ControlUMAPrefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    fun actualizarFotoUrl(context: Context, nuevaUrl: String) {
        fotoUrl = nuevaUrl // Actualiza la variable en tiempo real

        // Lo guarda en memoria para que sobreviva al cerrar la app
        val prefs = context.getSharedPreferences("ControlUMAPrefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("fotoUrl", nuevaUrl)
            apply()
        }
    }
}