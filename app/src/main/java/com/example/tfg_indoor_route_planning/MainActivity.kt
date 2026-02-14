package com.example.tfg_indoor_route_planning

import android.Manifest
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    private val TAG = "BLE_SCANNER"
    private val devices = mutableStateListOf<ScanResult>()
    private var scanner: BluetoothLeScanner? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            startScan()
        } else {
            Log.e(TAG, "Permisos denegados: ${permissions.filter { !it.value }.keys}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkAndRequestPermissions()

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Dispositivos Holy-IOT", style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        LazyColumn {
                            items(devices) { result ->
                                Column(modifier = Modifier.padding(8.dp)) {
                                    val deviceName = try {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                            if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                                                result.device.name
                                            } else {
                                                "Sin permiso de nombre"
                                            }
                                        } else {
                                            result.device.name
                                        }
                                    } catch (e: SecurityException) {
                                        null
                                    } ?: "Holy-IOT"

                                    Text(
                                        text = "Nombre: $deviceName",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "Dirección: ${result.device.address}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "RSSI: ${result.rssi} dBm",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Divider(modifier = Modifier.padding(top = 8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        if (hasBlePermissions()) {
            startScan()
        } else {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun hasBlePermissions(): Boolean {
        val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        return permissions.all {
            checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun startScan() {
        if (!hasBlePermissions()) return

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter ?: return

        if (!adapter.isEnabled) {
            Log.e(TAG, "Bluetooth no está activado")
            return
        }

        scanner = adapter.bluetoothLeScanner
        if (scanner == null) {
            Log.e(TAG, "No se pudo obtener el scanner BLE")
            return
        }

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .build()

        try {
            scanner?.startScan(emptyList<ScanFilter>(), settings, scanCallback)
            Log.d(TAG, "Escaneo iniciado")
        } catch (e: SecurityException) {
            Log.e(TAG, "Error de seguridad al iniciar escaneo: ${e.message}")
        }
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

            // Filtro por el nombre Holy-IOT
            if (deviceName != null && deviceName.contains("Holy-IOT", ignoreCase = true)) {
                runOnUiThread {
                    val existingDeviceIndex = devices.indexOfFirst { it.device.address == result.device.address }
                    if (existingDeviceIndex != -1) {
                        devices[existingDeviceIndex] = result
                    } else {
                        devices.add(result)
                    }
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Error en el escaneo: $errorCode")
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun onDestroy() {
        super.onDestroy()
        try {
            if (scanner != null && hasBlePermissions()) {
                scanner?.stopScan(scanCallback)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al detener el escaneo: ${e.message}")
        }
    }
}
