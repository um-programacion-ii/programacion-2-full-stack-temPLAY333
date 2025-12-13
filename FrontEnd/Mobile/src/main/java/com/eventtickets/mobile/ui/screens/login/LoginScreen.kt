package com.eventtickets.mobile.ui.screens.login

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eventtickets.mobile.ui.components.PrimaryButton
import com.eventtickets.mobile.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = viewModel(),
    onLoginSuccess: () -> Unit,
    onCreateAccountClick: () -> Unit
) {
    Log.d("AppFlow", "LoginScreen: Composing")
    val uiState by loginViewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    // Observe the loginSuccess flag to navigate
    LaunchedEffect(uiState.loginSuccess) {
        Log.d("AppFlow", "LoginScreen: LaunchedEffect running, loginSuccess = ${uiState.loginSuccess}")
        if (uiState.loginSuccess) {
            onLoginSuccess()
            loginViewModel.onLoginHandled() // Reset the flag
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Background, SurfaceVariant)
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🎫", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("EventTickets", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Ingresa a tu cuenta", color = TextSecondary, fontSize = 16.sp)

            Spacer(modifier = Modifier.height(48.dp))

            // Username field
            OutlinedTextField(
                value = uiState.username,
                onValueChange = { loginViewModel.onUsernameChange(it) },
                label = { Text("Usuario") },
                leadingIcon = { Icon(Icons.Default.Person, "Usuario") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.errorMessage != null
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password field
            OutlinedTextField(
                value = uiState.password,
                onValueChange = { loginViewModel.onPasswordChange(it) },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, "Contraseña") },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.errorMessage != null
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Login button
            PrimaryButton(
                text = "INICIAR SESIÓN",
                onClick = { loginViewModel.onLoginClick() },
                loading = uiState.isLoading,
                enabled = uiState.username.isNotEmpty() && uiState.password.isNotEmpty() && !uiState.isLoading
            )

            // Error message
            val errorMessage = uiState.errorMessage
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage,
                    color = Error,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Divider con texto
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = TextSecondary.copy(alpha = 0.3f)
                )
                Text(
                    text = "  o  ",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
                androidx.compose.material3.HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = TextSecondary.copy(alpha = 0.3f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Crear Cuenta
            androidx.compose.material3.OutlinedButton(
                onClick = onCreateAccountClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    contentColor = Primary
                ),
                border = BorderStroke(2.dp, Primary)
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "CREAR CUENTA",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("💡 Usuario: admin | Contraseña: admin", color = TextSecondary, fontSize = 12.sp)
        }
    }
}
