package com.example.geometric_neon_runner.data.local

import android.content.Context
import androidx.room.*
import com.example.geometric_neon_runner.data.model.GameMode
import com.example.geometric_neon_runner.data.model.Score
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

@Dao
interface ScoreDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: Score): Long

    @Query("SELECT * FROM scores WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getUserScores(userId: String): List<Score>

    @Query("SELECT MAX(score) FROM scores WHERE userId = :userId AND mode = :mode")
    suspend fun getBestScore(userId: String, mode: String): Int?

    @Query("SELECT * FROM scores WHERE synced = 0 ORDER BY timestamp ASC")
    suspend fun getUnsyncedScores(): List<Score>

    @Update
    suspend fun markAsSynced(score: Score)
}

class Converters {
    @TypeConverter
    fun fromGameMode(mode: GameMode?): String? = mode?.name

    @TypeConverter
    fun toGameMode(name: String?): GameMode? = name?.let { GameMode.fromName(it) }

    @TypeConverter
    fun fromDate(date: Date?): Long? = date?.time

    @TypeConverter
    fun toDate(time: Long?): Date? = time?.let { Date(it) }
}

@Database(entities = [Score::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scoreDao(): ScoreDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        private const val DB_NAME = "neon_tunnel_runner.db"

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }

        private fun buildDatabase(appContext: Context): AppDatabase {
            return Room.databaseBuilder(appContext, AppDatabase::class.java, DB_NAME)
                .fallbackToDestructiveMigration()
                .build()
        }

        suspend fun insertScore(context: Context, score: Score): Long {
            val db = getInstance(context)
            return withContext(Dispatchers.IO) {
                db.scoreDao().insertScore(score)
            }
        }
    }
}