package com.example.remember.feature.mvp.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MvpNavigationTest {

    @Test
    fun `back from details returns to more`() {
        listOf(
            MvpPage.PROFILE,
            MvpPage.HISTORY,
            MvpPage.SETTINGS,
            MvpPage.PRIVACY
        ).forEach { page ->
            assertEquals(MvpPage.MORE, page.backDestination())
        }
    }

    @Test
    fun `back from secondary roots returns to today`() {
        listOf(
            MvpPage.GOALS,
            MvpPage.AVATAR,
            MvpPage.STORE,
            MvpPage.MORE
        ).forEach { page ->
            assertEquals(MvpPage.TODAY, page.backDestination())
        }
    }

    @Test
    fun `back from today is delegated to Android`() {
        assertNull(MvpPage.TODAY.backDestination())
    }
}
