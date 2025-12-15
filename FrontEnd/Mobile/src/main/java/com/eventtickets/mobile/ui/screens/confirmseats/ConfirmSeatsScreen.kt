package com.eventtickets.mobile.ui.screens.confirmseats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eventtickets.mobile.ui.components.PrimaryButton
import com.eventtickets.mobile.ui.components.SecondaryButton
import com.eventtickets.mobile.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmSeatsScreen(
    eventId: Long,
    onBackClick: () -> Unit,
    onConfirmClick: () -> Unit,
    viewModel: ConfirmSeatsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val eventDetailViewModel: com.eventtickets.mobile.ui.screens.eventdetail.EventDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val eventDetailState by eventDetailViewModel.uiState.collectAsState()
    val confirmState by viewModel.uiState.collectAsState()

    LaunchedEffect(eventId) {
        eventDetailViewModel.loadEventDetail(eventId)
    }

    // Obtener asientos reales del PurchaseManager
    val selectedSeats = com.eventtickets.mobile.data.PurchaseManager.getSelectedSeats()
    val pricePerSeat = 1250.0
    val totalPrice = selectedSeats.size * pricePerSeat

    // Manejar el resultado de la confirmación
    LaunchedEffect(confirmState.isConfirmed) {
        if (confirmState.isConfirmed) {
            onConfirmClick()
        }
    }

    // Mostrar error si hay
    if (confirmState.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.resetError() },
            title = { Text("Error") },
            text = { Text(confirmState.error ?: "") },
            confirmButton = {
                TextButton(onClick = { viewModel.resetError() }) {
                    Text("OK")
                }
            }
        )
    }

    val event = when (val state = eventDetailState) {
        is com.eventtickets.mobile.ui.screens.eventdetail.EventDetailUiState.Success -> state.event
        else -> null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Confirmar Selección",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
        containerColor = Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "📋 Resumen de tu selección",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Event Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = event?.titulo ?: "",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "📅 ${event?.fecha?.substring(0, 10) ?: ""}",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )

                    Divider(modifier = Modifier.padding(vertical = 16.dp), color = SurfaceVariant)

                    Text(
                        text = "💺 Asientos:",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    selectedSeats.forEach { (fila, col) ->
                        Text(
                            text = "• Fila ${('A' + fila - 1)}, Asiento $col",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Divider(modifier = Modifier.padding(vertical = 16.dp), color = SurfaceVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "💰 Total:",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$${String.format("%.2f", totalPrice)}",
                            color = Primary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Warning Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Warning.copy(alpha = 0.1f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Warning,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "⚠️ Importante:",
                            color = Warning,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Los asientos serán bloqueados por 5 minutos. Debes completar la compra antes de que expire el tiempo.",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(
                text = "BLOQUEAR ASIENTOS",
                onClick = {
                    viewModel.bloquearAsientos(eventId, selectedSeats)
                },
                loading = confirmState.isLoading
            )

            Spacer(modifier = Modifier.height(12.dp))

            SecondaryButton(
                text = "VOLVER",
                onClick = onBackClick,
                enabled = !confirmState.isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
