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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tfg_indoor_route_planning.logic.GraphEngine
import com.example.tfg_indoor_route_planning.logic.PositioningEngine
import com.example.tfg_indoor_route_planning.models.Node
import com.example.tfg_indoor_route_planning.models.PointMeters
import com.example.tfg_indoor_route_planning.models.Wall
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {

    private val TAG = "BLE_SCANNER"
    private val devices = mutableStateListOf<ScanResult>()
    private var scanner: BluetoothLeScanner? = null

    // --- NUEVO: ESTADO PARA LA POSICIÓN CALCULADA ---
    private var userPosition by mutableStateOf<PointMeters?>(null)
    private val engine by lazy { PositioningEngine(knownBeacons) }
    // 1. VARIABLE DE CONTROL DE TIEMPO
    private var lastCalculationTime = 0L
    // Variable para recordar la posición anterior suavizada (fuera del callback)
    private var currentSmoothedPosition: PointMeters? = null

    // Guarda la última posición que REALMENTE se pintó en pantalla
    private var lastDrawnPosition: PointMeters? = null
    // Distancia mínima en metros para actualizar el mapa (0.25f = 25 cm)
    private val MOVEMENT_THRESHOLD_METERS = 0.25f

    // Factor de suavizado (0.1 = muy lento/suave, 0.9 = muy rápido/ruidoso)
    // 0.25f suele ser un buen equilibrio
    private val ALPHA = 0.5f

    // --- NUEVO: MAPA DE BEACONS CONOCIDOS Y SUS POSICIONES FIJAS ---
    // Asocia la dirección MAC de cada beacon con su posición en el mapa.
    private val knownBeacons = mapOf(
        "F0:DD:31:0E:CA:81" to PointMeters(1f, 1f), // Beacon 3
        "CC:06:A8:C7:B1:65" to PointMeters(9f, 1f), // Beacon 2
        "CA:C2:BA:EA:CD:C5" to PointMeters(4.5f, 10f)  // Beacon 1
    )

    private val nodes = listOf(
        // --- PASILLO (Eje Y = 2) ---
        // N1 conectado a N2 (siguiente pasillo) y N11 (entrada Hab3)
        Node("N1", PointMeters(2f, 2f), "Pasillo Inicio", neighbors = listOf("N2", "N13")),

        // N2 conectado a N1 (atrás), N3 (adelante) y N5 (entrada Hab1)
        Node("N2", PointMeters(4.25f, 2f), "Pasillo Centro", neighbors = listOf("N1", "N3", "N5")),

        // N3 conectado a N2 (atrás), N4 (adelante) y N8 (entrada Hab2)
        Node("N3", PointMeters(6.25f, 2f), "Pasillo Fondo", neighbors = listOf("N2", "N4", "N8")),

        // N4 Final pasillo, conectado a N3 y N11 (entrada Hab Extra)
        Node("N4", PointMeters(8f, 2f), "Pasillo Final", neighbors = listOf("N3", "N11")),

        // --- HABITACIÓN 1 (X = 4.25) ---
        // N5 es la puerta, conecta al Pasillo (N2) y adentro (N6)
        Node("N5", PointMeters(4.25f, 3f), "Hab1 Puerta", neighbors = listOf("N2", "N6")),
        Node("N6", PointMeters(4.25f, 4f), "Hab1 Centro", neighbors = listOf("N5", "N7")),
        Node("N7", PointMeters(4.25f, 5f), "Hab1 Fondo", neighbors = listOf("N6")),

        // --- HABITACIÓN 2 (X = 6.25) ---
        Node("N8", PointMeters(6.25f, 3f), "Hab2 Puerta", neighbors = listOf("N3", "N9")),
        Node("N9", PointMeters(6.25f, 4f), "Hab2 Centro", neighbors = listOf("N8", "N10")),
        Node("N10", PointMeters(6.25f, 5f), "Hab2 Fondo", neighbors = listOf("N9")),

        // --- ENTRADA (X = 8) ---
        Node("N11", PointMeters(8f, 3f), "ENTRADA Puerta", neighbors = listOf("N4", "N12")),
        Node("N12", PointMeters(8f, 4f), "ENTRADA Fondo", neighbors = listOf("N11", "N16")),

        // --- SALON
        Node("N16", PointMeters(8f, 5f), "SALON Puerta", neighbors = listOf("N12", "N16")),
        Node("N17", PointMeters(8f, 6f), "SALON MEDIO 1", neighbors = listOf("N16", "N18")),
        Node("N18", PointMeters(8f, 7f), "SALON MEDIO 2", neighbors = listOf("N17", "N19")),

        //--- TERRAZA
        Node("N19", PointMeters(6.25f, 7f), "TERRAZA Puerta", neighbors = listOf("N18", "N20")),
        Node("N20", PointMeters(4.25F, 7f), "TERRAZA", neighbors = listOf("N19", "N21")),
        Node("N23", PointMeters(4.25F, 8f), "TERRAZA", neighbors = listOf("N20", "N24")),
        Node("N24", PointMeters(2F, 8f), "TERRAZA", neighbors = listOf("N23", "N21")),
        Node("N21", PointMeters(2F, 7f), "TERRAZA", neighbors = listOf("N20", "N22")),
        Node("N22", PointMeters(2F, 6f), "TERRAZA-HAB3", neighbors = listOf("N21", "N15")),



        // --- HABITACIÓN 3 (X = 2) ---
        Node("N13", PointMeters(2f, 3f), "Hab3 Puerta", neighbors = listOf("N1", "N14")),
        Node("N14", PointMeters(2f, 4f), "Hab3 Centro", neighbors = listOf("N13", "N15")),
        Node("N15", PointMeters(2f, 5f), "Hab3 Fondo", neighbors = listOf("N14", "N22"))
    )

    // Instancias el motor del grafo
    private val graphEngine = GraphEngine(nodes)

    // Variable para pintar el nodo en la UI
    private var currentUserNode by mutableStateOf<Node?>(null)

    // --- CONFIGURACIÓN DEL MAPA ---
    private val viewSize = 10.7f
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
                            ListSection(Modifier.height(250.dp).fillMaxWidth())
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun MapSection(modifier: Modifier) {
        val density = LocalDensity.current // NUEVO: Obtenemos la densidad de la pantalla
        BoxWithConstraints(
            modifier = modifier
                .border(2.dp, Color.Gray)
                .background(Color.White)
        ) {
            val scaleX = constraints.maxWidth.toFloat() / viewSize
            val scaleY = constraints.maxHeight.toFloat() / viewSize

            Image(
                painter = painterResource(id = R.drawable.plano_casa),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            /*Canvas(modifier = Modifier.fillMaxSize()) {
                walls.forEach { wall ->
                    drawLine(
                        color = Color.Blue.copy(alpha = 0.3f),
                        start = Offset(wall.start.x * scaleX, wall.start.y * scaleY),
                        end = Offset(wall.end.x * scaleX, wall.end.y * scaleY),
                        strokeWidth = 4f
                    )
                }
            }*/

            // 1. Dibujamos los NODOS de navegación (Verde)
            nodes.forEach { node ->
                val xPos = node.position.x * scaleX
                val yPos = node.position.y * scaleY

                Box(
                    modifier = Modifier
                        .offset(x = (xPos / 2.75f).dp, y = (yPos / 2.75f).dp)
                        .size(10.dp)
                        .background(Color.Green, shape = CircleShape)
                        .border(1.dp, Color.Black, CircleShape)
                )
            }

            // 2. Beacons detectados (Rojo) - Ahora representa los beacons conocidos
            knownBeacons.values.forEach { beaconPos ->
                val xPos = beaconPos.x * scaleX
                val yPos = beaconPos.y * scaleY

                //Convertimos los Píxeles a Dp correctamente
                val xDp = with(density) { xPos.toDp() }
                val yDp = with(density) { yPos.toDp() }
                Box(
                    modifier = Modifier
                        .offset(x = xDp, y =yDp)
                        .size(12.dp)
                        .background(Color.Red, shape = MaterialTheme.shapes.small)
                )
            }

            // 3. NUEVO: Dibuja la posición calculada del usuario (Círculo Azul)
            userPosition?.let { pos ->
                val xPos = pos.x * scaleX
                val yPos = pos.y * scaleY

                val xDp = with(density) { xPos.toDp() }
                val yDp = with(density) { yPos.toDp() }
                Box(
                    modifier = Modifier
                        .offset(x = xDp, y =yDp)
                        .size(15.dp)
                        .background(Color.Blue, shape = CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                )
            }

            // EL USO DE currentUserNode: Dibuja el nodo "imantado"
            currentUserNode?.let { node ->
                val xPos = node.position.x * scaleX
                val yPos = node.position.y * scaleY

                val xDp = with(density) { xPos.toDp() }
                val yDp = with(density) { yPos.toDp() }
                Box(
                    modifier = Modifier
                        .offset(x = (xPos / 2.75f).dp, y = (yPos / 2.75f).dp)
                        .size(10.dp)
                        .background(Color.Magenta, shape = CircleShape)
                        .border(1.dp, Color.Black, CircleShape)
                )
            }
        }
    }

    @Composable
    fun ListSection(modifier: Modifier) {
        Column(modifier = modifier) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Text("Beacons Holy-IOT Detectados:",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color(0xFF0066CC)) // Color azul para diferenciar
                }

                // Aquí volvemos a filtrar por seguridad para la UI
                val filteredDevices = devices.filter { knownBeacons.containsKey(it.device.address) }

                items(filteredDevices) { result ->
                    val macAddress = result.device.address
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Holy-IOT (${macAddress.takeLast(5)})",
                            style = MaterialTheme.typography.bodySmall)
                        Text(text = "${result.rssi} dBm",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    }
                    Divider()
                }

                if (filteredDevices.isEmpty()) {
                    item {
                        Text("Buscando beacons conocidos...",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(8.dp))
                    }
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

    // --- CÓDIGO ACTUALIZADO: SCAN CALLBACK ---
    private val scanCallback = object : ScanCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            val macAddress = result.device.address

            // FILTRO ESTRICTO: Solo si la MAC está en mis beacons conocidos
            if (knownBeacons.containsKey(macAddress)) {
                val index = devices.indexOfFirst { it.device.address == macAddress }
                if (index != -1) {
                    devices[index] = result
                } else {
                    devices.add(result)
                }

                val currentTime = System.currentTimeMillis()

                // Solo recalculamos la posición si han pasado 500ms
                if (currentTime - lastCalculationTime > 500) {

                    // 1. Obtenemos la posición "cruda" (con ruido)
                    val rawPosition = engine.calculateUserPosition(devices)

                    if (rawPosition != null) {
                        // 2. ESTRATEGIA 1: Filtro de Paso Bajo (Suavizado EMA)
                        if (currentSmoothedPosition == null) {
                            // Si es la primera vez, confiamos en el dato crudo
                            currentSmoothedPosition = rawPosition
                        } else {
                            // Fórmula: (Nuevo * alpha) + (Anterior * (1 - alpha))
                            val newX = (rawPosition.x * ALPHA) + (currentSmoothedPosition!!.x * (1 - ALPHA))
                            val newY = (rawPosition.y * ALPHA) + (currentSmoothedPosition!!.y * (1 - ALPHA))

                            currentSmoothedPosition = PointMeters(newX, newY)
                        }

                        // 3. ESTRATEGIA 3: Umbral de Movimiento (Deadband)
                        // Calculamos cuánto nos hemos movido respecto a lo último que se dibujó
                        val distanceMoved = if (lastDrawnPosition == null) {
                            100f // Valor alto para forzar el primer pintado
                        } else {
                            sqrt((currentSmoothedPosition!!.x - lastDrawnPosition!!.x).pow(2) + (currentSmoothedPosition!!.y - lastDrawnPosition!!.y).pow(2))
                        }

                        // Solo actualizamos la UI si el cambio es significativo (evita el "baile" del punto)
                        if (distanceMoved >= MOVEMENT_THRESHOLD_METERS) {
                            userPosition = currentSmoothedPosition // Actualizamos el estado de Compose
                            // Le pedimos al motor del grafo que busque el nodo lógico
                            // usando la posición suavizada actual.
                            val snappedNode = graphEngine.snapToGraph(currentSmoothedPosition!!)

                            // Actualizamos la variable de estado para que la UI se repinte
                             currentUserNode = snappedNode
                            // -----------------------------------------------------------

                            Log.d(TAG, "Movimiento: ${"%.2f".format(distanceMoved)}m. Nodo actual: ${snappedNode?.name}")
                        }
                    }
                    lastCalculationTime = currentTime
                }
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            results?.forEach { result ->
                val macAddress = result.device.address
                if (knownBeacons.containsKey(macAddress)) {
                    val index = devices.indexOfFirst { it.device.address == macAddress }
                    if (index != -1) devices[index] = result else devices.add(result)
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "onScanFailed: code $errorCode")
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
    private fun startScan() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        scanner = bluetoothManager.adapter.bluetoothLeScanner

        if (scanner == null || !bluetoothManager.adapter.isMultipleAdvertisementSupported) {
            Log.e(TAG, "El dispositivo no soporta escaneo BLE.")
            return
        }

        val scanFilter = ScanFilter.Builder().build()
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        scanner?.startScan(listOf(scanFilter), scanSettings, scanCallback)
        Log.d(TAG, "Escaneo BLE iniciado...")
    }

    private fun hasBlePermissions(): Boolean {
        val perms = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            perms.add(Manifest.permission.BLUETOOTH_SCAN)
            perms.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        return perms.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
    }
}