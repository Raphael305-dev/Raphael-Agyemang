package com.example.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Player(
    val id: String,
    val name: String,
    val age: Int,
    val position: String, // GK, DEF, MID, FWD
    val ratingAttack: Int,  // 1-99
    val ratingDefend: Int,  // 1-99
    var stamina: Int = 100, // 0-100
    var fatigue: Int = 0,    // 0-100 (player gets tired, rests on bench)
    var morale: Int = 85,    // 0-100
    val value: Long,         // Transfer value
    val contractWage: Long,  // Weekly wage
    var form: Float = 6.5f,  // Average performance rating
    var goalsScored: Int = 0,
    var assists: Int = 0,
    var cleanSheets: Int = 0,
    var matchesPlayed: Int = 0,
    var yellowCards: Int = 0,
    var redCards: Int = 0,
    var isListed: Boolean = false,
    var askingPrice: Long = 0L,
    var contractYearsRemaining: Int = 3,
    var injuryWeeksRemaining: Int = 0,
    var injuryType: String? = null,
    var isLoanedOut: Boolean = false,
    var loanDestinationClub: String? = null,
    var loanRemainingWeeks: Int = 0,
    var isLoanedIn: Boolean = false,
    var loanSourceClub: String? = null
) {
    val overallRating: Int get() = when (position) {
        "GK" -> ratingDefend
        "DEF" -> ((ratingDefend * 0.8) + (ratingAttack * 0.2)).toInt()
        "MID" -> ((ratingDefend * 0.5) + (ratingAttack * 0.5)).toInt()
        "FWD" -> ((ratingAttack * 0.8) + (ratingDefend * 0.2)).toInt()
        else -> (ratingAttack + ratingDefend) / 2
    }

    val currentPotential: Int get() = overallRating
    val futurePotential: Int get() {
        val deterministicDiff = (Math.abs(id.hashCode()) % 11) + 2
        val potentialDiff = when (age) {
            in 16..20 -> deterministicDiff + 10
            in 21..24 -> deterministicDiff + 5
            in 25..28 -> deterministicDiff + 2
            else -> 0
        }
        return (overallRating + potentialDiff).coerceAtMost(99)
    }
}

@JsonClass(generateAdapter = true)
data class Team(
    val id: String,
    val name: String,
    val isUserControlled: Boolean,
    var budget: Long = 45_000_000L,
    var roster: List<Player> = emptyList(),
    var formation: String = "4-4-2", // "4-4-2", "4-3-3", "3-5-2", "5-4-1"
    var playMentality: String = "BALANCED", // "DEFENSIVE", "BALANCED", "ATTACKING", "COUNTER"
    var pressingIntensity: String = "NORMAL", // "CONSERVATIVE", "NORMAL", "AGGRESSIVE"
    var captainId: String? = null,
    var lineupIds: List<String> = emptyList(), // Starting XI player IDs
    var stadiumName: String = "Club Stadium",
    var stadiumCapacity: Int = 30000,
    var stadiumTicketPrice: Int = 30,
    var fanSatisfaction: Int = 80, // 0-100
    var boardConfidence: Int = 75, // 0-100
    var medicalLevel: Int = 1,
    var academyLevel: Int = 1,
    var clubStoreLevel: Int = 1,
    var trainingLevel: Int = 1,
    var scoutingLevel: Int = 1,
    var presidentName: String = "Sir Reginald Sterling",
    var presidentType: String = "BALANCED", // "BALANCED", "FINANCIAL", "INVESTOR", "YOUTH"
    var presidentEffect: String = "Balanced Board Expectations"
) {
    fun getStartingXI(): List<Player> = roster.filter { it.id in lineupIds }
    fun getBench(): List<Player> = roster.filter { it.id !in lineupIds }
    
    val teamRating: Int get() {
        val starting = getStartingXI()
        if (starting.isEmpty()) return 50
        return starting.map { it.overallRating }.average().toInt()
    }
}

@JsonClass(generateAdapter = true)
data class LeagueStanding(
    val teamId: String,
    val teamName: String,
    var played: Int = 0,
    var won: Int = 0,
    var drawn: Int = 0,
    var lost: Int = 0,
    var goalsFor: Int = 0,
    var goalsAgainst: Int = 0,
    var points: Int = 0
) {
    val goalDifference: Int get() = goalsFor - goalsAgainst
}

