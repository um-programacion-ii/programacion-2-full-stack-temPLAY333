package com.eventtickets.mobile.ui.screens.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventtickets.mobile.data.MockData
import com.eventtickets.mobile.data.model.EventoDetalle
import kotlinx.coroutines.delay
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

    fun loadEventDetails(eventId: Long) {
        viewModelScope.launch {
            _uiState.value = PurchaseSummaryUiState.Loading
            delay(500) // Simulate network delay
            try {
                val event = MockData.getEventoDetalleById(eventId)
                if (event != null) {
                    _uiState.value = PurchaseSummaryUiState.Success(event)
                } else {
                    _uiState.value = PurchaseSummaryUiState.Error
                }
            } catch (e: Exception) {
                _uiState.value = PurchaseSummaryUiState.Error
            }
        }
    }

    /**
     * Completa la compra mock y devuelve el ID de la compra
     */
    fun completePurchase(): Long? {
        return try {
            val purchase = com.eventtickets.mobile.data.PurchaseManager.completePurchase()
            purchase?.id
        } catch (e: Exception) {
            null
        }
    }
}
