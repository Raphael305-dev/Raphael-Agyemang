package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Fixture
import com.example.model.LeagueStanding
import com.example.ui.components.BentoBorderColor
import com.example.ui.components.CardBackground
import com.example.ui.components.CustomStatusBadge
import com.example.ui.components.PitchDark
import com.example.ui.components.PitchLime
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeagueScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsState()
    val userTeamId by viewModel.userTeamIdVal.collectAsState()

    val state = gameState ?: return

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Standings, 1 = Matches Schedule
    var selectedMatchWeekFilter by remember { mutableIntStateOf(state.currentMatchDay.coerceAtMost(14)) }
    var selectedFixtureForStats by remember { mutableStateOf<Fixture?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PitchDark)
            .padding(12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // TAB SELECTOR
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
                .border(BorderStroke(1.5.dp, BentoBorderColor), RoundedCornerShape(24.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("LEAGUE TABLE", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("MATCH CALENDAR", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 0) {
            // STANDINGS TABLE
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.5.dp, BentoBorderColor), RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // HEADER ROW
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Pos", modifier = Modifier.width(30.dp), style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(text = "Club", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(text = "Pl", modifier = Modifier.width(28.dp), style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Text(text = "W-D-L", modifier = Modifier.width(52.dp), style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Text(text = "GD", modifier = Modifier.width(32.dp), style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Text(text = "Pts", modifier = Modifier.width(32.dp), style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    }

                    HorizontalDivider(color = Color.DarkGray)

                    // DATA ROWS
                    state.standings.forEachIndexed { idx, row ->
                        val isUser = row.teamId == userTeamId
                        StandingRowItem(
                            pos = idx + 1,
                            entry = row,
                            isUser = isUser
                        )
                    }
                }
            }
        } else {
            // FIXTURES CALENDAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter Match Week",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                // Back and forward arrows
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (selectedMatchWeekFilter > 1) selectedMatchWeekFilter-- },
                        enabled = selectedMatchWeekFilter > 1
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Prev Week", tint = if (selectedMatchWeekFilter > 1) PitchLime else Color.DarkGray)
                    }
                    Text(
                        text = "Matchday $selectedMatchWeekFilter",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = PitchLime,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    IconButton(
                        onClick = { if (selectedMatchWeekFilter < 14) selectedMatchWeekFilter++ },
                        enabled = selectedMatchWeekFilter < 14
                    ) {
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Next Week", tint = if (selectedMatchWeekFilter < 14) PitchLime else Color.DarkGray)
                    }
                }
            }

            val weekFixtures = state.fixtures.filter { it.matchDay == selectedMatchWeekFilter }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                weekFixtures.forEach { fixture ->
                    val isUserMatch = fixture.homeTeamId == userTeamId || fixture.awayTeamId == userTeamId
                    CalendarFixtureItem(
                        fixture = fixture,
                        isUserMatch = isUserMatch,
                        onStatsClick = {
                            selectedFixtureForStats = fixture
                        }
                    )
                }
            }
        }
    }

    selectedFixtureForStats?.let { fixture ->
        MatchStatsDialog(
            fixture = fixture,
            onDismiss = { selectedFixtureForStats = null }
        )
    }
}

@Composable
fun StandingRowItem(
    pos: Int,
    entry: LeagueStanding,
    isUser: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isUser) Color(0xFF142F1C) else Color.Transparent)
            .border(1.dp, if (isUser) PitchLime.copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pos
        Text(
            text = pos.toString(),
            modifier = Modifier.width(28.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isUser) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (isUser) PitchLime else Color.White
        )

        // Club
        Text(
            text = entry.teamName,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isUser) FontWeight.Bold else FontWeight.Normal,
            color = if (isUser) PitchLime else Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Matches Played
        Text(
            text = entry.played.toString(),
            modifier = Modifier.width(28.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.LightGray,
            textAlign = TextAlign.Center
        )

        // W-D-L record
        Text(
            text = "${entry.won}-${entry.drawn}-${entry.lost}",
            modifier = Modifier.width(52.dp),
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        // Goal Difference
        val gdSym = if (entry.goalDifference > 0) "+${entry.goalDifference}" else entry.goalDifference.toString()
        Text(
            text = gdSym,
            modifier = Modifier.width(32.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (entry.goalDifference > 0) PitchLime else if (entry.goalDifference < 0) Color(0xFFFF4136) else Color.Gray,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )

        // Points
        Text(
            text = entry.points.toString(),
            modifier = Modifier.width(32.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (isUser) PitchLime else Color.White,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CalendarFixtureItem(
    fixture: Fixture,
    isUserMatch: Boolean,
    onStatsClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = fixture.isPlayed) { onStatsClick() }
            .border(1.dp, if (isUserMatch) PitchLime.copy(alpha = 0.4f) else Color.DarkGray.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = fixture.homeTeamName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isUserMatch) FontWeight.Bold else FontWeight.Medium,
                color = if (isUserMatch && fixture.homeTeamId.contains("user")) PitchLime else Color.White,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // SCORE INTERACTION BOX
            Box(
                modifier = Modifier
                    .width(78.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF151817)),
                contentAlignment = Alignment.Center
            ) {
                if (fixture.isPlayed) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "${fixture.homeScore} - ${fixture.awayScore}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = PitchLime
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "STATS",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                tint = PitchLime.copy(alpha = 0.7f),
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                } else {
                    Text(
                        text = "VS",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = fixture.awayTeamName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isUserMatch) FontWeight.Bold else FontWeight.Medium,
                color = if (isUserMatch && fixture.awayTeamId.contains("user")) PitchLime else Color.White,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ComparativeStatBar(
    label: String,
    homeValue: Int,
    awayValue: Int,
    homeDisplay: String,
    awayDisplay: String,
    homeColor: Color = PitchLime,
    awayColor: Color = Color(0xFF3498DB)
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = homeDisplay,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold,
                color = homeColor
            )
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 0.5.sp
            )
            Text(
                text = awayDisplay,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold,
                color = awayColor
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        
        // Single split bar matching home vs away ratio
        val total = (homeValue + awayValue).toFloat().coerceAtLeast(1f)
        val homeFraction = homeValue / total
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color(0xFF151817))
        ) {
            // Home portion
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(homeFraction.coerceIn(0.05f, 0.95f))
                    .background(homeColor)
            )
            // Away portion
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight((1f - homeFraction).coerceIn(0.05f, 0.95f))
                    .background(awayColor)
            )
        }
    }
}

