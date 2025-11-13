package com.example.geometric_neon_runner.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geometric_neon_runner.data.model.GameMode
import com.example.geometric_neon_runner.data.model.Score
import com.example.geometric_neon_runner.data.repository.AuthRepository
import com.example.geometric_neon_runner.data.repository.ScoreRepository
import com.example.geometric_neon_runner.game.GameState
import com.example.geometric_neon_runner.game.systems.SpawnMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel(
    private val scoreRepository: ScoreRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _gameState = MutableStateFlow<GameState>(GameState.Playing)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _currentScore = MutableStateFlow(0)
    val currentScore: StateFlow<Int> = _currentScore.asStateFlow()

    private val _elapsedTime = MutableStateFlow(0)
    val elapsedTime: StateFlow<Int> = _elapsedTime.asStateFlow()

    private val _gameMode = MutableStateFlow(GameMode.NORMAL)
    val gameMode: StateFlow<GameMode> = _gameMode.asStateFlow()

    private val _shouldNavigateToGameOver = MutableStateFlow(false)
    val shouldNavigateToGameOver: StateFlow<Boolean> = _shouldNavigateToGameOver.asStateFlow()

    var finalScore: Int = 0
        private set
    var finalTime: Int = 0
        private set

    private var timerJob: Job? = null


    fun initializeGame(modeString: String) {
        val mode = GameMode.fromName(modeString)
        _gameMode.value = mode
        _currentScore.value = 0
        _elapsedTime.value = 0
        _gameState.value = GameState.Playing
        startTimer()
    }


    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_gameState.value == GameState.Playing) {
                delay(1000)
                _elapsedTime.value += 1
            }
        }
    }


    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }


    fun getSpawnMode(): SpawnMode {
        return when (_gameMode.value) {
            GameMode.NORMAL -> SpawnMode.NORMAL
            GameMode.HARD -> SpawnMode.HARD
            GameMode.EXTREME -> SpawnMode.EXTREME
        }
    }


    fun updateScore(score: Int) {
        _currentScore.value = score
    }


    fun updateTime(time: Int) {
        _elapsedTime.value = time
    }

    fun setGameMode(gameMode: GameMode) {
        _gameMode.value = gameMode
    }


    fun onGameOver(score: Int, time: Int) {
        stopTimer()

        finalScore = score
        finalTime = time
        _gameState.value = GameState.GameOver

        saveScore()

        _shouldNavigateToGameOver.value = true
    }


    private fun saveScore() {
        viewModelScope.launch {
            try {
                val userId = authRepository.getCurrentUserId() ?: return@launch
                val username = authRepository.getCurrentUsername()

                val scoreData = Score(
                    userId = userId,
                    username = username,
                    score = finalScore,
                    timeSeconds = finalTime,
                    mode = _gameMode.value.name,
                    timestamp = System.currentTimeMillis(),
                    synced = false
                )

                scoreRepository.saveScore(scoreData)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun pauseGame() {
        if (_gameState.value == GameState.Playing) {
            _gameState.value = GameState.Paused
            stopTimer()
        }
    }


    fun resumeGame() {
        if (_gameState.value == GameState.Paused) {
            _gameState.value = GameState.Playing
            startTimer()
        }
    }


    fun onNavigatedToGameOver() {
        _shouldNavigateToGameOver.value = false
    }


    fun onGameStopped() {
        stopTimer()
    }


    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}