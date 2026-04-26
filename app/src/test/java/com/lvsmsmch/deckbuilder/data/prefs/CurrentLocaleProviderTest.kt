package com.lvsmsmch.deckbuilder.data.prefs

import com.lvsmsmch.deckbuilder.domain.entities.AppPreferences
import com.lvsmsmch.deckbuilder.domain.repositories.PreferencesRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CurrentLocaleProviderTest {

    private val prefs: PreferencesRepository = mockk(relaxed = true) {
        coEvery { current() } returns AppPreferences(cardLocale = "ru_RU")
        every { preferences } returns flowOf(AppPreferences(cardLocale = "ru_RU"))
    }

    @Test
    fun `resolve uses explicit value when provided`() = runTest {
        val provider = CurrentLocaleProvider(prefs)
        assertEquals("fr_FR", provider.resolve("fr_FR"))
    }

    @Test
    fun `resolve falls back to prefs locale when null`() = runTest {
        val provider = CurrentLocaleProvider(prefs)
        assertEquals("ru_RU", provider.resolve(null))
    }

    @Test
    fun `resolve with no arg uses default null overload`() = runTest {
        val provider = CurrentLocaleProvider(prefs)
        assertEquals("ru_RU", provider.resolve())
    }
}
