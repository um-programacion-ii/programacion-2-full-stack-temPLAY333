package com.eventtickets.mobile.ui.screens.signin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eventtickets.mobile.ui.components.PrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    signInViewModel: SignInViewModel = viewModel(),
    onBackClick: () -> Unit,
    onSignInSuccess: () -> Unit
) {
    val uiState by signInViewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Navegar cuando el registro sea exitoso
    LaunchedEffect(uiState.signInSuccess) {
        if (uiState.signInSuccess) {
            onSignInSuccess()
            signInViewModel.onSignInHandled()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Crear Cuenta",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color(0xFF212121)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF212121)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFFFFF))
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Icono
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(40.dp),
                    color = Color(0xFF6A5AE0).copy(alpha = 0.15f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = Color(0xFF6A5AE0),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Únete a EventTickets",
                    color = Color(0xFF212121),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Crea tu cuenta y disfruta de eventos increíbles",
                    color = Color(0xFF757575),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Campo Nombre
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { signInViewModel.onNameChange(it) },
                    label = { Text("Nombre completo") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Person,
                            "Nombre",
                            tint = Color(0xFF6A5AE0)
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.errorMessage != null && uiState.name.isBlank(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6A5AE0),
                        focusedLabelColor = Color(0xFF6A5AE0),
                        cursorColor = Color(0xFF6A5AE0),
                        unfocusedBorderColor = Color(0xFFBDBDBD),
                        unfocusedLabelColor = Color(0xFF757575),
                        focusedTextColor = Color(0xFF212121),
                        unfocusedTextColor = Color(0xFF212121)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo Email
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = { signInViewModel.onEmailChange(it) },
                    label = { Text("Email") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Email,
                            "Email",
                            tint = Color(0xFF6A5AE0)
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = uiState.errorMessage != null && !uiState.email.contains("@"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6A5AE0),
                        focusedLabelColor = Color(0xFF6A5AE0),
                        cursorColor = Color(0xFF6A5AE0),
                        unfocusedBorderColor = Color(0xFFBDBDBD),
                        unfocusedLabelColor = Color(0xFF757575),
                        focusedTextColor = Color(0xFF212121),
                        unfocusedTextColor = Color(0xFF212121)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo Teléfono (opcional)
                OutlinedTextField(
                    value = uiState.phone,
                    onValueChange = { signInViewModel.onPhoneChange(it) },
                    label = { Text("Teléfono (opcional)") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Phone,
                            "Teléfono",
                            tint = Color(0xFF6A5AE0)
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6A5AE0),
                        focusedLabelColor = Color(0xFF6A5AE0),
                        cursorColor = Color(0xFF6A5AE0),
                        unfocusedBorderColor = Color(0xFFBDBDBD),
                        unfocusedLabelColor = Color(0xFF757575),
                        focusedTextColor = Color(0xFF212121),
                        unfocusedTextColor = Color(0xFF212121)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo Contraseña
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = { signInViewModel.onPasswordChange(it) },
                    label = { Text("Contraseña") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Lock,
                            "Contraseña",
                            tint = Color(0xFF6A5AE0)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                tint = Color(0xFF757575)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.errorMessage != null && uiState.password.length < 6,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6A5AE0),
                        focusedLabelColor = Color(0xFF6A5AE0),
                        cursorColor = Color(0xFF6A5AE0),
                        unfocusedBorderColor = Color(0xFFBDBDBD),
                        unfocusedLabelColor = Color(0xFF757575),
                        focusedTextColor = Color(0xFF212121),
                        unfocusedTextColor = Color(0xFF212121)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo Confirmar Contraseña
                OutlinedTextField(
                    value = uiState.confirmPassword,
                    onValueChange = { signInViewModel.onConfirmPasswordChange(it) },
                    label = { Text("Confirmar contraseña") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Lock,
                            "Confirmar",
                            tint = Color(0xFF6A5AE0)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (confirmPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                tint = Color(0xFF757575)
                            )
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.errorMessage != null && uiState.password != uiState.confirmPassword,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6A5AE0),
                        focusedLabelColor = Color(0xFF6A5AE0),
                        cursorColor = Color(0xFF6A5AE0),
                        unfocusedBorderColor = Color(0xFFBDBDBD),
                        unfocusedLabelColor = Color(0xFF757575),
                        focusedTextColor = Color(0xFF212121),
                        unfocusedTextColor = Color(0xFF212121)
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Botón Crear Cuenta
                Button(
                    onClick = { signInViewModel.onSignInClick() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = uiState.name.isNotEmpty() &&
                              uiState.email.isNotEmpty() &&
                              uiState.password.isNotEmpty() &&
                              uiState.confirmPassword.isNotEmpty() &&
                              !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6A5AE0),
                        disabledContainerColor = Color(0xFF6A5AE0).copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "CREAR CUENTA",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Mensaje de error
                val errorMessage = uiState.errorMessage
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFD32F2F).copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage,
                                color = Color(0xFFD32F2F),
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Mensaje informativo
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0288D1).copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF0288D1),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Al crear una cuenta, aceptas nuestros términos y condiciones",
                            color = Color(0xFF757575),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

