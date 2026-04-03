package com.habit.app.presentation.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habit.app.data.repository.HabitTodayItem
import com.habit.app.presentation.components.ActivityHeatmap
import com.habit.app.presentation.theme.AccentEmerald
import com.habit.app.presentation.theme.ForestGreenLight
import com.habit.app.presentation.theme.StreakOrange
import com.habit.app.presentation.theme.StreakYellow
import com.habit.app.presentation.theme.SurfaceBorder
import com.habit.app.presentation.theme.SurfaceCard
import com.habit.app.presentation.theme.SurfaceElevated
import com.habit.app.presentation.theme.TextMuted
import com.habit.app.presentation.theme.TextSecondary
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddHabit: () -> Unit,
    onSettings: () -> Unit,
    onAllHabits: () -> Unit,
    onOpenHabit: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text  = greeting(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text  = "Today",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onAllHabits) {
                        Icon(
                            androidx.compose.material.icons.Icons.Default.List,
                            contentDescription = "All Habits",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = onSettings) {
                        Icon(
                            androidx.compose.material.icons.Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick   = onAddHabit,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor   = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add habit")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding    = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Date subtitle
            item {
                Text(
                    text  = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }

            // Heatmap
            item {
                ActivityHeatmap(
                    title = "Last 14 weeks",
                    days  = state.heatmap,
                )
            }

            // Section header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Text(
                        text  = "Today's habits",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (state.items.isNotEmpty()) {
                        val done = state.items.count { it.completed }
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape,
                        ) {
                            Text(
                                text     = "$done / ${state.items.size}",
                                style    = MaterialTheme.typography.labelSmall,
                                color    = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
            }

            // Empty state
            if (state.items.isEmpty()) {
                item {
                    EmptyState()
                }
            }

            // Habit cards
            items(state.items, key = { it.habit.id }) { item ->
                HabitCard(
                    item    = item,
                    onToggle = { viewModel.toggle(item.habit.id, it) },
                    onClick  = { onOpenHabit(item.habit.id) },
                )
            }
        }
    }
}

@Composable
private fun HabitCard(
    item: HabitTodayItem,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
) {
    val accentColor by animateColorAsState(
        targetValue = if (item.completed) AccentEmerald else SurfaceBorder,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "accent",
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = SurfaceCard,
        ),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(72.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(accentColor, accentColor.copy(alpha = 0.4f)),
                        ),
                        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
                    ),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text  = item.habit.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (item.completed)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    else
                        MaterialTheme.colorScheme.onSurface,
                )
                item.habit.description?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text  = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        maxLines = 1,
                    )
                }
                if (item.streak > 0) {
                    StreakBadge(streak = item.streak)
                }
            }

            // Checkbox-style toggle
            Box(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (item.completed) ForestGreenLight
                        else SurfaceElevated,
                    )
                    .border(
                        width = 1.5.dp,
                        color = if (item.completed) ForestGreenLight else SurfaceBorder,
                        shape = CircleShape,
                    )
                    .clickable { onToggle(!item.completed) },
                contentAlignment = Alignment.Center,
            ) {
                if (item.completed) {
                    Icon(
                        imageVector   = Icons.Filled.Check,
                        contentDescription = "Done",
                        tint          = Color(0xFF051005),
                        modifier      = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun StreakBadge(streak: Int) {
    val (bg, text, emoji) = when {
        streak >= 30 -> Triple(StreakOrange.copy(alpha = 0.2f), StreakOrange, "🔥")
        streak >= 7  -> Triple(StreakYellow.copy(alpha = 0.2f), StreakYellow, "⚡")
        else         -> Triple(ForestGreenLight.copy(alpha = 0.15f), ForestGreenLight, "✓")
    }
    Surface(
        color = bg,
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(text = emoji, fontSize = 10.sp)
            Text(
                text  = "$streak day streak",
                style = MaterialTheme.typography.labelSmall,
                color = text,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "🌱", fontSize = 48.sp)
        Text(
            text  = "Nothing scheduled for today",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text  = "Tap + to add a new habit or\nadjust its active days.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )
    }
}

private fun greeting(): String {
    return when (LocalTime.now().hour) {
        in 0..11  -> "Good morning"
        in 12..16 -> "Good afternoon"
        else      -> "Good evening"
    }
}
