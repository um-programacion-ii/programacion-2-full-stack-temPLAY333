package com.eventtickets.mobile.ui.screens.purchases

import androidx.lifecycle.ViewModel
import com.eventtickets.mobile.data.MockData
import com.eventtickets.mobile.data.model.Purchase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MyPurchasesUiState(
    val purchases: List<Purchase> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class MyPurchasesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MyPurchasesUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadPurchases()
    }

    private fun loadPurchases() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        // Simular carga desde el backend
        try {
            val purchases = MockData.getAllPurchases()
            _uiState.update {
                it.copy(
                    purchases = purchases,
                    isLoading = false,
                    errorMessage = null
                )
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

    fun refresh() {
        loadPurchases()
    }
}

