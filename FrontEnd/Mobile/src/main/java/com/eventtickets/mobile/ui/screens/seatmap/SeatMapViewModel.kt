package com.eventtickets.mobile.ui.screens.seatmap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventtickets.mobile.data.MockData
import com.eventtickets.mobile.data.model.AsientoMapaDto
import com.eventtickets.mobile.data.model.MapaAsientosDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SeatMapUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val eventTitle: String = "",
    val seatMap: MapaAsientosDto? = null,
    val selectedSeats: List<AsientoMapaDto> = emptyList()
)

class SeatMapViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SeatMapUiState())
    val uiState: StateFlow<SeatMapUiState> = _uiState.asStateFlow()

    fun loadSeatMap(eventId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Fetch both event details and seat map
                val event = MockData.getEventoDetalleById(eventId)
                val seatMap = MockData.getSeatMapForEvent(eventId)

                if (event != null && seatMap != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            eventTitle = event.titulo,
                            seatMap = seatMap
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "No se pudo cargar el mapa de asientos.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Ocurrió un error inesperado.") }
            }
        }
    }

    fun toggleSeatSelection(row: Int, column: Int) {
        val seatMap = _uiState.value.seatMap ?: return
        val existingSeat = seatMap.asientos.find { it.fila == row && it.columna == column }

        // Can only select available seats
        if (existingSeat != null && existingSeat.estado != "Disponible") {
            return // Or show a toast/message
        }

        val seatIdentifier = AsientoMapaDto(fila = row, columna = column, estado = "Seleccionado")

        _uiState.update { currentState ->
            val alreadySelected = currentState.selectedSeats.any { it.fila == row && it.columna == column }
            val newSelectedSeats = if (alreadySelected) {
                currentState.selectedSeats.filterNot { it.fila == row && it.columna == column }
            } else {
                if (currentState.selectedSeats.size < 4) { // Max 4 seats
                    currentState.selectedSeats + seatIdentifier
                } else {
                    // Optional: show a message that max seats reached
                    currentState.selectedSeats
                }
            }
            currentState.copy(selectedSeats = newSelectedSeats)
        }
    }

    /**
     * Guarda la selección de asientos en el PurchaseManager
     */
    fun confirmSelection(eventId: Long) {
        val selectedSeats = _uiState.value.selectedSeats.map { it.fila to it.columna }
        com.eventtickets.mobile.data.PurchaseManager.startPurchase(eventId, selectedSeats)
    }
}
