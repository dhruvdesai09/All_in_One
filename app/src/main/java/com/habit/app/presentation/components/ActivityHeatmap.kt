package com.habit.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habit.app.data.repository.DayIntensity
import com.habit.app.presentation.theme.HeatEmpty
import com.habit.app.presentation.theme.HeatHigh
import com.habit.app.presentation.theme.HeatLow
import com.habit.app.presentation.theme.HeatMid
import com.habit.app.presentation.theme.HeatMidHigh
import com.habit.app.presentation.theme.HeatNone
import java.time.format.TextStyle
import java.util.Locale

private val DAY_LABELS   = listOf("M", "T", "W", "T", "F", "S", "S")
private val CELL_SIZE    = 13.dp
private val CELL_GAP     = 3.dp
private val LABEL_WIDTH  = 12.dp

@Composable
fun ActivityHeatmap(
    title: String,
    days: List<DayIntensity>,
    modifier: Modifier = Modifier,
    showTitle: Boolean = true
) {
    // Pad days list so the first element is Monday (if needed)
    val paddedDays = mutableListOf<DayIntensity?>()
    val firstDate = days.firstOrNull()?.date
    if (firstDate != null) {
        val firstDow = firstDate.dayOfWeek.value - 1
        repeat(firstDow) { paddedDays.add(null) }
    }
    paddedDays.addAll(days)
    
    // Split into ISO weeks (each chunk of 7 = Mon..Sun)
    val weeks = paddedDays.chunked(7)

    // Derive month labels
    val monthLabels = weeks.mapIndexed { idx, week ->
        val firstRealDate = week.firstNotNullOfOrNull { it?.date } ?: return@mapIndexed null
        val month = firstRealDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        if (idx == 0) month
        else {
            val prevFirst = weeks[idx - 1].firstNotNullOfOrNull { it?.date }
            if (prevFirst?.month != firstRealDate.month) month else null
        }
    }

    Surface(
        modifier  = modifier.fillMaxWidth(),
        color     = MaterialTheme.colorScheme.surfaceVariant,
        shape     = MaterialTheme.shapes.large,
        tonalElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (showTitle) {
                Text(
                    text  = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(10.dp))
            }

            Row(verticalAlignment = Alignment.Top) {
                Spacer(modifier = Modifier.width(LABEL_WIDTH + CELL_GAP))
                Row(horizontalArrangement = Arrangement.spacedBy(CELL_GAP)) {
                    monthLabels.forEach { label ->
                        // Don't restrict the width, just prevent it from wrapping
                        Box(modifier = Modifier.width(CELL_SIZE)) {
                            if (label != null) {
                                Text(
                                    text      = label,
                                    fontSize  = 9.sp,
                                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines  = 1,
                                    softWrap  = false,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.Top) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(CELL_GAP),
                    modifier = Modifier.width(LABEL_WIDTH),
                ) {
                    DAY_LABELS.forEach { label ->
                        Box(
                            modifier = Modifier.size(CELL_SIZE),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text     = label,
                                fontSize = 9.sp,
                                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(CELL_GAP))

                Row(horizontalArrangement = Arrangement.spacedBy(CELL_GAP)) {
                    weeks.forEach { week ->
                        Column(verticalArrangement = Arrangement.spacedBy(CELL_GAP)) {
                            week.forEach { d ->
                                if (d == null) {
                                    Box(modifier = Modifier.size(CELL_SIZE))
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(CELL_SIZE)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(heatmapCellColor(d)),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Legend
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text     = "Less",
                    fontSize = 9.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(4.dp))
                listOf(HeatEmpty, HeatNone, HeatLow, HeatMid, HeatMidHigh, HeatHigh).forEach { c ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(c),
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text     = "More",
                    fontSize = 9.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun heatmapCellColor(d: DayIntensity): Color {
    if (!d.hadScheduledHabits) return HeatEmpty
    return when {
        d.intensity <= 0f   -> HeatNone
        d.intensity < 0.34f -> HeatLow
        d.intensity < 0.67f -> HeatMid
        d.intensity < 1f    -> HeatMidHigh
        else                -> HeatHigh
    }
}

