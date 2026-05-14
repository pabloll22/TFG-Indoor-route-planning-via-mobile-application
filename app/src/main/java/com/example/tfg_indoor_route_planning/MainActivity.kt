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
import android.speech.tts.TextToSpeech
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.tfg_indoor_route_planning.api.FavoritosRequest
import com.example.tfg_indoor_route_planning.api.MapApiService
import com.example.tfg_indoor_route_planning.api.MapaResumen
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
import com.example.tfg_indoor_route_planning.models.obtenerEstiloPoi
import com.example.tfg_indoor_route_planning.ui.BuscadorDestino
import com.example.tfg_indoor_route_planning.ui.ControlesNavegacion
import com.example.tfg_indoor_route_planning.ui.HojaGuardadosBottomSheet
import com.example.tfg_indoor_route_planning.ui.NavigationBanner
import com.example.tfg_indoor_route_planning.ui.NavigationItem
import com.example.tfg_indoor_route_planning.ui.PantallaListaFacultades
import com.example.tfg_indoor_route_planning.ui.SelectorDePlantas
import com.example.tfg_indoor_route_planning.web_scrapping.PantallaExplorarSheet
import kotlinx.coroutines.launch
import java.util.Locale
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

    // ESTADO PARA LA POSICIÓN CALCULADA
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
    private var listaMapas by mutableStateOf<List<MapaResumen>>(emptyList())
    private var cargandoLista by mutableStateOf(true) // Pantalla de carga inicial
    private var mapaAbiertoId by mutableStateOf<String?>(null)
    private var poiParaConfirmar by mutableStateOf<POI?>(null)
    private var pois by mutableStateOf<List<POI>>(emptyList())
    var todosLosPoisDelEdificio by mutableStateOf<List<POI>>(emptyList())
    private var plantaActivaId by mutableStateOf<String?>(null)
    private var plantaQueDebeParpadearId by mutableStateOf<String?>(null)
    // Esto guarda el RSSI suavizado y la hora exacta en la que lo escuchamos por última vez
    data class BeaconState(var smoothedRssi: Double, var lastSeenTimestamp: Long)

    // Variables globales para tu lógica de escaneo
    private val activeBeacons = mutableMapOf<String, BeaconState>()
    private val RSSI_ALPHA = 0.15 // Factor de suavizado (ajusta entre 0.1 y 0.3)
    private val STALE_TIMEOUT_MS = 3000L // Si pasan 3 segundos sin escuchar un beacon, lo borramos
    private var ancho: Float = 0.0f
    private var largo: Float = 0.0f

    private var modoNavegacionActiva by  mutableStateOf(false)
    private var origenSeleccionadoId by mutableStateOf<String?>(null)
    private var aulaVipPendiente: String? = null
    private var urlWeb: String = "https://www.uma.es/"

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

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

        // 1. RECOGEMOS LOS DATOS DEL INTENT (Si venimos del Horario)
        aulaVipPendiente = intent.getStringExtra("AULA_DESTINO_ID")
        val facultadVipId = intent.getStringExtra("FACULTAD_DESTINO_ID")

        if (facultadVipId != null) {
            // Como ahora SÍ tiene la facultad "2", se saltará la lista de facultades,
            // pondrá el mapa en modo carga e iniciará la descarga del edificio ETSI.
            mapaAbiertoId = facultadVipId
            isLoading = true
            cargarDatosDesdeServidor(facultadVipId)
        }

        val modoSeleccionAula = intent.getBooleanExtra("MODO_SELECCION_AULA", false)
        val facultadId = intent.getStringExtra("FACULTAD_ID") ?: "1"

        if (modoSeleccionAula) {
            mapaAbiertoId = facultadId
            isLoading = true
            cargarDatosDesdeServidor("2")
        }

        // INICIALIZAR EL MOTOR DE VOZ
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Configuramos el idioma a Español
                val result = tts?.setLanguage(Locale("es", "ES"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("VOZ", "El idioma Español no está soportado en este dispositivo")
                } else {
                    isTtsReady = true
                }
            } else {
                Log.e("VOZ", "Fallo al inicializar TextToSpeech")
            }
        }

        // Lanzamos la descarga nada más abrir la app
        setContent {
            //var plantaActivaId by remember { mutableStateOf<String?>(null) }

            var activarBuscadorExterno by remember { mutableStateOf(false) }
            var textoDestinoExterno by remember { mutableStateOf("") }
            var textoOrigenExterno by remember { mutableStateOf("") }

            var listaFavoritosIds by remember { mutableStateOf(setOf<String>()) }
            var mostrarHojaGuardados by remember { mutableStateOf(false) } // Para controlar el BottomSheet de guardados

            var mostrarHojaExplorar by remember { mutableStateOf(false) }

            var vozActivada by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                if (!UserSession.esInvitado) {
                    val idUsuarioActual = UserSession.usuarioId
                    try {
                        val usuario = RetrofitClient.apiService.getUsuario(idUsuarioActual)
                        // Convertimos la List del servidor al Set que usa la interfaz
                        listaFavoritosIds = usuario.poisFavoritos.toSet()
                        Log.d("FAVORITOS", "Cargados ${listaFavoritosIds.size} favoritos de la base de datos")
                    } catch (e: Exception) {
                        Log.e("FAVORITOS", "Error al cargar favoritos iniciales", e)
                    }
                } else {
                    Log.d("FAVORITOS", "Usuario invitado: no se cargan favoritos de la base de datos.")
                }
            }

            // Función para añadir/quitar de favoritos
            val toggleFavorito: (String) -> Unit = { idPoi ->
                if (UserSession.esInvitado) {
                    Toast.makeText(this@MainActivity, "Regístrate para guardar favoritos", Toast.LENGTH_SHORT).show()
                }else{
                    // 1. Calculamos la nueva lista en memoria
                    val nuevaLista = if (listaFavoritosIds.contains(idPoi)) {
                        listaFavoritosIds - idPoi
                    } else {
                        listaFavoritosIds + idPoi
                    }

                    // 2. Actualizamos la UI inmediatamente
                    listaFavoritosIds = nuevaLista

                    // 3. Enviamos la lista actualizada a la base de datos en segundo plano
                    val idUsuarioActual = UserSession.usuarioId

                    lifecycleScope.launch {
                        try {
                            // Convertimos el Set a List para enviarlo por Retrofit
                            val request =
                                FavoritosRequest(favoritos = nuevaLista.toList())
                            val response = RetrofitClient.apiService.actualizarFavoritos(idUsuarioActual, request)

                            if (!response.isSuccessful) {
                                Log.e("FAVORITOS", "Error al guardar en BD: ${response.code()}")
                            } else {
                                Log.d("FAVORITOS", "Favoritos sincronizados correctamente con MongoDB")
                            }
                        } catch (e: Exception) {
                            Log.e("FAVORITOS", "Excepción al guardar favoritos", e)
                        }
                    }
                }
            }

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

            // Control de la simulación
            var modoSimulacionActiva by remember { mutableStateOf(false) }
            var pasoSimulacionActual by remember { mutableIntStateOf(0) }
            var nodoSimuladoActual by remember { mutableStateOf<Node?>(null) }

            LaunchedEffect(nodoSimuladoActual) {
                val plantaDelNodo = nodoSimuladoActual?.plantaId

                // Si el nodo simulado existe y está en una planta distinta a la actual
                if (plantaDelNodo != null && plantaDelNodo != plantaActivaId) {
                    // 1. Cambiamos la variable visual del botón iluminado
                    plantaActivaId = plantaDelNodo

                    // 2. Ejecutamos tu función para cambiar la imagen y los nodos
                    cambiarDePlanta(plantaDelNodo)
                }
            }

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

                LaunchedEffect(isLoading) {
                    // Si ya no está cargando, tenemos un aula pendiente, y el mapa está listo
                    if (!isLoading && aulaVipPendiente != null && mapaDescargado != null) {

                        // Buscamos el POI en TODAS las plantas del edificio
                        val poiVip = todosLosPoisDelEdificio.find {
                            it.nodoId == aulaVipPendiente || it.nombre == aulaVipPendiente
                        }

                        if (poiVip != null) {
                            // Averiguamos en qué planta está
                            val plantaDelPoi = mapaDescargado!!.plantas.find { p ->
                                p.pois.any { it.nodoId == poiVip.nodoId }
                            }

                            if (plantaDelPoi != null) {
                                // Si está en otra planta distinta a la baja, cambiamos
                                if (plantaActivaId != plantaDelPoi.plantaId) {
                                    plantaActivaId = plantaDelPoi.plantaId
                                    cambiarDePlanta(plantaDelPoi.plantaId)
                                }

                                // Abrimos la tarjeta de destino.
                                poiParaConfirmar = poiVip
                            }
                        }
                        aulaVipPendiente = null
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
                                },
                                onBackClick = {
                                    finish() // Cerramos el mapa
                                }
                            )
                        }
                    }
                    // =========================================================
                    // PANTALLA 2: EL MAPA INTERACTIVO
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
                                        //ListSection(Modifier.weight(1f).fillMaxHeight())
                                    }
                                } else {
                                    Column(Modifier.padding(16.dp)) {
                                        MapSection(
                                            modifier = Modifier.weight(1f).fillMaxWidth(),
                                            nodes = nodes,
                                            planoFondo = planoFondo,
                                            rutaCalculada = rutaParaDibujar,
                                            userPosition = userPosition,
                                            currentUserNode = if (modoSimulacionActiva) nodoSimuladoActual else currentUserNode,
                                            userOrientation=userOrientation,
                                            onPoiClick = { poiTocado:POI ->
                                                poiParaConfirmar = poiTocado // Abre el popup
                                            },
                                            esSimulacion = modoSimulacionActiva,
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

                                    if (!modoNavegacionActiva && !modoSimulacionActiva) {
                                        // 1. MODO BÚSQUEDA: Solo mostramos el buscador si NO estamos navegando NI simulando
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
                                        // 2. MODO NAVEGACIÓN O SIMULACIÓN
                                        val instruccionActual = if (modoSimulacionActiva) {
                                            todasLasInstrucciones.getOrNull(pasoSimulacionActual)
                                        } else {
                                            todasLasInstrucciones.firstOrNull()
                                        }

                                        LaunchedEffect(instruccionActual) {
                                            if (instruccionActual != null && vozActivada) {
                                                hablar(instruccionActual.text)
                                            }
                                        }

                                        if (instruccionActual != null) {
                                            NavigationBanner(instruccionActual)
                                        }
                                    }
                                }

                                // 2. BARRA DE NAVEGACIÓN INTEGRADA
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth() // Ocupa todo el ancho
                                        .height(80.dp),
                                    color = Color.White,
                                    tonalElevation = 8.dp,
                                    shadowElevation = 16.dp
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceEvenly // Distribuye los 3 botones por igual
                                    ) {

                                        // BOTÓN 1: EXPLORAR
                                        NavigationItem(
                                            icon = Icons.Default.Explore,
                                            label = "Explorar",
                                            color = Color(0xFF1E88E5),
                                            onClick = { mostrarHojaExplorar = true }
                                        )

                                        // BOTÓN 2: GUARDADOS (Bookmark)
                                        NavigationItem(
                                            icon = Icons.Default.Bookmark,
                                            label = "Guardado",
                                            color = Color(0xFF4CAF50),
                                            onClick = { mostrarHojaGuardados = true }
                                        )

                                        // BOTÓN 3: VOLVER (Integrado aquí)
                                        NavigationItem(
                                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                                            label = "Volver",
                                            color = Color.DarkGray,
                                            onClick = {
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
                                            }
                                        )
                                    }
                                }

                                if (mostrarHojaGuardados) {
                                    HojaGuardadosBottomSheet(
                                        listaFavoritosIds = listaFavoritosIds,
                                        todosLosPoisDelEdificio = todosLosPoisDelEdificio,
                                        onDismiss = { mostrarHojaGuardados = false },
                                        onToggleFavorito = { id -> toggleFavorito(id) },
                                        onNavigateClick = { poi ->
                                            // Cuando el usuario le da a "Ir" en la tarjeta, hacemos estas dos cosas:
                                            // 2. Activamos la tarjeta de información pasándole el POI
                                            poiParaConfirmar = poi
                                            mostrarHojaGuardados = false
                                        }
                                    )
                                }

                                if (mostrarHojaExplorar) {
                                    PantallaExplorarSheet(
                                        urlFacultad = urlWeb,
                                        onDismiss = { mostrarHojaExplorar = false }
                                    )
                                }

                                if (modoNavegacionActiva || modoSimulacionActiva) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(top = 150.dp, end = 16.dp)
                                    ) {
                                        FloatingActionButton(
                                            onClick = { vozActivada = !vozActivada },
                                            containerColor = Color.White,
                                            contentColor = if (vozActivada) Color(0xFF1E88E5) else Color.Gray,
                                            modifier = Modifier.size(48.dp),
                                            elevation = FloatingActionButtonDefaults.elevation(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (vozActivada) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                                contentDescription = if (vozActivada) "Desactivar voz" else "Activar voz"
                                            )
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

                                    modoSimulacionActiva = modoSimulacionActiva,
                                    pasoActual = pasoSimulacionActual,
                                    totalPasos = todasLasInstrucciones.size,
                                    onSimularClick = { poi ->
                                        destinoSeleccionadoId = poi.nodoId
                                        modoSimulacionActiva = true
                                        pasoSimulacionActual = 0
                                        poiParaConfirmar = null // Cerramos la tarjeta de abajo

                                        if (rutaCalculada.isEmpty()) {
                                            val idInicio = origenSeleccionadoId ?: currentUserNode?.id
                                            if (idInicio != null) {
                                                rutaCalculada = graphEngine?.findPath(idInicio, poi.nodoId) ?: emptyList()
                                            }
                                        }
                                    },
                                    onAvanzarPaso = {
                                        if (pasoSimulacionActual < todasLasInstrucciones.size - 1) pasoSimulacionActual++
                                        nodoSimuladoActual = rutaCalculada.getOrNull(pasoSimulacionActual)
                                    },
                                    onRetrocederPaso = {
                                        if (pasoSimulacionActual > 0) pasoSimulacionActual--
                                        nodoSimuladoActual = rutaCalculada.getOrNull(pasoSimulacionActual)
                                    },

                                    // "Detener Ruta"
                                    onDetenerRutaClick = {
                                        rutaCalculada = emptyList()
                                        destinoSeleccionadoId = null
                                        modoNavegacionActiva = false
                                        origenSeleccionadoId = null
                                        activarBuscadorExterno = false
                                        textoDestinoExterno = ""
                                        textoOrigenExterno=""
                                        plantaQueDebeParpadearId=null
                                        poiParaConfirmar = null
                                        modoSimulacionActiva = false
                                        nodoSimuladoActual = null
                                    },

                                    // "X" de la tarjeta
                                    onCerrarTarjetaClick = {
                                        poiParaConfirmar = null
                                    },

                                    // Le decimos qué hacer cuando pulse "Cómo llegar"
                                    onComoLlegarClick = { poi ->
                                        destinoSeleccionadoId = poi.nodoId
                                        activarBuscadorExterno = true
                                        textoDestinoExterno = poi.nombre
                                        if (origenSeleccionadoId != null) {
                                            val poiOrigen = todosLosPoisDelEdificio.find { it.nodoId == origenSeleccionadoId }
                                            textoOrigenExterno = poiOrigen?.nombre ?: "Mi ubicación"
                                        } else {
                                            textoOrigenExterno = "Mi ubicación"
                                        }

                                        val idInicio = origenSeleccionadoId ?: currentUserNode?.id
                                        if (idInicio != null) {
                                            val nuevaRuta = graphEngine?.findPath(idInicio, destinoSeleccionadoId!!)
                                            rutaCalculada = nuevaRuta ?: emptyList()

                                            // Lo mantenemos en falso para que NO salga el banner
                                            modoNavegacionActiva = false
                                        }
                                        poiParaConfirmar = null
                                    },

                                    // BOTÓN VERDE (Iniciar)
                                    onIniciarRutaClick = { poi ->
                                        destinoSeleccionadoId = poi.nodoId
                                        activarBuscadorExterno = true
                                        textoDestinoExterno = poi.nombre

                                        val idInicio = origenSeleccionadoId ?: currentUserNode?.id
                                        if (idInicio != null) {
                                            val nuevaRuta = graphEngine?.findPath(idInicio, destinoSeleccionadoId!!)
                                            rutaCalculada = nuevaRuta ?: emptyList()

                                            // Aquí SÍ activamos el banner superior y quitamos el buscador
                                            modoNavegacionActiva = true

                                            poiParaConfirmar = null
                                        }
                                    },
                                    listaFavoritosIds = listaFavoritosIds,
                                    toggleFavorito = toggleFavorito,
                                    origenEsUbicacionUsuario = origenSeleccionadoId == null || origenSeleccionadoId == currentUserNode?.id,
                                    modoSeleccionAula = modoSeleccionAula
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun hablar(texto: String) {
        if (isTtsReady) {
            tts?.speak(texto, TextToSpeech.QUEUE_FLUSH, null, null)
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
        onPoiClick: (POI) -> Unit,
        esSimulacion: Boolean = false
    ) {
        val density = LocalDensity.current

        // ESTADOS PARA EL ZOOM Y DESPLAZAMIENTO
        var scale by remember { mutableStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }

        val diccionarioNodos = remember(nodes) {
            nodes.associateBy { it.id }
        }

        // Variable para activar/desactivar las líneas con un botón
        var mostrarGrafoDebug by remember { mutableStateOf(true) }

        BoxWithConstraints(
            modifier = modifier
                // 1. AÑADIMOS ESPACIO ARRIBA Y ABAJO
                //.padding(vertical = 70.dp,horizontal = 2.dp)
                //.padding(horizontal = 2.dp)
                .padding(top = 70.dp)
                .padding(bottom = 65.dp)

                .border(2.dp, Color.White, RoundedCornerShape(8.dp)) // Un toque redondeado queda mejor
                .background(Color.White)
                .clip(RoundedCornerShape(8.dp)) // Recortamos con el mismo redondeo
                // --- DETECTOR DE GESTOS ---
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        // 1. Actualizamos escala
                        val newScale = (scale * zoom).coerceIn(1f, 5f)

                        // 2. Calculamos los límites máximos permitidos para el desplazamiento
                        // Usamos size.width y size.height que están disponibles en el pointerInput
                        val maxX = (size.width.toFloat() * (newScale - 1)) / 2
                        val maxY = (size.height.toFloat() * (newScale - 1)) / 2

                        // 3. Aplicamos el movimiento y lo "encerramos" (coerceIn) en los límites
                        val newOffset = offset + pan * newScale

                        scale = newScale
                        offset = Offset(
                            x = newOffset.x.coerceIn(-maxX, maxX),
                            y = newOffset.y.coerceIn(-maxY, maxY)
                        )
                    }
                }
        ) {
            val scaleX = constraints.maxWidth.toFloat() / ancho
            val scaleY = constraints.maxHeight.toFloat() / largo

            // CONTENEDOR QUE APLICA EL ZOOM Y MOVIMIENTO
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
            ) {
                // 1. IMAGEN DE FONDO
                planoFondo?.let { miImagenDescargada ->
                    Image(
                        bitmap = miImagenDescargada,
                        contentDescription = "Plano del edificio",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                }

                // 2. DIBUJO DE LA RUTA (CANVAS)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (rutaCalculada.size > 1) {
                        for (i in 0 until rutaCalculada.size - 1) {
                            val startNode = rutaCalculada[i]
                            val endNode = rutaCalculada[i + 1]
                            drawLine(
                                color = Color.Blue,
                                start = Offset(startNode.position.x * scaleX, startNode.position.y * scaleY),
                                end = Offset(endNode.position.x * scaleX, endNode.position.y * scaleY),
                                strokeWidth = 12f,
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        }
                    }

                    if (mostrarGrafoDebug) {
                        nodes.forEach { nodo ->
                            val startX = (nodo.position.x * scaleX).toFloat()
                            val startY = (nodo.position.y * scaleY).toFloat()

                            // 2. Dibujar las líneas hacia sus vecinos
                            nodo.neighbors.forEach { idVecino ->
                                val vecino = diccionarioNodos[idVecino]
                                if (vecino != null) {
                                    val endX = (vecino.position.x * scaleX).toFloat()
                                    val endY = (vecino.position.y * scaleY).toFloat()

                                    drawLine(
                                        color = Color.Cyan.copy(alpha = 0.6f),
                                        start = Offset(startX, startY),
                                        end = Offset(endX, endY),
                                        strokeWidth = 4f
                                    )
                                } else {
                                    //Log.e("Grafo_Debug", "El nodo ${nodo.id} apunta a un vecino que no existe: $idVecino")
                                }
                            }

                            // 3. Dibujar un puntito rojo en cada nodo
                            drawCircle(
                                color = Color.Red.copy(alpha = 0.8f),
                                radius = 6f, // Un pelín más grande también
                                center = Offset(startX, startY)
                            )
                        }
                    }
                }

                // 3. NODOS DE NAVEGACIÓN
                nodes.forEach { node ->
                    val xDp = with(density) { (node.position.x * scaleX).toDp() }
                    val yDp = with(density) { (node.position.y * scaleY).toDp() }
                    Box(
                        modifier = Modifier
                            .offset(xDp - 3.dp, yDp - 3.dp)
                            .size(6.dp)
                            .background(Color.Green, shape = CircleShape)
                            .border(1.dp, Color.Black, CircleShape)
                    ) {
                        Text(node.id, color = Color.Black, fontSize = 6.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // 4. BEACONS CONOCIDOS
                knownBeacons.values.forEach { beaconPos ->
                    val xDp = with(density) { (beaconPos.x * scaleX).toDp() }
                    val yDp = with(density) { (beaconPos.y * scaleY).toDp() }
                    Box(
                        modifier = Modifier
                            .offset(xDp, yDp)
                            .size(4.dp)
                            .background(Color.Red, shape = MaterialTheme.shapes.small)
                    )
                }

                // 5. POSICIÓN DEL USUARIO (Círculo Azul)
                userPosition?.let { pos ->
                    val xDp = with(density) { (pos.x * scaleX).toDp() }
                    val yDp = with(density) { (pos.y * scaleY).toDp() }
                    Box(
                        modifier = Modifier
                            .offset(xDp, yDp)
                            .size(15.dp)
                            .background(Color.Blue, shape = CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                    )
                }

                // 6. ICONO DE NAVEGACIÓN (Flecha Magenta)
                currentUserNode?.let { node ->
                    val xDp = with(density) { (node.position.x * scaleX).toDp() }
                    val yDp = with(density) { (node.position.y * scaleY).toDp() }
                    val colorNodo = if (esSimulacion) Color(0xFFFF5722) else Color.Magenta
                    Icon(
                        imageVector = Icons.Filled.Navigation,
                        contentDescription = null,
                        tint = colorNodo,
                        modifier = Modifier
                            .offset(xDp - 4.dp, yDp - 4.dp)
                            .size(8.dp)
                            .rotate(userOrientation)
                    )
                }

                // 7. PUNTOS DE INTERÉS (POIs)
                pois.forEach { poi ->
                    val nodoDelPoi = nodes.find { it.id == poi.nodoId }
                    if (nodoDelPoi != null) {
                        val xDp = with(density) { (nodoDelPoi.position.x * scaleX).toDp() }
                        val yDp = with(density) { (nodoDelPoi.position.y * scaleY).toDp() }
                        val esDestino = (poi.nodoId == destinoSeleccionadoId || poi.nodoId == poiParaConfirmar?.nodoId)

                        val estilo = obtenerEstiloPoi(poi.nombre)

                        // 2. Tamaños un poco más grandes para que el icono vectorial se distinga bien
                        val tamanoCaja = if (esDestino) 24.dp else 18.dp
                        val ajusteOffset = tamanoCaja / 2

                        val colorFondo = if (esDestino) Color(0xFFD32F2F) else estilo.color

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .offset(xDp - ajusteOffset, yDp - ajusteOffset)
                                .size(tamanoCaja)
                                .background(colorFondo, shape = CircleShape)
                                .border(if (esDestino) 2.dp else 1.dp, Color.White, CircleShape)
                                .clickable { onPoiClick(poi) }
                        ) {
                            Icon(
                                imageVector = estilo.icono,
                                contentDescription = poi.nombre,
                                tint = Color.White, // Pintamos el icono de blanco para que contraste con el fondo de color
                                modifier = Modifier.padding(3.dp) // Un pequeño margen para que el icono respire dentro del círculo
                            )
                        }
                    }
                }
            }
        }
    }

    private fun cargarDatosDesdeServidor(idSeleccionado: String) {
        lifecycleScope.launch {
            try {
                mapaDescargado = RetrofitClient.apiService.getMapa(idSeleccionado)

                // 2. Protegemos el acceso con '?.let' para asegurarnos de que el mapa no es nulo
                mapaDescargado?.let { mapa ->
                    // Obtenemos la primera planta (Planta Baja) como planta activa por defecto
                    val plantaActivaPredeterminada = mapa.plantas.getOrNull(0)
                    Log.d("API_TFG", "Planta seleccionada: ${plantaActivaPredeterminada?.nombre}")

                    // Ahora es seguro acceder a las dimensiones
                    ancho = mapa.dimensiones.ancho
                    largo = mapa.dimensiones.largo
                    urlWeb = mapa.urlWeb

                    plantaActivaPredeterminada?.let { planta ->
                        // Accedemos a los datos DENTRO de la planta
                        knownBeacons = planta.knownBeacons

                        nodes = planta.nodos
                        pois = planta.pois

                        // Convertimos la imagen de ESTA planta
                        planoFondo = base64ToImageBitmap(planta.imagenBase64)
                    }

                    val todosLosNodosDelEdificio = mapa.plantas.flatMap { it.nodos }
                    todosLosPoisDelEdificio = mapa.plantas.flatMap { it.pois }

                    // Inicializamos los motores
                    engine = PositioningEngine(knownBeacons, ancho, largo)
                    graphEngine = GraphEngine(todosLosNodosDelEdificio)

                    // Todo listo, quitamos la pantalla de carga
                    isLoading = false

                    // 3. Arreglado el texto del Log usando las variables reales
                    Log.d("API_TFG", "¡Éxito! Nodos: ${nodes.size}, Ancho: $ancho m, Largo: $largo m")
                }

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

            devices.clear()
            currentSmoothedPosition = null
            lastDrawnPosition = null

            // 3. RESETEO TOTAL DE VARIABLES DE POSICIONAMIENTO
            if (rutaCalculada.isEmpty()) {

                currentUserNode = null
                userPosition = null
            }

            // IMPORTANTE: El motor de posicionamiento SÍ se reinicia con los beacons de esta planta
            engine = PositioningEngine(knownBeacons, ancho, largo)
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

    // --- CÓDIGO SCAN CALLBACK ---
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

                                if (modoNavegacionActiva && origenSeleccionadoId == null && destinoSeleccionadoId != null) {

                                    val nuevaRuta = graphEngine?.findPath(
                                        snappedNode.id,
                                        destinoSeleccionadoId!!
                                    )

                                    if (nuevaRuta != null && nuevaRuta.isNotEmpty()) {
                                        rutaCalculada = nuevaRuta
                                        Log.d(TAG, "🔄 Ruta recalculada. Pasos: ${nuevaRuta.size}")
                                    } else {
                                        // Si da error, es mejor no vaciar la ruta calculada de golpe,
                                        // por si es solo un pequeño error de cobertura de 1 segundo.
                                        Log.d(TAG, "✅ Has llegado al destino o no se pudo recalcular.")
                                    }
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

    override fun onDestroy() {
        super.onDestroy()

        // Apagar motor de voz para liberar memoria
        tts?.stop()
        tts?.shutdown()

        // 1. Detenemos el escáner Bluetooth para no drenar la batería
        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                scanner?.stopScan(scanCallback)
                Log.d(TAG, "🛑 Escáner BLE detenido correctamente al salir del mapa.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al detener el escáner: ${e.message}")
        }

        // 2. Limpiamos las listas pesadas de la memoria
        devices.clear()
        activeBeacons.clear()

        Log.d(TAG, "🧹 MainActivity destruida y memoria liberada.")
    }
}