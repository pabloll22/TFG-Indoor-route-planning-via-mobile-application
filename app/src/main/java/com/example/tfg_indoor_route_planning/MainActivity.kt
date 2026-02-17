package com.example.tfg_indoor_route_planning

import android.Manifest
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Modelos para la vectorización
data class PointMeters(val x: Float, val y: Float)
data class Wall(val start: PointMeters, val end: PointMeters)

class MainActivity : ComponentActivity() {

    private val TAG = "BLE_SCANNER"
    private val devices = mutableStateListOf<ScanResult>()
    private var scanner: BluetoothLeScanner? = null

    // --- CONFIGURACIÓN DE PAREDES (Extrae estos datos de tu SVG) ---
    // Supongamos que el área de tu casa en el SVG es de 100x100 unidades
    private val viewSize = 100f 
    
    private val walls = listOf(
        Wall(PointMeters(10f, 10f), PointMeters(90f, 10f)), // Pared superior
        Wall(PointMeters(10f, 10f), PointMeters(10f, 80f)), // Pared izquierda
        Wall(PointMeters(90f, 10f), PointMeters(90f, 80f)), // Pared derecha
        Wall(PointMeters(10f, 80f), PointMeters(90f, 80f)), // Pared inferior
        Wall(PointMeters(40f, 10f), PointMeters(40f, 40f))  // Tabique interno
    )

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) startScan()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions()
        setContent {
            MaterialTheme {
                val configuration = LocalConfiguration.current
                val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

                Surface(modifier = Modifier.fillMaxSize()) {
                    if (isLandscape) {
                        Row(Modifier.padding(16.dp)) {
                            MapSection(Modifier.weight(2f).fillMaxHeight())
                            Spacer(modifier = Modifier.width(16.dp))
                            ListSection(Modifier.weight(1f).fillMaxHeight())
                        }
                    } else {
                        Column(Modifier.padding(16.dp)) {
                            Text("Indoor Mapping - Holy-IOT", style = MaterialTheme.typography.headlineMedium)
                            Spacer(modifier = Modifier.height(16.dp))
                            MapSection(Modifier.weight(1f).fillMaxWidth())
                            Spacer(modifier = Modifier.height(16.dp))
                            ListSection(Modifier.height(200.dp).fillMaxWidth())
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun MapSection(modifier: Modifier) {
        BoxWithConstraints(
            modifier = modifier
                .border(2.dp, Color.Gray)
                .background(Color.White)
        ) {
            // Calculamos la escala entre las unidades del SVG y los píxeles de la pantalla
            val scaleX = constraints.maxWidth.toFloat() / viewSize
            val scaleY = constraints.maxHeight.toFloat() / viewSize

            // 1. Plano de fondo (SVG convertido a Vector Drawable)
            Image(
                painter = painterResource(id = R.drawable.plano_casa2),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            // 2. Dibujamos las paredes vectoriales (Capa lógica)
            Canvas(modifier = Modifier.fillMaxSize()) {
                walls.forEach { wall ->
                    drawLine(
                        color = Color.Blue.copy(alpha = 0.5f), // Color semitransparente para verificar
                        start = Offset(wall.start.x * scaleX, wall.start.y * scaleY),
                        end = Offset(wall.end.x * scaleX, wall.end.y * scaleY),
                        strokeWidth = 4f
                    )
                }
            }

            // 3. Beacons detectados
            devices.forEachIndexed { index, result ->
                val xPos = (20 + (index * 15)) * scaleX
                val yPos = (30 + (index * 10)) * scaleY

                Box(
                    modifier = Modifier
                        .offset(x = (xPos / 2.75f).dp, y = (yPos / 2.75f).dp)
                        .size(12.dp)
                        .background(Color.Red, shape = MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Text("${result.rssi}", color = Color.White, fontSize = 8.sp)
                }
            }
        }
    }

    @Composable
    fun ListSection(modifier: Modifier) {
        Column(modifier = modifier) {
            Text("Beacons detectados:", style = MaterialTheme.typography.titleMedium)
            LazyColumn {
                items(devices) { result ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "ID: ${result.device.address.takeLast(5)}", style = MaterialTheme.typography.bodySmall)
                        Text(text = "RSSI: ${result.rssi} dBm", style = MaterialTheme.typography.bodySmall)
                    }
                    Divider()
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if (hasBlePermissions()) startScan() else permissionLauncher.launch(permissions.toTypedArray())
    }

    private fun hasBlePermissions(): Boolean {
        val perms = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            perms.add(Manifest.permission.BLUETOOTH_SCAN)
            perms.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        return perms.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
    }

    private fun startScan() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val scannerLocal = bluetoothManager.adapter?.bluetoothLeScanner ?: return
        scanner = scannerLocal
        try {
            scannerLocal.startScan(emptyList(), ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build(), scanCallback)
        } catch (e: SecurityException) { Log.e(TAG, "Error: ${e.message}") }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val deviceName = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                        result.device.name
                    } else null
                } else result.device.name
            } catch (e: SecurityException) { null }

            if (deviceName != null && deviceName.contains("Holy-IOT", ignoreCase = true)) {
                runOnUiThread {
                    val idx = devices.indexOfFirst { it.device.address == result.device.address }
                    if (idx != -1) devices[idx] = result else devices.add(result)
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun onDestroy() {
        super.onDestroy()
        if (hasBlePermissions()) try { scanner?.stopScan(scanCallback) } catch (e: Exception) {}
    }
}
