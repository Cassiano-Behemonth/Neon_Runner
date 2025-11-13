package com.example.geometric_neon_runner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.geometric_neon_runner.data.model.GameMode
import com.example.geometric_neon_runner.ui.theme.DarkBackground
import com.example.geometric_neon_runner.ui.components.LoadingIndicator
import com.example.geometric_neon_runner.ui.viewmodels.RankingViewModel

@Composable
fun RankingScreen(
    navController: NavController,
    vm: RankingViewModel,
    initialMode: String = "NORMAL"
) {
    val rankingList by vm.rankingList.collectAsState()
    val selectedMode by vm.selectedMode.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val errorMessage by vm.errorMessage.collectAsState()
    val currentUserId by vm.currentUserId.collectAsState()

    LaunchedEffect(initialMode) {
        val mode = GameMode.fromName(initialMode)
        vm.changeMode(mode)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "RANKING",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        TabRow(
            selectedTabIndex = when (selectedMode) {
                GameMode.NORMAL -> 0
                GameMode.HARD -> 1
                GameMode.EXTREME -> 2
            }
        ) {
            Tab(
                selected = selectedMode == GameMode.NORMAL,
                onClick = { vm.changeMode(GameMode.NORMAL) },
                text = { Text("Normal") }
            )
            Tab(
                selected = selectedMode == GameMode.HARD,
                onClick = { vm.changeMode(GameMode.HARD) },
                text = { Text("Hard") }
            )
            Tab(
                selected = selectedMode == GameMode.EXTREME,
                onClick = { vm.changeMode(GameMode.EXTREME) },
                text = { Text("Extreme") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = errorMessage ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { vm.refresh() }) {
                            Text("Retry")
                        }
                    }
                }
            }
            rankingList.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No scores yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(rankingList) { index, score ->
                        RankingItem(
                            position = index + 1,
                            username = score.username,
                            scoreValue = score.score,
                            timeSeconds = score.timeSeconds,
                            isCurrentUser = score.userId == currentUserId,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { /* TODO: Show user profile */ }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RankingItem(
    position: Int,
    username: String,
    scoreValue: Int,
    timeSeconds: Int,
    isCurrentUser: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(vertical = 6.dp, horizontal = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#$position",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.width(64.dp),
                color = if (isCurrentUser)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = username,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isCurrentUser)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Time: ${formatTime(timeSeconds)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Text(
                text = "$scoreValue",
                style = MaterialTheme.typography.titleMedium,
                color = if (isCurrentUser)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}