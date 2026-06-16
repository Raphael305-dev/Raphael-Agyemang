package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.model.Player
import com.example.model.Team
import com.example.ui.components.*
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.Screen

@Composable
fun PlayerStatsScreen(viewModel: GameViewModel) {
    val state by viewModel.gameState.collectAsState()
    val game = state ?: return

    var activeLeaderboardTab by remember { mutableStateOf(0) } // 0=Goals, 1=Assists, 2=Clean Sheets, 3=Yellow Cards, 4=Red Cards

    val tabs = listOf(
        LeaderboardTab("GOALS", Icons.Default.SportsScore, PitchLime),
        LeaderboardTab("ASSISTS", Icons.Default.Transform, Color(0xFF64B5F6)),
        LeaderboardTab("CLEAN SHEETS", Icons.Default.Security, Color(0xFF81C784)),
        LeaderboardTab("YELLOW CARDS", Icons.Default.Square, Color(0xFFFFD54F)),
        LeaderboardTab("RED CARDS", Icons.Default.Square, Color(0xFFEF5350))
    )

    // flatMap all players with their respective parent team context
    val allPlayersAndTeams = remember(game.teams) {
        game.teams.flatMap { team ->
            team.roster.map { player -> player to team }
        }
    }

    // Sort players depending on active tab selected
    val sortedItems = remember(activeLeaderboardTab, allPlayersAndTeams) {
        when (activeLeaderboardTab) {
            0 -> allPlayersAndTeams.sortedByDescending { it.first.goalsScored }.filter { it.first.goalsScored > 0 }
            1 -> allPlayersAndTeams.sortedByDescending { it.first.assists }.filter { it.first.assists > 0 }
            2 -> allPlayersAndTeams.sortedByDescending { it.first.cleanSheets }.filter { it.first.cleanSheets > 0 }
            3 -> allPlayersAndTeams.sortedByDescending { it.first.yellowCards }.filter { it.first.yellowCards > 0 }
            4 -> allPlayersAndTeams.sortedByDescending { it.first.redCards }.filter { it.first.redCards > 0 }
            else -> emptyList()
        }.take(20) // Select Top 20
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchDark)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = { viewModel.selectScreen(Screen.Dashboard) },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(CardBackground)
                        .testTag("player_stats_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PitchLime
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "LEAGUE LEADERS",
                        style = MaterialTheme.typography.labelSmall,
                        color = PitchLime,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Player Statistics Hub",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Horizontal Category Selector Cards Scroll
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { index, tab ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (activeLeaderboardTab == index) tab.accentColor.copy(alpha = 0.15f) else CardBackground)
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (activeLeaderboardTab == index) tab.accentColor else BentoBorderColor
                                ),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { activeLeaderboardTab = index }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("leaderboard_tag_$index"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (activeLeaderboardTab == index) tab.accentColor else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (activeLeaderboardTab == index) Color.White else Color.Gray
                            )
                        }
                    }
                }
            }

            // Statistics Leaderboard List Panel
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardBackground)
                    .border(BorderStroke(1.dp, BentoBorderColor), RoundedCornerShape(16.dp))
            ) {
                if (sortedItems.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "Empty",
                            tint = Color.DarkGray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No recorded stats yet",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Simulate and play league Matchdays to see players populate the goals, assists, and disciplinary cards leaderboards.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            // Column labels
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "RANK / SQUAD PLAYER",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = tabs[activeLeaderboardTab].title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = tabs[activeLeaderboardTab].accentColor,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Divider(color = Color.DarkGray.copy(alpha = 0.5f))
                        }

                        items(sortedItems.size) { index ->
                            val (player, team) = sortedItems[index]
                            val tab = tabs[activeLeaderboardTab]
                            val statVal = when (activeLeaderboardTab) {
                                0 -> "${player.goalsScored} Goals"
                                1 -> "${player.assists} Assists"
                                2 -> "${player.cleanSheets} Sheets"
                                3 -> "${player.yellowCards} Yellow"
                                4 -> "${player.redCards} Red"
                                else -> "0"
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (team.isUserControlled) PitchDark.copy(alpha = 0.5f) else Color.Transparent)
                                    .padding(vertical = 8.dp, horizontal = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Rank index number
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (index) {
                                                    0 -> Color(0xFFFFD54F)
                                                    1 -> Color(0xFFB0BEC5)
                                                    2 -> Color(0xFFFFAB91)
                                                    else -> Color.DarkGray.copy(alpha = 0.5f)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (index <= 2) PitchDark else Color.White,
                                            fontSize = 11.sp
                                        )
                                    }

                                    // Name and Team details
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = player.name,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color.DarkGray)
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = player.position,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.LightGray,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = team.name,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.Gray
                                            )
                                            if (team.isUserControlled) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(PitchLime)
                                                )
                                            }
                                        }
                                    }
                                }

                                // Stat Tally Badge
                                Text(
                                    text = statVal,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Black,
                                    color = if (index == 0) tab.accentColor else Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class LeaderboardTab(
    val title: String,
    val icon: ImageVector,
    val accentColor: Color
)
