package com.eventtickets.mobile.ui.screens.summary

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eventtickets.mobile.data.MockData
import com.eventtickets.mobile.ui.components.PrimaryButton
import com.eventtickets.mobile.ui.components.SecondaryButton
import com.eventtickets.mobile.ui.theme.Background
import com.eventtickets.mobile.ui.theme.Primary
import com.eventtickets.mobile.ui.theme.Surface
import com.eventtickets.mobile.ui.theme.SurfaceVariant
import com.eventtickets.mobile.ui.theme.TextPrimary
import com.eventtickets.mobile.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseSummaryScreen(
    eventId: Long,
    onBackClick: () -> Unit,
    onConfirmClick: (Long) -> Unit
) {
    val event = MockData.getEventById(eventId)
    var isLoading by remember { mutableStateOf(false) }

    // Datos de ejemplo
    val attendees = listOf(
        Pair(Pair(5, 7), "Juan Pérez"),
        Pair(Pair(5, 8), "María García")
    )
    val pricePerSeat = 1250.0
    val subtotal = attendees.size * pricePerSeat
    val serviceFee = 0.0 // Puedes añadir una lógica para esto
    val total = subtotal + serviceFee

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Resumen de Compra",
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
            Text(
                text = event?.titulo ?: "",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "📅 ${event?.fecha?.substring(0, 10) ?: ""}",
                color = TextSecondary,
                fontSize = 14.sp
            )
            Text(
                text = "📍 ${event?.direccion ?: ""}",
                color = TextSecondary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(24.dp))
            Divider(color = SurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            // Tickets
            Text(
                text = "💺 Entradas (${attendees.size})",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            attendees.forEach { (seat, name) ->
                val (fila, col) = seat
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Fila ${('A' + fila - 1)}, Asiento $col",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = name,
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                        Text(
                            text = "$${String.format("%.2f", pricePerSeat)}",
                            color = Primary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = SurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            // Price breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Subtotal",
                    color = TextSecondary,
                    fontSize = 16.sp
                )
                Text(
                    text = "$${String.format("%.2f", subtotal)}",
                    color = TextPrimary,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Cargo por servicio",
                    color = TextSecondary,
                    fontSize = 16.sp
                )
                Text(
                    text = "$${String.format("%.2f", serviceFee)}",
                    color = TextPrimary,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Primary, thickness = 2.dp)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "💰 TOTAL",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$${String.format("%.2f", total)}",
                    color = Primary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(
                text = "CONFIRMAR COMPRA",
                onClick = {
                    // Simular compra exitosa
                    isLoading = true
                    onConfirmClick(123L) // ID de venta simulado
                },
                loading = isLoading
            )

            Spacer(modifier = Modifier.height(12.dp))

            SecondaryButton(
                text = "VOLVER",
                onClick = onBackClick,
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
