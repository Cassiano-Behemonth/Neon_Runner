package com.example.geometric_neon_runner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.geometric_neon_runner.game.GameView
import com.example.geometric_neon_runner.ui.theme.DarkBackground
import com.example.geometric_neon_runner.ui.navigation.Screen
import com.example.geometric_neon_runner.ui.viewmodels.GameViewModel

@Composable
fun GameScreen(
    navController: NavController,
    viewModel: GameViewModel,
    mode: String
) {
    val context = LocalContext.current

    val currentScore by viewModel.currentScore.collectAsState()
    val elapsedTime by viewModel.elapsedTime.collectAsState()
    val gameMode by viewModel.gameMode.collectAsState()
    val shouldNavigateToGameOver by viewModel.shouldNavigateToGameOver.collectAsState()

    LaunchedEffect(mode) {
        viewModel.initializeGame(mode)
    }

    LaunchedEffect(shouldNavigateToGameOver) {
        if (shouldNavigateToGameOver) {
            val finalScore = viewModel.finalScore
            val finalTime = viewModel.finalTime
            val modeName = viewModel.gameMode.value.name

            navController.navigate(
                Screen.GameOver.createRoute(finalScore, finalTime, modeName)
            ) {
                popUpTo(Screen.Game.createRoute(mode)) { inclusive = true }
            }
            viewModel.onNavigatedToGameOver()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        AndroidView(
            factory = { ctx ->
                GameView(ctx, viewModel.getSpawnMode()).apply {
                    onGameOver = { score, time ->
                        viewModel.onGameOver(score, time)
                    }

                    onScoreChanged = { score ->
                        viewModel.updateScore(score)
                    }

                    startGame()
                }
            },
            modifier = Modifier.fillMaxSize(),
            onRelease = { view ->
                view.stopGame()
                viewModel.onGameStopped()
            }
        )

        GameHUD(
            gameMode = gameMode,
            currentScore = currentScore,
            elapsedTime = elapsedTime,
            modifier = Modifier.align(Alignment.TopStart)
        )
    }
}

@Composable
private fun GameHUD(
    gameMode: com.example.geometric_neon_runner.data.model.GameMode,
    currentScore: Int,
    elapsedTime: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = gameMode.displayName.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = Color(android.graphics.Color.parseColor(gameMode.color)),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "SCORE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
                Text(
                    text = "$currentScore",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "TIME",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
                Text(
                    text = formatTime(elapsedTime),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}