package com.eventtickets.mobile.ui.screens.purchasedetail

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eventtickets.mobile.data.model.PurchaseDetail
import com.eventtickets.mobile.data.model.Seat
import com.eventtickets.mobile.ui.components.PrimaryButton
import com.eventtickets.mobile.ui.components.SecondaryButton
import com.eventtickets.mobile.ui.components.shimmerBrush
import com.eventtickets.mobile.ui.theme.*

@Composable
fun PurchaseDetailScreen(
    purchaseId: Long,
    purchaseDetailViewModel: PurchaseDetailViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    LaunchedEffect(key1 = purchaseId) {
        purchaseDetailViewModel.loadPurchaseDetail(purchaseId)
    }

    val uiState by purchaseDetailViewModel.uiState.collectAsState()

    when (val state = uiState) {
        is PurchaseDetailUiState.Loading -> LoadingPurchaseState(onBackClick)
        is PurchaseDetailUiState.Success -> SuccessPurchaseState(state.purchase, onBackClick)
        is PurchaseDetailUiState.Error -> ErrorPurchaseState(onBackClick) { purchaseDetailViewModel.loadPurchaseDetail(purchaseId) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuccessPurchaseState(purchase: PurchaseDetail, onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Compra", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } },
                actions = { IconButton(onClick = { /* Share */ }) { Icon(Icons.Default.Share, "Compartir", tint = Primary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background, titleContentColor = TextPrimary, navigationIconContentColor = TextPrimary)
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
            // Nombre del evento primero
            Text(
                text = purchase.evento.titulo,
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            // Fecha y dirección con texto más grande
            Text(
                text = "📅 ${purchase.evento.fecha.take(10)}",
                color = TextSecondary,
                fontSize = 16.sp
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "📍 ${purchase.evento.direccion}",
                color = TextSecondary,
                fontSize = 16.sp
            )

            Spacer(Modifier.height(24.dp))
            Divider(color = SurfaceVariant)
            Spacer(Modifier.height(24.dp))

            Text("📋 Información de compra", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            InfoRow("Fecha de compra:", purchase.fechaVenta.take(10))
            Spacer(Modifier.height(8.dp))
            InfoRow("Hora:", purchase.fechaVenta.substring(11, 16))

            Spacer(Modifier.height(24.dp))

            Text("💺 Entradas (${purchase.asientos.size}):", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            purchase.asientos.forEach { seat ->
                SeatCard(seat)
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(24.dp))
            Divider(color = SurfaceVariant)
            Spacer(Modifier.height(24.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("💰 Total pagado:", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("$${String.format("%.2f", purchase.precioVenta)} ARS", color = Primary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(32.dp))
            Divider(color = SurfaceVariant)
            Spacer(Modifier.height(32.dp))

            // Código QR centrado y responsive
            Card(
                colors = CardDefaults.cardColors(containerColor = Surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tu Código QR",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f) // 70% del ancho disponible para ser responsive
                            .aspectRatio(1f) // Mantiene relación 1:1 (cuadrado)
                            .background(TextPrimary, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "QR\nCode",
                            color = Background,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "ID: #VT-${purchase.id.toString().padStart(6, '0')}",
                        color = Primary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
            PrimaryButton("COMPARTIR QR", onClick = { /* Share QR */ })
            Spacer(Modifier.height(12.dp))
            SecondaryButton("DESCARGAR ENTRADAS", onClick = { /* Download */ })
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SeatCard(seat: Seat) {
    Card(colors = CardDefaults.cardColors(containerColor = SurfaceVariant), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(16.dp), Arrangement.SpaceBetween) {
            Text("Fila ${('A' + seat.fila - 1)}, Asiento ${seat.columna}", color = TextPrimary, fontSize = 16.sp)
            Text("✓", color = Success, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary, fontSize = 16.sp)
        Text(value, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadingPurchaseState(onBackClick: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Cargando...") }, navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }) },
        containerColor = Background
    ) { paddingValues ->
        Box(Modifier.fillMaxSize().padding(paddingValues).background(shimmerBrush()))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorPurchaseState(onBackClick: () -> Unit, onRetry: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Error") }, navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }) },
        containerColor = Background
    ) { paddingValues ->
        Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No se pudo cargar la compra", color = TextPrimary)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onRetry) { Text("Reintentar") }
            }
        }
    }
}
