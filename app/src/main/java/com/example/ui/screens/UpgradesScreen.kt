package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.model.Team
import com.example.ui.components.*
import com.example.viewmodel.GameViewModel

@Composable
fun UpgradesScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsState()
    val userTeamId by viewModel.userTeamIdVal.collectAsState()

    val state = gameState ?: return
    val userTeam = state.teams.firstOrNull { it.id == userTeamId } ?: return

    var activeTab by remember { mutableIntStateOf(0) } // 0 = Facilities, 1 = Stadium

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PitchDark)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Welcome and Budget card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BentoBorderColor, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "UPGRADES & INFRASTRUCTURE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = PitchLime,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = userTeam.stadiumName.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "REMAINING BUDGET",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "£${String.format("%.2f", state.transferBudget.toDouble() / 1_000_000)}M",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = PitchLime
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // TAB SWITCHER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF151817))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (activeTab == 0) PitchGreen else Color.Transparent)
                    .clickable { activeTab = 0 }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        tint = if (activeTab == 0) PitchLime else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "FACILITIES",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (activeTab == 0) Color.White else Color.Gray
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (activeTab == 1) PitchGreen else Color.Transparent)
                    .clickable { activeTab = 1 }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SportsSoccer,
                        contentDescription = null,
                        tint = if (activeTab == 1) PitchLime else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "STADIUM HUB",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (activeTab == 1) Color.White else Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (activeTab == 0) {
            // Facilities Tab
            Text(
                text = "CAMPUS FACILITIES",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Medical Facility Card
            FacilityUpgradeRow(
                title = "Medical Clinic HQ",
                description = "Speeds up squad physical recovery and reduces matchday injury hazard chances.",
                level = userTeam.medicalLevel,
                icon = Icons.Default.MedicalServices,
                accentColor = Color(0xFFF1C40F),
                onUpgrade = { viewModel.upgradeFacility("MEDICAL") },
                upgradeCost = getUpgradeCost("MEDICAL", userTeam.medicalLevel)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Youth Academy Card
            FacilityUpgradeRow(
                title = "Junior Youth Academy",
                description = "Nurtures younger recruits. Guarantees high rating graduates during pre-season academy intake.",
                level = userTeam.academyLevel,
                icon = Icons.Default.School,
                accentColor = Color(0xFF2ECC71),
                onUpgrade = { viewModel.upgradeFacility("ACADEMY") },
                upgradeCost = getUpgradeCost("ACADEMY", userTeam.academyLevel)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Club Megastore Card
            FacilityUpgradeRow(
                title = "Megastore & Merchandise",
                description = "Sells official apparel on matchdays, securing high passive trade revenue.",
                level = userTeam.clubStoreLevel,
                icon = Icons.Default.Storefront,
                accentColor = Color(0xFFE74C3C),
                onUpgrade = { viewModel.upgradeFacility("STORE") },
                upgradeCost = getUpgradeCost("STORE", userTeam.clubStoreLevel)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Advanced Training Center Card
            FacilityUpgradeRow(
                title = "Advanced Training Center",
                description = "Enhances squad training gains and boosts resting stamina recuperation.",
                level = userTeam.trainingLevel,
                icon = Icons.Default.FitnessCenter,
                accentColor = Color(0xFF3498DB),
                onUpgrade = { viewModel.upgradeFacility("TRAINING") },
                upgradeCost = getUpgradeCost("TRAINING", userTeam.trainingLevel)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Global Scouting Network Card
            FacilityUpgradeRow(
                title = "Global Scouting Hub",
                description = "Unlocks extra scout listing slots. Restructures the market toward world-class talent profiles.",
                level = userTeam.scoutingLevel,
                icon = Icons.Default.Search,
                accentColor = Color(0xFF9B59B6),
                onUpgrade = { viewModel.upgradeFacility("SCOUTING") },
                upgradeCost = getUpgradeCost("SCOUTING", userTeam.scoutingLevel)
            )
        } else {
            // Stadium Tab
            Text(
                text = "STADIUM INFRASTRUCTURE",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BentoBorderColor, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = PitchLime,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Stadium capacity",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF151817))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = String.format("%,d seats", userTeam.stadiumCapacity),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Black,
                                color = PitchLime
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Expanding stadium seating permits more ticket transactions on home matches. Build high tier stands to welcome additional supporters.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.upgradeStadiumCapacity() },
                        colors = ButtonDefaults.buttonColors(containerColor = PitchGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddHomeWork,
                            contentDescription = null,
                            tint = PitchLime,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Expand +5,000 Seats (£5.0M)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TICKET PRICE MANAGEMENT CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BentoBorderColor, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ConfirmationNumber,
                                contentDescription = null,
                                tint = PitchLime,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ticket Pricing",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF151817))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "£${userTeam.stadiumTicketPrice}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Black,
                                color = PitchLime
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Supporters are willing to buy more expensive seats if match ratings and satisfaction are high. Excessive prices drop fan turnouts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.adjustTicketPrice(userTeam.stadiumTicketPrice - 5) },
                            enabled = userTeam.stadiumTicketPrice > 10,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2B2F)),
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Decrease price (-£5)", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { viewModel.adjustTicketPrice(userTeam.stadiumTicketPrice + 5) },
                            enabled = userTeam.stadiumTicketPrice < 100,
                            colors = ButtonDefaults.buttonColors(containerColor = PitchGreen),
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Increase price (+£5)", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FacilityUpgradeRow(
    title: String,
    description: String,
    level: Int,
    icon: ImageVector,
    accentColor: Color,
    onUpgrade: () -> Unit,
    upgradeCost: Long?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BentoBorderColor, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "LEVEL $level OF 5",
                                style = MaterialTheme.typography.labelSmall,
                                color = accentColor,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Level dots
                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                for (i in 1..5) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(if (i <= level) accentColor else Color.DarkGray)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (level < 5) {
                val displayCost = if (upgradeCost != null) "£${String.format("%.1f", upgradeCost.toDouble() / 1_000_000)}M" else "Free"
                Button(
                    onClick = onUpgrade,
                    colors = ButtonDefaults.buttonColors(containerColor = PitchGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Upgrade,
                        contentDescription = "Upgrade",
                        tint = PitchLime,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Upgrade Facility to level ${level + 1} ($displayCost)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E2120))
                        .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = "Maxed out",
                            tint = Color(0xFFF1C40F),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "MAX LEVEL REACHED (Lv. 5)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF1C40F)
                        )
                    }
                }
            }
        }
    }
}

private fun getUpgradeCost(type: String, currentLevel: Int): Long {
    return when (type) {
        "MEDICAL", "ACADEMY" -> when (currentLevel) {
            1 -> 3_000_000L
            2 -> 6_000_000L
            3 -> 12_000_000L
            else -> 20_000_000L
        }
        "STORE" -> when (currentLevel) {
            1 -> 2_000_000L
            2 -> 4_000_000L
            3 -> 8_000_000L
            else -> 15_000_000L
        }
        else -> when (currentLevel) { // TRAINING, SCOUTING
            1 -> 2_500_000L
            2 -> 5_000_000L
            3 -> 10_000_000L
            else -> 18_000_000L
        }
    }
}
