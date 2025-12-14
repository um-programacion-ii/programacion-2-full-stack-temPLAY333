package com.eventtickets.mobile.ui.screens.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventtickets.mobile.data.model.EventoDetalle
import com.eventtickets.mobile.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PurchaseSummaryUiState {
    data class Success(val event: EventoDetalle) : PurchaseSummaryUiState
    object Error : PurchaseSummaryUiState
    object Loading : PurchaseSummaryUiState
}

class PurchaseSummaryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<PurchaseSummaryUiState>(PurchaseSummaryUiState.Loading)
    val uiState: StateFlow<PurchaseSummaryUiState> = _uiState.asStateFlow()

    private val repository = EventRepository()

    fun loadEventDetails(eventId: Long) {
        viewModelScope.launch {
            _uiState.value = PurchaseSummaryUiState.Loading
            try {
                val result = repository.getEventoDetalle(eventId)
                result.onSuccess { event ->
                    _uiState.value = PurchaseSummaryUiState.Success(event)
                }.onFailure {
                    _uiState.value = PurchaseSummaryUiState.Error
                }
            } catch (e: Exception) {
                _uiState.value = PurchaseSummaryUiState.Error
            }
        }
    }

    private val _purchaseResult = MutableStateFlow<Long?>(null)
    val purchaseResult: StateFlow<Long?> = _purchaseResult.asStateFlow()

    private val _purchaseError = MutableStateFlow<String?>(null)
    val purchaseError: StateFlow<String?> = _purchaseError.asStateFlow()

    /**
     * Completa la compra llamando al endpoint de realizar venta
     */
    fun completePurchase(eventId: Long, asientos: List<Triple<Int, Int, String>>) {
        viewModelScope.launch {
            try {
                val result = repository.realizarVenta(eventId, asientos)

                result.onSuccess { response ->
                    _purchaseResult.value = response.ventaId
                }.onFailure { error ->
                    _purchaseError.value = error.message ?: "Error al completar la compra"
                }
            } catch (e: Exception) {
                _purchaseError.value = "Error de conexión: ${e.message}"
            }
        }
    }

    fun resetPurchaseState() {
        _purchaseResult.value = null
        _purchaseError.value = null
    }
}
