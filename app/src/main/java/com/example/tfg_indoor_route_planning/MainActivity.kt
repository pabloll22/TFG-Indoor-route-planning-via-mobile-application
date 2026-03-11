package com.example.tfg_indoor_route_planning

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.lifecycleScope
import com.example.tfg_indoor_route_planning.api.MapApiService
import com.example.tfg_indoor_route_planning.api.RetrofitClient
import com.example.tfg_indoor_route_planning.logic.GraphEngine
import com.example.tfg_indoor_route_planning.logic.PositioningEngine
import com.example.tfg_indoor_route_planning.models.Mapa
import com.example.tfg_indoor_route_planning.models.Node
import com.example.tfg_indoor_route_planning.models.POI
import com.example.tfg_indoor_route_planning.models.PointMeters
import com.example.tfg_indoor_route_planning.ui.BuscadorDestino
import com.example.tfg_indoor_route_planning.ui.PantallaListaFacultades
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {

    private var knownBeacons by mutableStateOf<Map<String, PointMeters>>(emptyMap())
    private var nodes by mutableStateOf<List<Node>>(emptyList())
    private var planoFondo by mutableStateOf<ImageBitmap?>(null)
    private var isLoading by mutableStateOf(true)
    private var engine: PositioningEngine? = null
    private var graphEngine: GraphEngine? = null

    private val TAG = "BLE_SCANNER"
    private val devices = mutableStateListOf<ScanResult>()
    private var scanner: BluetoothLeScanner? = null

    // --- NUEVO: ESTADO PARA LA POSICIÓN CALCULADA ---
    private var userPosition by mutableStateOf<PointMeters?>(null)
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
    // Variable para pintar el nodo en la UI
    private var currentUserNode by mutableStateOf<Node?>(null)
    // --- CONFIGURACIÓN DEL MAPA ---
    private val viewSize = 10.7f
    private var mapaDescargado by mutableStateOf<Mapa?>(null)
    private var rutaCalculada by mutableStateOf<List<Node>>(emptyList())
    private var destinoSeleccionadoId by mutableStateOf<String?>(null)
    // Variables de estado para la lista principal
    private var listaMapas by mutableStateOf<List<MapApiService.MapaResumen>>(emptyList())
    private var cargandoLista by mutableStateOf(true) // Pantalla de carga inicial
    private var mapaAbiertoId by mutableStateOf<String?>(null)
    private var poiParaConfirmar by mutableStateOf<POI?>(null)
    private var pois by mutableStateOf<List<POI>>(emptyList())

    @SuppressLint("MissingPermission")
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) startScan()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions()
        // Lanzamos la descarga nada más abrir la app
        setContent {
            MaterialTheme {
                val configuration = LocalConfiguration.current
                val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

                // 1. EFECTO DE CARGA INICIAL: Descarga la lista ligera al abrir la app
                LaunchedEffect(Unit) {
                    try {
                        cargandoLista = true
                        listaMapas = RetrofitClient.apiService.getTodosLosMapas()
                        cargandoLista = false
                    } catch (e: Exception) {
                        Log.e("RED", "Error al bajar la lista: ${e.message}")
                        cargandoLista = false
                    }
                }

                Surface(modifier = Modifier.fillMaxSize()) {

                    // =========================================================
                    // PANTALLA 1: MENÚ PRINCIPAL (Lista de Facultades/Mapas)
                    // =========================================================
                    if (mapaAbiertoId == null) {
                        if (cargandoLista) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator() // Ruedita de carga
                            }
                        } else {
                            PantallaListaFacultades(
                                lista = listaMapas,
                                onFacultadClick = { idSeleccionado ->
                                    mapaAbiertoId = idSeleccionado
                                    cargarDatosDesdeServidor(idSeleccionado) // Inicia descarga pesada
                                }
                            )
                        }
                    }
                    // =========================================================
                    // PANTALLA 2: EL MAPA INTERACTIVO (¡Tu código responsive!)
                    // =========================================================
                    else {
                        if (isLoading) {
                            // Cargando el mapa específico pesado (Base64 y Nodos)
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        } else {
                            // Usamos un Box principal para que la Tarjeta y el Botón floten por encima de tu diseño
                            Box(modifier = Modifier.fillMaxSize()) {

                                // -----------------------------------------------------
                                // AQUÍ EMPIEZA TU CÓDIGO ORIGINAL DE LANDSCAPE/PORTRAIT
                                // -----------------------------------------------------
                                if (isLandscape) {
                                    Row(Modifier.padding(16.dp)) {
                                        MapSection(
                                            modifier = Modifier.weight(2f).fillMaxHeight(),
                                            nodes = nodes,
                                            planoFondo = planoFondo,
                                            rutaCalculada = rutaCalculada,
                                            userPosition = userPosition,
                                            currentUserNode = currentUserNode,
                                             //onNodeClick = { /* tu lógica de toque */ }
                                            onPoiClick = { poiTocado:POI ->
                                                poiParaConfirmar = poiTocado // Abre el popup
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        ListSection(Modifier.weight(1f).fillMaxHeight())
                                    }
                                } else {
                                    Column(Modifier.padding(16.dp)) {
                                        MapSection(
                                            modifier = Modifier.weight(1f).fillMaxWidth(),
                                            nodes = nodes,
                                            planoFondo = planoFondo,
                                            rutaCalculada = rutaCalculada,
                                            userPosition = userPosition,
                                            currentUserNode = currentUserNode,
                                            onPoiClick = { poiTocado:POI ->
                                                poiParaConfirmar = poiTocado // Abre el popup
                                            },
                                        )
                                        // ListSection(Modifier.height(250.dp).fillMaxWidth())
                                    }
                                }

                                //Buscador de POI

                                Box(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter)) {
                                    BuscadorDestino(
                                        pois = pois,
                                        onPoiSelected = { poiSeleccionado ->
                                            poiParaConfirmar = poiSeleccionado // Abre el popup de confirmación
                                        }
                                    )
                                }

                                // BOTÓN DE VOLVER A LA LISTA (Abajo a la derecha)
                                FloatingActionButton(
                                    onClick = {
                                        mapaAbiertoId = null // Esto devuelve a la pantalla 1
                                        rutaCalculada = emptyList() // Limpiamos la ruta
                                        destinoSeleccionadoId = null // Limpiamos el destino
                                    },
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(16.dp),
                                    containerColor = Color(0xFF1E88E5)
                                ) {
                                    Text("Volver", color = Color.White, modifier = Modifier.padding(horizontal = 16.dp))
                                }

                                // -----------------------------------------------------------------
                                // BOTÓN DE CANCELAR RUTA (Aparece solo cuando hay una ruta calculada)
                                // -----------------------------------------------------------------
                                AnimatedVisibility(
                                    visible = rutaCalculada.isNotEmpty(), // Solo se ve si hay línea azul en el mapa
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 32.dp), // Lo separamos un poco del borde inferior
                                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                                ) {
                                    ExtendedFloatingActionButton(
                                        onClick = {
                                            // ¡Magia! Al vaciar estas variables, el mapa borra la línea automáticamente
                                            rutaCalculada = emptyList()
                                            destinoSeleccionadoId = null
                                        },
                                        containerColor = Color(0xFFD32F2F), // Rojo elegante (Material Red 700)
                                        contentColor = Color.White,
                                        icon = {
                                            Icon(Icons.Default.Close, contentDescription = "Detener")
                                        },
                                        text = {
                                            Text("Detener ruta", fontWeight = FontWeight.Bold)
                                        }
                                    )
                                }


                                // -----------------------------------------------------------------
                                // TARJETA INFERIOR
                                // -----------------------------------------------------------------
                                // AnimatedVisibility hace que la tarjeta entre deslizando desde abajo en vez de aparecer de golpe
                                AnimatedVisibility(
                                    visible = poiParaConfirmar != null,
                                    modifier = Modifier.align(Alignment.BottomCenter),
                                    enter = slideInVertically(initialOffsetY = { it }), // Entra desde abajo
                                    exit = slideOutVertically(targetOffsetY = { it })   // Sale hacia abajo
                                ) {
                                    poiParaConfirmar?.let { poi ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            // Forma de Bottom Sheet: Arriba redondeado, abajo recto
                                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White)
                                        ) {
                                            Column(modifier = Modifier.padding(24.dp)) {

                                                // 1. Fila superior: Título y botón de la X
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = poi.nombre,
                                                            fontSize = 22.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.Black
                                                        )
                                                        Text(text = "Punto de interés", color = Color.Gray, fontSize = 14.sp)
                                                    }
                                                    // Botón para cerrar la tarjeta sin hacer nada
                                                    IconButton(onClick = { poiParaConfirmar = null }) {
                                                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Gray)
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(24.dp))

                                                // 2. Botón gigante de "Cómo llegar"
                                                Button(
                                                    onClick = {
                                                        destinoSeleccionadoId = poi.nodoId

                                                        if (currentUserNode != null) {
                                                            val nuevaRuta = graphEngine?.findPath(currentUserNode!!.id, destinoSeleccionadoId!!)
                                                            rutaCalculada = nuevaRuta ?: emptyList()
                                                        }

                                                        poiParaConfirmar = null // Ocultamos la tarjeta cuando inicia la ruta
                                                    },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(50.dp), // Botón más gordito para que sea fácil de pulsar
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Icon(Icons.Default.Directions, contentDescription = "Cómo llegar", tint = Color.White)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("Cómo llegar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }



    @SuppressLint("UnusedBoxWithConstraintsScope")
    @Composable
    fun MapSection(
        modifier: Modifier,
        nodes: List<Node>,
        planoFondo: ImageBitmap?,
        rutaCalculada: List<Node>,
        userPosition: PointMeters?,
        currentUserNode: Node?,
        onPoiClick: (POI) -> Unit
    ) {
        val density = LocalDensity.current // NUEVO: Obtenemos la densidad de la pantalla
        BoxWithConstraints(
            modifier = modifier
                .border(2.dp, Color.Gray)
                .background(Color.White)
        ) {
            val scaleX = constraints.maxWidth.toFloat() / viewSize
            val scaleY = constraints.maxHeight.toFloat() / viewSize

            planoFondo?.let { miImagenDescargada ->
                Image(
                    // 2. Usamos 'bitmap =' en lugar de 'painter ='
                    bitmap = miImagenDescargada,
                    contentDescription = "Plano del edificio descargado",

                    // 3. Mantén los modificadores que ya tuvieras, por ejemplo:
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
            }

            // Dibujamos la ruta debajo de los nodos para que no los tape
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (rutaCalculada.size > 1) {
                    for (i in 0 until rutaCalculada.size - 1) {
                        val startNode = rutaCalculada[i]
                        val endNode = rutaCalculada[i + 1]

                        drawLine(
                            color = Color(0xFF00BCD4), // Color Cyan brillante para la ruta
                            start = Offset((startNode.position.x * scaleX), (startNode.position.y * scaleY)),
                            end = Offset(endNode.position.x * scaleX, endNode.position.y * scaleY),
                            strokeWidth = 12f,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    }
                }
            }

            // 1. Dibujamos los NODOS de navegación (Verde)
            nodes.forEach { node ->
                val xPos = node.position.x * scaleX
                val yPos = node.position.y * scaleY

                val xDp = with(density) { xPos.toDp() }
                val yDp = with(density) { yPos.toDp() }

                Box(
                    modifier = Modifier
                        //.offset(x = (xPos / 2.75f).dp, y = (yPos / 2.75f).dp)
                        .offset(xDp-5.dp, yDp-5.dp)
                        .size(12.dp)
                        .background(Color.Green, shape = CircleShape)
                        .border(1.dp, Color.Black, CircleShape)
                ){
                    Text(
                        text = node.id,
                        color = Color.Black,
                        fontSize = 6.sp, // Letra microscópica para que quepa
                        fontWeight = FontWeight.Bold,
                        maxLines = 1, // Obligamos a que sea una sola línea
                        softWrap = false // Evitamos que haga saltos de línea raros
                    )
                }
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
                        .offset(x = xDp-5.dp, y = yDp-5.dp)
                        .size(10.dp)
                        .background(Color.Magenta, shape = CircleShape)
                        .border(1.dp, Color.Black, CircleShape)
                )
            }

            // --- 2. DIBUJAMOS LOS POIs (Naranja) ---
            pois.forEach { poi ->
                // Buscamos las coordenadas del nodo al que pertenece este POI
                val nodoDelPoi = nodes.find { it.id == poi.nodoId }

                if (nodoDelPoi != null) {
                    val xPos = nodoDelPoi.position.x * scaleX
                    val yPos = nodoDelPoi.position.y * scaleY
                    val xDp = with(density) { xPos.toDp() }
                    val yDp = with(density) { yPos.toDp() }

                    // Dibujamos un marcador naranja más grande para que el usuario lo toque
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .offset(x = xDp - 12.dp, y = yDp - 12.dp) // -12 porque mide 24
                            .size(24.dp)
                            .background(Color(0xFFFF9800), shape = RoundedCornerShape(8.dp)) // Naranja y cuadradito
                            .border(2.dp, Color.White, RoundedCornerShape(8.dp))
                            .clickable { onPoiClick(poi) } // ¡EL CLICK AHORA ESTÁ AQUÍ!
                    ) {
                        // Un pequeño icono o inicial adentro
                        Text("📍", fontSize = 12.sp)
                    }
                }
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

    private fun cargarDatosDesdeServidor(idSeleccionado: String) {
        lifecycleScope.launch {
            try {
                mapaDescargado = RetrofitClient.apiService.getMapa(idSeleccionado)

                // Asignamos las variables a la interfaz
                mapaDescargado?.let { mapa ->
                    knownBeacons = mapa.knownBeacons
                    nodes = mapa.nodos
                    pois = mapa.pois
                }

                // Convertimos la imagen
                planoFondo = base64ToImageBitmap(mapaDescargado!!.imagenBase64)

                // Inicializamos los motores
                 engine = PositioningEngine(knownBeacons)
                 graphEngine = GraphEngine(nodes)

                // Todo listo, quitamos la pantalla de carga
                isLoading = false
                Log.d("API_TFG", "¡Éxito! Nodos: ${nodes.size}, Ancho: $10,7 m")

            } catch (e: Exception) {
                Log.e("API_TFG", "Error al descargar los datos. Revisa la IP en BASE_URL.", e)
            }
        }
    }

    @SuppressLint("MissingPermission")
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
                    val rawPosition = engine?.calculateUserPosition(devices)

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
                            val snappedNode = graphEngine?.snapToGraph(currentSmoothedPosition!!)


                            if (snappedNode != null && snappedNode.id != currentUserNode?.id) {

                                // ¡Ha cambiado de nodo! Recalculamos la ruta hacia el destino
                                // (Asegúrate de tener la variable destinoSeleccionadoId definida arriba en tu MainActivity)
                                val nuevaRuta = graphEngine?.findPath(snappedNode.id, destinoSeleccionadoId)

                                if (nuevaRuta != null && nuevaRuta.isNotEmpty()) {
                                    rutaCalculada = nuevaRuta
                                    Log.d(TAG, "🔄 Ruta recalculada desde ${snappedNode.name}. Pasos: ${nuevaRuta.size}")
                                } else {
                                    rutaCalculada = emptyList() // Si llega a 0, ha llegado al final
                                    Log.d(TAG, "✅ Has llegado al destino o no hay ruta posible.")
                                }
                            }

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

    private fun base64ToImageBitmap(base64String: String): ImageBitmap? {
        return try {
            val cleanBase64 = if (base64String.contains(",")) {
                base64String.split(",")[1]
            } else {
                base64String
            }
            val imageBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            bitmap.asImageBitmap()
        } catch (e: Exception) {
            Log.e("API_TFG", "Error decodificando la imagen Base64", e)
            null
        }
    }
}