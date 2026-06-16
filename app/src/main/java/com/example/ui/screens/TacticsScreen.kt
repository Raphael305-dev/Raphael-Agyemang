package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.Player
import com.example.model.Team
import com.example.ui.components.*
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TacticsScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsState()
    val userTeamId by viewModel.userTeamIdVal.collectAsState()

    val state = gameState ?: return
    val userTeam = state.teams.firstOrNull { it.id == userTeamId } ?: return

    var playerToReplace by remember { mutableStateOf<Player?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Lineup, 1 = Tactics & Instructions

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PitchDark)
            .padding(12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // TOP TAB SWAPPER
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = CardBackground,
            contentColor = PitchLime,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = PitchLime
                )
            },
            divider = {},
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, BentoBorderColor, RoundedCornerShape(24.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("TEAM LINEUP", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("INSTRUCTIONS", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 0) {
            // THE TACTICAL DRAWING BOARD
            Text(
                text = "Starting XI Position Map",
                style = MaterialTheme.typography.titleSmall,
                color = Color.LightGray,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))

            TacticalPitchBoard(
                startingXI = userTeam.getStartingXI(),
                formation = userTeam.formation,
                onPlayerClick = { player -> playerToReplace = player }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // STARTING XI LIST CARD
            Text(
                text = "Starting XI Lineup",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))

            userTeam.getStartingXI().sortedBy {
                when(it.position) {
                    "GK" -> 1
                    "DEF" -> 2
                    "MID" -> 3
                    else -> 4
                }
            }.forEach { player ->
                PlayerRowLineupItem(
                    player = player,
                    isCaptain = player.id == userTeam.captainId,
                    onSwapClick = { playerToReplace = player }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // BENCH LIST
            Text(
                text = "Bench & Reserves",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))

            val bench = userTeam.getBench()
            if (bench.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CardBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No players on bench", color = Color.Gray)
                }
            } else {
                bench.forEach { player ->
                    PlayerRowBenchItem(
                        player = player,
                        onAppointCaptain = { viewModel.setCaptain(player.id) },
                        onSell = { viewModel.sellPlayer(player.id) },
                        onLoanOut = { viewModel.loanOutPlayer(player.id) }
                    )
                }
            }
        } else {
            // TACTICAL INSTRUCTIONS TAB
            Text(
                text = "Match Formations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Formation buttons grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("4-4-2", "4-3-3", "3-5-2", "5-4-1").forEach { form ->
                    val isSelected = userTeam.formation == form
                    Button(
                        onClick = { viewModel.changeFormation(form) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("formation_btn_$form"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) PitchLime else CardBackground,
                            contentColor = if (isSelected) Color(0xFF381E72) else Color.White
                        ),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.5.dp, if (isSelected) PitchLime else BentoBorderColor)
                    ) {
                        Text(text = form, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Tactical Play Mentalities",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))

            listOf("DEFENSIVE", "BALANCED", "ATTACKING", "COUNTER").forEach { ment ->
                val isSelected = userTeam.playMentality == ment
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(1.dp, if (isSelected) PitchLime else Color.Transparent, RoundedCornerShape(12.dp))
                        .clickable { viewModel.setMentality(ment) }
                        .testTag("mentality_$ment"),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = ment,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) PitchLime else Color.White
                            )
                            Text(
                                text = getMentalityExplainRaw(ment),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                        RadioButton(
                            selected = isSelected,
                            onClick = { viewModel.setMentality(ment) },
                            colors = RadioButtonDefaults.colors(selectedColor = PitchLime)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Pressing Intensity & Pressure",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("CONSERVATIVE", "NORMAL", "AGGRESSIVE").forEach { pres ->
                    val isSelected = userTeam.pressingIntensity == pres
                    Button(
                        onClick = { viewModel.setPressing(pres) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pressing_btn_$pres"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) PitchLime else CardBackground,
                            contentColor = if (isSelected) Color.Black else Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (isSelected) PitchLime else Color.DarkGray)
                    ) {
                        Text(text = pres, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }

    // SWAP MODAL / INTERACTIVE SQUAD DIALOG
    playerToReplace?.let { replacingPlayer ->
        Dialog(onDismissRequest = { playerToReplace = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clip(RoundedCornerShape(16.dp)),
                color = CardBackground,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Substitution / Swap Match",
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
                        text = "Replace Starting Player:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "${replacingPlayer.name} (${replacingPlayer.position} - ${replacingPlayer.overallRating} OVR)",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Select bench replacement talent:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val eligibleBench = userTeam.getBench().filter { it.position == replacingPlayer.position || true } // simplify let anyone swap
                    if (eligibleBench.isEmpty()) {
                        Text(
                            text = "No replacements in bench. Search scouts!",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Box(modifier = Modifier.heightIn(max = 240.dp)) {
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                eligibleBench.forEach { bPlayer ->
                                    val isInjured = bPlayer.injuryWeeksRemaining > 0
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable(enabled = !isInjured) {
                                                viewModel.swapPlayers(replacingPlayer.id, bPlayer.id)
                                                playerToReplace = null
                                            }
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = bPlayer.name,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isInjured) Color.Gray else Color.White
                                                )
                                                if (isInjured) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(Color(0xFFE74C3C))
                                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            "🤕 INJURED",
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = if (isInjured) "OUT — ${bPlayer.injuryType ?: "Unavailable"}"
                                                       else "${bPlayer.position} | OVR ${bPlayer.overallRating} | Stamina ${bPlayer.stamina}%",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }
                                        if (!isInjured) {
                                            Icon(
                                                imageVector = Icons.Default.SwapCalls,
                                                contentDescription = "Swap",
                                                tint = PitchLime
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
}

private fun getMentalityExplainRaw(mentality: String) = when (mentality) {
    "DEFENSIVE" -> "+25% defensive stability: reduces shooting opportunities conceded, reduces fatigue."
    "ATTACKING" -> "+15% striker efficiency: increases goal scoring chances, drains stamina faster."
    "COUNTER" -> "+10% interceptions: converts rival defense turnovers into instantaneous highlights."
    else -> "Balanced orientation: equal chances allocation across all positions."
}

@Composable
fun PlayerRowLineupItem(
    player: Player,
    isCaptain: Boolean,
    onSwapClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(CardBackground)
            .clickable { onSwapClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (player.position == "GK") Color(0xFFF1C40F) else PitchDark),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = player.position,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (player.position == "GK") PitchDark else Color.White
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
                    if (isCaptain) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFFE67E22))
                                .padding(horizontal = 4.dp)
                        ) {
                            Text("C", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    if (player.injuryWeeksRemaining > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE74C3C))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "🤕 OUT ${player.injuryWeeksRemaining}W",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
                Text(
                    text = "OVR: ${player.overallRating} | Form: ${String.format("%.2f", player.form)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = 8.dp)) {
                Text(
                    text = "Stamina",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Text(
                    text = "${player.stamina}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (player.stamina > 70) PitchLime else if (player.stamina > 40) Color.Yellow else Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = onSwapClick) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Swap player",
                    tint = PitchLime
                )
            }
        }
    }
}

@Composable
fun PlayerRowBenchItem(
    player: Player,
    onAppointCaptain: () -> Unit,
    onSell: () -> Unit,
    onLoanOut: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(CardBackground)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2C3E50)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = player.position,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.LightGray
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
                    if (player.injuryWeeksRemaining > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE74C3C))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "🤕 OUT ${player.injuryWeeksRemaining}W",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
                Text(
                    text = "OVR: ${player.overallRating} (Pot: ${player.currentPotential}➔${player.futurePotential}) | £${String.format("%.1f", player.value.toDouble() / 1_000_000)}M Val",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Actions",
                    tint = Color.Gray
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(CardBackground)
            ) {
                DropdownMenuItem(
                    text = { Text("Appoint Captain", color = Color.White) },
                    onClick = {
                        onAppointCaptain()
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, tint = PitchLime) }
                )
                DropdownMenuItem(
                    text = { Text("Loan Out Player (10 Weeks)", color = PitchLime) },
                    onClick = {
                        onLoanOut()
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = PitchLime) }
                )
                DropdownMenuItem(
                    text = { Text("Sell Depth Player", color = Color.Red) },
                    onClick = {
                        onSell()
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.Sell, contentDescription = null, tint = Color.Red) }
                )
            }
        }
    }
}
