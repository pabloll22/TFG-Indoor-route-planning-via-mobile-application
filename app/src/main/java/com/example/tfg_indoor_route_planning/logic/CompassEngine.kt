package com.example.tfg_indoor_route_planning.logic

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class CompassEngine(context: Context, private val onAngleChanged: (Float) -> Unit) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null

    // Variables para el suavizado (EMA adaptado a ángulos)
    private var smoothedSin = 0.0
    private var smoothedCos = 0.0
    private val ALPHA = 0.15 //inercia en la aguja

    fun start() {
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        magnetometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) gravity = event.values.clone()
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) geomagnetic = event.values.clone()

        if (gravity != null && geomagnetic != null) {
            val R = FloatArray(9)
            val I = FloatArray(9)


            val success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)

            if (success) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(R, orientation)

                // orientation[0] es el Azimut en radianes (-π a π)
                val azimuthRadians = orientation[0].toDouble()

                // Filtro EMA para ángulos (para evitar el salto brusco de 359º a 1º)
                val currentSin = sin(azimuthRadians)
                val currentCos = cos(azimuthRadians)

                smoothedSin = (currentSin * ALPHA) + (smoothedSin * (1 - ALPHA))
                smoothedCos = (currentCos * ALPHA) + (smoothedCos * (1 - ALPHA))

                // Volvemos a convertir a grados
                val smoothedRadians = atan2(smoothedSin, smoothedCos)
                var degrees = Math.toDegrees(smoothedRadians).toFloat()

                // Lo pasamos a formato 0 - 360 grados
                if (degrees < 0) degrees += 360f

                // Enviamos el ángulo limpio a la Interfaz Gráfica
                onAngleChanged(degrees)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}