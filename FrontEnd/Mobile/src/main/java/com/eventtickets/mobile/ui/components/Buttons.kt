package com.eventtickets.mobile.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eventtickets.mobile.ui.theme.Background
import com.eventtickets.mobile.ui.theme.Primary
import com.eventtickets.mobile.ui.theme.TextDisabled
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled && !loading,
        colors = ButtonDefaults.buttonColors(
            containerColor = Primary,
            contentColor = Background,
            disabledContainerColor = TextDisabled
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Background
            )
        } else {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Primary
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 2.dp,
            brush = SolidColor(Primary)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@SuppressLint("SimpleDateFormat")
fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.forLanguageTag("es-ES"))
        val date = inputFormat.parse(dateString)
        return outputFormat.format(date!!) + "h"
    } catch (e: Exception) {
        "Fecha inválida"
    }
}
