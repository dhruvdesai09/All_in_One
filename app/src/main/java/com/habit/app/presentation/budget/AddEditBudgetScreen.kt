package com.habit.app.presentation.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habit.app.presentation.theme.AccentEmerald
import com.habit.app.presentation.theme.DangerRed
import com.habit.app.presentation.theme.DangerRedDim
import com.habit.app.presentation.theme.ForestGreen
import com.habit.app.presentation.theme.SurfaceBorder
import com.habit.app.presentation.theme.SurfaceCard
import com.habit.app.presentation.theme.SurfaceElevated
import com.habit.app.presentation.theme.TextMuted
import com.habit.app.presentation.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditBudgetScreen(
    editId: Long?,
    onBack: () -> Unit,
    viewModel: BudgetViewModel = hiltViewModel(),
) {
    val form by viewModel.budgetForm.collectAsStateWithLifecycle()
    val currency by viewModel.currencySymbol.collectAsStateWithLifecycle()

    LaunchedEffect(editId) { viewModel.loadBudgetForEdit(editId) }
    LaunchedEffect(form.isSaved) { if (form.isSaved) { viewModel.resetBudgetForm(); onBack() } }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (editId == null) "New Budget" else "Edit Budget",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (editId != null) {
                        IconButton(onClick = { viewModel.deleteBudget(editId) { onBack() } }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete",
                                tint = DangerRed)
                        }
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Preview
            val previewColor = try { Color(form.colorHex.toColorInt()) } catch (e: Exception) { AccentEmerald }
            Box(
                modifier = Modifier.size(72.dp).clip(CircleShape)
                    .background(previewColor.copy(alpha = 0.2f))
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center,
            ) { Text(form.emoji, fontSize = 30.sp) }

            // Name
            SectionLabel("Category Name")
            OutlinedTextField(
                value = form.name,
                onValueChange = viewModel::onBudgetName,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. Food & Drinks", color = TextMuted) },
                isError = form.nameError != null,
                supportingText = form.nameError?.let { { Text(it, color = DangerRed) } },
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors(),
                singleLine = true,
            )

            // Monthly limit
            SectionLabel("Monthly Limit ($currency)")
            OutlinedTextField(
                value = form.limitInput,
                onValueChange = viewModel::onBudgetLimit,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. 5000", color = TextMuted) },
                isError = form.limitError != null,
                supportingText = form.limitError?.let { { Text(it, color = DangerRed) } },
                prefix = { Text(currency, color = TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors(),
                singleLine = true,
            )

            // Emoji picker
            SectionLabel("Icon")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PRESET_EMOJIS.forEach { emoji ->
                    val selected = emoji == form.emoji
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (selected) previewColor.copy(alpha = 0.2f) else SurfaceElevated)
                            .border(1.5.dp, if (selected) previewColor else SurfaceBorder, CircleShape)
                            .clickable { viewModel.onBudgetEmoji(emoji) },
                        contentAlignment = Alignment.Center,
                    ) { Text(emoji, fontSize = 18.sp) }
                }
            }

            // Color picker
            SectionLabel("Color")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PRESET_COLORS.forEach { hex ->
                    val color = try { Color(hex.toColorInt()) } catch (e: Exception) { AccentEmerald }
                    val selected = hex == form.colorHex
                    Box(
                        modifier = Modifier
                            .size(if (selected) 36.dp else 32.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(if (selected) 3.dp else 0.dp,
                                MaterialTheme.colorScheme.surface, CircleShape)
                            .clickable { viewModel.onBudgetColor(hex) },
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = viewModel::saveBudget,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ForestGreen,
                    contentColor = Color.White,
                    disabledContainerColor = SurfaceElevated,
                    disabledContentColor = TextMuted,
                ),
                enabled = !form.isLoading,
            ) {
                Text(
                    if (editId == null) "Create Budget" else "Save Changes",
                    fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
                )
            }

            if (editId != null) {
                Button(
                    onClick = { viewModel.deleteBudget(editId) { onBack() } },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DangerRedDim,
                        contentColor = DangerRed,
                    ),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("Delete Budget", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
internal fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelMedium,
        color = TextSecondary, fontWeight = FontWeight.SemiBold)
}

@Composable
internal fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AccentEmerald,
    unfocusedBorderColor = SurfaceBorder,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    cursorColor = AccentEmerald,
    focusedContainerColor = SurfaceCard,
    unfocusedContainerColor = SurfaceCard,
)
