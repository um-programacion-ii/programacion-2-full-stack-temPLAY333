package com.eventtickets.mobile.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventtickets.mobile.data.repository.AuthRepository
import com.eventtickets.mobile.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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

    // Hacer pública para que la UI pueda forzar una recarga al entrar en la pantalla
    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                println("[ProfileViewModel] Cargando datos de cuenta desde backend")
                val accountResult = authRepository.getAccount()

                accountResult.onSuccess { account ->
                    val displayName = when {
                        !account.firstName.isNullOrBlank() || !account.lastName.isNullOrBlank() -> listOfNotNull(account.firstName, account.lastName).joinToString(" ")
                        !account.login.isNullOrBlank() -> account.login.orEmpty()
                        else -> "Usuario"
                    }

                    val identifier = (account.email ?: account.login ?: "").orEmpty()
                    val safeId = try {
                        URLEncoder.encode(identifier, StandardCharsets.UTF_8.toString())
                    } catch (e: Exception) {
                        identifier.replace("@", "_")
                    }

                    // Ahora obtener estadísticas de ventas
                    val ventasResult = eventRepository.getVentas()

                    ventasResult.onSuccess { purchases ->
                        val uniqueEvents = purchases.map { it.evento.id }.distinct().size

                        _uiState.value = ProfileUiState(
                            name = displayName,
                            email = account.email ?: "",
                            phone = "", // Backend no devuelve teléfono en AccountDTO por defecto
                            memberSince = "",
                            avatarUrl = if (safeId.isNotBlank()) "https://i.pravatar.cc/300?u=$safeId" else "",
                            totalPurchases = purchases.size.coerceAtLeast(0),
                            totalEvents = uniqueEvents.coerceAtLeast(0),
                            isLoading = false
                        )
                    }.onFailure { _ ->
                        // Si falla cargar ventas, al menos mostrar datos de cuenta
                        _uiState.value = ProfileUiState(
                            name = displayName,
                            email = account.email ?: "",
                            phone = "",
                            memberSince = "",
                            avatarUrl = if (safeId.isNotBlank()) "https://i.pravatar.cc/300?u=$safeId" else "",
                            totalPurchases = 0,
                            totalEvents = 0,
                            isLoading = false
                        )
                    }
                }.onFailure { error ->
                    println("[ProfileViewModel] Error al obtener cuenta: ${error.message}")
                    // Si no se pudo obtener cuenta (token invalidado), limpiar UI y dejar valores por defecto
                    _uiState.value = ProfileUiState(
                        name = "Usuario",
                        email = "",
                        phone = "",
                        memberSince = "",
                        avatarUrl = "",
                        totalPurchases = 0,
                        totalEvents = 0,
                        isLoading = false
                    )
                }

            } catch (e: Exception) {
                println("[ProfileViewModel] Excepción al cargar perfil: ${e::class.simpleName}: ${e.message}")
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun onLogoutClick() {
        authRepository.logout()
        _uiState.value = ProfileUiState()
    }
}
