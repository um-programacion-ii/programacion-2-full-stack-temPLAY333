package com.eventtickets.mobile.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventtickets.mobile.data.AppConfig
import com.eventtickets.mobile.data.MockData
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
            println("[HomeViewModel] loadEvents: iniciar (isRefresh=$isRefresh)")
            if (!isRefresh) {
                _uiState.value = HomeUiState.Loading
            }

            // Si estamos en modo mock, no llamar al backend
            if (AppConfig.USE_MOCK_DATA) {
                println("[HomeViewModel] loadEvents: modo MOCK activado -> usando MockData")
                try {
                    val mock = MockData.getEventosResumidos()
                    allEvents = mock
                    _uiState.value = HomeUiState.Success(allEvents)
                    return@launch
                } catch (e: Exception) {
                    println("[HomeViewModel] loadEvents: error al cargar MockData -> ${e.message}")
                    _uiState.value = HomeUiState.Error("Error al cargar eventos de prueba: ${e.message}")
                    return@launch
                }
            }

            try {
                val result = repository.getEventosResumidos()
                println("[HomeViewModel] loadEvents: resultado del repositorio -> $result")
                result.onSuccess { events ->
                    allEvents = events
                    _uiState.value = HomeUiState.Success(allEvents)
                }.onFailure { error ->
                    println("[HomeViewModel] loadEvents: error -> ${error.message}")
                    // Mejorar mensajes para errores de red comunes
                    val msg = when (error) {
                        is java.net.UnknownHostException -> "No se pudo resolver el host. Verifica la URL del backend"
                        is java.net.ConnectException -> "No se pudo conectar al servidor. Verifica que el backend esté corriendo"
                        is java.net.SocketTimeoutException -> "Tiempo de espera agotado al conectar con el servidor"
                        else -> error.message ?: "Error al cargar eventos"
                    }
                    _uiState.value = HomeUiState.Error(msg)
                }
            } catch (e: Exception) {
                println("[HomeViewModel] loadEvents: excepción -> ${e::class.simpleName}: ${e.message}")
                val msg = when (e) {
                    is java.net.UnknownHostException -> "No se pudo resolver el host. Verifica la URL del backend"
                    is java.net.ConnectException -> "No se pudo conectar al servidor. Verifica que el backend esté corriendo"
                    is java.net.SocketTimeoutException -> "Tiempo de espera agotado al conectar con el servidor"
                    else -> e.message ?: "Error de conexión"
                }
                _uiState.value = HomeUiState.Error(msg)
            }
        }
    }

    fun searchEvents(query: String) {
        println("[HomeViewModel] searchEvents: query='$query'")
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
