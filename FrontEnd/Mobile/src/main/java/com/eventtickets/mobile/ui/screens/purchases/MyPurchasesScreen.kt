package com.eventtickets.mobile.ui.screens.purchases

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eventtickets.mobile.data.MockData
import com.eventtickets.mobile.data.model.Purchase
import com.eventtickets.mobile.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPurchasesScreen(
    onBackClick: () -> Unit,
    onPurchaseClick: (Long) -> Unit
) {
    val purchases = MockData.samplePurchases
    val totalSpent = purchases.sumOf { it.precioVenta }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mis Compras",
                        fontSize = 20.sp,
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
        if (purchases.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        text = "🎫",
                        fontSize = 64.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No hay compras",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Aún no has realizado ninguna compra",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Summary Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${purchases.size}",
                                    color = Primary,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Compras",
                                    color = TextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                            Divider(
                                modifier = Modifier
                                    .height(50.dp)
                                    .width(1.dp),
                                color = TextDisabled
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$${String.format("%.0f", totalSpent)}",
                                    color = Primary,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Total gastado",
                                    color = TextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Upcoming events
                val upcomingPurchases = purchases.filter { isUpcoming(it.evento.fecha) }
                if (upcomingPurchases.isNotEmpty()) {
                    item {
                        Text(
                            text = "🗓️ Próximos eventos",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(upcomingPurchases) { purchase ->
                        PurchaseCard(
                            purchase = purchase,
                            onClick = { onPurchaseClick(purchase.id) }
                        )
                    }
                }

                // Past events
                val pastPurchases = purchases.filter { !isUpcoming(it.evento.fecha) }
                if (pastPurchases.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "📜 Eventos pasados",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(pastPurchases) { purchase ->
                        PurchaseCard(
                            purchase = purchase,
                            onClick = { onPurchaseClick(purchase.id) },
                            isPast = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PurchaseCard(
    purchase: Purchase,
    onClick: () -> Unit,
    isPast: Boolean = false
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isPast) Surface.copy(alpha = 0.6f) else Surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = purchase.evento.titulo,
                    color = if (isPast) TextSecondary else TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "📅 ${formatPurchaseDate(purchase.evento.fecha)}",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(
                        text = "${purchase.asientos.size} ${if (purchase.asientos.size == 1) "entrada" else "entradas"}",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = " • ",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "$${String.format("%.2f", purchase.precioVenta)}",
                        color = Primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ver detalle",
                tint = TextSecondary
            )
        }
    }
}

@SuppressLint("SimpleDateFormat")
private fun isUpcoming(isoDate: String): Boolean {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val eventDate = parser.parse(isoDate) ?: return false
        eventDate.after(Date())
    } catch (e: Exception) {
        false
    }
}

@SuppressLint("SimpleDateFormat")
private fun formatPurchaseDate(isoDate: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val date = parser.parse(isoDate)
        val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("es-ES"))
        formatter.format(date) + "h"
    } catch (e: Exception) {
        ""
    }
}
