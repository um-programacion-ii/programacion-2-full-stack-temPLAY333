package com.eventtickets.mobile.ui.screens.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventtickets.mobile.data.AppConfig
import com.eventtickets.mobile.data.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SignInUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val signInSuccess: Boolean = false
)

class SignInViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState = _uiState.asStateFlow()

    fun onFirstNameChange(firstName: String) {
        _uiState.update { it.copy(firstName = firstName, errorMessage = null) }
    }

    fun onLastNameChange(lastName: String) {
        _uiState.update { it.copy(lastName = lastName, errorMessage = null) }
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
                currentState.firstName.isBlank() -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "El nombre es obligatorio"
                        )
                    }
                    return@launch
                }

                currentState.lastName.isBlank() -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "El apellido es obligatorio"
                        )
                    }
                    return@launch
                }

                currentState.firstName.replace(" ", "_").length !in 1..50 || currentState.lastName.replace(" ", "_").length !in 1..50 -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Nombre y apellido deben tener entre 1 y 50 caracteres"
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

                currentState.password.length < 4 -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "La contraseña debe tener al menos 4 caracteres"
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

            // Construir login a partir de nombre y apellido
            val username = "${currentState.firstName}_${currentState.lastName}".replace(" ", "_").lowercase()
            val email = if (currentState.email.contains("@")) {
                currentState.email
            } else {
                "${currentState.email}@ejemplo.com"
            }

            if (AppConfig.USE_MOCK_DATA) {
                // Modo Mock: Simular registro exitoso
                delay(1500)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        signInSuccess = true
                    )
                }
            } else {
                // Modo Backend: Llamada real
                try {
                    val result = authRepository.register(
                        username = username,
                        email = email,
                        password = currentState.password,
                        firstName = currentState.firstName,
                        lastName = currentState.lastName,
                        phone = if (currentState.phone.isBlank()) null else currentState.phone
                    )

                    println("[SignInViewModel] register: result=$result")

                    result.fold(
                        onSuccess = { response ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    signInSuccess = true
                                )
                            }
                        },
                        onFailure = { error ->
                            println("[SignInViewModel] register failed: ${error::class.simpleName}: ${error.message}")
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = error.message ?: "Error al crear cuenta"
                                )
                            }
                        }
                    )
                } catch (e: Exception) {
                    // Captura cualquier excepción inesperada (p.e. parsing) para evitar crash
                    println("[SignInViewModel] register: excepción inesperada -> ${e::class.simpleName}: ${e.message}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Error inesperado al crear la cuenta. Intenta de nuevo."
                        )
                    }
                }
            }
        }
    }

    fun onSignInHandled() {
        _uiState.update { it.copy(signInSuccess = false) }
    }
}
