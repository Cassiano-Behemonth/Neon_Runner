package com.example.geometric_neon_runner.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.geometric_neon_runner.ui.theme.DarkBackground
import com.example.geometric_neon_runner.ui.theme.NeonCyan
import com.example.geometric_neon_runner.ui.theme.NeonPink


@Composable
fun NeonButton(
    onClick: () -> Unit,
    text: String,
    neonColor: Color = NeonCyan,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding
) {
    val infiniteTransition = rememberInfiniteTransition(label = "neon_glow_pulse")
    val glow by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500),
            repeatMode = RepeatMode.Reverse
        ), label = "glow_animation"
    )

    val shadowElevation = if (enabled) glow.dp else 0.dp
    val buttonColor = if (enabled) neonColor else neonColor.copy(alpha = 0.5f)

    Box(
        modifier = modifier
            .shadow(
                elevation = shadowElevation,
                shape = RoundedCornerShape(8.dp),
                spotColor = buttonColor // Cor do glow
            )
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 2.dp,
                color = buttonColor,
                shape = RoundedCornerShape(8.dp)
            )
            .background(DarkBackground.copy(alpha = 0.5f))
            .clickable(
                onClick = onClick,
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge.copy(color = buttonColor),
            color = buttonColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeonTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    supportingText: (@Composable () -> Unit)? = null
) {
    val baseColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
    val errorColor = MaterialTheme.colorScheme.error

    val lineColor = when {
        isError -> errorColor
        value.isNotEmpty() -> baseColor
        else -> baseColor.copy(alpha = 0.4f)
    }

    val glowColor = if (isError) errorColor else baseColor
    val shadowElevation: Dp = if (value.isNotEmpty() || isError) 6.dp else 0.dp

    val containerColor = DarkBackground.copy(alpha = 0.8f)
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .shadow(
                elevation = shadowElevation,
                shape = RoundedCornerShape(8.dp),
                ambientColor = glowColor,
                spotColor = glowColor
            )
            .border(
                width = 1.dp,
                color = lineColor,
                shape = RoundedCornerShape(8.dp)
            ),
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = lineColor.copy(alpha = 0.8f)
            )
        },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground),
        supportingText = supportingText,
        isError = isError,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor,
            disabledContainerColor = containerColor,
            cursorColor = glowColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
        ),
        visualTransformation = visualTransformation
    )
}

@Composable
fun LoadingIndicator(
    neonColor: Color = NeonPink,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading_pulse")
    val glow by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse
        ), label = "loading_glow_animation"
    )

    Box(
        modifier = modifier
            .shadow(
                elevation = glow.dp,
                shape = RoundedCornerShape(50.dp),
                spotColor = neonColor,
                ambientColor = neonColor
            )
    ) {
        CircularProgressIndicator(
            color = neonColor,
            strokeWidth = 4.dp
        )
    }
}