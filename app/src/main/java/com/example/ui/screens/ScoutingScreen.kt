package com.example.ui.screens

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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Player
import com.example.model.ScoutCandidate
import com.example.model.TransferBid
import com.example.ui.components.*
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoutingScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsState()
    val scoutingFilter by viewModel.scoutingPositionFilter.collectAsState()
    val isScoutingBusy by viewModel.isScoutingBusy.collectAsState()

    val state = gameState ?: return

    var activeTab by remember { mutableStateOf("SCOUT") } // "SCOUT", "TRANSFERS", "INCOMING", "SQUAD"

    // Dialog state
    var showBidDialogForPlayer by remember { mutableStateOf<Pair<Player, String>?>(null) } // (Player, TeamId)
    var showListDialogForPlayer by remember { mutableStateOf<Player?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PitchDark)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // TOP BUDGET PANEL
        TransactionTitleBar(
            title = "Scouting & Transfers",
            budget = state.transferBudget
        )

        Spacer(modifier = Modifier.height(16.dp))

        // BENTO STYLE TAB BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CardBackground)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf(
                "SCOUT" to "Scouting",
                "TRANSFERS" to "Market",
                "INCOMING" to "Bids",
                "SQUAD" to "Squad"
            )
            tabs.forEach { (tabKey, label) ->
                val isSelected = activeTab == tabKey
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) PitchLime else Color.Transparent)
                        .clickable { activeTab = tabKey }
                        .testTag("transfer_tab_$tabKey"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        color = if (isSelected) Color(0xFF381E72) else Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        var scoutSubTab by remember { mutableStateOf("FIRST_TEAM") } // "FIRST_TEAM", "YOUTH_ACADEMY"
        val selectedScoutRegions = remember { mutableStateMapOf<String, String>() }
        val selectedScoutPositions = remember { mutableStateMapOf<String, String>() }

        when (activeTab) {
            "SCOUT" -> {
                // Sub-tabs row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF131514))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (scoutSubTab == "FIRST_TEAM") PitchGreen else Color.Transparent)
                            .clickable { scoutSubTab = "FIRST_TEAM" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("FIRST SQUAD MARKET", fontSize = 10.sp, fontWeight = FontWeight.Black, color = if (scoutSubTab == "FIRST_TEAM") PitchLime else Color.Gray)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (scoutSubTab == "YOUTH_ACADEMY") PitchGreen else Color.Transparent)
                            .clickable { scoutSubTab = "YOUTH_ACADEMY" }
                            .testTag("nav_youth_academy_tab"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("YOUTH SCOUT ACADEMY", fontSize = 10.sp, fontWeight = FontWeight.Black, color = if (scoutSubTab == "YOUTH_ACADEMY") PitchLime else Color.Gray)
                    }
                }

                if (scoutSubTab == "FIRST_TEAM") {
                    // Position filter selector Row
                    Text(
                        text = "Scouting Target Focus",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("ALL", "GK", "DEF", "MID", "FWD").forEach { pos ->
                            val isSelected = scoutingFilter == pos
                            Button(
                                onClick = { viewModel.setScoutingFilter(pos) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("scouting_filter_$pos"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) PitchLime else CardBackground,
                                    contentColor = if (isSelected) Color(0xFF381E72) else Color.White
                                ),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.5.dp, if (isSelected) PitchLime else BentoBorderColor),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(text = pos, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // LAUNCH SCOUT ACTION BUTTON
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        border = BorderStroke(1.5.dp, BentoBorderColor)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Launch Scouting Commission",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Launch specialist scouts to find fresh available players matching your current target focus position.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 4.dp),
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { viewModel.scoutNewTalent() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PitchLime,
                                    contentColor = Color(0xFF381E72)
                                ),
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("scout_new_prospects_btn"),
                                enabled = !isScoutingBusy
                            ) {
                                if (isScoutingBusy) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF381E72))
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color(0xFF381E72))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("COMMISSION SCOUT (Cost: £500k)", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // CANDIDATES Recommendations
                    Text(
                        text = "Scout Recommendations (${state.scoutCandidates.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (state.scoutCandidates.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .background(CardBackground)
                                .border(BorderStroke(1.5.dp, BentoBorderColor), RoundedCornerShape(28.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "No active scouting prospects on report.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            state.scoutCandidates.forEach { candidate ->
                                ScoutCandidateCard(
                                    candidate = candidate,
                                    onSign = { viewModel.buyPlayer(candidate) },
                                    onLoan = { viewModel.loanInPlayer(candidate, 10) }
                                )
                            }
                        }
                    }
                } else {
                    // YOUTH SCOUT ACADEMY TAB
                    Text(
                        text = "Academy Youth Scouting Roster",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Hire dedicated youth recruiters, configure training focus targets, and dispatch them across regions. Discovered wonderkids can sign formal five-season contracts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // HIRED SCOUTS
                    val hiredList = state.youthScouts
                    if (hiredList.isEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, BentoBorderColor, RoundedCornerShape(24.dp)),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBackground)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "ACADEMY CURRENTLY VACANT",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "No youth recruiters are currently hired. Review the agency profiles below to secure a contract with scout agents.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            hiredList.forEach { scout ->
                                val region = selectedScoutRegions[scout.id] ?: "South America"
                                val posFocus = selectedScoutPositions[scout.id] ?: "ANY"

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.5.dp, BentoBorderColor, RoundedCornerShape(24.dp)),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                                ) {
                                    Column(modifier = Modifier.padding(18.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(38.dp)
                                                        .clip(CircleShape)
                                                        .background(PitchGreen),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(imageVector = Icons.Default.Badge, contentDescription = null, tint = PitchLime, modifier = Modifier.size(18.dp))
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = scout.name,
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        fontWeight = FontWeight.Black,
                                                        color = Color.White
                                                    )
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        repeat(5) { idx ->
                                                            Icon(
                                                                imageVector = Icons.Default.Star,
                                                                contentDescription = null,
                                                                tint = if (idx < scout.ratingStars) PitchLime else Color.Gray.copy(alpha = 0.5f),
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            IconButton(
                                                onClick = { viewModel.releaseYouthScout(scout.id) },
                                                modifier = Modifier.testTag("release_scout_${scout.id}")
                                            ) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Release Contract", tint = Color(0xFFE74C3C))
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Status badge details
                                        when (scout.status) {
                                            "IDLE" -> {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(PitchLime.copy(alpha = 0.15f))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text("STATUS: STANDBY (Ready for Assignment)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = PitchLime)
                                                }

                                                Spacer(modifier = Modifier.height(14.dp))

                                                // region selection chips
                                                Text("Target Scouting Region:", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                                                val regionsList = listOf("South America", "West Africa", "Western Europe", "East Asia", "Eastern Europe")
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 4.dp)
                                                        .verticalScroll(rememberScrollState(), enabled = false), // standard raw row
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    regionsList.forEach { r ->
                                                        val isSel = region == r
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(if (isSel) PitchGreen else Color(0xFF131514))
                                                                .border(1.dp, if (isSel) PitchLime else Color.Transparent, RoundedCornerShape(8.dp))
                                                                .clickable { selectedScoutRegions[scout.id] = r }
                                                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                                        ) {
                                                            Text(r, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isSel) PitchLime else Color.White)
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(8.dp))

                                                // focus position selection chips
                                                Text("Squad Quality Focus:", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                                                val focusList = listOf("ANY", "FWD", "MID", "DEF", "GK")
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    focusList.forEach { p ->
                                                        val isSel = posFocus == p
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(if (isSel) PitchGreen else Color(0xFF131514))
                                                                .border(1.dp, if (isSel) PitchLime else Color.Transparent, RoundedCornerShape(8.dp))
                                                                .clickable { selectedScoutPositions[scout.id] = p }
                                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                                        ) {
                                                            Text(p, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isSel) PitchLime else Color.White)
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(12.dp))

                                                Button(
                                                    onClick = { viewModel.sendYouthScoutOnMission(scout.id, region, posFocus) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = PitchLime, contentColor = Color(0xFF381E72)),
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(38.dp)
                                                        .testTag("dispatch_scout_${scout.id}")
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF381E72))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("LAUNCH SCOUTING EXPEDITION", fontWeight = FontWeight.Black, fontSize = 11.sp)
                                                    }
                                                }
                                            }
                                            "SEARCHING" -> {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(Color(0xFF3498DB).copy(alpha = 0.15f))
                                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Text("STATUS: SEARCHING", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF3498DB))
                                                    }
                                                    Text(
                                                        text = "${scout.remainingDays} Weeks Left",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Color.LightGray,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(10.dp))
                                                Text(
                                                    text = "Scout ${scout.name} is currently searching for a high potential ${scout.searchType} in ${scout.currentRegion}.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.Gray
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                LinearProgressIndicator(
                                                    progress = { (2f - scout.remainingDays.toFloat()) / 2f },
                                                    color = Color(0xFF3498DB),
                                                    trackColor = Color(0xFF1c1f1e),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(6.dp)
                                                        .clip(RoundedCornerShape(3.dp))
                                                )
                                            }
                                            "COOLDOWN" -> {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(Color.Gray.copy(alpha = 0.15f))
                                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Text("STATUS: COOLDOWN REST", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.LightGray)
                                                    }
                                                    Text(
                                                        text = "${scout.remainingDays} Weeks Left",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Color.Gray,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(10.dp))
                                                Text(
                                                    text = "Visa administrative processing and restful recovery active. Scout can accept new missions once complete.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.Gray
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                LinearProgressIndicator(
                                                    progress = { (2f - scout.remainingDays.toFloat()) / 2f },
                                                    color = Color.Gray,
                                                    trackColor = Color(0xFF1c1f1e),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(6.dp)
                                                        .clip(RoundedCornerShape(3.dp))
                                                )
                                            }
                                            "REPORT_READY" -> {
                                                val wk = scout.foundWonderkid
                                                if (wk != null) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(PitchLime.copy(alpha = 0.15f))
                                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                        ) {
                                                            Text("🏆 WONDERKID REPORT UNLOCKED", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = PitchLime)
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(12.dp))

                                                    // Asset dossier card
                                                    Card(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .border(1.dp, PitchLime.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                                                        shape = RoundedCornerShape(16.dp),
                                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF111413))
                                                    ) {
                                                        Column(modifier = Modifier.padding(14.dp)) {
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Column {
                                                                    Text(
                                                                        text = wk.name,
                                                                        style = MaterialTheme.typography.bodyLarge,
                                                                        fontWeight = FontWeight.Black,
                                                                        color = Color.White
                                                                    )
                                                                    Text(
                                                                        text = "${wk.age}y.o | Position: ${wk.position}",
                                                                        style = MaterialTheme.typography.labelSmall,
                                                                        color = Color.Gray
                                                                    )
                                                                }
                                                                Box(
                                                                    modifier = Modifier
                                                                        .clip(RoundedCornerShape(6.dp))
                                                                        .background(PitchGreen)
                                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                                ) {
                                                                    Text(
                                                                        text = "Val £${String.format("%.1f", wk.value.toDouble() / 1_000_000)}M",
                                                                        style = MaterialTheme.typography.labelSmall,
                                                                        fontWeight = FontWeight.ExtraBold,
                                                                        color = PitchLime
                                                                    )
                                                                }
                                                            }

                                                            Spacer(modifier = Modifier.height(8.dp))
                                                            Text(
                                                                text = wk.description,
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = PitchLime,
                                                                fontWeight = FontWeight.SemiBold
                                                            )

                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            Text(
                                                                text = "Attack rating: ${wk.ratingAttack} | Defend rating: ${wk.ratingDefend}\nWeekly contract wage request: £${String.format("%,d", wk.wage)}/wk",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = Color.Gray,
                                                                lineHeight = 14.sp
                                                            )

                                                            Spacer(modifier = Modifier.height(14.dp))

                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                            ) {
                                                                Button(
                                                                    onClick = { viewModel.rejectScoutReport(scout.id) },
                                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E2E31), contentColor = Color.White),
                                                                    shape = RoundedCornerShape(10.dp),
                                                                    modifier = Modifier
                                                                        .weight(1f)
                                                                        .height(34.dp)
                                                                        .testTag("reject_wk_${scout.id}"),
                                                                    contentPadding = PaddingValues(0.dp)
                                                                ) {
                                                                    Text("DISCARD REPORT", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                                                }

                                                                Button(
                                                                    onClick = { viewModel.signWonderkidToSquad(scout.id) },
                                                                    colors = ButtonDefaults.buttonColors(containerColor = PitchLime, contentColor = Color(0xFF381E72)),
                                                                    shape = RoundedCornerShape(10.dp),
                                                                    modifier = Modifier
                                                                        .weight(1.5f)
                                                                        .height(34.dp)
                                                                        .testTag("sign_wk_${scout.id}"),
                                                                    contentPadding = PaddingValues(0.dp)
                                                                ) {
                                                                    Text("✍️ SIGN WONDERKID", fontWeight = FontWeight.Black, fontSize = 10.sp)
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
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // RECRUITMENT MARKET
                    Text(
                        text = "Global Recruit Agency Exchange",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val recList = state.availableScoutsToHire
                    if (recList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(CardBackground)
                                .border(BorderStroke(1.5.dp, BentoBorderColor), RoundedCornerShape(24.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("All available scout profiles have been contacted or hired.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            recList.forEach { toHire ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, BentoBorderColor, RoundedCornerShape(20.dp)),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = toHire.name,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    repeat(5) { idx ->
                                                        Icon(
                                                            imageVector = Icons.Default.Star,
                                                            contentDescription = null,
                                                            tint = if (idx < toHire.ratingStars) PitchLime else Color.Gray.copy(alpha = 0.5f),
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Wage: £${String.format("%,d", toHire.weeklyWage)}/wk",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Color.Gray
                                                    )
                                                }
                                            }

                                            Button(
                                                onClick = { viewModel.hireYouthScout(toHire.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = PitchLime, contentColor = Color(0xFF381E72)),
                                                shape = RoundedCornerShape(10.dp),
                                                enabled = state.youthScouts.size < 3 && state.transferBudget >= toHire.hireFee,
                                                modifier = Modifier
                                                    .height(32.dp)
                                                    .testTag("hire_scout_${toHire.id}"),
                                                contentPadding = PaddingValues(horizontal = 12.dp)
                                            ) {
                                                Text(
                                                    text = "HIRE (£${String.format("%.1f", toHire.hireFee.toDouble() / 1_000_000)}M)",
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 10.sp
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
            "TRANSFERS" -> {
                // Listed Players from CPU teams
                val listedCpuPlayers = state.teams
                    .filter { !it.isUserControlled }
                    .flatMap { t -> t.roster.filter { it.isListed }.map { p -> Pair(t, p) } }

                Text(
                    text = "Players Listed on Market",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (listedCpuPlayers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(CardBackground)
                            .border(BorderStroke(1.5.dp, BentoBorderColor), RoundedCornerShape(28.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.TrendingUp, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "No listings on the transfer block currently.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        listedCpuPlayers.forEach { (team, player) ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = CardBackground),
                                border = BorderStroke(1.5.dp, BentoBorderColor)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(PitchGreen),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = player.position,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PitchLime
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = player.name,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = "${team.name} • Age ${player.age}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.LightGray
                                                )
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF2E4053))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "${player.overallRating} OVR",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // POTENTIAL METRICS SECTION
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0x1AFFFFFF))
                                                .padding(vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Current Pot: ${player.currentPotential} OVR",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = PitchLime,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0x1AFFFFFF))
                                                .padding(vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Future Pot: ${player.futurePotential} OVR",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF3498DB),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = Color.DarkGray)
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Asking Price",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.Gray
                                            )
                                            Text(
                                                text = "£${String.format("%.1f", player.askingPrice.toDouble() / 1_000_000)}M",
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = PitchLime
                                            )
                                        }

                                        Button(
                                            onClick = { showBidDialogForPlayer = Pair(player, team.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = PitchLime, contentColor = Color(0xFF381E72)),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.testTag("bid_btn_${player.id}")
                                        ) {
                                            Text("NEGOTIATE BID", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "INCOMING" -> {
                // User's Incoming offers
                val incomingBids = state.pendingBids.filter { !it.isFromUser && it.status == "PENDING" }

                Text(
                    text = "Bidding Offers Received",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (incomingBids.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(CardBackground)
                            .border(BorderStroke(1.5.dp, BentoBorderColor), RoundedCornerShape(28.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(imageVector = Icons.Default.MailOutline, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "No pending bids on your players.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "Go to the 'Squad' tab to list surplus players on the Transfer Market to invite AI offers.",
                                color = Color.DarkGray,
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        incomingBids.forEach { bid ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = CardBackground),
                                border = BorderStroke(1.5.dp, BentoBorderColor)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "FROM: ${bid.offeringTeamName.uppercase()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PitchLime,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = bid.playerName,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "${bid.playerPosition} • Rating: ${bid.playerRating}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.LightGray
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF381E72))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "£${String.format("%.1f", bid.amount.toDouble() / 1_000_000)}M",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))
                                    HorizontalDivider(color = Color.DarkGray)
                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.acceptUserIncomingBid(bid) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4D3E), contentColor = PitchLime),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("accept_bid_${bid.id}")
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("ACCEPT", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            }
                                        }

                                        Button(
                                            onClick = { viewModel.declineUserIncomingBid(bid.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A1521), contentColor = Color(0xFFFFDAD9)),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("decline_bid_${bid.id}")
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("DECLINE", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "SQUAD" -> {
                // User Team list with lists parameters
                val userTeam = state.teams.first { it.isUserControlled }

                Text(
                    text = "Sell & Renew Contract HQ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    userTeam.roster.sortedByDescending { it.overallRating }.forEach { player ->
                        val isLineup = player.id in userTeam.lineupIds
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            border = BorderStroke(1.5.dp, BentoBorderColor)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(if (isLineup) PitchGreen else Color.DarkGray),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = player.position,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isLineup) PitchLime else Color.LightGray
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = player.name,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                if (player.isListed) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(Color(0xFF8C1D18))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = "LISTED",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = Color.White,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 8.sp
                                                        )
                                                    }
                                                }
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "Rating: ${player.overallRating} • Age: ${player.age} • Pot: ${player.currentPotential}➔${player.futurePotential}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.Gray
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(if (player.contractYearsRemaining <= 1) Color(0xFF4A1521) else Color.Transparent)
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = "Contract: ${player.contractYearsRemaining}Y",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = if (player.contractYearsRemaining <= 1) Color(0xFFFFDAD9) else Color.Gray,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Text(
                                        text = "£${String.format("%.1f", player.value.toDouble() / 1_000_000)}M",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = Color.DarkGray)
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Renew Contract details first
                                    Button(
                                        onClick = { viewModel.renewContract(player.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E4053), contentColor = Color.White),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("renew_${player.id}")
                                    ) {
                                        Text("RENEW (Cost: 15% val)", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                    }

                                    if (player.isListed) {
                                        Button(
                                            onClick = { viewModel.delistPlayer(player.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8C1D18), contentColor = Color.White),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("delist_${player.id}")
                                        ) {
                                            Text("DELIST BLOCK", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Button(
                                            onClick = { showListDialogForPlayer = player },
                                            colors = ButtonDefaults.buttonColors(containerColor = PitchLime, contentColor = Color(0xFF381E72)),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("list_${player.id}"),
                                            enabled = !isLineup
                                        ) {
                                            Text(if (isLineup) "STARTING XI" else "LIST FOR SALE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
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

    // 1. PLACE BID DIALOG (on Cpu Player)
    if (showBidDialogForPlayer != null) {
        val (player, teamId) = showBidDialogForPlayer!!
        var bidAmountStr by remember { mutableStateOf((player.askingPrice.coerceAtLeast(100_000L)).toString()) }

        AlertDialog(
            onDismissRequest = { showBidDialogForPlayer = null },
            title = {
                Text(
                    text = "Negotiate Bid for ${player.name}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column {
                    Text(
                        text = "Valuation: £${String.format("%,d", player.value)}\nAsking Price: £${String.format("%,d", player.askingPrice)}\n\nEnter your official club bid below (in GBP):",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = bidAmountStr,
                        onValueChange = { bidAmountStr = it.filter { char -> char.isDigit() } },
                        label = { Text("Offer Price (£)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0x33FFFFFF),
                            unfocusedContainerColor = Color(0x1AFFFFFF)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bid_amount_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = bidAmountStr.toLongOrNull() ?: player.value
                        viewModel.bidOnCpuPlayer(player.id, teamId, amt)
                        showBidDialogForPlayer = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PitchLime, contentColor = Color(0xFF381E72))
                ) {
                    Text("SUBMIT OFFER", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBidDialogForPlayer = null }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = CardBackground,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // 2. LIST PLAYER DIALOG (on User Player)
    if (showListDialogForPlayer != null) {
        val player = showListDialogForPlayer!!
        var askingPriceStr by remember { mutableStateOf(player.value.toString()) }

        AlertDialog(
            onDismissRequest = { showListDialogForPlayer = null },
            title = {
                Text(
                    text = "List ${player.name} on Transfer Block",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column {
                    Text(
                        text = "Value: £${String.format("%,d", player.value)}\n\nEnter asking price to invite matching offers from competing AI clubs:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = askingPriceStr,
                        onValueChange = { askingPriceStr = it.filter { char -> char.isDigit() } },
                        label = { Text("Asking Price (£)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0x33FFFFFF),
                            unfocusedContainerColor = Color(0x1AFFFFFF)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("asking_price_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = askingPriceStr.toLongOrNull() ?: player.value
                        viewModel.listPlayerForSale(player.id, amt)
                        showListDialogForPlayer = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PitchLime, contentColor = Color(0xFF381E72))
                ) {
                    Text("PUBLISH LISTING", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showListDialogForPlayer = null }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = CardBackground,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun ScoutCandidateCard(
    candidate: ScoutCandidate,
    onSign: () -> Unit,
    onLoan: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("scout_candidate_${candidate.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.5.dp, BentoBorderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2E4053)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = candidate.position,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PitchLime
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = candidate.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Age: ${candidate.age} | ${candidate.description}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = PitchLime,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Potentials: Current ${candidate.currentPotential} ➔ Future ${candidate.futurePotential}",
                                style = MaterialTheme.typography.labelSmall,
                                color = PitchLime,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1B4D3E))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${candidate.overallRating} OVR",
                        style = MaterialTheme.typography.labelMedium,
                        color = PitchLime,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color.DarkGray)
            Spacer(modifier = Modifier.height(10.dp))

            // Stats attack vs defend
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Attacking Power", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    LinearProgressIndicator(
                        progress = { candidate.ratingAttack / 100f },
                        color = PitchLime,
                        trackColor = Color(0x33FFFFFF),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Defending Power", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    LinearProgressIndicator(
                        progress = { candidate.ratingDefend / 100f },
                        color = Color(0xFF3498DB),
                        trackColor = Color(0x33FFFFFF),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sign player terms
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Transfer Valuation",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "£${String.format("%,d", candidate.value)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (onLoan != null) {
                        Button(
                            onClick = onLoan,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PitchGreen,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("loan_player_${candidate.id}")
                        ) {
                            Text("LOAN IN", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Button(
                        onClick = onSign,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PitchLime,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("buy_player_${candidate.id}")
                    ) {
                        Text("SIGN CONTRACT", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

