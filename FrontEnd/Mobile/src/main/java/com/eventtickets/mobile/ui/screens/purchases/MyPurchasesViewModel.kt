package com.eventtickets.mobile.ui.screens.purchases

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventtickets.mobile.data.model.Purchase
import com.eventtickets.mobile.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyPurchasesUiState(
    val purchases: List<Purchase> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class MyPurchasesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MyPurchasesUiState())
    val uiState = _uiState.asStateFlow()

    private val repository = EventRepository()

    init {
        loadPurchases()
    }

    private fun loadPurchases() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val result = repository.getVentas()
                result.onSuccess { purchases ->
                    _uiState.update {
                        it.copy(
                            purchases = purchases,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Error al cargar las compras: ${error.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar las compras: ${e.message}"
                    )
                }
            }
        }
    }

    fun refresh() {
        loadPurchases()
    }
}

