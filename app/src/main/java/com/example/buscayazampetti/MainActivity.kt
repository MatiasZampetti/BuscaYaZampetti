package com.example.buscayazampetti

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.SubcomposeAsyncImage
import com.example.buscayazampetti.data.model.FarmaciaUi
import com.example.buscayazampetti.data.model.SearchResult
import com.example.buscayazampetti.ui.DetalleResultadoScreen
import com.example.buscayazampetti.ui.DetalleScreen
import com.example.buscayazampetti.ui.FarmaciaAdapter
import com.example.buscayazampetti.ui.theme.BuscaYaZampettiTheme
import com.example.buscayazampetti.viewmodel.FarmaciaUiState
import com.example.buscayazampetti.viewmodel.FarmaciaViewModel
import com.example.buscayazampetti.viewmodel.SearchUiState

// ── Colores ───────────────────────────────────────────────────────────────────
val AzulPrimario = Color(0xFF2B6FFF)
val AzulOscuro   = Color(0xFF0A1F5C)
val FondoClaro   = Color(0xFFF2F5FF)
val Blanco       = Color(0xFFFFFFFF)
val TextoGris    = Color(0xFF8A94A6)
val TextoOscuro  = Color(0xFF1A2340)

val avatarColors = listOf(
    Color(0xFF00C48C),
    Color(0xFF2B6FFF),
    Color(0xFFFF6B6B),
    Color(0xFFFF9F43),
    Color(0xFF9B59B6),
)

val GradienteHeader = Brush.verticalGradient(
    colors = listOf(AzulOscuro, AzulPrimario)
)

// ── Activity ──────────────────────────────────────────────────────────────────
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BuscaYaZampettiTheme {
                val navController = rememberNavController()
                // El ViewModel necesita el repositorio (Room + API), así que
                // le pasamos la fábrica para que Android sepa cómo crearlo.
                val viewModel: FarmaciaViewModel = viewModel(
                    factory = FarmaciaViewModel.factory(applicationContext)
                )

                NavHost(navController = navController, startDestination = "lista") {
                    composable("lista") {
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            BuscaYaScreen(
                                modifier = Modifier.padding(innerPadding),
                                viewModel = viewModel,
                                onVerDetalle = { farmacia ->
                                    viewModel.seleccionar(farmacia)
                                    navController.navigate("detalle")
                                },
                                onVerDetalleResult = { result ->
                                    viewModel.seleccionarResult(result)
                                    navController.navigate("detalle_resultado")
                                }
                            )
                        }
                    }
                    composable("detalle") {
                        val farmacia by viewModel.selectedFarmacia.collectAsState()
                        farmacia?.let {
                            DetalleScreen(farmacia = it, navController = navController)
                        }
                    }
                    composable("detalle_resultado") {
                        val result by viewModel.selectedResult.collectAsState()
                        result?.let {
                            DetalleResultadoScreen(result = it, navController = navController)
                        }
                    }
                }
            }
        }
    }
}

// ── Pantalla principal ────────────────────────────────────────────────────────
@Composable
fun BuscaYaScreen(
    modifier: Modifier = Modifier,
    viewModel: FarmaciaViewModel,
    onVerDetalle: (FarmaciaUi) -> Unit = {},
    onVerDetalleResult: (SearchResult) -> Unit = {}
) {
    var busqueda by remember { mutableStateOf("") }
    // collectAsStateWithLifecycle = el equivalente en Compose de repeatOnLifecycle:
    // deja de escuchar el StateFlow cuando la app pasa a segundo plano.
    val farmaciaState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchState   by viewModel.searchState.collectAsStateWithLifecycle()
    val filtroLocal   by viewModel.filtroLocal.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier.fillMaxSize().background(FondoClaro),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Header
        item {
            HeaderGradiente(
                busqueda         = busqueda,
                onBusquedaChange = {
                    busqueda = it
                    if (it.isBlank()) viewModel.limpiarBusqueda()
                },
                onBuscar = { viewModel.buscar(busqueda) }
            )
        }

        // ── Resultados de búsqueda ────────────────────────────────────────────
        when (val ss = searchState) {
            is SearchUiState.Cargando -> item {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AzulPrimario, modifier = Modifier.size(28.dp))
                }
            }
            is SearchUiState.Error -> item {
                Text(
                    text = "Error al buscar: ${ss.mensaje}",
                    color = Color(0xFFFF6B6B),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
            is SearchUiState.Exito -> {
                if (ss.results.isNotEmpty()) {
                    item {
                        SeccionHeader(
                            titulo    = "Resultados",
                            subtitulo = "${ss.results.size} resultados encontrados"
                        )
                    }
                    items(ss.results) { result ->
                        SearchResultCard(result, onVerDetalleResult)
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                } else {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Sin resultados", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextoOscuro)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("No encontramos nada para tu búsqueda", fontSize = 13.sp, color = TextoGris)
                            }
                        }
                    }
                }
            }
            is SearchUiState.Idle -> { }
        }

        // ── Farmacias de turno (siempre visibles, leídas desde Room) ─────────
        val fs = farmaciaState
        val refrescando = fs is FarmaciaUiState.Exito && fs.refrescando

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(end = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    SeccionHeader(
                        titulo    = "Farmacias de Turno",
                        subtitulo = "08:00 am — 08:00 am del día siguiente"
                    )
                }
                // Botón "Actualizar": fuerza ir a la API aunque haya datos guardados.
                if (refrescando) {
                    CircularProgressIndicator(
                        color = AzulPrimario,
                        strokeWidth = 2.dp,
                        modifier = Modifier.padding(end = 12.dp).size(20.dp)
                    )
                } else {
                    TextButton(onClick = { viewModel.refrescar(forzar = true) }) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = AzulPrimario, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Actualizar", color = AzulPrimario, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Buscador local: arriba de la lista. Cada letra que se escribe
        // dispara una consulta con LIKE en Room (no filtra en memoria).
        item {
            BuscadorLocal(
                texto = filtroLocal,
                onTextoChange = { viewModel.filtrarLocal(it) }
            )
        }

        when (fs) {
            is FarmaciaUiState.Cargando -> item {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AzulPrimario)
                }
            }
            is FarmaciaUiState.Error -> item {
                EstadoError(fs.mensaje) { viewModel.refrescar(forzar = true) }
            }
            is FarmaciaUiState.Exito -> {
                listaFarmacias(fs.farmacias, filtroLocal, onVerDetalle)
            }
            is FarmaciaUiState.SinConexion -> {
                // Aviso arriba: los datos pueden estar desactualizados.
                item { BannerSinConexion(fs.mensaje) }
                listaFarmacias(fs.farmacias, filtroLocal, onVerDetalle)
            }
        }
    }
}

