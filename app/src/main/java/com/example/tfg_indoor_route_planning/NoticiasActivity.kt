package com.example.tfg_indoor_route_planning

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tfg_indoor_route_planning.web_scrapping.NoticiaUma
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class NoticiasActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                NoticiasScreen(onVolver = { finish() })
            }
        }
    }
}

data class EventoUma(
    val titulo: String,
    val categoria: String,
    val colorHex: String,
    val dia: String,
    val mes: String,
    val link: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticiasScreen(onVolver: () -> Unit) {
    var noticias by remember { mutableStateOf<List<NoticiaUma>>(emptyList()) }
    var eventos by remember { mutableStateOf<List<EventoUma>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    // Estado para saber qué pestaña está seleccionada: 0 = Noticias, 1 = Eventos
    var tabSeleccionada by remember { mutableIntStateOf(0) }
    val titulosTabs = listOf("Noticias", "Eventos")

    // Cargamos ambas cosas a la vez al entrar
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            // async permite descargar ambas cosas al mismo tiempo sin esperar una a la otra
            val deferredNoticias = async { obtenerNoticiasUMA() }
            val deferredEventos = async { obtenerEventosUMA() }

            noticias = deferredNoticias.await()
            eventos = deferredEventos.await()
            cargando = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Actualidad UMA", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(paddingValues)
        ) {
            // TABS
            TabRow(
                selectedTabIndex = tabSeleccionada,
                containerColor = Color.White,
                contentColor = Color(0xFF6200EE),
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[tabSeleccionada]),
                        color = Color(0xFF6200EE),
                        height = 3.dp
                    )
                }
            ) {
                titulosTabs.forEachIndexed { index, titulo ->
                    Tab(
                        selected = tabSeleccionada == index,
                        onClick = { tabSeleccionada = index },
                        text = {
                            Text(
                                text = titulo,
                                fontWeight = if (tabSeleccionada == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (tabSeleccionada == index) Color(0xFF6200EE) else Color.Gray
                            )
                        }
                    )
                }
            }

            // CONTENIDO DE LA PESTAÑA SELECCIONADA
            Box(modifier = Modifier.fillMaxSize()) {
                if (cargando) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF6200EE))
                } else {
                    if (tabSeleccionada == 0) {
                        // CONTENIDO: NOTICIAS
                        if (noticias.isEmpty()) {
                            Text("No se pudieron cargar las noticias.", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(noticias) { noticia -> NoticiaCard(noticia) }
                            }
                        }
                    } else {
                        // CONTENIDO: EVENTOS
                        if (eventos.isEmpty()) {
                            Text("No se pudieron cargar los eventos.", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(eventos) { evento -> EventoCard(evento) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoticiaCard(noticia: NoticiaUma) {
    val context = LocalContext.current
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                noticia.link?.let { enlace ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(enlace))
                    context.startActivity(intent)
                }
            }
    ) {
        Column {
            if (!noticia.imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = noticia.imageUrl,
                    contentDescription = "Imagen de la noticia",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = noticia.titulo, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = noticia.descripcion, fontSize = 13.sp, color = Color.DarkGray, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun EventoCard(evento: EventoUma) {
    val context = LocalContext.current

    // Intentamos parsear el color hexadecimal que sacamos del HTML
    val colorCategoria = try {
        Color(android.graphics.Color.parseColor(evento.colorHex))
    } catch (e: Exception) {
        Color.Gray // Si falla, gris por defecto
    }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                evento.link?.let { enlace ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(enlace))
                    context.startActivity(intent)
                }
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // BLOQUE IZQUIERDO: FECHA CON FONDO DE COLOR SUAVE
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(70.dp)
                    .background(colorCategoria.copy(alpha = 0.15f))
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = evento.dia, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = colorCategoria)
                Text(text = evento.mes, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colorCategoria)
            }

            // BLOQUE DERECHO: TEXTOS
            Column(
                modifier = Modifier.weight(1f).padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = evento.categoria.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorCategoria
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = evento.titulo,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

suspend fun obtenerNoticiasUMA(): List<NoticiaUma> {
    return withContext(Dispatchers.IO) {
        val listaNoticias = mutableListOf<NoticiaUma>()
        val enlacesProcesados = mutableSetOf<String>()
        try {
            val urlBase = "https://www.uma.es"
            val doc = Jsoup.connect(urlBase).get()
            val elementosNoticia = doc.select("#carousel123 .carousel-inner > div > div[class^=col-]")

            for (elemento in elementosNoticia) {
                val enlaceParcial = elemento.select("p.titulo-noticia a").attr("href")
                if (enlacesProcesados.contains(enlaceParcial) || enlaceParcial.isBlank()) continue

                enlacesProcesados.add(enlaceParcial)
                val enlaceCompleto = urlBase + enlaceParcial
                val titulo = elemento.select("p.titulo-noticia a").text()
                val descripcion = elemento.select("p.cuerpo-noticia").text()
                val imgParcial = elemento.select("img.img-responsive").attr("src")
                val imagenCompleta = if (imgParcial.isNotBlank()) urlBase + imgParcial else null

                if (titulo.isNotBlank()) {
                    listaNoticias.add(NoticiaUma(titulo, descripcion, imagenCompleta, enlaceCompleto))
                }
            }
        } catch (e: Exception) {
            Log.e("SCRAPING_UMA", "Error extrayendo noticias: ${e.message}")
        }
        listaNoticias
    }
}

suspend fun obtenerEventosUMA(): List<EventoUma> {
    return withContext(Dispatchers.IO) {
        val listaEventos = mutableListOf<EventoUma>()
        try {
            val urlBase = "https://www.uma.es"
            val doc = Jsoup.connect(urlBase).get()

            val elementosEvento = doc.select(".eventos-home .event-row")

            for (elemento in elementosEvento) {
                // Título y enlace
                val tagEnlace = elemento.select(".titulo-agenda a")
                val titulo = tagEnlace.text()
                if (titulo.isBlank()) continue

                val enlaceParcial = tagEnlace.attr("href")
                val enlaceCompleto = if (enlaceParcial.startsWith("http")) enlaceParcial else urlBase + enlaceParcial

                val categoria = elemento.select(".nombre-filtro span").text()

                val styleColor = elemento.select(".nombre-filtro").attr("style")
                val colorHex = if (styleColor.contains("#")) {
                    "#" + styleColor.substringAfter("#").take(6)
                } else {
                    "#6200EE"
                }

                // Fecha
                val dia = elemento.select(".dia-agenda").text()
                val mes = elemento.select(".mes-agenda").text()

                listaEventos.add(EventoUma(titulo, categoria, colorHex, dia, mes, enlaceCompleto))
            }
        } catch (e: Exception) {
            Log.e("SCRAPING_UMA", "Error extrayendo eventos: ${e.message}")
        }
        listaEventos
    }
}