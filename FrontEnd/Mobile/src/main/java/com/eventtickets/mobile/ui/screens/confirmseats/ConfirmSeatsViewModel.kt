package com.eventtickets.mobile.ui.screens.confirmseats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventtickets.mobile.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ConfirmSeatsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isConfirmed: Boolean = false
)

class ConfirmSeatsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ConfirmSeatsUiState())
    val uiState: StateFlow<ConfirmSeatsUiState> = _uiState.asStateFlow()

    private val repository = EventRepository()

    fun bloquearAsientos(eventId: Long, asientos: List<Pair<Int, Int>>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val result = repository.bloquearAsientos(eventId, asientos)

                result.onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isConfirmed = true
                    )
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Error al bloquear asientos"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error de conexión: ${e.message}"
                )
            }
        }
    }

    fun resetError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

