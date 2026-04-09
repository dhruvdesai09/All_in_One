package com.habit.app.data.security

import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Handles PBKDF2 key derivation + AES-256-GCM encryption for portable vault backups.
 * All key material lives in the passphrase — no hardware dependency, so backups are
 * portable to any device.
 */
object BackupCrypto {

    private const val PBKDF2_ALGO    = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS     = 310_000  // OWASP 2023 recommended minimum
    private const val KEY_BITS       = 256
    private const val SALT_BYTES     = 32
    private const val GCM_TAG_BITS   = 128
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val BACKUP_VERSION = 1

    data class BackupEntry(
        val name: String,
        val username: String,
        val password: String,
    )

    /**
     * Encrypts [entries] with [passphrase] and returns a self-contained JSON string
     * that contains the salt, IV, and ciphertext – safe to store anywhere.
     */
    fun encrypt(entries: List<BackupEntry>, passphrase: String): String {
        val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
        val key  = deriveKey(passphrase, salt)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv        = cipher.iv
        val plaintext = buildPayloadJson(entries).toByteArray(Charsets.UTF_8)
        val encrypted = cipher.doFinal(plaintext)

        return JSONObject().apply {
            put("version", BACKUP_VERSION)
            put("salt",    Base64.encodeToString(salt,      Base64.NO_WRAP))
            put("iv",      Base64.encodeToString(iv,        Base64.NO_WRAP))
            put("data",    Base64.encodeToString(encrypted, Base64.NO_WRAP))
        }.toString()
    }

    /**
     * Decrypts a backup string produced by [encrypt].
     * Throws if the passphrase is wrong (GCM tag verification fails) or the file is corrupt.
     */
    fun decrypt(fileContent: String, passphrase: String): List<BackupEntry> {
        val json    = JSONObject(fileContent)
        val version = json.getInt("version")
        require(version == BACKUP_VERSION) { "Unsupported backup version: $version" }

        val salt      = Base64.decode(json.getString("salt"), Base64.NO_WRAP)
        val iv        = Base64.decode(json.getString("iv"),   Base64.NO_WRAP)
        val encrypted = Base64.decode(json.getString("data"), Base64.NO_WRAP)

        val key    = deriveKey(passphrase, salt)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        val plaintext = cipher.doFinal(encrypted) // throws AEADBadTagException on wrong passphrase

        return parsePayloadJson(String(plaintext, Charsets.UTF_8))
    }

    private fun deriveKey(passphrase: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance(PBKDF2_ALGO)
        val spec    = PBEKeySpec(passphrase.toCharArray(), salt, ITERATIONS, KEY_BITS)
        return SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
    }

    private fun buildPayloadJson(entries: List<BackupEntry>): String {
        val array = JSONArray()
        entries.forEach { e ->
            array.put(JSONObject().apply {
                put("name",     e.name)
                put("username", e.username)
                put("password", e.password)
            })
        }
        return JSONObject().put("entries", array).toString()
    }

    private fun parsePayloadJson(json: String): List<BackupEntry> {
        val root  = JSONObject(json)
        val array = root.getJSONArray("entries")
        return (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            BackupEntry(
                name     = obj.getString("name"),
                username = obj.optString("username", ""),
                password = obj.getString("password"),
            )
        }
    }
}
