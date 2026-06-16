package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.Player
import com.example.model.Team

@Composable
fun SquadManagementComponent(
    userTeam: Team,
    onSwapPlayers: (startingId: String, benchId: String) -> Unit,
    onChangeFormation: (String) -> Unit,
    onChangeMentality: (String) -> Unit,
    onChangePressing: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf("LINEUP") } // LINEUP or BENCH or TACTICS
    var playerToReplace by remember { mutableStateOf<Player?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CardBackground)
            .border(1.5.dp, BentoBorderColor, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        // HEADER ROW
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SQUAD WORKBENCH",
                    style = MaterialTheme.typography.labelSmall,
                    color = PitchLime,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${userTeam.name} (OVR ${userTeam.teamRating})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(PitchGreen.copy(alpha = 0.3f))
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = null,
                    tint = PitchLime,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // TAB NAVIGATION BAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val tabs = listOf(
                "LINEUP" to "Starting XI",
                "BENCH" to "Bench Sub",
                "TACTICS" to "Tactics"
            )
            tabs.forEach { (tabId, tabName) ->
                val isSelected = activeTab == tabId
                Button(
                    onClick = { activeTab = tabId },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) PitchLime else Color(0xFF2C2B2D),
                        contentColor = if (isSelected) Color.Black else Color.LightGray
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("squad_tab_$tabId"),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = tabName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // DYNAMIC CONTENT AREA
        when (activeTab) {
            "LINEUP" -> {
                val startingXI = userTeam.getStartingXI()
                if (startingXI.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No players in starting XI!", color = Color.Gray)
                    }
                } else {
                    Text(
                        text = "Tap on any starter to make a tactical substitution:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Box(modifier = Modifier.heightIn(max = 280.dp)) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(startingXI) { player ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF1E1E20))
                                        .border(
                                            1.dp,
                                            if (player.id == userTeam.captainId) PitchLime.copy(alpha = 0.4f) else Color.Transparent,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { playerToReplace = player }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        // Position Badge
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(if (player.position == "GK") Color(0xFFF1C40F) else Color(0xFF3A393C)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = player.position,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (player.position == "GK") Color.Black else Color.White
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = player.name,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                if (player.id == userTeam.captainId) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(CircleShape)
                                                            .background(PitchLime)
                                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                                    ) {
                                                        Text("Ⓒ", fontSize = 8.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                            
                                            // Stamina progress bar with label
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier.padding(top = 4.dp)
                                            ) {
                                                val staminaColor = when {
                                                    player.stamina >= 70 -> PitchLime
                                                    player.stamina >= 45 -> Color(0xFFF1C40F)
                                                    else -> Color(0xFFE74C3C)
                                                }
                                                LinearProgressIndicator(
                                                    progress = { player.stamina / 100f },
                                                    color = staminaColor,
                                                    trackColor = Color(0x22FFFFFF),
                                                    modifier = Modifier
                                                        .width(60.dp)
                                                        .height(4.dp)
                                                        .clip(RoundedCornerShape(2.dp))
                                                )
                                                Text(
                                                    text = "Stamina ${player.stamina}%",
                                                    fontSize = 9.sp,
                                                    color = staminaColor
                                                )
                                            }
                                        }
                                    }

                                    // Form & rating details on the right
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "${player.overallRating} OVR",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "⭐ " + String.format("%.1f", player.form),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = PitchLime,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.Default.SwapHoriz,
                                            contentDescription = "Swap",
                                            tint = PitchLime,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "BENCH" -> {
                val bench = userTeam.getBench()
                if (bench.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No substitute players on bench!", color = Color.Gray)
                    }
                } else {
                    Text(
                        text = "Substitute bench squad:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Box(modifier = Modifier.heightIn(max = 280.dp)) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(bench) { player ->
                                val isInjured = player.injuryWeeksRemaining > 0
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isInjured) Color(0x11FF0000) else Color(0xFF1E1E20))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(if (isInjured) Color(0xFF2C1E1E) else Color(0xFF333235)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = player.position,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = player.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isInjured) Color.Gray else Color.White
                                            )
                                            if (isInjured) {
                                                Text(
                                                    "Injured: ${player.injuryType} (${player.injuryWeeksRemaining} wks)",
                                                    fontSize = 9.sp,
                                                    color = Color(0xFFE74C3C),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            } else {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    val staminaColor = when {
                                                        player.stamina >= 70 -> PitchLime
                                                        player.stamina >= 45 -> Color(0xFFF1C40F)
                                                        else -> Color(0xFFE74C3C)
                                                    }
                                                    LinearProgressIndicator(
                                                        progress = { player.stamina / 100f },
                                                        color = staminaColor,
                                                        trackColor = Color(0x22FFFFFF),
                                                        modifier = Modifier
                                                            .width(50.dp)
                                                            .height(4.dp)
                                                            .clip(RoundedCornerShape(2.dp))
                                                    )
                                                    Text(
                                                        text = "${player.stamina}% Stam",
                                                        fontSize = 9.sp,
                                                        color = staminaColor
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${player.overallRating} OVR",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isInjured) Color.Gray else Color.White
                                        )
                                        Text(
                                            text = "⭐ " + String.format("%.1f", player.form),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = PitchLime,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "TACTICS" -> {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // FORMATION SECTOR
                    Text(
                        text = "Match Formation",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("4-4-2", "4-3-3", "3-5-2", "5-4-1").forEach { form ->
                            val isSelected = userTeam.formation == form
                            Button(
                                onClick = { onChangeFormation(form) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) PitchLime else Color(0xFF2C2B2D),
                                    contentColor = if (isSelected) Color.Black else Color.White
                                ),
                                border = if (isSelected) null else BorderStroke(1.dp, Color.DarkGray),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .testTag("inmatch_form_$form"),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(form, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // PLAY MENTALITY SECTOR
                    Text(
                        text = "Play Mentality",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("DEFENSIVE", "BALANCED", "ATTACKING", "COUNTER").forEach { ment ->
                            val isSelected = userTeam.playMentality == ment
                            Button(
                                onClick = { onChangeMentality(ment) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) PitchLime else Color(0xFF2C2B2D),
                                    contentColor = if (isSelected) Color.Black else Color.White
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .testTag("inmatch_ment_$ment"),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = ment.take(5) + if (ment.length > 5) "." else "",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // PRESSING INTENSITY SECTOR
                    Text(
                        text = "Pressing Intensity & Physical Stress",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("CONSERVATIVE", "NORMAL", "AGGRESSIVE").forEach { pres ->
                            val isSelected = userTeam.pressingIntensity == pres
                            Button(
                                onClick = { onChangePressing(pres) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) PitchLime else Color(0xFF2C2B2D),
                                    contentColor = if (isSelected) Color.Black else Color.White
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .testTag("inmatch_pres_$pres"),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(pres, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // SWAP / SUBSTITUTION SELECTION DIALOG
    playerToReplace?.let { replacingPlayer ->
        Dialog(onDismissRequest = { playerToReplace = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clip(RoundedCornerShape(20.dp)),
                color = CardBackground,
                border = BorderStroke(1.5.dp, BentoBorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SELECT SUB REPLACEMENT",
                            style = MaterialTheme.typography.labelSmall,
                            color = PitchLime,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { playerToReplace = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Replacing: ${replacingPlayer.name} (${replacingPlayer.position})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(10.dp))

                    val eligibleBench = userTeam.getBench().filter { it.injuryWeeksRemaining == 0 }
                    if (eligibleBench.isEmpty()) {
                        Text(
                            text = "No active players available on substitute bench!",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        Box(modifier = Modifier.heightIn(max = 240.dp)) {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(eligibleBench) { bPlayer ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                onSwapPlayers(replacingPlayer.id, bPlayer.id)
                                                playerToReplace = null
                                            }
                                            .background(Color(0xFF1E1E20))
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = bPlayer.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "${bPlayer.position} | OVR ${bPlayer.overallRating} | ${bPlayer.stamina}% Stamina",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.LightGray
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.Default.SwapHoriz,
                                            contentDescription = "Confirm Swap",
                                            tint = PitchLime,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
