package com.example.remember.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class AppThemePreferenceTest {

    @Test
    fun `stored theme modes are restored`() {
        AppThemeMode.entries.forEach { mode ->
            assertEquals(mode, AppThemeMode.fromStoredValue(mode.name))
        }
    }

    @Test
    fun `missing or invalid value falls back to system`() {
        assertEquals(AppThemeMode.SYSTEM, AppThemeMode.fromStoredValue(null))
        assertEquals(AppThemeMode.SYSTEM, AppThemeMode.fromStoredValue("UNKNOWN"))
    }
}
