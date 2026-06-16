package com.example.ui.screens

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
import com.example.ui.components.*
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.Screen

@Composable
fun FinancialsScreen(viewModel: GameViewModel) {
    val state by viewModel.gameState.collectAsState()
    val save = state ?: return

    val userTeam = save.teams.first { it.isUserControlled }

    // Dynamic wage bill & facility upkeep computations
    val squadWageBill = userTeam.roster.sumOf { it.contractWage }
    val facilitiesUpkeep = (userTeam.medicalLevel * 30_000L) +
            (userTeam.academyLevel * 50_000L) +
            (userTeam.trainingLevel * 40_000L) +
            (userTeam.scoutingLevel * 35_000L)
    val totalWeeklyExpenses = squadWageBill + facilitiesUpkeep

    // Calculate approximate match revenues
    val avgAttendance = (userTeam.stadiumCapacity * 0.75).toInt()
    val ticketRevEstimate = avgAttendance * userTeam.stadiumTicketPrice
    val concessionRevEstimate = avgAttendance * 12L
    val merchandiseRevEstimate = userTeam.clubStoreLevel * 100_000L
    val totalProjectedMathdayRevenue = ticketRevEstimate + concessionRevEstimate + merchandiseRevEstimate

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Bar
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
                        .testTag("financials_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PitchLime
                    )
                }

                Column {
                    Text(
                        text = "TREASURY HUB",
                        style = MaterialTheme.typography.labelSmall,
                        color = PitchLime,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "${userTeam.name} Finances",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // PRIMARY METRIC: Cash reserves balance card
            Card(
                colors = CardDefaults.cardColors(containerColor = PitchGreen),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.5.dp, PitchLime.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cash_reserves_card")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TOTAL AVAILABLE BALANCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = PitchLime,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "£${String.format("%,d", save.transferBudget)}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF81C784))
                        )
                        Text(
                            text = "Club Liquidity: Secure",
                            fontSize = 12.sp,
                            color = ChalkWhite,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // SECTION 2: Weekly Run Rate Sheet / Projections
            Text(
                text = "WEEKLY RUN RATE SHEET",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, BentoBorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Matchday Revenue estimates
                    Text(
                        text = "Projected Matchday Income (When playing Home)",
                        style = MaterialTheme.typography.labelMedium,
                        color = PitchLime,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    RowItem("Gate Ticket Sales (Est. 75% attendance)", "£${String.format("%,d", ticketRevEstimate)}")
                    RowItem("Stadium Concession Spend", "£${String.format("%,d", concessionRevEstimate)}")
                    RowItem("Merchandising Store Receipts", "£${String.format("%,d", merchandiseRevEstimate)}")

                    DividerRow()

                    RowItem("Gross Goal Revenue", "£${String.format("%,d", totalProjectedMathdayRevenue)}", highlight = true)

                    Spacer(modifier = Modifier.height(20.dp))

                    // Fixed Outgoings
                    Text(
                        text = "Fixed Outgoings / Expenses (Per Week)",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFE57373),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    RowItem("Squad Total Wage Bill", "£${String.format("%,d", squadWageBill)}")
                    RowItem("Facilities Maintenance & Insurance", "£${String.format("%,d", facilitiesUpkeep)}")
                    Text(
                        text = "• Medical staff: £${String.format("%,d", userTeam.medicalLevel * 30_000L)} | Academy: £${String.format("%,d", userTeam.academyLevel * 50_000L)}",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                    )
                    Text(
                        text = "• Training: £${String.format("%,d", userTeam.trainingLevel * 40_000L)} | Scouting: £${String.format("%,d", userTeam.scoutingLevel * 35_000L)}",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                    )

                    DividerRow()

                    RowItem("Total Fixed Outgoings", "-£${String.format("%,d", totalWeeklyExpenses)}", highlight = true)

                    Spacer(modifier = Modifier.height(14.dp))

                    // Net position estimate
                    val netHome = totalProjectedMathdayRevenue - totalWeeklyExpenses
                    val isPositive = netHome >= 0
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isPositive) Color(0xFF81C784).copy(alpha = 0.12f)
                                else Color(0xFFE57373).copy(alpha = 0.12f)
                            )
                            .padding(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Est. Net Home Week Margin",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${if (isPositive) "+" else ""}£${String.format("%,d", netHome)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isPositive) Color(0xFF81C784) else Color(0xFFE57373)
                            )
                        }
                    }
                }
            }

            // SECTION 3: Ledger Transactions list
            Text(
                text = "TRANSACTIONS REGISTRY LEDGER",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            if (save.financialTransactions.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, BentoBorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = "No receipts",
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No recorded transactions yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Play your first Match Week to see cash entries here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    save.financialTransactions.forEach { tx ->
                        val isGain = tx.amount >= 0
                        val isInfoOnly = tx.amount == 0L
                        val sign = when {
                            isInfoOnly -> ""
                            isGain -> "+£"
                            else -> "-£"
                        }
                        val valueDisplay = if (isInfoOnly) "STATEMENT" else "${sign}${String.format("%,d", Math.abs(tx.amount))}"
                        val valueColor = when {
                            isInfoOnly -> PitchLime
                            isGain -> Color(0xFF81C784)
                            else -> Color(0xFFE57373)
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, BentoBorderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVerticallyHorizontal,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (tx.category) {
                                                    "TICKET" -> Color(0xFFE91E63).copy(alpha = 0.15f)
                                                    "CONCESSION" -> Color(0xFFFF9800).copy(alpha = 0.15f)
                                                    "STORE" -> Color(0xFFD0BCFF).copy(alpha = 0.15f)
                                                    "WAGES" -> Color(0xFF00BCD4).copy(alpha = 0.15f)
                                                    "TRANSFERS" -> Color(0xFFFFEB3B).copy(alpha = 0.12f)
                                                    else -> PitchGreen.copy(alpha = 0.3f)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getLedgerIcon(tx.category),
                                            contentDescription = tx.category,
                                            tint = when (tx.category) {
                                                "TICKET" -> Color(0xFFE91E63)
                                                "CONCESSION" -> Color(0xFFFF9800)
                                                "STORE" -> Color(0xFFD0BCFF)
                                                "WAGES" -> Color(0xFF00BCD4)
                                                "TRANSFERS" -> Color(0xFFFFD54F)
                                                else -> PitchLime
                                            },
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(PitchGreen)
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "WK ${tx.matchDay}",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PitchLime
                                                )
                                            }
                                        }

                                        Text(
                                            text = tx.description,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                Text(
                                    text = valueDisplay,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = valueColor,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun RowItem(label: String, value: String, highlight: Boolean = false) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = if (highlight) 13.sp else 12.sp,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium,
            color = if (highlight) Color.White else Color.Gray
        )
        Text(
            text = value,
            fontSize = if (highlight) 13.sp else 12.sp,
            fontWeight = if (highlight) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = if (highlight) Color.White else Color.White.copy(alpha = 0.9f)
        )
    }
}

@Composable
fun DividerRow() {
    HorizontalDivider(
        color = Color.Gray.copy(alpha = 0.15f),
        thickness = 1.dp,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

fun getLedgerIcon(category: String): ImageVector {
    return when (category) {
        "TICKET" -> Icons.Default.ConfirmationNumber
        "CONCESSION" -> Icons.Default.Fastfood
        "STORE" -> Icons.Default.ShoppingCart
        "WAGES" -> Icons.Default.MonetizationOn
        "TRANSFERS" -> Icons.Default.SwapCalls
        else -> Icons.Default.ReceiptLong
    }
}

// Helper expansion align
private val Alignment.Companion.CenterVerticallyHorizontal: Alignment.Vertical
    get() = Alignment.CenterVertically
