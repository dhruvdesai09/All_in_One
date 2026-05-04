package com.habit.app.presentation.allhabits

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.habit.app.presentation.home.HomeViewModel
import com.habit.app.presentation.theme.NexoraGold
import com.habit.app.presentation.theme.NexoraGoldDim
import com.habit.app.presentation.theme.NexoraMint
import com.habit.app.presentation.theme.SurfaceBorder
import com.habit.app.presentation.theme.SurfaceCard
import com.habit.app.presentation.theme.SurfaceElevated
import com.habit.app.presentation.theme.TextMuted
import java.time.LocalDate

@Composable
fun AllHabitsScreen(
    onBack: () -> Unit,
    onHabitClick: (Long) -> Unit,
    onAddHabit: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.linearGradient(listOf(NexoraGold, NexoraGoldDim)))
                    .clickable(onClick = onAddHabit),
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = Color.White, fontSize = 28.sp)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(10.dp)) }

            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Habits",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    // We omit the extra header icons for now as per minimal UI, 
                    // or user can tap back if needed (but it's a bottom nav tab).
                }
            }

            // Days row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val today = LocalDate.now()
                    val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
                    val days = (0..6).map { startOfWeek.plusDays(it.toLong()) }
                    
                    days.forEach { date ->
                        val isToday = date == today
                        val isPast = date.isBefore(today)
                        DayDot(
                            dayInitial = date.dayOfWeek.name.take(1),
                            date = date.dayOfMonth.toString(),
                            isToday = isToday,
                            isDone = isPast // mockup simplification: past days marked as done/tinted
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }

            item {
                Text(
                    text = "TODAY'S HABITS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            if (state.items.isEmpty()) {
                item {
                    Text(
                        text = "No habits scheduled for today.",
                        color = TextMuted,
                        modifier = Modifier.padding(top = 20.dp)
                    )
                }
            }

            items(state.items, key = { it.habit.id }) { item ->
                HabitItemRow(
                    item = item,
                    onToggle = { viewModel.toggle(item.habit.id, it) },
                    onClick = { onHabitClick(item.habit.id) }
                )
            }
            
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun DayDot(dayInitial: String, date: String, isToday: Boolean, isDone: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = dayInitial, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    when {
                        isToday -> NexoraGold
                        isDone -> NexoraGold.copy(alpha = 0.18f)
                        else -> SurfaceElevated
                    }
                )
                .border(
                    width = 1.dp,
                    color = if (isToday) NexoraGold else SurfaceBorder,
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = when {
                    isToday -> Color(0xFF111111)
                    isDone -> NexoraGold
                    else -> TextMuted
                }
            )
        }
    }
}

@Composable
private fun HabitItemRow(
    item: HabitTodayItem,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    Surface(
        color = SurfaceElevated,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Checkbox
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (item.completed) NexoraGold else Color.Transparent)
                    .border(2.dp, if (item.completed) NexoraGold else SurfaceBorder, CircleShape)
                    .clickable { onToggle(!item.completed) },
                contentAlignment = Alignment.Center
            ) {
                if (item.completed) {
                    Text("✓", fontSize = 14.sp, color = Color(0xFF111111))
                }
            }

            // Info
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = item.habit.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val status = if (item.completed) "Done" else "Pending"
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            // Streak badge
            if (item.streak > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NexoraGold.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "🔥 ${item.streak}",
                        style = MaterialTheme.typography.labelMedium,
                        color = NexoraGold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
