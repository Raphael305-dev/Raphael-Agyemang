package com.example.model

import java.util.UUID
import kotlin.random.Random

object GameEngine {

    private val FIRST_NAMES = listOf(
        "Harry", "Marcus", "Kevin", "Erling", "Martin", "Cole", "Bukayo", "Declan", "Virgil", "Trent",
        "Alisson", "Mo", "Bruno", "Alejandro", "Bernardo", "Phil", "Jack", "Jude", "Kai", "Leroy",
        "Thomas", "Robert", "Manuel", "Jamal", "Leon", "Joshua", "Lautaro", "Nicolo", "Alessandro", "Federico",
        "Rafael", "Theo", "Mike", "Dusan", "Weston", "Christian", "Antoine", "Ousmane", "Kylian", "Warren",
        "Marquinhos", "Achraf", "Gianluigi", "Vinicius", "Rodrygo", "Luka", "Toni", "Thibaut", "Dani", "Pedri",
        "Gavi", "Frenkie", "Ronald", "Marc", "Joao", "Luis", "Karim", "Mason", "Raheem", "Ruben"
    )

    private val LAST_NAMES = listOf(
        "Kane", "Rashford", "De Bruyne", "Haaland", "Odegaard", "Palmer", "Saka", "Rice", "van Dijk", "Alexander-Arnold",
        "Becker", "Salah", "Fernandes", "Garnacho", "Silva", "Foden", "Grealish", "Bellingham", "Havertz", "Sane",
        "Muller", "Lewandowski", "Neuer", "Musiala", "Goretzka", "Kimmich", "Martinez", "Barella", "Bastoni", "Chiesa",
        "Leao", "Hernandez", "Maignan", "Vlahovic", "McKennie", "Pulisic", "Griezmann", "Dembele", "Mbappe", "Zaire-Emery",
        "Hakimi", "Donnarumma", "Junior", "Valverde", "Modric", "Kroos", "Courtois", "Carvajal", "Gonzalez", "Gavira",
        "De Jong", "Araujo", "Stegen", "Felix", "Diaz", "Benzema", "Mount", "Sterling", "Dias", "Stones"
    )

    private val CLUB_PREFIXES = listOf("London", "Manchester", "Liverpool", "Madrid", "Barcelona", "Munich", "Milan", "Paris")
    private val CLUB_SUFFIXES = listOf("FC", "United", "City", "Athletic", "Real", "Inter", "Saint-Germain", "Rovers")

    fun generatePlayer(position: String, qualityMin: Int, qualityMax: Int): Player {
        val firstName = FIRST_NAMES.random()
        val lastName = LAST_NAMES.random()
        val name = "$firstName $lastName"
        val age = Random.nextInt(18, 34)

        val ratAttack: Int
        val ratDefend: Int

        when (position) {
            "GK" -> {
                ratAttack = Random.nextInt(5, 16)
                ratDefend = Random.nextInt(qualityMin.coerceAtLeast(60), qualityMax.coerceAtMost(99))
            }
            "DEF" -> {
                ratAttack = Random.nextInt(10, 46)
                ratDefend = Random.nextInt(qualityMin.coerceAtLeast(60), qualityMax.coerceAtMost(99))
            }
            "MID" -> {
                ratAttack = Random.nextInt(qualityMin - 10, qualityMax - 5).coerceIn(40, 95)
                ratDefend = Random.nextInt(qualityMin - 10, qualityMax - 5).coerceIn(40, 95)
            }
            "FWD" -> {
                ratAttack = Random.nextInt(qualityMin.coerceAtLeast(60), qualityMax.coerceAtMost(99))
                ratDefend = Random.nextInt(10, 46)
            }
            else -> {
                ratAttack = Random.nextInt(qualityMin, qualityMax)
                ratDefend = Random.nextInt(qualityMin, qualityMax)
            }
        }

        // Calculate cost based on overall rating
        val baseRating = when (position) {
            "GK" -> ratDefend
            "DEF" -> ((ratDefend * 0.8) + (ratAttack * 0.2)).toInt()
            "MID" -> ((ratDefend * 0.5) + (ratAttack * 0.5)).toInt()
            "FWD" -> ((ratAttack * 0.8) + (ratDefend * 0.2)).toInt()
            else -> (ratAttack + ratDefend) / 2
        }

        // Value scales exponentially
        val value = when {
            baseRating >= 90 -> Random.nextLong(65_000_000L, 120_000_000L)
            baseRating >= 85 -> Random.nextLong(35_000_000L, 64_000_000L)
            baseRating >= 80 -> Random.nextLong(15_000_000L, 34_000_000L)
            baseRating >= 75 -> Random.nextLong(5_000_000L, 14_000_000L)
            baseRating >= 70 -> Random.nextLong(2_000_000L, 4_900_000L)
            else -> Random.nextLong(500_000L, 1_900_000L)
        }

        val wage = (value * 0.003 + Random.nextLong(2000L, 10000L)).toLong()

        return Player(
            id = UUID.randomUUID().toString(),
            name = name,
            age = age,
            position = position,
            ratingAttack = ratAttack,
            ratingDefend = ratDefend,
            stamina = 100,
            fatigue = 0,
            morale = Random.nextInt(75, 95),
            value = value,
            contractWage = wage,
            form = Random.nextDouble(6.0, 7.5).toFloat(),
            goalsScored = 0,
            assists = 0,
            cleanSheets = 0,
            matchesPlayed = 0,
            contractYearsRemaining = Random.nextInt(1, 5)
        )
    }

    fun generateSquad(clubTier: String): List<Player> {
        val (minQ, maxQ) = when (clubTier) {
            "ELITE" -> Pair(82, 98)
            "STRONG" -> Pair(75, 87)
            "BALANCED" -> Pair(68, 79)
            else -> Pair(60, 73)
        }

        val squad = mutableListOf<Player>()
        // 2 GK
        squad.add(generatePlayer("GK", minQ, maxQ))
        squad.add(generatePlayer("GK", minQ - 5, minQ + 5))

        // 6 DEF
        for (i in 1..6) squad.add(generatePlayer("DEF", minQ, maxQ))

        // 6 MID
        for (i in 1..6) squad.add(generatePlayer("MID", minQ, maxQ))

        // 4 FWD
        for (i in 1..4) squad.add(generatePlayer("FWD", minQ, maxQ))

        return squad
    }

