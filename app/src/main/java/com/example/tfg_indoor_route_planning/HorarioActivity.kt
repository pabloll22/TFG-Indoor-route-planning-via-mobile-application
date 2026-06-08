package com.example.tfg_indoor_route_planning

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import com.example.tfg_indoor_route_planning.horario.HorarioScreen

class HorarioActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                HorarioScreen(
                    onVolver = { finish() },
                    onVerEnMapa = {nodoId, facultadId ->

                        val intent = Intent(this@HorarioActivity, MainActivity::class.java)

                        intent.putExtra("AULA_DESTINO_ID", nodoId) // Ej: "N306"

                        intent.putExtra("FACULTAD_DESTINO_ID", facultadId)

                        startActivity(intent)
                        // Cierra la actividad actual
                        finish()
                    }
                )
            }
        }
    }
}