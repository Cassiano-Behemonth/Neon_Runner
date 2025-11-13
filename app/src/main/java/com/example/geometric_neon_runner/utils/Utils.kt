package com.example.geometric_neon_runner.utils

import android.util.Patterns

object Constants {
    // Game constants
    const val LANE_COUNT = 3
    const val BASE_SPEED = 600f
    const val POINTS_PER_SECOND = 15

    // Spawn intervals
    const val SPAWN_INTERVAL_NORMAL = 1100L
    const val SPAWN_INTERVAL_HARD = 900L
    const val SPAWN_INTERVAL_EXTREME = 700L
    const val MIN_SPAWN_INTERVAL = 500L

    // Sizes
    const val PLAYER_SIZE = 40f
    const val ENEMY_SIZE = 35f
    const val COLLISION_THRESHOLD = 45f

    // Firebase collections
    const val COLLECTION_USERS = "users"
    const val COLLECTION_SCORES = "scores"

    // SharedPreferences
    const val PREFS_NAME = "NeonTunnelPrefs"
    const val KEY_USER_ID = "user_id"
    const val KEY_USERNAME = "username"
    const val KEY_EMAIL = "email"
    const val KEY_IS_LOGGED_IN = "is_logged_in"

    // Game positioning
    const val PLAYER_Y_POSITION = 0.85f
    const val TUNNEL_CENTER_Y = 0.35f

    // Visual
    const val PLAYER_LERP_SPEED = 0.35f
    const val TUNNEL_RING_COUNT = 25
    const val GRID_SPACING = 80f
}

// Extension functions for Int
fun Int.toTimeFormat(): String {
    val minutes = this / 60
    val seconds = this % 60
    return String.format("%02d:%02d", minutes, seconds)
}

fun Int.formatScore(): String {
    return "%,d".format(this).replace(',', '.')
}

// Extension functions for Float
fun Float.lerp(target: Float, speed: Float): Float {
    return this + (target - this) * speed
}

fun Float.clamp(min: Float, max: Float): Float {
    return when {
        this < min -> min
        this > max -> max
        else -> this
    }
}

// Extension functions for String
fun String.isValidEmail(): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.isValidPassword(): Boolean {
    return this.length >= 6
}

fun String.isValidUsername(): Boolean {
    return this.length >= 3
}

// Extension functions for Long
fun Long.toFormattedDate(): String {
    val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
    return dateFormat.format(java.util.Date(this))
}

// Result sealed class
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val exception: Exception? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()

    // Helper methods
    fun isLoading() = this is Loading
    fun isSuccess() = this is Success
    fun isError() = this is Error

    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    fun getErrorMessage(): String? = when (this) {
        is Error -> message
        else -> null
    }
}