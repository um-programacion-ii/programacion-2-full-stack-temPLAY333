package com.eventtickets.mobile.ui.screens.eventdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventtickets.mobile.data.MockData
import com.eventtickets.mobile.data.model.EventoDetalle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface EventDetailUiState {
    data class Success(val event: EventoDetalle) : EventDetailUiState
    data object Error : EventDetailUiState
    data object Loading : EventDetailUiState
}

class EventDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<EventDetailUiState>(EventDetailUiState.Loading)
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    fun loadEventDetail(eventId: Long) {
        viewModelScope.launch {
            _uiState.value = EventDetailUiState.Loading
            delay(1000) // Simulate network delay
            try {
                // In the future, this will be a real API call
                val event = MockData.getEventoDetalleById(eventId)
                if (event != null) {
                    _uiState.value = EventDetailUiState.Success(event)
                } else {
                    _uiState.value = EventDetailUiState.Error
                }
            } catch (e: Exception) {
                _uiState.value = EventDetailUiState.Error
            }
        }
    }
}