    fun createInitialLeagueState(managerName: String, selectedClubName: String): SaveState {
        val userTeamId = "team_user"
        val opponents = listOf(
            "Manchester Blue" to "ELITE",
            "Manchester Red" to "STRONG",
            "Liverpool Red" to "ELITE",
            "London Gunner" to "ELITE",
            "London Blue" to "STRONG",
            "Madrid Whites" to "ELITE",
            "Munich Giants" to "ELITE"
        )

        val teams = mutableListOf<Team>()

        // Generate user's roster
        val userRoster = generateSquad("STRONG")
        val userTeam = Team(
            id = userTeamId,
            name = selectedClubName,
            isUserControlled = true,
            budget = 40_000_000L,
            roster = userRoster,
            formation = "4-4-2",
            playMentality = "BALANCED",
            pressingIntensity = "NORMAL",
            stadiumName = "$selectedClubName Park",
            stadiumCapacity = Random.nextInt(32, 48) * 1000,
            stadiumTicketPrice = Random.nextInt(28, 42),
            fanSatisfaction = 80
        )
        // Set starting XI
        val (xi, _) = selectAutoLineup(userRoster, "4-4-2")
        userTeam.lineupIds = xi.map { it.id }
        userTeam.captainId = xi.maxByOrNull { it.overallRating }?.id
        teams.add(userTeam)

        // Generate opponents
        opponents.forEachIndexed { index, (name, tier) ->
            val opponentRoster = generateSquad(tier)
            // List 2 random squad players on the transfer block initially
            val listedIndices = opponentRoster.indices.shuffled().take(2)
            listedIndices.forEach { idx ->
                val p = opponentRoster[idx]
                p.isListed = true
                p.askingPrice = (p.value * Random.nextDouble(0.85, 1.25)).toLong()
            }
            val formation = listOf("4-4-2", "4-3-3", "3-5-2", "5-4-1").random()
            val (oppXi, _) = selectAutoLineup(opponentRoster, formation)

            val (stadName, stadCap, stadPrice) = when (name) {
                "Manchester Blue" -> Triple("Etihad Dome", 54000, 45)
                "Manchester Red" -> Triple("Trafford Arena", 73000, 40)
                "Liverpool Red" -> Triple("Anfield Stadium", 53000, 42)
                "London Gunner" -> Triple("Gunners Bowl", 60000, 45)
                "London Blue" -> Triple("Bridge Arena", 41000, 38)
                "Madrid Whites" -> Triple("Castellana Stadium", 81000, 50)
                "Munich Giants" -> Triple("Allianz Bowl", 75000, 44)
                else -> Triple("$name Ground", Random.nextInt(20, 50) * 1000, Random.nextInt(25, 45))
            }

            val opponentTeam = Team(
                id = "team_opp_${index + 1}",
                name = name,
                isUserControlled = false,
                budget = if (tier == "ELITE") 80_000_000L else 45_000_000L,
                roster = opponentRoster,
                formation = formation,
                playMentality = listOf("BALANCED", "ATTACKING", "DEFENSIVE", "COUNTER").random(),
                pressingIntensity = listOf("CONSERVATIVE", "NORMAL", "AGGRESSIVE").random(),
                lineupIds = oppXi.map { it.id },
                stadiumName = stadName,
                stadiumCapacity = stadCap,
                stadiumTicketPrice = stadPrice,
                fanSatisfaction = Random.nextInt(75, 88)
            )
            opponentTeam.captainId = oppXi.maxByOrNull { it.overallRating }?.id
            teams.add(opponentTeam)
        }

        // Generate standins/standings
        val standings = teams.map {
            LeagueStanding(teamId = it.id, teamName = it.name)
        }

        // Generate round-robin fixtures (8 teams total)
        val fixtures = generateFixturesList(teams)

        // Initial scouting candidate list
        val scouts = List(6) { generateScoutCandidate() }

        val firstInbox = InboxMessage(
            id = "msg_welcome",
            sender = "Club Board",
            subject = "Welcome to the Club, Boss!",
            content = "Dear Manager $managerName,\n\nWe are absolutely delighted to welcome you to the club. Your objective this season is to build a highly competitive squad, establish core tactical systems, and climb the league table.\n\nYou have an initial transfer budget of £40.0M. Visit the Tactics Hub to finalize your Lineup, search the Scouting Market for fresh targets, and press 'Simulate' when you are ready to kick-off Match Day 1.\n\nGood Luck!\n- The Board"
        )

        return SaveState(
            managerName = managerName,
            clubName = selectedClubName,
            userTeamId = userTeamId,
            currentMatchDay = 1,
            teams = teams,
            standings = standings,
            fixtures = fixtures,
            scoutCandidates = scouts,
            transferBudget = 40_000_000L,
            inbox = listOf(firstInbox),
            gameYear = 2026,
            isCompleted = false,
            youthScouts = emptyList(),
            availableScoutsToHire = generateInitialYouthScoutsToHire(),
            managerWins = 0,
            managerDraws = 0,
            managerLosses = 0,
            managerMatches = 0,
            managerGoalsFor = 0,
            managerGoalsAgainst = 0,
            activeSponsorships = emptyList(),
            availableSponsorshipOffers = generateSponsorshipOffers(),
            activeBoardGoals = generateBoardGoals(),
            hiredStaff = emptyList(),
            availableStaffToHire = generateStaffOffers()
        )
    }

