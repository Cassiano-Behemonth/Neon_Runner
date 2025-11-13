package com.example.geometric_neon_runner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.geometric_neon_runner.ui.theme.DarkBackground
import com.example.geometric_neon_runner.ui.theme.DarkSurface
import com.example.geometric_neon_runner.ui.theme.NeonCyan
import com.example.geometric_neon_runner.ui.theme.NeonMagenta
import com.example.geometric_neon_runner.ui.theme.NeonPink
import com.example.geometric_neon_runner.ui.theme.NeonTunnelTheme
import com.example.geometric_neon_runner.ui.viewmodels.MenuViewModel

data class GameModeInfo(
    val name: String,
    val description: String,
    val color: Color,
    val bestScore: Int
)

@Composable
fun ModeSelectionScreen(
    onModeSelected: (mode: String) -> Unit,
    onBackPressed: () -> Unit,
    viewModel: MenuViewModel
) {
    val bestScores by viewModel.bestScores.collectAsState()

    val modes = listOf(
        GameModeInfo(
            "NORMAL",
            "Experiência padrão. Velocidade constante, obstáculos moderados.",
            NeonCyan,
            bestScores["NORMAL"] ?: 0
        ),
        GameModeInfo(
            "HARD",
            "Mais rápido, mais obstáculos e túnel mais estreito. Desafio intermediário.",
            NeonMagenta,
            bestScores["HARD"] ?: 0
        ),
        GameModeInfo(
            "EXTREME",
            "Velocidade máxima, padrões de obstáculos complexos. Apenas para profissionais.",
            NeonPink,
            bestScores["EXTREME"] ?: 0
        )
    )

    NeonTunnelTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "SELECT MODE",
                    style = MaterialTheme.typography.displayMedium.copy(
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(modes) { mode ->
                        ModeCard(
                            mode = mode,
                            onClick = { onModeSelected(mode.name) }
                        )
                    }
                }
            }

            IconButton(
                onClick = onBackPressed,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .shadow(elevation = 8.dp, spotColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Voltar",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun ModeCard(mode: GameModeInfo, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    val shadowColor = mode.color.copy(alpha = 0.8f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = shape,
                spotColor = shadowColor
            )
            .clip(shape)
            .border(
                width = 2.dp,
                color = mode.color,
                shape = shape
            )
            .background(DarkSurface.copy(alpha = 0.8f))
            .clickable(onClick = onClick)
            .padding(20.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = mode.name,
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = mode.color
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = mode.description,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "BEST: ${mode.bestScore}",
            style = MaterialTheme.typography.titleLarge,
            color = mode.color
        )
    }
}