package com.habit.app.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habit.app.domain.WidgetDisplayMode
import com.habit.app.presentation.theme.AccentEmerald
import com.habit.app.presentation.theme.ForestGreenLight
import com.habit.app.presentation.theme.SurfaceBorder
import com.habit.app.presentation.theme.SurfaceCard
import com.habit.app.presentation.theme.TextMuted
import com.habit.app.presentation.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()
    var showTime by remember { mutableStateOf(false) }

    if (showTime) {
        val timeState = rememberTimePickerState(
            initialHour   = prefs.reminderHour,
            initialMinute = prefs.reminderMinute,
            is24Hour      = true,
        )
        AlertDialog(
            onDismissRequest = { showTime = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setReminder(timeState.hour, timeState.minute)
                    showTime = false
                }) { Text("OK", color = ForestGreenLight) }
            },
            dismissButton = {
                TextButton(onClick = { showTime = false }) { Text("Cancel") }
            },
            text  = { TimePicker(state = timeState) },
            title = { Text("Reminder time") },
            containerColor = SurfaceCard,
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Settings", style = MaterialTheme.typography.titleLarge)
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
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ---------- Notifications section ----------
            SectionCard {
                SectionHeader(
                    icon  = Icons.Outlined.Notifications,
                    title = "Notifications",
                )
                HorizontalDivider(color = SurfaceBorder, modifier = Modifier.padding(vertical = 4.dp))

                SettingsRow(
                    label    = "Daily reminder",
                    subtitle = "Notify when habits are incomplete",
                ) {
                    Switch(
                        checked  = prefs.remindersEnabled,
                        onCheckedChange = viewModel::setRemindersEnabled,
                        colors   = SwitchDefaults.colors(
                            checkedThumbColor  = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor  = ForestGreenLight,
                        ),
                    )
                }

                HorizontalDivider(color = SurfaceBorder.copy(alpha = 0.5f))

                SettingsRow(
                    label    = "Reminder time",
                    subtitle = "When to send the daily notification",
                ) {
                    Surface(
                        onClick = { if (prefs.remindersEnabled) showTime = true },
                        color   = if (prefs.remindersEnabled) ForestGreenLight.copy(alpha = 0.15f)
                                  else MaterialTheme.colorScheme.surfaceVariant,
                        shape   = MaterialTheme.shapes.small,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                Icons.Outlined.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (prefs.remindersEnabled) ForestGreenLight else TextMuted,
                            )
                            Text(
                                text  = "%02d:%02d".format(prefs.reminderHour, prefs.reminderMinute),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = if (prefs.remindersEnabled) ForestGreenLight else TextMuted,
                            )
                        }
                    }
                }
            }

            // ---------- Widget section ----------
            SectionCard {
                SectionHeader(
                    icon  = Icons.Outlined.Widgets,
                    title = "Home screen widget",
                )
                HorizontalDivider(color = SurfaceBorder, modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    text     = "Choose the default widget view. Re-add the widget to apply layout changes.",
                    style    = MaterialTheme.typography.bodySmall,
                    color    = TextSecondary,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    WidgetDisplayMode.entries.forEach { mode ->
                        val selected = prefs.widgetMode == mode
                        FilterChip(
                            selected = selected,
                            onClick  = { viewModel.setWidgetMode(mode) },
                            label    = {
                                Text(
                                    text  = if (mode == WidgetDisplayMode.Pending) "Pending list" else "Heatmap",
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor     = ForestGreenLight.copy(alpha = 0.2f),
                                selectedLabelColor         = ForestGreenLight,
                                selectedLeadingIconColor   = ForestGreenLight,
                            ),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Card(
        colors    = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape     = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(2.dp),
        modifier  = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(bottom = 4.dp),
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint     = AccentEmerald,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text  = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun SettingsRow(
    label: String,
    subtitle: String,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextMuted)
        }
        trailing()
    }
}
