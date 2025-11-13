package com.example.geometric_neon_runner.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.geometric_neon_runner.ui.components.NeonButton
import com.example.geometric_neon_runner.ui.navigation.Screen
import com.example.geometric_neon_runner.ui.theme.DarkBackground

@Composable
fun GameOverScreen(
    navController: NavController,
    score: Int,
    timeSeconds: Int,
    mode: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            NeonGameOverText()

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Score: $score",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Time: ${formatTime(timeSeconds)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Mode: $mode",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            NeonButton(
                text = "PLAY AGAIN",
                onClick = {
                    navController.navigate(Screen.Game.createRoute(mode)) {
                        popUpTo(Screen.GameOver.createRoute(0, 0, mode)) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            NeonButton(
                text = "MENU",
                onClick = {
                    navController.navigate(Screen.Menu.route) {
                        popUpTo(Screen.Menu.route) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            NeonButton(
                text = "RANKING",
                onClick = {
                    navController.navigate(Screen.Ranking.createRoute(mode))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )
        }
    }
}

@Composable
private fun NeonGameOverText() {
    val infiniteTransition = rememberInfiniteTransition(label = "game_over_glow")
    val glow by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_animation"
    )

    Text(
        text = "GAME OVER",
        color = Color.White,
        fontWeight = FontWeight.ExtraBold,
        style = MaterialTheme.typography.displaySmall,
        modifier = Modifier
            .padding(8.dp)
            .drawBehind {
                drawRect(
                    color = Color(0xFFFF00FF).copy(alpha = 0.06f * glow),
                    size = this.size
                )
            }
    )
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}