package com.lvsmsmch.deckbuilder.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.cash.turbine.test
import com.lvsmsmch.deckbuilder.domain.entities.AppPreferences
import com.lvsmsmch.deckbuilder.domain.entities.ThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

class PreferencesRepositoryImplTest {

    private lateinit var tmpFile: File
    private lateinit var scope: CoroutineScope
    private lateinit var store: DataStore<Preferences>
    private lateinit var repo: PreferencesRepositoryImpl

    @Before
    fun setUp() {
        tmpFile = Files.createTempFile("prefs", ".preferences_pb").toFile().also { it.delete() }
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        store = PreferenceDataStoreFactory.create(scope = scope, produceFile = { tmpFile })
        repo = PreferencesRepositoryImpl(store)
    }

    @After
    fun tearDown() {
        scope.cancel()
        tmpFile.delete()
    }

    @Test
    fun `defaults match AppPreferences()`() = runTest {
        assertEquals(AppPreferences(), repo.current())
    }

    @Test
    fun `setCardLocale immediately reflected by current()`() = runTest {
        repo.setCardLocale("ru_RU")
        // Critical for SettingsViewModel.setLocale → forceMetadataRefresh:
        // current() must see the new value before refresh resolves locale.
        assertEquals("ru_RU", repo.current().cardLocale)
    }

    @Test
    fun `setTheme persists across reads`() = runTest {
        repo.setTheme(ThemeMode.Dark)
        assertEquals(ThemeMode.Dark, repo.current().theme)
        repo.setTheme(ThemeMode.Light)
        assertEquals(ThemeMode.Light, repo.current().theme)
    }

    @Test
    fun `setCrashReportingEnabled toggles boolean`() = runTest {
        assertTrue(repo.current().crashReportingEnabled)
        repo.setCrashReportingEnabled(false)
        assertEquals(false, repo.current().crashReportingEnabled)
    }

    @Test
    fun `setLastSeenSetSlug null clears the value`() = runTest {
        repo.setLastSeenSetSlug("rastakhan")
        assertEquals("rastakhan", repo.current().lastSeenSetSlug)
        repo.setLastSeenSetSlug(null)
        assertNull(repo.current().lastSeenSetSlug)
    }

    @Test
    fun `preferences flow emits updates`() = runTest {
        repo.preferences.test {
            assertEquals(AppPreferences(), awaitItem())
            repo.setCardLocale("de_DE")
            assertEquals("de_DE", awaitItem().cardLocale)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `unknown theme string falls back to System`() = runTest {
        // Inject a bad value to verify the safe fallback in toDomain().
        store.updateData {
            it.toMutablePreferences().apply {
                this[androidx.datastore.preferences.core.stringPreferencesKey("theme")] = "Holographic"
            }
        }
        assertEquals(ThemeMode.System, repo.current().theme)
    }
}
