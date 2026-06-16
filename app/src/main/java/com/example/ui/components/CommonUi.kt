package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.Player

// Dynamic global theme state
var currentThemeChoice by mutableStateOf("BENTO") // BENTO, TURF, SUNSET, GOLD

// Bento Grid Theme Accent Colors
val PitchGreen: Color
    get() = when (currentThemeChoice) {
        "TURF" -> Color(0xFF003F1E)
        "SUNSET" -> Color(0xFF4A141A)
        "GOLD" -> Color(0xFF3F2B00)
        else -> Color(0xFF4F378B)
    }

val ChalkWhite = Color(0xB3EADDFF) // Bento light purple

val PitchDark: Color
    get() = when (currentThemeChoice) {
        "TURF" -> Color(0xFF050A07)
        "SUNSET" -> Color(0xFF0D0607)
        "GOLD" -> Color(0xFF0A0803)
        else -> Color(0xFF1C1B1F)
    }

val CardBackground: Color
    get() = when (currentThemeChoice) {
        "TURF" -> Color(0xFF0C1910)
        "SUNSET" -> Color(0xFF1C0D0E)
        "GOLD" -> Color(0xFF1E1508)
        else -> Color(0xFF313033)
    }

val PitchLime: Color
    get() = when (currentThemeChoice) {
        "TURF" -> Color(0xFF00FF87)
        "SUNSET" -> Color(0xFFFF5E62)
        "GOLD" -> Color(0xFFFFD100)
        else -> Color(0xFFD0BCFF)
    }

val BentoBorderColor: Color
    get() = when (currentThemeChoice) {
        "TURF" -> Color(0xFF152A1C)
        "SUNSET" -> Color(0xFF3D1619)
        "GOLD" -> Color(0xFF3D2D10)
        else -> Color(0xFF49454F)
    }

val DeepOrange = Color(0xFFE91E63) // Match vibrant accents
val GoldMorale = Color(0xFFFFD700)

@Composable
fun TacticalPitchBoard(
    startingXI: List<Player>,
    formation: String,
    modifier: Modifier = Modifier,
    onPlayerClick: (Player) -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(290.dp)
            .padding(4.dp)
            .border(1.5.dp, BentoBorderColor, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = PitchGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Draw field markings
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val strokeWidth = 3f
                val effect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                // Outer border
                drawRect(
                    color = ChalkWhite,
                    topLeft = Offset(10f, 10f),
                    size = Size(w - 20f, h - 20f),
                    style = Stroke(width = strokeWidth)
                )

                // Halfway Line
                drawLine(
                    color = ChalkWhite,
                    start = Offset(w / 2, 10f),
                    end = Offset(w / 2, h - 10f),
                    strokeWidth = strokeWidth
                )

                // Center Circle
                drawCircle(
                    color = ChalkWhite,
                    center = Offset(w / 2, h / 2),
                    radius = (h / 5),
                    style = Stroke(width = strokeWidth)
                )
                
                // Center Spot
                drawCircle(
                    color = ChalkWhite,
                    center = Offset(w / 2, h / 2),
                    radius = 6f
                )

                // Left Penalty box
                drawRect(
                    color = ChalkWhite,
                    topLeft = Offset(10f, h / 6),
                    size = Size(w / 7, h * 2 / 3),
                    style = Stroke(width = strokeWidth)
                )

                // Right Penalty box
                drawRect(
                    color = ChalkWhite,
                    topLeft = Offset(w - (w / 7) - 10f, h / 6),
                    size = Size(w / 7, h * 2 / 3),
                    style = Stroke(width = strokeWidth)
                )
            }

            // Scatter 11 nodes according to formation
            val positionedPlayers = assignTacticalCoordinates(startingXI, formation)

            positionedPlayers.forEach { (player, coords) ->
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(
                            x = (coords.first * 0.88f).dp,
                            y = (coords.second * 0.85f).dp
                        )
                        .testTag("pitch_player_${player.id}")
                        .clickable { onPlayerClick(player) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(62.dp)
                    ) {
                        // Player jersey node
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (player.position == "GK") Color(0xFFF1C40F) else PitchDark)
                                .border(1.5.dp, if (player.injuryWeeksRemaining > 0) Color(0xFFE74C3C) else PitchLime, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = player.overallRating.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (player.position == "GK") PitchDark else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // Player short name
                        Text(
                            text = player.name.split(" ").lastOrNull() ?: player.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .background(Color(0xE6121413), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                .offset(y = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// Map the starting XI onto vertical/horizontal screen grid based on position
private fun assignTacticalCoordinates(startingXI: List<Player>, formation: String): List<Pair<Player, Pair<Float, Float>>> {
    val result = mutableListOf<Pair<Player, Pair<Float, Float>>>()
    val gks = startingXI.filter { it.position == "GK" }
    val defs = startingXI.filter { it.position == "DEF" }
    val mids = startingXI.filter { it.position == "MID" }
    val fwds = startingXI.filter { it.position == "FWD" }

    // Map GK
    gks.firstOrNull()?.let { result.add(it to Pair(18f, 130f)) }

    // Map Defenders
    val dSize = defs.size
    defs.forEachIndexed { i, p ->
        val yOffset = if (dSize <= 1) 130f else 35f + (i * (190f / (dSize - 1)))
        result.add(p to Pair(85f, yOffset))
    }

    // Map Midfielders
    val mSize = mids.size
    mids.forEachIndexed { i, p ->
        val yOffset = if (mSize <= 1) 130f else 30f + (i * (200f / (mSize - 1)))
        result.add(p to Pair(180f, yOffset))
    }

    // Map Forwards
    val fSize = fwds.size
    fwds.forEachIndexed { i, p ->
        val yOffset = if (fSize <= 1) 130f else 45f + (i * (170f / (fSize - 1)))
        result.add(p to Pair(278f, yOffset))
    }

    // Safety fallback for extra/misplaced nodes
    val processedIds = result.map { it.first.id }.toSet()
    val remaining = startingXI.filter { it.id !in processedIds }
    remaining.forEachIndexed { i, p ->
        result.add(p to Pair(150f, 40f + (i * 40f)))
    }

    return result.take(11)
}

@Composable
fun StatDetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    highlight: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (highlight) PitchLime else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(end = 6.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.SemiBold,
            color = if (highlight) PitchLime else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun CustomStatusBadge(
    text: String,
    containerColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(30.dp))
            .background(containerColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ProgressBarWithLabel(
    label: String,
    progress: Float, // 0f to 1f
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = activeColor,
            trackColor = Color(0x33FFFFFF)
        )
    }
}

@Composable
fun TransactionTitleBar(
    title: String,
    budget: Long,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp)),
        color = CardBackground,
        border = BorderStroke(1.5.dp, BentoBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SportsBasketball,
                    contentDescription = null,
                    tint = PitchLime,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF142F1C))
                    .border(1.dp, PitchLime, RoundedCornerShape(16.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AttachMoney,
                    contentDescription = null,
                    tint = PitchLime,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${String.format("%.1f", budget.toDouble() / 1_000_000)}M",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = PitchLime
                )
            }
        }
    }
}
