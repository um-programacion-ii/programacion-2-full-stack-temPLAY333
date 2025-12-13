package com.eventtickets.mobile.data.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

import com.eventtickets.mobile.data.AppConfig

/**
 * Cliente Retrofit configurado para el backend
 *
 * IMPORTANTE:
 * - En emulador Android usa: http://10.0.2.2:8081
 * - En dispositivo físico usa la IP de tu máquina: http://192.168.X.X:8081
 *
 * Configura la URL en AppConfig.kt
 */
object RetrofitClient {

    // URL base del backend (configurada en AppConfig)
    private val BASE_URL = AppConfig.BACKEND_URL

    // Token JWT almacenado en memoria (en producción usar EncryptedSharedPreferences)
    private var authToken: String? = null

    fun setAuthToken(token: String?) {
        authToken = token
    }

    fun getAuthToken(): String? = authToken

    private val authInterceptor = Interceptor { chain ->
        val request = chain.request()
        val authenticatedRequest = authToken?.let {
            request.newBuilder()
                .header("Authorization", "Bearer $it")
                .build()
        } ?: request

        chain.proceed(authenticatedRequest)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (AppConfig.ENABLE_NETWORK_LOGS) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(AppConfig.HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(AppConfig.HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(AppConfig.HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}

