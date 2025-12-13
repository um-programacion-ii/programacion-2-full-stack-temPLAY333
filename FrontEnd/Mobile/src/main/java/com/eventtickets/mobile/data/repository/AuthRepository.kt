package com.eventtickets.mobile.data.repository

import com.eventtickets.mobile.data.network.RetrofitClient
import com.eventtickets.mobile.data.network.dto.*

/**
 * Repositorio para manejo de autenticación
 */
class AuthRepository {

    suspend fun login(username: String, password: String): Result<String> {
        return try {
            val response = RetrofitClient.apiService.authenticate(
                LoginRequest(username, password)
            )

            if (response.isSuccessful && response.body() != null) {
                val token = response.body()!!.idToken
                RetrofitClient.setAuthToken(token)
                Result.success(token)
            } else {
                Result.failure(Exception("Error de autenticación: ${response.code()}"))
            }
        } catch (e: Exception) {
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

