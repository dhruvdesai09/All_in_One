package com.habit.app.presentation.passwords

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.habit.app.presentation.theme.AccentEmerald
import com.habit.app.presentation.theme.DangerRed
import com.habit.app.presentation.theme.ForestGreen
import com.habit.app.presentation.theme.SurfaceBorder
import com.habit.app.presentation.theme.SurfaceCard
import com.habit.app.presentation.theme.SurfaceElevated
import com.habit.app.presentation.theme.TextMuted
import com.habit.app.presentation.theme.TextSecondary

private val changePinKeypad = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "⌫")

@Composable
fun ChangePinDialog(
    state: ChangePinState,
    onInputChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
    onSuccessDismiss: () -> Unit,
) {
    // Auto-dismiss after success
    LaunchedEffect(state.success) {
        if (state.success) {
            kotlinx.coroutines.delay(1800)
            onSuccessDismiss()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
        ) {
            if (state.success) {
                // ── Success screen ────────────────────────────────────
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(AccentEmerald.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = AccentEmerald,
                                modifier = Modifier.size(40.dp),
                            )
                        }
                        Text(
                            "PIN Changed!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            "Your vault PIN has been updated successfully.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp),
                        )
                    }
                }
            } else {
                // ── Step flow ─────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.height(24.dp))

                    // Top bar with back button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Cancel",
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        // Step indicator
                        StepIndicator(
                            currentStep = when (state.step) {
                                ChangePinStep.VERIFY_OLD -> 1
                                ChangePinStep.SET_NEW -> 2
                                ChangePinStep.CONFIRM_NEW -> 3
                            },
                            totalSteps = 3,
                        )
                    }

                    Spacer(Modifier.height(32.dp))

                    // Icon
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(ForestGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.LockReset,
                            contentDescription = null,
                            tint = AccentEmerald,
                            modifier = Modifier.size(36.dp),
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // Title & subtitle animate with step
                    AnimatedContent(
                        targetState = state.step,
                        transitionSpec = {
                            (fadeIn() + slideInVertically { it / 3 }) togetherWith
                                (fadeOut() + slideOutVertically { -it / 3 })
                        },
                        label = "step_title",
                    ) { step ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = when (step) {
                                    ChangePinStep.VERIFY_OLD -> "Verify Current PIN"
                                    ChangePinStep.SET_NEW -> "Set New PIN"
                                    ChangePinStep.CONFIRM_NEW -> "Confirm New PIN"
                                },
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = when (step) {
                                    ChangePinStep.VERIFY_OLD -> "Enter your existing PIN to continue"
                                    ChangePinStep.SET_NEW -> "Choose a new 4–6 digit PIN"
                                    ChangePinStep.CONFIRM_NEW -> "Re-enter your new PIN to confirm"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

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
                                    .background(if (filled) AccentEmerald else SurfaceElevated)
                                    .border(
                                        width = 1.5.dp,
                                        color = if (filled) AccentEmerald else SurfaceBorder,
                                        shape = CircleShape,
                                    ),
                            )
                        }
                    }

                    // Error
                    AnimatedVisibility(
                        visible = state.error != null,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut(),
                    ) {
                        Text(
                            text = state.error ?: "",
                            color = DangerRed,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 12.dp),
                            textAlign = TextAlign.Center,
                        )
                    }

                    Spacer(Modifier.height(32.dp))

                    // Keypad
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        changePinKeypad.chunked(3).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                                row.forEach { key ->
                                    ChangePinKeypadButton(
                                        label = key,
                                        onClick = {
                                            when (key) {
                                                "⌫" -> onInputChange(state.pinInput.dropLast(1))
                                                "" -> {}
                                                else -> onInputChange(state.pinInput + key)
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = onSubmit,
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
                            text = when (state.step) {
                                ChangePinStep.VERIFY_OLD -> "Verify"
                                ChangePinStep.SET_NEW -> "Next"
                                ChangePinStep.CONFIRM_NEW -> "Change PIN"
                            },
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(currentStep: Int, totalSteps: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(totalSteps) { idx ->
            val active = idx + 1 == currentStep
            val done = idx + 1 < currentStep
            Box(
                modifier = Modifier
                    .size(width = if (active) 24.dp else 8.dp, height = 8.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            active -> AccentEmerald
                            done -> AccentEmerald.copy(alpha = 0.5f)
                            else -> SurfaceElevated
                        },
                    ),
            )
        }
    }
}

@Composable
private fun ChangePinKeypadButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(if (label.isEmpty()) Color.Transparent else SurfaceCard)
            .then(if (label.isNotEmpty()) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                fontSize = 22.sp,
                fontWeight = if (label == "⌫") FontWeight.Normal else FontWeight.Medium,
                color = if (label == "⌫") TextSecondary else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
