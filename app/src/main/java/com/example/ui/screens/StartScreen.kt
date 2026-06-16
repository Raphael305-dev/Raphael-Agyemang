package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CareerSave
import com.example.ui.components.BentoBorderColor
import com.example.ui.components.CardBackground
import com.example.ui.components.PitchLime
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.Screen

@Composable
fun StartScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val saveSlots by viewModel.saveSlots.collectAsState()
    val activeSlot by viewModel.activeSaveSlot.collectAsState()

    var managerNameInput by remember { mutableStateOf("") }
    var clubNameInput by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1C1B1F), Color(0xFF121212))
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // Cool football icon/logo (Bento rounded style)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0x33D0BCFF))
                    .border(1.5.dp, PitchLime, RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SportsSoccer,
                    contentDescription = "Soccer logo",
                    tint = PitchLime,
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "FOOTBALL MANAGER",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 2.sp
            )
            Text(
                text = "Live Tactical Text Simulator",
                style = MaterialTheme.typography.bodySmall,
                color = PitchLime,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(30.dp))

            when (currentScreen) {
                Screen.SlotSelection -> {
                    Text(
                        text = "Choose Career Slot",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // 3 Profile slots
                    for (slotNum in 1..3) {
                        val existingSave = saveSlots.find { it.id == slotNum }
                        SaveSlotCard(
                            slotId = slotNum,
                            save = existingSave,
                            onSelect = { viewModel.loadSaveSlot(slotNum) },
                            onDelete = { viewModel.deleteSlot(slotNum) }
                        )
                    }
                }
                Screen.NewGameSetup -> {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp)),
                        color = CardBackground,
                        border = BorderStroke(1.5.dp, BentoBorderColor)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Setup Manager Profile (Slot #${activeSlot ?: 1})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = PitchLime
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = managerNameInput,
                                onValueChange = { managerNameInput = it },
                                label = { Text("Manager Name", color = Color(0xFFCAC4D0)) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("manager_name_field"),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PitchLime,
                                    unfocusedBorderColor = BentoBorderColor,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = clubNameInput,
                                onValueChange = { clubNameInput = it },
                                label = { Text("Club Name", color = Color(0xFFCAC4D0)) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("club_name_field"),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PitchLime,
                                    unfocusedBorderColor = BentoBorderColor,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.selectScreen(Screen.SlotSelection) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    border = BorderStroke(1.5.dp, BentoBorderColor)
                                ) {
                                    Text("Back")
                                }

                                Button(
                                    onClick = { viewModel.startNewCareer(managerNameInput, clubNameInput) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("submit_manager_profile"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PitchLime,
                                        contentColor = Color(0xFF381E72)
                                    )
                                ) {
                                    Text("Sign Contract", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun SaveSlotCard(
    slotId: Int,
    save: CareerSave?,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(1.5.dp, if (save != null) PitchLime else BentoBorderColor, RoundedCornerShape(28.dp))
            .clickable { onSelect() }
            .testTag("save_slot_$slotId"),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(28.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudQueue,
                        contentDescription = null,
                        tint = if (save != null) PitchLime else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SAVE SLOT $slotId",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (save != null) PitchLime else Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                if (save != null) {
                    Text(
                        text = save.clubName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Manager: ${save.managerName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )
                } else {
                    Text(
                        text = "Empty Save Profile",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                    Text(
                        text = "Start fresh journey",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                }
            }
            if (save != null) {
                IconButton(
                    onClick = { onDelete() },
                    modifier = Modifier.testTag("delete_slot_$slotId")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = "Delete Profile",
                        tint = Color.Red.copy(alpha = 0.8f)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = null,
                    tint = Color.DarkGray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
