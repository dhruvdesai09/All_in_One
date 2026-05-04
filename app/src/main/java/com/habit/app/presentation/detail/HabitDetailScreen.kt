package com.habit.app.presentation.detail

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habit.app.presentation.components.ActivityHeatmap
import com.habit.app.presentation.theme.AccentEmerald
import com.habit.app.presentation.theme.SurfaceBorder
import com.habit.app.presentation.theme.NexoraMint
import com.habit.app.presentation.theme.NexoraRose
import com.habit.app.presentation.theme.NexoraGold
import com.habit.app.presentation.theme.SurfaceCard
import com.habit.app.presentation.theme.TextMuted
import com.habit.app.presentation.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: HabitDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val habit = state.habit

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text  = habit?.title ?: "Habit",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        floatingActionButton = {
            if (habit != null) {
                ExtendedFloatingActionButton(
                    onClick           = { onEdit(habit.id) },
                    icon              = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                    text              = { Text("Edit") },
                    containerColor    = MaterialTheme.colorScheme.primary,
                    contentColor      = MaterialTheme.colorScheme.onPrimary,
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        if (habit == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text("Habit not found.", color = TextMuted)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Hero streak card
            StreakHeroCard(streak = state.streak)

            // Description card (if present)
            habit.description?.takeIf { it.isNotBlank() }?.let { desc ->
                Card(
                    colors    = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape     = MaterialTheme.shapes.large,
                    elevation = CardDefaults.cardElevation(2.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text  = "About",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextMuted,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text  = desc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            // Stats row
            Card(
                colors    = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape     = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(2.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    val activeDayLabels = listOf("M","T","W","T","F","S","S")
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text  = "Active days",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            activeDayLabels.forEachIndexed { i, label ->
                                val day = i + 1
                                val active = day in habit.activeWeekdays
                                Surface(
                                    color  = if (active) NexoraMint.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                    shape  = CircleShape,
                                    modifier = Modifier.size(24.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text  = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (active) NexoraMint else TextMuted,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Heatmap
            ActivityHeatmap(
                title = "Activity (20 weeks)",
                days  = state.heatmap,
            )

            Spacer(modifier = Modifier.height(80.dp)) // FAB clearance
        }
    }
}

@Composable
private fun StreakHeroCard(streak: Int) {
    val (gradStart, gradEnd, emoji, label) = when {
        streak >= 30 -> Quad(NexoraRose, NexoraRose.copy(alpha = 0.4f), "🔥", "on fire!")
        streak >= 7  -> Quad(NexoraGold, NexoraGold.copy(alpha = 0.4f), "⚡", "great streak!")
        streak > 0   -> Quad(NexoraMint, SurfaceBorder, "✅", "keep it up!")
        else         -> Quad(SurfaceCard, SurfaceCard, "🌱", "start today!")
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(
                brush = Brush.horizontalGradient(listOf(gradStart.copy(alpha = 0.25f), gradEnd.copy(alpha = 0.1f))),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = emoji, fontSize = 40.sp)
            Text(
                text       = "$streak",
                style      = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text  = if (streak == 1) "day streak" else "days streak",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
            Text(
                text  = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (streak == 0) TextMuted else AccentEmerald,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
private operator fun <A, B, C, D> Quad<A, B, C, D>.component1() = a
private operator fun <A, B, C, D> Quad<A, B, C, D>.component2() = b
private operator fun <A, B, C, D> Quad<A, B, C, D>.component3() = c
private operator fun <A, B, C, D> Quad<A, B, C, D>.component4() = d
