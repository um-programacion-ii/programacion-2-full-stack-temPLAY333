package com.eventtickets.mobile.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventtickets.mobile.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginSuccess: Boolean = false
)

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val authRepository = AuthRepository()

    fun onUsernameChange(username: String) {
        _uiState.update { it.copy(username = username, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onLoginClick() {
        viewModelScope.launch {
            // Validar campos vacíos
            if (_uiState.value.username.isEmpty() || _uiState.value.password.isEmpty()) {
                _uiState.update { it.copy(errorMessage = "Usuario y contraseña requeridos") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val result = authRepository.login(
                    username = _uiState.value.username.trim(),
                    password = _uiState.value.password
                )

                result.onSuccess { token ->
                    // El token ya se guardó en RetrofitClient dentro del repository
                    _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Error de autenticación"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error de conexión: ${e.message}"
                    )
                }
            }
        }
    }

    fun onLoginHandled() {
        _uiState.update { it.copy(loginSuccess = false) }
    }
}
