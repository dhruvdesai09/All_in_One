package com.habit.app.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp                              // ✅ Fixed: was androidx.glance.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.Action
import androidx.glance.action.clickable                         // ✅ Fixed: was androidx.glance.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.habit.app.MainActivity
import com.habit.app.data.repository.DayIntensity
import com.habit.app.di.WidgetEntryPoint
import com.habit.app.domain.WidgetDisplayMode
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class HabitWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entry = EntryPointAccessors.fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
        val repo = entry.habitRepository()
        val prefs = entry.userPreferences().flow.first()
        val today = LocalDate.now()
        val openApp: Action = actionStartActivity(
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
        )
        val mode = prefs.widgetMode
        val pendingTitles = if (mode == WidgetDisplayMode.Pending) repo.getPendingTitlesForDate(today) else emptyList()
        val heatStart = today.minusDays(48)
        val heatmap = if (mode == WidgetDisplayMode.Heatmap) repo.heatmapSnapshot(heatStart, today).associateBy { it.date } else emptyMap()

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(ColorProvider(Color(0xFF121318)))
                        .padding(10.dp)
                        .clickable(openApp),
                ) {
                    when (mode) {
                        WidgetDisplayMode.Pending -> PendingSection(pendingTitles)
                        WidgetDisplayMode.Heatmap -> HeatmapSection(heatStart, heatmap)
                    }
                }
            }
        }
    }
}

@SuppressLint("RestrictedApi")
@Composable
private fun PendingSection(titles: List<String>) {
    Text(
        text = "Today",
        style = TextStyle(color = ColorProvider(Color(0xFFE8E8E8)), fontWeight = FontWeight.Medium),
    )
    Spacer(modifier = GlanceModifier.height(6.dp))
    if (titles.isEmpty()) {
        Text(
            text = "All caught up",
            style = TextStyle(color = ColorProvider(Color(0xFF8F9A8B))),
        )
    } else {
        titles.take(5).forEach { t ->
            Text(
                text = "• $t",
                style = TextStyle(color = ColorProvider(Color(0xFFB8C5B0))),
            )
            Spacer(modifier = GlanceModifier.height(2.dp))
        }
        if (titles.size > 5) {
            Text(
                text = "+${titles.size - 5} more",
                style = TextStyle(color = ColorProvider(Color(0xFF6B7C66))),
            )
        }
    }
}

@Composable
private fun HeatmapSection(start: LocalDate, cells: Map<LocalDate, DayIntensity>) {
    Text(
        text = "Activity",
        style = TextStyle(color = ColorProvider(Color(0xFFE8E8E8)), fontWeight = FontWeight.Medium),
    )
    Spacer(modifier = GlanceModifier.height(6.dp))
    Column(horizontalAlignment = Alignment.Start) {
        repeat(7) { row ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(7) { col ->
                    val idx = row * 7 + col
                    val d = start.plusDays(idx.toLong())
                    val di = cells[d]
                    Box(
                        modifier = GlanceModifier
                            .padding(2.dp)
                            .size(10.dp)
                            .background(ColorProvider(cellColor(di))),
                    ) {}
                }
            }
        }
    }
}

private fun cellColor(di: DayIntensity?): Color {
    if (di == null || !di.hadScheduledHabits) return Color(0xFF23262D)
    val v = di.intensity
    return when {
        v <= 0f -> Color(0xFF2E4034)
        v < 0.34f -> Color(0xFF3D5C45)
        v < 0.67f -> Color(0xFF4CAF50)
        else -> Color(0xFF1B5E20)
    }
}