// ── Lista de farmacias (la usan los estados Exito y SinConexion) ─────────────
private fun LazyListScope.listaFarmacias(
    farmacias: List<FarmaciaUi>,
    filtro: String,
    onVerDetalle: (FarmaciaUi) -> Unit
) {
    if (farmacias.isEmpty()) {
        item {
            val texto = if (filtro.isBlank()) {
                "Todavía no hay farmacias cargadas"
            } else {
                "No hay farmacias que coincidan con \"$filtro\""
            }
            Text(
                text = texto,
                fontSize = 13.sp,
                color = TextoGris,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp, horizontal = 24.dp)
            )
        }
    } else {
        item { FarmaciasRecyclerView(farmacias, onVerDetalle) }
    }
}

// ── RecyclerView de farmacias ─────────────────────────────────────────────────
// La pantalla está hecha en Compose, así que el RecyclerView (una vista clásica)
// se mete con AndroidView. Debajo del buscador, siempre muestra lo que hay en Room.
@Composable
fun FarmaciasRecyclerView(
    farmacias: List<FarmaciaUi>,
    onVerDetalle: (FarmaciaUi) -> Unit
) {
    // remember: el adapter se crea UNA vez, no en cada recomposición.
    val adapter = remember { FarmaciaAdapter(onVerDetalle) }

    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { context ->
            RecyclerView(context).apply {
                // El scroll lo hace la pantalla entera (el LazyColumn de afuera),
                // así que el RecyclerView muestra todas sus filas sin scrollear él.
                layoutManager = object : LinearLayoutManager(context) {
                    override fun canScrollVertically() = false
                }
                isNestedScrollingEnabled = false
                itemAnimator = null
                this.adapter = adapter
            }
        },
        // Cada vez que Room emite una lista nueva (por ejemplo, al escribir en
        // el buscador), se la pasamos al adapter y DiffUtil actualiza las filas.
        update = { adapter.submitList(farmacias) }
    )
}

// ── Buscador local (Room) ─────────────────────────────────────────────────────
@Composable
fun BuscadorLocal(texto: String, onTextoChange: (String) -> Unit) {
    OutlinedTextField(
        value = texto,
        onValueChange = onTextoChange,
        placeholder = { Text("Filtrar farmacias por nombre...", color = TextoGris, fontSize = 14.sp) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextoGris) },
        trailingIcon = {
            // Cruz para borrar el filtro (solo si hay algo escrito).
            if (texto.isNotEmpty()) {
                IconButton(onClick = { onTextoChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Borrar filtro", tint = TextoGris)
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor   = Blanco,
            unfocusedContainerColor = Blanco,
            focusedBorderColor      = AzulPrimario,
            unfocusedBorderColor    = Color.Transparent,
            focusedTextColor        = TextoOscuro,
            unfocusedTextColor      = TextoOscuro,
            cursorColor             = AzulPrimario
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp)
    )
}

// ── Aviso "sin conexión" ──────────────────────────────────────────────────────
@Composable
fun BannerSinConexion(mensaje: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFFF3CD))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.WifiOff, contentDescription = null, tint = Color(0xFF664D03), modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(mensaje, fontSize = 13.sp, color = Color(0xFF664D03), fontWeight = FontWeight.Medium)
    }
}

