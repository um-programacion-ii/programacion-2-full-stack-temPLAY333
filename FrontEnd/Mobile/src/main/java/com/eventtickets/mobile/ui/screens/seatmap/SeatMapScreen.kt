package com.eventtickets.mobile.ui.screens.seatmap

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eventtickets.mobile.data.MockData
import com.eventtickets.mobile.data.model.SeatState
import com.eventtickets.mobile.ui.components.PrimaryButton
import com.eventtickets.mobile.ui.components.SeatView
import com.eventtickets.mobile.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatMapScreen(
    eventId: Long,
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit,
    viewModel: SeatMapViewModel = viewModel()
) {
    val event = MockData.getEventById(eventId)
    val selectedSeats by viewModel.selectedSeats.collectAsState()
    val seatMap by viewModel.seatMap.collectAsState()

    LaunchedEffect(eventId) {
        viewModel.loadSeats(eventId)
    }

    if (event == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background),
            contentAlignment = Alignment.Center
        ) {
            Text("Evento no encontrado", color = TextPrimary)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = event.titulo,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
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
                    containerColor = Background,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            Surface(
                color = Surface,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Seleccionados: ${selectedSeats.size}/4",
                        color = if (selectedSeats.isNotEmpty()) Primary else TextSecondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (selectedSeats.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedSeats.joinToString(", ") { "F${it.fila}A${it.columna}" },
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    PrimaryButton(
                        text = "CONTINUAR (${selectedSeats.size})",
                        onClick = onContinueClick,
                        enabled = selectedSeats.isNotEmpty()
                    )
                }
            }
        },
        containerColor = Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Stage
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🎬 ESCENARIO 🎬",
                        color = Primary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Divider(
                        color = Primary,
                        thickness = 2.dp,
                        modifier = Modifier.width(200.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Seat Grid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    for (fila in 1..event.filaAsientos) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Row label
                            Text(
                                text = ('A' + fila - 1).toString(),
                                color = TextSecondary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(24.dp),
                                textAlign = TextAlign.Center
                            )

                            // Seats
                            for (col in 1..event.columnAsientos) {
                                val seat = seatMap[Pair(fila, col)]
                                if (seat != null) {
                                    SeatView(
                                        seat = seat,
                                        isSelected = viewModel.isSeatSelected(seat),
                                        onClick = { viewModel.toggleSeat(seat) },
                                        enabled = seat.estado == SeatState.AVAILABLE
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Column numbers
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(start = 32.dp)
                    ) {
                        for (col in 1..event.columnAsientos) {
                            Text(
                                text = col.toString(),
                                color = TextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.width(40.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Legend
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = SurfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Leyenda",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LegendItem(color = SeatAvailable, text = "Disponible")
                    Spacer(modifier = Modifier.height(8.dp))
                    LegendItem(color = SeatSelected, text = "Seleccionado")
                    Spacer(modifier = Modifier.height(8.dp))
                    LegendItem(color = SeatBlocked, text = "Bloqueado")
                    Spacer(modifier = Modifier.height(8.dp))
                    LegendItem(color = SeatSold, text = "Ocupado")
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            color = TextSecondary,
            fontSize = 14.sp
        )
    }
}
