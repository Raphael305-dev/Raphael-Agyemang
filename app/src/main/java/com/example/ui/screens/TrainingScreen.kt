package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.model.Player
import com.example.ui.components.*
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.Screen

@Composable
fun TrainingScreen(viewModel: GameViewModel) {
    val state by viewModel.gameState.collectAsState()
    val game = state ?: return
    val userTeam = game.teams.first { it.isUserControlled }

    var selectedTab by remember { mutableStateOf(0) } // 0 = Squad Training, 1 = Legend Transitions

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
            // Header with Back Button
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
                        .border(1.dp, BentoBorderColor, CircleShape)
                        .testTag("training_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to dashboard",
                        tint = PitchLime
                    )
                }
                Column {
                    Text(
                        text = "TRAINING ACADEMY",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${userTeam.name} Training Facility • Level ${userTeam.trainingLevel}",
                        style = MaterialTheme.typography.labelSmall,
                        color = PitchLime
                    )
                }
            }

            // Tabs Panel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { selectedTab = 0 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == 0) PitchLime else CardBackground,
                        contentColor = if (selectedTab == 0) Color.Black else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(45.dp)
                        .border(1.5.dp, if (selectedTab == 0) Color.Transparent else BentoBorderColor, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Weekly Drills", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = { selectedTab = 1 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == 1) PitchLime else CardBackground,
                        contentColor = if (selectedTab == 1) Color.Black else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(45.dp)
                        .border(1.5.dp, if (selectedTab == 1) Color.Transparent else BentoBorderColor, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Retirement Transitions", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            when (selectedTab) {
                0 -> {
                    WeeklyDrillsTab(viewModel = viewModel, userTeam = userTeam, isDone = game.isTrainingConductedThisWeek)
                }
                1 -> {
                    LegendTransitionsTab(viewModel = viewModel, team = userTeam)
                }
            }
        }
    }
}

