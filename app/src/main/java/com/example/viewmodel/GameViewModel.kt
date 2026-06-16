package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CareerSave
import com.example.data.GameDatabase
import com.example.data.GameRepository
import com.example.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

sealed interface Screen {
    object SlotSelection : Screen
    object NewGameSetup : Screen
    object Dashboard : Screen
    object Tactics : Screen
    object Scouting : Screen
    object LeagueTable : Screen
    object ActiveMatch : Screen
    object Upgrades : Screen
    object PressConference : Screen
    object Financials : Screen
    object Sponsorships : Screen
    object BoardGoals : Screen
    object Staff : Screen
    object SeasonRecap : Screen
    object PlayerStats : Screen
    object Training : Screen
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val db = GameDatabase.getDatabase(application)
    private val repository = GameRepository(db.careerDao)

    // Save slots flow
    val saveSlots: StateFlow<List<CareerSave>> = repository.allCareers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state
    private val _currentScreen = MutableStateFlow<Screen>(Screen.SlotSelection)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _activeSaveSlot = MutableStateFlow<Int?>(null)
    val activeSaveSlot: StateFlow<Int?> = _activeSaveSlot.asStateFlow()

    private val _gameState = MutableStateFlow<SaveState?>(null)
    val gameState: StateFlow<SaveState?> = _gameState.asStateFlow()

    // Scouting states
    private val _scoutingPositionFilter = MutableStateFlow("ALL")
    val scoutingPositionFilter: StateFlow<String> = _scoutingPositionFilter.asStateFlow()

    private val _isScoutingBusy = MutableStateFlow(false)
    val isScoutingBusy: StateFlow<Boolean> = _isScoutingBusy.asStateFlow()

    // Live Match Simulator state
    private val _liveMatchFixture = MutableStateFlow<Fixture?>(null)
    val liveMatchFixture: StateFlow<Fixture?> = _liveMatchFixture.asStateFlow()

    private val _liveMatchMinute = MutableStateFlow(0)
    val liveMatchMinute: StateFlow<Int> = _liveMatchMinute.asStateFlow()

    private val _liveHomeScore = MutableStateFlow(0)
    val liveHomeScore: StateFlow<Int> = _liveHomeScore.asStateFlow()

    private val _liveAwayScore = MutableStateFlow(0)
    val liveAwayScore: StateFlow<Int> = _liveAwayScore.asStateFlow()

    private val _liveCommentary = MutableStateFlow<List<MatchCommentaryEvent>>(emptyList())
    val liveCommentary: StateFlow<List<MatchCommentaryEvent>> = _liveCommentary.asStateFlow()

    private val _matchStatus = MutableStateFlow("PRE_MATCH") // "PRE_MATCH", "PLAYING", "HALF_TIME", "SECOND_HALF", "FULL_TIME"
    val matchStatus: StateFlow<String> = _matchStatus.asStateFlow()

    private val _isFastForward = MutableStateFlow(false)
    val isFastForward: StateFlow<Boolean> = _isFastForward.asStateFlow()

    private val _userTeamIdVal = MutableStateFlow("team_user")
    val userTeamIdVal: StateFlow<String> = _userTeamIdVal.asStateFlow()

    // Game message overlay
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private var matchSimulationJob: Job? = null

    fun selectScreen(screen: Screen) {
        _currentScreen.value = screen
        com.example.utils.SoundEngine.playClick()
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
        viewModelScope.launch {
            delay(2500)
            if (_toastMessage.value == msg) _toastMessage.value = null
        }
    }

    // Load Career Slot
    fun loadSaveSlot(slotId: Int) {
        viewModelScope.launch {
            val state = repository.loadSaveState(slotId)
            if (state != null) {
                // Defensive safe back-filling for older career slots
                val sanitisedState = state.copy(
                    availableSponsorshipOffers = if (state.availableSponsorshipOffers.isNullOrEmpty()) {
                        com.example.model.GameEngine.generateSponsorshipOffers()
                    } else {
                        state.availableSponsorshipOffers
                    },
                    activeBoardGoals = if (state.activeBoardGoals.isNullOrEmpty()) {
                        com.example.model.GameEngine.generateBoardGoals()
                    } else {
                        state.activeBoardGoals
                    }
                )
                _gameState.value = sanitisedState
                _userTeamIdVal.value = sanitisedState.userTeamId
                _activeSaveSlot.value = slotId
                _currentScreen.value = Screen.Dashboard
                showToast("Welcome back! Career loaded.")
            } else {
                _activeSaveSlot.value = slotId
                _currentScreen.value = Screen.NewGameSetup
            }
        }
    }

    // Start a new career
    fun startNewCareer(managerName: String, clubName: String) {
        val slot = _activeSaveSlot.value ?: 1
        viewModelScope.launch {
            val mName = managerName.ifBlank { "Unsigned Manager" }
            val cName = clubName.ifBlank { "City FC" }
            val initialState = GameEngine.createInitialLeagueState(mName, cName)
            _gameState.value = initialState
            _userTeamIdVal.value = initialState.userTeamId
            repository.saveCareer(slot, mName, cName, initialState)
            _currentScreen.value = Screen.Dashboard
            showToast("Career started as Manager of $cName!")
        }
    }

    // Force Delete slot
    fun deleteSlot(slotId: Int) {
        viewModelScope.launch {
            repository.deleteSlot(slotId)
            if (_activeSaveSlot.value == slotId) {
                _gameState.value = null
                _activeSaveSlot.value = null
                _currentScreen.value = Screen.SlotSelection
            }
            showToast("Save slot $slotId deleted.")
        }
    }

    // Save Game state
    fun saveGame() {
        val slot = _activeSaveSlot.value ?: return
        val state = _gameState.value ?: return
        viewModelScope.launch {
            repository.saveCareer(slot, state.managerName, state.clubName, state)
            showToast("Game saved successfully.")
        }
    }

    // Save current squad and standings mimicking browser local storage using SharedPreferences
    fun saveToLocalStorage() {
        val state = _gameState.value ?: return
        try {
            val context = getApplication<Application>().applicationContext
            val sharedPrefs = context.getSharedPreferences("browser_local_storage", Context.MODE_PRIVATE)
            
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
                
            val teamsType = Types.newParameterizedType(List::class.java, Team::class.java)
            val standingsType = Types.newParameterizedType(List::class.java, LeagueStanding::class.java)
            
            val teamsJson = moshi.adapter<List<Team>>(teamsType).toJson(state.teams)
            val standingsJson = moshi.adapter<List<LeagueStanding>>(standingsType).toJson(state.standings)
            
            sharedPrefs.edit()
                .putString("local_storage_teams", teamsJson)
                .putString("local_storage_standings", standingsJson)
                .apply()
                
            showToast("Standings & Squad persisted to local storage!")
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Failed to save to local storage: ${e.message}")
        }
    }

