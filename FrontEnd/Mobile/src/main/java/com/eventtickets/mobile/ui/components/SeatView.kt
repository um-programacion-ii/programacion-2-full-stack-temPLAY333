package com.eventtickets.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eventtickets.mobile.data.model.Seat
import com.eventtickets.mobile.data.model.SeatState
import com.eventtickets.mobile.ui.theme.*

@Composable
fun SeatView(
    seat: Seat,
    isSelected: Boolean,
    onClick: (Seat) -> Unit,
    enabled: Boolean
) {
    val seatColor = when {
        isSelected -> SeatSelected
        seat.estado == SeatState.AVAILABLE -> SeatAvailable
        seat.estado == SeatState.BLOCKED -> SeatBlocked
        else -> SeatSold
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(seatColor)
            .clickable(enabled = enabled, onClick = { onClick(seat) }),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = seat.columna.toString(),
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
