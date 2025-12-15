package com.eventtickets.mobile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test de instrumentación para verificar el flujo de navegación principal.
 * Este test se ejecuta en un emulador o dispositivo Android.
 */
@RunWith(AndroidJUnit4::class)
class MainFlowTest {

    /**
     * Regla que permite controlar y testear componentes de Compose dentro de una Activity.
     */
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun splashScreen_to_loginScreen_navigation_flow() {
        // 1. Verificar que la SplashScreen se muestra primero
        // Buscamos el nodo (componente) que contiene el texto "EventTickets" y verificamos que se está mostrando.
        composeTestRule.onNodeWithText("EventTickets").assertIsDisplayed()

        // 2. Esperar a que pase el tiempo del splash (2 segundos) y ocurra la navegación.
        // Le damos un margen de 3 segundos para asegurar que la navegación se complete.
        Thread.sleep(3000)

        // 3. Verificar que la LoginScreen se muestra después del splash
        // Buscamos el nodo que contiene el texto "Ingresa a tu cuenta" y verificamos que ahora es visible.
        composeTestRule.onNodeWithText("Ingresa a tu cuenta").assertIsDisplayed()
    }
}
