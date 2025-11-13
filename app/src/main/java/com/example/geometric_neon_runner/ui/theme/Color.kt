package com.example.geometric_neon_runner.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

val NeonCyan = Color(0xFF00FFFF)
val NeonMagenta = Color(0xFFFF00FF)
val NeonPink = Color(0xFFFF0066)

val DarkBackground = Color(0xFF0A0A1A)
val DarkSurface = Color(0xFF1B1B2B)

val LightText = Color.White
val DarkText = Color.Black

val NeonError = Color(0xFFFF4D94)

val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = DarkBackground,
    secondary = NeonMagenta,
    tertiary = NeonPink,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = LightText,
    onSurface = LightText,
    error = NeonError,
    onError = DarkBackground
)