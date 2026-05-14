package com.example.tfg_indoor_route_planning.web_scrapping

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import android.webkit.CookieManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Campaign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaExplorarSheet(
    urlFacultad: String, // <--- ¡NUEVO INPUT AQUÍ!
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // --- ESTADOS ---
    var noticias by remember { mutableStateOf<List<NoticiaUma>>(emptyList()) }
    var destacados by remember { mutableStateOf<List<DestacadoUma>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    var pestanaActiva by remember { mutableIntStateOf(0) }

    // --- LÓGICA DE CARGA (Scraping) ---
    // Reactivamos el bloque si cambia la urlFacultad
    LaunchedEffect(urlFacultad) {
        if (urlFacultad.isEmpty()) {
            cargando = false
            mensajeError = "No hay URL disponible para esta facultad."
            return@LaunchedEffect
        }

        withContext(Dispatchers.IO) {
            try {
                // Jsoup se conecta dinámicamente a la URL de la facultad seleccionada
                val docPortada = Jsoup.connect(urlFacultad).get()

                // Scraping Noticias
                noticias = docPortada.select("#noticias-carousel .item").map { el ->
                    NoticiaUma(
                        titulo = el.select("h3, h4, strong").first()?.text() ?: "Noticia",
                        descripcion = el.select("p").first()?.text() ?: "",
                        imageUrl = el.select("img").first()?.attr("abs:src"),
                        // abs:href obtiene automáticamente la ruta completa sea cual sea la facultad
                        link = el.select("a").first()?.attr("abs:href")?.takeIf { it.isNotEmpty() }
                    )
                }

                // Scraping Destacados
                destacados = docPortada.select("#destacados-carousel .item").map { el ->
                    DestacadoUma(
                        titulo = el.select("h4").text().ifEmpty { "Destacado" },
                        descripcion = el.select("p").text(),
                        imageUrl = el.select("img").first()?.attr("abs:src"),
                        link = el.select("a").first()?.attr("abs:href")?.takeIf { it.isNotEmpty() }
                    )
                }
            } catch (e: Exception) {
                mensajeError = e.localizedMessage
            } finally {
                cargando = false
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.9f) // Hacemos que la hoja sea casi pantalla completa
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // 1. CABECERA
            Text(
                text = "Explorar Facultad",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            // 2. SELECTOR DE PESTAÑAS (3 TABS)
            TabRow(selectedTabIndex = pestanaActiva) {
                Tab(
                    selected = pestanaActiva == 0,
                    onClick = { pestanaActiva = 0 },
                    text = { Text("Noticias") },
                    icon = { Icon(Icons.Default.Newspaper, null) }
                )
                Tab(
                    selected = pestanaActiva == 1,
                    onClick = { pestanaActiva = 1 },
                    text = { Text("Destacados") },
                    icon = { Icon(Icons.Default.Campaign, null) }
                )
                Tab(
                    selected = pestanaActiva == 2,
                    onClick = { pestanaActiva = 2 },
                    text = { Text("Estadísticas") },
                    icon = { Icon(Icons.Default.BarChart, null) }
                )
            }

            // 3. CONTENIDO DINÁMICO
            Box(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                when (pestanaActiva) {
                    0 -> ListadoNoticias(noticias, cargando, mensajeError, context)
                    1 -> ListadoDestacados(destacados, cargando, mensajeError, context)
                    2 -> PantallaPowerBI() // Pestaña de Power BI
                }
            }
        }
    }
}

// COMPONENTE: POWER BI (WEBVIEW)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PantallaPowerBI() {
    val context = LocalContext.current

    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true

                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false

                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
            }

            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                    Log.e("PowerBI_Espia", "JS Error: ${consoleMessage?.message()} -- Línea: ${consoleMessage?.lineNumber()}")
                    return super.onConsoleMessage(consoleMessage)
                }
            }

            webViewClient = object : WebViewClient() {
                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                    Log.e("PowerBI_Espia", "Error de Red: ${error?.description}")
                    super.onReceivedError(view, request, error)
                }

                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    return false
                }
            }

            loadUrl("https://app.powerbi.com/view?r=eyJrIjoiNjk4MDQ1MDUtNjM5Ni00MmMzLTgzODktMDRiMGZiM2NlMzhiIiwidCI6ImU3ZjUzZjNmLTYzNmItNDNhZC04MDdlLTU3Yzk2NmZmN2RiOCIsImMiOjh9")
        }
    }

    AndroidView(modifier = Modifier.fillMaxSize(), factory = { webView })
}

// OTROS LISTADOS

@Composable
fun ListadoNoticias(noticias: List<NoticiaUma>, cargando: Boolean, error: String?, context: android.content.Context) {
    if (cargando) CircularProgressIndicator(modifier = Modifier.fillMaxWidth().wrapContentSize(Alignment.Center))
    else if (error != null) Text("Error al cargar noticias: $error", color = Color.Red, modifier = Modifier.padding(16.dp))
    else if (noticias.isEmpty()) Text("No hay noticias disponibles en este momento.", modifier = Modifier.padding(16.dp))
    else {
        LazyColumn {
            items(noticias) { noticia ->
                CardNoticia(noticia, context)
            }
        }
    }
}

@Composable
fun CardNoticia(noticia: NoticiaUma, context: android.content.Context) {
    Card(
        modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()
            .clickable {
                noticia.link?.let { url ->
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            }
    ) {
        Column {
            noticia.imageUrl?.let {
                AsyncImage(
                    model = it, contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(noticia.titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(noticia.descripcion, fontSize = 14.sp, color = Color.DarkGray, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

//LISTADO Y TARJETA PARA DESTACADOS
@Composable
fun ListadoDestacados(destacados: List<DestacadoUma>, cargando: Boolean, error: String?, context: android.content.Context) {
    if (cargando) CircularProgressIndicator(modifier = Modifier.fillMaxWidth().wrapContentSize(Alignment.Center))
    else if (error != null) Text("Error al cargar destacados: $error", color = Color.Red, modifier = Modifier.padding(16.dp))
    else if (destacados.isEmpty()) Text("No hay destacados disponibles en este momento.", modifier = Modifier.padding(16.dp))
    else {
        LazyColumn {
            items(destacados) { destacado ->
                CardDestacado(destacado, context)
            }
        }
    }
}

@Composable
fun CardDestacado(destacado: DestacadoUma, context: android.content.Context) {
    Card(
        modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()
            .clickable {
                destacado.link?.let { url ->
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            }
    ) {
        Column {
            destacado.imageUrl?.let {
                AsyncImage(
                    model = it, contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(destacado.titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(destacado.descripcion, fontSize = 14.sp, color = Color.DarkGray, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}