// ── Header ────────────────────────────────────────────────────────────────────
@Composable
fun HeaderGradiente(
    busqueda: String,
    onBusquedaChange: (String) -> Unit,
    onBuscar: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(brush = GradienteHeader)
            .padding(horizontal = 24.dp)
            .padding(top = 32.dp, bottom = 28.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {

            Text("BuscaYa", fontSize = 38.sp, fontWeight = FontWeight.Black, color = Blanco, letterSpacing = (-0.5).sp)

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Blanco.copy(alpha = 0.6f)))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Villa del Rosario", fontSize = 14.sp, color = Blanco.copy(alpha = 0.7f), letterSpacing = 0.5.sp)
            }

            Spacer(modifier = Modifier.height(28.dp))

            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = Blanco.copy(alpha = 0.15f)) {
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = onBusquedaChange,
                    placeholder = { Text("Buscar...", color = Blanco.copy(alpha = 0.5f), fontSize = 15.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Blanco.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor   = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor      = Blanco.copy(alpha = 0.4f),
                        unfocusedBorderColor    = Color.Transparent,
                        focusedTextColor        = Blanco,
                        unfocusedTextColor      = Blanco,
                        cursorColor             = Blanco
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onBuscar,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blanco)
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = AzulOscuro, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Buscar", color = AzulOscuro, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

// ── Encabezado de sección ─────────────────────────────────────────────────────
@Composable
fun SeccionHeader(titulo: String, subtitulo: String) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text(titulo, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextoOscuro)
        Spacer(modifier = Modifier.height(2.dp))
        Text(subtitulo, fontSize = 12.sp, color = TextoGris)
    }
}

// ── Card resultado de búsqueda ────────────────────────────────────────────────
@Composable
fun SearchResultCard(result: SearchResult, onVerDetalle: (SearchResult) -> Unit = {}) {
    val inicial = result.negocioNombre.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val color   = avatarColors[Math.abs(result.negocioNombre.hashCode()) % avatarColors.size]

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SubcomposeAsyncImage(
                model = result.imagen,
                contentDescription = result.nombre,
                modifier = Modifier.size(54.dp).clip(CircleShape),
                contentScale = ContentScale.Crop,
                error = {
                    Box(Modifier.fillMaxSize().background(color), contentAlignment = Alignment.Center) {
                        Text(inicial, color = Blanco, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    }
                },
                loading = {
                    Box(Modifier.fillMaxSize().background(color.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                        Text(inicial, color = Blanco, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    }
                }
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(result.negocioNombre, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextoOscuro, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    if (result.rating > 0f) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(13.dp))
                        Text(String.format("%.1f", result.rating), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextoOscuro)
                    }
                }
                if (result.descripcion.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(result.descripcion, fontSize = 12.sp, color = TextoGris, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                if (result.ubicacion.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(result.ubicacion, fontSize = 11.sp, color = TextoGris, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            OutlinedButton(
                onClick = { onVerDetalle(result) },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, AzulPrimario),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Más info", fontSize = 11.sp, color = AzulPrimario, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ── Card farmacia ─────────────────────────────────────────────────────────────
@Composable
fun FarmaciaCard(farmacia: FarmaciaUi, onVerDetalle: (FarmaciaUi) -> Unit) {
    val color = avatarColors.getOrElse(farmacia.colorIndex) { AzulPrimario }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SubcomposeAsyncImage(
                model = farmacia.imagen,
                contentDescription = farmacia.nombre,
                modifier = Modifier.size(54.dp).clip(CircleShape),
                contentScale = ContentScale.Crop,
                error = {
                    Box(Modifier.fillMaxSize().background(color), contentAlignment = Alignment.Center) {
                        Text(farmacia.iniciales, color = Blanco, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                    }
                },
                loading = {
                    Box(Modifier.fillMaxSize().background(color.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                        Text(farmacia.iniciales, color = Blanco, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                    }
                }
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
                if (farmacia.esHoy) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF00C48C).copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Hoy", fontSize = 9.sp, color = Color(0xFF00A376), fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(farmacia.diaSemana, fontSize = 9.sp, color = TextoGris)
                }
                Text(farmacia.dia.toString(), fontSize = 26.sp, fontWeight = FontWeight.Black, color = TextoOscuro)
                Text(farmacia.mes, fontSize = 9.sp, color = TextoGris)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(farmacia.nombre, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextoOscuro)
                Spacer(modifier = Modifier.height(3.dp))
                Text(farmacia.direccion, fontSize = 12.sp, color = TextoGris)
            }

            OutlinedButton(
                onClick = { onVerDetalle(farmacia) },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, AzulPrimario),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Más info", fontSize = 11.sp, color = AzulPrimario, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ── Estado error ──────────────────────────────────────────────────────────────
@Composable
fun EstadoError(mensaje: String, onReintentar: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(32.dp)
    ) {
        Text("Ocurrió un error", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextoOscuro)
        Spacer(modifier = Modifier.height(8.dp))
        Text(mensaje, fontSize = 13.sp, color = TextoGris, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onReintentar,
            colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Reintentar")
        }
    }
}
