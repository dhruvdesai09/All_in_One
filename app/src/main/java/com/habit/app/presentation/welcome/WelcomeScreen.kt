package com.habit.app.presentation.welcome

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habit.app.presentation.theme.NexoraGold
import com.habit.app.presentation.theme.NexoraGoldDim
import com.habit.app.presentation.theme.NexoraMint
import com.habit.app.presentation.theme.NexoraRose
import com.habit.app.presentation.theme.SurfaceBorder
import com.habit.app.presentation.theme.SurfaceCard
import com.habit.app.presentation.theme.SurfaceElevated
import com.habit.app.presentation.theme.TextMuted
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WelcomeScreen(onEnter: () -> Unit) {
    // Entrance animations
    val logoScale  = remember { Animatable(0.6f) }
    val logoAlpha  = remember { Animatable(0f) }
    val bodyAlpha  = remember { Animatable(0f) }
    val btnAlpha   = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            logoScale.animateTo(1f, tween(600, easing = EaseOutCubic))
        }
        launch {
            logoAlpha.animateTo(1f, tween(500))
        }
        delay(300)
        bodyAlpha.animateTo(1f, tween(500))
        delay(200)
        btnAlpha.animateTo(1f, tween(400))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Radial glow
        Box(
            modifier = Modifier
                .offset(x = 100.dp, y = (-80).dp)
                .size(300.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NexoraGold.copy(alpha = 0.14f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            // App logo / icon
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
                    .shadow(32.dp, RoundedCornerShape(22.dp), spotColor = NexoraGold.copy(alpha = 0.35f))
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(NexoraGold, NexoraGoldDim)
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "N",
                    fontSize = 32.sp,
                    fontFamily = MaterialTheme.typography.headlineLarge.fontFamily,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                )
            }

            Spacer(Modifier.height(24.dp))

            // Tagline & Subtitle
            Column(
                modifier = Modifier.alpha(bodyAlpha.value),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val tagline = buildAnnotatedString {
                    append("Your life,\n")
                    withStyle(style = SpanStyle(color = NexoraGold)) {
                        append("one command\n")
                    }
                    append("center.")
                }
                Text(
                    text = tagline,
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 32.sp,
                    lineHeight = 36.sp
                )
                Text(
                    text = "Habits, passwords & budget — beautifully unified. Private by default.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    lineHeight = 22.sp
                )
            }

            Spacer(Modifier.height(32.dp))

            // Feature pills
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(bodyAlpha.value),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                FeaturePill(
                    emoji = "🌱",
                    title = "Habit Tracker",
                    subtitle = "Build streaks, stay consistent",
                    tint = NexoraMint
                )
                FeaturePill(
                    emoji = "🔑",
                    title = "Password Vault",
                    subtitle = "PIN-protected, encrypted",
                    tint = NexoraGold
                )
                FeaturePill(
                    emoji = "💰",
                    title = "Budget Tracker",
                    subtitle = "Know where your money goes",
                    tint = NexoraRose
                )
            }

            Spacer(Modifier.height(40.dp))

            // Enter button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(btnAlpha.value)
                    .shadow(24.dp, RoundedCornerShape(16.dp), spotColor = NexoraGold.copy(alpha = 0.3f))
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(NexoraGold, NexoraGoldDim)
                        )
                    )
            ) {
                Button(
                    onClick = onEnter,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Get Started →",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun FeaturePill(
    emoji: String,
    title: String,
    subtitle: String,
    tint: Color,
) {
    Surface(
        color = SurfaceElevated,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = emoji, fontSize = 18.sp)
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}
