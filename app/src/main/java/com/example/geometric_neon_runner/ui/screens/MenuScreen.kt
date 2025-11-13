package com.example.geometric_neon_runner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.geometric_neon_runner.ui.theme.DarkBackground
import com.example.geometric_neon_runner.ui.components.NeonButton
import com.example.geometric_neon_runner.ui.theme.NeonTunnelTheme
import com.example.geometric_neon_runner.ui.viewmodels.MenuViewModel
import com.example.geometric_neon_runner.ui.navigation.Screen

@Composable
fun MenuScreen(
    viewModel: MenuViewModel,
    navController: NavController
) {
    val username by viewModel.username.collectAsState()
    val bestScores by viewModel.bestScores.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val bestScore = bestScores.values.maxOrNull() ?: 0
    val scrollState = rememberScrollState()

    NeonTunnelTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "NEON RUNNER",
                style = MaterialTheme.typography.displayLarge.copy(color = MaterialTheme.colorScheme.primary),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "MAIN MENU",
                style = MaterialTheme.typography.headlineLarge.copy(color = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            PlayerStatus(username = username, bestScore = bestScore)

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                MenuButton(
                    text = "P L A Y",
                    onClick = {
                        navController.navigate(Screen.ModeSelection.route)
                    },
                    icon = Icons.Default.Star,
                    neonColor = MaterialTheme.colorScheme.primary
                )

                MenuButton(
                    text = "RANKING",
                    onClick = { navController.navigate(Screen.Ranking.createRoute("NORMAL")) },
                    icon = Icons.Default.Person,
                    neonColor = MaterialTheme.colorScheme.secondary
                )

                MenuButton(
                    text = "PROFILE",
                    onClick = { navController.navigate(Screen.Profile.route) },
                    icon = Icons.Default.Person,
                    neonColor = MaterialTheme.colorScheme.tertiary
                )

                Spacer(modifier = Modifier.height(32.dp))

                MenuButton(
                    text = "LOGOUT",
                    onClick = {
                        viewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Menu.route) { inclusive = true }
                        }
                    },
                    icon = Icons.Default.ExitToApp,
                    neonColor = Color.Gray.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PlayerStatus(username: String, bestScore: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "RUNNER: $username",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "BEST SCORE: $bestScore",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun MenuButton(
    text: String,
    onClick: () -> Unit,
    icon: ImageVector,
    neonColor: Color
) {
    NeonButton(
        onClick = onClick,
        text = text,
        neonColor = neonColor,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    )
}