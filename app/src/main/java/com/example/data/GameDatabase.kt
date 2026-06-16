package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "careers")
data class CareerSave(
    @PrimaryKey val id: Int, // Slot 1, 2, or 3
    val managerName: String,
    val clubName: String,
    val lastPlayed: Long,
    val jsonState: String // Serialized SaveState
)

@Dao
interface CareerDao {
    @Query("SELECT * FROM careers ORDER BY lastPlayed DESC")
    fun getAllCareers(): Flow<List<CareerSave>>

    @Query("SELECT * FROM careers WHERE id = :id LIMIT 1")
    suspend fun getCareerById(id: Int): CareerSave?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCareer(career: CareerSave)

    @Query("DELETE FROM careers WHERE id = :id")
    suspend fun deleteCareerById(id: Int)
}

@Database(entities = [CareerSave::class], version = 1, exportSchema = false)
abstract class GameDatabase : RoomDatabase() {
    abstract val careerDao: CareerDao

    companion object {
        @Volatile
        private var INSTANCE: GameDatabase? = null

        fun getDatabase(context: Context): GameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameDatabase::class.java,
                    "football_manager_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
