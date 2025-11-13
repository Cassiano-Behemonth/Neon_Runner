package com.example.geometric_neon_runner.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geometric_neon_runner.data.repository.ScoreRepository
import com.example.geometric_neon_runner.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class GameOverViewModel(
    private val scoreRepository: ScoreRepository
) : ViewModel() {


    private val _userRank = MutableStateFlow<Int?>(null)
    val userRank: StateFlow<Int?> = _userRank.asStateFlow()


    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()


    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()


    var score: Int = 0
        private set
    var time: Int = 0
        private set
    var mode: String = "NORMAL"
        private set


    fun initialize(score: Int, time: Int, mode: String) {
        this.score = score
        this.time = time
        this.mode = mode

        loadUserRank()
    }


    fun loadUserRank() {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {

                val result = scoreRepository.getGlobalRanking(mode)

                when (result) {
                    is Result.Success -> {

                        val ranking = result.data
                        val position = ranking.indexOfFirst { it.score < score }

                        _userRank.value = if (position == -1) {
                            ranking.size + 1
                        } else {
                            position + 1
                        }
                    }
                    is Result.Error -> {
                        _errorMessage.value = result.message
                        _userRank.value = null
                    }
                    Result.Loading -> {

                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao carregar ranking"
                _userRank.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }


    private val _shouldRetryGame = MutableStateFlow(false)
    val shouldRetryGame: StateFlow<Boolean> = _shouldRetryGame.asStateFlow()

    private val _shouldGoToMenu = MutableStateFlow(false)
    val shouldGoToMenu: StateFlow<Boolean> = _shouldGoToMenu.asStateFlow()

    private val _shouldGoToRanking = MutableStateFlow(false)
    val shouldGoToRanking: StateFlow<Boolean> = _shouldGoToRanking.asStateFlow()


    fun retryGame() {
        _shouldRetryGame.value = true
    }

    fun goToMenu() {
        _shouldGoToMenu.value = true
    }

    fun goToRanking() {
        _shouldGoToRanking.value = true
    }


    fun onNavigated() {
        _shouldRetryGame.value = false
        _shouldGoToMenu.value = false
        _shouldGoToRanking.value = false
    }
}