    fun generateStaffOffers(): List<StaffMember> {
        return listOf(
            StaffMember(
                id = "staff_coach_stamina",
                name = "Sir Alex Finch",
                role = "COACH",
                ratingStars = 5,
                cost = 1_500_000L,
                weeklyWage = 18_000L,
                specialty = "High Press Guru",
                description = "Master of athletic conditioning and high-intensity press.",
                effectDescription = "+10 Stamina Recovery during matchday rests.",
                specialtyType = "TRAINING_BOOST",
                age = 56
            ),
            StaffMember(
                id = "staff_coach_tactics",
                name = "Marcus Aurelio",
                role = "COACH",
                ratingStars = 4,
                cost = 1_100_000L,
                weeklyWage = 13_500L,
                specialty = "Tiki-Taka Strategist",
                description = "Tactical mastermind focused on ball possession and strategic buildup.",
                effectDescription = "+15% Tactical control and slightly reduced team fatigue.",
                specialtyType = "TRAINING_BOOST",
                age = 51
            ),
            StaffMember(
                id = "staff_coach_academy",
                name = "Julio Valente",
                role = "COACH",
                ratingStars = 3,
                cost = 600_000L,
                weeklyWage = 7_500L,
                specialty = "Technical Drill Coach",
                description = "Focused on precision passing and shooting exercises.",
                effectDescription = "+20% Chance for healthy players to gain random skill attributes.",
                specialtyType = "TRAINING_BOOST",
                age = 43
            ),
            StaffMember(
                id = "staff_coach_youth",
                name = "Anya Chen",
                role = "COACH",
                ratingStars = 4,
                cost = 950_000L,
                weeklyWage = 11_000L,
                specialty = "Youth Scout Director",
                description = "Passionate about finding local raw talents and nurturing their development.",
                effectDescription = "+15% attribute boost to newly signed youth academy players.",
                specialtyType = "TRAINING_BOOST",
                age = 58
            ),
            StaffMember(
                id = "staff_physio_doctor",
                name = "Dr. Eva Sterling",
                role = "PHYSIO",
                ratingStars = 5,
                cost = 1_800_000L,
                weeklyWage = 22_000L,
                specialty = "Pro Rehab Surgeon",
                description = "Top tier orthopedic expert specialized in severe muscle tears.",
                effectDescription = "+50% Recovery speed to completely heal injured players faster.",
                specialtyType = "INJURY_REDUCTION",
                age = 47
            ),
            StaffMember(
                id = "staff_physio_gordon",
                name = "Gordon Vance",
                role = "PHYSIO",
                ratingStars = 4,
                cost = 1_200_000L,
                weeklyWage = 14_000L,
                specialty = "Hamstring Guru",
                description = "Elite therapist specialized in minimizing athletic muscle strains.",
                effectDescription = "Reduces squad acute muscle injuries by 30%.",
                specialtyType = "INJURY_REDUCTION",
                age = 63
            ),
            StaffMember(
                id = "staff_physio_masseur",
                name = "Milo Fletcher",
                role = "PHYSIO",
                ratingStars = 3,
                cost = 500_000L,
                weeklyWage = 5_000L,
                specialty = "Muscle Therapist",
                description = "Specialized in deep-tissue work and match fatigue relief.",
                effectDescription = "-20% Chance of suffering a physical matchday injury.",
                specialtyType = "INJURY_REDUCTION",
                age = 39
            ),
            StaffMember(
                id = "staff_physio_fitness",
                name = "Liam O'Connor",
                role = "PHYSIO",
                ratingStars = 3,
                cost = 450_000L,
                weeklyWage = 4_500L,
                specialty = "Recovery Coach",
                description = "Dedicated physical fitness instructor focusing on fast bench recovery.",
                effectDescription = "Regenerates bench player stamina 10% faster.",
                specialtyType = "INJURY_REDUCTION",
                age = 35
            ),
            StaffMember(
                id = "staff_scout_legend",
                name = "Sven Larsson",
                role = "SCOUT",
                ratingStars = 5,
                cost = 2_000_000L,
                weeklyWage = 25_000L,
                specialty = "Talent Magnet",
                description = "Legendary European scout credited with discovering 12 world class wonderkids.",
                effectDescription = "+20 rating points range to all newly scouted players.",
                specialtyType = "SCOUT_QUALITY",
                age = 65
            ),
            StaffMember(
                id = "staff_scout_undervalued",
                name = "Lucas Thorne",
                role = "SCOUT",
                ratingStars = 4,
                cost = 1_250_000L,
                weeklyWage = 15_000L,
                specialty = "Undervalued Finder",
                description = "Fuses digital intelligence with raw scouting to pinpoint highly optimized deals.",
                effectDescription = "Injects discounted high potential signings to your scouting inbox weekly.",
                specialtyType = "SCOUT_QUALITY",
                age = 41
            ),
            StaffMember(
                id = "staff_scout_analyst",
                name = "Maya Lin",
                role = "SCOUT",
                ratingStars = 3,
                cost = 700_000L,
                weeklyWage = 8_000L,
                specialty = "Statistical Analyst",
                description = "Combines high-tech data modeling with traditional squad scouting.",
                effectDescription = "+10 rating points range to all newly scouted players.",
                specialtyType = "SCOUT_QUALITY",
                age = 38
            ),
            StaffMember(
                id = "staff_scout_hans",
                name = "Hans Gruber",
                role = "SCOUT",
                ratingStars = 4,
                cost = 1_150_000L,
                weeklyWage = 13_000L,
                specialty = "Global Scouting Networks",
                description = "Veteran scout with a huge database across South America and Europe.",
                effectDescription = "+20% boost to scout region search efficiency.",
                specialtyType = "SCOUT_QUALITY",
                age = 64
            )
        )
    }

    fun generateInitialYouthScoutsToHire(): List<YouthScout> {
        return listOf(
            YouthScout("scout_1", "Gabriel Silva", 5, 2_400_000L, 22_000L),
            YouthScout("scout_2", "Hans Müller", 4, 1_400_000L, 14_000L),
            YouthScout("scout_3", "Diego Alvarez", 3, 750_000L, 7_500L),
            YouthScout("scout_4", "Kwame Mensah", 2, 350_000L, 3_500L),
            YouthScout("scout_5", "Jean Dupont", 1, 120_000L, 1_800L)
        )
    }

