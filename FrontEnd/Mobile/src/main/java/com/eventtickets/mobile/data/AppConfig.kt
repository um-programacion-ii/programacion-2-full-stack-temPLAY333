package com.eventtickets.mobile.data

/**
 * Configuración global de la app
 *
 * Cambia USE_MOCK_DATA para alternar entre datos mock y backend real
 */
object AppConfig {

    /**
     * true = Usa MockData (sin conexión a backend)
     * false = Usa backend real en http://10.0.2.2:8081
     *
     * Cambia esto según tus necesidades:
     * - Para desarrollo/demo SIN backend: true
     * - Para testing CON backend: false
     */
    const val USE_MOCK_DATA = true // <-- CAMBIA ESTO SEGÚN NECESITES

    /**
     * URL del backend (solo se usa si USE_MOCK_DATA = false)
     *
     * - Emulador Android: "http://10.0.2.2:8081"
     * - Dispositivo físico: "http://TU_IP_LOCAL:8081" (ej: "http://192.168.1.100:8081")
     */
    const val BACKEND_URL = "http://10.0.2.2:8081"

    /**
     * Timeout para requests HTTP (en segundos)
     */
    const val HTTP_TIMEOUT_SECONDS = 30L

    /**
     * Habilitar logs detallados de red
     */
    const val ENABLE_NETWORK_LOGS = true
}

