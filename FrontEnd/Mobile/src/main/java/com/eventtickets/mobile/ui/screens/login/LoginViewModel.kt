package com.eventtickets.mobile.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
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
            delay(1000) // Simulate network call
            if (_uiState.value.username == "admin" && _uiState.value.password == "admin") {
                _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Usuario o contraseña incorrectos") }
            }
        }
    }

    fun onLoginHandled() {
        _uiState.update { it.copy(loginSuccess = false) }
    }
}