    fun generateWonderkid(ratingStars: Int, region: String, searchPosition: String): ScoutCandidate {
        val position = if (searchPosition == "ANY") listOf("GK", "DEF", "MID", "FWD").random() else searchPosition
        
        // Quality range based on scout's stars (1-5)
        val minQ = 55 + (ratingStars * 4) + Random.nextInt(0, 5)
        val maxQ = 65 + (ratingStars * 6) + Random.nextInt(3, 8)
        
        // Generate Regional specific names!
        val firstName = when (region) {
            "South America" -> listOf("Enzo", "Mateo", "Thiago", "Lucas", "Gabriel", "Luiz", "Felipe", "Alexis", "Santiago", "Ignacio", "Diego").random()
            "West Africa" -> listOf("Kofi", "Musa", "Sadio", "Victor", "Samuel", "Abubakar", "Amara", "Didier", "Ousmane", "Boubacar", "Chidi").random()
            "Western Europe" -> listOf("Arthur", "Theo", "Lukas", "Bastien", "Noah", "Oliver", "Maximilian", "Sandro", "Kevin", "Leo", "Marc").random()
            "East Asia" -> listOf("Hiro", "Daiki", "Seung", "Min", "Takahiro", "Kento", "Joon", "Sang", "Chen", "Yong", "Wei").random()
            "Eastern Europe" -> listOf("Luka", "Milan", "Ivan", "Kiril", "Andrei", "Tomas", "Dragan", "Stefan", "Marek", "Pavel", "Nikolai").random()
            else -> listOf("Jack", "Harry", "Mason", "Jude", "Connor", "Bobby", "Tyler", "Liam", "Ethan").random()
        }
        val lastName = when (region) {
            "South America" -> listOf("Silva", "Santos", "Rodriguez", "Fernandez", "Martinez", "Gomez", "Diaz", "Alvarez", "Castillo", "Torres").random()
            "West Africa" -> listOf("Mensah", "Abubakar", "Keita", "Toure", "Diop", "Okocha", "Iwobi", "Koulibaly", "Adebayor", "Sow").random()
            "Western Europe" -> listOf("Müller", "Dubois", "Smith", "Jones", "Lefebvre", "Janssen", "Weber", "Schmidt", "Green", "White").random()
            "East Asia" -> listOf("Sato", "Tanaka", "Kim", "Park", "Lee", "Suzuki", "Watanabe", "Chen", "Wang", "Takahashi").random()
            "Eastern Europe" -> listOf("Modric", "Kovacic", "Ivanov", "Petrov", "Novak", "Dmitriev", "Popov", "Sokolov", "Shevchenko").random()
            else -> listOf("Smith", "Taylor", "Brown", "Walker", "Davis", "Wilson", "Carter").random()
        }
        val fullName = "$firstName $lastName"
        
        val attack = if (position == "FWD") Random.nextInt(minQ + 5, maxQ + 10).coerceAtMost(99) 
                     else if (position == "MID") Random.nextInt(minQ, maxQ).coerceAtMost(99)
                     else Random.nextInt(minQ - 20, minQ).coerceAtLeast(10)
                     
        val defend = if (position == "DEF" || position == "GK") Random.nextInt(minQ + 5, maxQ + 10).coerceAtMost(99)
                     else if (position == "MID") Random.nextInt(minQ, maxQ).coerceAtMost(99)
                     else Random.nextInt(minQ - 20, minQ).coerceAtLeast(10)
                     
        val finalAttack = if (position == "GK") 5 else attack
        val finalDefend = defend
        
        val baseOverall = when (position) {
            "GK" -> finalDefend
            "DEF" -> ((finalDefend * 0.8) + (finalAttack * 0.2)).toInt()
            "MID" -> ((finalDefend * 0.5) + (finalAttack * 0.5)).toInt()
            "FWD" -> ((finalAttack * 0.8) + (finalDefend * 0.2)).toInt()
            else -> (finalAttack + finalDefend) / 2
        }

        // Value of high-potential 16-year-old wonderkids
        val valueMultiplier = 1.0 + (ratingStars * 0.3)
        val youthValue = (baseOverall * baseOverall * 2000L * valueMultiplier).toLong()
        val wage = (baseOverall * 45).toLong()
        
        // Custom Descriptions for wonderkids based on scout quality and baseOverall
        val descOutput = when {
            ratingStars == 5 && baseOverall >= 74 -> "👑 GENERATIONAL WONDERKID (A+ Potential)"
            ratingStars >= 4 && baseOverall >= 70 -> "⭐ HIGHLY PROMISING ACADEMY PROSPECT (A Potential)"
            baseOverall >= 65 -> "✨ SOLID SQUAD PROSPECT (B Potential)"
            else -> "📈 DEVELOPING YOUTH SQUAD DEPTH (C Potential)"
        }
        
        return ScoutCandidate(
            id = "wonderkid_${UUID.randomUUID()}",
            name = fullName,
            age = listOf(15, 16, 17).random(),
            position = position,
            ratingAttack = finalAttack,
            ratingDefend = finalDefend,
            value = youthValue,
            wage = wage,
            description = "$descOutput - Scouted in $region"
        )
    }

    fun generateFixturesList(teams: List<Team>): List<Fixture> {
        val fixtures = mutableListOf<Fixture>()
        val n = teams.size
        val teamList = teams.toMutableList()
        var fid = 1

        // Use standard round-robin scheduler (Circle method)
        for (matchDay in 1..(n - 1) * 2) {
            val isReturnLeg = matchDay > (n - 1)
            
            for (i in 0 until n / 2) {
                val homeIdx = i
                val awayIdx = n - 1 - i

                val home = teamList[homeIdx]
                val away = teamList[awayIdx]

                val h = if (isReturnLeg) away else home
                val a = if (isReturnLeg) home else away

                val conditionRoll = Random.nextDouble()
                val condition = when {
                    conditionRoll < 0.55 -> "EXCELLENT"
                    conditionRoll < 0.72 -> "SOGGY"
                    conditionRoll < 0.88 -> "MUDDY"
                    else -> "FROZEN"
                }

                fixtures.add(
                    Fixture(
                        id = "f_$fid",
                        matchDay = matchDay,
                        homeTeamId = h.id,
                        homeTeamName = h.name,
                        awayTeamId = a.id,
                        awayTeamName = a.name,
                        pitchCondition = condition
                    )
                )
                fid++
            }
            // Rotate team list (keep index 0 fixed)
            val last = teamList.removeAt(teamList.size - 1)
            teamList.add(1, last)
        }
        return fixtures
    }

    fun selectAutoLineup(roster: List<Player>, formation: String): Pair<List<Player>, List<Player>> {
        // Find best players according to formation requirements
        // e.g. 4-4-2 -> 1 GK, 4 DEF, 4 MID, 2 FWD
        // 4-3-3 -> 1 GK, 4 DEF, 3 MID, 3 FWD
        // 3-5-2 -> 1 GK, 3 DEF, 5 MID, 2 FWD
        // 5-4-1 -> 1 GK, 5 DEF, 4 MID, 1 FWD
        val healthy = roster.filter { it.injuryWeeksRemaining == 0 }
        val pool = if (healthy.size >= 11) healthy else roster

        val gks = pool.filter { it.position == "GK" }.sortedByDescending { it.overallRating }
        val defs = pool.filter { it.position == "DEF" }.sortedByDescending { it.overallRating }
        val mids = pool.filter { it.position == "MID" }.sortedByDescending { it.overallRating }
        val fwds = pool.filter { it.position == "FWD" }.sortedByDescending { it.overallRating }

        val (reqDef, reqMid, reqFwd) = when (formation) {
            "4-3-3" -> Triple(4, 3, 3)
            "3-5-2" -> Triple(3, 5, 2)
            "5-4-1" -> Triple(5, 4, 1)
            else -> Triple(4, 4, 2) // default is 4-4-2
        }

        val startingXI = mutableListOf<Player>()

        // Add 1 GK
        if (gks.isNotEmpty()) startingXI.add(gks.first())

        // Add DEF
        startingXI.addAll(defs.take(reqDef))
        if (defs.size < reqDef) {
            val leftover = pool.filter { it !in startingXI && it.position != "GK" }
                .sortedByDescending { it.overallRating }
            startingXI.addAll(leftover.take(reqDef - defs.size))
        }

        // Add MID
        val remainingMids = mids.filter { it !in startingXI }
        startingXI.addAll(remainingMids.take(reqMid))
        if (startingXI.size < reqMid + reqDef + 1) {
            val needed = (reqMid + reqDef + 1) - startingXI.size
            val leftover = pool.filter { it !in startingXI && it.position != "GK" }
                .sortedByDescending { it.overallRating }
            startingXI.addAll(leftover.take(needed))
        }

        // Add FWD
        val remainingFwds = fwds.filter { it !in startingXI }
        startingXI.addAll(remainingFwds.take(reqFwd))
        if (startingXI.size < 11) {
            val needed = 11 - startingXI.size
            val leftover = pool.filter { it !in startingXI && it.position != "GK" }
                .sortedByDescending { it.overallRating }
            startingXI.addAll(leftover.take(needed))
        }

        val finalXI = startingXI.take(11)
        val bench = roster.filter { it !in finalXI }
        return Pair(finalXI, bench)
    }

