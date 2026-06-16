package com.example.data

import com.example.model.SaveState
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class GameRepository(private val careerDao: CareerDao) {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val saveStateAdapter = moshi.adapter(SaveState::class.java)

    val allCareers: Flow<List<CareerSave>> = careerDao.getAllCareers()

    suspend fun loadSaveState(slotId: Int): SaveState? = withContext(Dispatchers.IO) {
        val career = careerDao.getCareerById(slotId) ?: return@withContext null
        try {
            saveStateAdapter.fromJson(career.jsonState)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun saveCareer(slotId: Int, managerName: String, clubName: String, state: SaveState) = withContext(Dispatchers.IO) {
        val jsonString = saveStateAdapter.toJson(state)
        val career = CareerSave(
            id = slotId,
            managerName = managerName,
            clubName = clubName,
            lastPlayed = System.currentTimeMillis(),
            jsonState = jsonString
        )
        careerDao.insertCareer(career)
    }

    suspend fun deleteSlot(slotId: Int) = withContext(Dispatchers.IO) {
        careerDao.deleteCareerById(slotId)
    }
}
