package com.eventtickets.mobile.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventtickets.mobile.data.model.Event
import com.eventtickets.mobile.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data class Success(val events: List<Event>, val searchQuery: String = "") : HomeUiState
    data class Error(val message: String) : HomeUiState
    data object Loading : HomeUiState
}

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val repository = EventRepository()
    private var allEvents: List<Event> = emptyList()

    init {
        loadEvents()
    }

    fun loadEvents(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!isRefresh) {
                _uiState.value = HomeUiState.Loading
            }
            try {
                val result = repository.getEventosResumidos()
                result.onSuccess { events ->
                    allEvents = events
                    _uiState.value = HomeUiState.Success(allEvents)
                }.onFailure { error ->
                    _uiState.value = HomeUiState.Error(
                        error.message ?: "Error al cargar eventos"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(
                    e.message ?: "Error de conexión"
                )
            }
        }
    }

    fun searchEvents(query: String) {
        val currentState = _uiState.value
        if (currentState !is HomeUiState.Success) return

        val filteredEvents = if (query.isBlank()) {
            allEvents
        } else {
            allEvents.filter { event ->
                event.titulo.contains(query, ignoreCase = true) ||
                event.resumen.contains(query, ignoreCase = true) ||
                event.eventoTipo.nombre.contains(query, ignoreCase = true)
            }
        }

        _uiState.value = HomeUiState.Success(filteredEvents, query)
    }

    fun clearSearch() {
        _uiState.value = HomeUiState.Success(allEvents, "")
    }
}