    // Load squad and standings from local storage
    fun loadFromLocalStorage() {
        val state = _gameState.value ?: return
        try {
            val context = getApplication<Application>().applicationContext
            val sharedPrefs = context.getSharedPreferences("browser_local_storage", Context.MODE_PRIVATE)
            
            val teamsJson = sharedPrefs.getString("local_storage_teams", null)
            val standingsJson = sharedPrefs.getString("local_storage_standings", null)
            
            if (teamsJson == null || standingsJson == null) {
                showToast("No local storage backup found.")
                return
            }
            
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
                
            val teamsType = Types.newParameterizedType(List::class.java, Team::class.java)
            val standingsType = Types.newParameterizedType(List::class.java, LeagueStanding::class.java)
            
            val loadedTeams = moshi.adapter<List<Team>>(teamsType).fromJson(teamsJson)
            val loadedStandings = moshi.adapter<List<LeagueStanding>>(standingsType).fromJson(standingsJson)
            
            if (loadedTeams != null && loadedStandings != null) {
                _gameState.value = state.copy(
                    teams = loadedTeams,
                    standings = loadedStandings
                )
                showToast("Standings & Squad loaded from local storage!")
            } else {
                showToast("Failed to parse local storage data.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Failed to load from local storage: ${e.message}")
        }
    }

    private fun addFinancialTransaction(
        state: SaveState,
        description: String,
        amount: Long,
        category: String
    ): SaveState {
        val transaction = FinancialTransaction(
            id = "tx_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}",
            matchDay = state.currentMatchDay,
            description = description,
            amount = amount,
            category = category
        )
        return state.copy(financialTransactions = listOf(transaction) + state.financialTransactions)
    }

    fun selectPressConferenceOption(optionIndex: Int) {
        val state = _gameState.value ?: return
        val pc = state.activePressConference ?: return
        val currentQ = pc.questions.getOrNull(pc.currentQuestionIndex) ?: return
        val option = currentQ.options.getOrNull(optionIndex) ?: return

        val updatedAnswersList = pc.answersChosen + optionIndex
        val nextIndex = pc.currentQuestionIndex + 1
        val isCompleted = nextIndex >= pc.questions.size

        // Apply outcomes immediately
        // Morale and board confidence update
        val updatedTeams = state.teams.map { t ->
            if (t.isUserControlled) {
                val updatedRoster = t.roster.map { p ->
                    p.copy(morale = (p.morale + option.teamMoraleImpact).coerceIn(30, 100))
                }
                t.copy(
                    roster = updatedRoster,
                    boardConfidence = (t.boardConfidence + option.boardConfidenceImpact).coerceIn(10, 100)
                )
            } else t
        }

        val updatedPc = pc.copy(
            currentQuestionIndex = nextIndex,
            answersChosen = updatedAnswersList,
            completed = isCompleted
        )

        var nextScreen = _currentScreen.value
        var finalPc: PressConference? = updatedPc
        if (isCompleted) {
            finalPc = null
            nextScreen = Screen.Dashboard
            showToast("Press Conference Concluded!")
        }

        _gameState.value = state.copy(
            teams = updatedTeams,
            activePressConference = finalPc
        ).let { s ->
            // Optionally log a tiny dynamic entry in transactions just so the user sees press presence
            val messageFeedback = "Press statement: ${option.text.take(30)}..."
            addFinancialTransaction(s, messageFeedback, 0L, "STORE")
        }
        _currentScreen.value = nextScreen
        saveGame()
    }

    private fun generatePressConference(
        userTeamName: String,
        scored: Int,
        conceded: Int,
        matchDay: Int,
        opponentName: String
    ): PressConference {
        val title = "Post-Match Press Room"
        val desc = "Journalists gather at the media desk at $userTeamName to analyze the Match Day $matchDay results."
        val questions = mutableListOf<PressConferenceQuestion>()

        val diff = scored - conceded

        // Question 1: Match analysis based on outcome
        if (matchDay == 1) {
            questions.add(
                PressConferenceQuestion(
                    questionId = "opener_q1",
                    text = "This is the season opener, Boss. How do you assess the team momentum going forward after Match Day 1?",
                    options = listOf(
                        PressConferenceOption(
                            text = "It's just the beginning. Our long-term tactical vision is what matters.",
                            teamMoraleImpact = 4,
                            boardConfidenceImpact = 5,
                            explanation = "The press noted your calm, professional focus."
                        ),
                        PressConferenceOption(
                            text = "We wanted maximum impact! We're here to win silverware for our supporters.",
                            teamMoraleImpact = 8,
                            boardConfidenceImpact = 3,
                            explanation = "Your high-ambition tone excites both the fans and players!"
                        ),
                        PressConferenceOption(
                            text = "A grueling game. We must improve our discipline if we are to survive.",
                            teamMoraleImpact = -5,
                            boardConfidenceImpact = 2,
                            explanation = "Your cautious tone disappointed players, but the board likes your realism."
                        )
                    )
                )
            )
        } else if (matchDay == 7) {
            questions.add(
                PressConferenceQuestion(
                    questionId = "midseason_q1",
                    text = "We are halfway through the season, Boss. Are you happy with our overall competitive trajectory?",
                    options = listOf(
                        PressConferenceOption(
                            text = "Absolutely. The squad is uniting perfectly and our focus is razor sharp.",
                            teamMoraleImpact = 6,
                            boardConfidenceImpact = 4,
                            explanation = "The crew responds positively to your leadership consistency."
                        ),
                        PressConferenceOption(
                            text = "We expect more of ourselves. Change is coming in the next weeks.",
                            teamMoraleImpact = -3,
                            boardConfidenceImpact = 6,
                            explanation = "The board feels assured by your high standards, but some squad members feel uneasy."
                        )
                    )
                )
            )
        } else if (matchDay == 14) {
            questions.add(
                PressConferenceQuestion(
                    questionId = "finale_q1",
                    text = "The season has officially concluded. How do you evaluate your tenure under the boardroom?",
                    options = listOf(
                        PressConferenceOption(
                            text = "I am extremely proud of my players. We worked with total commitment.",
                            teamMoraleImpact = 15,
                            boardConfidenceImpact = 5,
                            explanation = "The squad highly appreciates your loyalty."
                        ),
                        PressConferenceOption(
                            text = "It has been structured, but there are areas we must invest in.",
                            teamMoraleImpact = 5,
                            boardConfidenceImpact = 8,
                            explanation = "The board welcomes your professional blueprint for squad growth."
                        )
                    )
                )
            )
        } else {
            // Match outcome based Q1
            when {
                diff >= 3 -> {
                    questions.add(
                        PressConferenceQuestion(
                            questionId = "win_big_q1",
                            text = "An absolute masterclass performance! Are you starting to believe your tactics are completely unstoppable?",
                            options = listOf(
                                PressConferenceOption(
                                    text = "Indeed! We put on a clinical show, and we're hungry for more.",
                                    teamMoraleImpact = 10,
                                    boardConfidenceImpact = 5,
                                    explanation = "Players are absolutely ecstatic, and the press appreciates the bold stance."
                                ),
                                PressConferenceOption(
                                    text = "Complacency is our greatest threat. We remain focused and realistic.",
                                    teamMoraleImpact = 2,
                                    boardConfidenceImpact = 8,
                                    explanation = "Excellent professionalism. The board heavily admires your cautious mentality."
                                ),
                                PressConferenceOption(
                                    text = "It was easy today, but our opponent didn't offer much resistance.",
                                    teamMoraleImpact = -3,
                                    boardConfidenceImpact = 2,
                                    explanation = "Your humble yet blunt response received subtle criticism."
                                )
                            )
                        )
                    )
                }
                diff in 1..2 -> {
                    questions.add(
                        PressConferenceQuestion(
                            questionId = "win_narrow_q1",
                            text = "You ground out a close victory today. How crucial are these closely-fought tactical battles?",
                            options = listOf(
                                PressConferenceOption(
                                    text = "These are the results that build character. Three points is all that matters.",
                                    teamMoraleImpact = 5,
                                    boardConfidenceImpact = 4,
                                    explanation = "The room appreciates your positive attitude."
                                ),
                                PressConferenceOption(
                                    text = "We played well, but we must kill the match off earlier next time.",
                                    teamMoraleImpact = 1,
                                    boardConfidenceImpact = 6,
                                    explanation = "The board respects your constant demand for better performance."
                                )
                            )
                        )
                    )
                }
                diff == 0 -> {
                    questions.add(
                        PressConferenceQuestion(
                            questionId = "draw_q1",
                            text = "It ends in a stalemate today. Do you view today as a point gained or two points dropped?",
                            options = listOf(
                                PressConferenceOption(
                                    text = "We wanted all three points, so it's frustrating. We must improve our productivity.",
                                    teamMoraleImpact = -2,
                                    boardConfidenceImpact = 5,
                                    explanation = "The board feels comfortable with your high standards."
                                ),
                                PressConferenceOption(
                                    text = "A point gained. In a tough, competitive campaign, every single point matters.",
                                    teamMoraleImpact = 5,
                                    boardConfidenceImpact = 2,
                                    explanation = "The players appreciate your defensive praise."
                                )
                            )
                        )
                    )
                }
                diff in -2..-1 -> {
                    questions.add(
                        PressConferenceQuestion(
                            questionId = "loss_narrow_q1",
                            text = "A narrow defeat today, Boss. What went wrong with the team's execution?",
                            options = listOf(
                                PressConferenceOption(
                                    text = "We lacked sharpness in key moments, but I have absolute faith in these players.",
                                    teamMoraleImpact = 8,
                                    boardConfidenceImpact = 2,
                                    explanation = "The players admire your personal support."
                                ),
                                PressConferenceOption(
                                    text = "The technical execution wasn't professional. Individual mistakes cost us.",
                                    teamMoraleImpact = -12,
                                    boardConfidenceImpact = 5,
                                    explanation = "Your squad feels scapegoated, but the media loves the drama!"
                                ),
                                PressConferenceOption(
                                    text = "Tactically we were fine. It was purely bad luck with the officiating.",
                                    teamMoraleImpact = 2,
                                    boardConfidenceImpact = -5,
                                    explanation = "The board frowns upon shifting blame, though squad camaraderie holds."
                                )
                            )
                        )
                    )
                }
                else -> { // diff <= -3
                    questions.add(
                        PressConferenceQuestion(
                            questionId = "loss_big_q1",
                            text = "A catastrophic breakdown on the pitch today. Is the squad losing belief in your tactical vision?",
                            options = listOf(
                                PressConferenceOption(
                                    text = "I take full responsibility. The game plan was wrong, not the players.",
                                    teamMoraleImpact = 12,
                                    boardConfidenceImpact = -10,
                                    explanation = "The players appreciate your integrity enormously, but board confidence has dropped."
                                ),
                                PressConferenceOption(
                                    text = "We need a complete tactical overhaul. Certain players simply didn't put in the shifts.",
                                    teamMoraleImpact = -15,
                                    boardConfidenceImpact = 4,
                                    explanation = "Morale takes a severe hit as you publicly question player loyalty."
                                ),
                                PressConferenceOption(
                                    text = "It is simple: we must stay unified and work twice as hard in training.",
                                    teamMoraleImpact = 4,
                                    boardConfidenceImpact = 4,
                                    explanation = "A level-headed response that calms the storm."
                                )
                            )
                        )
                    )
                }
            }
        }

        // Question 2: Tactical or player focus
        questions.add(
            PressConferenceQuestion(
                questionId = "tactical_q2",
                text = "Critics in the boardroom are questioning your squad's stamina. Will you customize training intensity?",
                options = listOf(
                    PressConferenceOption(
                        text = "We believe in our identity. Consistency in preparation is what creates winning eras.",
                        teamMoraleImpact = 4,
                        boardConfidenceImpact = 4,
                        explanation = "Your dedication demonstrates sturdy character."
                    ),
                    PressConferenceOption(
                        text = "We are always adaptive. Tactics are fluid based on our roster's current exhaustion.",
                        teamMoraleImpact = 3,
                        boardConfidenceImpact = 5,
                        explanation = "A diplomatic and technically sound statement."
                    )
                )
            )
        )

        // Question 3: Financials/Board support
        questions.add(
            PressConferenceQuestion(
                questionId = "financial_q3",
                text = "Supporters are anxious about club investments and stadium infrastructure. Are you satisfied with the transfer budget?",
                options = listOf(
                    PressConferenceOption(
                        text = "The board has been collaborative. We invest carefully for the future of $userTeamName.",
                        teamMoraleImpact = 2,
                        boardConfidenceImpact = 8,
                        explanation = "The board is highly pleased with your corporate diplomacy."
                    ),
                    PressConferenceOption(
                        text = "We have huge resources, and we are going to sign star players in the market!",
                        teamMoraleImpact = 6,
                        boardConfidenceImpact = -5,
                        explanation = "Fans are thrilled, but the board is irritated by your loose financial talking."
                    )
                )
            )
        )

        return PressConference(
            title = title,
            description = desc,
            questions = questions
        )
    }

    // Modify Formation
    fun changeFormation(formation: String) {
        val state = _gameState.value ?: return
        val updatedTeams = state.teams.map { team ->
            if (team.isUserControlled) {
                val roster = team.roster
                val (newXI, _) = GameEngine.selectAutoLineup(roster, formation)
                team.copy(
                    formation = formation,
                    lineupIds = newXI.map { it.id }
                )
            } else team
        }
        _gameState.value = state.copy(teams = updatedTeams)
        saveGame()
        showToast("Formation updated to $formation! Best XI set.")

        if (_currentScreen.value == Screen.ActiveMatch) {
            val userTeam = state.teams.firstOrNull { it.isUserControlled }
            if (userTeam != null) {
                val currentMin = _liveMatchMinute.value
                val shapeEvent = MatchCommentaryEvent(
                    minute = currentMin,
                    type = "INFO",
                    description = "$currentMin' | 📋 [TACTICS] ${userTeam.name} switches to a new tactical outline: $formation shape design.",
                    currentHomeScore = _liveHomeScore.value,
                    currentAwayScore = _liveAwayScore.value
                )
                _liveCommentary.value = _liveCommentary.value + shapeEvent
            }
        }
    }

    // Swap starting player with bench player
    fun swapPlayers(startingId: String, benchId: String) {
        val state = _gameState.value ?: return
        val updatedTeams = state.teams.map { team ->
            if (team.isUserControlled) {
                val currentLineup = team.lineupIds.toMutableList()
                if (startingId in currentLineup && benchId !in currentLineup) {
                    currentLineup.remove(startingId)
                    currentLineup.add(benchId)
                    showToast("Squad changes updated successfully.")
                }
                team.copy(lineupIds = currentLineup)
            } else team
        }
        _gameState.value = state.copy(teams = updatedTeams)
        saveGame()

        if (_currentScreen.value == Screen.ActiveMatch) {
            val userTeam = state.teams.firstOrNull { it.isUserControlled }
            val outPlayer = userTeam?.roster?.firstOrNull { it.id == startingId }
            val inPlayer = userTeam?.roster?.firstOrNull { it.id == benchId }
            if (outPlayer != null && inPlayer != null) {
                val currentMin = _liveMatchMinute.value
                val subEvent = MatchCommentaryEvent(
                    minute = currentMin,
                    type = "INFO",
                    description = "$currentMin' | 🔄 [TACTICS SUB] ${userTeam.name} substitution: ${outPlayer.name} (${outPlayer.position}) is replaced by ${inPlayer.name} (${inPlayer.position}).",
                    currentHomeScore = _liveHomeScore.value,
                    currentAwayScore = _liveAwayScore.value
                )
                _liveCommentary.value = _liveCommentary.value + subEvent
            }
        }
    }

    // Appoint new Captain
    fun setCaptain(playerId: String) {
        val state = _gameState.value ?: return
        val updatedTeams = state.teams.map { team ->
            if (team.isUserControlled) {
                team.copy(captainId = playerId)
            } else team
        }
        _gameState.value = state.copy(teams = updatedTeams)
        saveGame()
        showToast("New Captain appointed.")
    }

    // Change tactics Mentality
    fun setMentality(mentality: String) {
        val state = _gameState.value ?: return
        val updatedTeams = state.teams.map { team ->
            if (team.isUserControlled) team.copy(playMentality = mentality) else team
        }
        _gameState.value = state.copy(teams = updatedTeams)
        saveGame()
        showToast("Mentality changed to $mentality.")

        if (_currentScreen.value == Screen.ActiveMatch) {
            val userTeam = state.teams.firstOrNull { it.isUserControlled }
            if (userTeam != null) {
                val currentMin = _liveMatchMinute.value
                val mentEvent = MatchCommentaryEvent(
                    minute = currentMin,
                    type = "INFO",
                    description = "$currentMin' | 📋 [TACTICS] Coach adjusts team orientation to $mentality mentality on the fly.",
                    currentHomeScore = _liveHomeScore.value,
                    currentAwayScore = _liveAwayScore.value
                )
                _liveCommentary.value = _liveCommentary.value + mentEvent
            }
        }
    }

    // Change tactics Pressing
    fun setPressing(pressing: String) {
        val state = _gameState.value ?: return
        val updatedTeams = state.teams.map { team ->
            if (team.isUserControlled) team.copy(pressingIntensity = pressing) else team
        }
        _gameState.value = state.copy(teams = updatedTeams)
        saveGame()
        showToast("Team pressing is now set to $pressing.")

        if (_currentScreen.value == Screen.ActiveMatch) {
            val userTeam = state.teams.firstOrNull { it.isUserControlled }
            if (userTeam != null) {
                val currentMin = _liveMatchMinute.value
                val pressEvent = MatchCommentaryEvent(
                    minute = currentMin,
                    type = "INFO",
                    description = "$currentMin' | 📋 [TACTICS] Pressing lines modified to $pressing strategy intensity.",
                    currentHomeScore = _liveHomeScore.value,
                    currentAwayScore = _liveAwayScore.value
                )
                _liveCommentary.value = _liveCommentary.value + pressEvent
            }
        }
    }

    // Scout Talent action
    fun scoutNewTalent() {
        val state = _gameState.value ?: return
        if (state.transferBudget < 500_000L) {
            showToast("Insufficient budget to launch modern scouting operations (£500k needed).")
            return
        }

        viewModelScope.launch {
            _isScoutingBusy.value = true
            delay(1200) // atmospheric wait

            val filter = if (_scoutingPositionFilter.value == "ALL") null else _scoutingPositionFilter.value
            val userTeam = state.teams.firstOrNull { it.isUserControlled }
            val scoutingLevel = userTeam?.scoutingLevel ?: 1
            
            val slotsCount = 6 + (scoutingLevel - 1) * 2
            val minQ = 65 + (scoutingLevel - 1) * 2
            val maxQ = 92 + (scoutingLevel - 1) * 1
            
            val hasSven = state.hiredStaff.any { it.id == "staff_scout_legend" }
            val hasMaya = state.hiredStaff.any { it.id == "staff_scout_analyst" }
            val ratingBoost = if (hasSven) 20 else if (hasMaya) 10 else 0

            val newCandidates = List(slotsCount) {
                val baseCandidate = GameEngine.generateScoutCandidate(filter, minRating = minQ, maxRating = maxQ.coerceAtMost(99))
                if (ratingBoost > 0) {
                    val boostedAttack = (baseCandidate.ratingAttack + ratingBoost / 2).coerceAtMost(99)
                    val boostedDefend = (baseCandidate.ratingDefend + ratingBoost / 2).coerceAtMost(99)
                    val newValue = (baseCandidate.value * (1.1 + ratingBoost * 0.05)).toLong()
                    baseCandidate.copy(
                        ratingAttack = boostedAttack,
                        ratingDefend = boostedDefend,
                        value = newValue,
                        description = baseCandidate.description + " [Staff Boost]"
                    )
                } else {
                    baseCandidate
                }
            }

            val updatedState = state.copy(
                scoutCandidates = newCandidates,
                transferBudget = state.transferBudget - 500_000L
            )
            // Update user controlled team budget in the list
            val finalTeams = updatedState.teams.map {
                if (it.isUserControlled) it.copy(budget = updatedState.transferBudget) else it
            }
            updatedState.teams = finalTeams

            _gameState.value = updatedState
            _isScoutingBusy.value = false
            saveGame()
            showToast("Scout report generated! Deducted £500,000 scout launch fee. (Lv.$scoutingLevel Scouting: $slotsCount profiles found)")
        }
    }

    fun setScoutingFilter(pos: String) {
        _scoutingPositionFilter.value = pos
    }

    // Bid and buy player from scouting
    fun buyPlayer(candidate: ScoutCandidate) {
        val state = _gameState.value ?: return
        if (state.transferBudget < candidate.value) {
            showToast("Bidding failed! Your club requires £${String.format("%,d", candidate.value)} but budget is £${String.format("%,d", state.transferBudget)}.")
            return
        }

        val userTeam = state.teams.first { it.isUserControlled }
        if (userTeam.roster.size >= 25) {
            showToast("Roster full! Max capacity is 25 players. Sell squad depth first.")
            return
        }

        // Add to squad roster
        val newPlayer = Player(
            id = candidate.id,
            name = candidate.name,
            age = candidate.age,
            position = candidate.position,
            ratingAttack = candidate.ratingAttack,
            ratingDefend = candidate.ratingDefend,
            stamina = 100,
            morale = 95,
            value = candidate.value,
            contractWage = candidate.wage,
            form = 7.0f
        )

        val updatedRoster = userTeam.roster + newPlayer
        val nextBudget = state.transferBudget - candidate.value
        val updatedCandidates = state.scoutCandidates.filter { it.id != candidate.id }

        val updatedTeams = state.teams.map {
            if (it.isUserControlled) it.copy(roster = updatedRoster, budget = nextBudget) else it
        }

        val nextInbox = IntroNewsInboxGenerator(state.currentMatchDay, "Transfer Sign-On: ${pPosFullName(newPlayer.position)} ${newPlayer.name}", "Boss,\n\nWe have finalised contract terms with ${newPlayer.name}. Welcome transfer signed in a fee of £${String.format("%,d", candidate.value)}. Supporters are excited! Good luck incorporating them.\n\n- Chief Negotiator")

        val isNewRecord = candidate.value > state.recordSignValue
        val nextState = state.copy(
            teams = updatedTeams,
            scoutCandidates = updatedCandidates,
            transferBudget = nextBudget,
            inbox = listOf(nextInbox) + state.inbox,
            recordSignName = if (isNewRecord) candidate.name else state.recordSignName,
            recordSignValue = if (isNewRecord) candidate.value else state.recordSignValue,
            recordSignClub = if (isNewRecord) userTeam.name else state.recordSignClub
        ).let { s ->
            addFinancialTransaction(s, "Signed player: ${newPlayer.name}", -candidate.value, "TRANSFERS")
        }

        _gameState.value = nextState
        saveGame()
        updateBoardGoalProgress("SIGN_PLAYER", absoluteValue = newPlayer.overallRating)
        showToast("Signed ${candidate.name} for £${String.format("%,d", candidate.value)}!")
    }

    // Sell squad player
    fun sellPlayer(playerId: String) {
        val state = _gameState.value ?: return
        val userTeam = state.teams.first { it.isUserControlled }
        if (userTeam.roster.size <= 14) {
            showToast("Refused! Your squad requires at least 14 players to compete.")
            return
        }

        val playerToSell = userTeam.roster.firstOrNull { it.id == playerId } ?: return
        if (playerId in userTeam.lineupIds) {
            showToast("Remove ${playerToSell.name} from your Starting XI first.")
            return
        }

        val updatedRoster = userTeam.roster.filter { it.id != playerId }
        val saleRevenue = (playerToSell.value * 0.90).toLong() // 10% agent cuts
        val nextBudget = state.transferBudget + saleRevenue

        val updatedTeams = state.teams.map {
            if (it.isUserControlled) it.copy(roster = updatedRoster, budget = nextBudget) else it
        }

        val nextInbox = IntroNewsInboxGenerator(state.currentMatchDay, "Squad Exit: ${playerToSell.name} Sold!", "Manager,\n\nWe successfully negotiated the sale of ${playerToSell.name} to overseas competitors for £${String.format("%,d", saleRevenue)} (after 10% agent commission cuts).\n\nFunds have been added directly to your transfer budget.\n\n- Treasury department")

        val isNewRecord = saleRevenue > state.recordSaleValue
        val nextState = state.copy(
            teams = updatedTeams,
            transferBudget = nextBudget,
            inbox = listOf(nextInbox) + state.inbox,
            recordSaleName = if (isNewRecord) playerToSell.name else state.recordSaleName,
            recordSaleValue = if (isNewRecord) saleRevenue else state.recordSaleValue,
            recordSaleClub = if (isNewRecord) userTeam.name else state.recordSaleClub
        ).let { s ->
            addFinancialTransaction(s, "Sold player: ${playerToSell.name}", saleRevenue, "TRANSFERS")
        }

        _gameState.value = nextState
        saveGame()
        showToast("Sold ${playerToSell.name} for £${String.format("%,d", saleRevenue)}!")
    }

    // LOAN IN A PLAYER Method
    fun loanInPlayer(candidate: ScoutCandidate, weeks: Int = 10) {
        val state = _gameState.value ?: return
        val userTeam = state.teams.first { it.isUserControlled }
        if (userTeam.roster.size >= 25) {
            showToast("Roster full! Max capacity is 25 players.")
            return
        }

        val loanPlayer = Player(
            id = candidate.id,
            name = candidate.name,
            age = candidate.age,
            position = candidate.position,
            ratingAttack = candidate.ratingAttack,
            ratingDefend = candidate.ratingDefend,
            stamina = 100,
            morale = 95,
            value = candidate.value,
            contractWage = candidate.wage,
            form = 7.0f,
            isLoanedIn = true,
            loanSourceClub = "Overseas Club",
            loanRemainingWeeks = weeks
        )

        val updatedRoster = userTeam.roster + loanPlayer
        val updatedTeams = state.teams.map {
            if (it.isUserControlled) it.copy(roster = updatedRoster) else it
        }
        val updatedCandidates = state.scoutCandidates.filter { it.id != candidate.id }

        val nextInbox = IntroNewsInboxGenerator(
            state.currentMatchDay,
            "Incoming Loan Deal: ${loanPlayer.name} Signed!",
            "Manager,\n\nWe successfully closed a short-term LOAN deal for ${loanPlayer.name} (${loanPlayer.position}) from our affiliate club for a duration of $weeks Match Weeks.\n\nWe will list them on our active tactics roster. Best of luck!\n\n- Loan Coordinator"
        )

        val nextState = state.copy(
            teams = updatedTeams,
            scoutCandidates = updatedCandidates,
            inbox = listOf(nextInbox) + state.inbox
        )
        _gameState.value = nextState
        saveGame()
        showToast("Signed ${candidate.name} on a $weeks-week loan!")
    }

    // LOAN OUT A PLAYER Method
    fun loanOutPlayer(playerId: String, weeks: Int = 10) {
        val state = _gameState.value ?: return
        val userTeam = state.teams.first { it.isUserControlled }
        if (userTeam.roster.size <= 14) {
            showToast("Refused! Your squad requires at least 14 players to compete.")
            return
        }
        val p = userTeam.roster.firstOrNull { it.id == playerId } ?: return
        if (playerId in userTeam.lineupIds) {
            showToast("Remove ${p.name} from your Starting XI first.")
            return
        }

        val destinationOptions = listOf("Rangers", "Celtic", "Preston NE", "Stoke City", "Blackburn Rovers")
        val destClub = destinationOptions.random()

        val updatedRoster = userTeam.roster.map {
            if (it.id == playerId) {
                it.copy(
                    isLoanedOut = true,
                    loanDestinationClub = destClub,
                    loanRemainingWeeks = weeks
                )
            } else it
        }

        val updatedTeams = state.teams.map {
            if (it.isUserControlled) it.copy(roster = updatedRoster) else it
        }

        val nextInbox = IntroNewsInboxGenerator(
            state.currentMatchDay,
            "Outgoing Loan: ${p.name} joined $destClub!",
            "Boss,\n\nWe have finalized the paperwork. ${p.name} has joined $destClub on a $weeks-week loan deal. Their club will cover 100% of the player's £${String.format("%,d", p.contractWage)} weekly wages.\n\nGood for their development!\n\n- Academy Loan Desk"
        )

        val nextState = state.copy(
            teams = updatedTeams,
            inbox = listOf(nextInbox) + state.inbox
        )
        _gameState.value = nextState
        saveGame()
        showToast("Loaned ${p.name} to $destClub for $weeks weeks!")
    }

    fun listPlayerForSale(playerId: String, askingPrice: Long) {
        val state = _gameState.value ?: return
        val updatedTeams = state.teams.map { team ->
            if (team.isUserControlled) {
                val updatedRoster = team.roster.map { p ->
                    if (p.id == playerId) {
                        p.copy(isListed = true, askingPrice = askingPrice)
                    } else p
                }
                team.copy(roster = updatedRoster)
            } else team
        }
        _gameState.value = state.copy(teams = updatedTeams)
        saveGame()
        showToast("Player listed on the Transfer Market!")
    }

    fun delistPlayer(playerId: String) {
        val state = _gameState.value ?: return
        val updatedTeams = state.teams.map { team ->
            if (team.isUserControlled) {
                val updatedRoster = team.roster.map { p ->
                    if (p.id == playerId) {
                        p.copy(isListed = false, askingPrice = 0L)
                    } else p
                }
                team.copy(roster = updatedRoster)
            } else team
        }
        _gameState.value = state.copy(teams = updatedTeams)
        saveGame()
        showToast("Player delisted off the market.")
    }

    fun renewContract(playerId: String) {
        val state = _gameState.value ?: return
        val userTeam = state.teams.first { it.isUserControlled }
        val player = userTeam.roster.firstOrNull { it.id == playerId } ?: return

        val cost = (player.value * 0.15).toLong().coerceAtLeast(500_000L) // 15% sign-on fee
        if (state.transferBudget < cost) {
            showToast("Required sign-on bonus: £${String.format("%,d", cost)} but budget is too low!")
            return
        }

        val updatedRoster = userTeam.roster.map { p ->
            if (p.id == playerId) {
                p.copy(
                    contractYearsRemaining = p.contractYearsRemaining + 2,
                    morale = (p.morale + 15).coerceAtMost(100)
                )
            } else p
        }

        val nextBudget = state.transferBudget - cost
        val updatedTeams = state.teams.map {
            if (it.isUserControlled) it.copy(roster = updatedRoster, budget = nextBudget) else it
        }

        _gameState.value = state.copy(
            teams = updatedTeams,
            transferBudget = nextBudget
        )
        saveGame()
        showToast("Renewed contract for +2 years!")
    }

    fun bidOnCpuPlayer(playerId: String, targetTeamId: String, offerAmount: Long) {
        val state = _gameState.value ?: return
        if (state.transferBudget < offerAmount) {
            showToast("Bidding failed! Offer exceeds your transfer budget.")
            return
        }

        val targetTeam = state.teams.firstOrNull { it.id == targetTeamId } ?: return
        val player = targetTeam.roster.firstOrNull { it.id == playerId } ?: return

        val userTeam = state.teams.first { it.isUserControlled }
        if (userTeam.roster.size >= 25) {
            showToast("Roster full! Max capacity is 25 players.")
            return
        }

        val askingPrice = if (player.askingPrice > 0) player.askingPrice else player.value

        when {
            offerAmount >= askingPrice -> {
                // Transfer player immediately!
                val transferredPlayer = player.copy(
                    isListed = false,
                    askingPrice = 0L,
                    contractYearsRemaining = Random.nextInt(2, 5)
                )

                val updatedUserRoster = userTeam.roster + transferredPlayer
                val updatedTargetRoster = targetTeam.roster.filter { it.id != playerId }
                val targetLineup = targetTeam.lineupIds.filter { it != playerId }

                val updatedTeams = state.teams.map { team ->
                    when (team.id) {
                        userTeam.id -> team.copy(roster = updatedUserRoster, budget = team.budget - offerAmount)
                        targetTeam.id -> team.copy(roster = updatedTargetRoster, budget = team.budget + offerAmount, lineupIds = targetLineup)
                        else -> team
                    }
                }

                val welcomeMessage = InboxMessage(
                    id = "msg_sign_${System.currentTimeMillis()}",
                    sender = "Transfer Desk",
                    subject = "SQUAD INBOUND: Welcome ${transferredPlayer.name}!",
                    content = "Dear Manager,\n\nWe have successfully purchased ${transferredPlayer.name} from ${targetTeam.name} for £${String.format("%,d", offerAmount)}!\n\nThe player has joined our squad and is ready for training.\n\n- Transfer Desk"
                )

                _gameState.value = state.copy(
                    teams = updatedTeams,
                    transferBudget = state.transferBudget - offerAmount,
                    inbox = listOf(welcomeMessage) + state.inbox
                )
                saveGame()
                showToast("Signed ${player.name} from ${targetTeam.name}!")
            }
            offerAmount >= (askingPrice * 0.85).toLong() -> {
                val counter = (askingPrice * Random.nextDouble(0.96, 1.0)).toLong()
                showToast("Stalled! Counter-offer demands £${String.format("%,d", counter)}.")
            }
            else -> {
                showToast("Bid Rejected! ${targetTeam.name} refused our offer.")
            }
        }
    }

    fun acceptUserIncomingBid(bid: TransferBid) {
        val state = _gameState.value ?: return
        val userTeam = state.teams.first { it.isUserControlled }
        if (userTeam.roster.size <= 14) {
            showToast("Refused! Your squad requires at least 14 players to compete.")
            return
        }

        val player = userTeam.roster.firstOrNull { it.id == bid.playerId } ?: return
        if (player.id in userTeam.lineupIds) {
            showToast("Remove ${player.name} from your Starting XI first.")
            return
        }

        val buyerTeam = state.teams.firstOrNull { it.id == bid.offeringTeamId } ?: return

        // Remove from user, add to buyer
        val updatedUserRoster = userTeam.roster.filter { it.id != bid.playerId }
        val updatedBuyerRoster = buyerTeam.roster + player.copy(
            isListed = false,
            askingPrice = 0L,
            contractYearsRemaining = Random.nextInt(2, 5)
        )

        // financial update
        val nextBudget = state.transferBudget + bid.amount
        val buyerBudget = buyerTeam.budget - bid.amount

        val updatedTeams = state.teams.map { team ->
            when (team.id) {
                userTeam.id -> team.copy(roster = updatedUserRoster, budget = nextBudget)
                buyerTeam.id -> team.copy(roster = updatedBuyerRoster, budget = buyerBudget)
                else -> team
            }
        }

        val updatedBids = state.pendingBids.filter { it.playerId != bid.playerId }

        val inboxConfirmation = InboxMessage(
            id = "msg_ac_${System.currentTimeMillis()}",
            sender = "Treasury department",
            subject = "COMPLETED TRANSFER EXIT: ${player.name}",
            content = "Boss,\n\nWe confirm the transfer sale of ${player.name} to ${buyerTeam.name} has been processed.\n\nIncoming funds of £${String.format("%,d", bid.amount)} are securely added to our bank account.\n\n- Chief Financial Officer"
        )

        _gameState.value = state.copy(
            teams = updatedTeams,
            transferBudget = nextBudget,
            pendingBids = updatedBids,
            inbox = listOf(inboxConfirmation) + state.inbox
        )
        saveGame()
        showToast("Sold ${player.name} to ${buyerTeam.name}!")
    }

    fun declineUserIncomingBid(bidId: String) {
        val state = _gameState.value ?: return
        val updatedBids = state.pendingBids.filter { it.id != bidId }
        _gameState.value = state.copy(pendingBids = updatedBids)
        saveGame()
        showToast("Offer declined.")
    }

    private fun simulateAiTransfersAndBids(state: SaveState): SaveState {
        val updatedTeams = state.teams.map { it.copy(roster = it.roster.toList()) }.toMutableList()
        val mutableBids = state.pendingBids.toMutableList()
        val nextInboxMessages = mutableListOf<InboxMessage>()

        // 1. AI Teams listing decisions
        updatedTeams.forEachIndexed { i, team ->
            if (!team.isUserControlled && team.roster.size > 16) {
                if (Random.nextDouble() < 0.20) {
                    val surplus = team.roster
                        .filter { it.id !in team.lineupIds && !it.isListed && it.position != "GK" }
                        .minByOrNull { it.overallRating }
                    if (surplus != null) {
                        surplus.isListed = true
                        surplus.askingPrice = (surplus.value * Random.nextDouble(0.85, 1.15)).toLong()
                    }
                }
            }
        }

        // 2. AI Teams buying decisions
        if (Random.nextDouble() < 0.25) {
            val potentialBuyers = updatedTeams.filter { !it.isUserControlled && it.budget > 15_000_000L && it.roster.size < 22 }
            if (potentialBuyers.isNotEmpty()) {
                val buyer = potentialBuyers.random()
                val listedPool = updatedTeams
                    .filter { !it.isUserControlled && it.id != buyer.id }
                    .flatMap { t -> t.roster.filter { it.isListed }.map { p -> Pair(t, p) } }

                if (listedPool.isNotEmpty()) {
                    val (seller, playerToBuy) = listedPool.random()
                    if (buyer.budget >= playerToBuy.askingPrice) {
                        // CPU trade
                        buyer.budget -= playerToBuy.askingPrice
                        buyer.roster = buyer.roster + playerToBuy.copy(isListed = false, askingPrice = 0L)
                        
                        seller.budget += playerToBuy.askingPrice
                        seller.roster = seller.roster.filter { it.id != playerToBuy.id }
                        seller.lineupIds = seller.lineupIds.filter { it != playerToBuy.id }

                        nextInboxMessages.add(InboxMessage(
                            id = "msg_cpu_tx_${System.currentTimeMillis()}",
                            sender = "League News Feed",
                            subject = "CPU Transfer: ${playerToBuy.name}",
                            content = "League report:\n\n${buyer.name} has signed ${playerToBuy.name} from rivals ${seller.name} for £${String.format("%.1f", playerToBuy.askingPrice.toDouble() / 1_000_000)}M!"
                        ))
                    }
                }
            }
        }

        // 3. AI Offers on User's Listed Players!
        val userTeam = updatedTeams.first { it.isUserControlled }
        val userListed = userTeam.roster.filter { it.isListed }
        userListed.forEach { player ->
            if (Random.nextDouble() < 0.40) {
                val buyers = updatedTeams.filter { !it.isUserControlled && it.budget >= (player.askingPrice * 0.85).toLong() && it.roster.size < 25 }
                if (buyers.isNotEmpty()) {
                    val buyer = buyers.random()
                    val bidAmount = (player.askingPrice * Random.nextDouble(0.88, 1.05)).toLong()
                    
                    val bidId = "bid_${UUID.randomUUID()}"
                    val newBid = TransferBid(
                        id = bidId,
                        playerId = player.id,
                        playerName = player.name,
                        playerRating = player.overallRating,
                        playerPosition = player.position,
                        offeringTeamId = buyer.id,
                        offeringTeamName = buyer.name,
                        targetTeamId = userTeam.id,
                        amount = bidAmount,
                        isFromUser = false,
                        status = "PENDING"
                    )
                    mutableBids.add(newBid)

                    val news = InboxMessage(
                        id = "msg_bid_${System.currentTimeMillis()}_${player.id}",
                        sender = buyer.name,
                        subject = "TRANSFER OFFER: £${String.format("%.1f", bidAmount.toDouble() / 1_000_000)}M for ${player.name}",
                        content = "Manager,\n\nWe have submitted a formal transfer offer of £${String.format("%,d", bidAmount)} for ${player.name}.\n\nGo to Scouting & Transfers to sign off.\n\n- ${buyer.name}"
                    )
                    nextInboxMessages.add(news)
                }
            }
        }

        return state.copy(
            teams = updatedTeams,
            pendingBids = mutableBids,
            inbox = nextInboxMessages + state.inbox
        )
    }

    fun startNextSeason() {
        val state = _gameState.value ?: return
        if (state.isUserSacked) {
            showToast("⚠️ You must accept a Job Offer before starting the next campaign!")
            return
        }
        
        val nextYear = state.gameYear + 1
        val retiredList = mutableListOf<String>()
        val expiredList = mutableListOf<String>()
        
        val updatedTeams = state.teams.map { team ->
            val updatedRoster = team.roster.map { p ->
                val nextAge = p.age + 1
                val nextContract = (p.contractYearsRemaining - 1).coerceAtLeast(0)
                p.copy(
                    age = nextAge,
                    contractYearsRemaining = nextContract,
                    goalsScored = 0,
                    assists = 0,
                    cleanSheets = 0,
                    matchesPlayed = 0,
                    stamina = 100,
                    fatigue = 0
                )
            }.filter { p ->
                // Check retirement
                val retirementChance = when {
                    p.age >= 35 -> 1.0
                    p.age >= 34 -> 0.70
                    p.age >= 33 -> 0.45
                    p.age >= 32 -> 0.20
                    else -> 0.0
                }
                val retires = Random.nextDouble() < retirementChance
                if (retires) {
                    retiredList.add("${p.name} (${p.position}, age ${p.age})")
                    false
                } else if (p.contractYearsRemaining <= 0) {
                    expiredList.add("${p.name} (${p.position}, age ${p.age})")
                    false
                } else {
                    true
                }
            }.toMutableList()
            
            while (updatedRoster.size < 15) {
                val positions = listOf("GK", "DEF", "MID", "FWD")
                val missingPos = positions.random()
                val academyBoost = (team.academyLevel - 1) * 3
                val qualityMin = if (team.isUserControlled) (70 + academyBoost) else 64
                val qualityMax = if (team.isUserControlled) (78 + academyBoost) else 75
                val youth = GameEngine.generatePlayer(missingPos, qualityMin, qualityMax).copy(
                    age = Random.nextInt(18, 20),
                    contractYearsRemaining = Random.nextInt(3, 6)
                )
                updatedRoster.add(youth)
            }
            
            val (newXI, _) = GameEngine.selectAutoLineup(updatedRoster, team.formation)
            
            team.copy(
                roster = updatedRoster,
                lineupIds = newXI.map { it.id },
                captainId = team.captainId?.let { capId ->
                    if (updatedRoster.any { it.id == capId }) capId else newXI.firstOrNull()?.id
                } ?: newXI.firstOrNull()?.id
            )
        }
        
        val newTable = updatedTeams.map { LeagueStanding(teamId = it.id, teamName = it.name) }
        val nextFixtures = GameEngine.generateFixturesList(updatedTeams)
        val freshScouted = List(6) { GameEngine.generateScoutCandidate() }
        
        val retiredContent = if (retiredList.isNotEmpty()) {
            "Retired Players:\n" + retiredList.joinToString("\n") { "• $it" }
        } else {
            "No players retired."
        }
        
        val expiredContent = if (expiredList.isNotEmpty()) {
            "Players whose contracts expired & left:\n" + expiredList.joinToString("\n") { "• $it" }
        } else {
            "No contracts expired."
        }
        
        val seasonMessage = InboxMessage(
            id = "msg_season_${System.currentTimeMillis()}",
            sender = "Chairman",
            subject = "Season $nextYear Board Directive",
            content = "Boss,\n\nWelcome to the $nextYear season!\n\n$retiredContent\n\n$expiredContent\n\nYouth squad reserve promotions have stabilized our depths.\n\nGood luck,\n- The Board"
        )
        
        val decayedSponsors = state.activeSponsorships.map { sponsor ->
            sponsor.copy(seasonsRemaining = sponsor.seasonsRemaining - 1)
        }.filter { it.seasonsRemaining > 0 }

        val freshOffers = GameEngine.generateSponsorshipOffers()
        val freshGoals = GameEngine.generateBoardGoals()

        val nextState = state.copy(
            currentMatchDay = 1,
            standings = newTable,
            fixtures = nextFixtures,
            scoutCandidates = freshScouted,
            inbox = listOf(seasonMessage) + state.inbox,
            gameYear = nextYear,
            isCompleted = false,
            pendingBids = emptyList(),
            activeSponsorships = decayedSponsors,
            availableSponsorshipOffers = freshOffers,
            activeBoardGoals = freshGoals
        )
        
        _gameState.value = nextState
        saveGame()
        _currentScreen.value = Screen.Dashboard
        showToast("Season $nextYear has commenced!")
    }

    private fun pPosFullName(pos: String) = when(pos) {
        "GK" -> "Goalkeeper"
        "DEF" -> "Defender"
        "MID" -> "Midfielder"
        "FWD" -> "Striker"
        else -> pos
    }

    // Match Simulation management
    fun launchNextMatch() {
        val state = _gameState.value ?: return
        val userTeam = state.teams.firstOrNull { it.id == state.userTeamId } ?: return
        val injuredStarters = userTeam.getStartingXI().filter { it.injuryWeeksRemaining > 0 }
        
        if (injuredStarters.isNotEmpty()) {
            showToast("⚠️ Substitute out injured starting players: ${injuredStarters.joinToString { it.name }}")
            return
        }

        val fixture = state.fixtures.firstOrNull { 
            it.matchDay == state.currentMatchDay && 
            (it.homeTeamId == state.userTeamId || it.awayTeamId == state.userTeamId) 
        } ?: return

        val homeTeam = state.teams.first { it.id == fixture.homeTeamId }
        val awayTeam = state.teams.first { it.id == fixture.awayTeamId }
        val (isRivalry, rivalryType) = GameEngine.determineRivalry(homeTeam, awayTeam, state.standings)
        fixture.isRivalryMatch = isRivalry
        fixture.rivalryType = rivalryType

        _liveMatchFixture.value = fixture
        _liveMatchMinute.value = 0
        _liveHomeScore.value = 0
        _liveAwayScore.value = 0
        _matchStatus.value = "PRE_MATCH"
        _liveCommentary.value = listOf(
            MatchCommentaryEvent(
                0,
                "INFO",
                if (isRivalry) "0' | 🔥 DUST AND FIRE! Welcome to the $rivalryType matching ${fixture.homeTeamName} against ${fixture.awayTeamName}! The atmosphere in the stadium is absolutely electric with rivalry tension!"
                else "0' | 🏟️ Welcome! Squads are lining up on the pitch. Pitch looks perfect tonight.",
                0,
                0
            )
        )
        _isFastForward.value = false
        _currentScreen.value = Screen.ActiveMatch
    }

    fun toggleFastForward() {
        _isFastForward.value = !_isFastForward.value
    }

    // Coroutine loop match simulation
    fun startLiveMatchSimulation() {
        if (_matchStatus.value != "PRE_MATCH" && _matchStatus.value != "HALF_TIME") return
        
        val isSecondHalf = _matchStatus.value == "HALF_TIME"
        _matchStatus.value = "PLAYING"

        val state = _gameState.value ?: return
        val homeTeam = state.teams.first { it.id == _liveMatchFixture.value?.homeTeamId }
        val awayTeam = state.teams.first { it.id == _liveMatchFixture.value?.awayTeamId }
        val pitchCondition = _liveMatchFixture.value?.pitchCondition ?: "EXCELLENT"

        // Play Kickoff referee whistle blow!
        com.example.utils.SoundEngine.playWhistle()

        matchSimulationJob = viewModelScope.launch {
            val startMin = if (isSecondHalf) 46 else 1
            val endMin = if (isSecondHalf) 90 else 45

            for (m in startMin..endMin) {
                _liveMatchMinute.value = m
                
                // Live minute engine
                val event = GameEngine.simulateLiveMinute(
                    m, homeTeam, awayTeam, _liveHomeScore.value, _liveAwayScore.value, pitchCondition,
                    isRivalry = _liveMatchFixture.value?.isRivalryMatch == true
                )

                if (event != null) {
                    _liveCommentary.value = _liveCommentary.value + event
                    if (event.type == "GOAL") {
                        _liveHomeScore.value = event.currentHomeScore
                        _liveAwayScore.value = event.currentAwayScore
                        // Play epic synthetic goal celebration tone!
                        com.example.utils.SoundEngine.playGoalCheer()
                    }
                }

                // Stamina Decay start-XI
                decayStamina(homeTeam, awayTeam, pitchCondition, isRivalry = _liveMatchFixture.value?.isRivalryMatch == true)

                // Sim speed
                val speed = if (_isFastForward.value) 250L else 700L
                delay(speed)
            }

            if (endMin == 45) {
                _matchStatus.value = "HALF_TIME"
                // Play half time whistle
                com.example.utils.SoundEngine.playWhistle()
                _liveCommentary.value = _liveCommentary.value + MatchCommentaryEvent(45, "INFO", "45' | 🔔 Half time! Players are walking into the dressing rooms. Change tactics if needed.", _liveHomeScore.value, _liveAwayScore.value)
            } else {
                _matchStatus.value = "FULL_TIME"
                // Play final full time whistle
                com.example.utils.SoundEngine.playWhistle()
                _liveCommentary.value = _liveCommentary.value + MatchCommentaryEvent(90, "INFO", "90' | 🏁 FULL TIME whistles blow! Final results are secured. Press Continue to lock it in.", _liveHomeScore.value, _liveAwayScore.value)
                saveCompletedMatchToFixtures()
            }
        }
    }

    private fun decayStamina(home: Team, away: Team, pitchCondition: String = "EXCELLENT", isRivalry: Boolean = false) {
        var multiplier = when (pitchCondition) {
            "SOGGY" -> 1.2
            "MUDDY" -> 1.5
            "FROZEN" -> 1.2
            else -> 1.0
        }
        if (isRivalry) {
            multiplier *= 1.15
        }
        val hIntensityFactor = when (home.pressingIntensity) {
            "AGGRESSIVE" -> 0.45
            "CONSERVATIVE" -> 0.15
            else -> 0.28
        } * multiplier
        val aIntensityFactor = when (away.pressingIntensity) {
            "AGGRESSIVE" -> 0.45
            "CONSERVATIVE" -> 0.15
            else -> 0.28
        } * multiplier

        home.getStartingXI().forEach {
            it.stamina = (it.stamina - hIntensityFactor.toInt().coerceAtLeast(1)).coerceIn(15, 100)
        }
        away.getStartingXI().forEach {
            it.stamina = (it.stamina - aIntensityFactor.toInt().coerceAtLeast(1)).coerceIn(15, 100)
        }
    }

    fun pauseLiveMatch() {
        matchSimulationJob?.cancel()
        _matchStatus.value = "HALF_TIME"
    }

    private fun saveCompletedMatchToFixtures() {
        val state = _gameState.value ?: return
        val fixtureId = _liveMatchFixture.value?.id ?: return
        val hScore = _liveHomeScore.value
        val aScore = _liveAwayScore.value
        val homeTeam = state.teams.first { it.id == _liveMatchFixture.value?.homeTeamId }
        val awayTeam = state.teams.first { it.id == _liveMatchFixture.value?.awayTeamId }

        // update fixture
        val finalEvents = _liveCommentary.value

        var finalFixt = _liveMatchFixture.value
        val updatedFixtures = state.fixtures.map { f ->
            if (f.id == fixtureId) {
                val base = f.copy(isPlayed = true, homeScore = hScore, awayScore = aScore, eventsLog = finalEvents)
                val filled = GameEngine.fillMatchStats(base, homeTeam, awayTeam)
                val recapText = GameEngine.generateMatchRecap(filled, homeTeam, awayTeam, emptyList())
                val updated = filled.copy(highlightRecap = recapText)
                finalFixt = updated
                updated
            } else f
        }

        _liveMatchFixture.value = finalFixt
        _gameState.value = state.copy(fixtures = updatedFixtures)
    }

    // Complete the Match-Day and auto-run CPU scores
    fun advanceMatchDay() {
        val state = _gameState.value ?: return
        val mDay = state.currentMatchDay

        // Auto run independent CPU vs CPU fixtures for this matchday
        val updatedFixtures = state.fixtures.map { f ->
            if (f.matchDay == mDay && !f.isPlayed) {
                val fHome = state.teams.first { it.id == f.homeTeamId }
                val fAway = state.teams.first { it.id == f.awayTeamId }
                val (hScore, aScore) = GameEngine.runCpuMatchEndScore(fHome, fAway, f.pitchCondition)
                val base = f.copy(isPlayed = true, homeScore = hScore, awayScore = aScore)
                GameEngine.fillMatchStats(base, fHome, fAway)
            } else f
        }

        // Apply Player Stats for all fixtures on this MatchDay (Both user match and auto-run CPU matches)
        updatedFixtures.filter { f -> f.matchDay == mDay && f.isPlayed }.forEach { f ->
            val fHome = state.teams.first { it.id == f.homeTeamId }
            val fAway = state.teams.first { it.id == f.awayTeamId }
            distributePlayerStatsForFixture(f, fHome, fAway)
        }

        // Recalculate League standings
        val newTable = state.teams.map { LeagueStanding(teamId = it.id, teamName = it.name) }
        updatedFixtures.filter { f -> f.matchDay <= mDay && f.isPlayed }.forEach { f ->
            val hStanding = newTable.first { it.teamId == f.homeTeamId }
            val aStanding = newTable.first { it.teamId == f.awayTeamId }

            hStanding.played++
            aStanding.played++

            hStanding.goalsFor += f.homeScore
            hStanding.goalsAgainst += f.awayScore
            aStanding.goalsFor += f.awayScore
            aStanding.goalsAgainst += f.homeScore

            when {
                f.homeScore > f.awayScore -> {
                    hStanding.won++
                    hStanding.points += 3
                    aStanding.lost++
                }
                f.homeScore < f.awayScore -> {
                    aStanding.won++
                    aStanding.points += 3
                    hStanding.lost++
                }
                else -> {
                    hStanding.drawn++
                    hStanding.points += 1
                    aStanding.drawn++
                    aStanding.points += 1
                }
            }
        }

        // Recover stamina on bench, decrement remaining injury durations, and check matchday physical injuries
        val injuredReportsList = mutableListOf<String>()
        state.teams.forEach { t ->
            val playedFixture = updatedFixtures.firstOrNull { f ->
                f.matchDay == mDay && (f.homeTeamId == t.id || f.awayTeamId == t.id)
            }
            val pitch = playedFixture?.pitchCondition ?: "EXCELLENT"
            val baseInjuryRate = when (pitch) {
                "SOGGY" -> 0.05
                "MUDDY" -> 0.12
                "FROZEN" -> 0.08
                else -> 0.02
            }
            // Medical facility lowers the chance of matchday injuries
            val injuryRate = baseInjuryRate / (1.0 + (t.medicalLevel - 1) * 0.35)

            var finalInjuryRate = injuryRate
            var doubleHealBonus = false
            if (t.isUserControlled) {
                val hasMilo = state.hiredStaff.any { it.id == "staff_physio_masseur" }
                val hasEva = state.hiredStaff.any { it.id == "staff_physio_doctor" }
                if (hasMilo) finalInjuryRate *= 0.8 // -20% injury chance
                if (hasEva) doubleHealBonus = true
            }

            t.roster.forEach { p ->
                // First, countdown existing injuries. Higher medical facility increases recovery speed
                if (p.injuryWeeksRemaining > 0) {
                    val doubleRecoverChance = (t.medicalLevel - 1) * 0.12 + (if (doubleHealBonus) 0.50 else 0.0) // Eva adds +50% double-recover chance
                    val healedAmount = if (Random.nextDouble() < doubleRecoverChance) 2 else 1
                    p.injuryWeeksRemaining = (p.injuryWeeksRemaining - healedAmount).coerceAtLeast(0)
                    if (p.injuryWeeksRemaining == 0) {
                        p.injuryType = null
                    }
                }

                if (p.id in t.lineupIds) {
                    p.morale = (p.morale + Random.nextInt(-3, 6)).coerceIn(30, 99)
                    p.matchesPlayed++ // Active starting XI player increments matches played
                    
                    // Fatigue accumulation
                    p.fatigue = (p.fatigue + Random.nextInt(10, 18)).coerceIn(0, 100)

                    // Stamina & fatigue physical factors affecting injury rate
                    val staminaInjuryFactor = if (p.stamina < 75) 1.0 + (75 - p.stamina) * 0.08 else 1.0
                    val fatigueInjuryFactor = 1.0 + (p.fatigue / 20.0)
                    val ageInjuryFactor = if (p.age > 28) 1.0 + (p.age - 28) * 0.06 else 1.0
                    val configIntensityFactor = when (t.pressingIntensity) {
                        "AGGRESSIVE" -> 1.4
                        "CONSERVATIVE" -> 0.7
                        else -> 1.0
                    }
                    val playerInjuryChance = (finalInjuryRate * staminaInjuryFactor * fatigueInjuryFactor * ageInjuryFactor * configIntensityFactor).coerceAtMost(0.40)

                    // Pitch physical injury hazard check (only if they aren't already injured)
                    if (p.injuryWeeksRemaining == 0 && Random.nextDouble() < playerInjuryChance) {
                        val duration = Random.nextInt(1, 4) + (if (p.age > 30) 1 else 0) + (if (p.fatigue > 60) 1 else 0) // Extra length for fatigue/age
                        val type = when (p.position) {
                            "GK" -> listOf("Wrist Sprain", "Shoulder Dislocation", "Finger Fracture", "Bruised Ribs").random()
                            "DEF" -> listOf("Sprained Ankle", "Knee Strain", "Hamstring Pull", "Calf Strain").random()
                            "MID" -> listOf("Hamstring Pull", "Ankle Sprain", "Groin Strain", "Knee Strain").random()
                            else -> listOf("Hamstring Tear", "Ankle Sprain", "Knee Strain", "Thigh Strain").random()
                        }
                        p.injuryWeeksRemaining = duration
                        p.injuryType = type
                        p.stamina = (p.stamina - 40).coerceAtLeast(10)
                        
                        if (t.isUserControlled) {
                            injuredReportsList.add("• **${p.name}** (${p.position}) has sustained a *${type}* and is OUT for **$duration weeks** (Exhaustion level: ${p.fatigue}%).")
                        }
                    }
                } else {
                    // Rest reduces fatigue
                    p.fatigue = (p.fatigue - Random.nextInt(15, 25)).coerceIn(0, 100)
                    val trainingBoost = (t.trainingLevel - 1) * 5
                    var staminaRec = 35 + trainingBoost
                    if (t.isUserControlled) {
                        val hasAlex = state.hiredStaff.any { it.id == "staff_coach_stamina" }
                        val hasJulio = state.hiredStaff.any { it.id == "staff_coach_academy" }
                        if (hasAlex) staminaRec += 10
                        if (hasJulio && Random.nextDouble() < 0.20) {
                            // Julio training boost: boost random skill
                            val pCopy = p
                            val ratingAttackCopy = pCopy.ratingAttack
                            val ratingDefendCopy = pCopy.ratingDefend
                            if (Random.nextBoolean()) {
                                if (ratingAttackCopy < 95) {
                                    val prop = pCopy::class.java.getDeclaredField("ratingAttack")
                                    prop.isAccessible = true
                                    prop.set(pCopy, ratingAttackCopy + 1)
                                }
                            } else {
                                if (ratingDefendCopy < 95) {
                                    val prop = pCopy::class.java.getDeclaredField("ratingDefend")
                                    prop.isAccessible = true
                                    prop.set(pCopy, ratingDefendCopy + 1)
                                }
                            }
                        }
                    }
                    if (p.injuryWeeksRemaining == 0) {
                        p.stamina = (p.stamina + staminaRec).coerceAtMost(100) // healthy rest recovers stamina
                    } else {
                        val rehabBoost = (t.medicalLevel - 1) * 3
                        p.stamina = (p.stamina + 15 + rehabBoost).coerceAtMost(100) // injured rehab recovery is slower
                    }
                    p.morale = (p.morale + Random.nextInt(-1, 3)).coerceIn(30, 99)
                }
                // Set form slightly randomized based on match scores
                p.form = (p.form * 0.7f + Random.nextDouble(5.5, 8.5).toFloat() * 0.3f).coerceIn(4.0f, 10.0f)
            }

            // Decrease loan durations
            val loanReturnedPlayerNames = mutableListOf<String>()
            val loanExpiredPlayerNames = mutableListOf<String>()

            t.roster.forEach { p ->
                if ((p.isLoanedIn || p.isLoanedOut) && p.loanRemainingWeeks > 0) {
                    p.loanRemainingWeeks--
                }
            }

            // Filter out expired loaned-in players
            val expiredLoanInIds = t.roster.filter { it.isLoanedIn && it.loanRemainingWeeks == 0 }.map { it.id }
            if (expiredLoanInIds.isNotEmpty()) {
                t.roster.filter { it.isLoanedIn && it.loanRemainingWeeks == 0 }.forEach {
                    loanExpiredPlayerNames.add(it.name)
                }
                t.roster = t.roster.filter { it.id !in expiredLoanInIds }
                t.lineupIds = t.lineupIds.filter { it !in expiredLoanInIds }
            }

            // Return loaned-out players
            t.roster.forEach { p ->
                if (p.isLoanedOut && p.loanRemainingWeeks == 0) {
                    p.isLoanedOut = false
                    loanReturnedPlayerNames.add(p.name)
                }
            }

            if (t.isUserControlled) {
                loanExpiredPlayerNames.forEach { name ->
                    injuredReportsList.add("• **Loan Expired**: $name has returned to their parent club.")
                }
                loanReturnedPlayerNames.forEach { name ->
                    injuredReportsList.add("• **Loan Return**: $name is back from loan and available in our squad.")
                }
            }

            // Remove newly injured starting XI players so they don't start automatically next week
            val injuredStarterIds = t.roster.filter { it.injuryWeeksRemaining > 0 }.map { it.id }
            if (injuredStarterIds.isNotEmpty()) {
                t.lineupIds = t.lineupIds.filter { it !in injuredStarterIds }
                
                // If they are not user-controlled, CPU will automatically pick healthy replacements later during auto-lineup
            }
        }

        // Set goals scored in user match
        val userFixt = updatedFixtures.firstOrNull { f -> f.matchDay == mDay && (f.homeTeamId == state.userTeamId || f.awayTeamId == state.userTeamId) }
        var winsInc = 0
        var lossesInc = 0
        var drawsInc = 0
        var goalsForInc = 0
        var goalsAgainstInc = 0

        if (userFixt != null) {
            val isHome = userFixt.homeTeamId == state.userTeamId
            val scored = if (isHome) userFixt.homeScore else userFixt.awayScore
            val conceded = if (isHome) userFixt.awayScore else userFixt.homeScore
            
            goalsForInc = scored
            goalsAgainstInc = conceded
            when {
                scored > conceded -> winsInc = 1
                scored < conceded -> lossesInc = 1
                else -> drawsInc = 1
            }
        }

        val nextMatchDay = mDay + 1
        val isSeasonOver = nextMatchDay > 14

        // Generate and update highlight recaps including any logged physical dynamic injuries
        val updatedFixturesWithRecaps = updatedFixtures.map { f ->
            if (f.matchDay == mDay) {
                val fHome = state.teams.first { it.id == f.homeTeamId }
                val fAway = state.teams.first { it.id == f.awayTeamId }
                val isUserMatch = (f.homeTeamId == state.userTeamId || f.awayTeamId == state.userTeamId)
                val injuryList = if (isUserMatch) injuredReportsList else emptyList()
                val recapText = GameEngine.generateMatchRecap(f, fHome, fAway, injuryList)
                f.copy(highlightRecap = recapText)
            } else f
        }

        // Build temporary state to feed into revenue process
        val tempState = state.copy(fixtures = updatedFixturesWithRecaps)
        val stateWithRevenue = processMatchdayRevenues(tempState, mDay)

        // Gather user fixture for revenue summary details added to Inbox message
        val userRevFixt = stateWithRevenue.fixtures.firstOrNull { f -> f.matchDay == mDay && (f.homeTeamId == state.userTeamId || f.awayTeamId == state.userTeamId) }
        val revenueSummaryPart = if (userRevFixt != null) {
            val isHome = userRevFixt.homeTeamId == state.userTeamId
            if (isHome) {
                "\n\n🏟️ HOME GATE REVENUE:\nWe generated £${String.format("%,d", userRevFixt.revenueGenerated)} from an attendance of ${String.format("%,d", userRevFixt.stadiumAttendance)} fans at ${state.teams.first { it.isUserControlled }.stadiumName}! This has been credited to your Transfer Budget."
            } else {
                "\n\n🏟️ AWAY MATCH DELEGATION:\nWe played away at ${userRevFixt.homeTeamName}. The home side claimed matchday ticket receipts. Supporter mood adjustments have been initialized."
            }
        } else ""

        // Weekly Inbox compilation
        val userStand = newTable.first { it.teamId == state.userTeamId }
        val injurySummaryPart = if (injuredReportsList.isNotEmpty()) {
            "\n\n🏥 MEDICAL HUB INJURY ALERTS:\n" + injuredReportsList.joinToString("\n") + "\n\nThese players are currently sidelined and unavailable for starting selection."
        } else {
            "\n\n🏥 PHYSIO HEALTH REPORT:\nExcellent! Physiotherapy confirms zero matchday physical injuries. The squad is completely healthy and fit."
        }
        val uWon = if (userRevFixt != null) {
            val isHome = userRevFixt.homeTeamId == state.userTeamId
            if (isHome) userRevFixt.homeScore > userRevFixt.awayScore else userRevFixt.awayScore > userRevFixt.homeScore
        } else false
        val uAttendance = userRevFixt?.stadiumAttendance ?: 0

        val sponsorBreakdowns = mutableListOf<String>()
        var totalInflow = 0L

        state.activeSponsorships.forEach { sponsor ->
            var payout = sponsor.baseWeeklyPayout
            val details = mutableListOf("Base: £${String.format("%,d", sponsor.baseWeeklyPayout)}")
            if (uWon && sponsor.winBonus > 0) {
                payout += sponsor.winBonus
                details.add("Win: £${String.format("%,d", sponsor.winBonus)}")
            }
            if (sponsor.attendanceThreshold > 0 && uAttendance >= sponsor.attendanceThreshold) {
                payout += sponsor.attendanceBonus
                details.add("Crowd met: £${String.format("%,d", sponsor.attendanceBonus)}")
            }
            totalInflow += payout
            sponsorBreakdowns.add("• **${sponsor.brandName}** (${sponsor.category}): +£${String.format("%,d", payout)} (${details.joinToString(", ")})")
        }

        val sponsorshipSummaryPart = if (totalInflow > 0) {
            "\n\n💰 BRAND SPONSORSHIP PORTFOLIO INCOME:\n" + sponsorBreakdowns.joinToString("\n") + "\nThese funds have been credited directly to your treasury accounts."
        } else {
            "\n\n💰 SPONSORSHIP HUB REPORT:\nYou currently have no active commercial sponsors signed. To resolve this and secure steady matchday revenue, visit the Sponsorship board in your Executive boardroom."
        }

        val weeklyNews = IntroNewsInboxGenerator(
            mDay,
            "Matchday $mDay Review: Board Assessment",
            "Boss,\n\nFollowing Match Day $mDay, the team holds ${userStand.points} pts in #${pPosRanking(newTable.sortedWith(compareByDescending<LeagueStanding> { it.points }.thenByDescending { it.goalDifference }.thenByDescending { it.goalsFor }), state.userTeamId)} position.\n\n" +
            (if (userStand.goalDifference > 0) "Our scoring record is positive. Tactically you have us structured very nicely!" else "Our defenses are shipping too many chances. Consider switching to a 'DEFENSIVE' mentality or 5-4-1 formation in Tactics Hub.") +
            revenueSummaryPart +
            sponsorshipSummaryPart +
            injurySummaryPart +
            "\n\nKeep driving progress.\n- Chief Executive"
        )

        // 🚨 STAFF RETIREMENT MECHANISM
        val retiredStaffList = mutableListOf<StaffMember>()
        val remainingHiredStaff = mutableListOf<StaffMember>()
        val retirementInboxMessages = mutableListOf<InboxMessage>()
        
        state.hiredStaff.forEach { staff ->
            if (staff.age >= 60 && Random.nextDouble() < 0.15 && mDay > 1) {
                retiredStaffList.add(staff)
                
                val retMessage = InboxMessage(
                    id = "msg_staff_retirement_${System.currentTimeMillis()}_${staff.id}",
                    sender = "Human Resources Desk",
                    subject = "🚨 RETIREMENT NOTICE: Head ${staff.role} ${staff.name}",
                    content = "Boss,\n\nWe would like to formally notify you that **${staff.name}** (${staff.age} y.o., Head ${staff.role.lowercase()}) has announced their formal retirement, effective immediately.\n\n\"I have thoroughly enjoyed my tenure here and contributing to the squad. However, the time has come for me to step away from professional football and spend time with my family.\"\n\nWe have settled their outstanding contract. You must head to the Staff Board and hire a suitable head ${staff.role.lowercase()} replacement to restore vital squad operational bonuses!\n\nRespectfully,\nHR Coordinator",
                    isRead = false
                )
                retirementInboxMessages.add(retMessage)
            } else {
                remainingHiredStaff.add(staff)
            }
        }

        // 🔍 SCOUT RECOMMENDATION MECHANISM
        val scoutRecommendedCandidates = mutableListOf<ScoutCandidate>()
        val scoutRecommendedInbox = mutableListOf<InboxMessage>()
        
        remainingHiredStaff.filter { it.role == "SCOUT" }.forEach { scout ->
            if (Random.nextDouble() < 0.45) { // 45% chance per scout
                val recPos = listOf("GK", "DEF", "MID", "FWD").random()
                val scoutRatingMin = 70 + scout.ratingStars * 3
                val scoutRatingMax = 90 + scout.ratingStars * 2
                val candidate = GameEngine.generateScoutCandidate(
                    positionFilter = recPos,
                    minRating = scoutRatingMin,
                    maxRating = scoutRatingMax.coerceAtMost(99)
                )
                scoutRecommendedCandidates.add(candidate)
                
                val recMsg = InboxMessage(
                    id = "msg_scout_rec_${System.currentTimeMillis()}_${scout.id}",
                    sender = "Head Scout: ${scout.name}",
                    subject = "📋 SCOUT RECORD: Recommended signing ${candidate.name}!",
                    content = "Boss,\n\nMy scouting assignments have turned up an absolute gem who matches our strategic profiles perfectly:\n\n👤 Player: **${candidate.name}**\n🏃 Position: **${candidate.position}**\n📈 Age: **${candidate.age}** y.o. | Rating: **${candidate.overallRating} OVR**\n🔮 Current Potential: **${candidate.currentPotential}** | Future Potential: **${candidate.futurePotential}**\nDescription: *${candidate.description}*\n💰 Valuation: **£${String.format("%,d", candidate.value)}**\n📝 Expected Wage: **£${String.format("%,d", candidate.wage)}/wk**\n\nI have added them directly to our recruitment pipeline in the Scouting Agency Board on our dashboard. They are waiting for your final signature.\n\nRegards,\n${scout.name}",
                    isRead = false
                )
                scoutRecommendedInbox.add(recMsg)
            }
        }

        // Generate fresh scouting candidates automatically each Matchday
        val uTeam = state.teams.first { it.isUserControlled }
        val scoutingLevel = uTeam.scoutingLevel
        val autoSlots = 6 + (scoutingLevel - 1) * 2
        val minQ = 65 + (scoutingLevel - 1) * 2
        val maxQ = 92 + (scoutingLevel - 1) * 1

        val hasSvenAuto = remainingHiredStaff.any { it.id == "staff_scout_legend" }
        val hasMayaAuto = remainingHiredStaff.any { it.id == "staff_scout_analyst" }
        val ratingBoostAuto = if (hasSvenAuto) 20 else if (hasMayaAuto) 10 else 0

        val freshScouts = List(autoSlots) { 
            val baseCandidate = GameEngine.generateScoutCandidate(minRating = minQ, maxRating = maxQ.coerceAtMost(99)) 
            if (ratingBoostAuto > 0) {
                val boostedAttack = (baseCandidate.ratingAttack + ratingBoostAuto / 2).coerceAtMost(99)
                val boostedDefend = (baseCandidate.ratingDefend + ratingBoostAuto / 2).coerceAtMost(99)
                val newValue = (baseCandidate.value * (1.1 + ratingBoostAuto * 0.05)).toLong()
                baseCandidate.copy(
                    ratingAttack = boostedAttack,
                    ratingDefend = boostedDefend,
                    value = newValue,
                    description = baseCandidate.description + " [Staff Boost]"
                )
            } else {
                baseCandidate
            }
        }

        // Process youth scouting missions
        val freshMessages = mutableListOf<InboxMessage>()
        val updatedYouthScouts = state.youthScouts.map { scout ->
            var newStatus = scout.status
            var newRemaining = scout.remainingDays
            var wonderkid = scout.foundWonderkid

            if (scout.status == "SEARCHING") {
                newRemaining--
                if (newRemaining <= 0) {
                    newStatus = "REPORT_READY"
                    // Generate wonderkid
                    val wk = GameEngine.generateWonderkid(
                        ratingStars = scout.ratingStars,
                        region = scout.currentRegion ?: "South America",
                        searchPosition = scout.searchType ?: "ANY"
                    )
                    wonderkid = wk
                    
                    val notification = InboxMessage(
                        id = "msg_scout_report_${scout.id}_${System.currentTimeMillis()}",
                        sender = "Youth Recruiter: ${scout.name}",
                        subject = "📋 SCOUT REPORT: Wonderkid in ${scout.currentRegion}!",
                        content = "Boss,\n\nI have successfully finished scouting in ${scout.currentRegion} seeking a promising ${scout.searchType} talent.\n\nAfter auditing local youth tournaments and training facilities, I've discovered **${wk.name}** (${wk.age}y.o, position: ${wk.position}) with a scout potential assessment of **${wk.description}**.\n\nGo to your Youth Academy Hub to review or sign them directly into our squad!\n\nBest regards,\n${scout.name}"
                    )
                    freshMessages.add(notification)
                }
            } else if (scout.status == "COOLDOWN") {
                newRemaining--
                if (newRemaining <= 0) {
                    newStatus = "IDLE"
                    
                    val notification = InboxMessage(
                        id = "msg_scout_cooldown_${scout.id}_${System.currentTimeMillis()}",
                        sender = "Youth Recruiter: ${scout.name}",
                        subject = "Rest Completed & Ready for Dispatch",
                        content = "Manager,\n\nMy rest and vacation period has officially completed. I have fully updated my regional travel visas and am ready to be dispatched to a new target region immediately.\n\n- ${scout.name}"
                    )
                    freshMessages.add(notification)
                }
            }
            scout.copy(
                status = newStatus,
                remainingDays = newRemaining,
                foundWonderkid = wonderkid
            )
        }

        var finalAvailableOffers = state.availableJobOffers
        var finalInboxList = freshMessages + listOf(weeklyNews) + stateWithRevenue.inbox

        // Mid-season dynamic job offers
        if (!isSeasonOver) {
            if (Random.nextDouble() < 0.15) {
                val potentialTeams = state.teams.filter { it.id != state.userTeamId }
                if (potentialTeams.isNotEmpty()) {
                    val offerTeam = potentialTeams.random()
                    val jobReason = listOf(
                        "Following a string of recent disappointing performances, our club's Board has lost faith in our head coach and is keen to hire a proven tactician immediately.",
                        "We are launching a mid-season squad revolution and want to tempt you with our impressive wage and squad structure. Join us to direct our campaign!",
                        "Our board is seeking an ambitious coach to secure our survival and build a powerful unit for next season."
                    ).random()

                    val midOffer = JobOffer(
                        teamId = offerTeam.id,
                        teamName = offerTeam.name,
                        teamRating = offerTeam.teamRating,
                        budget = offerTeam.budget,
                        reason = jobReason
                    )
                    finalAvailableOffers = listOf(midOffer)

                    val offerMsg = InboxMessage(
                        id = "msg_mid_season_offer_${System.currentTimeMillis()}",
                        sender = "Chairman of the Board of ${offerTeam.name}",
                        subject = "💼 EXCLUSIVE PRE-CONTRACT CONTRACT VACANCY OFFER",
                        content = "Manager,\n\nOur board has been tracking your managerial campaign. We are extremely impressed with your leadership style and tactical discipline.\n\nWe would like to offer you a contract with our club immediately! Our current squad rating is ⭐ ${offerTeam.teamRating} OVR and we have a transfer budget of £${String.format("%,d", offerTeam.budget)} to invest under your leadership.\n\nTo accept this, review the Job Offers board on your dashboard. This offer will expire if you choose to advance the next matchday as the current club coach.\n\nBest regards,\nBoard of Executive Directors"
                    )
                    finalInboxList = listOf(offerMsg) + finalInboxList
                }
            } else {
                // Pre-contract offers expire if you proceed to next match day
                finalAvailableOffers = emptyList()
            }
        }

        val budgetInM = (stateWithRevenue.transferBudget / 1_000_000L).toInt()
        val totalWins = state.managerWins + winsInc

        // Progress check of Board Goals
        val progressedGoals = stateWithRevenue.activeBoardGoals.map { goal ->
            var nextCur = goal.currentValue
            if (goal.targetType == "WINS") {
                nextCur = totalWins
            } else if (goal.targetType == "BALANCE") {
                nextCur = budgetInM
            } else if (goal.targetType == "BOARD_CONFID") {
                val userTeamInState = stateWithRevenue.teams.first { it.isUserControlled }
                nextCur = userTeamInState.boardConfidence
            }

            val isCompleted = nextCur >= goal.targetValue && !goal.completed && !goal.failed
            val isFailed = !goal.completed && !goal.failed && nextMatchDay > goal.deadlineMatchDay && nextCur < goal.targetValue

            goal.copy(
                currentValue = nextCur,
                completed = if (isCompleted) true else goal.completed,
                failed = if (isFailed) true else goal.failed
            )
        }

        // Apply rewards / penalties for transitioned goals
        var finalBudget = stateWithRevenue.transferBudget
        var finalTeams = stateWithRevenue.teams.map { t ->
            val matchingInRevenue = stateWithRevenue.teams.first { it.id == t.id }
            matchingInRevenue.copy()
        }
        val inboxNotifications = mutableListOf<InboxMessage>()

        val evaluatedGoals = progressedGoals.map { goal ->
            val wasNotCompleted = stateWithRevenue.activeBoardGoals.firstOrNull { it.id == goal.id }?.completed == false
            val wasNotFailed = stateWithRevenue.activeBoardGoals.firstOrNull { it.id == goal.id }?.failed == false

            if (goal.completed && wasNotCompleted) {
                finalBudget += goal.financialReward
                finalTeams = finalTeams.map { t ->
                    if (t.isUserControlled) {
                        t.copy(
                            budget = t.budget + goal.financialReward,
                            boardConfidence = (t.boardConfidence + goal.confidenceReward).coerceAtMost(100)
                        )
                    } else t
                }
                inboxNotifications.add(InboxMessage(
                    id = "msg_bg_win_${goal.id}_${System.currentTimeMillis()}",
                    sender = "Club Boardroom",
                    subject = "🎯 Directive Completed: ${goal.title}",
                    content = "Boss,\n\nWe are absolutely delighted with your tactical achievements. You successfully completed our directive: '${goal.description}'.\n\nWe have injected a Cash Prize of £${String.format("%,d", goal.financialReward)} directly into your core transfer budget and boosted your professional standing.\n\nKeep up the spectacular work!\n- The Board of Directors"
                ))
            } else if (goal.failed && wasNotFailed) {
                finalTeams = finalTeams.map { t ->
                    if (t.isUserControlled) {
                        t.copy(
                            boardConfidence = (t.boardConfidence - goal.confidencePenalty).coerceAtLeast(0)
                        )
                    } else t
                }
                inboxNotifications.add(InboxMessage(
                    id = "msg_bg_fail_${goal.id}_${System.currentTimeMillis()}",
                    sender = "Club Boardroom",
                    subject = "🚨 DEADLINE FAILED: ${goal.title}",
                    content = "Boss,\n\nOur board is extremely disappointed that you failed to achieve our requested directive: '${goal.description}' before the Match Day ${goal.deadlineMatchDay} deadline.\n\nThis failure has severely shaken our confidence in your leadership capabilities. We expect immediate tactical turnarounds in upcoming match fixtures.\n\n- The Board of Directors"
                ))
            }
            goal
        }

        val updatedInboxList = inboxNotifications + finalInboxList + retirementInboxMessages + scoutRecommendedInbox

        val intermediateState = stateWithRevenue.copy(
            currentMatchDay = nextMatchDay,
            standings = newTable.sortedWith(compareByDescending<LeagueStanding> { it.points }.thenByDescending { it.goalDifference }.thenByDescending { it.goalsFor }),
            scoutCandidates = freshScouts + scoutRecommendedCandidates,
            hiredStaff = remainingHiredStaff,
            inbox = updatedInboxList,
            isCompleted = isSeasonOver,
            youthScouts = updatedYouthScouts,
            availableJobOffers = finalAvailableOffers,
            managerMatches = state.managerMatches + 1,
            managerWins = state.managerWins + winsInc,
            managerDraws = state.managerDraws + drawsInc,
            managerLosses = state.managerLosses + lossesInc,
            managerGoalsFor = state.managerGoalsFor + goalsForInc,
            managerGoalsAgainst = state.managerGoalsAgainst + goalsAgainstInc,
            activeBoardGoals = evaluatedGoals,
            transferBudget = finalBudget,
            teams = finalTeams,
            isTrainingConductedThisWeek = false
        )

        val finalState = if (isSeasonOver) evaluateSeasonFinish(intermediateState) else intermediateState
        val stateAfterAi = simulateAiTransfersAndBids(finalState)

        // Programmatic News Bulletin Generator
        val addedNews = mutableListOf<NewsArticle>()
        val otherTeams = stateAfterAi.teams.filter { it.id != stateAfterAi.userTeamId }
        val randomTeam = if (otherTeams.isNotEmpty()) otherTeams.random() else null
        val randomPlayer = randomTeam?.roster?.randomOrNull()

        // News 1: Tactical/Managerial news
        if (randomTeam != null) {
            val headline = listOf(
                "TACTICS ROUNDUP: ${randomTeam.name} shifting to defensive ${randomTeam.formation} schemes",
                "BOARDROOM PRESSURE: ${randomTeam.name} directors expect tactical turnarounds",
                "TRAINING REPORT: ${randomTeam.name} squad records high stamina ratings"
            ).random()
            val text = "Sports reporters comment on ${randomTeam.name}'s performance under President ${randomTeam.presidentName}. Squad forms are currently trending at an average of 6.8 OVR, with fans expressing a ${randomTeam.fanSatisfaction}% approval rating of recent tactical setups."
            addedNews.add(NewsArticle(
                id = "news_team_${System.currentTimeMillis()}",
                matchDay = mDay,
                category = "MANAGER",
                title = headline,
                body = text,
                source = "Athletic Press"
            ))
        }

        // News 2: Player performance spotlight
        if (randomPlayer != null && randomTeam != null) {
            val headline = "PLAYER SPOTLIGHT: ${randomPlayer.name} (${randomPlayer.position}) shines at ${randomTeam.name}"
            val text = "Scout desks report high performance ratings for ${randomPlayer.name} (Age: ${randomPlayer.age}, OVR: ${randomPlayer.overallRating}). Elite clubs are reportedly monitoring their progress closely as transfer negotiations heat up."
            addedNews.add(NewsArticle(
                id = "news_player_${System.currentTimeMillis()}",
                matchDay = mDay,
                category = "PLAYER",
                title = headline,
                body = text,
                source = "World Football Network"
            ))
        }

        // News 3: General transfer season rumors
        val listedCpuPlayers = stateAfterAi.teams.flatMap { it.roster }.filter { it.isListed }
        if (listedCpuPlayers.isNotEmpty()) {
            val gossipPlayer = listedCpuPlayers.random()
            val headline = "TRANSFER GOSSIP: ${gossipPlayer.name} linked with multi-million move!"
            val text = "Following official listing announcements, speculative bids are circulating around ${gossipPlayer.name} (Valued at £${String.format("%,d", gossipPlayer.value)}). Negotiators hope to close terms before the transfer window shuts."
            addedNews.add(NewsArticle(
                id = "news_transfer_${System.currentTimeMillis()}",
                matchDay = mDay,
                category = "TRANSFER",
                title = headline,
                body = text,
                source = "Transfer Rumor Mill"
            ))
        } else {
            // General gossip
            val headline = "TRANSFER WINDOW EXPLODES: Mid-Season targets evaluated"
            val text = "Top-tier club boards are reviewing balance sheets today to fund high-value contracts. Pundits anticipate record-breaking transfer sign-ons in the coming weeks."
            addedNews.add(NewsArticle(
                id = "news_transfer_gen_${System.currentTimeMillis()}",
                matchDay = mDay,
                category = "TRANSFER",
                title = headline,
                body = text,
                source = "Football Insider"
            ))
        }

        val nextState = stateAfterAi.copy(globalNewsFeed = addedNews + stateAfterAi.globalNewsFeed)

        // Trigger post-match press conference if applicable
        var triggeredPressConference: PressConference? = null
        if (userFixt != null) {
            val isHome = userFixt.homeTeamId == state.userTeamId
            val scored = if (isHome) userFixt.homeScore else userFixt.awayScore
            val conceded = if (isHome) userFixt.awayScore else userFixt.homeScore
            val diff = scored - conceded

            // Triggered if opener (1st match), mid-season (7th match), finale (14th match),
            // or high-scoring margin match, or a random 25% chance of media interest.
            val isKeyMatch = mDay == 1 || mDay == 7 || mDay == 14 || Math.abs(diff) >= 3 || Random.nextDouble() < 0.25
            if (isKeyMatch) {
                val userTeam = state.teams.first { it.isUserControlled }
                val opponentName = if (isHome) userFixt.awayTeamName else userFixt.homeTeamName
                triggeredPressConference = generatePressConference(
                    userTeamName = userTeam.name,
                    scored = scored,
                    conceded = conceded,
                    matchDay = mDay,
                    opponentName = opponentName
                )
            }
        }

        val stateWithPress = if (triggeredPressConference != null) {
            nextState.copy(activePressConference = triggeredPressConference)
        } else {
            nextState
        }

        _gameState.value = stateWithPress
        _liveMatchFixture.value = null
        saveGame()

        if (triggeredPressConference != null) {
            _currentScreen.value = Screen.PressConference
            showToast("🎤 Media Desk: Post-Match press conference is assembled!")
        } else {
            _currentScreen.value = Screen.Dashboard
            showToast("Match Day Completed! Advanced to Match Day $nextMatchDay.")
        }
    }

    private fun processMatchdayRevenues(state: SaveState, matchDay: Int): SaveState {
        val updatedTeams = state.teams.map { it.copy() }
        var userRevenueEarned = 0L
        var homeTicketRev = 0L
        var homeConcessionRev = 0L
        var homeStoreRev = 0L
        var userWasHome = false

        val updatedFixtures = state.fixtures.map { f ->
            if (f.matchDay == matchDay && f.isPlayed && f.stadiumAttendance == 0) {
                val homeTeam = updatedTeams.first { it.id == f.homeTeamId }
                val awayTeam = updatedTeams.first { it.id == f.awayTeamId }

                // Calculate attendance rate
                val baseRate = 0.55 + (homeTeam.teamRating / 400.0) + (homeTeam.fanSatisfaction / 400.0)
                val weatherPenalty = when (f.pitchCondition) {
                    "SOGGY" -> 0.05
                    "MUDDY" -> 0.15
                    "FROZEN" -> 0.10
                    else -> 0.0
                }
                val attendanceRate = (baseRate - weatherPenalty).coerceIn(0.40, 1.0)
                val attendance = (homeTeam.stadiumCapacity * attendanceRate).toInt()

                val ticketRev = attendance.toLong() * homeTeam.stadiumTicketPrice
                val concessionRev = attendance.toLong() * 12 // £12 concessions spend per fan
                val storeRev = homeTeam.clubStoreLevel * 100_000L
                val totalRevenue = ticketRev + concessionRev + storeRev

                // Pay home team
                homeTeam.budget += totalRevenue
                if (homeTeam.isUserControlled) {
                    userRevenueEarned += totalRevenue
                    homeTicketRev = ticketRev
                    homeConcessionRev = concessionRev
                    homeStoreRev = storeRev
                    userWasHome = true
                }

                // Supporter satisfaction adjustments based on match outcome
                val resultDiff = f.homeScore - f.awayScore
                if (resultDiff > 0) {
                    homeTeam.fanSatisfaction = (homeTeam.fanSatisfaction + 5).coerceAtMost(100)
                    awayTeam.fanSatisfaction = (awayTeam.fanSatisfaction - 3).coerceAtLeast(20)
                } else if (resultDiff < 0) {
                    homeTeam.fanSatisfaction = (homeTeam.fanSatisfaction - 6).coerceAtLeast(20)
                    awayTeam.fanSatisfaction = (awayTeam.fanSatisfaction + 5).coerceAtMost(100)
                } else {
                    homeTeam.fanSatisfaction = (homeTeam.fanSatisfaction + 1).coerceAtMost(100)
                    awayTeam.fanSatisfaction = (awayTeam.fanSatisfaction + 1).coerceAtMost(100)
                }

                f.copy(stadiumAttendance = attendance, revenueGenerated = totalRevenue)
            } else f
        }

        // Determine user match outcome for sponsorship bonuses
        val userFixt = updatedFixtures.firstOrNull { f ->
            f.matchDay == matchDay && (f.homeTeamId == state.userTeamId || f.awayTeamId == state.userTeamId)
        }
        val userWon = if (userFixt != null && userFixt.isPlayed) {
            val isHome = userFixt.homeTeamId == state.userTeamId
            if (isHome) userFixt.homeScore > userFixt.awayScore else userFixt.awayScore > userFixt.homeScore
        } else false

        val userAttendance = userFixt?.stadiumAttendance ?: 0

        var totalSponsorInflow = 0L
        state.activeSponsorships.forEach { sponsor ->
            var payout = sponsor.baseWeeklyPayout
            if (userWon && sponsor.winBonus > 0) {
                payout += sponsor.winBonus
            }
            if (sponsor.attendanceThreshold > 0 && userAttendance >= sponsor.attendanceThreshold) {
                payout += sponsor.attendanceBonus
            }
            totalSponsorInflow += payout
        }

        var nextTransferBudget = state.transferBudget
        if (userRevenueEarned > 0L) {
            nextTransferBudget += userRevenueEarned
        }
        nextTransferBudget += totalSponsorInflow

        // Deduct squad wage bill, staff wages, and upkeep
        val userTeamForWages = updatedTeams.first { it.isUserControlled }
        val squadWageBill = userTeamForWages.roster.sumOf { it.contractWage }
        val staffWageBill = state.hiredStaff.sumOf { it.weeklyWage }
        val facilitiesUpkeep = (userTeamForWages.medicalLevel * 30_000L) +
                (userTeamForWages.academyLevel * 50_000L) +
                (userTeamForWages.trainingLevel * 40_000L) +
                (userTeamForWages.scoutingLevel * 35_000L)
        val totalExpenses = squadWageBill + facilitiesUpkeep + staffWageBill
        nextTransferBudget -= totalExpenses

        // Find user team in updatedTeams and sync budget
        val syncedTeams = updatedTeams.map { t ->
            if (t.isUserControlled) {
                t.copy(budget = nextTransferBudget)
            } else t
        }

        var nextState = state.copy(
            teams = syncedTeams,
            fixtures = updatedFixtures,
            transferBudget = nextTransferBudget
        )

        // Add transaction ledger records
        if (userWasHome) {
            nextState = addFinancialTransaction(nextState, "Matchday Ticket Receipts", homeTicketRev, "TICKET")
            nextState = addFinancialTransaction(nextState, "Stadium Fan Concessions", homeConcessionRev, "CONCESSION")
            nextState = addFinancialTransaction(nextState, "Club Store Sales", homeStoreRev, "STORE")
        }
        nextState = addFinancialTransaction(nextState, "Squad Weekly Wages", -squadWageBill, "WAGES")
        if (staffWageBill > 0L) {
            nextState = addFinancialTransaction(nextState, "Hired Staff Weekly Payroll", -staffWageBill, "WAGES")
        }
        nextState = addFinancialTransaction(nextState, "Facilities Upkeep Costs", -facilitiesUpkeep, "WAGES")

        if (totalSponsorInflow > 0) {
            nextState = addFinancialTransaction(nextState, "Brand Sponsorships Weekly Inflow", totalSponsorInflow, "STORE")
        }

        return nextState
    }

    private fun pPosRanking(stands: List<LeagueStanding>, userTeamId: String): Int {
        val idx = stands.indexOfFirst { it.teamId == userTeamId }
        return if (idx == -1) 1 else idx + 1
    }

    private fun IntroNewsInboxGenerator(day: Int, subj: String, text: String): InboxMessage {
        return InboxMessage(
            id = "msg_${System.currentTimeMillis()}",
            sender = "Staff HQ",
            subject = subj,
            content = text
        )
    }

    fun markMessageRead(msgId: String) {
        val state = _gameState.value ?: return
        val updatedInbox = state.inbox.map { 
            if (it.id == msgId) it.copy(isRead = true) else it 
        }
        _gameState.value = state.copy(inbox = updatedInbox)
        saveGame()
    }

    fun upgradeStadiumCapacity() {
        val state = _gameState.value ?: return
        val cost = 5_000_000L
        if (state.transferBudget < cost) {
            showToast("⚠️ Insufficient funds! Stadium expansion costs £${cost / 1_000_000}M.")
            return
        }
        val updatedTeams = state.teams.map { t ->
            if (t.isUserControlled) {
                t.copy(
                    stadiumCapacity = t.stadiumCapacity + 5000,
                    budget = t.budget - cost
                )
            } else t
        }
        _gameState.value = state.copy(
            transferBudget = state.transferBudget - cost,
            teams = updatedTeams
        ).let { s ->
            addFinancialTransaction(s, "Stadium expansion (+5k seats)", -cost, "UPGRADES")
        }
        saveGame()
        com.example.utils.SoundEngine.playUpgradeSuccess()
        showToast("🏟️ Stadium expanded! +5,000 seats added successfully.")
    }

    fun adjustTicketPrice(newPrice: Int) {
        val state = _gameState.value ?: return
        val adjustedPrice = newPrice.coerceIn(10, 100)
        val updatedTeams = state.teams.map { t ->
            if (t.isUserControlled) {
                t.copy(stadiumTicketPrice = adjustedPrice)
            } else t
        }
        _gameState.value = state.copy(teams = updatedTeams)
        saveGame()
        showToast("🏷️ General admission ticket price set to £$adjustedPrice.")
    }

    fun upgradeFacility(type: String) {
        val state = _gameState.value ?: return
        val userTeam = state.teams.firstOrNull { it.isUserControlled } ?: return
        val currentLevel = when (type) {
            "MEDICAL" -> userTeam.medicalLevel
            "ACADEMY" -> userTeam.academyLevel
            "STORE" -> userTeam.clubStoreLevel
            "TRAINING" -> userTeam.trainingLevel
            "SCOUTING" -> userTeam.scoutingLevel
            else -> 1
        }
        if (currentLevel >= 5) {
            showToast("⭐ Facility is already at Maximum Level (Lv. 5)!")
            return
        }
        val cost = when (type) {
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
        if (state.transferBudget < cost) {
            showToast("⚠️ Insufficient budget! Need £${cost / 1_000_000}M to upgrade.")
            return
        }
        val updatedTeams = state.teams.map { t ->
            if (t.isUserControlled) {
                when (type) {
                    "MEDICAL" -> t.copy(medicalLevel = t.medicalLevel + 1, budget = t.budget - cost)
                    "ACADEMY" -> t.copy(academyLevel = t.academyLevel + 1, budget = t.budget - cost)
                    "STORE" -> t.copy(clubStoreLevel = t.clubStoreLevel + 1, budget = t.budget - cost)
                    "TRAINING" -> t.copy(trainingLevel = t.trainingLevel + 1, budget = t.budget - cost)
                    "SCOUTING" -> t.copy(scoutingLevel = t.scoutingLevel + 1, budget = t.budget - cost)
                    else -> t
                }
            } else t
        }
        _gameState.value = state.copy(
            transferBudget = state.transferBudget - cost,
            teams = updatedTeams
        ).let { s ->
            addFinancialTransaction(s, "Upgraded $type facility (Lv. ${currentLevel + 1})", -cost, "UPGRADES")
        }
        saveGame()
        com.example.utils.SoundEngine.playUpgradeSuccess()
        showToast("🏪 Successfully upgraded $type facility to Level ${currentLevel + 1}!")
    }

    fun signSponsorship(sponsorshipId: String) {
        val state = _gameState.value ?: return
        val offer = state.availableSponsorshipOffers.firstOrNull { it.id == sponsorshipId } ?: return

        // Remove any signed sponsor of the same category
        val updatedActive = state.activeSponsorships.filter { it.category != offer.category }.toMutableList()
        val signedOffer = offer.copy(signed = true)
        updatedActive.add(signedOffer)

        // Give instant sign-on bonus
        val signOnBonus = offer.baseWeeklyPayout * 2L
        val nextBudget = state.transferBudget + signOnBonus

        val updatedTeams = state.teams.map { t ->
            if (t.isUserControlled) t.copy(budget = t.budget + signOnBonus) else t
        }

        val updatedOffers = state.availableSponsorshipOffers.filter { it.id != sponsorshipId }

        val inboxMsg = InboxMessage(
            id = "msg_sponsor_signed_${System.currentTimeMillis()}",
            sender = "${offer.brandName} Partnership",
            subject = "🤝 Welcome to the ${offer.brandName} Portfolio!",
            content = "Manager,\n\nOur board has finalized the brand contract with you today as our new ${offer.category} provider.\n\nWe have deposited a sign-on bonus of £${String.format("%,d", signOnBonus)} directly into your boardroom treasury ledger.\n\nWe look forward to showcasing incredible results together!\n\nBest regards,\nPartnership Accounts Manager"
        )

        val nextState = state.copy(
            activeSponsorships = updatedActive,
            availableSponsorshipOffers = updatedOffers,
            transferBudget = nextBudget,
            teams = updatedTeams,
            inbox = listOf(inboxMsg) + state.inbox
        ).let { s ->
            addFinancialTransaction(s, "Sponsorship signed: ${offer.brandName} (Sign-on Bonus)", signOnBonus, "STORE")
        }

        _gameState.value = nextState
        saveGame()
        showToast("✍️ Contract Signed! Secured £${String.format("%,d", offer.baseWeeklyPayout)}/wk with ${offer.brandName}!")
    }

    fun negotiateSponsorship(sponsorshipId: String) {
        val state = _gameState.value ?: return
        val updatedOffers = state.availableSponsorshipOffers.map { offer ->
            if (offer.id == sponsorshipId) {
                if (offer.counterCount >= 3) {
                    showToast("⚠️ This brand rep has rejected further negotiations. Sign or select another offer!")
                    return
                }

                // Roll walk check
                val roll = Random.nextInt(100)
                if (roll < offer.negotiationRisk) {
                    val nextOffers = state.availableSponsorshipOffers.filter { it.id != sponsorshipId }
                    val walkMsg = InboxMessage(
                        id = "msg_sponsor_withdrew_${System.currentTimeMillis()}",
                        sender = "${offer.brandName} Reps",
                        subject = "⚠️ Negotiation Terminated: ${offer.brandName}",
                        content = "Manager,\n\nYour repeated pushback and elevated demands do not match our standard market allocations. We are withdrawing our contract offer with immediate effect.\n\n- Partnership Group"
                    )
                    _gameState.value = state.copy(
                        availableSponsorshipOffers = nextOffers,
                        inbox = listOf(walkMsg) + state.inbox
                    )
                    saveGame()
                    showToast("⚠️ Negotiations Failed! ${offer.brandName} walked away from the table.")
                    return
                }

                // Successful counter
                val increasedBase = (offer.baseWeeklyPayout * 1.15).toLong()
                val increasedBonus = (offer.winBonus * 1.15).toLong()
                val nextCounter = offer.counterCount + 1
                val increasedRisk = offer.negotiationRisk + 20

                showToast("🤝 Counter Successful! Base payout and win bonus increased by +15%!")
                offer.copy(
                    baseWeeklyPayout = increasedBase,
                    winBonus = increasedBonus,
                    counterCount = nextCounter,
                    negotiationRisk = increasedRisk
                )
            } else offer
        }

        _gameState.value = state.copy(availableSponsorshipOffers = updatedOffers)
        saveGame()
    }

    fun updateBoardGoalProgress(targetType: String, amount: Int = 1, absoluteValue: Int? = null) {
        val state = _gameState.value ?: return
        var goalsUpdated = false
        val nextGoals = state.activeBoardGoals.map { goal ->
            if (goal.targetType == targetType && !goal.completed && !goal.failed) {
                val nextVal = absoluteValue ?: (goal.currentValue + amount)
                val isCompleted = nextVal >= goal.targetValue
                goalsUpdated = true
                
                goal.copy(
                    currentValue = nextVal.coerceAtMost(goal.targetValue),
                    completed = isCompleted
                )
            } else goal
        }
        
        if (goalsUpdated) {
            var nextBudget = state.transferBudget
            var nextTeams = state.teams.map { it.copy() }

            val updatedGoals = nextGoals.map { goal ->
                val wasNotCompleted = state.activeBoardGoals.firstOrNull { it.id == goal.id }?.completed == false
                if (goal.completed && wasNotCompleted) {
                    nextBudget += goal.financialReward
                    nextTeams = nextTeams.map { t ->
                        if (t.isUserControlled) {
                            t.copy(
                                budget = t.budget + goal.financialReward,
                                boardConfidence = (t.boardConfidence + goal.confidenceReward).coerceAtMost(100)
                            )
                        } else t
                    }
                }
                goal
            }

            var nextState = state.copy(
                activeBoardGoals = updatedGoals,
                transferBudget = nextBudget,
                teams = nextTeams
            )

            updatedGoals.forEach { goal ->
                val wasNotCompleted = state.activeBoardGoals.firstOrNull { it.id == goal.id }?.completed == false
                if (goal.completed && wasNotCompleted) {
                    nextState = addFinancialTransaction(nextState, "Board Goal Cash Prize: ${goal.title}", goal.financialReward, "STORE")
                    val inboxMail = InboxMessage(
                        id = "msg_bg_${goal.id}_${System.currentTimeMillis()}",
                        sender = "Club Boardroom",
                        subject = "🎯 Directive Completed: ${goal.title}",
                        content = "Boss,\n\nWe are absolutely delighted with your tactical achievements. You successfully completed our directive: '${goal.description}'.\n\nWe have injected a Cash Prize of £${String.format("%,d", goal.financialReward)} directly into your core transfer budget and boosted your professional standing.\n\nKeep up the spectacular work!\n- The Board of Directors"
                    )
                    nextState = nextState.copy(inbox = listOf(inboxMail) + nextState.inbox)
                }
            }

            _gameState.value = nextState
            saveGame()
        }
    }

    private fun evaluateSeasonFinish(state: SaveState): SaveState {
        val sortedStandings = state.standings
        
        val userTeam = state.teams.first { it.id == state.userTeamId }
        val userStandingIdx = sortedStandings.indexOfFirst { it.teamId == state.userTeamId }
        val userRank = userStandingIdx + 1
        
        var userSacked = false
        if (userRank == 8) {
            userSacked = true
        } else if (userRank == 7 && userTeam.fanSatisfaction < 75) {
            userSacked = true
        } else if (userRank == 6 && userTeam.fanSatisfaction < 55) {
            userSacked = true
        }
        
        val userStand = sortedStandings.first { it.teamId == state.userTeamId }
        val historyRecord = CareerHistoryRecord(
            seasonYear = state.gameYear,
            clubName = userTeam.name,
            finalPosition = userRank,
            points = userStand.points,
            goalsFor = userStand.goalsFor,
            goalsAgainst = userStand.goalsAgainst,
            wins = userStand.won,
            draws = userStand.drawn,
            losses = userStand.lost,
            trophyWon = userRank == 1,
            wasSacked = userSacked
        )
        val updatedHistory = state.careerHistory + historyRecord
        
        val newInboxMessages = mutableListOf<InboxMessage>()

        // Evaluate core Season Standing commercial sponsorship performance rewards
        var totalStandingBonus = 0L
        val sponsorBonusReports = mutableListOf<String>()

        state.activeSponsorships.forEach { sponsor ->
            if (sponsor.standingGoalOrdinal > 0 && userRank <= sponsor.standingGoalOrdinal) {
                totalStandingBonus += sponsor.standingBonus
                sponsorBonusReports.add("• **${sponsor.brandName}**: British Pound £${String.format("%,d", sponsor.standingBonus)} (Target: Top ${sponsor.standingGoalOrdinal})")
            }
        }

        var nextBudget = state.transferBudget
        if (totalStandingBonus > 0) {
            nextBudget += totalStandingBonus
            val sponsorInbox = InboxMessage(
                id = "msg_seasonal_sponsor_${System.currentTimeMillis()}",
                sender = "Club Treasury Desk",
                subject = "🏆 SEASON STANDING SPONSOR BONUS UNLOCKED",
                content = "Manager,\n\nWe are pleased to report that our commercial brand partners have finalized their evaluation of our season finish (Rank #${userRank}) and unlocked the following contract bonuses:\n\n" +
                sponsorBonusReports.joinToString("\n") + "\n\nA gross total of £${String.format("%,d", totalStandingBonus)} has been successfully deposited into our transfer accounts.\n\nBest regards,\nAccounting & Corporate Relations Office"
            )
            newInboxMessages.add(sponsorInbox)
        }
        
        val updatedTeams = state.teams.map { t ->
            if (t.id == state.userTeamId) {
                t.copy(budget = t.budget + totalStandingBonus)
            } else {
                val tStandingIdx = sortedStandings.indexOfFirst { it.teamId == t.id }
                val tRank = tStandingIdx + 1
                val trVal = t.teamRating
                
                val shouldSack = when {
                    trVal >= 80 && tRank >= 5 -> Random.nextDouble() < 0.70
                    trVal in 74..79 && tRank >= 7 -> Random.nextDouble() < 0.60
                    trVal < 74 && tRank == 8 -> Random.nextDouble() < 0.50
                    else -> false
                }
                
                if (shouldSack) {
                    val message = InboxMessage(
                        id = "msg_sack_${System.currentTimeMillis()}_${t.id}",
                        sender = "League News Association",
                        subject = "📰 HEADLINE: ${t.name} sack manager!",
                        content = "Following a disappointing season finishing in #${tRank} position, the board of ${t.name} has terminated their manager's contract. The club is actively evaluating candidates for the vacant post."
                    )
                    newInboxMessages.add(message)
                    t.copy(budget = t.budget + 5_000_000L)
                } else {
                    t
                }
            }
        }
        
        val availableOffers = mutableListOf<JobOffer>()
        sortedStandings.forEach { standing ->
            if (standing.teamId != state.userTeamId) {
                val t = state.teams.first { it.id == standing.teamId }
                val tStandingIdx = sortedStandings.indexOfFirst { it.teamId == t.id }
                val tRank = tStandingIdx + 1
                
                if (tRank >= 4) {
                    availableOffers.add(
                        JobOffer(
                            teamId = t.id,
                            teamName = t.name,
                            teamRating = t.teamRating,
                            budget = t.budget,
                            reason = when (tRank) {
                                4, 5 -> "The Board is seeking an ambitious leader to launch a campaign into the league elite next year."
                                6, 7 -> "After underperforming this year, the club seeks a tactical upgrade to restore direct competitiveness."
                                else -> "Following a disastrous season finishing bottom, the squad requires complete rebuilding and tactical overhaul."
                            }
                        )
                    )
                }
            }
        }
        
        val finalOffers = availableOffers.shuffled().take(3)
        
        if (userSacked) {
            val sackMessage = InboxMessage(
                id = "msg_user_sack_${System.currentTimeMillis()}",
                sender = "Chairman of the Board",
                subject = "🚨 TERMINATION OF MANAGEMENT CONTRACT",
                content = "Manager,\n\nFollowing a board meeting reviewing our Season performance where we finished in #${userRank} position, the Board of Directors has unanimously voted to terminate your contract with immediate effect.\n\nYour belongings have been cleared from the manager office. You are now a free agent. Head to the Job Offers Board on the dashboard to sign a contract with a new club for this upcoming campaign.\n\n- Chief Executive"
            )
            newInboxMessages.add(sackMessage)
        }

        // Seasonal Awards & Team of the Year (TOTY) Calculations
        val allPlayersWithTeams = state.teams.flatMap { t -> t.roster.map { p -> Pair(t, p) } }
        val goldenBootWinner = allPlayersWithTeams.maxByOrNull { it.second.goalsScored }
        val bestKeeper = allPlayersWithTeams.filter { it.second.position == "GK" }.maxByOrNull { it.second.cleanSheets }
        val bestPlaymaker = allPlayersWithTeams.maxByOrNull { it.second.assists }
        val playerOfSeason = allPlayersWithTeams.filter { it.second.matchesPlayed >= 5 }.maxByOrNull { it.second.form }
        val youngPlayerOfSeason = allPlayersWithTeams.filter { it.second.age <= 22 }.maxByOrNull { it.second.overallRating }

        // Manager of the Year (MOTY)
        val winningTeamId = sortedStandings.firstOrNull()?.teamId
        val winningTeam = state.teams.firstOrNull { it.id == winningTeamId }
        val motyTeamName = winningTeam?.name ?: "Unknown Team"
        val motyName = if (winningTeam?.isUserControlled == true) state.managerName else "Gaffer " + listOf("Arteta", "Ancelotti", "Klopp", "Mourinho", "Guardiola").random()

        // Team of the Year selection (1 GK, 4 DEF, 3 MID, 3 FWD)
        val totGk = allPlayersWithTeams.filter { it.second.position == "GK" }.maxByOrNull { it.second.overallRating }
        val totDefs = allPlayersWithTeams.filter { it.second.position == "DEF" }.sortedByDescending { it.second.overallRating }.take(4)
        val totMids = allPlayersWithTeams.filter { it.second.position == "MID" }.sortedByDescending { it.second.overallRating }.take(3)
        val totFwds = allPlayersWithTeams.filter { it.second.position == "FWD" }.sortedByDescending { it.second.overallRating }.take(3)

        val totLines = mutableListOf<String>()
        totGk?.let { totLines.add("🧤 GK: ${it.second.name} (${it.first.name} - ${it.second.overallRating} OVR)") }
        totDefs.forEach { totLines.add("🛡️ DEF: ${it.second.name} (${it.first.name} - ${it.second.overallRating} OVR)") }
        totMids.forEach { totLines.add("🎯 MID: ${it.second.name} (${it.first.name} - ${it.second.overallRating} OVR)") }
        totFwds.forEach { totLines.add("🔥 FWD: ${it.second.name} (${it.first.name} - ${it.second.overallRating} OVR)") }

        val awardsBody = StringBuilder()
        awardsBody.append("Good morning Boss,\n\n")
        awardsBody.append("The Football Association has finalized the official Seasonal Honors and Awards list for the Year ${state.gameYear}!\n\n")

        awardsBody.append("🏆 INDIVIDUAL PLAYER HONORS:\n")
        if (goldenBootWinner != null) {
            awardsBody.append("• **Golden Boot**: ${goldenBootWinner.second.name} (${goldenBootWinner.first.name}) - ${goldenBootWinner.second.goalsScored} Goals\n")
        }
        if (bestPlaymaker != null) {
            awardsBody.append("• **Best Playmaker**: ${bestPlaymaker.second.name} (${bestPlaymaker.first.name}) - ${bestPlaymaker.second.assists} Assists\n")
        }
        if (bestKeeper != null) {
            awardsBody.append("• **Golden Glove**: ${bestKeeper.second.name} (${bestKeeper.first.name}) - ${bestKeeper.second.cleanSheets} Clean Sheets\n")
        }
        if (playerOfSeason != null) {
            awardsBody.append("• **Player of the Season**: ${playerOfSeason.second.name} (${playerOfSeason.first.name}) - Avg Rating: ${String.format("%.2f", playerOfSeason.second.form)}\n")
        }
        if (youngPlayerOfSeason != null) {
            awardsBody.append("• **Young Player of the Season**: ${youngPlayerOfSeason.second.name} (${youngPlayerOfSeason.first.name}) - ${youngPlayerOfSeason.second.age} yrs (${youngPlayerOfSeason.second.overallRating} OVR)\n")
        }

        awardsBody.append("\n👔 MANAGERIAL HONORS:\n")
        awardsBody.append("• **Manager of the Year**: $motyName ($motyTeamName) - Led squad to League Finish (#1)\n")

        awardsBody.append("\n⭐ OFFICIAL TEAM OF THE YEAR (XI):\n")
        if (totLines.isNotEmpty()) {
            awardsBody.append(totLines.joinToString("\n"))
        } else {
            awardsBody.append("No selections recorded.")
        }

        awardsBody.append("\n\nCongratulations to all recipients. Ready the squad preparations for the next campaign!\n\n- League FA Press Office")

        val awardsInbox = InboxMessage(
            id = "msg_seasonal_awards_${System.currentTimeMillis()}",
            sender = "Football Association News",
            subject = "🏅 YEAR ${state.gameYear} OFFICIAL LEAGUE AWARDS & TOTY",
            content = awardsBody.toString()
        )
        newInboxMessages.add(awardsInbox)

        var returnedState = state.copy(
            careerHistory = updatedHistory,
            isUserSacked = userSacked,
            availableJobOffers = finalOffers,
            lastSeasonPosition = userRank,
            teams = updatedTeams,
            inbox = newInboxMessages + state.inbox,
            transferBudget = nextBudget
        )

        if (totalStandingBonus > 0) {
            returnedState = addFinancialTransaction(returnedState, "Commercial Standing Goals Inflow", totalStandingBonus, "STORE")
        }
        
        return returnedState
    }

    fun acceptJobOffer(targetTeamId: String) {
        val state = _gameState.value ?: return
        val teams = state.teams
        
        val newUserTeam = teams.firstOrNull { it.id == targetTeamId } ?: return
        
        val updatedTeams = teams.map { t ->
            when (t.id) {
                state.userTeamId -> t.copy(isUserControlled = false)
                targetTeamId -> t.copy(isUserControlled = true)
                else -> t
            }
        }
        
        _userTeamIdVal.value = targetTeamId
        
        val nextState = state.copy(
            userTeamId = targetTeamId,
            clubName = newUserTeam.name,
            transferBudget = newUserTeam.budget,
            teams = updatedTeams,
            isUserSacked = false,
            availableJobOffers = emptyList()
        )
        
        val welcomeMsg = InboxMessage(
            id = "msg_hire_${System.currentTimeMillis()}",
            sender = "Board of Directors",
            subject = "Welcome to ${newUserTeam.name}!",
            content = "Manager,\n\nWe are absolutely thrilled to welcome you as the new head coach of ${newUserTeam.name}!\n\nOur current budget stands at £${String.format("%,d", newUserTeam.budget)}. Review the list of players in the Tactics screen to assemble your ideal lineup and begin training.\n\nLet's write history together.\n\n- The Board"
        )
        _gameState.value = nextState.copy(inbox = listOf(welcomeMsg) + nextState.inbox)
        saveGame()
        showToast("🤝 Signed contract with ${newUserTeam.name}!")
    }

    fun resignFromCurrentClub() {
        val state = _gameState.value ?: return
        if (state.isUserSacked) {
            showToast("⚠️ You are already clubless!")
            return
        }
        
        val userTeam = state.teams.first { it.id == state.userTeamId }
        
        // Generate mid-season vacant jobs from non-user teams
        val availableOffers = mutableListOf<JobOffer>()
        state.teams.forEach { t ->
            if (t.id != state.userTeamId) {
                availableOffers.add(
                    JobOffer(
                        teamId = t.id,
                        teamName = t.name,
                        teamRating = t.teamRating,
                        budget = t.budget,
                        reason = "Following the sudden resignation of their previous head coach, the board has reached out to request your urgent signature."
                    )
                )
            }
        }
        
        val finalOffers = availableOffers.shuffled().take(3)
        
        val updatedTeams = state.teams.map { t ->
            if (t.id == state.userTeamId) {
                t.copy(isUserControlled = false)
            } else {
                t
            }
        }
        
        val resignationMsg = InboxMessage(
            id = "msg_resign_${System.currentTimeMillis()}",
            sender = "CHIEF EXECUTIVE",
            subject = "Mutual Contract Termination Confirmed",
            content = "Manager,\n\nWe confirm receipt and acceptance of your immediate resignation from ${userTeam.name}.\n\nThe board wishes to thank you for your service and effort during your time at the club.\n\nYou are now a free agent. Head to the Job Offers Board on the dashboard to sign a contract with a new club.\n\n- Chief Executive"
        )
        
        val nextState = state.copy(
            isUserSacked = true,
            availableJobOffers = finalOffers,
            teams = updatedTeams,
            clubName = "Unemployed / Free Agent",
            inbox = listOf(resignationMsg) + state.inbox
        )
        
        _gameState.value = nextState
        saveGame()
        showToast("💼 Successfully resigned from ${userTeam.name}!")
    }

    fun hireYouthScout(scoutId: String) {
        val state = _gameState.value ?: return
        if (state.youthScouts.size >= 3) {
            showToast("⚠️ Maximum of 3 youth scouts can be hired at once!")
            return
        }
        val scout = state.availableScoutsToHire.firstOrNull { it.id == scoutId } ?: return
        if (state.transferBudget < scout.hireFee) {
            showToast("❌ Insufficient transfer budget (Requires £${String.format("%,d", scout.hireFee)})!")
            return
        }
        
        // Update teams roster budget
        val updatedTeams = state.teams.map { t ->
            if (t.id == state.userTeamId) {
                t.copy(budget = t.budget - scout.hireFee)
            } else {
                t
            }
        }
        
        val updatedHired = state.youthScouts + scout.copy(status = "IDLE")
        val updatedToHire = state.availableScoutsToHire.filter { it.id != scoutId }
        
        val nextState = state.copy(
            transferBudget = state.transferBudget - scout.hireFee,
            youthScouts = updatedHired,
            availableScoutsToHire = updatedToHire,
            teams = updatedTeams
        )
        
        _gameState.value = nextState
        saveGame()
        showToast("🤝 Hired youth scout ${scout.name}!")
    }

    fun releaseYouthScout(scoutId: String) {
        val state = _gameState.value ?: return
        val scout = state.youthScouts.firstOrNull { it.id == scoutId } ?: return
        
        val updatedHired = state.youthScouts.filter { it.id != scoutId }
        // recycle back to hiring pool
        val updatedToHire = state.availableScoutsToHire + scout.copy(status = "IDLE", currentRegion = null, foundWonderkid = null)
        
        _gameState.value = state.copy(
            youthScouts = updatedHired,
            availableScoutsToHire = updatedToHire
        )
        saveGame()
        showToast("👋 Released youth scout ${scout.name} from contract.")
    }

    fun hireStaffMember(staffId: String) {
        val state = _gameState.value ?: return
        val staff = state.availableStaffToHire.firstOrNull { it.id == staffId } ?: return
        if (state.transferBudget < staff.cost) {
            showToast("❌ Insufficient budget (Requires £${String.format("%,d", staff.cost)})!")
            return
        }
        
        val existingOfRole = state.hiredStaff.firstOrNull { it.role == staff.role }
        var updatedHired = state.hiredStaff
        var updatedToHire = state.availableStaffToHire
        var nextBudget = state.transferBudget
        
        if (existingOfRole != null) {
            updatedHired = updatedHired.filter { it.id != existingOfRole.id }
            updatedToHire = updatedToHire + existingOfRole
            showToast("ℹ️ Released ${existingOfRole.name} to make way for ${staff.name}!")
        }
        
        nextBudget -= staff.cost
        updatedHired = updatedHired + staff
        updatedToHire = updatedToHire.filter { it.id != staffId }
        
        val updatedTeams = state.teams.map { t ->
            if (t.id == state.userTeamId) {
                t.copy(budget = nextBudget)
            } else t
        }
        
        var nextState = state.copy(
            transferBudget = nextBudget,
            hiredStaff = updatedHired,
            availableStaffToHire = updatedToHire,
            teams = updatedTeams
        )
        
        nextState = addFinancialTransaction(nextState, "Hired ${staff.role.lowercase().replaceFirstChar { it.uppercase() }}: ${staff.name}", -staff.cost, "UPGRADES")
        
        _gameState.value = nextState
        saveGame()
        showToast("🤝 Hired ${staff.name} as your new head ${staff.role.lowercase()}!")
    }

    fun releaseStaffMember(staffId: String) {
        val state = _gameState.value ?: return
        val staff = state.hiredStaff.firstOrNull { it.id == staffId } ?: return
        
        val updatedHired = state.hiredStaff.filter { it.id != staffId }
        val updatedToHire = state.availableStaffToHire + staff
        
        _gameState.value = state.copy(
            hiredStaff = updatedHired,
            availableStaffToHire = updatedToHire
        )
        saveGame()
        showToast("👋 Released ${staff.name} from contract.")
    }

    fun sendYouthScoutOnMission(scoutId: String, region: String, searchType: String) {
        val state = _gameState.value ?: return
        val scout = state.youthScouts.firstOrNull { it.id == scoutId } ?: return
        if (scout.status != "IDLE") {
            showToast("⚠️ Scout is currently busy or resting!")
            return
        }
        
        val duration = 2
        
        val updatedScouts = state.youthScouts.map { s ->
            if (s.id == scoutId) {
                s.copy(
                    status = "SEARCHING",
                    currentRegion = region,
                    searchType = searchType,
                    remainingDays = duration,
                    foundWonderkid = null
                )
            } else {
                s
            }
        }
        
        _gameState.value = state.copy(youthScouts = updatedScouts)
        saveGame()
        showToast("🚀 ${scout.name} has flown to $region searching for a $searchType wonderkid!")
    }

    fun signWonderkidToSquad(scoutId: String) {
        val state = _gameState.value ?: return
        val scout = state.youthScouts.firstOrNull { s -> s.id == scoutId } ?: return
        val candidate = scout.foundWonderkid ?: return
        
        val wonderkidPlayer = Player(
            id = candidate.id,
            name = candidate.name,
            age = candidate.age,
            position = candidate.position,
            ratingAttack = candidate.ratingAttack,
            ratingDefend = candidate.ratingDefend,
            stamina = 100,
            fatigue = 0,
            morale = 95,
            value = candidate.value,
            contractWage = candidate.wage,
            contractYearsRemaining = 5
        )
        
        val updatedTeams = state.teams.map { t ->
            if (t.id == state.userTeamId) {
                t.copy(roster = t.roster + wonderkidPlayer)
            } else {
                t
            }
        }
        
        val updatedScouts = state.youthScouts.map { s ->
            if (s.id == scoutId) {
                s.copy(
                    status = "COOLDOWN",
                    remainingDays = 2,
                    foundWonderkid = null
                )
            } else {
                s
            }
        }
        
        _gameState.value = state.copy(
            teams = updatedTeams,
            youthScouts = updatedScouts
        )
        saveGame()
        showToast("✍️ Signed wonderkid ${candidate.name} to the first team academy!")
    }

    fun rejectScoutReport(scoutId: String) {
        val state = _gameState.value ?: return
        val scout = state.youthScouts.firstOrNull { s -> s.id == scoutId } ?: return
        
        val updatedScouts = state.youthScouts.map { s ->
            if (s.id == scoutId) {
                s.copy(
                    status = "COOLDOWN",
                    remainingDays = 1,
                    foundWonderkid = null
                )
            } else {
                s
            }
        }
        
        _gameState.value = state.copy(youthScouts = updatedScouts)
        saveGame()
        showToast("🗑️ Rejected scouting report from ${scout.name}. Scout is now resting.")
    }

    private fun distributePlayerStatsForFixture(fixture: Fixture, homeTeam: Team, awayTeam: Team) {
        distributeTeamPlayerStats(fixture.homeScore, homeTeam, awayTeam)
        distributeTeamPlayerStats(fixture.awayScore, awayTeam, homeTeam)
    }

    private fun distributeTeamPlayerStats(goals: Int, team: Team, opponentOriginal: Team) {
        val xi = team.getStartingXI()
        if (xi.isEmpty()) return
        
        repeat(goals) {
            val scorer = when {
                xi.any { it.position == "FWD" } && Random.nextDouble() < 0.60 -> xi.filter { it.position == "FWD" }.random()
                xi.any { it.position == "MID" } && Random.nextDouble() < 0.85 -> xi.filter { it.position == "MID" }.random()
                xi.any { it.position == "DEF" } -> xi.filter { it.position == "DEF" }.random()
                else -> xi.random()
            }
            scorer.goalsScored++
            
            if (Random.nextDouble() < 0.70 && xi.size > 1) {
                val assistLineup = xi.filter { it.id != scorer.id && it.position != "GK" }
                if (assistLineup.isNotEmpty()) {
                    val assister = when {
                        assistLineup.any { it.position == "MID" } && Random.nextDouble() < 0.60 -> assistLineup.filter { it.position == "MID" }.random()
                        assistLineup.any { it.position == "FWD" } && Random.nextDouble() < 0.80 -> assistLineup.filter { it.position == "FWD" }.random()
                        else -> assistLineup.random()
                    }
                    assister.assists++
                }
            }
        }
        
        val oppGoalsConceded = goals
        if (oppGoalsConceded == 0) {
            team.roster.forEach {
                if ((it.position == "GK" || it.position == "DEF") && it.id in team.lineupIds) {
                    it.cleanSheets++
                }
            }
        }
        
        val cardsCount = Random.nextInt(0, 3)
        if (cardsCount > 0) {
            repeat(cardsCount) {
                val badGuy = xi.randomOrNull()
                if (badGuy != null) {
                    badGuy.yellowCards++
                    if (badGuy.yellowCards >= 3 && Random.nextDouble() < 0.15) {
                        badGuy.redCards++
                    }
                }
            }
        }
        
        if (Random.nextDouble() < 0.012) {
            val redGuy = xi.randomOrNull()
            if (redGuy != null) {
                redGuy.redCards++
            }
        }
    }

    // --- TRAINING METHOD ---
    fun conductWeeklyTraining(focus: String) {
        val state = _gameState.value ?: return
        if (state.isTrainingConductedThisWeek) {
            showToast("Weekly training has already been wrapped up for this Match Day!")
            return
        }

        val updatedTeams = state.teams.map { t ->
            if (t.isUserControlled) {
                val updatedRoster = t.roster.map { p ->
                    when (focus) {
                        "ATTACK" -> {
                            val nextAttack = if (p.position == "FWD" || p.position == "MID") (p.ratingAttack + 1).coerceAtMost(99) else p.ratingAttack
                            val nextMorale = if (p.position == "FWD" || p.position == "MID") (p.morale + 4).coerceAtMost(99) else p.morale
                            p.copy(
                                ratingAttack = nextAttack,
                                stamina = (p.stamina - 6).coerceAtLeast(10),
                                fatigue = (p.fatigue + 8).coerceAtMost(100),
                                morale = nextMorale
                            )
                        }
                        "DEFENSE" -> {
                            val nextDefend = if (p.position == "DEF" || p.position == "GK") (p.ratingDefend + 1).coerceAtMost(99) else p.ratingDefend
                            val nextMorale = if (p.position == "DEF" || p.position == "GK") (p.morale + 4).coerceAtMost(99) else p.morale
                            p.copy(
                                ratingDefend = nextDefend,
                                stamina = (p.stamina - 6).coerceAtLeast(10),
                                fatigue = (p.fatigue + 8).coerceAtMost(100),
                                morale = nextMorale
                            )
                        }
                        "FITNESS" -> {
                            p.copy(
                                stamina = (p.stamina + 20).coerceAtMost(100),
                                fatigue = (p.fatigue - 15).coerceAtLeast(0),
                                morale = (p.morale + 3).coerceAtMost(99)
                            )
                        }
                        else -> { // TACTICAL
                            val nextAttack = if (Random.nextDouble() < 0.25) (p.ratingAttack + 1).coerceAtMost(99) else p.ratingAttack
                            val nextDefend = if (Random.nextDouble() < 0.25) (p.ratingDefend + 1).coerceAtMost(99) else p.ratingDefend
                            p.copy(
                                ratingAttack = nextAttack,
                                ratingDefend = nextDefend,
                                stamina = (p.stamina - 3).coerceAtLeast(10),
                                fatigue = (p.fatigue + 4).coerceAtMost(100),
                                morale = (p.morale + 10).coerceAtMost(99)
                            )
                        }
                    }
                }
                t.copy(roster = updatedRoster)
            } else t
        }

        val nextState = state.copy(
            teams = updatedTeams,
            isTrainingConductedThisWeek = true
        )

        _gameState.value = nextState
        saveGame()
        
        val focusTitle = when(focus) {
            "ATTACK" -> "Attacking Drills & Overlaps"
            "DEFENSE" -> "Defensive Blocks & Marking"
            "FITNESS" -> "Physical Conditioning & Recovery"
            else -> "Tactical Coordination & Setup"
        }
        showToast("⚽ Prepared! Training session completed: $focusTitle.")
    }

    // --- PRESIDENTIAL ELECTIONS METHOD ---
    val isPresidentialElectionDue: Boolean get() {
        val state = _gameState.value ?: return false
        val mDay = state.currentMatchDay
        // Election is due on match days 6 and 11, if not voted yet
        return (mDay == 6 || mDay == 11) && state.lastPresidentialVoteMatchDay < mDay
    }

    fun castPresidentialVote(choice: String) {
        val state = _gameState.value ?: return
        val mDay = state.currentMatchDay

        // Details of candidates
        val selectedPresName = if (choice == "A") "Arthur Pendelton" else "Julian Sterling"
        val selectedPresType = if (choice == "A") "FINANCIAL" else "INVESTOR"
        val selectedPresEffect = if (choice == "A") "-20% Stadium Upgrade Expenses" else "£10,000,000 Tactical Cash Injection"

        // Cash injection if choosing candidate B
        var cashPrize = 0L
        if (choice == "B") {
            cashPrize = 10_000_000L
        }

        // Apply new president to user team and inject cash if applicable
        var userTeamBudgetBonus = 0L
        val updatedTeams = state.teams.map { t ->
            if (t.isUserControlled) {
                val newBudget = t.budget + cashPrize
                userTeamBudgetBonus = cashPrize
                t.copy(
                    presidentName = selectedPresName,
                    presidentType = selectedPresType,
                    presidentEffect = selectedPresEffect,
                    budget = newBudget
                )
            } else {
                // RUN ELECTIONS FOR ALL OTHER LEAGUE CLUBS AT THE SAME TIME
                val cpuCandidateName = listOf("Victor Vance", "Clara Croft", "Oliver Stone", "Derrick Rose").random()
                val cpuCandidateType = listOf("BALANCED", "FINANCIAL", "INVESTOR", "YOUTH").random()
                val cpuCandidateEffect = when(cpuCandidateType) {
                    "FINANCIAL" -> "-20% Stadium Expenses"
                    "INVESTOR" -> "+£10m Transfer Cash Flow"
                    "YOUTH" -> "+25% Academy Success Chance"
                    else -> "Coordinated Commercial Deals"
                }
                t.copy(
                    presidentName = cpuCandidateName,
                    presidentType = cpuCandidateType,
                    presidentEffect = cpuCandidateEffect
                )
            }
        }

        // Generate news broadcast bulletin inbox notification
        val electionBulletins = StringBuilder()
        electionBulletins.append("🗳️ **LEAGUE-WIDE CLUB PRESIDENT OUTCOMES**\n\n")
        updatedTeams.forEach { t ->
            electionBulletins.append("• **${t.name}**: Elected **${t.presidentName}** (Focus: ${t.presidentType})\n")
        }

        val inboxMessage = InboxMessage(
            id = "msg_election_${mDay}_${System.currentTimeMillis()}",
            sender = "League Broadcasting Commission",
            subject = "🗳️ ELECTION RESULTS: New Club Presidents Sworn In!",
            content = "Dear Executive Staff,\n\nFollowing a historic league-wide vote conducted across all clubs, millions of supporters have decided on who will take over executive seats.\n\nHere are the newly elected leaders across the country:\n\n${electionBulletins.toString()}\n\nAs managers, you must align directly with their administrative philosophies.\n\nGood luck!"
        )

        val nextState = state.copy(
            teams = updatedTeams,
            inbox = listOf(inboxMessage) + state.inbox,
            lastPresidentialVoteMatchDay = mDay,
            transferBudget = state.transferBudget + userTeamBudgetBonus
        )

        _gameState.value = nextState
        saveGame()
        showToast("🗳️ Presidential Campaign ended! ${selectedPresName} has taken office.")
    }

    // --- RETIRED PLAYER TO STAFF CONVERTER ---
    fun convertRetiredPlayerToStaff(teamId: String, playerId: String, role: String) {
        val state = _gameState.value ?: return
        val team = state.teams.firstOrNull { it.id == teamId } ?: return
        val player = team.roster.firstOrNull { it.id == playerId } ?: return

        // 1. Create StaffMember from player's characteristics
        val stars = (player.overallRating / 17).coerceIn(1, 5)
        val wage = (player.contractWage / 3).coerceAtLeast(300L)
        val specialtyObj = when(role) {
            "COACH" -> Pair("TRAINING_BOOST", "Tactical Visionary: Boosts rating development of midfielders")
            "PHYSIO" -> Pair("INJURY_REDUCTION", "Deep Tissue Therapy: Accelerates injury recoveries by 1 week")
            else -> Pair("SCOUT_QUALITY", "Talent Scout: Increases youth player quality pool by 15%")
        }

        val newStaff = StaffMember(
            id = "staff_retired_${player.id}_${System.currentTimeMillis()}",
            name = player.name,
            role = role,
            ratingStars = stars,
            cost = 0L, // Free transition
            weeklyWage = wage,
            specialty = specialtyObj.second,
            description = "Former dynamic player for ${team.name} who retired at Age ${player.age} with ${player.overallRating} OVR.",
            effectDescription = "Specialized transition staff member.",
            specialtyType = specialtyObj.first,
            age = player.age
        )

        // 2. Remove retired player from roster
        val updatedRoster = team.roster.filter { it.id != player.id }
        
        val updatedTeams = state.teams.map { t ->
            if (t.id == teamId) {
                t.copy(
                    roster = updatedRoster,
                    lineupIds = t.lineupIds.filter { it != player.id }
                )
            } else t
        }

        val nextState = state.copy(
            teams = updatedTeams,
            hiredStaff = state.hiredStaff + newStaff
        )

        _gameState.value = nextState
        saveGame()
        showToast("🤝 Signed! Retired club legend ${player.name} is now our backroom ${role.lowercase()}!")
    }
}
