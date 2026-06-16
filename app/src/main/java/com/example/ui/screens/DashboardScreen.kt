package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.InboxMessage
import com.example.model.Team
import com.example.ui.components.*
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.Screen

@Composable
fun DashboardScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsState()
    val userTeamId by viewModel.userTeamIdVal.collectAsState()

    val state = gameState ?: return
    val userTeam = state.teams.firstOrNull { it.id == userTeamId } ?: return

    var selectedMessage by remember { mutableStateOf<InboxMessage?>(null) }
    var showVoteDialog by remember { mutableStateOf(false) }
    var showPresidentInfoDialog by remember { mutableStateOf(false) }

    val initials = if (state.managerName.isNotEmpty()) {
        state.managerName.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("").take(2)
    } else {
        "JK"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PitchDark)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // BENTO HEADER BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = userTeam.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = PitchLime, // Bento Lavender Accent
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(PitchGreen) // Bento deep purple accent
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Manager: ${state.managerName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFEADDFF),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Right budget profile
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { viewModel.selectScreen(Screen.Financials) }
                    .padding(4.dp)
                    .testTag("dashboard_budget_indicator")
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "BUDGET 📊",
                        style = MaterialTheme.typography.labelSmall,
                        color = PitchLime,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "£${String.format("%.1f", state.transferBudget.toDouble() / 1_000_000)}M",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(PitchLime),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF381E72)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // BENTO BLOCK 1: NEXT MATCH HERO BANNER (Full Width)
        val currentFixture = state.fixtures.firstOrNull {
            it.matchDay == state.currentMatchDay && (it.homeTeamId == userTeamId || it.awayTeamId == userTeamId)
        }

        if (state.isUserSacked) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color(0xFFE74C3C), RoundedCornerShape(28.dp))
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = null,
                        tint = Color(0xFFE74C3C),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "TERMINATION OF CONTRACT",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "You have been sacked from your position as manager of ${userTeam.name} due to poor season performance. Below are vacancy contracts from other league clubs seeking leadership. Accept an offer to sign the contract.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFCAC4D0),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (currentFixture != null && state.currentMatchDay <= 14) {
            val isHome = currentFixture.homeTeamId == userTeamId
            val rivalName = if (isHome) currentFixture.awayTeamName else currentFixture.homeTeamName
            val venue = if (isHome) "Home Match" else "Away Match"

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, BentoBorderColor, RoundedCornerShape(28.dp))
                    .testTag("launch_match_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "NEXT MATCH",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = PitchLime,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "vs $rivalName",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Text(
                                text = "Matchday ${state.currentMatchDay} of 14 • $venue",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCAC4D0),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(PitchGreen)
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsSoccer,
                                contentDescription = null,
                                tint = Color(0xFFEADDFF),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy((-8).dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(Color(0xFFEFB8C8), Color(0xFFD0BCFF), Color(0xFFCCC2DC)).forEach { col ->
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(col)
                                        .border(2.dp, CardBackground, CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "Squad fit & match ready",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFCAC4D0)
                            )
                        }

                        Button(
                            onClick = { viewModel.launchNextMatch() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PitchLime,
                                contentColor = Color(0xFF381E72)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("play_match_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color(0xFF381E72),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("KICKOFF", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, BentoBorderColor, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = PitchLime,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "SEASON COMPLETED!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "You have finished your season fixtures! Process the transition to start next year's campaign with your current squad, process retirements, contract expirations, and receive the board budget allocation.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFCAC4D0),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.startNextSeason() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PitchLime,
                            contentColor = Color(0xFF381E72)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("start_next_season_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.NavigateNext, contentDescription = null, tint = Color(0xFF381E72))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("START NEXT SEASON", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // JOB OFFERS REGISTRY BOARD
        if (state.isUserSacked || state.availableJobOffers.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(
                            if (state.isUserSacked) 2.dp else 1.dp,
                            if (state.isUserSacked) Color(0xFFE74C3C) else BentoBorderColor
                        ),
                        RoundedCornerShape(28.dp)
                    ),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "OFFICIAL REGULATOR BOARD",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (state.isUserSacked) Color(0xFFE74C3C) else PitchLime,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = if (state.isUserSacked) "🚨 VACANT NEW CONTRACTS" else "💼 EXOTIC JOB OFFERS",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (state.isUserSacked) Color(0xFFE74C3C).copy(alpha = 0.2f) else PitchGreen)
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assignment,
                                contentDescription = null,
                                tint = if (state.isUserSacked) Color(0xFFE74C3C) else Color(0xFFEADDFF),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (state.isUserSacked) {
                            "You have been dismissed from your duties. You must accept a job offering from one of the following clubs to sign a contract and launch the new season campaign."
                        } else {
                            "Your achievements have caught the eye of opposing club directors! Consider moving to a new team to gain different squad depths and manager challenges."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        state.availableJobOffers.forEach { offer ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1c1f1e))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = offer.teamName,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Squad Rating: ⭐ ${offer.teamRating}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.LightGray
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(PitchLime.copy(alpha = 0.15f))
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "Budget £${String.format("%.1f", offer.budget.toDouble() / 1_000_000)}M",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Black,
                                                color = PitchLime
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = offer.reason,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray,
                                        lineHeight = 15.sp
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { viewModel.acceptJobOffer(offer.teamId) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (state.isUserSacked) Color(0xFFE74C3C) else PitchGreen,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(36.dp)
                                            .testTag("accept_job_${offer.teamId}"),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.LockOpen,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("SIGN CONTRACT WITH ${offer.teamName.uppercase()}", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // BENTO BLOCK 2 & 3: ASYMMETRIC GRID ROW
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left block - TACTICS SPOTLIGHT
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp)
                    .clickable { viewModel.selectScreen(com.example.viewmodel.Screen.Tactics) },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEADDFF))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = Color(0xFF381E72),
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = "Tactics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF21005D)
                        )
                        Text(
                            text = "${userTeam.formation} Holding",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF381E72),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Right block - SCOUTING REPORT
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp)
                    .border(1.5.dp, BentoBorderColor, RoundedCornerShape(28.dp))
                    .clickable { viewModel.selectScreen(com.example.viewmodel.Screen.Scouting) },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonSearch,
                            contentDescription = null,
                            tint = PitchLime,
                            modifier = Modifier.size(24.dp)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF49454F))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "NEW",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Scouting",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Targets focus ready",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFCAC4D0)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // BENTO BLOCK 4: TRAINING PROGRESS STATUS (Horizontal Pill Banner)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, BentoBorderColor, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF49454F))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Training Progress",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { 0.75f },
                            color = PitchLime,
                            trackColor = Color(0xFF1C1B1F),
                            modifier = Modifier
                                .width(130.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "75%",
                            style = MaterialTheme.typography.labelSmall,
                            color = PitchLime,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "FOCUS: DEFENSIVE POSITIONING",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFEADDFF),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CardBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = Color(0xFFEADDFF),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // BENTO BLOCK 5: MINI STATS GRID SLOTS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            InfoBlockMetric(
                label = "Formation",
                value = userTeam.formation,
                icon = Icons.Default.GridOn,
                modifier = Modifier.weight(1f)
            )
            InfoBlockMetric(
                label = "Team Rating",
                value = "${userTeam.teamRating} OVR",
                icon = Icons.Default.Leaderboard,
                modifier = Modifier.weight(1f)
            )
            InfoBlockMetric(
                label = "Squad Size",
                value = "${userTeam.roster.size}/25 Players",
                icon = Icons.Default.People,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // STAFF INBOX SECTION
        // STADIUM AND SUPPORTERS HUB BENTO BLOCK
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, BentoBorderColor, RoundedCornerShape(28.dp))
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground)
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "STADIUM & SUPPORTERS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PitchLime,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = userTeam.stadiumName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PitchGreen)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Domain,
                            contentDescription = null,
                            tint = Color(0xFFEADDFF),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "CAPACITY", style = MaterialTheme.typography.labelSmall, color = Color(0xFFCAC4D0))
                        Text(
                            text = String.format("%,d seats", userTeam.stadiumCapacity),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "TICKET PRICE", style = MaterialTheme.typography.labelSmall, color = Color(0xFFCAC4D0))
                        Text(
                            text = "£${userTeam.stadiumTicketPrice}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Fan satisfaction meter
                Text(text = "FAN SATISFACTION", style = MaterialTheme.typography.labelSmall, color = Color(0xFFCAC4D0))
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { userTeam.fanSatisfaction.toFloat() / 100f },
                        color = if (userTeam.fanSatisfaction >= 70) PitchLime else if (userTeam.fanSatisfaction >= 45) Color(0xFFFFC107) else Color(0xFFE91E63),
                        trackColor = Color(0xFF1C1B1F),
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${userTeam.fanSatisfaction}% " + when {
                            userTeam.fanSatisfaction >= 80 -> "🔥"
                            userTeam.fanSatisfaction >= 65 -> "😊"
                            userTeam.fanSatisfaction >= 45 -> "😐"
                            else -> "😠"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // BENTO BLOCK: FINANCIAL STATEMENT & TREASURY
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, BentoBorderColor, RoundedCornerShape(28.dp))
                .clickable { viewModel.selectScreen(Screen.Financials) }
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground)
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CLUB BOARDROOM TREASURY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PitchLime,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Financial Statement & Ledger",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PitchGreen)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color(0xFFEADDFF),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "LIQUID RESERVES", style = MaterialTheme.typography.labelSmall, color = Color(0xFFCAC4D0))
                        Text(
                            text = "£${String.format("%.1f", state.transferBudget.toDouble() / 1_000_000)}M",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF81C784)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        val rosterWage = userTeam.roster.sumOf { it.contractWage }
                        Text(text = "SQUAD WAGES/WK", style = MaterialTheme.typography.labelSmall, color = Color(0xFFCAC4D0))
                        Text(
                            text = "£${String.format("%,d", rosterWage)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFE57373)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Tap to open financial spreadsheet & expense logs",
                        fontSize = 11.sp,
                        color = PitchLime,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = PitchLime,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 1: Board Directives
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.5.dp, BentoBorderColor, RoundedCornerShape(20.dp))
                    .clickable { viewModel.selectScreen(Screen.BoardGoals) }
                    .testTag("dashboard_board_goals_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = PitchLime,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "${userTeam.boardConfidence}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (userTeam.boardConfidence >= 55) Color(0xFF2ECC71) else Color(0xFFE74C3C),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Board Goals",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Track directives & club trust",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        lineHeight = 14.sp
                    )
                }
            }

            // Card 2: Commercial Deals
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.5.dp, BentoBorderColor, RoundedCornerShape(20.dp))
                    .clickable { viewModel.selectScreen(Screen.Sponsorships) }
                    .testTag("dashboard_sponsorships_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val activeDealsCount = state.activeSponsorships.size
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Handshake,
                            contentDescription = null,
                            tint = PitchLime,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "$activeDealsCount Active",
                            style = MaterialTheme.typography.labelSmall,
                            color = PitchLime,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Brand Deals",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Negotiate commercial sponsors",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 3: Staff Management
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.5.dp, BentoBorderColor, RoundedCornerShape(20.dp))
                    .clickable { viewModel.selectScreen(Screen.Staff) }
                    .testTag("dashboard_staff_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val activeStaffCount = state.hiredStaff.size
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = PitchLime,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "$activeStaffCount/3",
                            style = MaterialTheme.typography.labelSmall,
                            color = PitchLime,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Club Staff",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Hire Coaches & Physios",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        lineHeight = 14.sp
                    )
                }
            }

            // Card 4: Player Stats
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.5.dp, BentoBorderColor, RoundedCornerShape(20.dp))
                    .clickable { viewModel.selectScreen(Screen.PlayerStats) }
                    .testTag("dashboard_player_stats_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = PitchLime,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Stats",
                            style = MaterialTheme.typography.labelSmall,
                            color = PitchLime,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Squad Stats",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Goals, Assists & Cards",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        lineHeight = 14.sp
                    )
                }
            }

            // Card 5: Season Recap
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.5.dp, BentoBorderColor, RoundedCornerShape(20.dp))
                    .clickable { viewModel.selectScreen(Screen.SeasonRecap) }
                    .testTag("dashboard_season_recap_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val seasonsCount = state.careerHistory.size
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = PitchLime,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Y$seasonsCount",
                            style = MaterialTheme.typography.labelSmall,
                            color = PitchLime,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Season Recap",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Past achievements log",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- NEW ROW of CUSTOM BENTO CARDS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card: Training Academy
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.5.dp, BentoBorderColor, RoundedCornerShape(20.dp))
                    .clickable { viewModel.selectScreen(Screen.Training) }
                    .testTag("dashboard_training_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val isTrained = state.isTrainingConductedThisWeek
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = PitchLime,
                            modifier = Modifier.size(20.dp)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isTrained) Color(0xFF1B2F22) else Color(0xFF2E2413))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isTrained) "DONE" else "READY",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isTrained) PitchLime else Color(0xFFFF9800),
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp
                            )
                        }
                    }
                    Text(
                        text = "Training Center",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Practice Drills & Legend transitions",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        lineHeight = 14.sp
                    )
                }
            }

            // Card: President & Executive Suite
            val isVoteDue = viewModel.isPresidentialElectionDue
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.5.dp, if (isVoteDue) PitchLime else BentoBorderColor, RoundedCornerShape(20.dp))
                    .clickable {
                        if (isVoteDue) {
                            showVoteDialog = true
                        } else {
                            showPresidentInfoDialog = true
                        }
                    }
                    .testTag("dashboard_executive_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = if (isVoteDue) Color(0xFF1B221E) else CardBackground)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.HowToVote,
                            contentDescription = null,
                            tint = if (isVoteDue) PitchLime else Color(0xFF3498DB),
                            modifier = Modifier.size(20.dp)
                        )
                        if (isVoteDue) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(PitchLime)
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "VOTE NOW",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }
                    Text(
                        text = "Executive Suite",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "President: ${userTeam.presidentName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Staff & Board Inbox",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(start = 4.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (state.inbox.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.5.dp, BentoBorderColor, RoundedCornerShape(22.dp))
                    .background(CardBackground),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Empty inbox folders", color = Color.Gray)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.inbox.forEach { msg ->
                    InboxRowItem(
                        msg = msg,
                        onOpen = {
                            viewModel.markMessageRead(msg.id)
                            selectedMessage = msg
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // LOCAL STORAGE BACKUP & PERSISTENCE BLOCK
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, BentoBorderColor, RoundedCornerShape(28.dp))
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PERSISTENCE LABORATORY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PitchLime,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Local Storage Backup",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2E7D32).copy(alpha = 0.2f))
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = PitchLime,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Backup or restore your active campaign squad data and league standings directly to your browser's persistent local storage. This preserves your progress safely.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.saveToLocalStorage() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("local_storage_save_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SAVE TO LOCAL", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    Button(
                        onClick = { viewModel.loadFromLocalStorage() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1976D2),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("local_storage_load_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("LOAD FROM LOCAL", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // RESIGNATION DESK BLOCK
        if (!state.isUserSacked) {
            var showResignConfirmDialog by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, BentoBorderColor, RoundedCornerShape(28.dp))
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "BOARDROOM DESK",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = PitchLime,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Club Resignation Desk",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF4A1521))
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = null,
                                tint = Color(0xFFFFDAD9),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Instantly resign from your current post to become an unemployed free agent. The regulator board will immediately issue vacant job offers wishing for your tactile leadership on the vacancy registry.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showResignConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4A1521),
                            contentColor = Color(0xFFFFDAD9)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("resign_button")
                    ) {
                        Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("RESIGN FROM CURRENT CLUB", fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                }
            }

            if (showResignConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showResignConfirmDialog = false },
                    title = {
                        Text(
                            text = "Confirm Mutual Resignation?",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    text = {
                        Text(
                            text = "Are you absolutely sure you want to terminate your contract with ${userTeam.name}?\n\nYou will immediately leave your office, forfeit your active upgrades, and enter the Free Agent vacancy board.",
                            color = Color.LightGray
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showResignConfirmDialog = false
                                viewModel.resignFromCurrentClub()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE74C3C), contentColor = Color.White)
                        ) {
                            Text("YES, RESIGN", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showResignConfirmDialog = false }) {
                            Text("CANCEL", color = Color.White)
                        }
                    },
                    containerColor = CardBackground,
                    shape = RoundedCornerShape(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // CAREER PROFILE AND HISTORY BLOCK
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, BentoBorderColor, RoundedCornerShape(28.dp))
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CAREER STATISTICS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PitchLime,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Managerial History",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PitchGreen)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HistoryEdu,
                            contentDescription = null,
                            tint = Color(0xFFEADDFF),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val totalTrophies = state.careerHistory.count { it.trophyWon }
                val totalSacks = state.careerHistory.count { it.wasSacked }
                val totalSeasons = state.careerHistory.size

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF151817))
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("SEASONS", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("$totalSeasons", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color.White)
                    }
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF151817))
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("LEAGUE TITLES", style = MaterialTheme.typography.labelSmall, color = Color.Gray, maxLines = 1)
                        Text("$totalTrophies 🏆", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = if (totalTrophies > 0) PitchLime else Color.White)
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF151817))
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("SACKINGS", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("$totalSacks 🚨", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = if (totalSacks > 0) Color(0xFFE74C3C) else Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color.DarkGray)
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "LIFETIME PERFORMANCE DATA",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = PitchLime,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Match stats grid
                val winsPercent = if (state.managerMatches > 0) (state.managerWins * 100) / state.managerMatches else 0
                val drawsPercent = if (state.managerMatches > 0) (state.managerDraws * 100) / state.managerMatches else 0
                val lossesPercent = if (state.managerMatches > 0) (state.managerLosses * 100) / state.managerMatches else 0

                // Aesthetic custom result distribution horizontal bar
                if (state.managerMatches > 0) {
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        ) {
                            Box(modifier = Modifier.weight(state.managerWins.toFloat().coerceAtLeast(0.1f)).background(PitchLime))
                            Box(modifier = Modifier.weight(state.managerDraws.toFloat().coerceAtLeast(0.1f)).background(Color.Gray))
                            Box(modifier = Modifier.weight(state.managerLosses.toFloat().coerceAtLeast(0.1f)).background(Color(0xFFE74C3C)))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Wins (${winsPercent}%)", style = MaterialTheme.typography.labelSmall, color = PitchLime)
                            Text("Draws (${drawsPercent}%)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("Losses (${lossesPercent}%)", style = MaterialTheme.typography.labelSmall, color = Color(0xFFE74C3C))
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF151817))
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("PLAYED", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("${state.managerMatches}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color.White)
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF151817))
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("WON", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("${state.managerWins}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = PitchLime)
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF151817))
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("DRAWN", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("${state.managerDraws}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color.Gray)
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF151817))
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("LOST", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("${state.managerLosses}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color(0xFFE74C3C))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Goals stats banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF151817))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("GOALS FOR", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("⚽ ${state.managerGoalsFor}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val goalsDiff = state.managerGoalsFor - state.managerGoalsAgainst
                        Text("GOAL DIFF", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(
                            text = if (goalsDiff > 0) "+$goalsDiff" else "$goalsDiff",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (goalsDiff > 0) PitchLime else if (goalsDiff < 0) Color(0xFFE74C3C) else Color.White
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("GOALS AGAINST", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("🥅 ${state.managerGoalsAgainst}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color.DarkGray)
                Spacer(modifier = Modifier.height(14.dp))

                if (state.careerHistory.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Currently on your first campaign. Complete 14 Matchdays to log your historical performance here!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        state.careerHistory.forEach { record ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF1c1f1e))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Season Year: ${record.seasonYear}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = record.clubName.uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = PitchLime,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${record.wins}W - ${record.draws}D - ${record.losses}L • GF ${record.goalsFor} GA ${record.goalsAgainst}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when {
                                                record.trophyWon -> PitchLime.copy(alpha = 0.15f)
                                                record.wasSacked -> Color(0xFFE74C3C).copy(alpha = 0.15f)
                                                else -> Color.Gray.copy(alpha = 0.15f)
                                            }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when {
                                            record.trophyWon -> "CHAMPION 🏆"
                                            record.wasSacked -> "SACKED 🚨"
                                            else -> "RANK #${record.finalPosition}"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = when {
                                            record.trophyWon -> PitchLime
                                            record.wasSacked -> Color(0xFFE74C3C)
                                            else -> Color.LightGray
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // GLOBAL FOOTBALL NEWS BULLETIN BOARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, BentoBorderColor, RoundedCornerShape(28.dp))
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "GLOBAL FOOTBALL NEWS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PitchLime,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Around the Globe Feed",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PitchGreen)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Newspaper,
                            contentDescription = null,
                            tint = Color(0xFFEADDFF),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (state.globalNewsFeed.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF151817))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(imageVector = Icons.Default.HourglassEmpty, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                            Text(
                                "No news headlines recorded yet. Run match weeks to trigger global updates and dynamic gossip reports!",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        state.globalNewsFeed.take(6).forEach { article ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color.DarkGray, RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF151817)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(PitchLime.copy(alpha = 0.15f))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = article.category,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = PitchLime
                                            )
                                        }
                                        Text(
                                            text = "Week ${article.matchDay} • ${article.source}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = article.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = article.body,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.LightGray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // CLUB RECORDS & STATS BLOCK
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, BentoBorderColor, RoundedCornerShape(28.dp))
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CLUB OF ALL-TIME RECORDS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PitchLime,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Historical Hall of Fame",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PitchGreen)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = Color(0xFFEADDFF),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // All-Time Best Player
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF151817)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("⭐ CLUB BEST PLAYER OF ALL TIME", style = MaterialTheme.typography.labelSmall, color = PitchLime, fontWeight = FontWeight.Bold)
                                Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = PitchLime, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = state.clubAllTimeBestPlayerName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Black, color = Color.White)
                            Text(text = state.clubAllTimeBestPlayerDesc, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }

                    // Record Signed Player
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF151817)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("📈 HIGHEST PLAYER BOUGHT OF ALL TIME", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text("Record Buy", style = MaterialTheme.typography.labelSmall, color = PitchLime)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = state.recordSignName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Black, color = Color.White)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Club: ${state.recordSignClub}", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                                Text("Fee: £${String.format("%,d", state.recordSignValue)}", style = MaterialTheme.typography.bodySmall, color = PitchLime, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Record Sold Player
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF151817)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("📉 HIGHEST PLAYER SOLD OF ALL TIME", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text("Record Sell", style = MaterialTheme.typography.labelSmall, color = Color(0xFFE74C3C))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = state.recordSaleName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Black, color = Color.White)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Destination: ${state.recordSaleClub}", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                                Text("Returns: £${String.format("%,d", state.recordSaleValue)}", style = MaterialTheme.typography.bodySmall, color = PitchLime, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // SQUAD FORM & LOAN STATUS WORKBENCH
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, BentoBorderColor, RoundedCornerShape(28.dp))
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SQUAD FORM & LOAN TRACKER",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PitchLime,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Roster Physical Reports",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PitchGreen)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sports,
                            contentDescription = null,
                            tint = Color(0xFFEADDFF),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val sortedSquad = userTeam.roster.sortedByDescending { it.form }
                Text("Form Breakdown (Highest to Lowest):", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    sortedSquad.take(15).forEach { player ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF151817))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = player.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color.DarkGray)
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(player.position, style = MaterialTheme.typography.labelSmall, color = Color.LightGray, fontSize = 8.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                when {
                                    player.isLoanedIn -> {
                                        Text("Loaned In • from ${player.loanSourceClub} (${player.loanRemainingWeeks} wks remaining)", style = MaterialTheme.typography.labelSmall, color = PitchLime, fontWeight = FontWeight.Bold)
                                    }
                                    player.isLoanedOut -> {
                                        Text("Loaned Out • to ${player.loanDestinationClub} (${player.loanRemainingWeeks} wks remaining)", style = MaterialTheme.typography.labelSmall, color = Color(0xFFE74C3C), fontWeight = FontWeight.Bold)
                                    }
                                    player.injuryWeeksRemaining > 0 -> {
                                        Text("Injured: ${player.injuryType} (${player.injuryWeeksRemaining} wks)", style = MaterialTheme.typography.labelSmall, color = Color(0xFFE74C3C))
                                    }
                                    else -> {
                                        Text("Active Squad Member • Stamina ${player.stamina}%", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                }
                            }

                            // Visual circular element or rating score for Form
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(imageVector = Icons.Default.TrendingUp, contentDescription = null, tint = if (player.form >= 7.0f) PitchLime else Color.Gray, modifier = Modifier.size(12.dp))
                                Text(
                                    text = String.format("%.1f", player.form),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (player.form >= 7.0f) PitchLime else Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Inbox detail reader dialog
    selectedMessage?.let { msg ->
        Dialog(onDismissRequest = { selectedMessage = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
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
                            text = "OFFICIAL INBOX",
                            style = MaterialTheme.typography.labelSmall,
                            color = PitchLime,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { selectedMessage = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = msg.subject,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "From: ${msg.sender}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = msg.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { selectedMessage = null },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PitchLime, contentColor = Color.Black)
                    ) {
                        Text("Acknowledge Message")
                    }
                }
            }
        }
    }

    // Presidential Voting Dialog
    if (showVoteDialog) {
        Dialog(onDismissRequest = { showVoteDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clip(RoundedCornerShape(24.dp)),
                color = CardBackground,
                border = BorderStroke(1.5.dp, PitchLime)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "🗳️ EXECUTIVE BOARDROOM ELECTION",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = PitchLime
                    )

                    Text(
                        text = "The club's presidential campaign is underway. Club shareholders and fans have split support across two candidates. Your public ballot count will determine who is elected to run administrative protocols!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )

                    HorizontalDivider(color = Color.DarkGray)

                    // Candidate A
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BentoBorderColor, RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.castPresidentialVote("A")
                                showVoteDialog = false
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF14222E))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Candidate A: Arthur Pendelton", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Slogan: \"Infrastructure and Sustainable Growth\"", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("✔ PROMISE: Cuts all Stadium and Medical Facility upgrade expenses by 20% indefinitely!", style = MaterialTheme.typography.bodySmall, color = PitchLime)
                        }
                    }

                    // Candidate B
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BentoBorderColor, RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.castPresidentialVote("B")
                                showVoteDialog = false
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1A14))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Candidate B: Julian Sterling", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Slogan: \"Immediate Impact and Star-Driven Era\"", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("✔ PROMISE: Instantly donates +£10,000,000 into our core transfer reserve funds!", style = MaterialTheme.typography.bodySmall, color = PitchLime)
                        }
                    }

                    Button(
                        onClick = { showVoteDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray, contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Abstain from Vote")
                    }
                }
            }
        }
    }

    // Presidential Policy Information Dialog
    if (showPresidentInfoDialog) {
        Dialog(onDismissRequest = { showPresidentInfoDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(20.dp)),
                color = CardBackground,
                border = BorderStroke(1.dp, BentoBorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "👑 ACTIVE CLUB PRESIDENT DETAILS",
                        style = MaterialTheme.typography.labelSmall,
                        color = PitchLime,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = userTeam.presidentName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Policy Orientation:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text(userTeam.presidentType, style = MaterialTheme.typography.bodySmall, color = PitchLime, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Administrative Bonus:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text(userTeam.presidentEffect, style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    HorizontalDivider(color = Color.DarkGray)

                    Text(
                        text = "Next democratic presidential election campaigns occur every few weeks across all league divisions (Match Day 6 and Match Day 11). Keep an eye on executive bulletins!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    Button(
                        onClick = { showPresidentInfoDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = PitchLime, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close Executive Suite")
                    }
                }
            }
        }
    }
}

@Composable
fun InfoBlockMetric(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.5.dp, BentoBorderColor)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PitchLime,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFCAC4D0)
            )
        }
    }
}

@Composable
fun InboxRowItem(
    msg: InboxMessage,
    onOpen: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(CardBackground)
            .border(1.5.dp, if (!msg.isRead) PitchLime else BentoBorderColor, RoundedCornerShape(22.dp))
            .clickable { onOpen() }
            .padding(14.dp)
            .testTag("inbox_item_${msg.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (!msg.isRead) PitchGreen else Color(0x1AFFFFFF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (!msg.isRead) Icons.Default.Markunread else Icons.Default.Drafts,
                contentDescription = null,
                tint = if (!msg.isRead) PitchLime else Color.Gray,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = msg.sender,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (!msg.isRead) PitchLime else Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                if (!msg.isRead) {
                    CustomStatusBadge(
                        text = "NEW",
                        containerColor = PitchLime,
                        textColor = Color(0xFF381E72)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = msg.subject,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (!msg.isRead) FontWeight.Bold else FontWeight.Medium,
                color = if (!msg.isRead) Color.White else Color.LightGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
