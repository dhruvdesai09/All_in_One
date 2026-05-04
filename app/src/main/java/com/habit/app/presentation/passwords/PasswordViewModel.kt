package com.habit.app.presentation.passwords

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habit.app.data.local.PasswordEntity
import com.habit.app.data.preferences.UserPreferences
import com.habit.app.data.repository.PasswordItem
import com.habit.app.data.repository.PasswordRepository
import com.habit.app.data.repository.VaultBackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PinScreenMode { CREATE, VERIFY }

enum class ChangePinStep { VERIFY_OLD, SET_NEW, CONFIRM_NEW }

data class ChangePinState(
    val isActive: Boolean = false,
    val step: ChangePinStep = ChangePinStep.VERIFY_OLD,
    val pinInput: String = "",
    val newPin: String = "",
    val error: String? = null,
    val success: Boolean = false,
)

data class PasswordVaultState(
    val pinHash: String? = null,
    val isUnlocked: Boolean = false,
    val pinError: String? = null,
    val pinInput: String = "",
)

data class AddEditState(
    val id: Long = 0,
    val name: String = "",
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val nameError: String? = null,
)

data class DetailState(
    val item: PasswordItem? = null,
    val isLoading: Boolean = true,
)

enum class BackupStatus { IDLE, LOADING, SUCCESS, ERROR }

data class BackupState(
    val status: BackupStatus = BackupStatus.IDLE,
    val exportUri: Uri? = null,
    val importedCount: Int = 0,
    val error: String? = null,
)

