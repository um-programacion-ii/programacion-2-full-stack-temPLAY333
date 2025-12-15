package com.eventtickets.mobile.data.repository

import com.eventtickets.mobile.data.network.RetrofitClient
import com.eventtickets.mobile.data.network.dto.*

/**
 * Repositorio para manejo de autenticación
 */
class AuthRepository {

    suspend fun register(
        username: String,
        email: String,
        password: String,
        firstName: String? = null,
        lastName: String? = null,
        phone: String? = null
    ): Result<String> {
        return try {
            val response = RetrofitClient.apiService.register(
                RegisterRequest(
                    login = username,    // Mapear username a login para JHipster
                    email = email,
                    password = password,
                    langKey = "es",      // Requerido por JHipster
                    firstName = firstName,
                    lastName = lastName,
                    phone = phone
                )
            )

            // JHipster retorna 201 Created SIN BODY
            if (response.isSuccessful) {
                Result.success("Usuario registrado exitosamente")
            } else {
                // Intentar parsear RFC Problem Details de JHipster
                val errorBody = response.errorBody()?.string()
                val errorMsg = try {
                    if (errorBody != null) {
                        val json = org.json.JSONObject(errorBody)
                        val title = json.optString("title", "Error")
                        val detail = json.optString("detail", "")
                        if (detail.isNotEmpty()) "$title: $detail" else title
                    } else {
                        "Error al crear cuenta"
                    }
                } catch (e: Exception) {
                    when (response.code()) {
                        400 -> "Usuario o email ya existe"
                        401 -> "No autorizado"
                        404 -> "Endpoint no encontrado"
                        500 -> "Error del servidor"
                        else -> "Error al crear cuenta (código ${response.code()})"
                    }
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: java.net.ConnectException) {
            Result.failure(Exception("No se pudo conectar al servidor. ¿El backend está corriendo?"))
        } catch (e: java.net.UnknownHostException) {
            Result.failure(Exception("No se pudo resolver el host. Verifica la URL del backend."))
        } catch (e: java.net.SocketTimeoutException) {
            Result.failure(Exception("Tiempo de espera agotado. El servidor no respondió."))
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e::class.simpleName} - ${e.message}"))
        }
    }

    suspend fun login(username: String, password: String): Result<String> {
        return try {
            val response = RetrofitClient.apiService.authenticate(
                LoginRequest(username, password)
            )

            if (response.isSuccessful && response.body() != null) {
                val token = response.body()!!.idToken
                RetrofitClient.setAuthToken(token)

                // Guardar username
                val tokenManager = com.eventtickets.mobile.EventTicketsApplication.instance.tokenManager
                tokenManager.saveUsername(username)

                Result.success(token)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = when (response.code()) {
                    401 -> "Usuario o contraseña incorrectos"
                    404 -> "Endpoint no encontrado - Verifica que el backend esté corriendo"
                    else -> "Error de autenticación (código ${response.code()}): ${errorBody ?: "Sin detalles"}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: java.net.ConnectException) {
            Result.failure(Exception("No se pudo conectar al servidor. ¿El backend está corriendo?"))
        } catch (e: java.net.UnknownHostException) {
            Result.failure(Exception("No se pudo resolver el host. Verifica la URL del backend."))
        } catch (e: java.net.SocketTimeoutException) {
            Result.failure(Exception("Tiempo de espera agotado. El servidor no respondió."))
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e::class.simpleName} - ${e.message}"))
        }
    }

    suspend fun getAccount(): Result<com.eventtickets.mobile.data.network.AccountDTO> {
        return try {
            println("[AuthRepository] getAccount: llamando ApiService.getAccount()")
            val response = RetrofitClient.apiService.getAccount()
            println("[AuthRepository] getAccount: response.code=${response.code()}")
            val bodyStr = response.errorBody()?.string() ?: response.body()?.toString()
            println("[AuthRepository] getAccount: bodyPreview=${bodyStr?.take(500)}")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                if (response.code() == 401) {
                    // Token inválido o expirado
                    // Limpiar sesión local
                    RetrofitClient.setAuthToken(null)
                    Result.failure(Exception("No autorizado (token inválido)"))
                } else {
                    Result.failure(Exception("Error al obtener cuenta: ${response.code()}"))
                }
            }
        } catch (e: Exception) {
            println("[AuthRepository] getAccount: excepción -> ${e::class.simpleName}: ${e.message}")
            Result.failure(e)
        }
    }

    fun logout() {
        RetrofitClient.setAuthToken(null)
    }

    fun isLoggedIn(): Boolean {
        return RetrofitClient.getAuthToken() != null
    }
}
