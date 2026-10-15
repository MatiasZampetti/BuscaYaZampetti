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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
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
import com.example.buscayazampetti.data.model.FarmaciaUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleScreen(farmacia: FarmaciaUi, navController: NavController) {
    val color = avatarColors.getOrElse(farmacia.colorIndex) { AzulPrimario }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = farmacia.nombre,
                        color = Blanco,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Blanco
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AzulOscuro
                )
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
            // Banner superior con imagen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Brush.verticalGradient(listOf(AzulOscuro, AzulPrimario))),
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = farmacia.imagen,
                    contentDescription = farmacia.nombre,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    error = {
                        Box(
                            modifier = Modifier.fillMaxSize().background(color),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = farmacia.iniciales,
                                color = Blanco,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 36.sp
                            )
                        }
                    },
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize().background(color.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = farmacia.iniciales,
                                color = Blanco,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 36.sp
                            )
                        }
                    }
                )

                if (farmacia.esHoy) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF00C48C))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("De turno hoy", color = Blanco, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Nombre
            Text(
                text = farmacia.nombre,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = TextoOscuro,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Fecha del turno
            Text(
                text = "${farmacia.diaSemana} ${farmacia.dia} de ${farmacia.mes}",
                fontSize = 14.sp,
                color = TextoGris,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tarjeta de información
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Información",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoGris,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val partesDireccion = farmacia.direccion.split("–", "-").map { it.trim() }

                    FilaInfo(
                        icono = Icons.Default.LocationOn,
                        titulo = "Dirección",
                        valor = partesDireccion.getOrElse(0) { farmacia.direccion }
                    )

                    if (partesDireccion.size > 1) {
                        Spacer(modifier = Modifier.height(16.dp))
                        FilaInfo(
                            icono = Icons.Default.Phone,
                            titulo = "Teléfono",
                            valor = partesDireccion[1]
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    FilaInfo(
                        icono = Icons.Default.DateRange,
                        titulo = "Turno",
                        valor = "${farmacia.diaSemana} ${farmacia.dia} de ${farmacia.mes} — 08:00 am a 08:00 am"
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FilaInfo(icono: ImageVector, titulo: String, valor: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AzulPrimario.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = AzulPrimario,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(text = titulo, fontSize = 11.sp, color = TextoGris, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = valor, fontSize = 14.sp, color = TextoOscuro, fontWeight = FontWeight.Medium)
        }
    }
}
