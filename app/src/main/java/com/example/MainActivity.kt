package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BentoBorderColor
import com.example.ui.components.CardBackground
import com.example.ui.components.PitchGreen
import com.example.ui.components.PitchLime
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.Screen

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: GameViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val gameState by viewModel.gameState.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (currentScreen != Screen.ActiveMatch) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "FOOTBALL MANAGER",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = PitchLime,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = if (gameState != null) "Career Mode Active" else "Tactical Setup Lab",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    },
                    actions = {
                        // Game Settings configuration button
                        IconButton(
                            onClick = { 
                                com.example.utils.SoundEngine.playClick()
                                showSettingsDialog = true 
                            },
                            modifier = Modifier.testTag("app_settings_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Game Settings Hub",
                                tint = PitchLime
                            )
                        }

                        if (gameState != null) {
                            // Quick Save Career button
                            IconButton(
                                onClick = { 
                                    com.example.utils.SoundEngine.playNotification()
                                    viewModel.saveGame() 
                                },
                                modifier = Modifier.testTag("quick_save_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = "Quick Save Career",
                                    tint = PitchLime
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = CardBackground,
                        titleContentColor = Color.White
                    )
                )
            }
        },
        bottomBar = {
            if (gameState != null && currentScreen != Screen.ActiveMatch && currentScreen != Screen.PressConference) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    NavigationBar(
                        containerColor = CardBackground,
                        contentColor = Color.White,
                        modifier = Modifier
                            .clip(RoundedCornerShape(28.dp))
                            .border(1.5.dp, BentoBorderColor, RoundedCornerShape(28.dp))
                            .height(68.dp),
                        tonalElevation = 0.dp
                    ) {
                        NavigationBarItem(
                            selected = currentScreen == Screen.Dashboard,
                            onClick = { viewModel.selectScreen(Screen.Dashboard) },
                            icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Dashboard") },
                            label = { Text("Hub", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF381E72),
                                selectedTextColor = PitchLime,
                                indicatorColor = PitchLime,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            ),
                            modifier = Modifier.testTag("nav_hub")
                        )

                        NavigationBarItem(
                            selected = currentScreen == Screen.Tactics,
                            onClick = { viewModel.selectScreen(Screen.Tactics) },
                            icon = { Icon(imageVector = Icons.Default.GridOn, contentDescription = "Tactics") },
                            label = { Text("Tactics", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF381E72),
                                selectedTextColor = PitchLime,
                                indicatorColor = PitchLime,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            ),
                            modifier = Modifier.testTag("nav_tactics")
                        )

                        NavigationBarItem(
                            selected = currentScreen == Screen.Scouting,
                            onClick = { viewModel.selectScreen(Screen.Scouting) },
                            icon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Scouting") },
                            label = { Text("Scout Market", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF381E72),
                                selectedTextColor = PitchLime,
                                indicatorColor = PitchLime,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            ),
                            modifier = Modifier.testTag("nav_scouting")
                        )

                        NavigationBarItem(
                            selected = currentScreen == Screen.Upgrades,
                            onClick = { viewModel.selectScreen(Screen.Upgrades) },
                            icon = { Icon(imageVector = Icons.Default.Business, contentDescription = "Upgrades") },
                            label = { Text("Upgrades", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF381E72),
                                selectedTextColor = PitchLime,
                                indicatorColor = PitchLime,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            ),
                            modifier = Modifier.testTag("nav_upgrades")
                        )

                        NavigationBarItem(
                            selected = currentScreen == Screen.LeagueTable,
                            onClick = { viewModel.selectScreen(Screen.LeagueTable) },
                            icon = { Icon(imageVector = Icons.Default.FormatListNumbered, contentDescription = "Standings") },
                            label = { Text("League", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF381E72),
                                selectedTextColor = PitchLime,
                                indicatorColor = PitchLime,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            ),
                            modifier = Modifier.testTag("nav_standings")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main navigation router/selector
            when (currentScreen) {
                Screen.SlotSelection, Screen.NewGameSetup -> {
                    StartScreen(viewModel = viewModel)
                }
                Screen.Dashboard -> {
                    DashboardScreen(viewModel = viewModel)
                }
                Screen.Tactics -> {
                    TacticsScreen(viewModel = viewModel)
                }
                Screen.Scouting -> {
                    ScoutingScreen(viewModel = viewModel)
                }
                Screen.LeagueTable -> {
                    LeagueScreen(viewModel = viewModel)
                }
                Screen.Upgrades -> {
                    UpgradesScreen(viewModel = viewModel)
                }
                Screen.ActiveMatch -> {
                    MatchSimulationScreen(viewModel = viewModel)
                }
                Screen.PressConference -> {
                    PressConferenceScreen(viewModel = viewModel)
                }
                Screen.Financials -> {
                    FinancialsScreen(viewModel = viewModel)
                }
                Screen.Sponsorships -> {
                    SponsorshipScreen(viewModel = viewModel)
                }
                Screen.BoardGoals -> {
                    BoardGoalsScreen(viewModel = viewModel)
                }
                Screen.Staff -> {
                    StaffScreen(viewModel = viewModel)
                }
                Screen.SeasonRecap -> {
                    SeasonRecapScreen(viewModel = viewModel)
                }
                Screen.PlayerStats -> {
                    PlayerStatsScreen(viewModel = viewModel)
                }
                Screen.Training -> {
                    TrainingScreen(viewModel = viewModel)
                }
            }

            // Beautiful status floating custom toast
            AnimatedVisibility(
                visible = toastMessage != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -50 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -50 }),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            ) {
                toastMessage?.let { msg ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEADDFF)),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.5.dp, PitchGreen),
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .clip(RoundedCornerShape(20.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF21005D),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF21005D),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // GORGEOUS ADJUSTABLE SYSTEM SETTINGS DIALOG
            if (showSettingsDialog) {
                androidx.compose.ui.window.Dialog(onDismissRequest = { showSettingsDialog = false }) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clip(RoundedCornerShape(26.dp)),
                        color = CardBackground,
                        border = BorderStroke(1.5.dp, BentoBorderColor)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(20.dp)
                                .fillMaxWidth()
                        ) {
                            // HEADER Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "SYSTEM CONFIGURATION",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PitchLime,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        "Atmosphere Settings",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                                IconButton(onClick = { 
                                    com.example.utils.SoundEngine.playClick()
                                    showSettingsDialog = false 
                                }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close Dialog", tint = Color.Gray)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = BentoBorderColor)
                            Spacer(modifier = Modifier.height(14.dp))

                            // Scrollable settings contents
                            Column(
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                // SECTION 1: SOUND SYNTHESIZER
                                Text(
                                    "SOUND SYNTHESIZER & EFFECTS",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.LightGray
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                var syntheticSoundEnabled by remember { mutableStateOf(com.example.utils.SoundEngine.isSoundEnabled) }
                                var soundVolume by remember { mutableFloatStateOf(com.example.utils.SoundEngine.volumePercent.toFloat()) }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0x19FFFFFF))
                                        .clickable {
                                            syntheticSoundEnabled = !syntheticSoundEnabled
                                            com.example.utils.SoundEngine.isSoundEnabled = syntheticSoundEnabled
                                            com.example.utils.SoundEngine.playClick()
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (syntheticSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                            contentDescription = null,
                                            tint = PitchLime
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text("Synthetic Match Audio", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                            Text("Referee kickoff whistle, goals, levelups", fontSize = 11.sp, color = Color.Gray)
                                        }
                                    }
                                    Switch(
                                        checked = syntheticSoundEnabled,
                                        onCheckedChange = {
                                            syntheticSoundEnabled = it
                                            com.example.utils.SoundEngine.isSoundEnabled = it
                                            com.example.utils.SoundEngine.playClick()
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.Black,
                                            checkedTrackColor = PitchLime,
                                            uncheckedThumbColor = Color.Gray,
                                            uncheckedTrackColor = Color.DarkGray
                                        ),
                                        modifier = Modifier.testTag("audio_toggle_switch")
                                    )
                                }

                                if (syntheticSoundEnabled) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0x0DFFFFFF))
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Synthesizer Volume", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                            Text("${soundVolume.toInt()}%", fontWeight = FontWeight.Bold, color = PitchLime, fontSize = 12.sp)
                                        }
                                        Slider(
                                            value = soundVolume,
                                            onValueChange = {
                                                soundVolume = it
                                                com.example.utils.SoundEngine.updateVolume(it.toInt())
                                            },
                                            valueRange = 10f..100f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = PitchLime,
                                                activeTrackColor = PitchLime,
                                                inactiveTrackColor = Color.DarkGray
                                            ),
                                            modifier = Modifier.testTag("audio_volume_slider")
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Acoustic Cues Playground:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = { com.example.utils.SoundEngine.playWhistle() },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2B2D), contentColor = Color.White),
                                                modifier = Modifier.weight(1f).height(32.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text("📢 Referee", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Button(
                                                onClick = { com.example.utils.SoundEngine.playGoalCheer() },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2B2D), contentColor = Color.White),
                                                modifier = Modifier.weight(1f).height(32.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text("⚽ Goal!", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Button(
                                                onClick = { com.example.utils.SoundEngine.playUpgradeSuccess() },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2B2D), contentColor = Color.White),
                                                modifier = Modifier.weight(1f).height(32.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text("⭐ RiseUp", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))
                                HorizontalDivider(color = BentoBorderColor)
                                Spacer(modifier = Modifier.height(14.dp))

                                // SECTION 2: STADIUM THEME SETTING
                                Text(
                                    "STADIUM ATMOSPHERE THEME",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.LightGray
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                val themesList = listOf(
                                    Triple("BENTO", "Classic Purple Bento", "Lavender primary with high contrast violet borders."),
                                    Triple("TURF", "Neon Turf Grass", "Vibrant pitch lime green with dark night stadium hues."),
                                    Triple("SUNSET", "Cyber Derby Sunset", "Vibrant flame crimson with evening bleacher textures."),
                                    Triple("GOLD", "Gold Trophy Room", "Elite luxury trophy-gold and mahogany executive design.")
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    themesList.forEach { (themeId, name, desc) ->
                                        val isSelected = com.example.ui.components.currentThemeChoice == themeId
                                        val accentCol = when (themeId) {
                                            "TURF" -> Color(0xFF00FF87)
                                            "SUNSET" -> Color(0xFFFF5E62)
                                            "GOLD" -> Color(0xFFFFD100)
                                            else -> Color(0xFFD0BCFF)
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isSelected) Color(0x1AFFFFFF) else Color(0x08FFFFFF))
                                                .border(
                                                    width = 1.5.dp,
                                                    color = if (isSelected) accentCol else Color.Transparent,
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .clickable {
                                                    com.example.ui.components.currentThemeChoice = themeId
                                                    com.example.utils.SoundEngine.playClick()
                                                }
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Color indicator circle
                                            Box(
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                                    .background(accentCol)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    name,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) accentCol else Color.White,
                                                    fontSize = 13.sp
                                                )
                                                Text(desc, fontSize = 10.sp, color = Color.Gray)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))
                            Button(
                                onClick = {
                                    com.example.utils.SoundEngine.playClick()
                                    showSettingsDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PitchLime, contentColor = Color.Black),
                                modifier = Modifier.fillMaxWidth().height(42.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("APPLY CONFIGURATION", fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }
        }
    }
}
