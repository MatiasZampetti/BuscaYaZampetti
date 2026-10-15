package com.example.buscayazampetti.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.example.buscayazampetti.AzulOscuro
import com.example.buscayazampetti.AzulPrimario
import com.example.buscayazampetti.Blanco
import com.example.buscayazampetti.FondoClaro
import com.example.buscayazampetti.TextoGris
import com.example.buscayazampetti.TextoOscuro
import com.example.buscayazampetti.avatarColors
import com.example.buscayazampetti.data.model.SearchResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleResultadoScreen(result: SearchResult, navController: NavController) {
    val color = avatarColors[Math.abs(result.negocioNombre.hashCode()) % avatarColors.size]
    val inicial = result.negocioNombre.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(result.negocioNombre, color = Blanco, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Blanco)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulOscuro)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(FondoClaro)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Banner con imagen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Brush.verticalGradient(listOf(AzulOscuro, AzulPrimario))),
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = result.imagen,
                    contentDescription = result.nombre,
                    modifier = Modifier.size(120.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    error = {
                        Box(Modifier.fillMaxSize().background(color), contentAlignment = Alignment.Center) {
                            Text(inicial, color = Blanco, fontWeight = FontWeight.ExtraBold, fontSize = 36.sp)
                        }
                    },
                    loading = {
                        Box(Modifier.fillMaxSize().background(color.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                            Text(inicial, color = Blanco, fontWeight = FontWeight.ExtraBold, fontSize = 36.sp)
                        }
                    }
                )

                if (result.rating > 0f) {
                    Row(
                        modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFFFB300))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Blanco, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(String.format("%.1f", result.rating), color = Blanco, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(result.negocioNombre, fontSize = 22.sp, fontWeight = FontWeight.Black, color = TextoOscuro, modifier = Modifier.padding(horizontal = 24.dp))

            if (result.descripcion.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(result.descripcion, fontSize = 14.sp, color = TextoGris, modifier = Modifier.padding(horizontal = 24.dp))
            }

            if (result.precio.isNotBlank() && result.precio != "Consultar") {
                Spacer(modifier = Modifier.height(4.dp))
                Text(result.precio, fontSize = 14.sp, color = AzulPrimario, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 24.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Información", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextoGris, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    if (result.ubicacion.isNotBlank()) {
                        FilaInfoResultado(icono = Icons.Default.LocationOn, titulo = "Ubicación", valor = result.ubicacion)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (result.telefono.isNotBlank()) {
                        FilaInfoResultado(icono = Icons.Default.Phone, titulo = "Teléfono", valor = result.telefono)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (result.emailContacto.isNotBlank() && result.emailContacto != "contacto@demo.com") {
                        FilaInfoResultado(icono = Icons.Default.Email, titulo = "Email", valor = result.emailContacto)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FilaInfoResultado(icono: ImageVector, titulo: String, valor: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(AzulPrimario.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icono, contentDescription = null, tint = AzulPrimario, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(titulo, fontSize = 11.sp, color = TextoGris, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(valor, fontSize = 14.sp, color = TextoOscuro, fontWeight = FontWeight.Medium)
        }
    }
}
