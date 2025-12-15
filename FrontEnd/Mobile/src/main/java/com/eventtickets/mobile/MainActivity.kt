package com.eventtickets.mobile

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.eventtickets.mobile.ui.components.BottomNavBar
import com.eventtickets.mobile.ui.components.BottomNavItem
import com.eventtickets.mobile.ui.screens.attendees.AttendeeNamesScreen
import com.eventtickets.mobile.ui.screens.confirmseats.ConfirmSeatsScreen
import com.eventtickets.mobile.ui.screens.eventdetail.EventDetailScreen
import com.eventtickets.mobile.ui.screens.home.HomeScreen
import com.eventtickets.mobile.ui.screens.login.LoginScreen
import com.eventtickets.mobile.ui.screens.profile.ProfileScreen
import com.eventtickets.mobile.ui.screens.signin.SignInScreen
import com.eventtickets.mobile.ui.screens.purchasedetail.PurchaseDetailScreen
import com.eventtickets.mobile.ui.screens.purchases.MyPurchasesScreen
import com.eventtickets.mobile.ui.screens.seatmap.SeatMapScreen
import com.eventtickets.mobile.ui.screens.splash.SplashScreen
import com.eventtickets.mobile.ui.screens.success.PurchaseSuccessScreen
import com.eventtickets.mobile.ui.screens.summary.PurchaseSummaryScreen
import com.eventtickets.mobile.ui.theme.EventTicketsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("AppFlow", "MainActivity: onCreate")
        setContent {
            EventTicketsTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    EventTicketsApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventTicketsApp() {
    Log.d("AppFlow", "EventTicketsApp: Composing")
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            val showBottomBar = currentDestination?.route in listOf(BottomNavItem.Home.route, BottomNavItem.MyPurchases.route, BottomNavItem.Profile.route)

            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentDestination?.route,
                    onNavigate = {
                        navController.navigate(it) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splash", // Start with the splash screen
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("splash") {
                Log.d("AppFlow", "Navigating to SplashScreen")
                SplashScreen(onTimeout = {
                    Log.d("AppFlow", "SplashScreen: Timeout -> Navigating to login")
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true } // Remove splash from back stack
                    }
                })
            }
            composable("login") {
                Log.d("AppFlow", "Navigating to LoginScreen")
                LoginScreen(
                    onLoginSuccess = {
                        Log.d("AppFlow", "LoginScreen: Success -> Navigating to home")
                        navController.navigate(BottomNavItem.Home.route) {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onCreateAccountClick = {
                        Log.d("AppFlow", "LoginScreen: CreateAccount -> Navigating to signin")
                        navController.navigate("signin")
                    }
                )
            }
            composable("signin") {
                Log.d("AppFlow", "Navigating to SignInScreen")
                SignInScreen(
                    onBackClick = {
                        Log.d("AppFlow", "SignInScreen: Back -> Navigating to login")
                        navController.popBackStack()
                    },
                    onSignInSuccess = {
                        Log.d("AppFlow", "SignInScreen: Success -> Navigating to home")
                        navController.navigate(BottomNavItem.Home.route) {
                            popUpTo("signin") { inclusive = true }
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }
            composable(BottomNavItem.Home.route) {
                Log.d("AppFlow", "Navigating to HomeScreen")
                HomeScreen(
                    onEventClick = { eventId -> navController.navigate("eventDetail/$eventId") },
                    onProfileClick = { navController.navigate(BottomNavItem.Profile.route) },
                    onSearchClick = { /* TODO */ }
                )
            }
            composable(BottomNavItem.MyPurchases.route) {
                MyPurchasesScreen(
                    onPurchaseClick = { purchaseId -> navController.navigate("purchaseDetail/$purchaseId") },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(onLogout = {
                    Log.d("AppFlow", "ProfileScreen: Logout -> Navigating to login")
                    navController.navigate("login") {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                    }
                })
            }
            // ... (el resto de las rutas)
            composable(
                route = "eventDetail/{eventId}",
                arguments = listOf(navArgument("eventId") { type = NavType.LongType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getLong("eventId") ?: 0L
                EventDetailScreen(
                    eventId = eventId,
                    onBackClick = { navController.popBackStack() },
                    onBuyTicketsClick = { eId -> navController.navigate("seatMap/$eId") }
                )
            }
            composable(
                route = "seatMap/{eventId}",
                arguments = listOf(navArgument("eventId") { type = NavType.LongType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getLong("eventId") ?: 0L
                SeatMapScreen(
                    eventId = eventId,
                    onBackClick = { navController.popBackStack() },
                    onContinueClick = { selectedSeats ->
                        // Guardar los asientos en PurchaseManager
                        val seatPairs = selectedSeats.map { it.fila to it.columna }
                        com.eventtickets.mobile.data.PurchaseManager.startPurchase(eventId, seatPairs)
                        navController.navigate("confirmSeats/$eventId")
                    }
                )
            }
            composable(
                route = "confirmSeats/{eventId}",
                arguments = listOf(navArgument("eventId") { type = NavType.LongType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getLong("eventId") ?: 0L
                ConfirmSeatsScreen(
                    eventId = eventId,
                    onBackClick = { navController.popBackStack() },
                    onConfirmClick = { navController.navigate("attendees/$eventId") }
                )
            }
            composable(
                route = "attendees/{eventId}",
                arguments = listOf(navArgument("eventId") { type = NavType.LongType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getLong("eventId") ?: 0L
                AttendeeNamesScreen(
                    eventId = eventId,
                    onBackClick = { navController.popBackStack() },
                    onConfirmClick = { navController.navigate("summary/$eventId") },
                    onTimeExpired = {
                        // Navegar a EventDetail cuando expire el tiempo
                        navController.navigate("eventDetail/$eventId") {
                            // Limpiar el backstack hasta EventDetail
                            popUpTo("eventDetail/$eventId") { inclusive = true }
                        }
                    }
                )
            }
            composable(
                route = "summary/{eventId}",
                arguments = listOf(navArgument("eventId") { type = NavType.LongType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getLong("eventId") ?: 0L
                PurchaseSummaryScreen(
                    eventId = eventId,
                    onBackClick = { navController.popBackStack() },
                    onConfirmClick = { saleId -> navController.navigate("success/$saleId") { popUpTo(BottomNavItem.Home.route) } }
                )
            }
            composable(
                route = "success/{saleId}",
                arguments = listOf(navArgument("saleId") { type = NavType.LongType })
            ) { backStackEntry ->
                val saleId = backStackEntry.arguments?.getLong("saleId") ?: 0L
                PurchaseSuccessScreen(
                    ventaId = saleId,
                    onBackToHomeClick = { navController.navigate(BottomNavItem.Home.route) { popUpTo(BottomNavItem.Home.route) { inclusive = true } } },
                    onViewPurchasesClick = { navController.navigate("purchaseDetail/$saleId") { popUpTo(BottomNavItem.Home.route) } }
                )
            }
            composable(
                route = "purchaseDetail/{purchaseId}",
                arguments = listOf(navArgument("purchaseId") { type = NavType.LongType })
            ) { backStackEntry ->
                val purchaseId = backStackEntry.arguments?.getLong("purchaseId") ?: 0L
                PurchaseDetailScreen(
                    purchaseId = purchaseId,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
