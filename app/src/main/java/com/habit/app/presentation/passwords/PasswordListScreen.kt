package com.habit.app.presentation.passwords

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habit.app.data.local.PasswordEntity
import com.habit.app.presentation.theme.NexoraGold
import com.habit.app.presentation.theme.NexoraGoldDim
import com.habit.app.presentation.theme.SurfaceBorder
import com.habit.app.presentation.theme.SurfaceElevated
import com.habit.app.presentation.theme.TextMuted

@Composable
fun PasswordListScreen(
    onAdd: () -> Unit,
    onItemClick: (Long) -> Unit,
    onLocked: () -> Unit = {},
    onBackup: () -> Unit,
    viewModel: PasswordViewModel = hiltViewModel(),
) {
    val passwords by viewModel.passwords.collectAsStateWithLifecycle()
    val changePinState by viewModel.changePinState.collectAsStateWithLifecycle()
    var showMenu by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.lockVault()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val vaultState by viewModel.vaultState.collectAsStateWithLifecycle()
    var wasUnlocked by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(vaultState.isUnlocked) {
        if (vaultState.isUnlocked) {
            wasUnlocked = true
        } else if (wasUnlocked) {
            onLocked()
        }
    }

    if (changePinState.isActive) {
        ChangePinDialog(
            state = changePinState,
            onInputChange = viewModel::onChangePinInputChange,
            onSubmit = viewModel::submitChangePinStep,
            onDismiss = viewModel::cancelChangePinFlow,
            onSuccessDismiss = viewModel::resetChangePinSuccess,
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.linearGradient(listOf(NexoraGold, NexoraGoldDim)))
                    .clickable(onClick = onAdd),
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
                        text = "Vault",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceElevated)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                                .clickable(onClick = onBackup),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("↺", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Box {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceElevated)
                                    .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                                    .clickable { showMenu = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🔒", fontSize = 16.sp)
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Change PIN") },
                                    onClick = {
                                        showMenu = false
                                        viewModel.startChangePinFlow()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Lock Vault") },
                                    onClick = {
                                        showMenu = false
                                        viewModel.lockVault()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Search Bar Mock
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceElevated)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔍", fontSize = 16.sp, modifier = Modifier.alpha(0.4f))
                    Spacer(Modifier.width(10.dp))
                    Text("Search passwords...", fontSize = 13.sp, color = TextMuted)
                }
            }

            item { Spacer(Modifier.height(8.dp)) }

            item {
                Text(
                    text = "ALL ENTRIES",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            if (passwords.isEmpty()) {
                item {
                    PasswordEmptyState(modifier = Modifier.padding(top = 40.dp))
                }
            } else {
                items(passwords, key = { it.id }) { entity ->
                    PasswordCard(entity = entity, onClick = { onItemClick(entity.id) })
                }
            }
            
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun PasswordCard(entity: PasswordEntity, onClick: () -> Unit) {
    Surface(
        color = SurfaceElevated,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Avatar letter
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(NexoraGold.copy(alpha=0.25f), NexoraGoldDim.copy(alpha=0.2f))))
                    .border(1.dp, NexoraGold.copy(alpha=0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = entity.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    fontFamily = MaterialTheme.typography.headlineLarge.fontFamily,
                    fontSize = 16.sp,
                    color = NexoraGold,
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entity.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "••••••••••",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
            }

            Text(
                text = "🗝",
                fontSize = 18.sp,
                modifier = Modifier.alpha(0.4f)
            )
        }
    }
}

@Composable
private fun PasswordEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("🔐", fontSize = 48.sp)
        Text(
            text = "No passwords saved",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Tap + to securely store\nyour first password",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            textAlign = TextAlign.Center,
        )
    }
}