@HiltViewModel
class PasswordViewModel @Inject constructor(
    private val repo: PasswordRepository,
    private val prefs: UserPreferences,
    private val backupManager: VaultBackupManager,
) : ViewModel() {

    // --- Vault / PIN state ---
    private val _vaultState = MutableStateFlow(PasswordVaultState())
    val vaultState: StateFlow<PasswordVaultState> = _vaultState.asStateFlow()

    // --- Change PIN state ---
    private val _changePinState = MutableStateFlow(ChangePinState())
    val changePinState: StateFlow<ChangePinState> = _changePinState.asStateFlow()

    // --- Password list ---
    val passwords: StateFlow<List<PasswordEntity>> = repo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // --- Add/Edit state ---
    private val _addEditState = MutableStateFlow(AddEditState())
    val addEditState: StateFlow<AddEditState> = _addEditState.asStateFlow()

    // --- Detail state ---
    private val _detailState = MutableStateFlow(DetailState())
    val detailState: StateFlow<DetailState> = _detailState.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.flow.collect { p ->
                _vaultState.update { it.copy(pinHash = p.pinHash) }
            }
        }
    }

    // ---- PIN actions ----

    fun onPinInputChange(value: String) {
        if (value.length > 6) return
        _vaultState.update { it.copy(pinInput = value, pinError = null) }
    }

    fun submitPin() {
        val state = _vaultState.value
        if (state.pinHash == null) {
            // CREATE mode: set new PIN
            if (state.pinInput.length < 4) {
                _vaultState.update { it.copy(pinError = "PIN must be at least 4 digits") }
                return
            }
            viewModelScope.launch {
                prefs.setPin(state.pinInput)
                _vaultState.update { it.copy(isUnlocked = true, pinInput = "", pinError = null) }
            }
        } else {
            // VERIFY mode
            if (prefs.verifyPin(state.pinInput, state.pinHash)) {
                _vaultState.update { it.copy(isUnlocked = true, pinInput = "", pinError = null) }
            } else {
                _vaultState.update { it.copy(pinError = "Incorrect PIN", pinInput = "") }
            }
        }
    }

    fun lockVault() {
        _vaultState.update { it.copy(isUnlocked = false, pinInput = "", pinError = null) }
        // Also cancel any in-progress change PIN flow
        _changePinState.value = ChangePinState()
    }

    // ---- Change PIN actions ----

    fun startChangePinFlow() {
        _changePinState.value = ChangePinState(isActive = true, step = ChangePinStep.VERIFY_OLD)
    }

    fun cancelChangePinFlow() {
        _changePinState.value = ChangePinState()
    }

    fun onChangePinInputChange(value: String) {
        if (value.length > 6) return
        _changePinState.update { it.copy(pinInput = value, error = null) }
    }

    fun submitChangePinStep() {
        val cs = _changePinState.value
        val currentHash = _vaultState.value.pinHash
        when (cs.step) {
            ChangePinStep.VERIFY_OLD -> {
                if (!prefs.verifyPin(cs.pinInput, currentHash)) {
                    _changePinState.update { it.copy(error = "Incorrect current PIN", pinInput = "") }
                } else {
                    _changePinState.update {
                        it.copy(step = ChangePinStep.SET_NEW, pinInput = "", error = null)
                    }
                }
            }
            ChangePinStep.SET_NEW -> {
                if (cs.pinInput.length < 4) {
                    _changePinState.update { it.copy(error = "New PIN must be at least 4 digits") }
                } else {
                    _changePinState.update {
                        it.copy(step = ChangePinStep.CONFIRM_NEW, newPin = cs.pinInput, pinInput = "", error = null)
                    }
                }
            }
            ChangePinStep.CONFIRM_NEW -> {
                if (cs.pinInput != cs.newPin) {
                    _changePinState.update { it.copy(error = "PINs do not match", pinInput = "") }
                } else {
                    viewModelScope.launch {
                        prefs.setPin(cs.newPin)
                        // pinHash in vaultState will auto-update via the init collector
                        _changePinState.update { it.copy(success = true) }
                    }
                }
            }
        }
    }

    fun resetChangePinSuccess() {
        _changePinState.value = ChangePinState()
    }

    // ---- Add / Edit actions ----

    fun loadForEdit(id: Long?) {
        _addEditState.value = AddEditState(isLoading = id != null)
        if (id == null) return
        viewModelScope.launch {
            val item = repo.getDecrypted(id)
            if (item != null) {
                _addEditState.value = AddEditState(
                    id = item.id,
                    name = item.name,
                    username = item.username,
                    password = item.password,
                )
            }
        }
    }

    fun onNameChange(v: String) = _addEditState.update { it.copy(name = v, nameError = null) }
    fun onUsernameChange(v: String) = _addEditState.update { it.copy(username = v) }
    fun onPasswordChange(v: String) = _addEditState.update { it.copy(password = v) }

    fun saveEntry() {
        val s = _addEditState.value
        if (s.name.isBlank()) {
            _addEditState.update { it.copy(nameError = "Name is required") }
            return
        }
        _addEditState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            if (s.id == 0L) {
                repo.insert(s.name.trim(), s.username.trim(), s.password)
            } else {
                repo.update(s.id, s.name.trim(), s.username.trim(), s.password)
            }
            _addEditState.update { it.copy(isLoading = false, isSaved = true) }
        }
    }

    fun resetAddEdit() {
        _addEditState.value = AddEditState()
    }

    // ---- Detail actions ----

    fun loadDetail(id: Long) {
        _detailState.value = DetailState(isLoading = true)
        viewModelScope.launch {
            _detailState.value = DetailState(item = repo.getDecrypted(id), isLoading = false)
        }
    }

    fun deleteEntry(id: Long, onDone: () -> Unit) {
        viewModelScope.launch {
            repo.delete(id)
            onDone()
        }
    }

    // ---- Backup actions ----

    private val _backupState = MutableStateFlow(BackupState())
    val backupState: StateFlow<BackupState> = _backupState.asStateFlow()

    fun exportVault(passphrase: String) {
        _backupState.update { it.copy(status = BackupStatus.LOADING, error = null) }
        viewModelScope.launch {
            try {
                val uri = backupManager.export(passphrase)
                _backupState.update { it.copy(status = BackupStatus.SUCCESS, exportUri = uri) }
            } catch (e: Exception) {
                _backupState.update { it.copy(status = BackupStatus.ERROR, error = e.message) }
            }
        }
    }

    fun importVault(uri: Uri, passphrase: String) {
        _backupState.update { it.copy(status = BackupStatus.LOADING, error = null) }
        viewModelScope.launch {
            try {
                val count = backupManager.import(uri, passphrase)
                _backupState.update { it.copy(status = BackupStatus.SUCCESS, importedCount = count) }
            } catch (e: javax.crypto.AEADBadTagException) {
                _backupState.update { it.copy(status = BackupStatus.ERROR, error = "Wrong passphrase") }
            } catch (e: Exception) {
                _backupState.update { it.copy(status = BackupStatus.ERROR, error = "Import failed: ${e.message}") }
            }
        }
    }

    fun resetBackupState() {
        _backupState.value = BackupState()
    }
}
