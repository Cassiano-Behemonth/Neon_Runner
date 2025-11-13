package com.example.geometric_neon_runner.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geometric_neon_runner.data.repository.AuthRepository
import com.example.geometric_neon_runner.data.repository.ScoreRepository
import com.example.geometric_neon_runner.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileStats(
    val normalBestScore: Int = 0,
    val normalBestTime: Int = 0,
    val normalRanking: Int? = null,

    val hardBestScore: Int = 0,
    val hardBestTime: Int = 0,
    val hardRanking: Int? = null,

    val extremeBestScore: Int = 0,
    val extremeBestTime: Int = 0,
    val extremeRanking: Int? = null,

    val totalGames: Int = 0,
    val totalPlayTime: Int = 0
)

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val scoreRepository: ScoreRepository
) : ViewModel() {

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _profileStats = MutableStateFlow(ProfileStats())
    val profileStats: StateFlow<ProfileStats> = _profileStats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadProfileData() {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                _username.value = authRepository.getCurrentUsername()
                val userId = authRepository.getCurrentUserId() ?: ""

                val normalScore = scoreRepository.getBestLocalScore(userId, "NORMAL")
                val hardScore = scoreRepository.getBestLocalScore(userId, "HARD")
                val extremeScore = scoreRepository.getBestLocalScore(userId, "EXTREME")

                val normalScores = scoreRepository.getLocalScores(userId)
                val normalBestTime = normalScores
                    .filter { it.mode == "NORMAL" }
                    .maxByOrNull { it.timeSeconds }?.timeSeconds ?: 0

                val hardBestTime = normalScores
                    .filter { it.mode == "HARD" }
                    .maxByOrNull { it.timeSeconds }?.timeSeconds ?: 0

                val extremeBestTime = normalScores
                    .filter { it.mode == "EXTREME" }
                    .maxByOrNull { it.timeSeconds }?.timeSeconds ?: 0

                val normalRanking = getUserRankingPosition(userId, "NORMAL", normalScore)
                val hardRanking = getUserRankingPosition(userId, "HARD", hardScore)
                val extremeRanking = getUserRankingPosition(userId, "EXTREME", extremeScore)

                val totalGames = normalScores.size
                val totalPlayTime = normalScores.sumOf { it.timeSeconds }

                _profileStats.value = ProfileStats(
                    normalBestScore = normalScore,
                    normalBestTime = normalBestTime,
                    normalRanking = normalRanking,

                    hardBestScore = hardScore,
                    hardBestTime = hardBestTime,
                    hardRanking = hardRanking,

                    extremeBestScore = extremeScore,
                    extremeBestTime = extremeBestTime,
                    extremeRanking = extremeRanking,

                    totalGames = totalGames,
                    totalPlayTime = totalPlayTime
                )
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun getUserRankingPosition(userId: String, mode: String, userScore: Int): Int? {
        if (userScore == 0) return null

        return try {
            val result = scoreRepository.getGlobalRanking(mode, 100)
            when (result) {
                is Result.Success -> {
                    val ranking = result.data
                    val position = ranking.indexOfFirst { it.userId == userId && it.score == userScore }
                    if (position >= 0) position + 1 else null
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun refresh() {
        loadProfileData()
    }
}