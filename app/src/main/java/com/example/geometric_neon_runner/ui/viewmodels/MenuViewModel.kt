package com.example.geometric_neon_runner.ui.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geometric_neon_runner.data.repository.AuthRepository
import com.example.geometric_neon_runner.data.repository.ScoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class MenuViewModel(
    private val authRepository: AuthRepository,
    private val scoreRepository: ScoreRepository
) : ViewModel() {


    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()


    private val _bestScores = MutableStateFlow<Map<String, Int>>(emptyMap())
    val bestScores: StateFlow<Map<String, Int>> = _bestScores.asStateFlow()


    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadUserData()
    }


    fun loadUserData() {
        _isLoading.value = true

        viewModelScope.launch {
            try {

                _username.value = authRepository.getCurrentUsername()


                val userId = authRepository.getCurrentUserId() ?: ""
                val scores = mutableMapOf<String, Int>()

                scores["NORMAL"] = scoreRepository.getBestLocalScore(userId, "NORMAL")
                scores["HARD"] = scoreRepository.getBestLocalScore(userId, "HARD")
                scores["EXTREME"] = scoreRepository.getBestLocalScore(userId, "EXTREME")

                _bestScores.value = scores
            } catch (e: Exception) {

                _username.value = "Jogador"
                _bestScores.value = mapOf(
                    "NORMAL" to 0,
                    "HARD" to 0,
                    "EXTREME" to 0
                )
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }


    fun getBestScore(mode: String): Int {
        return _bestScores.value[mode] ?: 0
    }
}