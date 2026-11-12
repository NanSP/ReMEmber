package com.example.remember

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.remember.ui.theme.AppThemeMode
import com.example.remember.ui.theme.AppThemePreference
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThemePreferenceInstrumentedTest {

    @Test
    fun selectedThemePersistsLocally() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val preference = AppThemePreference(context)
        val original = preference.read()

        try {
            preference.write(AppThemeMode.DARK)
            assertEquals(AppThemeMode.DARK, preference.read())

            preference.write(AppThemeMode.LIGHT)
            assertEquals(AppThemeMode.LIGHT, preference.read())
        } finally {
            preference.write(original)
        }
    }
}
