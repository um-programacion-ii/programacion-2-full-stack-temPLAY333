package com.eventtickets.mobile.ui.screens.attendeenames

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eventtickets.mobile.ui.components.PrimaryButton
import com.eventtickets.mobile.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendeeNamesScreen(
    eventId: Long,
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit
) {
    // Datos de ejemplo (en implementación real vendrían del ViewModel)
    val selectedSeats = listOf(
        Pair(5, 7),
        Pair(5, 8)
    )

    var names by remember { mutableStateOf(selectedSeats.map { "" }) }
    var remainingSeconds by remember { mutableStateOf(300) } // 5 minutos

    // Simular timer
    LaunchedEffect(Unit) {
        while (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
        }
    }

    val allNamesValid = names.all { it.length >= 3 }
    val progress = remainingSeconds / 300f
    val isWarning = remainingSeconds < 60

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Datos de Asistentes",
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
            // Timer
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isWarning) Warning.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⏱️ Tiempo restante:",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${remainingSeconds / 60}:${(remainingSeconds % 60).toString().padStart(2, '0')}",
                            color = if (isWarning) Warning else Primary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth(),
                        color = if (isWarning) Warning else Primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Ingresa los nombres de los asistentes para cada entrada:",
                color = TextSecondary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Name inputs
            selectedSeats.forEachIndexed { index, (fila, col) ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "💺 Fila ${('A' + fila - 1)}, Asiento $col",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            if (names[index].length >= 3) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Válido",
                                    tint = Success,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = names[index],
                            onValueChange = { newValue ->
                                names = names.toMutableList().apply {
                                    this[index] = newValue
                                }
                            },
                            label = { Text("Nombre completo") },
                            placeholder = { Text("Ej: Juan Pérez") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = TextSecondary,
                                focusedLabelColor = Primary,
                                unfocusedLabelColor = TextSecondary,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = Primary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        if (names[index].isNotEmpty() && names[index].length < 3) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Mínimo 3 caracteres",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (allNamesValid) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Success.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Success
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "✓ Todos los campos completos",
                            color = Success,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton(
                text = "CONTINUAR",
                onClick = onContinueClick,
                enabled = allNamesValid
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
