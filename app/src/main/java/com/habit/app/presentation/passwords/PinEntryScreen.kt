package com.habit.app.presentation.passwords

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habit.app.presentation.theme.NexoraGold
import com.habit.app.presentation.theme.NexoraGoldDim
import com.habit.app.presentation.theme.NexoraRose
import com.habit.app.presentation.theme.SurfaceBorder
import com.habit.app.presentation.theme.SurfaceElevated
import com.habit.app.presentation.theme.TextMuted

private val keypad = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "⌫")

@Composable
fun PinEntryScreen(
    viewModel: PasswordViewModel = hiltViewModel(),
    onUnlocked: () -> Unit,
) {
    val state by viewModel.vaultState.collectAsStateWithLifecycle()

    if (state.isUnlocked) {
        onUnlocked()
        return
    }

    val isCreate = state.pinHash == null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(72.dp)
                .shadow(40.dp, RoundedCornerShape(24.dp), spotColor = NexoraGold.copy(alpha = 0.2f))
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            NexoraGold.copy(alpha = 0.2f),
                            NexoraGoldDim.copy(alpha = 0.15f)
                        )
                    )
                )
                .border(1.dp, NexoraGold.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "🔐", fontSize = 32.sp)
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = if (isCreate) "Set Vault PIN" else "Vault Access",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (isCreate)
                "Choose a 4–6 digit PIN to protect your passwords"
            else
                "Enter your PIN to continue",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(32.dp))

        // PIN dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(6) { idx ->
                val filled = idx < state.pinInput.length
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(if (filled) NexoraGold else SurfaceElevated)
                        .border(
                            width = 2.dp,
                            color = if (filled) NexoraGold else SurfaceBorder,
                            shape = CircleShape,
                        ),
                )
            }
        }

        // Error message
        AnimatedVisibility(
            visible = state.pinError != null,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut(),
        ) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = state.pinError ?: "",
                color = NexoraRose,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        Spacer(Modifier.height(32.dp))

        // Keypad
        Column(
            modifier = Modifier.fillMaxWidth(0.85f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            keypad.chunked(3).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    row.forEach { key ->
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            KeypadButton(
                                label = key,
                                onClick = {
                                    when (key) {
                                        "⌫" -> viewModel.onPinInputChange(state.pinInput.dropLast(1))
                                        "" -> { /* empty slot */ }
                                        else -> viewModel.onPinInputChange(state.pinInput + key)
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // CTA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(24.dp, RoundedCornerShape(16.dp), spotColor = NexoraGold.copy(alpha = 0.3f))
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.linearGradient(listOf(NexoraGold, NexoraGoldDim)))
        ) {
            Button(
                onClick = { viewModel.submitPin() },
                enabled = state.pinInput.length >= 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = Color.White.copy(alpha = 0.5f),
                )
            ) {
                Text(
                    text = if (isCreate) "Set PIN" else "Unlock Vault",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun KeypadButton(label: String, onClick: () -> Unit) {
    if (label.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f))
        return
    }

    Box(
        modifier = Modifier
            .fillMaxWidth(0.8f) // scale down slightly so they aren't touching
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(SurfaceElevated)
            .border(1.dp, SurfaceBorder, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = if (label == "⌫") 16.sp else 20.sp,
            fontWeight = FontWeight.Medium,
            color = if (label == "⌫") TextMuted else MaterialTheme.colorScheme.onSurface,
        )
    }
}