    fun generateScoutCandidate(positionFilter: String? = null, label: String = "Normal Search", minRating: Int = 65, maxRating: Int = 92): ScoutCandidate {
        val pos = positionFilter ?: listOf("GK", "DEF", "MID", "FWD").random()
        val p = generatePlayer(pos, minRating, maxRating)

        val desc = when {
            p.overallRating >= 88 -> "World Class Superstar"
            p.overallRating >= 83 -> "Tactical Engine / High Caliber"
            p.overallRating >= 78 && p.age <= 22 -> "Wonderkid Potential"
            p.overallRating >= 75 -> "Reliable Squad Anchor"
            else -> "Bargain Squad Depth Specialist"
        }

        return ScoutCandidate(
            id = UUID.randomUUID().toString(),
            name = p.name,
            age = p.age,
            position = p.position,
            ratingAttack = p.ratingAttack,
            ratingDefend = p.ratingDefend,
            value = p.value,
            wage = p.contractWage,
            description = desc
        )
    }

    // Match engine simulation computation
    fun runCpuMatchEndScore(home: Team, away: Team, pitchCondition: String = "EXCELLENT"): Pair<Int, Int> {
        val homeRating = home.teamRating
        val awayRating = away.teamRating

        val homeMentalityMod = when (home.playMentality) {
            "ATTACKING" -> 1.05f
            "DEFENSIVE" -> 0.90f
            else -> 1.00f
        }
        val awayMentalityMod = when (away.playMentality) {
            "ATTACKING" -> 1.05f
            "DEFENSIVE" -> 0.90f
            else -> 1.00f
        }

        val fanSatisfactionBoost = 1.0f + (home.fanSatisfaction - 50) / 1000.0f
        val weatherPenalty = when (pitchCondition) {
            "FROZEN" -> 0.92f
            "MUDDY" -> 0.95f
            else -> 1.0f
        }

        val hPower = homeRating * homeMentalityMod * fanSatisfactionBoost * weatherPenalty
        val aPower = awayRating * awayMentalityMod * weatherPenalty

        // Expected goals based on rating gap
        val diff = hPower - aPower
        var expectedHome = 1.3
        var expectedAway = 1.1

        if (diff > 15) {
            expectedHome += 1.8
            expectedAway -= 0.5
        } else if (diff > 5) {
            expectedHome += 0.8
            expectedAway -= 0.2
        } else if (diff < -15) {
            expectedAway += 1.8
            expectedHome -= 0.5
        } else if (diff < -5) {
            expectedAway += 0.8
            expectedHome -= 0.2
        }

        // Match luck / Poisson-ish distribution approximation
        val hScore = simulatePoissonGoals(expectedHome)
        val aScore = simulatePoissonGoals(expectedAway)
        return Pair(hScore, aScore)
    }

    private fun simulatePoissonGoals(expected: Double): Int {
        val correctedExp = expected.coerceAtLeast(0.1)
        val L = Math.exp(-correctedExp)
        var k = 0
        var p = 1.0
        do {
            k++
            p *= Random.nextDouble()
        } while (p > L)
        return k - 1
    }

