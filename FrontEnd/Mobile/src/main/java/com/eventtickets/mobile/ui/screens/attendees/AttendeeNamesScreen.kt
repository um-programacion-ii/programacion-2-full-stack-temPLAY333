package com.eventtickets.mobile.ui.screens.attendees

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eventtickets.mobile.ui.components.PrimaryButton
import com.eventtickets.mobile.ui.components.SecondaryButton
import com.eventtickets.mobile.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendeeNamesScreen(
    eventId: Long,
    onBackClick: () -> Unit,
    onConfirmClick: () -> Unit,
    onTimeExpired: () -> Unit = onBackClick,
    viewModel: AttendeeNamesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = eventId) {
        viewModel.initialize()
    }

    if (uiState.timerFinished) {
        AlertDialog(
            onDismissRequest = { /* Cannot be dismissed */ },
            title = { Text("Tiempo Expirado") },
            text = { Text("Tu reserva de asientos ha expirado. Puedes volver a intentar la compra desde el detalle del evento.") },
            confirmButton = {
                TextButton(onClick = {
                    // Limpiar el estado de PurchaseManager
                    com.eventtickets.mobile.data.PurchaseManager.clear()
                    onTimeExpired()
                }) {
                    Text("Aceptar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Asignar Nombres", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = TextPrimary)
                    }
                },
                actions = {
                    Text(
                        text = uiState.remainingTime,
                        color = if (uiState.remainingTime.startsWith("0:")) Error else Primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 16.dp)
                    )
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
            Text(
                text = "👤 Introduce los nombres de los asistentes",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Estos nombres se asociarán a las entradas. Asegúrate de que sean correctos.",
                color = TextSecondary,
                fontSize = 16.sp,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(24.dp))

            uiState.seats.forEach { seat ->
                val seatLabel = "Fila ${('A' + seat.row - 1)}, Asiento ${seat.column}"
                val currentName = uiState.attendeeNames[seat] ?: ""

                AttendeeNameCard(
                    seatLabel = seatLabel,
                    name = currentName,
                    onNameChange = { newName -> viewModel.onNameChanged(seat, newName) }
                )
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(24.dp))

            PrimaryButton(
                text = "CONTINUAR",
                onClick = {
                    viewModel.saveAttendeeNames()
                    onConfirmClick()
                },
                enabled = uiState.isContinueEnabled
            )
            Spacer(Modifier.height(12.dp))
            SecondaryButton("VOLVER", onClick = onBackClick)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendeeNameCard(seatLabel: String, name: String, onNameChange: (String) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(seatLabel, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Nombre y Apellido") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = SurfaceVariant,
                    cursorColor = Primary
                )
            )
        }
    }
}
