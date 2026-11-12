package com.example.remember.ui.theme

import android.content.Context

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    companion object {
        fun fromStoredValue(value: String?): AppThemeMode =
            entries.firstOrNull { it.name == value } ?: SYSTEM
    }
}

class AppThemePreference(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    fun read(): AppThemeMode = AppThemeMode.fromStoredValue(
        preferences.getString(KEY_THEME_MODE, null)
    )

    fun write(mode: AppThemeMode) {
        preferences.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "remember_appearance"
        const val KEY_THEME_MODE = "theme_mode"
    }
}
