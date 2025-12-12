package com.eventtickets.mobile.ui.screens.eventdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.eventtickets.mobile.data.MockData
import com.eventtickets.mobile.ui.components.PrimaryButton
import com.eventtickets.mobile.ui.components.formatDate
import com.eventtickets.mobile.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: Long,
    onBackClick: () -> Unit,
    onBuyTicketsClick: () -> Unit
) {
    val event = MockData.getEventById(eventId)

    if (event == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text("Evento no encontrado", color = TextPrimary)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background
                )
            )
        },
        bottomBar = {
            Surface(
                color = Surface,
                shadowElevation = 8.dp
            ) {
                PrimaryButton(
                    text = "COMPRAR ENTRADAS",
                    onClick = onBuyTicketsClick,
                    modifier = Modifier.padding(16.dp)
                )
            }
        },
        containerColor = Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Event Image
            AsyncImage(
                model = event.imagen,
                contentDescription = event.titulo,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(16.dp)) {
                // Title
                Text(
                    text = event.titulo,
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Info Cards
                InfoRow(
                    icon = Icons.Default.CalendarToday,
                    text = formatDate(event.fecha)
                )
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow(
                    icon = Icons.Default.LocationOn,
                    text = event.direccion
                )
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow(
                    icon = Icons.Default.AccessTime,
                    text = "Duración: 2 horas"
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Category
                Surface(
                    color = Secondary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "🏷️ ${event.categoria}",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = Secondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Description
                Text(
                    text = "📖 Descripción",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = event.descripcion,
                    color = TextSecondary,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Integrantes
                if (event.integrantes.isNotEmpty()) {
                    Text(
                        text = "👥 Integrantes",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    event.integrantes.forEach { integrante ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(
                                text = "• ${integrante.nombre}",
                                color = TextPrimary,
                                fontSize = 16.sp
                            )
                            Text(
                                text = " - ${integrante.rol}",
                                color = TextSecondary,
                                fontSize = 16.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Capacity
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = SurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "💺 Información de asientos",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Capacidad: ${event.filaAsientos} filas × ${event.columnAsientos} columnas",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Total: ${event.filaAsientos * event.columnAsientos} asientos",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = TextSecondary,
            fontSize = 16.sp
        )
    }
}