    // Interactive commentary simulator (minute-by-minute)
    fun simulateLiveMinute(
        minute: Int,
        homeTeam: Team,
        awayTeam: Team,
        currentHomeScore: Int,
        currentAwayScore: Int,
        pitchCondition: String = "EXCELLENT",
        isRivalry: Boolean = false
    ): MatchCommentaryEvent? {
        val homeXI = homeTeam.getStartingXI()
        val awayXI = awayTeam.getStartingXI()

        if (homeXI.isEmpty() || awayXI.isEmpty()) {
            return MatchCommentaryEvent(minute, "INFO", "Kick-off delayed due to insufficient lineup.", currentHomeScore, currentAwayScore)
        }

        // Baseline chance calculations
        val hAtt = homeXI.map { it.ratingAttack }.average()
        val hDef = homeXI.map { it.ratingDefend }.average()
        val aAtt = awayXI.map { it.ratingAttack }.average()
        val aDef = awayXI.map { it.ratingDefend }.average()

        // Tactical alterations
        val hMentality = homeTeam.playMentality
        val aMentality = awayTeam.playMentality

        val hAttModifier = if (hMentality == "ATTACKING") 1.15 else if (hMentality == "DEFENSIVE") 0.8 else 1.0
        val hDefModifier = if (hMentality == "DEFENSIVE") 1.2 else if (hMentality == "ATTACKING") 0.85 else 1.0

        val aAttModifier = if (aMentality == "ATTACKING") 1.15 else if (aMentality == "DEFENSIVE") 0.8 else 1.0
        val aDefModifier = if (aMentality == "DEFENSIVE") 1.2 else if (aMentality == "ATTACKING") 0.85 else 1.0

        // Formation alterations
        val hForm = homeTeam.formation
        val aForm = awayTeam.formation

        var hAttFormMod = 1.0
        var hDefFormMod = 1.0
        var hMidFormMod = 1.0

        var aAttFormMod = 1.0
        var aDefFormMod = 1.0
        var aMidFormMod = 1.0

        when (hForm) {
            "4-3-3" -> {
                hAttFormMod = 1.25
                hDefFormMod = 0.80
                hMidFormMod = 0.95
            }
            "5-4-1" -> {
                hAttFormMod = 0.75
                hDefFormMod = 1.30
                hMidFormMod = 1.00
            }
            "3-5-2" -> {
                hAttFormMod = 1.05
                hDefFormMod = 0.85
                hMidFormMod = 1.35
            }
            else -> { // Balanced (4-4-2, etc)
                hAttFormMod = 1.00
                hDefFormMod = 1.00
                hMidFormMod = 1.05
            }
        }

        when (aForm) {
            "4-3-3" -> {
                aAttFormMod = 1.25
                aDefFormMod = 0.80
                aMidFormMod = 0.95
            }
            "5-4-1" -> {
                aAttFormMod = 0.75
                aDefFormMod = 1.30
                aMidFormMod = 1.00
            }
            "3-5-2" -> {
                aAttFormMod = 1.05
                aDefFormMod = 0.85
                aMidFormMod = 1.35
            }
            else -> {
                aAttFormMod = 1.00
                aDefFormMod = 1.00
                aMidFormMod = 1.05
            }
        }

        val finalHAtt = hAtt * hAttModifier * hAttFormMod
        val finalHDef = hDef * hDefModifier * hDefFormMod
        val finalAAtt = aAtt * aAttModifier * aAttFormMod
        val finalADef = aDef * aDefModifier * aDefFormMod

        // Event chances (incorporates midfield control factor)
        val hMidfieldControl = (finalHAtt * hMidFormMod) / ((finalHAtt * hMidFormMod) + (finalAAtt * aMidFormMod))
        
        // Random rolls
        if (Random.nextDouble() > 0.08) return null // Only 8% chance of an update each minute

        val isHomeAttack = Random.nextDouble() < hMidfieldControl
        val striker = if (isHomeAttack) homeXI.filter { it.position == "FWD" }.randomOrNull() ?: homeXI.random()
                      else awayXI.filter { it.position == "FWD" }.randomOrNull() ?: awayXI.random()
        val defender = if (isHomeAttack) awayXI.filter { it.position == "DEF" }.randomOrNull() ?: awayXI.random()
                       else homeXI.filter { it.position == "DEF" }.randomOrNull() ?: homeXI.random()
        val gk = if (isHomeAttack) awayXI.firstOrNull { it.position == "GK" } ?: awayXI.random()
                 else homeXI.firstOrNull { it.position == "GK" } ?: homeXI.random()

        val attackingTeam = if (isHomeAttack) homeTeam.name else awayTeam.name
        val defendingTeam = if (isHomeAttack) awayTeam.name else homeTeam.name

        // Supporters home ground acoustic boost
        val fanMultiplier = 1.0 + (homeTeam.fanSatisfaction - 50) / 1000.0 // e.g. 1.03x at 80
        val homeAdvantage = if (isHomeAttack) fanMultiplier else 1.0
        val defenseHomeAdvantage = if (!isHomeAttack) fanMultiplier else 1.0

        // Weather/pitch penalty on coordination
        val weatherFactor = when (pitchCondition) {
            "FROZEN" -> 0.88
            "MUDDY" -> 0.93
            else -> 1.0
        }

        // Determine outcome: Shot on target, blocked, saved, or GOAL
        val attRoll = striker.ratingAttack * Random.nextDouble(0.7, 1.3) * homeAdvantage * weatherFactor
        val defRoll = defender.ratingDefend * Random.nextDouble(0.6, 1.2) * defenseHomeAdvantage * weatherFactor
        val gkRoll = gk.ratingDefend * Random.nextDouble(0.7, 1.3) * defenseHomeAdvantage * weatherFactor

        val activeAttackingTeam = if (isHomeAttack) homeTeam else awayTeam
        val activeDefendingTeam = if (isHomeAttack) awayTeam else homeTeam

        val tacticsRemark = when {
            activeAttackingTeam.playMentality == "ATTACKING" && Random.nextDouble() < 0.4 -> 
                " (Excellent fluid movement, highly energized by ${activeAttackingTeam.name}'s ultra-offensive setup!)"
            activeAttackingTeam.formation == "4-3-3" && Random.nextDouble() < 0.4 -> 
                " (Slick forward combination play, facilitated by ${activeAttackingTeam.name}'s signature 4-3-3 shape!)"
            activeAttackingTeam.formation == "3-5-2" && Random.nextDouble() < 0.4 -> 
                " (Superb central ball supply, initiated by ${activeAttackingTeam.name}'s dense 3-5-2 midfield pivot!)"
            activeDefendingTeam.playMentality == "DEFENSIVE" && Random.nextDouble() < 0.4 -> 
                " (Struggling to find clean space, heavily frustrated by ${activeDefendingTeam.name}'s cohesive defensive low-block.)"
            activeDefendingTeam.formation == "5-4-1" && Random.nextDouble() < 0.4 -> 
                " (Completely shut down, denied by ${activeDefendingTeam.name}'s robust 5-4-1 protective barrier.)"
            else -> ""
        }

        return when {
            attRoll > defRoll + 15 && attRoll > gkRoll + 10 -> {
                // GOAL!
                val hScore = if (isHomeAttack) currentHomeScore + 1 else currentHomeScore
                val aScore = if (isHomeAttack) currentAwayScore else currentAwayScore + 1
                val desc = listOf(
                    "${striker.name} picks up the ball in the box, pivots elegantly, and fires it past the diving ${gk.name}! GOAL for $attackingTeam!",
                    "Incredible teamwork! ${striker.name} receives a fine assisting cross and drives a superb thumping header into the net. GOAL $attackingTeam!",
                    "Disaster for $defendingTeam defenses! They leave ${striker.name} unmarked, who thrashes a stunning half-volley into the top-corner! GOAL!"
                ).random()

                MatchCommentaryEvent(minute, "GOAL", "$minute' | ⚽ [GOAL] $desc$tacticsRemark (${homeTeam.name} $hScore-$aScore ${awayTeam.name})", hScore, aScore)
            }
            attRoll > defRoll -> {
                // GK SAVE!
                val desc = listOf(
                    "${striker.name} breaks past the defensive line and attempts a low shot... but ${gk.name} pulls off a spectacular fingertip save!",
                    "A booming shot from long range by ${striker.name}! It's heading into the corner... but ${gk.name} flies across goal to hold it!",
                    "${striker.name} with a snap shot! It flies through a crowd of players but the keeper is well-positioned to collect securely."
                ).random()
                MatchCommentaryEvent(minute, "SAVE", "$minute' | 🧤 [SAVE] $desc$tacticsRemark", currentHomeScore, currentAwayScore)
            }
            else -> {
                // DEFENDER INTERACTION OR MISS
                val cardRoll = Random.nextDouble()
                val cardThreshold = (if (pitchCondition == "MUDDY") 0.25 else 0.15) + (if (isRivalry) 0.15 else 0.0)
                if (cardRoll < cardThreshold) {
                    // FOUL & CARD!
                    val desc = listOf(
                        "Clumsy slide tackle! ${defender.name} wipes out ${striker.name} near the box. The referee stops play and brandishes a YELLOW CARD! 🟨",
                        "High pressing challenge! ${defender.name} pulls down ${striker.name} to stop a quick break. Yellow card issued."
                    ).random()
                    MatchCommentaryEvent(minute, "CARD", "$minute' | ⚠️ [WARNING] $desc", currentHomeScore, currentAwayScore)
                } else {
                    val desc = listOf(
                        "${striker.name} lines up a powerful shot from distance... but ${defender.name} puts in a heroic lunging block!",
                        "${striker.name} tries to weave a complex pass into the penalty box, but ${defender.name} cuts it out comfortably.",
                        "Direct free-kick attempt from ${striker.name}... Oh! It lacks curling accuracy and sails safely wide."
                    ).random()
                    MatchCommentaryEvent(minute, "MISS", "$minute' | ❌ [MISS] $desc$tacticsRemark", currentHomeScore, currentAwayScore)
                }
            }
        }
    }

