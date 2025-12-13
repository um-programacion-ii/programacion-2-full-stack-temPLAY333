package com.eventtickets.mobile.ui.screens.eventdetail

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale.Companion.Crop
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.eventtickets.mobile.data.model.EventoDetalle
import com.eventtickets.mobile.ui.components.PrimaryButton
import com.eventtickets.mobile.ui.components.shimmerBrush
import com.eventtickets.mobile.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun EventDetailScreen(
    eventId: Long,
    eventDetailViewModel: EventDetailViewModel = viewModel(),
    onBackClick: () -> Unit,
    onBuyTicketsClick: (Long) -> Unit
) {
    LaunchedEffect(key1 = eventId) {
        eventDetailViewModel.loadEventDetail(eventId)
    }

    val uiState by eventDetailViewModel.uiState.collectAsState()

    when (val state = uiState) {
        is EventDetailUiState.Loading -> LoadingDetailState(onBackClick = onBackClick)
        is EventDetailUiState.Success -> SuccessDetailState(
            event = state.event,
            onBackClick = onBackClick,
            onBuyTicketsClick = { onBuyTicketsClick(eventId) }
        )
        is EventDetailUiState.Error -> ErrorDetailState(
            onBackClick = onBackClick,
            onRetry = { eventDetailViewModel.loadEventDetail(eventId) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuccessDetailState(
    event: EventoDetalle,
    onBackClick: () -> Unit,
    onBuyTicketsClick: () -> Unit
) {
    Scaffold(
        bottomBar = {
            Surface(
                color = Background,
                shadowElevation = 8.dp
            ) {
                PrimaryButton(
                    text = "COMPRAR ENTRADAS",
                    onClick = onBuyTicketsClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        },
        containerColor = Background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    // Use padding at the bottom to avoid content being hidden by the bottom bar
                    .padding(bottom = paddingValues.calculateBottomPadding())
            ) {
                // Header Image
                Box(modifier = Modifier.height(300.dp)) {
                    AsyncImage(
                        model = event.imagen,
                        contentDescription = event.titulo,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = Crop
                    )
                }

                // Content
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = event.titulo,
                        color = TextPrimary,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    InfoRow(
                        icon = Icons.Default.CalendarToday,
                        text = event.fecha.formatDetailDate()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    InfoRow(
                        icon = Icons.Default.LocationOn,
                        text = event.direccion
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Secondary)
                    ) {
                        Text(
                            text = event.eventoTipo.nombre,
                            color = White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Divider(color = SurfaceVariant)
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "📖 Sobre el evento",
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = event.descripcion,
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (event.integrantes.isNotEmpty()) {
                        Text(
                            text = "👥 Integrantes",
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        event.integrantes.forEach { integrante ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "• ${integrante.nombre}",
                                    color = TextPrimary,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = " - ${integrante.rol}",
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }

            // Top bar overlaid on top of the content
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Transparent,
                ),
                modifier = Modifier.background(
                    verticalGradient(
                        colors = listOf(Black.copy(alpha = 0.4f), Transparent)
                    )
                )
            )
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            color = TextSecondary,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

private fun String.formatDetailDate(): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(this)
        val outputFormat = SimpleDateFormat("EEEE d 'de' MMMM, yyyy 'a las' HH:mm 'hs'", Locale("es", "ES"))
        date?.let { dateObject -> outputFormat.format(dateObject).replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString() } } ?: this
    } catch (e: Exception) {
        this
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadingDetailState(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        containerColor = Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(300.dp).background(shimmerBrush()))
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Box(modifier = Modifier.fillMaxWidth(0.8f).height(32.dp).background(shimmerBrush()))
                Spacer(modifier = Modifier.height(24.dp))
                Box(modifier = Modifier.fillMaxWidth(0.6f).height(20.dp).background(shimmerBrush()))
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth(0.7f).height(20.dp).background(shimmerBrush()))
                Spacer(modifier = Modifier.height(24.dp))
                Box(modifier = Modifier.width(100.dp).height(30.dp).background(shimmerBrush()))
                Spacer(modifier = Modifier.height(24.dp))
                Divider(color = SurfaceVariant)
                Spacer(modifier = Modifier.height(24.dp))
                Box(modifier = Modifier.fillMaxWidth(0.5f).height(24.dp).background(shimmerBrush()))
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth().height(20.dp).background(shimmerBrush()))
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth().height(20.dp).background(shimmerBrush()))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorDetailState(onBackClick: () -> Unit, onRetry: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Error") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background, titleContentColor = TextPrimary)
            )
        },
        containerColor = Background
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(text = "No se pudo cargar el evento.", color = TextPrimary)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onRetry) {
                    Text("Reintentar")
                }
            }
        }
    }
}
