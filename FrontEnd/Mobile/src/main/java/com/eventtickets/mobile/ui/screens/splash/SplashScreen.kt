package com.eventtickets.mobile.ui.screens.splash

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eventtickets.mobile.ui.theme.Background
import com.eventtickets.mobile.ui.theme.Primary
import com.eventtickets.mobile.ui.theme.SurfaceVariant
import com.eventtickets.mobile.ui.theme.TextPrimary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        Log.d("AppFlow", "SplashScreen: LaunchedEffect started")
        delay(2000L) // Wait for 2 seconds
        Log.d("AppFlow", "SplashScreen: Delay finished, calling onTimeout")
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Background, SurfaceVariant)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🎫", fontSize = 80.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Text("EventTickets", color = TextPrimary, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(48.dp))
            CircularProgressIndicator(color = Primary)
        }
    }
}
