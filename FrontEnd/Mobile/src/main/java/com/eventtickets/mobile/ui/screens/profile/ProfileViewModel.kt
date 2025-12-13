package com.eventtickets.mobile.ui.screens.profile

import androidx.lifecycle.ViewModel
import com.eventtickets.mobile.data.MockData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val memberSince: String = "",
    val avatarUrl: String = "",
    val totalPurchases: Int = 0,
    val totalEvents: Int = 0
)

class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        // En una app real, esto vendría del backend después del login
        // Por ahora usamos datos mock
        val totalPurchases = MockData.samplePurchases.size
        val uniqueEvents = MockData.samplePurchases.map { it.evento.id }.distinct().size

        _uiState.value = ProfileUiState(
            name = "Admin User",
            email = "admin@eventtickets.com",
            phone = "+52 55 1234 5678",
            memberSince = "Diciembre 2024",
            avatarUrl = "https://i.pravatar.cc/300?u=admin@eventtickets.com",
            totalPurchases = totalPurchases,
            totalEvents = uniqueEvents
        )
    }

    fun onLogoutClick() {
        // En una app real, esto limpiaría el token JWT y la sesión
        _uiState.value = ProfileUiState()
    }

    fun onCreateAccountClick() {
        // TODO: Implementar navegación a pantalla de registro
        // o abrir diálogo de creación de cuenta
    }
}