    fun fillMatchStats(
        fixture: Fixture,
        home: Team,
        away: Team
    ): Fixture {
        val ratingDiff = home.teamRating - away.teamRating
        var hPoss = 50 + (ratingDiff * 0.4).toInt()
        
        val hMentality = home.playMentality
        val aMentality = away.playMentality
        if (hMentality == "ATTACKING" && aMentality == "DEFENSIVE") hPoss += 6
        if (hMentality == "DEFENSIVE" && aMentality == "ATTACKING") hPoss -= 6
        if (hMentality == "COUNTER" && aMentality == "ATTACKING") hPoss -= 4
        if (hMentality == "ATTACKING" && aMentality == "COUNTER") hPoss += 4
        
        hPoss += Random.nextInt(-6, 7)
        hPoss = hPoss.coerceIn(32, 68)
        val aPoss = 100 - hPoss

        val hAttacks = if (hMentality == "ATTACKING") 2 else if (hMentality == "DEFENSIVE") -1 else 0
        val aAttacks = if (aMentality == "ATTACKING") 2 else if (aMentality == "DEFENSIVE") -1 else 0
        
        val hShots = fixture.homeScore + Random.nextInt(1, 6) + hAttacks
        val aShots = fixture.awayScore + Random.nextInt(1, 6) + aAttacks

        val homeShotsOnTarget = hShots.coerceAtLeast(fixture.homeScore).coerceAtLeast(1)
        val awayShotsOnTarget = aShots.coerceAtLeast(fixture.awayScore).coerceAtLeast(1)

        val isMuddy = fixture.pitchCondition == "MUDDY"
        val isSoggy = fixture.pitchCondition == "SOGGY"
        val isFrozen = fixture.pitchCondition == "FROZEN"
        
        val weatherPenaltyH = if (isMuddy) -10 else if (isSoggy) -4 else if (isFrozen) -6 else 0
        val weatherPenaltyA = if (isMuddy) -10 else if (isSoggy) -4 else if (isFrozen) -6 else 0

        var hPass = 78 + ((home.teamRating - 70) / 3) + weatherPenaltyH
        var aPass = 78 + ((away.teamRating - 70) / 3) + weatherPenaltyA

        if (hMentality == "DEFENSIVE") hPass += 3
        if (hMentality == "ATTACKING") hPass -= 2
        if (aMentality == "DEFENSIVE") aPass += 3
        if (aMentality == "ATTACKING") aPass -= 2

        hPass += Random.nextInt(-4, 5)
        aPass += Random.nextInt(-4, 5)

        val homePassCompletion = hPass.coerceIn(52, 92)
        val awayPassCompletion = aPass.coerceIn(52, 92)

        return fixture.copy(
            homePossession = hPoss,
            awayPossession = aPoss,
            homeShotsOnTarget = homeShotsOnTarget,
            awayShotsOnTarget = awayShotsOnTarget,
            homePassCompletion = homePassCompletion,
            awayPassCompletion = awayPassCompletion
        )
    }

    fun generateSponsorshipOffers(): List<Sponsorship> {
        return listOf(
            Sponsorship(
                id = "sp_main_titan",
                brandName = "Titan Airways",
                category = "MAIN_SHIRT",
                durationSeasons = 2,
                seasonsRemaining = 2,
                baseWeeklyPayout = 450_000L,
                winBonus = 80_000L,
                standingGoalOrdinal = 4, // Top 4
                standingBonus = 1_200_000L,
                negotiationRisk = 15
            ),
            Sponsorship(
                id = "sp_main_vortex",
                brandName = "Vortex Energy",
                category = "MAIN_SHIRT",
                durationSeasons = 1,
                seasonsRemaining = 1,
                baseWeeklyPayout = 600_000L,
                winBonus = 30_000L,
                standingGoalOrdinal = 6, // Top 6
                standingBonus = 500_000L,
                negotiationRisk = 25
            ),
            Sponsorship(
                id = "sp_main_aura",
                brandName = "Aura Cosmetics",
                category = "MAIN_SHIRT",
                durationSeasons = 3,
                seasonsRemaining = 3,
                baseWeeklyPayout = 350_000L,
                winBonus = 120_000L,
                standingGoalOrdinal = 1, // Winner
                standingBonus = 2_500_000L,
                negotiationRisk = 20
            ),
            Sponsorship(
                id = "sp_stad_apex",
                brandName = "Apex Retail Dome",
                category = "STADIUM_NAMING",
                durationSeasons = 3,
                seasonsRemaining = 3,
                baseWeeklyPayout = 750_000L,
                winBonus = 0L,
                attendanceThreshold = 35_000,
                attendanceBonus = 240_000L,
                negotiationRisk = 15
            ),
            Sponsorship(
                id = "sp_stad_horizon",
                brandName = "Horizon Group Park",
                category = "STADIUM_NAMING",
                durationSeasons = 2,
                seasonsRemaining = 2,
                baseWeeklyPayout = 520_000L,
                winBonus = 20_000L,
                attendanceThreshold = 25_000,
                attendanceBonus = 120_000L,
                negotiationRisk = 20
            ),
            Sponsorship(
                id = "sp_sleeve_swift",
                brandName = "Swift Courier",
                category = "SLEEVE",
                durationSeasons = 1,
                seasonsRemaining = 1,
                baseWeeklyPayout = 150_000L,
                winBonus = 20_000L,
                negotiationRisk = 10
            ),
            Sponsorship(
                id = "sp_sleeve_fintech",
                brandName = "FinTech Pay",
                category = "SLEEVE",
                durationSeasons = 2,
                seasonsRemaining = 2,
                baseWeeklyPayout = 220_000L,
                winBonus = 10_000L,
                negotiationRisk = 15
            )
        )
    }

    fun generateBoardGoals(): List<BoardGoal> {
        return listOf(
            BoardGoal(
                id = "bg_wins",
                title = "Promised Victories",
                description = "Lead the team to achieve at least 5 wins across the 14 league matchdays.",
                targetType = "WINS",
                targetValue = 5,
                currentValue = 0,
                confidenceReward = 15,
                confidencePenalty = 20,
                deadlineMatchDay = 14
            ),
            BoardGoal(
                id = "bg_facilities",
                title = "Club Infrastructures",
                description = "Secure physical growth by conducting at least 1 facility upgrade.",
                targetType = "FACILITY_UPGRADE",
                targetValue = 1,
                currentValue = 0,
                confidenceReward = 10,
                confidencePenalty = 10,
                financialReward = 2_000_000L,
                deadlineMatchDay = 10
            ),
            BoardGoal(
                id = "bg_stadium",
                title = "Stadium Expansion",
                description = "Expand stadium capacity once before Match Day 8 to host growing supporter volumes.",
                targetType = "STADIUM_UPGRADE",
                targetValue = 1,
                currentValue = 0,
                confidenceReward = 10,
                confidencePenalty = 8,
                financialReward = 1_500_000L,
                deadlineMatchDay = 8
            ),
            BoardGoal(
                id = "bg_talent",
                title = "Scouting Recruitment",
                description = "Sign a top-tier athlete with an overall rating of 76+ from the scouting market.",
                targetType = "SIGN_PLAYER",
                targetValue = 76,
                currentValue = 0,
                confidenceReward = 12,
                confidencePenalty = 12,
                financialReward = 2_500_000L,
                deadlineMatchDay = 12
            )
        )
    }

