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
import com.example.model.Sponsorship
import com.example.ui.components.*
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.Screen

@Composable
fun SponsorshipScreen(viewModel: GameViewModel) {
    val state by viewModel.gameState.collectAsState()
    val game = state ?: return

    var selectedTab by remember { mutableStateOf(0) } // 0 = Active Deals, 1 = Brand Proposals

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
                        .testTag("sponsorships_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PitchLime
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "EXECUTIVE DECK",
                        style = MaterialTheme.typography.labelSmall,
                        color = PitchLime,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Commercial Sponsorships",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Wallet/Budget Header
            TransactionTitleBar(
                title = "Treasury Accounts",
                budget = game.transferBudget,
                modifier = Modifier.testTag("sponsorship_budget")
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
                listOf("ACTIVE CONTRACTS", "SPONSOR PROPOSALS").forEachIndexed { index, title ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedTab == index) PitchGreen else Color.Transparent)
                            .clickable { selectedTab = index }
                            .padding(vertical = 12.dp, horizontal = 8.dp)
                            .testTag("sponsorship_tab_$index"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selectedTab == index) PitchLime else Color.LightGray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Scrollable List container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (selectedTab == 0) {
                    ActiveDealsList(sponsorships = game.activeSponsorships)
                } else {
                    ProposalsList(
                        offers = game.availableSponsorshipOffers,
                        onSign = { viewModel.signSponsorship(it) },
                        onNegotiate = { viewModel.negotiateSponsorship(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveDealsList(sponsorships: List<Sponsorship>) {
    if (sponsorships.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Handshake,
                contentDescription = null,
                tint = BentoBorderColor,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Active Sponsors Signed",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Your kit, sleeve, stadium, and main chest branding slots are completely open! Shift to the proposed brand list to negotiate cash injections.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            sponsorships.forEach { sponsor ->
                ActiveSponsorCard(sponsor = sponsor)
            }
        }
    }
}

@Composable
fun ActiveSponsorCard(sponsor: Sponsorship) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, BentoBorderColor, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Brand Name & Category Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = getBrandIcon(sponsor.category),
                        contentDescription = null,
                        tint = PitchLime,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = sponsor.brandName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                CustomStatusBadge(
                    text = sponsor.category.uppercase(),
                    containerColor = PitchGreen,
                    textColor = PitchLime
                )
            }

            HorizontalDivider(color = BentoBorderColor, thickness = 1.dp)

            // financial payouts details
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StatDetailRow(
                    label = "Base Matchday Payment",
                    value = "£${String.format("%,d", sponsor.baseWeeklyPayout)}/wk",
                    icon = Icons.Default.AttachMoney,
                    highlight = true
                )

                if (sponsor.winBonus > 0) {
                    StatDetailRow(
                        label = "Tactical Match Win Incentive",
                        value = "+£${String.format("%,d", sponsor.winBonus)}/win",
                        icon = Icons.Default.EmojiEvents
                    )
                }

                if (sponsor.attendanceBonus > 0) {
                    StatDetailRow(
                        label = "Supporter Crowd Threshold Goal",
                        value = "+£${String.format("%,d", sponsor.attendanceBonus)} (if ≥ ${String.format("%,d", sponsor.attendanceThreshold)} fans)",
                        icon = Icons.Default.Groups
                    )
                }

                if (sponsor.standingGoalOrdinal > 0) {
                    StatDetailRow(
                        label = "End-Of-Season League Finish Goal",
                        value = "£${String.format("%,d", sponsor.standingBonus)} (Top ${sponsor.standingGoalOrdinal})",
                        icon = Icons.Default.Leaderboard
                    )
                }
            }

            HorizontalDivider(color = BentoBorderColor, thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Remaining Duration",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
                Text(
                    text = "${sponsor.seasonsRemaining} Seasons",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun ProposalsList(
    offers: List<Sponsorship>,
    onSign: (String) -> Unit,
    onNegotiate: (String) -> Unit
) {
    if (offers.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.SpeakerNotesOff,
                contentDescription = null,
                tint = BentoBorderColor,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Proposals Available",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Firms are currently analyzing current standings. Play more season matches to spark dynamic bidding wars from top-tier brands!",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            offers.forEach { offer ->
                ProposalOfferCard(
                    offer = offer,
                    onSign = onSign,
                    onNegotiate = onNegotiate
                )
            }
        }
    }
}

@Composable
fun ProposalOfferCard(
    offer: Sponsorship,
    onSign: (String) -> Unit,
    onNegotiate: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, BentoBorderColor, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = PitchDark),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = offer.brandName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Season contract term: ${offer.seasonsRemaining} yrs",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                CustomStatusBadge(
                    text = offer.category.uppercase(),
                    containerColor = CardBackground,
                    textColor = PitchLime
                )
            }

            HorizontalDivider(color = BentoBorderColor, thickness = 1.dp)

            // Financial details
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                StatDetailRow(
                    label = "Base Week Wage",
                    value = "£${String.format("%,d", offer.baseWeeklyPayout)}/wk",
                    icon = Icons.Default.AttachMoney,
                    highlight = true
                )

                StatDetailRow(
                    label = "Win Incentive",
                    value = "£${String.format("%,d", offer.winBonus)}/win",
                    icon = Icons.Default.EmojiEvents
                )

                if (offer.attendanceThreshold > 0) {
                    StatDetailRow(
                        label = "Attendance Target",
                        value = "£${String.format("%,d", offer.attendanceBonus)} if ≥ ${String.format("%,d", offer.attendanceThreshold)}",
                        icon = Icons.Default.Groups
                    )
                }
            }

            HorizontalDivider(color = BentoBorderColor, thickness = 1.dp)

            // Negotiation Controls status info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "NEGOTIATION RISK",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val riskColor = when {
                            offer.negotiationRisk >= 60 -> Color(0xFFE74C3C)
                            offer.negotiationRisk >= 30 -> Color(0xFFF1C40F)
                            else -> Color(0xFF2ECC71)
                        }
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(riskColor)
                        )
                        Text(
                            text = "${offer.negotiationRisk}% Walkaway Chance",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = riskColor
                        )
                    }
                }

                Text(
                    text = "Counters: ${offer.counterCount}/3",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold
                )
            }

            // Buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // If counters full, disable negotiation
                val canNegotiate = offer.counterCount < 3
                FilledTonalButton(
                    onClick = { onNegotiate(offer.id) },
                    enabled = canNegotiate,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = PitchGreen,
                        contentColor = PitchLime,
                        disabledContainerColor = Color.DarkGray,
                        disabledContentColor = Color.Gray
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("negotiate_${offer.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Counter Offer")
                }

                Button(
                    onClick = { onSign(offer.id) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PitchLime,
                        contentColor = PitchDark
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sign_${offer.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.BorderColor,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sign Deal", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

fun getBrandIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "kit" -> Icons.Default.Checkroom
        "sleeve" -> Icons.Default.LocalOffer
        "stadium" -> Icons.Default.LocationOn
        else -> Icons.Default.SportsSoccer
    }
}
