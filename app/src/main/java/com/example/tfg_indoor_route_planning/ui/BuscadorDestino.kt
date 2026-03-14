package com.example.tfg_indoor_route_planning.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.tfg_indoor_route_planning.models.POI
import androidx.compose.material3.Divider
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

@Composable
fun BuscadorDestino(
    pois: List<POI>,
    rutaActiva: Boolean,
    onRutaConfirmada: (origenId: String?, destino: POI) -> Unit,
    onSoloVerDestino: (POI) -> Unit
) {
    // Restauramos la variable interna que controlaba la doble barra perfectamente
    var modoRuta by remember { mutableStateOf(false) }
    var textoOrigen by remember { mutableStateOf("") }
    var textoDestino by remember { mutableStateOf("") }
    var destinoSeleccionado by remember { mutableStateOf<POI?>(null) }

    var editandoOrigen by remember { mutableStateOf(false) }
    var buscadorActivo by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val focusRequesterOrigen = remember { FocusRequester() }
    val context = LocalContext.current

    // Si arranca la ruta, cerramos la lista desplegable al instante
    LaunchedEffect(rutaActiva) {
        if (rutaActiva) {
            buscadorActivo = false
            focusManager.clearFocus()

            //Si arrancó la ruta y el origen estaba en blanco, escribe "Mi ubicación"
            if (textoOrigen.isEmpty()) {
                textoOrigen = "Mi ubicación"
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).zIndex(1f)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            if (!modoRuta) {
                // MODO 1: BÚSQUEDA SIMPLE (Solo Destino)
                TextField(
                    value = textoDestino,
                    onValueChange = { textoDestino = it; buscadorActivo = it.isNotEmpty() },
                    placeholder = { Text("Buscar destino...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    trailingIcon = {
                        if (textoDestino.isNotEmpty()) {
                            IconButton(onClick = { textoDestino = ""; buscadorActivo = false }) {
                                Icon(Icons.Default.Clear, contentDescription = "Borrar")
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    modifier = Modifier.fillMaxWidth().onFocusChanged { if (it.isFocused) editandoOrigen = false },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )
            } else {
                // MODO 2: NAVEGACIÓN COMPACTA
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        modoRuta = false
                        textoOrigen = ""
                        textoDestino = ""
                        destinoSeleccionado = null
                        buscadorActivo = false
                        focusManager.clearFocus()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }

                    Column(modifier = Modifier.weight(1f)) {

                        // CAMPO ORIGEN COMPACTO
                        Row(
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.RadioButtonUnchecked, contentDescription = "Origen", tint = Color(0xFFFBC02D), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                                if (textoOrigen.isEmpty()) Text("Elige punto de partida...", color = Color.Gray, fontSize = 15.sp)
                                BasicTextField(
                                    value = textoOrigen,
                                    onValueChange = { textoOrigen = it; buscadorActivo = true },
                                    singleLine = true,
                                    textStyle = TextStyle(color = Color.Black, fontSize = 15.sp),
                                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequesterOrigen)
                                        .onFocusChanged { if (it.isFocused) { editandoOrigen = true; buscadorActivo = true } }
                                )
                            }
                        }

                        // Retraso seguro para el teclado
                        LaunchedEffect(modoRuta, editandoOrigen) {
                            if (modoRuta && editandoOrigen) {
                                kotlinx.coroutines.delay(50)
                                try { focusRequesterOrigen.requestFocus() } catch (e: Exception) {}
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp), color = Color.LightGray.copy(alpha = 0.5f))

                        // CAMPO DESTINO COMPACTO
                        Row(
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = "Destino", tint = Color(0xFF1E88E5), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                                if (textoDestino.isEmpty()) Text("Elige destino...", color = Color.Gray, fontSize = 15.sp)
                                BasicTextField(
                                    value = textoDestino,
                                    onValueChange = { textoDestino = it; buscadorActivo = true },
                                    singleLine = true,
                                    textStyle = TextStyle(color = Color.Black, fontSize = 15.sp),
                                    modifier = Modifier.fillMaxWidth()
                                        .onFocusChanged { if (it.isFocused) { editandoOrigen = false; buscadorActivo = true } }
                                )
                            }
                        }
                    }

                    IconButton(onClick = {
                        val temp = textoOrigen
                        textoOrigen = textoDestino
                        textoDestino = temp
                    }) {
                        Icon(Icons.Default.SwapVert, contentDescription = "Intercambiar", tint = Color.Gray)
                    }
                }
            }
        }

        // =========================================================
        // LISTA DE SUGERENCIAS DESPLEGABLE
        // =========================================================
        if (buscadorActivo) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                LazyColumn(modifier = Modifier.heightIn(max = 250.dp)) {

                    if (modoRuta && editandoOrigen && textoOrigen.isEmpty()) {
                        item {
                            Row(modifier = Modifier.fillMaxWidth().clickable {
                                textoOrigen = "Mi ubicación"
                                buscadorActivo = false
                                focusManager.clearFocus()
                                if (destinoSeleccionado != null) onRutaConfirmada(null, destinoSeleccionado!!)
                            }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color(0xFF1E88E5))
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Tu ubicación", color = Color(0xFF1E88E5), fontWeight = FontWeight.Bold)
                            }
                            Divider(color = Color.LightGray.copy(alpha = 0.5f))
                        }
                    }

                    val textoBusqueda = if (editandoOrigen) textoOrigen else textoDestino
                    val poisFiltrados = pois.filter { it.nombre.contains(textoBusqueda, ignoreCase = true) }

                    items(poisFiltrados) { poi ->
                        Row(modifier = Modifier.fillMaxWidth().clickable {
                            val intentandoPonerMismoSitio = if (editandoOrigen) poi.nombre == textoDestino else poi.nombre == textoOrigen
                            if (intentandoPonerMismoSitio) {
                                Toast.makeText(context, "El origen y destino no pueden ser iguales", Toast.LENGTH_SHORT).show()
                                return@clickable
                            }

                            if (!modoRuta) {
                                textoDestino = poi.nombre
                                destinoSeleccionado = poi
                                modoRuta = true
                                editandoOrigen = false
                                buscadorActivo = false
                                focusManager.clearFocus()
                                textoOrigen = ""
                                onSoloVerDestino(poi)
                            } else {
                                if (editandoOrigen) {
                                    textoOrigen = poi.nombre
                                    focusManager.clearFocus()
                                    if (destinoSeleccionado != null) onRutaConfirmada(poi.nodoId, destinoSeleccionado!!)
                                } else {
                                    textoDestino = poi.nombre
                                    destinoSeleccionado = poi
                                    editandoOrigen = true
                                }
                                buscadorActivo = false
                            }
                        }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = poi.nombre, color = Color.Black, fontWeight = FontWeight.Medium)
                        }
                        Divider(color = Color.LightGray.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}