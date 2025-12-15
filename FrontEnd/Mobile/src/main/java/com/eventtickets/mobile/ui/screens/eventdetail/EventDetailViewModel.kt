package com.eventtickets.mobile.ui.screens.eventdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventtickets.mobile.data.model.EventoDetalle
import com.eventtickets.mobile.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface EventDetailUiState {
    data class Success(val event: EventoDetalle) : EventDetailUiState
    data class Error(val message: String) : EventDetailUiState
    data object Loading : EventDetailUiState
}

class EventDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<EventDetailUiState>(EventDetailUiState.Loading)
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    private val repository = EventRepository()

    fun loadEventDetail(eventId: Long) {
        viewModelScope.launch {
            _uiState.value = EventDetailUiState.Loading
            try {
                val result = repository.getEventoDetalle(eventId)
                result.onSuccess { event ->
                    _uiState.value = EventDetailUiState.Success(event)
                }.onFailure { error ->
                    _uiState.value = EventDetailUiState.Error(
                        error.message ?: "Error al cargar el evento"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = EventDetailUiState.Error(
                    e.message ?: "Error inesperado"
                )
            }
        }
    }
}
