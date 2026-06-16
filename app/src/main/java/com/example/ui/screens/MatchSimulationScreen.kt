package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.model.MatchCommentaryEvent
import com.example.ui.components.*
import com.example.viewmodel.GameViewModel

@Composable
fun MatchSimulationScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val liveFixture by viewModel.liveMatchFixture.collectAsState()
    val liveMinute by viewModel.liveMatchMinute.collectAsState()
    val homeScore by viewModel.liveHomeScore.collectAsState()
    val awayScore by viewModel.liveAwayScore.collectAsState()
    val commentary by viewModel.liveCommentary.collectAsState()
    val matchStatus by viewModel.matchStatus.collectAsState()
    val isFastForward by viewModel.isFastForward.collectAsState()

    val gameState by viewModel.gameState.collectAsState()
    val userTeamId by viewModel.userTeamIdVal.collectAsState()

    val state = gameState ?: return
    val userTeam = state.teams.first { it.id == userTeamId }
    val fixture = liveFixture ?: return

    val listState = rememberLazyListState()

    // Auto-scroll the commentary to the bottom as events occur
    LaunchedEffect(commentary.size) {
        if (commentary.isNotEmpty()) {
            listState.animateScrollToItem(commentary.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PitchDark)
            .padding(16.dp)
    ) {
        // MATCH STATUS BAR
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, PitchLime.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "MATCH SIMULATION LIVE",
                    style = MaterialTheme.typography.labelSmall,
                    color = PitchLime,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                // Pitch/Weather Condition Badge
                val conditionColor = when (fixture.pitchCondition) {
                    "EXCELLENT" -> PitchLime
                    "SOGGY" -> Color(0xFF3498DB)
                    "MUDDY" -> Color(0xFFE5A93B)
                    "FROZEN" -> Color(0xFF95A5A6)
                    else -> Color.Gray
                }
                val conditionEmoji = when (fixture.pitchCondition) {
                    "EXCELLENT" -> "☀️ Excellent pitch"
                    "SOGGY" -> "🌧️ Soggy surface (stamina drain)"
                    "MUDDY" -> "🌧️ Muddy field (slip-ups, foul risk!)"
                    "FROZEN" -> "❄️ Icy/Frozen turf (speed penalty)"
                    else -> "Excellent pitch"
                }

                Box(
                    modifier = Modifier
                        .padding(top = 4.dp, bottom = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(conditionColor.copy(alpha = 0.15f))
                        .border(1.dp, conditionColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = conditionEmoji,
                        style = MaterialTheme.typography.labelSmall,
                        color = conditionColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // CURRENT SCORE BOARD
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = fixture.homeTeamName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // SCORE SPLASH
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF142F1C))
                            .border(1.dp, PitchLime, RoundedCornerShape(8.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = homeScore.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = PitchLime
                        )
                        Text(
                            text = " - ",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = awayScore.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = PitchLime
                        )
                    }

                    Text(
                        text = fixture.awayTeamName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // MINUTE DIALER
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (matchStatus == "PLAYING") PitchLime else Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (matchStatus == "FULL_TIME") "90' (Full Time)" else "${liveMinute}' Minute",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Progress line
                LinearProgressIndicator(
                    progress = { liveMinute / 90f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = PitchLime,
                    trackColor = Color(0x33FFFFFF)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- TAB BAR SELECTOR ---
        var activeSubTab by remember(matchStatus) { 
            mutableStateOf(if (matchStatus == "PRE_MATCH") "SQUAD" else "LIVE") 
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Button(
                onClick = { activeSubTab = "BRIEFING" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeSubTab == "BRIEFING") PitchLime else CardBackground,
                    contentColor = if (activeSubTab == "BRIEFING") Color.Black else Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).height(40.dp).border(1.dp, if (activeSubTab == "BRIEFING") Color.Transparent else BentoBorderColor, RoundedCornerShape(10.dp)),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Briefing", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { activeSubTab = "LIVE" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeSubTab == "LIVE") PitchLime else CardBackground,
                    contentColor = if (activeSubTab == "LIVE") Color.Black else Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).height(40.dp).border(1.dp, if (activeSubTab == "LIVE") Color.Transparent else BentoBorderColor, RoundedCornerShape(10.dp)),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Commentary", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { activeSubTab = "STATS" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeSubTab == "STATS") PitchLime else CardBackground,
                    contentColor = if (activeSubTab == "STATS") Color.Black else Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1.1f).height(40.dp).border(1.dp, if (activeSubTab == "STATS") Color.Transparent else BentoBorderColor, RoundedCornerShape(10.dp)),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Live Stats", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { activeSubTab = "SQUAD" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeSubTab == "SQUAD") PitchLime else CardBackground,
                    contentColor = if (activeSubTab == "SQUAD") Color.Black else Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1.3f).height(40.dp).border(1.dp, if (activeSubTab == "SQUAD") Color.Transparent else BentoBorderColor, RoundedCornerShape(10.dp)),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Squad & Tactics", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // --- SUBTAB ROUTER SWITCHER ---
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (activeSubTab) {
                "BRIEFING" -> {
                    // Pre-Match Briefing
                    val oppTeam = state.teams.firstOrNull { it.id == (if (fixture.homeTeamId == userTeamId) fixture.awayTeamId else fixture.homeTeamId) }
                    val highestRatedOpponent = oppTeam?.roster?.maxByOrNull { it.overallRating }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, BentoBorderColor, RoundedCornerShape(12.dp))
                            .background(CardBackground)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Text(
                                text = "📋 MATCH DAY PREVIEW REPORT",
                                style = MaterialTheme.typography.labelSmall,
                                color = PitchLime,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        // Pitch report
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0x19FFFFFF)),
                                modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "🏟️ Field Surface Condition: ${fixture.pitchCondition}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = when (fixture.pitchCondition) {
                                            "SOGGY" -> "Heavy rainfall creates soggy ground! Player stamina will drain -10% faster."
                                            "MUDDY" -> "Muddy turf multiplies physical stress! Player fatigue and severe injury rates are doubled during match minutes."
                                            "FROZEN" -> "Frozen surface causes high slips! Tactics calculations will decrease ball speed by -8%."
                                            else -> "Outstanding condition. Perfect setup for technical passes."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.LightGray
                                    )
                                }
                            }
                        }

                        // Oppponent rating info
                        if (oppTeam != null) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0x19FFFFFF)),
                                    modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = "⚡ Opposition Overview: ${oppTeam.name} (OVR: ${oppTeam.teamRating})",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                        if (highestRatedOpponent != null) {
                                            Text(
                                                text = "Scout Alert: Midfield coordinator **${highestRatedOpponent.name}** (${highestRatedOpponent.position}, OVR ${highestRatedOpponent.overallRating}) acts as their primary playmaker. Adjust defensive blocks as appropriate.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.LightGray
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Fatigue status listing
                        val lowFit = userTeam.roster.filter { it.stamina < 65 || it.fatigue > 50 }
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "🚨 PHYSICAL INTEGRITY WARNINGS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Red,
                                    fontWeight = FontWeight.Bold
                                )

                                if (lowFit.isEmpty()) {
                                    Text(
                                        text = "✔ Outstanding squad physics! All starting lineup assets have great stamina.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PitchLime
                                    )
                                } else {
                                    lowFit.forEach { p ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("• ${p.name} (${p.position})", style = MaterialTheme.typography.bodySmall, color = Color.White)
                                            Text("Fatigue: ${p.fatigue}% • Stamina: ${p.stamina}%", style = MaterialTheme.typography.bodySmall, color = Color.Red, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Text(
                                        text = "We advise utilizing the Fitness Conditioning program at the Training Academy to avoid muscle tears or match injuries.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                "STATS" -> {
                    // Match Outcome Visualizer View
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, BentoBorderColor, RoundedCornerShape(12.dp))
                            .background(CardBackground)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "📊 LIVE INTERACTIVE STATS MAP",
                                style = MaterialTheme.typography.labelSmall,
                                color = PitchLime,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        // Possession Bar
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${fixture.homeTeamName} ${fixture.homePossession}%", style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text("Ball Control", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text("${fixture.awayPossession}% ${fixture.awayTeamName}", style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(Color.DarkGray)
                                ) {
                                    Box(modifier = Modifier.weight(fixture.homePossession.toFloat().coerceAtLeast(1f)).fillMaxHeight().background(PitchLime))
                                    Box(modifier = Modifier.weight(fixture.awayPossession.toFloat().coerceAtLeast(1f)).fillMaxHeight().background(Color(0xFF3498DB)))
                                }
                            }
                        }

                        // Shots on Target
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${fixture.homeShotsOnTarget} Hits", style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text("Shots on Target", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text("${fixture.awayShotsOnTarget} Hits", style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(Color.DarkGray)
                                ) {
                                    val homeShots = fixture.homeShotsOnTarget.toFloat().coerceAtLeast(0.1f)
                                    val awayShots = fixture.awayShotsOnTarget.toFloat().coerceAtLeast(0.1f)
                                    Box(modifier = Modifier.weight(homeShots).fillMaxHeight().background(Color(0xFFE74C3C)))
                                    Box(modifier = Modifier.weight(awayShots).fillMaxHeight().background(Color(0xFFF1C40F)))
                                }
                            }
                        }

                        // Passing success
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${fixture.homePassCompletion}% Acc", style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text("Passing Precision", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text("${fixture.awayPassCompletion}% Acc", style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(Color.DarkGray)
                                ) {
                                    val homePass = fixture.homePassCompletion.toFloat().coerceAtLeast(1f)
                                    val awayPass = fixture.awayPassCompletion.toFloat().coerceAtLeast(1f)
                                    Box(modifier = Modifier.weight(homePass).fillMaxHeight().background(Color(0xFF2ECC71)))
                                    Box(modifier = Modifier.weight(awayPass).fillMaxHeight().background(Color(0xFF9859B6)))
                                }
                            }
                        }

                        // Pitch Map representation of match
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF14221D)),
                                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF2ECC71).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("⚽ Strategic Field Domination", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(
                                        text = when {
                                            homeScore > awayScore -> "${fixture.homeTeamName} holds a strong central block, winning key transitional challenges and converting clean opportunities."
                                            homeScore < awayScore -> "${fixture.awayTeamName} plays deep counter moves, breaking through isolated lines down the flanks."
                                            else -> "Midfield stalemate. High defensive concentration remains active across the center line."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.LightGray
                                    )
                                }
                            }
                        }
                    }
                }

                "LIVE" -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // LIVE TACTICS TWEAK BAR UNDER MID-MATCH OR HALF-TIME
                        if (matchStatus == "PRE_MATCH" || matchStatus == "HALF_TIME") {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = CardBackground),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Tactician Mid-Match Review",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Mentality", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                            Button(
                                                onClick = {
                                                    val nextMent = when(userTeam.playMentality) {
                                                        "BALANCED" -> "ATTACKING"
                                                        "ATTACKING" -> "DEFENSIVE"
                                                        "DEFENSIVE" -> "COUNTER"
                                                        else -> "BALANCED"
                                                    }
                                                    viewModel.setMentality(nextMent)
                                                },
                                                modifier = Modifier.fillMaxWidth().testTag("sub_tactic_mentality"),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x11FFFFFF), contentColor = PitchLime),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text(userTeam.playMentality, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Intensity", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                            Button(
                                                onClick = {
                                                    val nextInt = when(userTeam.pressingIntensity) {
                                                        "NORMAL" -> "AGGRESSIVE"
                                                        "AGGRESSIVE" -> "CONSERVATIVE"
                                                        else -> "NORMAL"
                                                    }
                                                    viewModel.setPressing(nextInt)
                                                },
                                                modifier = Modifier.fillMaxWidth().testTag("sub_tactic_pressing"),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x11FFFFFF), contentColor = PitchLime),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text(userTeam.pressingIntensity, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // POST-MATCH NARRATIVE HIGHLIGHT RECAP CARD (Visible when FULL TIME)
                        if (matchStatus == "FULL_TIME") {
                            fixture.highlightRecap?.let { recap ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                        .border(1.dp, Color(0xFF3498DB).copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Assignment,
                                                contentDescription = null,
                                                tint = Color(0xFF3498DB),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "NARRATIVE HIGHLIGHTS RECAP",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF3498DB),
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        
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
                        }

                        // POST-MATCH FINANCIAL BREAKDOWN CARD (Visible when FULL TIME)
                        if (matchStatus == "FULL_TIME") {
                            val isUserHome = fixture.homeTeamId == userTeamId
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                                    .border(1.5.dp, PitchLime.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF142F1C)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "MATCHDAY FINANCIAL RECEIPTS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PitchLime,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    if (isUserHome) {
                                        // Calculate metrics dynamically based on standard formula
                                        val baseRate = 0.55 + (userTeam.teamRating / 400.0) + (userTeam.fanSatisfaction / 400.0)
                                        val weatherPenalty = when (fixture.pitchCondition) {
                                            "SOGGY" -> 0.05
                                            "MUDDY" -> 0.15
                                            "FROZEN" -> 0.10
                                            else -> 0.0
                                        }
                                        val attendanceRate = (baseRate - weatherPenalty).coerceIn(0.40, 1.0)
                                        val attendance = (userTeam.stadiumCapacity * attendanceRate).toInt()
                                        val totalTicketReceipts = attendance.toLong() * userTeam.stadiumTicketPrice
                                        val concessionsRevenue = attendance.toLong() * 12
                                        val grandTotal = totalTicketReceipts + concessionsRevenue

                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Stadium Venue:", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                                            Text(userTeam.stadiumName, style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Stadium Capacity:", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                                            Text("${String.format("%,d", userTeam.stadiumCapacity)} seats", style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Match Attendance:", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                                            Text("${String.format("%,d", attendance)} (${String.format("%.1f", attendanceRate * 100)}%)", style = MaterialTheme.typography.bodySmall, color = PitchLime, fontWeight = FontWeight.Bold)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Ticket Sales (£${userTeam.stadiumTicketPrice}/seat):", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                                            Text("£${String.format("%,d", totalTicketReceipts)}", style = MaterialTheme.typography.bodySmall, color = Color.White)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Concessions Spend (£12/person):", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                                            Text("£${String.format("%,d", concessionsRevenue)}", style = MaterialTheme.typography.bodySmall, color = Color.White)
                                        }
                                        
                                        HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 10.dp))
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Revenue Deposited:", style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
                                            Text("+£${String.format("%,d", grandTotal)}", style = MaterialTheme.typography.bodyMedium, color = PitchLime, fontWeight = FontWeight.ExtraBold)
                                        }
                                    } else {
                                        // Played away
                                        Text(
                                            text = "We played Away at ${fixture.homeTeamName}. The home club claimed gate receipts of £200k+, but our supporters made substantial noise! Satisfied fans are key to next home fixture's turnouts.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.LightGray
                                        )
                                    }
                                }
                            }
                        }

                        // COMMENTARY LOG SCROLLER
                        Text(
                            text = "Match Highlights & Logs",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color.DarkGray, RoundedCornerShape(12.dp))
                                .background(CardBackground)
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(commentary) { idx, event ->
                                CommentaryBubble(event = event)
                            }
                        }
                    }
                }

                "SQUAD" -> {
                    com.example.ui.components.SquadManagementComponent(
                        userTeam = userTeam,
                        onSwapPlayers = { startingId, benchId ->
                            viewModel.swapPlayers(startingId, benchId)
                        },
                        onChangeFormation = { form ->
                            viewModel.changeFormation(form)
                        },
                        onChangeMentality = { ment ->
                            viewModel.setMentality(ment)
                        },
                        onChangePressing = { pres ->
                            viewModel.setPressing(pres)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ACTION BUTTONS SECTION BASED ON STATE
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (matchStatus) {
                "PRE_MATCH" -> {
                    Button(
                        onClick = { viewModel.startLiveMatchSimulation() },
                        colors = ButtonDefaults.buttonColors(containerColor = PitchLime, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("match_kickoff_btn")
                    ) {
                        Text("KICK OFF MATCH DAY!", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                    }
                }
                "PLAYING" -> {
                    Button(
                        onClick = { viewModel.pauseLiveMatch() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f), contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(50.dp)
                            .testTag("match_pause_btn")
                    ) {
                        Text("PAUSE MATCH", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    // Toggles simulation speed (Fast forward)
                    Button(
                        onClick = { viewModel.toggleFastForward() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFastForward) PitchLime else Color(0xFF2C3E50),
                            contentColor = if (isFastForward) Color.Black else Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("match_speed_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isFastForward) Icons.Default.FastForward else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isFastForward) "2x Fast" else "Normal", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                "HALF_TIME" -> {
                    Button(
                        onClick = { viewModel.startLiveMatchSimulation() },
                        colors = ButtonDefaults.buttonColors(containerColor = PitchLime, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("match_resume_btn")
                    ) {
                        Text("RESUME SECOND HALF", fontWeight = FontWeight.ExtraBold)
                    }
                }
                "FULL_TIME" -> {
                    Button(
                        onClick = { viewModel.advanceMatchDay() },
                        colors = ButtonDefaults.buttonColors(containerColor = PitchLime, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("match_advance_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.LockClock, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("COMPLETE FIXTURE & ADVANCE LINE", fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommentaryBubble(event: MatchCommentaryEvent) {
    val bBg = when (event.type) {
        "GOAL" -> Color(0xFF142F1C)
        "CARD" -> Color(0xFF2C1919)
        "SAVE" -> Color(0xFF1B2B3A)
        else -> Color.Transparent
    }
    val bBorder = when(event.type) {
        "GOAL" -> PitchLime.copy(alpha = 0.5f)
        "CARD" -> Color.Red.copy(alpha = 0.5f)
        "SAVE" -> Color(0xFF3498DB).copy(alpha = 0.5f)
        else -> Color.Transparent
    }
    val bText = when(event.type) {
        "GOAL" -> PitchLime
        "CARD" -> Color(0xFFFF4136)
        "SAVE" -> Color(0xFF3498DB)
        else -> Color.LightGray
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bBg)
            .border(1.dp, bBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = when(event.type) {
                    "GOAL" -> Icons.Default.SportsBasketball
                    "CARD" -> Icons.Default.Warning
                    "SAVE" -> Icons.Default.Shield
                    else -> Icons.Default.ChatBubbleOutline
                },
                contentDescription = null,
                tint = bText,
                modifier = Modifier
                    .size(16.dp)
                    .offset(y = 2.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyMedium,
                color = if (event.type == "GOAL") Color.White else bText,
                lineHeight = 18.sp,
                fontWeight = if (event.type == "GOAL") FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
