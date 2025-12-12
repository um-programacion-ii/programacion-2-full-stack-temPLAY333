package com.eventtickets.mobile.ui.screens.seatmap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventtickets.mobile.data.model.Seat
import com.eventtickets.mobile.data.model.SeatState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SeatMapViewModel : ViewModel() {

    private val _seatMap = MutableStateFlow<Map<Pair<Int, Int>, Seat>>(emptyMap())
    val seatMap: StateFlow<Map<Pair<Int, Int>, Seat>> = _seatMap

    private val _selectedSeats = MutableStateFlow<List<Seat>>(emptyList())
    val selectedSeats: StateFlow<List<Seat>> = _selectedSeats

    fun loadSeats(eventId: Long) {
        viewModelScope.launch {
            // Aquí iría la lógica para cargar los asientos desde una API
            // Por ahora, generaremos un mapa de asientos de ejemplo
            val seats = mutableMapOf<Pair<Int, Int>, Seat>()
            for (row in 1..15) {
                for (col in 1..20) {
                    val state = when {
                        row == 5 && col in 7..8 -> SeatState.SOLD // Ejemplo de asientos vendidos
                        row == 6 && col == 10 -> SeatState.BLOCKED
                        else -> SeatState.AVAILABLE
                    }
                    seats[Pair(row, col)] = Seat(
                        id = "$row-$col",
                        fila = row,
                        columna = col,
                        estado = state,
                        precio = 1250.0
                    )
                }
            }
            _seatMap.value = seats
        }
    }

    fun toggleSeat(seat: Seat) {
        val currentSelected = _selectedSeats.value.toMutableList()
        if (currentSelected.contains(seat)) {
            currentSelected.remove(seat)
        } else {
            if (currentSelected.size < 4) { // Límite de 4 asientos
                currentSelected.add(seat)
            }
        }
        _selectedSeats.value = currentSelected
    }

    fun isSeatSelected(seat: Seat): Boolean {
        return _selectedSeats.value.contains(seat)
    }
}
