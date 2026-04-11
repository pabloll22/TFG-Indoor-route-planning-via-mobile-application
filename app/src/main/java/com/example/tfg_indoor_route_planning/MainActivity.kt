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
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.tfg_indoor_route_planning.api.MapApiService
import com.example.tfg_indoor_route_planning.api.RetrofitClient
import com.example.tfg_indoor_route_planning.logic.CompassEngine
import com.example.tfg_indoor_route_planning.logic.GraphEngine
import com.example.tfg_indoor_route_planning.logic.NavigationHelper
import com.example.tfg_indoor_route_planning.logic.PositioningEngine
import com.example.tfg_indoor_route_planning.logic.dividirRutaPorPlantas
import com.example.tfg_indoor_route_planning.models.Mapa
import com.example.tfg_indoor_route_planning.models.Node
import com.example.tfg_indoor_route_planning.models.POI
import com.example.tfg_indoor_route_planning.models.PointMeters
import com.example.tfg_indoor_route_planning.ui.BuscadorDestino
import com.example.tfg_indoor_route_planning.ui.ControlesNavegacion
import com.example.tfg_indoor_route_planning.ui.NavigationBanner
import com.example.tfg_indoor_route_planning.ui.PantallaListaFacultades
import com.example.tfg_indoor_route_planning.ui.SelectorDePlantas
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
    private val ALPHA = 0.35f
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
    private var plantaActivaId by mutableStateOf<String?>(null)
    private var plantaQueDebeParpadearId by mutableStateOf<String?>(null)
    // Esto guarda el RSSI suavizado y la hora exacta en la que lo escuchamos por última vez
    data class BeaconState(var smoothedRssi: Double, var lastSeenTimestamp: Long)

    // Variables globales para tu lógica de escaneo
    private val activeBeacons = mutableMapOf<String, BeaconState>()
    private val RSSI_ALPHA = 0.15 // Factor de suavizado (ajusta entre 0.1 y 0.3)
    private val STALE_TIMEOUT_MS = 3000L // Si pasan 3 segundos sin escuchar un beacon, lo borramos

    @SuppressLint("MissingPermission")
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) startScan()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions()
        // Lanzamos la descarga nada más abrir la app
        setContent {
            var modoNavegacionActiva by remember { mutableStateOf(false) }
            var origenSeleccionadoId by remember { mutableStateOf<String?>(null) }
            //var plantaActivaId by remember { mutableStateOf<String?>(null) }

            var activarBuscadorExterno by remember { mutableStateOf(false) }
            var textoDestinoExterno by remember { mutableStateOf("") }
            var textoOrigenExterno by remember { mutableStateOf("") }

            // ==========================================
            // BRÚJULA
            // ==========================================
            val context = LocalContext.current
            var userOrientation by remember { mutableStateOf(0f) }

            val compassEngine = remember {
                CompassEngine(context) { newAngle ->
                    userOrientation = newAngle
                }
            }

            DisposableEffect(Unit) {
                compassEngine.start()
                onDispose {
                    compassEngine.stop()
                }
            }

            // 1. Calculamos la lista completa de instrucciones cuando cambie la ruta
            val todasLasInstrucciones = remember(rutaCalculada) {
                NavigationHelper.generateInstructions(rutaCalculada)
            }

            // 2. Obtenemos la instrucción actual (la primera de la lista de la ruta restante)
            val instruccionActual = todasLasInstrucciones.firstOrNull()

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
                                    isLoading = true
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
                            val rutaParaDibujar = if (rutaCalculada.isNotEmpty()) {
                                val rutaCortada = dividirRutaPorPlantas(rutaCalculada) // Asegúrate de tener esta función creada

                                if (rutaCortada.hayCambioDePlanta && plantaActivaId == rutaCalculada.firstOrNull()?.plantaId) {
                                    // Si la ruta cambia de planta, y estamos en la planta de origen,
                                    // hacemos que parpadee el botón de la planta de destino.
                                    plantaQueDebeParpadearId = rutaCortada.tramoPlantaDestino.firstOrNull()?.plantaId
                                } else {
                                    plantaQueDebeParpadearId = null
                                }

                                if (plantaActivaId == rutaCalculada.first().plantaId) {
                                    rutaCortada.tramoPlantaOrigen
                                } else if (plantaActivaId == rutaCalculada.last().plantaId) {
                                    rutaCortada.tramoPlantaDestino
                                } else {
                                    emptyList()
                                }
                            } else {
                                emptyList()
                            }

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
                                            rutaCalculada = rutaParaDibujar,
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
                                            rutaCalculada = rutaParaDibujar,
                                            userPosition = userPosition,
                                            currentUserNode = currentUserNode,
                                            userOrientation=userOrientation,
                                            onPoiClick = { poiTocado:POI ->
                                                poiParaConfirmar = poiTocado // Abre el popup
                                            },
                                        )
                                        // ListSection(Modifier.height(250.dp).fillMaxWidth())
                                    }
                                }

                                if (mapaDescargado != null && mapaDescargado!!.plantas.size > 1) {
                                    if (plantaActivaId == null) {
                                        val primeraPlanta = mapaDescargado!!.plantas.first() // O la que consideres "Planta Baja"

                                        // 1. Iluminamos el botón
                                        plantaActivaId = primeraPlanta.plantaId

                                    }
                                    SelectorDePlantas(
                                        plantas = mapaDescargado!!.plantas,
                                        plantaActivaId = plantaActivaId,
                                        plantaParpadeandoId = plantaQueDebeParpadearId,
                                        onPlantaSeleccionada = { idPlantaPulsada ->
                                            // 1. Iluminamos el botón nuevo
                                            plantaActivaId = idPlantaPulsada

                                            // 2. Llamamos a nuestra nueva función para cambiar los datos y la imagen
                                            cambiarDePlanta(idPlantaPulsada)

                                            if (idPlantaPulsada == plantaQueDebeParpadearId) {
                                                plantaQueDebeParpadearId = null
                                            }
                                        }
                                    )
                                }

                                // =========================================================
                                // BANNER SUPERIOR (DINÁMICO: BUSCADOR O NAVEGACIÓN)
                                // =========================================================
                                Box(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter)) {

                                    if (!modoNavegacionActiva) {
                                        // 1. MODO BÚSQUEDA: Solo mostramos el buscador si NO estamos navegando
                                        BuscadorDestino(
                                            pois = mapaDescargado?.plantas?.flatMap { it.pois } ?: emptyList(),
                                            rutaActiva = rutaCalculada.isNotEmpty(),
                                            modoRuta2 = activarBuscadorExterno,
                                            textoDestinoAUX = textoDestinoExterno,
                                            textoOrigenAUX = textoOrigenExterno,
                                            onVistaPreviaActualizada = { origenId, destinoPoi ->
                                                origenSeleccionadoId = origenId
                                                poiParaConfirmar = destinoPoi
                                            },
                                            onRutaConfirmada = { origenId, destinoPoi ->
                                                origenSeleccionadoId = origenId
                                                poiParaConfirmar = destinoPoi
                                            }
                                        )
                                    } else {
                                        // 2. MODO NAVEGACIÓN: Si estamos navegando, mostramos las instrucciones
                                        // Solo si hay una instrucción válida que mostrar
                                        val instruccionActual = remember(rutaCalculada) {
                                            NavigationHelper.generateInstructions(rutaCalculada).firstOrNull()
                                        }

                                        if (instruccionActual != null) {
                                            NavigationBanner(instruccionActual)
                                        }
                                    }
                                }

                                ControlesNavegacion(
                                    hayRutaActiva = rutaCalculada.isNotEmpty(),
                                    modoNavegacionActiva = modoNavegacionActiva,
                                    esVistaPrevia = origenSeleccionadoId != null && origenSeleccionadoId != currentUserNode?.id,
                                    poiParaConfirmar = poiParaConfirmar,
                                    poiDestinoActivo = mapaDescargado?.plantas?.flatMap { it.pois }?.find { it.nodoId == destinoSeleccionadoId },
                                    distanciaMetros = if (rutaCalculada.isNotEmpty()) graphEngine?.calcularDistanciaMetros(rutaCalculada) else 0,

                                    // Le decimos qué hacer cuando pulse "Volver"
                                    onVolverClick = {
                                        mapaAbiertoId = null
                                        rutaCalculada = emptyList()
                                        destinoSeleccionadoId = null
                                        poiParaConfirmar = null // También cerramos la tarjeta por si acaso
                                        modoNavegacionActiva = false
                                        origenSeleccionadoId = null
                                        plantaActivaId=null
                                        activarBuscadorExterno = false
                                        textoDestinoExterno = ""
                                        textoOrigenExterno=""
                                    },

                                    // Le decimos qué hacer cuando pulse "Detener Ruta"
                                    onDetenerRutaClick = {
                                        rutaCalculada = emptyList()
                                        destinoSeleccionadoId = null
                                        modoNavegacionActiva = false
                                        origenSeleccionadoId = null
                                        activarBuscadorExterno = false
                                        textoDestinoExterno = ""
                                        textoOrigenExterno=""
                                        plantaQueDebeParpadearId=null
                                    },

                                    // Le decimos qué hacer cuando pulse la "X" de la tarjeta
                                    onCerrarTarjetaClick = {
                                        poiParaConfirmar = null
                                    },

                                    // Le decimos qué hacer cuando pulse "Cómo llegar"
                                    onComoLlegarClick = { poi ->
                                        destinoSeleccionadoId = poi.nodoId

                                        activarBuscadorExterno = true
                                        textoDestinoExterno = poi.nombre

                                        // Comprobamos si es una "Vista previa" o una ruta normal
                                        if (origenSeleccionadoId == null) {
                                            // Ruta normal: empezamos desde donde estamos
                                            textoOrigenExterno = "Mi ubicación"
                                        } else {
                                            // Vista previa: buscamos el nombre del POI que elegimos como origen
                                            val poiOrigen = mapaDescargado?.plantas
                                                ?.flatMap { it.pois }
                                                ?.find { it.nodoId == origenSeleccionadoId }

                                            // Si lo encuentra pone su nombre, si no, un texto por defecto
                                            textoOrigenExterno = poiOrigen?.nombre ?: "Origen seleccionado"
                                        }

                                        // Usa el origen que elegido, o "Mi ubicación" por defecto si es null
                                        val idInicio = origenSeleccionadoId ?: currentUserNode?.id

                                        if (idInicio != null) {
                                            val nuevaRuta = graphEngine?.findPath(idInicio, destinoSeleccionadoId!!)
                                            rutaCalculada = nuevaRuta ?: emptyList()
                                            Log.d("DEBUG_RUTA", "=========================================")
                                            Log.d("DEBUG_RUTA", "Origen: $idInicio | Destino: $destinoSeleccionadoId")
                                            Log.d("DEBUG_RUTA", "Nodos devueltos por A*: ${rutaCalculada.map { it.id }}")
                                            Log.d("DEBUG_RUTA", "=========================================")

                                            // Calculamos el color del botón inferior
                                            modoNavegacionActiva = (origenSeleccionadoId == null || origenSeleccionadoId == currentUserNode?.id)
                                        }

                                        poiParaConfirmar = null // Ocultamos la tarjeta al empezar a caminar
                                    }
                                )
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
        userOrientation: Float = 0f,
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
                            color = Color.Blue, // Color Cyan brillante para la ruta
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

            // 3. Dibuja la posición calculada del usuario (Círculo Azul)
            userPosition?.let { pos ->
                val xPos = pos.x * scaleX
                val yPos = pos.y * scaleY

                val xDp = with(density) { xPos.toDp() }
                val yDp = with(density) { yPos.toDp() }
                Icon(
                    imageVector = Icons.Filled.Navigation, // Flecha de Android
                    contentDescription = "Posición del Usuario",
                    tint = Color.Blue,
                    modifier = Modifier
                        .offset(x = xDp - 12.dp, y = yDp - 12.dp) // Centramos el icono (asumiendo size 24)
                        .size(24.dp)
                        .rotate(userOrientation) // ¡Aquí usamos el parámetro!
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

                    val esDestino = (poi.nodoId == destinoSeleccionadoId || poi.nodoId == poiParaConfirmar?.nodoId)

                    // Definimos el tamaño y color dinámicamente
                    val tamanoCaja = if (esDestino) 36.dp else 24.dp
                    val ajusteOffset = if (esDestino) 18.dp else 12.dp // La mitad del tamaño para centrarlo
                    val colorFondo = if (esDestino) Color(0xFFD32F2F) else Color(0xFFFF9800) // Rojo si es destino, naranja si no
                    val grosorBorde = if (esDestino) 3.dp else 2.dp

                    // Dibujamos un marcador naranja más grande para que el usuario lo toque
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .offset(x = xDp - ajusteOffset, y = yDp - ajusteOffset)
                            .size(tamanoCaja)
                            .background(colorFondo, shape = RoundedCornerShape(8.dp))
                            .border(grosorBorde, Color.White, RoundedCornerShape(8.dp))
                            // .then(if (esDestino) Modifier.shadow(8.dp, RoundedCornerShape(8.dp)) else Modifier)
                            .clickable { onPoiClick(poi) }
                    ) {
                        // Un pequeño icono que también crece si es el destino
                        Text("📍", fontSize = if (esDestino) 18.sp else 12.sp)
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

                // Obtenemos la primera planta (Planta BajA) como planta activa por defecto
                val plantaActivaPredeterminada = mapaDescargado?.plantas?.getOrNull(0)
                Log.d("API_TFG", "Planta seleccionada: ${plantaActivaPredeterminada?.nombre}")

                plantaActivaPredeterminada?.let { planta ->
                    // Accedemos a los datos DENTRO de la planta
                    knownBeacons = planta.knownBeacons

                    // NOTA: Para el dibujo del Canvas, solo dibujamos los nodos/pois de ESTA planta.
                    // Pero para el buscador/ruta usamos las listas "todosLosPois" / "todosLosNodos" del Paso 1.
                    nodes = planta.nodos
                    pois = planta.pois

                    // Convertimos la imagen de ESTA planta
                    planoFondo = base64ToImageBitmap(planta.imagenBase64)
                }
                val todosLosNodosDelEdificio = mapaDescargado!!.plantas.flatMap { it.nodos }

                // Inicializamos los motores
                 engine = PositioningEngine(knownBeacons)
                 graphEngine = GraphEngine(todosLosNodosDelEdificio)

                // Todo listo, quitamos la pantalla de carga
                isLoading = false
                Log.d("API_TFG", "¡Éxito! Nodos: ${nodes.size}, Ancho: $10,7 m")

            } catch (e: Exception) {
                Log.e("API_TFG", "Error al descargar los datos. Revisa la IP en BASE_URL.", e)
            }
        }
    }

    private fun cambiarDePlanta(nuevaPlantaId: String) {
        // Buscamos los datos de la planta que el usuario ha tocado
        val plantaSeleccionada = mapaDescargado?.plantas?.find { it.plantaId == nuevaPlantaId }

        plantaSeleccionada?.let { planta ->
            // Actualizamos las variables que dibujan el Canvas
            knownBeacons = planta.knownBeacons
            nodes = planta.nodos
            pois = planta.pois

            // Cambiamos la imagen de fondo
            planoFondo = base64ToImageBitmap(planta.imagenBase64)

            // 3. RESETEO TOTAL DE VARIABLES DE POSICIONAMIENTO
            devices.clear()                  // Borramos los beacons de la planta anterior
            //currentSmoothedPosition = null   // Reiniciamos el filtro EMA
            //lastDrawnPosition = null         // Reiniciamos el umbral de movimiento

            //userPosition = null              // Quitamos el punto azul del Canvas
            //currentUserNode = null           // Olvidamos en qué nodo estábamos
            //rutaCalculada = emptyList()

            // IMPORTANTE: El motor de posicionamiento SÍ se reinicia con los beacons de esta planta
            engine = PositioningEngine(knownBeacons)
            plantaActivaId=planta.plantaId
            //graphEngine = GraphEngine(nodes)

            Log.d("API_TFG", "Cambiado a planta: ${planta.nombre}")
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

            if (knownBeacons.containsKey(macAddress)) {

                val currentTime = System.currentTimeMillis()
                val rawRssi = result.rssi.toDouble()

                // =========================
                // 1. FILTRO EMA (RSSI)
                // =========================
                val existingState = activeBeacons[macAddress]

                if (existingState == null) {
                    activeBeacons[macAddress] = BeaconState(rawRssi, currentTime)
                } else {
                    val newSmoothed = (rawRssi * RSSI_ALPHA) +
                            (existingState.smoothedRssi * (1 - RSSI_ALPHA))

                    existingState.smoothedRssi = newSmoothed
                    existingState.lastSeenTimestamp = currentTime
                }

                // =========================
                // 2. RECÁLCULO CONTROLADO
                // =========================
                if (currentTime - lastCalculationTime > 1000) {

                    // =========================
                    // 3. LIMPIEZA BEACONS
                    // =========================
                    activeBeacons.entries.removeIf {
                        currentTime - it.value.lastSeenTimestamp > STALE_TIMEOUT_MS
                    }

                    // =========================
                    // 4. CALCULAR POSICIÓN (HÍBRIDO)
                    // =========================
                    val stablePosition = engine?.calculateUserPosition(activeBeacons)

                    if (stablePosition != null) {

                        // =========================
                        // 5. SUAVIZADO POSICIÓN (EMA)
                        // =========================
                        val alpha = 0.5f

                        currentSmoothedPosition = if (currentSmoothedPosition == null) {
                            stablePosition
                        } else {
                            PointMeters(
                                currentSmoothedPosition!!.x + alpha * (stablePosition.x - currentSmoothedPosition!!.x),
                                currentSmoothedPosition!!.y + alpha * (stablePosition.y - currentSmoothedPosition!!.y)
                            )
                        }

                        // =========================
                        // 6. LIMITADOR DE VELOCIDAD
                        // =========================
                        if (lastDrawnPosition != null) {

                            val dx = currentSmoothedPosition!!.x - lastDrawnPosition!!.x
                            val dy = currentSmoothedPosition!!.y - lastDrawnPosition!!.y

                            val dist = sqrt(dx * dx + dy * dy)
                            val maxStep = 1.5f // metros máx por actualización

                            if (dist > maxStep) {
                                val scale = maxStep / dist
                                currentSmoothedPosition = PointMeters(
                                    lastDrawnPosition!!.x + dx * scale,
                                    lastDrawnPosition!!.y + dy * scale
                                )
                            }
                        }

                        // =========================
                        // 7. DEADBAND (evitar micro saltos)
                        // =========================
                        val distanceMoved = if (lastDrawnPosition == null) {
                            100f
                        } else {
                            sqrt(
                                (currentSmoothedPosition!!.x - lastDrawnPosition!!.x).pow(2) +
                                        (currentSmoothedPosition!!.y - lastDrawnPosition!!.y).pow(2)
                            )
                        }

                        if (distanceMoved >= MOVEMENT_THRESHOLD_METERS) {

                            userPosition = currentSmoothedPosition
                            lastDrawnPosition = currentSmoothedPosition

                            // =========================
                            // 8. SNAP AL GRAFO
                            // =========================
                            val snappedNode = graphEngine?.snapToGraph(
                                currentSmoothedPosition!!,
                                plantaActivaId ?: "planta_0",
                                rutaCalculada.isEmpty()
                            )

                            if (snappedNode != null && snappedNode.id != currentUserNode?.id) {

                                val nuevaRuta = graphEngine?.findPath(
                                    snappedNode.id,
                                    destinoSeleccionadoId
                                )

                                if (nuevaRuta != null && nuevaRuta.isNotEmpty()) {
                                    rutaCalculada = nuevaRuta
                                    Log.d(TAG, "🔄 Ruta recalculada. Pasos: ${nuevaRuta.size}")
                                } else {
                                    rutaCalculada = emptyList()
                                    Log.d(TAG, "✅ Has llegado al destino o no hay ruta.")
                                }
                            }

                            currentUserNode = snappedNode

                            Log.d(TAG,
                                "Movimiento: ${"%.2f".format(distanceMoved)}m | " +
                                        "Pos: (${currentSmoothedPosition!!.x}, ${currentSmoothedPosition!!.y})"
                            )
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