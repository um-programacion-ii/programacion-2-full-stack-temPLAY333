package com.eventtickets.mobile.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Gestor de tokens JWT
 * Almacena el token en SharedPreferences para mantener la sesión del usuario
 */
class TokenManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "event_tickets_prefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USERNAME = "username"

        @Volatile
        private var instance: TokenManager? = null

        fun getInstance(context: Context): TokenManager {
            return instance ?: synchronized(this) {
                instance ?: TokenManager(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
     * Guarda el token JWT
     */
    fun saveToken(token: String) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    /**
     * Obtiene el token JWT
     */
    fun getToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    /**
     * Guarda el nombre de usuario
     */
    fun saveUsername(username: String) {
        prefs.edit().putString(KEY_USERNAME, username).apply()
    }

    /**
     * Obtiene el nombre de usuario
     */
    fun getUsername(): String? {
        return prefs.getString(KEY_USERNAME, null)
    }

    /**
     * Verifica si hay una sesión activa
     */
    fun hasValidSession(): Boolean {
        return getToken() != null
    }

    /**
     * Limpia la sesión (logout)
     */
    fun clearSession() {
        prefs.edit().clear().apply()
    }
}

