package com.example.geometric_neon_runner.data.repository

import android.content.Context
import com.example.geometric_neon_runner.data.local.AppDatabase
import com.example.geometric_neon_runner.data.model.Score
import com.example.geometric_neon_runner.data.remote.FirestoreSource
import com.example.geometric_neon_runner.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScoreRepository(
    private val context: Context,
    private val firestoreSource: FirestoreSource = FirestoreSource()
) {
    private val db by lazy { AppDatabase.getInstance(context) }
    private val dao by lazy { db.scoreDao() }

    suspend fun saveScore(score: Score): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val localId = dao.insertScore(score.copy(synced = false))

                val remoteResult = firestoreSource.saveScore(score)
                if (remoteResult is Result.Success) {
                    val updatedLocal = score.copy(id = localId.toInt(), synced = true)
                    dao.markAsSynced(updatedLocal)
                    firestoreSource.updateBestScore(score.userId, score.mode, score.score)
                    Result.Success(Unit)
                } else if (remoteResult is Result.Error) {
                    Result.Error(remoteResult.message, remoteResult.exception)
                } else {
                    Result.Error("Unknown error saving score remotely")
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Unknown error", e)
            }
        }
    }

    suspend fun getGlobalRanking(mode: String, limit: Int = 50): Result<List<Score>> {
        return firestoreSource.getGlobalRanking(mode, limit)
    }

    suspend fun getLocalScores(userId: String): List<Score> {
        return withContext(Dispatchers.IO) {
            dao.getUserScores(userId)
        }
    }

    suspend fun getBestLocalScore(userId: String, mode: String): Int {
        return withContext(Dispatchers.IO) {
            dao.getBestScore(userId, mode) ?: 0
        }
    }

    suspend fun syncPendingScores(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val pending = dao.getUnsyncedScores()
                pending.forEach { s ->
                    val res = firestoreSource.saveScore(s)
                    if (res is Result.Success) {
                        dao.markAsSynced(s.copy(synced = true))
                        firestoreSource.updateBestScore(s.userId, s.mode, s.score)
                    }
                }
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Unknown error", e)
            }
        }
    }
}