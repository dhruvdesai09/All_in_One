package com.habit.app.presentation.addedithabit

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habit.app.presentation.theme.AccentEmerald
import com.habit.app.presentation.theme.DangerRed
import com.habit.app.presentation.theme.NexoraMint
import com.habit.app.presentation.theme.SurfaceBorder
import com.habit.app.presentation.theme.SurfaceCard
import com.habit.app.presentation.theme.SurfaceElevated
import com.habit.app.presentation.theme.TextMuted

private val DAY_DATA = listOf(
    1 to "M", 2 to "T", 3 to "W", 4 to "T", 5 to "F", 6 to "S", 7 to "S",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditHabitScreen(
    onBack: () -> Unit,
    viewModel: AddEditHabitViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.saved, state.deleted) {
        if (state.saved || state.deleted) onBack()
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = null,
                    tint   = DangerRed,
                    modifier = Modifier.size(28.dp),
                )
            },
            title = { Text("Delete habit?") },
            text  = {
                Text(
                    "This will permanently delete \"${state.title}\" and all its history.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.delete(); showDeleteDialog = false },
                    colors  = ButtonDefaults.buttonColors(containerColor = DangerRed),
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
            containerColor = SurfaceCard,
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.habitId == null) "New habit" else "Edit habit",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.habitId != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = "Delete",
                                tint = DangerRed,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
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
            // Name & description card
            Card(
                colors    = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape     = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(2.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value          = state.title,
                        onValueChange  = viewModel::setTitle,
                        label          = { Text("Habit name") },
                        placeholder    = { Text("e.g. Morning run", color = TextMuted) },
                        singleLine     = true,
                        modifier       = Modifier.fillMaxWidth(),
                        textStyle      = MaterialTheme.typography.bodyLarge,
                        colors         = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = NexoraMint,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedLabelColor    = NexoraMint,
                        ),
                    )
                    OutlinedTextField(
                        value         = state.description,
                        onValueChange = viewModel::setDescription,
                        label         = { Text("Description (optional)") },
                        modifier      = Modifier.fillMaxWidth(),
                        minLines      = 2,
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = NexoraMint,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedLabelColor    = NexoraMint,
                        ),
                    )
                }
            }

            // Schedule Type
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.setIsOneTime(false) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!state.isOneTime) NexoraMint else SurfaceElevated,
                        contentColor = if (!state.isOneTime) Color(0xFF051005) else TextMuted
                    )
                ) {
                    Text("Repeating Habit")
                }
                Button(
                    onClick = { viewModel.setIsOneTime(true) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.isOneTime) NexoraMint else SurfaceElevated,
                        contentColor = if (state.isOneTime) Color(0xFF051005) else TextMuted
                    )
                ) {
                    Text("One-time Task")
                }
            }

            // Scheduling Card
            Card(
                colors    = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape     = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(2.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (!state.isOneTime) {
                        Text(
                            text  = "Active days",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text  = "Which days should this habit repeat?",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            DAY_DATA.forEach { (day, label) ->
                                val selected = day in state.activeDays
                                DayToggle(
                                    label    = label,
                                    selected = selected,
                                    onClick  = { viewModel.toggleDay(day) },
                                )
                            }
                        }
                    } else {
                        var showDateDialog by remember { mutableStateOf(false) }
                        if (showDateDialog) {
                            @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
                            val dateState = androidx.compose.material3.rememberDatePickerState(
                                initialSelectedDateMillis = state.targetDateEpochDay * 86400000L
                            )
                            @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
                            androidx.compose.material3.DatePickerDialog(
                                onDismissRequest = { showDateDialog = false },
                                confirmButton = {
                                    TextButton(onClick = {
                                        dateState.selectedDateMillis?.let {
                                            viewModel.setTargetDate(it / 86400000L)
                                        }
                                        showDateDialog = false
                                    }) { Text("OK", color = NexoraMint) }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDateDialog = false }) { Text("Cancel") }
                                },
                                colors = androidx.compose.material3.DatePickerDefaults.colors(containerColor = SurfaceCard)
                            ) {
                                androidx.compose.material3.DatePicker(state = dateState)
                            }
                        }

                        Text(
                            text  = "Target Date",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text  = "When should this task be completed?",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            onClick = { showDateDialog = true },
                            color   = MaterialTheme.colorScheme.surfaceVariant,
                            shape   = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = NexoraMint,
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(
                                    text  = java.time.LocalDate.ofEpochDay(state.targetDateEpochDay).format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy")),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NexoraMint,
                                )
                            }
                        }
                    }
                }
            }

            // Save button
            Button(
                onClick  = viewModel::save,
                enabled  = state.title.isNotBlank() && state.activeDays.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape  = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor  = NexoraMint,
                    contentColor    = Color(0xFF051005),
                    disabledContainerColor = SurfaceElevated,
                    disabledContentColor   = TextMuted,
                ),
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text  = if (state.habitId == null) "Create habit" else "Save changes",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun DayToggle(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg    = if (selected) NexoraMint else SurfaceElevated
    val fg    = if (selected) Color(0xFF051005) else TextMuted
    val border = if (selected) NexoraMint else SurfaceBorder

    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(bg)
            .border(1.5.dp, border, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text       = label,
            style      = MaterialTheme.typography.labelMedium,
            color      = fg,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}
