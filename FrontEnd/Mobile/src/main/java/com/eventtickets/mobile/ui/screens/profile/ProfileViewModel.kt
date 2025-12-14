package com.eventtickets.mobile.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventtickets.mobile.data.repository.AuthRepository
import com.eventtickets.mobile.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val memberSince: String = "",
    val avatarUrl: String = "",
    val totalPurchases: Int = 0,
    val totalEvents: Int = 0,
    val isLoading: Boolean = true
)

class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val authRepository = AuthRepository()
    private val eventRepository = EventRepository()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Obtener estadísticas de compras
                val ventasResult = eventRepository.getVentas()

                ventasResult.onSuccess { purchases ->
                    val uniqueEvents = purchases.map { it.evento.id }.distinct().size

                    _uiState.value = ProfileUiState(
                        name = "Usuario",
                        email = "usuario@eventtickets.com",
                        phone = "+54 11 1234 5678",
                        memberSince = "Diciembre 2024",
                        avatarUrl = "https://i.pravatar.cc/300?u=usuario@eventtickets.com",
                        totalPurchases = purchases.size,
                        totalEvents = uniqueEvents,
                        isLoading = false
                    )
                }.onFailure {
                    // Si falla, mostrar datos básicos sin estadísticas
                    _uiState.value = ProfileUiState(
                        name = "Usuario",
                        email = "usuario@eventtickets.com",
                        phone = "+54 11 1234 5678",
                        memberSince = "Diciembre 2024",
                        avatarUrl = "https://i.pravatar.cc/300?u=usuario@eventtickets.com",
                        totalPurchases = 0,
                        totalEvents = 0,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun onLogoutClick() {
        authRepository.logout()
        _uiState.value = ProfileUiState()
    }
}
