package com.eventtickets.mobile.ui.screens.seatmap

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eventtickets.mobile.data.model.AsientoMapaDto
import com.eventtickets.mobile.ui.components.PrimaryButton
import com.eventtickets.mobile.ui.components.shimmerBrush
import com.eventtickets.mobile.ui.theme.*

@Composable
fun SeatMapScreen(
    eventId: Long,
    onBackClick: () -> Unit,
    onContinueClick: (List<AsientoMapaDto>) -> Unit,
    viewModel: SeatMapViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(eventId) {
        viewModel.loadSeatMap(eventId)
    }

    when {
        uiState.isLoading -> LoadingSeatMapState(onBackClick)
        uiState.error != null -> ErrorSeatMapState(onBackClick) { viewModel.loadSeatMap(eventId) }
        uiState.seatMap != null -> {
            SuccessSeatMapState(
                uiState = uiState,
                onBackClick = onBackClick,
                onSeatClick = { row, col -> viewModel.toggleSeatSelection(row, col) },
                onContinueClick = { onContinueClick(uiState.selectedSeats) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuccessSeatMapState(
    uiState: SeatMapUiState,
    onBackClick: () -> Unit,
    onSeatClick: (Int, Int) -> Unit,
    onContinueClick: () -> Unit
) {
    val seatMap = uiState.seatMap!!
    val selectedSeats = uiState.selectedSeats

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.eventTitle, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } },
                colors = TopAppBarDefaults.topAppBarColors(Background, titleContentColor = TextPrimary)
            )
        },
        bottomBar = {
            Surface(color = Surface, shadowElevation = 8.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val selectionText = if (selectedSeats.isNotEmpty()) {
                        selectedSeats.joinToString(", ") { "F${it.fila}A${it.columna}" }
                    } else {
                        "No hay asientos seleccionados"
                    }
                    Text("Seleccionados: ${selectedSeats.size}/4", color = if (selectedSeats.isNotEmpty()) Primary else TextSecondary, fontWeight = FontWeight.Bold)
                    if (selectedSeats.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text(selectionText, color = TextSecondary, fontSize = 14.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    PrimaryButton("CONTINUAR (${selectedSeats.size})", onClick = onContinueClick, enabled = selectedSeats.isNotEmpty())
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
            Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎬 ESCENARIO 🎬", color = Primary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Divider(color = Primary, thickness = 2.dp, modifier = Modifier.width(200.dp))
                }
            }
            Spacer(Modifier.height(24.dp))

            Box(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    (1..seatMap.totalFilas).forEach { row ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = ('A' + row - 1).toString(), color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                            (1..seatMap.totalColumnas).forEach { col ->
                                val isSelected = selectedSeats.any { it.fila == row && it.columna == col }
                                val seatState = seatMap.asientos.find { it.fila == row && it.columna == col }?.estado ?: "Disponible"

                                SeatView(
                                    state = if (isSelected) "Seleccionado" else seatState,
                                    onClick = { onSeatClick(row, col) }
                                )
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(start = 30.dp)) {
                        (1..seatMap.totalColumnas).forEach { col ->
                            Text(col.toString(), color = TextSecondary, fontSize = 10.sp, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))

            Card(colors = CardDefaults.cardColors(containerColor = SurfaceVariant), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Leyenda", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    LegendItem(color = SeatAvailable, text = "Disponible")
                    Spacer(Modifier.height(8.dp))
                    LegendItem(color = SeatSelected, text = "Seleccionado")
                    Spacer(Modifier.height(8.dp))
                    LegendItem(color = SeatBlocked, text = "Bloqueado")
                    Spacer(Modifier.height(8.dp))
                    LegendItem(color = SeatSold, text = "Vendido")
                }
            }
            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
fun SeatView(state: String, onClick: () -> Unit) {
    val (color, isClickable) = when (state) {
        "Disponible" -> SeatAvailable to true
        "Seleccionado" -> SeatSelected to true
        "Bloqueado" -> SeatBlocked to false
        "Vendido" -> SeatSold to false
        else -> Color.Gray to false
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .background(color, CircleShape)
            .clickable(enabled = isClickable, onClick = onClick)
    )
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(20.dp).background(color, CircleShape))
        Spacer(Modifier.width(12.dp))
        Text(text, color = TextSecondary, fontSize = 14.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadingSeatMapState(onBackClick: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Cargando...") }, navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }) },
        containerColor = Background
    ) { paddingValues ->
        Box(Modifier.fillMaxSize().padding(paddingValues).background(shimmerBrush()))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorSeatMapState(onBackClick: () -> Unit, onRetry: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Error") }, navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }) },
        containerColor = Background
    ) { paddingValues ->
        Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No se pudo cargar el mapa de asientos", color = TextPrimary)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onRetry) { Text("Reintentar") }
            }
        }
    }
}
