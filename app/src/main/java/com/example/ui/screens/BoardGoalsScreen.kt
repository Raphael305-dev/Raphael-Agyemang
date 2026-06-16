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
import com.example.model.BoardGoal
import com.example.ui.components.*
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.Screen

@Composable
fun BoardGoalsScreen(viewModel: GameViewModel) {
    val state by viewModel.gameState.collectAsState()
    val game = state ?: return

    val userTeam = game.teams.first { it.isUserControlled }
    val confidence = userTeam.boardConfidence

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchDark)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
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
                        .testTag("boardgoals_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PitchLime
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CORPORATE SUITE",
                        style = MaterialTheme.typography.labelSmall,
                        color = PitchLime,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Board of Directors Directives",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Wallet/Budget Header
            TransactionTitleBar(
                title = "Club Reserves treasury",
                budget = game.transferBudget,
                modifier = Modifier.testTag("boardgoals_budget")
            )

            // Boardroom Confidence Indicator Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, BentoBorderColor, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = PitchLime,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Board Trust & Approval",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        val statusWord = when {
                            confidence >= 80 -> "EXCELLENT"
                            confidence >= 55 -> "STABLE"
                            else -> "CRITICAL THREAT"
                        }
                        val statusColor = when {
                            confidence >= 80 -> Color(0xFF2ECC71)
                            confidence >= 55 -> Color(0xFFF1C40F)
                            else -> Color(0xFFE74C3C)
                        }

                        CustomStatusBadge(
                            text = statusWord,
                            containerColor = statusColor.copy(alpha = 0.2f),
                            textColor = statusColor
                        )
                    }

                    ProgressBarWithLabel(
                        label = "Job Security confidence rating",
                        progress = confidence.toFloat() / 100f,
                        activeColor = if (confidence >= 55) PitchLime else Color(0xFFE74C3C),
                        modifier = Modifier.testTag("board_confidence_bar")
                    )

                    Text(
                        text = "Directors review your performance at the end of each season. Dropping below 50% trust can result in contract termination and immediate sacking! Satisfy active directives to secure your tenure.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        lineHeight = 16.sp
                    )
                }
            }

            // Dividers
            Text(
                text = "ACTIVE DIRECTIVES",
                style = MaterialTheme.typography.labelSmall,
                color = PitchLime,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            // Directives list
            val activeGoals = game.activeBoardGoals
            if (activeGoals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Boardroom Targets assigned for this campaign.",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                activeGoals.forEach { goal ->
                    BoardGoalCard(goal = goal)
                }
            }
        }
    }
}

@Composable
fun BoardGoalCard(goal: BoardGoal) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, BentoBorderColor, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Title and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = goal.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Status Badge
                val (badgeText, badgeContainer, badgeTextCol) = when {
                    goal.completed -> Triple("ACCOMPLISHED", Color(0xFF142F1C), Color(0xFF2ECC71))
                    goal.failed -> Triple("FAILED", Color(0xFF2D1418), Color(0xFFE74C3C))
                    else -> Triple("ACTIVE", Color(0xFF1A2B3C), PitchLime)
                }

                CustomStatusBadge(
                    text = badgeText,
                    containerColor = badgeContainer,
                    textColor = badgeTextCol
                )
            }

            HorizontalDivider(color = BentoBorderColor, thickness = 1.dp)

            // Progress Indicators
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Directive Milestone Progress",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = getProgressLabel(goal),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                val fraction = (goal.currentValue.toFloat() / goal.targetValue.toFloat()).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (goal.completed) Color(0xFF2ECC71) else if (goal.failed) Color(0xFFE74C3C) else PitchLime,
                    trackColor = Color(0x1AFFFFFF)
                )
            }

            HorizontalDivider(color = BentoBorderColor, thickness = 1.dp)

            // Payout Inflows and Penalties
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reward Block
                Column {
                    Text(
                        text = "OUTCOME REWARDS",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "+£${String.format("%,d", goal.financialReward)} Budget",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2ECC71)
                    )
                    Text(
                        text = "+${goal.confidenceReward}% Board Trust",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )
                }

                // Deadline/Penalty Block
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "MATCHTIME DEADLINE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Match Day ${goal.deadlineMatchDay}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "-${goal.confidencePenalty}% Trust penalty",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE74C3C)
                    )
                }
            }
        }
    }
}

private fun getProgressLabel(goal: BoardGoal): String {
    return when (goal.targetType) {
        "STADIUM_UPGRADE" -> "Upgrades: ${goal.currentValue} / ${goal.targetValue}"
        "FACILITY_UPGRADE" -> "Upgrades: ${goal.currentValue} / ${goal.targetValue}"
        "SIGN_PLAYER" -> "Sign Player: ${goal.currentValue} OVR / ${goal.targetValue} OVR"
        "WINS" -> "Wins Count: ${goal.currentValue} / ${goal.targetValue}"
        "BALANCE" -> "Reserves: £${goal.currentValue}M / £${goal.targetValue}M"
        else -> "Progress: ${goal.currentValue} / ${goal.targetValue}"
    }
}