@Composable
fun WeeklyDrillsTab(viewModel: GameViewModel, userTeam: com.example.model.Team, isDone: Boolean) {
    var selectedFocus by remember { mutableStateOf("ATTACK") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Status Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = if (isDone) Color(0xFF1B2F22) else Color(0xFF2E2413)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, if (isDone) PitchLime.copy(alpha = 0.5f) else Color(0xFFFF9800).copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.Info,
                    contentDescription = null,
                    tint = if (isDone) PitchLime else Color(0xFFFF9800),
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = if (isDone) "Weekly Training Wrapped Up!" else "Session Awaiting Preparation",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (isDone) "All players ran targeted conditioning drills. Ready for the next fixture matches!" else "Launch one coaching program this week to increase player metrics.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )
                }
            }
        }

        // Drills Selection Grid (Scrollable list of customizable options)
        Text(
            text = "Select Focus Program",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )

        val drills = listOf(
            DrillOption("ATTACK", "Attacking Drills & Finishes", "FWD & MID gain +1 Attack rating.", "-6 Stamina, +8 Fatigue", Icons.Default.SportsSoccer, Color(0xFF3498DB)),
            DrillOption("DEFENSE", "Intense Tactical Low-Block", "DEF & GK gain +1 Defense rating.", "-6 Stamina, +8 Fatigue", Icons.Default.Shield, Color(0xFFE74C3C)),
            DrillOption("FITNESS", "Aerobic Recovery & Wellness", "Restores +20 Stamina, reduces -15 Fatigue.", "+3 Morale booster", Icons.Default.Favorite, PitchLime),
            DrillOption("TACTICAL", "Squad Tiki-Taka Integration", "Morale boost +10, overall alignment.", "-3 Stamina, +4 Fatigue", Icons.Default.QueryStats, Color(0xFF9B59B6))
        )

        Row(
            modifier = Modifier.fillMaxWidth().height(160.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            drills.take(2).forEach { drill ->
                DrillCard(drill = drill, isSelected = selectedFocus == drill.id, isDone = isDone) {
                    selectedFocus = drill.id
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().height(160.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            drills.drop(2).forEach { drill ->
                DrillCard(drill = drill, isSelected = selectedFocus == drill.id, isDone = isDone) {
                    selectedFocus = drill.id
                }
            }
        }

        // Run Training Button
        Button(
            onClick = { viewModel.conductWeeklyTraining(selectedFocus) },
            enabled = !isDone,
            colors = ButtonDefaults.buttonColors(
                containerColor = PitchLime,
                contentColor = Color.Black,
                disabledContainerColor = Color.DarkGray,
                disabledContentColor = Color.Gray
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("run_training_btn")
        ) {
            Text(
                text = if (isDone) "TRAINING DRILLS COMPLETED" else "CONDUCT TACTICAL PREPARATION",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Squad Energy Breakdown
        Text(
            text = "Squad Physiological Energy",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(userTeam.roster) { player ->
                TrainingPlayerRow(player = player)
            }
        }
    }
}

class DrillOption(
    val id: String,
    val title: String,
    val benefit: String,
    val cost: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val tint: Color
)

@Composable
fun RowScope.DrillCard(drill: DrillOption, isSelected: Boolean, isDone: Boolean, onSelect: () -> Unit) {
    Card(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .border(
                2.dp,
                if (isSelected) PitchLime else if (isDone) Color.Transparent else BentoBorderColor,
                RoundedCornerShape(16.dp)
            )
            .clickable(enabled = !isDone) { onSelect() },
        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFF1E2822) else CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = drill.icon,
                    contentDescription = null,
                    tint = drill.tint,
                    modifier = Modifier.size(24.dp)
                )
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.RadioButtonChecked,
                        contentDescription = "Selected",
                        tint = PitchLime,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = drill.title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = drill.benefit,
                    style = MaterialTheme.typography.labelSmall,
                    color = PitchLime,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = drill.cost,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
fun TrainingPlayerRow(player: Player) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, BentoBorderColor, RoundedCornerShape(12.dp))
            .background(CardBackground)
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1.2f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = player.position,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = PitchLime,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0x2239FF14))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
                Text(
                    text = player.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Text(
                text = "OVR: ${player.overallRating} • Age: ${player.age}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }

        // Physical indicators
        Row(
            modifier = Modifier.weight(1.8f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Stamina", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 9.sp)
                    Text("${player.stamina}%", style = MaterialTheme.typography.labelSmall, color = if (player.stamina < 70) Color.Red else Color.White, fontSize = 9.sp)
                }
                LinearProgressIndicator(
                    progress = { player.stamina / 100f },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                    color = if (player.stamina < 70) Color.Red else PitchLime,
                    trackColor = Color(0x11FFFFFF)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Fatigue", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 9.sp)
                    Text("${player.fatigue}%", style = MaterialTheme.typography.labelSmall, color = if (player.fatigue > 50) Color.Red else Color.White, fontSize = 9.sp)
                }
                LinearProgressIndicator(
                    progress = { player.fatigue / 100f },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                    color = if (player.fatigue > 50) Color.Red else Color(0xFF3498DB),
                    trackColor = Color(0x11FFFFFF)
                )
            }
        }
    }
}

@Composable
fun LegendTransitionsTab(viewModel: GameViewModel, team: com.example.model.Team) {
    val retiredLegends = team.roster.filter { it.age >= 33 }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF142F3C)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF3498DB).copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFF3498DB),
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "Legendary Career Academy",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Squad members aged 33+ are preparing for technical retirement. Sign them as immediate Backroom Staff!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )
                }
            }
        }

        Text(
            text = "Eligible Veterans for Recruitment",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )

        if (retiredLegends.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, BentoBorderColor, RoundedCornerShape(20.dp))
                    .background(CardBackground),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Groups, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                    Text(
                        text = "No eligible veteran legends (33+ years old) in your current active team squad yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(retiredLegends) { p ->
                    VeteranLegendCard(player = p) { role ->
                        viewModel.convertRetiredPlayerToStaff(team.id, p.id, role)
                    }
                }
            }
        }
    }
}

@Composable
fun VeteranLegendCard(player: Player, onConvert: (String) -> Unit) {
    var expandedRoleOpt by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BentoBorderColor, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = player.position,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PitchLime,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x2239FF14))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        Text(
                            text = player.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "Age: ${player.age} Years • Rating OVR: ${player.overallRating}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )
                }

                Button(
                    onClick = { expandedRoleOpt = !expandedRoleOpt },
                    colors = ButtonDefaults.buttonColors(containerColor = PitchLime, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("SIGN AS STAFF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Icon(imageVector = if (expandedRoleOpt) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                }
            }

            AnimatedVisibility(visible = expandedRoleOpt) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.3f))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "CHOOSE NEW TECHNICAL ROLE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { onConvert("COACH") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E2721), contentColor = PitchLime),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Coach", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = { onConvert("PHYSIO") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E2721), contentColor = PitchLime),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Physio", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = { onConvert("SCOUT") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E2721), contentColor = PitchLime),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Scout", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text(
                        text = "Sign on wage: £${player.contractWage / 3}/week contract (approx 66% lower than professional players' active play contract rates)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.LightGray,
                        fontSize = 8.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