@JsonClass(generateAdapter = true)
data class Fixture(
    val id: String,
    val matchDay: Int,
    val homeTeamId: String,
    val homeTeamName: String,
    val awayTeamId: String,
    val awayTeamName: String,
    var isPlayed: Boolean = false,
    var homeScore: Int = 0,
    var awayScore: Int = 0,
    var eventsLog: List<MatchCommentaryEvent> = emptyList(),
    var pitchCondition: String = "EXCELLENT", // "EXCELLENT", "SOGGY", "MUDDY", "FROZEN"
    var stadiumAttendance: Int = 0,
    var revenueGenerated: Long = 0L,
    var homePossession: Int = 50,
    var awayPossession: Int = 50,
    var homeShotsOnTarget: Int = 0,
    var awayShotsOnTarget: Int = 0,
    var homePassCompletion: Int = 75,
    var awayPassCompletion: Int = 75,
    var highlightRecap: String? = null,
    var isRivalryMatch: Boolean = false,
    var rivalryType: String? = null
)

@JsonClass(generateAdapter = true)
data class MatchCommentaryEvent(
    val minute: Int,
    val type: String, // "GOAL", "MISS", "SAVE", "CARD", "INFO"
    val description: String,
    val currentHomeScore: Int,
    val currentAwayScore: Int
)

@JsonClass(generateAdapter = true)
data class ScoutCandidate(
    val id: String,
    val name: String,
    val age: Int,
    val position: String,
    val ratingAttack: Int,
    val ratingDefend: Int,
    val value: Long,
    val wage: Long,
    val description: String // e.g. "Young Promising Talent", "Rock Solid Defender"
) {
    val overallRating: Int get() = when (position) {
        "GK" -> ratingDefend
        "DEF" -> ((ratingDefend * 0.8) + (ratingAttack * 0.2)).toInt()
        "MID" -> ((ratingDefend * 0.5) + (ratingAttack * 0.5)).toInt()
        "FWD" -> ((ratingAttack * 0.8) + (ratingDefend * 0.2)).toInt()
        else -> (ratingAttack + ratingDefend) / 2
    }

    val currentPotential: Int get() = overallRating
    val futurePotential: Int get() {
        val deterministicDiff = (Math.abs(id.hashCode()) % 11) + 2
        val potentialDiff = when (age) {
            in 16..20 -> deterministicDiff + 10
            in 21..24 -> deterministicDiff + 5
            in 25..28 -> deterministicDiff + 2
            else -> 0
        }
        return (overallRating + potentialDiff).coerceAtMost(99)
    }
}

@JsonClass(generateAdapter = true)
data class InboxMessage(
    val id: String,
    val sender: String,
    val subject: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    var isRead: Boolean = false
)

@JsonClass(generateAdapter = true)
data class TransferBid(
    val id: String,
    val playerId: String,
    val playerName: String,
    val playerRating: Int,
    val playerPosition: String,
    val offeringTeamId: String,
    val offeringTeamName: String,
    val targetTeamId: String,
    val amount: Long,
    val isFromUser: Boolean,
    var status: String = "PENDING" // "PENDING", "ACCEPTED", "REJECTED"
)

@JsonClass(generateAdapter = true)
data class CareerHistoryRecord(
    val seasonYear: Int,
    val clubName: String,
    val finalPosition: Int,
    val points: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val trophyWon: Boolean,
    val wasSacked: Boolean = false
)

@JsonClass(generateAdapter = true)
data class JobOffer(
    val teamId: String,
    val teamName: String,
    val teamRating: Int,
    val budget: Long,
    val reason: String
)

@JsonClass(generateAdapter = true)
data class YouthScout(
    val id: String,
    val name: String,
    val ratingStars: Int, // 1 to 5 stars
    val hireFee: Long,
    val weeklyWage: Long,
    var status: String = "IDLE", // "IDLE", "SEARCHING", "REPORT_READY", "COOLDOWN"
    var currentRegion: String? = null,
    var searchType: String? = null, // "FWD", "MID", "DEF", "GK", "ANY"
    var remainingDays: Int = 0, // days remaining for SEARCHING active mission or COOLDOWN rest
    var foundWonderkid: ScoutCandidate? = null
)

@JsonClass(generateAdapter = true)
data class PressConferenceOption(
    val text: String,
    val teamMoraleImpact: Int,
    val boardConfidenceImpact: Int,
    val explanation: String
)

@JsonClass(generateAdapter = true)
data class PressConferenceQuestion(
    val questionId: String,
    val text: String,
    val options: List<PressConferenceOption>
)

@JsonClass(generateAdapter = true)
data class PressConference(
    val title: String,
    val description: String,
    val questions: List<PressConferenceQuestion>,
    var currentQuestionIndex: Int = 0,
    var answersChosen: List<Int> = emptyList(),
    var completed: Boolean = false
)

@JsonClass(generateAdapter = true)
data class FinancialTransaction(
    val id: String,
    val matchDay: Int,
    val description: String,
    val amount: Long, // positive for revenue, negative for expense
    val category: String // "TICKET", "CONCESSION", "STORE", "WAGES", "TRANSFERS", "UPGRADES", "YOUTH"
)

