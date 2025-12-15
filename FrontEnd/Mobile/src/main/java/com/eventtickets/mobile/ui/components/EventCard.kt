package com.eventtickets.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eventtickets.mobile.data.model.Event
import com.eventtickets.mobile.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventCard(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        )
    ) {
        Column {
            // Icono según tipo de evento
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Primary.copy(alpha = 0.7f),
                                Secondary.copy(alpha = 0.9f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getEventIcon(event.eventoTipo.nombre),
                    contentDescription = event.eventoTipo.nombre,
                    tint = Color.White,
                    modifier = Modifier.size(80.dp)
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = event.titulo,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = event.fecha.formatDate(),
                    color = TextSecondary,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Secondary
                    )
                ) {
                    Text(
                        text = event.eventoTipo.nombre,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// Función para obtener icono según tipo de evento
private fun getEventIcon(tipoNombre: String): ImageVector {
    return when (tipoNombre.lowercase()) {
        "música", "musica", "concierto" -> Icons.Default.MusicNote
        "deportes", "deporte", "fútbol", "futbol", "basketball" -> Icons.Default.SportsBasketball
        "teatro", "obra" -> Icons.Default.TheaterComedy
        "cine", "película", "pelicula", "film" -> Icons.Default.Movie
        "arte", "exposición", "exposicion", "galería", "galeria" -> Icons.Default.Palette
        "conferencia", "charla", "seminario" -> Icons.Default.School
        "festival", "feria" -> Icons.Default.Celebration
        "comedia", "humor", "stand-up" -> Icons.Default.SentimentSatisfied
        else -> Icons.Default.Event  // Icono por defecto
    }
}

private fun String.formatDate(): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(this)
        val outputFormat = SimpleDateFormat("d 'de' MMMM 'de' yyyy, HH:mm'h'", Locale("es", "ES"))
        date?.let { outputFormat.format(it) } ?: this
    } catch (e: Exception) {
        this // Devuelve la fecha original si hay un error
    }
}