    fun determineRivalry(home: Team, away: Team, standings: List<LeagueStanding>): Pair<Boolean, String?> {
        val homeName = home.name.lowercase()
        val awayName = away.name.lowercase()
        
        // Cities for Geographic Derby
        val cities = listOf("manchester", "london", "liverpool", "madrid", "munich", "milan", "paris", "barcelona")
        for (city in cities) {
            if (homeName.contains(city) && awayName.contains(city)) {
                return Pair(true, "${city.replaceFirstChar { it.uppercase() }} Derby")
            }
        }
        
        // Big historical rivalries
        if ((homeName.contains("manchester") || homeName.contains("london") || homeName.contains("liverpool")) &&
            (awayName.contains("manchester") || awayName.contains("london") || awayName.contains("liverpool"))) {
            return Pair(true, "English Super Clash")
        }
        if ((homeName.contains("madrid") || homeName.contains("barcelona")) &&
            (awayName.contains("madrid") || awayName.contains("barcelona"))) {
            return Pair(true, "El Clásico")
        }
        if ((homeName.contains("munich") || awayName.contains("munich")) && 
            (homeName.contains("madrid") || awayName.contains("madrid") || homeName.contains("liverpool") || awayName.contains("liverpool"))) {
            return Pair(true, "European Giant Clash")
        }
        
        // Dynamically compute Standings-based Rivalry
        if (standings.isNotEmpty()) {
            val homeStand = standings.firstOrNull { it.teamId == home.id }
            val awayStand = standings.firstOrNull { it.teamId == away.id }
            if (homeStand != null && awayStand != null) {
                val sorted = standings.sortedByDescending { it.points }
                val homePos = sorted.indexOfFirst { it.teamId == home.id } + 1
                val awayPos = sorted.indexOfFirst { it.teamId == away.id } + 1
                val pointsDiff = Math.abs(homeStand.points - awayStand.points)
                
                if (homePos <= 3 && awayPos <= 3 && pointsDiff <= 3) {
                    return Pair(true, "Title Battle Derby")
                }
                if (homePos >= sorted.size - 2 && awayPos >= sorted.size - 2 && pointsDiff <= 3) {
                    return Pair(true, "Relegation Fight")
                }
            }
        }
        return Pair(false, null)
    }

    fun generateMatchRecap(
        fixture: Fixture,
        homeTeam: Team,
        awayTeam: Team,
        injuredPlayersList: List<String> = emptyList()
    ): String {
        val hScore = fixture.homeScore
        val aScore = fixture.awayScore
        val isRivalry = fixture.isRivalryMatch
        val rivalryType = fixture.rivalryType

        val sb = java.lang.StringBuilder()

        // 1. Hook / Intro
        val intro = when {
            hScore > aScore -> {
                if (isRivalry) {
                    "A massive victory in the highly anticipated $rivalryType! ${fixture.homeTeamName} claimed absolute bragging rights over ${fixture.awayTeamName} with a sensational $hScore-$aScore win."
                } else {
                    "${fixture.homeTeamName} delivered an impressive display to overpower ${fixture.awayTeamName}, comfortably securing all three points in a $hScore-$aScore home triumph."
                }
            }
            aScore > hScore -> {
                if (isRivalry) {
                    "Silence fell upon the home camp as ${fixture.awayTeamName} conquered ${fixture.homeTeamName} in a historic $rivalryType away performance, sealing a $hScore-$aScore win."
                } else {
                    "A flawless away performance from ${fixture.awayTeamName} saw them dictate the tempo and secure a well-earned $hScore-$aScore victory against ${fixture.homeTeamName}."
                }
            }
            else -> { // Draw
                if (hScore == 0) {
                    if (isRivalry) {
                        "A grueling, high-pressure $rivalryType ended in a scoreless stalemate. Both ${fixture.homeTeamName} and ${fixture.awayTeamName} defended with exceptional intensity, leaving honors even."
                    } else {
                        "Defense reigned supreme as both teams neutralized each other's attacking threads. A highly tactical encounter finished in a 0-0 draw."
                    }
                } else {
                    if (isRivalry) {
                        "A spectacular, pulsating $rivalryType ended in a dramatic $hScore-$hScore draw. Neither ${fixture.homeTeamName} nor ${fixture.awayTeamName} yielded an inch in a match full of pure passion."
                    } else {
                        "A balanced match ended with honors shared in an entertaining $hScore-$aScore draw, both teams walking away with a single credit point."
                    }
                }
            }
        }
        sb.append(intro).append("\n\n")

        // 2. Goal Analysis
        val goalEvents = fixture.eventsLog.filter { it.type == "GOAL" }
        if (goalEvents.isNotEmpty()) {
            sb.append("🎯 KEY GOAL INVOLVEMENTS:\n")
            goalEvents.forEach { event ->
                val descClean = event.description.substringAfter("] ").substringAfter(" | ").trim()
                sb.append("• **Min ${event.minute}'**: $descClean\n")
            }
            sb.append("\n")
        }

        // 3. Disciplinary highlights
        val cardEvents = fixture.eventsLog.filter { it.type == "CARD" }
        if (cardEvents.isNotEmpty()) {
            sb.append("🟨 DISCIPLINARY LOGS:\n")
            cardEvents.forEach { event ->
                val descClean = event.description.substringAfter("] ").substringAfter(" | ").trim()
                sb.append("• **Min ${event.minute}'**: $descClean\n")
            }
            sb.append("\n")
        }

        // 4. Matchday health & injuries
        if (injuredPlayersList.isNotEmpty()) {
            sb.append("🏥 MEDICAL UPDATES:\n")
            injuredPlayersList.forEach { pReport ->
                sb.append("$pReport\n")
            }
            sb.append("\n")
        } else {
            sb.append("🏥 HEALTH STATUS:\n• Fortunate week! No physical matchday injury concerns logged for either squad.\n\n")
        }

        // 5. Supporter Atmospherics
        val fanSatisfactionResult = when {
            hScore > aScore && homeTeam.isUserControlled -> "Buzzing with ecstatic celebrations! Supporter ticket demands are projected to spike for the next home match."
            aScore > hScore && awayTeam.isUserControlled -> "Absolutely thrilled with our grit! Travelling supporters sang loudly under the floodlights."
            hScore < aScore && homeTeam.isUserControlled -> "Left the venue in frustration. The manager is under pressure to tweak tactics for the upcoming fixture."
            aScore < hScore && awayTeam.isUserControlled -> "Quiet. We need to bounce back quickly to restore confidence before fans grow restive."
            else -> "Appreciated the grittiness. They remain hopeful that tactical tweaks will pay off in the weeks ahead."
        }
        sb.append("👥 SUPPORTER ATMOSPHERICS:\n")
        if (isRivalry) {
            sb.append("• Fueled by the immense $rivalryType passion, ")
        } else {
            sb.append("• ")
        }
        sb.append("The club support is $fanSatisfactionResult")

        return sb.toString()
    }
}