@JsonClass(generateAdapter = true)
data class Sponsorship(
    val id: String,
    val brandName: String,
    val category: String, // "MAIN_SHIRT", "STADIUM_NAMING", "SLEEVE"
    val durationSeasons: Int,
    var seasonsRemaining: Int,
    var baseWeeklyPayout: Long,
    var winBonus: Long,
    var attendanceThreshold: Int = 0,
    var attendanceBonus: Long = 0L,
    var standingGoalOrdinal: Int = 0, // e.g. Rank index (1, 4 or 6, or 0 for none)
    var standingBonus: Long = 0L,
    var signed: Boolean = false,
    var negotiationRisk: Int = 15, // percent probability of walking away
    var counterCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class BoardGoal(
    val id: String,
    val title: String,
    val description: String,
    val targetType: String, // "WINS", "STADIUM_UPGRADE", "FACILITY_UPGRADE", "SIGN_PLAYER", "BALANCE", "BOARD_CONFID"
    val targetValue: Int,
    var currentValue: Int = 0,
    var completed: Boolean = false,
    var failed: Boolean = false,
    val confidenceReward: Int,
    val confidencePenalty: Int,
    val financialReward: Long = 0L,
    val deadlineMatchDay: Int
)

@JsonClass(generateAdapter = true)
data class SaveState(
    val managerName: String,
    var clubName: String,
    val userTeamId: String,
    var currentMatchDay: Int = 1,
    var teams: List<Team> = emptyList(),
    var standings: List<LeagueStanding> = emptyList(),
    var fixtures: List<Fixture> = emptyList(),
    var scoutCandidates: List<ScoutCandidate> = emptyList(),
    var transferBudget: Long = 45_000_000L,
    var inbox: List<InboxMessage> = emptyList(),
    var gameYear: Int = 2026,
    var isCompleted: Boolean = false,
    var matchLogs: List<String> = emptyList(),
    var pendingBids: List<TransferBid> = emptyList(),
    var careerHistory: List<CareerHistoryRecord> = emptyList(),
    var isUserSacked: Boolean = false,
    var availableJobOffers: List<JobOffer> = emptyList(),
    var lastSeasonPosition: Int = 0,
    var youthScouts: List<YouthScout> = emptyList(),
    var availableScoutsToHire: List<YouthScout> = emptyList(),
    var managerWins: Int = 0,
    var managerDraws: Int = 0,
    var managerLosses: Int = 0,
    var managerMatches: Int = 0,
    var managerGoalsFor: Int = 0,
    var managerGoalsAgainst: Int = 0,
    var activePressConference: PressConference? = null,
    var financialTransactions: List<FinancialTransaction> = emptyList(),
    var activeSponsorships: List<Sponsorship> = emptyList(),
    var availableSponsorshipOffers: List<Sponsorship> = emptyList(),
    var activeBoardGoals: List<BoardGoal> = emptyList(),
    var hiredStaff: List<StaffMember> = emptyList(),
    var availableStaffToHire: List<StaffMember> = emptyList(),
    var isTrainingConductedThisWeek: Boolean = false,
    var lastPresidentialVoteMatchDay: Int = 0,
    var recordSignName: String = "Arthur Pendragon",
    var recordSignValue: Long = 32_000_000L,
    var recordSignClub: String = "London Knights",
    var recordSaleName: String = "Marcus Sterling",
    var recordSaleValue: Long = 48_000_000L,
    var recordSaleClub: String = "Madrid Royal",
    var clubAllTimeBestPlayerName: String = "Thierry Henry",
    var clubAllTimeBestPlayerDesc: String = "Club Legend, 228 Goals, undefeated Invincible of 2004.",
    var globalNewsFeed: List<NewsArticle> = emptyList()
)

@JsonClass(generateAdapter = true)
data class NewsArticle(
    val id: String,
    val matchDay: Int,
    val category: String, // "TRANSFER", "INJURY", "MANAGER", "MATCH_DAY", "AWARD"
    val title: String,
    val body: String,
    val source: String = "Sky Sports"
)

@JsonClass(generateAdapter = true)
data class StaffMember(
    val id: String,
    val name: String,
    val role: String, // "COACH", "PHYSIO", "SCOUT"
    val ratingStars: Int, // 1 to 5 stars
    val cost: Long, // Hiring cost
    val weeklyWage: Long,
    val specialty: String, // Specialty text
    val description: String,
    val effectDescription: String,
    var specialtyType: String, // e.g. "TRAINING_BOOST", "INJURY_REDUCTION", "SCOUT_QUALITY"
    val age: Int = 45,
    var isRetired: Boolean = false
)
