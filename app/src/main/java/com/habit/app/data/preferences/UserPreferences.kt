package com.habit.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.habit.app.domain.WidgetDisplayMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "habit_prefs")

data class UserPreferencesState(
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0,
    val remindersEnabled: Boolean = true,
    val widgetMode: WidgetDisplayMode = WidgetDisplayMode.Pending,
    val pinHash: String? = null,
    val currencySymbol: String = "₹",
)

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore = context.dataStore

    private object Keys {
        val reminderHour = intPreferencesKey("reminder_hour")
        val reminderMinute = intPreferencesKey("reminder_minute")
        val remindersEnabled = booleanPreferencesKey("reminders_enabled")
        val widgetMode = stringPreferencesKey("widget_mode")
        val pinHash = stringPreferencesKey("pin_hash")
        val currencySymbol = stringPreferencesKey("currency_symbol")
    }

    val flow: Flow<UserPreferencesState> = dataStore.data.map { p ->
        UserPreferencesState(
            reminderHour = p[Keys.reminderHour] ?: 9,
            reminderMinute = p[Keys.reminderMinute] ?: 0,
            remindersEnabled = p[Keys.remindersEnabled] ?: true,
            widgetMode = WidgetDisplayMode.fromRaw(p[Keys.widgetMode]),
            pinHash = p[Keys.pinHash],
            currencySymbol = p[Keys.currencySymbol] ?: "₹",
        )
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        dataStore.edit {
            it[Keys.reminderHour] = hour
            it[Keys.reminderMinute] = minute
        }
    }

    suspend fun setRemindersEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.remindersEnabled] = enabled }
    }

    suspend fun setWidgetMode(mode: WidgetDisplayMode) {
        dataStore.edit { it[Keys.widgetMode] = mode.raw }
    }

    suspend fun setPin(pin: String) {
        val hash = hashPin(pin)
        dataStore.edit { it[Keys.pinHash] = hash }
    }

    suspend fun clearPin() {
        dataStore.edit { it.remove(Keys.pinHash) }
    }

    fun verifyPin(pin: String, storedHash: String?): Boolean {
        return storedHash != null && hashPin(pin) == storedHash
    }

    suspend fun setCurrencySymbol(symbol: String) {
        dataStore.edit { it[Keys.currencySymbol] = symbol }
    }

    private fun hashPin(pin: String): String {
        val bytes = pin.toByteArray()
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }
}
