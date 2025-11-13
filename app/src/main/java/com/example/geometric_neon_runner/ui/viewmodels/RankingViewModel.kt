package com.example.geometric_neon_runner.ui.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geometric_neon_runner.data.model.GameMode
import com.example.geometric_neon_runner.data.model.Score
import com.example.geometric_neon_runner.data.repository.AuthRepository
import com.example.geometric_neon_runner.data.repository.ScoreRepository
import com.example.geometric_neon_runner.utils.Result.Error
import com.example.geometric_neon_runner.utils.Result.Loading
import com.example.geometric_neon_runner.utils.Result.Success
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class RankingViewModel(
    private val scoreRepository: ScoreRepository,
    private val authRepository: AuthRepository
) : ViewModel() {


    private val _rankingList = MutableStateFlow<List<Score>>(emptyList())
    val rankingList: StateFlow<List<Score>> = _rankingList.asStateFlow()


    private val _selectedMode = MutableStateFlow(GameMode.NORMAL)
    val selectedMode: StateFlow<GameMode> = _selectedMode.asStateFlow()


    private val _currentUserId = MutableStateFlow("")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()


    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()


    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        _currentUserId.value = authRepository.getCurrentUserId() ?: ""
        loadRanking(GameMode.NORMAL.name)
    }


    fun loadRanking(mode: String) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val result = scoreRepository.getGlobalRanking(mode)

                when (result) {
                    is Success -> {
                        _rankingList.value = result.data
                        _errorMessage.value = null
                    }
                    is Error -> {
                        _errorMessage.value = result.message
                        _rankingList.value = emptyList()
                    }
                    Loading -> {
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao carregar ranking: ${e.message}"
                _rankingList.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun changeMode(mode: GameMode) {
        if (_selectedMode.value != mode) {
            _selectedMode.value = mode
            loadRanking(mode.name)
        }
    }


    fun refresh() {
        loadRanking(_selectedMode.value.name)
    }


    fun isCurrentUser(score: Score): Boolean {
        return score.userId == _currentUserId.value
    }


    fun getPosition(score: Score): Int {
        return _rankingList.value.indexOf(score) + 1
    }
}