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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.model.CareerHistoryRecord
import com.example.model.LeagueStanding
import com.example.ui.components.*
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.Screen

@Composable
fun SeasonRecapScreen(viewModel: GameViewModel) {
    val state by viewModel.gameState.collectAsState()
    val game = state ?: return

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
            // Header with Nav Back
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
                        .testTag("season_recap_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PitchLime
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CAREER TIMELINE",
                        style = MaterialTheme.typography.labelSmall,
                        color = PitchLime,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Season Recap & Hall of Fame",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Career Hall of Fame Bento Summary
            val history = game.careerHistory
            val totalSeasons = history.size
            val trophies = history.count { it.trophyWon }
            val careerWins = history.sumOf { it.wins }
            val careerDraws = history.sumOf { it.draws }
            val careerLosses = history.sumOf { it.losses }
            val careerGoalsScored = history.sumOf { it.goalsFor }
            val careerPoints = history.sumOf { it.points }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Seasons Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(BorderStroke(1.dp, BentoBorderColor), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Seasons",
                            tint = PitchLime,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (totalSeasons == 0) "S1 IN PROGRESS" else "$totalSeasons SEASON" + if (totalSeasons > 1) "S" else "",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (totalSeasons == 0) "1st Year" else "$totalSeasons",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Trophies Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(BorderStroke(1.dp, BentoBorderColor), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Trophies",
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "TROPHIES WON",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$trophies",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Career Points Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(BorderStroke(1.dp, BentoBorderColor), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircleOutline,
                            contentDescription = "Points",
                            tint = Color(0xFF64B5F6),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "CAREER POINTS",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$careerPoints pts",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Scrollable Timeline
            Text(
                text = "CAMPAIGN CHRONOLOGY",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (history.isEmpty()) {
                    // Current Season 1 projection UI logic
                    val userTeam = game.teams.firstOrNull { it.isUserControlled }
                    val currentStandIndex = game.standings.indexOfFirst { it.teamId == game.userTeamId }
                    val currentStanding = if (currentStandIndex != -1) game.standings[currentStandIndex] else null
                    val currentPos = if (currentStandIndex != -1) (currentStandIndex + 1) else 3

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(1.dp, PitchLime.copy(alpha = 0.3f)), RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = CardBackground)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timeline,
                                        contentDescription = null,
                                        tint = PitchLime
                                    )
                                    Text(
                                        text = "CURRENT CAMPAIGN PROJECTION",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PitchLime,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(PitchLime.copy(alpha = 0.12f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "LIVE UPDATE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PitchLime,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Divider(color = Color.DarkGray.copy(alpha = 0.3f))

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = userTeam?.name ?: "Club Academy Manager",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Current Matchday: ${game.currentMatchDay - 1} / 14 Matches Played",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.LightGray
                                )
                            }

                            // Projected stats indicators row
                            if (currentStanding != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    StatIndicator("Current Seat", "#$currentPos")
                                    StatIndicator("Points", "${currentStanding.points} pts")
                                    StatIndicator("Win Loss Record", "${currentStanding.won}W - ${currentStanding.drawn}D - ${currentStanding.lost}L")
                                    StatIndicator("Goal Diff", "${currentStanding.goalDifference} GD")
                                }
                            } else {
                                Text(
                                    text = "Launch active matches to verify standard standing projection.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "💡 As you advance the match weeks, this section holds standard campaign highlights. Archive and complete season Year 1 up to Matchday 14 to see final trophy recaps logged here permanently.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                lineHeight = 16.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(history.size) { index ->
                            val record = history[index]
                            SeasonRecordCard(index + 1, record)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SeasonRecordCard(nth: Int, record: CareerHistoryRecord) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                BorderStroke(
                    1.dp,
                    if (record.trophyWon) Color(0xFFFFD54F) else BentoBorderColor
                ),
                RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: season title and highlights
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SEASON RECAP — YEAR ${record.seasonYear}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (record.trophyWon) Color(0xFFFFD54F) else PitchLime,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                if (record.trophyWon) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFFD54F).copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "CHAMPIONS",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else if (record.wasSacked) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFEF5350).copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SACKED BY BOARD",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFEF5350),
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.DarkGray)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Position #${record.finalPosition}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.LightGray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Divider(color = Color.DarkGray.copy(alpha = 0.3f))

            // Body
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = record.clubName,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "${record.points} Points Gained  •  ${record.wins}W — ${record.draws}D — ${record.losses}L",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )
                Text(
                    text = "Goals Forward: ${record.goalsFor}  •  Goals Conceded: ${record.goalsAgainst} (GD: ${record.goalsFor - record.goalsAgainst})",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun StatIndicator(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}
