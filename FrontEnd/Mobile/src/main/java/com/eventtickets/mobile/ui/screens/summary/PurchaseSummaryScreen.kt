package com.eventtickets.mobile.ui.screens.summary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eventtickets.mobile.data.model.EventoDetalle
import com.eventtickets.mobile.ui.components.PrimaryButton
import com.eventtickets.mobile.ui.components.SecondaryButton
import com.eventtickets.mobile.ui.components.shimmerBrush
import com.eventtickets.mobile.ui.theme.Background
import com.eventtickets.mobile.ui.theme.Primary
import com.eventtickets.mobile.ui.theme.Surface
import com.eventtickets.mobile.ui.theme.SurfaceVariant
import com.eventtickets.mobile.ui.theme.TextPrimary
import com.eventtickets.mobile.ui.theme.TextSecondary

@Composable
fun PurchaseSummaryScreen(
    eventId: Long,
    purchaseSummaryViewModel: PurchaseSummaryViewModel = viewModel(),
    onBackClick: () -> Unit,
    onConfirmClick: (Long) -> Unit
) {
    LaunchedEffect(key1 = eventId) {
        purchaseSummaryViewModel.loadEventDetails(eventId)
    }

    val uiState by purchaseSummaryViewModel.uiState.collectAsState()

    when (val state = uiState) {
        is PurchaseSummaryUiState.Loading -> LoadingSummaryState(onBackClick = onBackClick)
        is PurchaseSummaryUiState.Success -> SuccessSummaryState(
            event = state.event,
            onBackClick = onBackClick,
            onConfirmClick = onConfirmClick,
            purchaseSummaryViewModel = purchaseSummaryViewModel
        )
        is PurchaseSummaryUiState.Error -> ErrorSummaryState(
            onBackClick = onBackClick,
            onRetry = { purchaseSummaryViewModel.loadEventDetails(eventId) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuccessSummaryState(
    event: EventoDetalle,
    onBackClick: () -> Unit,
    onConfirmClick: (Long) -> Unit,
    purchaseSummaryViewModel: PurchaseSummaryViewModel = viewModel()
) {
    var isLoading by remember { mutableStateOf(false) }

    // Obtener datos reales del PurchaseManager
    val selectedSeats = com.eventtickets.mobile.data.PurchaseManager.getSelectedSeats()
    val attendeeNames = com.eventtickets.mobile.data.PurchaseManager.getAttendeeNames()
    val attendees = selectedSeats.map { seat ->
        val name = attendeeNames[seat] ?: "Sin nombre"
        Pair(seat, name)
    }
    val pricePerSeat = 1250.0 // This should also come from the event/seat data
    val subtotal = attendees.size * pricePerSeat
    val serviceFee = subtotal * 0.1 // 10% service fee example
    val total = subtotal + serviceFee

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resumen de Compra", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(Background, titleContentColor = TextPrimary)
            )
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
            // Event info
            Text(event.titulo, color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("📅 ${event.fecha.substring(0, 10)}", color = TextSecondary, fontSize = 14.sp)
            Text("📍 ${event.direccion}", color = TextSecondary, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(24.dp))
            Divider(color = SurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            // Tickets
            Text("💺 Entradas (${attendees.size})", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            attendees.forEach { (seat, name) ->
                val (fila, col) = seat
                Card(
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Fila ${('A' + fila - 1)}, Asiento $col", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(name, color = TextSecondary, fontSize = 14.sp)
                        }
                        Text("$${String.format("%.2f", pricePerSeat)}", color = Primary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = SurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            // Price breakdown
            PriceRow(label = "Subtotal", amount = subtotal)
            Spacer(modifier = Modifier.height(8.dp))
            PriceRow(label = "Cargo por servicio", amount = serviceFee)

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Primary, thickness = 2.dp)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("💰 TOTAL", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("$${String.format("%.2f", total)}", color = Primary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(
                text = "CONFIRMAR COMPRA",
                onClick = {
                    isLoading = true
                    val purchaseId = purchaseSummaryViewModel.completePurchase()
                    if (purchaseId != null) {
                        onConfirmClick(purchaseId)
                    } else {
                        // Mostrar error - por ahora usa un ID de respaldo
                        onConfirmClick(999L)
                    }
                    isLoading = false
                },
                loading = isLoading
            )
            Spacer(modifier = Modifier.height(12.dp))
            SecondaryButton("VOLVER", onClick = onBackClick, enabled = !isLoading)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PriceRow(label: String, amount: Double) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary, fontSize = 16.sp)
        Text("$${String.format("%.2f", amount)}", color = TextPrimary, fontSize = 16.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadingSummaryState(onBackClick: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Resumen de Compra") }, navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }) },
        containerColor = Background
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues).padding(16.dp)) {
            Box(modifier = Modifier.fillMaxWidth(0.7f).height(30.dp).background(shimmerBrush()))
            Spacer(Modifier.height(32.dp))
            Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(shimmerBrush()))
            Spacer(Modifier.height(24.dp))
            Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(shimmerBrush()))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorSummaryState(onBackClick: () -> Unit, onRetry: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Error") }, navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }) },
        containerColor = Background
    ) { paddingValues ->
        Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No se pudo cargar el resumen", color = TextPrimary)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onRetry) { Text("Reintentar") }
            }
        }
    }
}
