package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PressConferenceScreen(viewModel: GameViewModel) {
    val state by viewModel.gameState.collectAsState()
    val save = state ?: return
    val pc = save.activePressConference ?: return

    val currentQ = pc.questions.getOrNull(pc.currentQuestionIndex)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Upper Title Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.5.dp, BentoBorderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("press_header_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .background(PitchGreen, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "🎤 LIVE PRESS BRIEFING",
                            color = PitchLime,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = pc.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = pc.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress tracker indicators
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        pc.questions.forEachIndexed { idx, _ ->
                            val isPast = idx < pc.currentQuestionIndex
                            val isCurrent = idx == pc.currentQuestionIndex
                            val color = when {
                                isPast -> PitchLime
                                isCurrent -> Color.White
                                else -> Color.Gray.copy(alpha = 0.3f)
                            }
                            val scaleWidth = if (isCurrent) 24.dp else 12.dp
                            Box(
                                modifier = Modifier
                                    .size(width = scaleWidth, height = 6.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                        }
                    }
                }
            }

            // Journalist Prompt Area
            if (currentQ != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground.copy(alpha = 0.9f)),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.5.dp, PitchLime.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("press_journalist_prompt")
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(PitchGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = "Journalist",
                                tint = PitchLime,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "REPORTER QUESTION",
                                style = MaterialTheme.typography.labelSmall,
                                color = PitchLime,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )

                            Text(
                                text = "\"${currentQ.text}\"",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "SELECT YOUR RESPONSE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.align(Alignment.Start)
                )

                // Render options
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    currentQ.options.forEachIndexed { index, option ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF232225)),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, BentoBorderColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectPressConferenceOption(index)
                                }
                                .testTag("press_option_$index")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(PitchGreen),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = when (index) {
                                                0 -> "A"
                                                1 -> "B"
                                                else -> "C"
                                            },
                                            color = PitchLime,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp
                                        )
                                    }

                                    Text(
                                        text = option.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Predicted Impact
                                HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f), thickness = 1.dp)

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ImpactPill(
                                        label = "Morale",
                                        impact = option.teamMoraleImpact
                                    )

                                    ImpactPill(
                                        label = "Board Confidence",
                                        impact = option.boardConfidenceImpact
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Completed State
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = PitchLime,
                            modifier = Modifier.size(54.dp)
                        )

                        Text(
                            text = "Briefing Complete!",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "You have successfully finished field and boardroom analyses. Good luck with the next matchday!",
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun ImpactPill(label: String, impact: Int) {
    val isPositive = impact >= 0
    val isZero = impact == 0
    val colorText = when {
        isZero -> Color.Gray
        isPositive -> Color(0xFF81C784) // green
        else -> Color(0xFFE57373) // red
    }
    val backgroundPill = when {
        isZero -> Color.Gray.copy(alpha = 0.15f)
        isPositive -> Color(0xFF81C784).copy(alpha = 0.12f)
        else -> Color(0xFFE57373).copy(alpha = 0.12f)
    }
    val iconSign = when {
        isZero -> "•"
        isPositive -> "+$impact%"
        else -> "$impact%"
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundPill)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )

            Text(
                text = iconSign,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = colorText
            )
        }
    }
}
