package com.eventtickets.mobile.ui.screens.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SignInUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val signInSuccess: Boolean = false
)

class SignInViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, errorMessage = null) }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPhoneChange(phone: String) {
        _uiState.update { it.copy(phone = phone, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, errorMessage = null) }
    }

    fun onSignInClick() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Validaciones
            val currentState = _uiState.value

            when {
                currentState.name.isBlank() -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "El nombre es obligatorio"
                        )
                    }
                    return@launch
                }

                currentState.email.isBlank() -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "El email es obligatorio"
                        )
                    }
                    return@launch
                }

                !currentState.email.contains("@") -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Email inválido"
                        )
                    }
                    return@launch
                }

                currentState.password.length < 6 -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "La contraseña debe tener al menos 6 caracteres"
                        )
                    }
                    return@launch
                }

                currentState.password != currentState.confirmPassword -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Las contraseñas no coinciden"
                        )
                    }
                    return@launch
                }
            }

            // Simular llamada al backend
            delay(1500)

            // Por ahora, siempre es exitoso (lógica simple)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    signInSuccess = true
                )
            }
        }
    }

    fun onSignInHandled() {
        _uiState.update { it.copy(signInSuccess = false) }
    }
}

