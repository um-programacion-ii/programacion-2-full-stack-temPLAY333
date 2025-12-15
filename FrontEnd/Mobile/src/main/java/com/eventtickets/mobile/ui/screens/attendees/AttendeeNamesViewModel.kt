package com.eventtickets.mobile.ui.screens.attendees

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// A simple representation of a seat for this screen's state
data class SeatIdentifier(val row: Int, val column: Int)

data class AttendeeNamesUiState(
    val seats: List<SeatIdentifier> = emptyList(),
    val attendeeNames: Map<SeatIdentifier, String> = emptyMap(),
    val isContinueEnabled: Boolean = false,
    val remainingTime: String = "5:00",
    val timerFinished: Boolean = false
)

class AttendeeNamesViewModel(
    private val enableTimer: Boolean = true // Para testing, puede deshabilitarse
) : ViewModel() {

    private val _uiState = MutableStateFlow(AttendeeNamesUiState())
    val uiState: StateFlow<AttendeeNamesUiState> = _uiState.asStateFlow()

    private var timer: CountDownTimer? = null
    private val blockDurationMillis = 5 * 60 * 1000L // 5 minutes

    // In a real app, the list of seats would be passed in or retrieved from a shared repository
    fun initialize() {
        // Obtener asientos reales del PurchaseManager
        val seatsFromManager = com.eventtickets.mobile.data.PurchaseManager.getSelectedSeats()
        val selectedSeats = seatsFromManager.map { (row, col) -> SeatIdentifier(row, col) }
        val initialNames = selectedSeats.associateWith { "" }
        _uiState.value = AttendeeNamesUiState(seats = selectedSeats, attendeeNames = initialNames, remainingTime = "5:00")
        if (enableTimer) {
            startTimer()
        }
    }

    fun onNameChanged(seat: SeatIdentifier, newName: String) {
        _uiState.update { currentState ->
            val updatedNames = currentState.attendeeNames.toMutableMap()
            updatedNames[seat] = newName

            // Validate all names to enable the continue button
            val allNamesValid = updatedNames.values.all { it.isNameValid() }

            currentState.copy(
                attendeeNames = updatedNames,
                isContinueEnabled = allNamesValid
            )
        }
    }

    private fun startTimer() {
        timer?.cancel()
        timer = object : CountDownTimer(blockDurationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                _uiState.update {
                    it.copy(remainingTime = String.format("%d:%02d", minutes, seconds))
                }
            }

            override fun onFinish() {
                _uiState.update {
                    it.copy(remainingTime = "0:00", timerFinished = true)
                }
            }
        }.start()
    }

    /**
     * Guarda los nombres de los asistentes en el PurchaseManager
     */
    fun saveAttendeeNames() {
        val namesMap = _uiState.value.attendeeNames.mapKeys { (seat, _) ->
            Pair(seat.row, seat.column)
        }
        com.eventtickets.mobile.data.PurchaseManager.setAttendeeNames(namesMap)
    }

    // Validation logic as per README
    private fun String.isNameValid(): Boolean {
        // Debe tener al menos 3 caracteres, contener al menos una letra, y solo letras y espacios
        return this.length >= 3 &&
               this.any { it.isLetter() } && // Al menos una letra
               this.all { it.isLetter() || it.isWhitespace() }
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }
}