@Composable
fun MatchStatsDialog(
    fixture: Fixture,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 24.dp)
                .border(1.5.dp, PitchLime.copy(alpha = 0.6f), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131514))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MATCHDAY ${fixture.matchDay} REPORT",
                        style = MaterialTheme.typography.labelMedium,
                        color = PitchLime,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.LightGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Score Board Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Home
                    Text(
                        text = fixture.homeTeamName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    // Score
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E2120))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${fixture.homeScore} - ${fixture.awayScore}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = PitchLime
                        )
                    }

                    // Away
                    Text(
                        text = fixture.awayTeamName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Pitch & Attendance metadata
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF191C1B))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PITCH STATUS", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        val badgeTxt = when (fixture.pitchCondition) {
                            "EXCELLENT" -> "☀️ Excellent"
                            "SOGGY" -> "🌧️ Soggy Surface"
                            "MUDDY" -> "🌧️ Muddy Field"
                            "FROZEN" -> "❄️ Frozen Turf"
                            else -> "☀️ Excellent"
                        }
                        Text(badgeTxt, style = MaterialTheme.typography.bodySmall, color = Color.LightGray, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ATTENDANCE", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (fixture.stadiumAttendance > 0) String.format("%,d fans", fixture.stadiumAttendance) else "N/A",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Stats Chart Divider Label
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.BarChart, contentDescription = null, tint = PitchLime, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MATCH STATISTICS CHART",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                }

                // Comparative bars
                val homePoss = if (fixture.homePossession > 0) fixture.homePossession else 50
                val awayPoss = if (fixture.awayPossession > 0) fixture.awayPossession else 50
                ComparativeStatBar(
                    label = "Possession",
                    homeValue = homePoss,
                    awayValue = awayPoss,
                    homeDisplay = "$homePoss%",
                    awayDisplay = "$awayPoss%"
                )

                val homeShots = if (fixture.homeShotsOnTarget > 0) fixture.homeShotsOnTarget else fixture.homeScore + 2
                val awayShots = if (fixture.awayShotsOnTarget > 0) fixture.awayShotsOnTarget else fixture.awayScore + 2
                ComparativeStatBar(
                    label = "Shots on Target",
                    homeValue = homeShots,
                    awayValue = awayShots,
                    homeDisplay = "$homeShots shots",
                    awayDisplay = "$awayShots shots",
                    homeColor = Color(0xFF2ECC71),
                    awayColor = Color(0xFF3498DB)
                )

                val homePass = if (fixture.homePassCompletion > 0) fixture.homePassCompletion else 75
                val awayPass = if (fixture.awayPassCompletion > 0) fixture.awayPassCompletion else 75
                ComparativeStatBar(
                    label = "Pass Accuracy",
                    homeValue = homePass,
                    awayValue = awayPass,
                    homeDisplay = "$homePass%",
                    awayDisplay = "$awayPass%",
                    homeColor = Color(0xFF1ABC9C),
                    awayColor = Color(0xFF3498DB)
                )

                // NARRATIVE HIGHLIGHT RECAP SECTION
                fixture.highlightRecap?.let { recap ->
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Assignment, contentDescription = null, tint = Color(0xFF3498DB), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "NARRATIVE HIGHLIGHT RECAP",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF3498DB).copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF191C1B)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            recap.split("\n").forEach { line ->
                                if (line.isNotBlank()) {
                                    val isHeader = line.endsWith(":") || line.startsWith("🎯") || line.startsWith("🟨") || line.startsWith("🏥") || line.startsWith("👥")
                                    val cleanLine = line.replace("**", "")
                                    Text(
                                        text = cleanLine,
                                        style = if (isHeader) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
                                        color = when {
                                            isHeader -> PitchLime
                                            line.contains("Min ") -> Color.White
                                            else -> Color.LightGray
                                        },
                                        fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Close Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF193D22)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Dismiss Report", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = PitchLime)
                }
            }
        }
    }
}
