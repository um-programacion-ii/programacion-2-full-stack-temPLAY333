package com.eventtickets.mobile.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventtickets.mobile.data.MockData
import com.eventtickets.mobile.data.model.Event
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data class Success(val events: List<Event>, val searchQuery: String = "") : HomeUiState
    data object Error : HomeUiState
    data object Loading : HomeUiState
}

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var allEvents: List<Event> = emptyList()

    init {
        loadEvents()
    }

    fun loadEvents(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!isRefresh) {
                _uiState.value = HomeUiState.Loading
                delay(1500) // Simulate initial network delay
            }
            try {
                allEvents = MockData.getEventosResumidos()
                _uiState.value = HomeUiState.Success(allEvents)
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error
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
