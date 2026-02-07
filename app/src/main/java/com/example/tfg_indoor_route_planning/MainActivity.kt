package com.example.tfg_indoor_route_planning

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.MonitorNotifier
import org.altbeacon.beacon.RangeNotifier
import org.altbeacon.beacon.Region

class MainActivity : ComponentActivity() {
    private val TAG = "BeaconScanner"
    private val beaconsList = mutableStateListOf<Beacon>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Solicitar permisos en tiempo de ejecución
        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.all { it.value }) {
                setupBeaconScanning()
            } else {
                Log.e(TAG, "Permisos denegados")
            }
        }

        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        permissionLauncher.launch(permissions.toTypedArray())

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Escaneando Beacons...", style = MaterialTheme.typography.headlineMedium)
                        beaconsList.forEach { beacon ->
                            Text(text = "ID: ${beacon.id1}, Distancia: ${String.format("%.2f", beacon.distance)}m")
                        }
                    }
                }
            }
        }
    }

    private fun setupBeaconScanning() {
        val beaconManager = BeaconManager.getInstanceForApplication(this)

        // Configurar el parser para detectar diferentes tipos de beacons (iBeacon por defecto)
        beaconManager.beaconParsers.add(
            BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")
        )

        val region = Region("all-beacons-region", null, null, null)

        beaconManager.addRangeNotifier { beacons, _ ->
            if (beacons.isNotEmpty()) {
                beaconsList.clear()
                beaconsList.addAll(beacons)
                for (beacon in beacons) {
                    Log.d(TAG, "Beacon detectado: ${beacon.id1} a ${beacon.distance} metros")
                }
            }
        }

        beaconManager.startRangingBeacons(region)
    }
}
