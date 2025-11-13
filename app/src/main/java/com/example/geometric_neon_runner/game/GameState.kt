package com.example.geometric_neon_runner.game

sealed class GameState {
    object Playing : GameState()
    object Paused : GameState()
    object GameOver : GameState()
}

enum class GameMode {
    NORMAL, HARD, EXTREME
}