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
import com.example.model.StaffMember
import com.example.ui.components.*
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.Screen

@Composable
fun StaffScreen(viewModel: GameViewModel) {
    val state by viewModel.gameState.collectAsState()
    val game = state ?: return

    var selectedTab by remember { mutableStateOf(0) } // 0 = Hired Staff, 1 = Recruitment Pool

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
                        .testTag("staff_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PitchLime
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "FRONT OFFICE HQ",
                        style = MaterialTheme.typography.labelSmall,
                        color = PitchLime,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Staff & Personnel Hub",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Wallet treasury check
            TransactionTitleBar(
                title = "Club Capital Portfolio",
                budget = game.transferBudget,
                modifier = Modifier.testTag("staff_budget")
            )

            // Custom Tab Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardBackground)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("ACTIVE ROSTER (${game.hiredStaff.size}/3)", "RECRUIT CANDIDATES (${game.availableStaffToHire.size})").forEachIndexed { index, title ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedTab == index) PitchDark else Color.Transparent)
                            .clickable { selectedTab = index }
                            .padding(vertical = 12.dp)
                            .testTag("staff_tab_$index"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == index) PitchLime else Color.Gray
                        )
                    }
                }
            }

            // Content scroll panel
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                val staffToShow = if (selectedTab == 0) game.hiredStaff else game.availableStaffToHire

                if (staffToShow.isEmpty()) {
                    // Friendly Empty State
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Default.Groups else Icons.Default.SupervisorAccount,
                            contentDescription = "Empty",
                            tint = Color.DarkGray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (selectedTab == 0) "No Active Head Personnel" else "No Candidates in Market",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (selectedTab == 0) "Hire professional Coaches, Physios, or Scouts in the Recruitment pool to unlock automatic squad bonuses." else "All elite staff candidates have current assignments. Check back next week.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(staffToShow.size) { index ->
                            val staff = staffToShow[index]
                            StaffCard(
                                staff = staff,
                                isHired = selectedTab == 0,
                                onAction = {
                                    if (selectedTab == 0) {
                                        viewModel.releaseStaffMember(staff.id)
                                    } else {
                                        viewModel.hireStaffMember(staff.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StaffCard(
    staff: StaffMember,
    isHired: Boolean,
    onAction: () -> Unit
) {
    val roleColor = when (staff.role) {
        "COACH" -> PitchLime
        "PHYSIO" -> Color(0xFF64B5F6)
        "SCOUT" -> Color(0xFFFFB74D)
        else -> Color.White
    }

    val roleIcon = when (staff.role) {
        "COACH" -> Icons.Default.Sports
        "PHYSIO" -> Icons.Default.MedicalServices
        "SCOUT" -> Icons.Default.Search
        else -> Icons.Default.Person
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, BentoBorderColor), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Role badge and Stars rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(roleColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = roleIcon,
                        contentDescription = staff.role,
                        tint = roleColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "HEAD ${staff.role}",
                        style = MaterialTheme.typography.labelSmall,
                        color = roleColor,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                // Stars rating
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(5) { starIdx ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (starIdx < staff.ratingStars) Color(0xFFFFD54F) else Color.DarkGray,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Staff Details Column
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = staff.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${staff.age} yrs",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.LightGray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = staff.specialty,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }

            // Description text
            Text(
                text = staff.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray,
                lineHeight = 16.sp
            )

            // Dynamic glow perk banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(roleColor.copy(alpha = 0.08f))
                    .border(BorderStroke(1.dp, roleColor.copy(alpha = 0.25f)), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.OfflineBolt,
                        contentDescription = "Bonus Efficiency",
                        tint = roleColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = "OFFICIAL PERSONNEL EFFECT:",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = staff.effectDescription,
                            style = MaterialTheme.typography.bodySmall,
                            color = roleColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Divider(color = Color.DarkGray.copy(alpha = 0.5f))

            // Footer Row: Fees details & Hire/Fire Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Costs column
                Column {
                    if (!isHired) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Contract Fee:",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                            Text(
                                text = "£${String.format("%,d", staff.cost / 1000)}k",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Weekly Wage:",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Text(
                            text = "£${String.format("%,d", staff.weeklyWage)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Call to tactical/management action
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isHired) Color(0xFFEF5350) else roleColor,
                        contentColor = if (isHired) Color.White else PitchDark
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("staff_action_${staff.id}")
                ) {
                    Text(
                        text = if (isHired) "RELEASE CONTRACT" else "HIRE PERSONNEL",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
