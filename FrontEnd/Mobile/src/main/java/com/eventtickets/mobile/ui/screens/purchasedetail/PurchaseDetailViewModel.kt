package com.eventtickets.mobile.ui.screens.purchasedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventtickets.mobile.data.model.PurchaseDetail
import com.eventtickets.mobile.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PurchaseDetailUiState {
    data class Success(val purchase: PurchaseDetail) : PurchaseDetailUiState
    object Error : PurchaseDetailUiState
    object Loading : PurchaseDetailUiState
}

class PurchaseDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<PurchaseDetailUiState>(PurchaseDetailUiState.Loading)
    val uiState: StateFlow<PurchaseDetailUiState> = _uiState.asStateFlow()

    private val repository = EventRepository()

    fun loadPurchaseDetail(purchaseId: Long) {
        viewModelScope.launch {
            _uiState.value = PurchaseDetailUiState.Loading
            try {
                val result = repository.getVentaDetalle(purchaseId)
                result.onSuccess { purchase ->
                    _uiState.value = PurchaseDetailUiState.Success(purchase)
                }.onFailure {
                    _uiState.value = PurchaseDetailUiState.Error
                }
            } catch (e: Exception) {
                _uiState.value = PurchaseDetailUiState.Error
            }
        }
    }
}
