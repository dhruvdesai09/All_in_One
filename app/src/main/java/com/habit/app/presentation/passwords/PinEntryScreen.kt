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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habit.app.presentation.theme.AccentEmerald
import com.habit.app.presentation.theme.DangerRed
import com.habit.app.presentation.theme.ForestGreen
import com.habit.app.presentation.theme.SurfaceBorder
import com.habit.app.presentation.theme.SurfaceCard
import com.habit.app.presentation.theme.SurfaceElevated
import com.habit.app.presentation.theme.TextMuted
import com.habit.app.presentation.theme.TextSecondary

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
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(ForestGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = AccentEmerald,
                modifier = Modifier.size(36.dp),
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = if (isCreate) "Set a vault PIN" else "Enter vault PIN",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (isCreate)
                "Choose a 4–6 digit PIN to protect your passwords"
            else
                "Enter your PIN to access the password vault",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
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
                        .size(if (filled) 16.dp else 14.dp)
                        .clip(CircleShape)
                        .background(
                            if (filled) AccentEmerald
                            else SurfaceElevated
                        )
                        .border(
                            width = 1.5.dp,
                            color = if (filled) AccentEmerald else SurfaceBorder,
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
                color = DangerRed,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        Spacer(Modifier.height(32.dp))

        // Keypad
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            keypad.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    row.forEach { key ->
                        KeypadButton(
                            label = key,
                            onClick = {
                                when (key) {
                                    "⌫" -> viewModel.onPinInputChange(
                                        state.pinInput.dropLast(1)
                                    )
                                    "" -> { /* empty slot */ }
                                    else -> viewModel.onPinInputChange(state.pinInput + key)
                                }
                            },
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { viewModel.submitPin() },
            enabled = state.pinInput.length >= 4,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ForestGreen,
                contentColor = Color.White,
                disabledContainerColor = SurfaceElevated,
                disabledContentColor = TextMuted,
            ),
        ) {
            Text(
                text = if (isCreate) "Set PIN" else "Unlock",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
            )
        }
    }
}

@Composable
private fun KeypadButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(
                if (label.isEmpty()) Color.Transparent
                else SurfaceCard
            )
            .then(
                if (label.isNotEmpty()) Modifier.clickable(onClick = onClick)
                else Modifier
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                fontSize = 22.sp,
                fontWeight = if (label == "⌫") FontWeight.Normal else FontWeight.Medium,
                color = if (label == "⌫") TextSecondary
                else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
