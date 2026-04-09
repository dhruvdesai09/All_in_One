package com.habit.app.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.habit.app.data.security.BackupCrypto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultBackupManager @Inject constructor(
    private val passwordRepo: PasswordRepository,
    @ApplicationContext private val context: Context,
) {
    /**
     * Decrypts all vault entries with the device KeyStore key, re-encrypts them
     * with the chosen [passphrase] (PBKDF2 + AES-GCM), writes a .dvault file to
     * the app cache dir, and returns a shareable URI via FileProvider.
     */
    suspend fun export(passphrase: String): Uri = withContext(Dispatchers.IO) {
        val items = passwordRepo.getAllDecrypted()
        val entries = items.map { BackupCrypto.BackupEntry(it.name, it.username, it.password) }
        val encrypted = BackupCrypto.encrypt(entries, passphrase)

        val date = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(context.cacheDir, "dailybase_backup_$date.dvault")
        file.writeText(encrypted)

        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
    }

    /**
     * Reads a .dvault URI, decrypts with [passphrase], then re-encrypts each entry
     * with this device's KeyStore key before persisting to Room.
     * Returns the number of entries imported.
     * Throws [javax.crypto.AEADBadTagException] if the passphrase is wrong.
     */
    suspend fun import(uri: Uri, passphrase: String): Int = withContext(Dispatchers.IO) {
        val content = context.contentResolver
            .openInputStream(uri)
            ?.bufferedReader()
            ?.readText()
            ?: error("Cannot read backup file")

        val entries = BackupCrypto.decrypt(content, passphrase)
        entries.forEach { passwordRepo.insert(it.name, it.username, it.password) }
        entries.size
    }
}
