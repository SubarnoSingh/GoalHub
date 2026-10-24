package com.example.goalhub.data.local

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Tiny settings store. Swap for DataStore later if needed — the public surface
 * (StateFlows + setters) is all the UI depends on.
 */
class SettingsStore(context: Context) {

    private val prefs = context.getSharedPreferences("goalhub_settings", Context.MODE_PRIVATE)

    private val _userName = MutableStateFlow(prefs.getString(KEY_USER_NAME, DEFAULT_USER_NAME) ?: DEFAULT_USER_NAME)
    val userName: StateFlow<String> = _userName

    private val _remindersEnabled = MutableStateFlow(prefs.getBoolean(KEY_REMINDERS, true))
    val remindersEnabled: StateFlow<Boolean> = _remindersEnabled

    fun setUserName(name: String) {
        val cleaned = name.trim().ifEmpty { DEFAULT_USER_NAME }
        prefs.edit().putString(KEY_USER_NAME, cleaned).apply()
        _userName.value = cleaned
    }

    fun setRemindersEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REMINDERS, enabled).apply()
        _remindersEnabled.value = enabled
    }

    companion object {
        const val DEFAULT_USER_NAME = "Subarno"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_REMINDERS = "reminders_enabled"
    }
}
