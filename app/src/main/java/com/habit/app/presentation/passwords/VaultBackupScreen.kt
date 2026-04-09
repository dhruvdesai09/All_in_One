package com.habit.app.presentation.passwords

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habit.app.presentation.theme.AccentEmerald
import com.habit.app.presentation.theme.DangerRed
import com.habit.app.presentation.theme.ForestGreen
import com.habit.app.presentation.theme.ForestGreenLight
import com.habit.app.presentation.theme.SurfaceBorder
import com.habit.app.presentation.theme.SurfaceCard
import com.habit.app.presentation.theme.SurfaceElevated
import com.habit.app.presentation.theme.TextMuted
import com.habit.app.presentation.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultBackupScreen(
    onBack: () -> Unit,
    viewModel: PasswordViewModel = hiltViewModel(),
) {
    val backupState by viewModel.backupState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var exportPassphrase by remember { mutableStateOf("") }
    var exportPassphraseVisible by remember { mutableStateOf(false) }

    var importPassphrase by remember { mutableStateOf("") }
    var importPassphraseVisible by remember { mutableStateOf(false) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }

    // File picker for import
    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            selectedFileUri = it
            selectedFileName = it.lastPathSegment?.substringAfterLast("/") ?: "backup.dvault"
        }
    }

    // Handle export success — trigger share sheet
    LaunchedEffect(backupState.exportUri) {
        val uri = backupState.exportUri ?: return@LaunchedEffect
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Save DailyBase Backup"))
        viewModel.resetBackupState()
    }

    // Handle import success
    LaunchedEffect(backupState.importedCount, backupState.status) {
        if (backupState.status == BackupStatus.SUCCESS && backupState.importedCount > 0) {
            scope.launch {
                snackbarHostState.showSnackbar("✅ ${backupState.importedCount} passwords imported")
            }
            importPassphrase = ""
            selectedFileUri = null
            selectedFileName = ""
            viewModel.resetBackupState()
        }
    }

    // Handle errors
    LaunchedEffect(backupState.error) {
        backupState.error?.let {
            scope.launch { snackbarHostState.showSnackbar("❌ $it") }
            viewModel.resetBackupState()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Backup & Restore",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {

            // ── Info banner ──────────────────────────────────────────
            Surface(
                color = AccentEmerald.copy(alpha = 0.08f),
                shape = RoundedCornerShape(14.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        "🔒 How backup works",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentEmerald,
                    )
                    Text(
                        "Your passwords are decrypted, then re-encrypted with your chosen passphrase using AES-256. " +
                            "The backup file can be restored on any device — just remember the passphrase.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }

            // ── Export section ───────────────────────────────────────
            SectionCard(
                icon = "📤",
                title = "Export Vault",
                subtitle = "Save an encrypted backup to share or store",
            ) {
                BackupPassphraseField(
                    value = exportPassphrase,
                    onValueChange = { exportPassphrase = it },
                    visible = exportPassphraseVisible,
                    onToggleVisibility = { exportPassphraseVisible = !exportPassphraseVisible },
                    placeholder = "Choose a backup passphrase",
                )

                Spacer(Modifier.height(4.dp))

                Button(
                    onClick = {
                        if (exportPassphrase.length < 4) {
                            scope.launch { snackbarHostState.showSnackbar("Passphrase must be at least 4 characters") }
                            return@Button
                        }
                        viewModel.exportVault(exportPassphrase)
                    },
                    enabled = backupState.status != BackupStatus.LOADING,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ForestGreen,
                        contentColor = Color.White,
                        disabledContainerColor = SurfaceElevated,
                    ),
                ) {
                    if (backupState.status == BackupStatus.LOADING && backupState.exportUri == null) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = AccentEmerald,
                        )
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Filled.FileUpload, null, modifier = Modifier.size(18.dp))
                            Text("Export & Share", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            HorizontalDivider(color = SurfaceBorder)

            // ── Import section ───────────────────────────────────────
            SectionCard(
                icon = "📥",
                title = "Import Vault",
                subtitle = "Restore passwords from a .dvault backup file",
            ) {
                // File picker button
                Surface(
                    color = SurfaceCard,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { filePicker.launch(arrayOf("application/octet-stream", "*/*")) },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ForestGreenLight.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Filled.FileDownload,
                                contentDescription = null,
                                tint = ForestGreenLight,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (selectedFileName.isNotEmpty()) selectedFileName else "No file selected",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (selectedFileName.isNotEmpty())
                                    MaterialTheme.colorScheme.onSurface else TextMuted,
                            )
                            Text(
                                text = "Tap to choose .dvault file",
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentEmerald,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                BackupPassphraseField(
                    value = importPassphrase,
                    onValueChange = { importPassphrase = it },
                    visible = importPassphraseVisible,
                    onToggleVisibility = { importPassphraseVisible = !importPassphraseVisible },
                    placeholder = "Enter backup passphrase",
                )

                Spacer(Modifier.height(4.dp))

                Button(
                    onClick = {
                        val uri = selectedFileUri
                        if (uri == null) {
                            scope.launch { snackbarHostState.showSnackbar("Please select a .dvault file first") }
                            return@Button
                        }
                        if (importPassphrase.isEmpty()) {
                            scope.launch { snackbarHostState.showSnackbar("Enter the backup passphrase") }
                            return@Button
                        }
                        viewModel.importVault(uri, importPassphrase)
                    },
                    enabled = backupState.status != BackupStatus.LOADING,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ForestGreenLight,
                        contentColor = Color.White,
                        disabledContainerColor = SurfaceElevated,
                    ),
                ) {
                    if (backupState.status == BackupStatus.LOADING && backupState.importedCount == 0) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White,
                        )
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Filled.FileDownload, null, modifier = Modifier.size(18.dp))
                            Text("Import Passwords", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "⚠️ If the passphrase is wrong, import will fail. Passwords are never stored in plain text.",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SectionCard(
    icon: String,
    title: String,
    subtitle: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(icon, fontSize = 22.sp)
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                )
            }
        }
        content()
    }
}

@Composable
private fun BackupPassphraseField(
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onToggleVisibility: () -> Unit,
    placeholder: String,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = TextMuted, fontSize = 14.sp) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    imageVector = if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = null,
                    tint = TextMuted,
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = SurfaceCard,
            unfocusedContainerColor = SurfaceCard,
            focusedBorderColor = AccentEmerald,
            unfocusedBorderColor = SurfaceBorder,
            cursorColor = AccentEmerald,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        ),
    )
}
