package com.lvsmsmch.deckbuilder.presentation.ui.screen.settings

import app.cash.turbine.test
import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.domain.entities.Metadata
import com.lvsmsmch.deckbuilder.domain.entities.ThemeMode
import com.lvsmsmch.deckbuilder.domain.repositories.MetadataRepository
import com.lvsmsmch.deckbuilder.domain.usecases.ObservePreferencesUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.RefreshMetadataUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.SetCardLocaleUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.SetCrashReportingEnabledUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.SetThemeUseCase
import com.lvsmsmch.deckbuilder.util.FakePreferencesRepository
import com.lvsmsmch.deckbuilder.util.MainDispatcherRule
import com.lvsmsmch.deckbuilder.util.fakeMetadata
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/**
 * VM is unit-tested against a synchronous fake prefs and a mock metadata repo.
 * The "did the new locale actually flow through resolve()?" integration sits in
 * `LocaleChangeIntegrationTest` — this file just guards VM-level orchestration.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val prefs = FakePreferencesRepository()
    private val metadata: MetadataRepository = mockk(relaxed = true)
    private val current = MutableStateFlow<Metadata?>(null)

    private fun newVm(): SettingsViewModel {
        coEvery { metadata.current } returns current
        coEvery { metadata.loadFromCache(any()) } returns null
        coEvery { metadata.refresh(any(), any()) } coAnswers {
            val meta = fakeMetadata(locale = firstArg() ?: prefs.current().cardLocale, refreshedAtMs = 42L)
            current.value = meta
            Result.Success(meta)
        }
        return SettingsViewModel(
            observePrefs = ObservePreferencesUseCase(prefs),
            metadata = metadata,
            setThemeUseCase = SetThemeUseCase(prefs),
            setCardLocale = SetCardLocaleUseCase(prefs),
            setCrashReporting = SetCrashReportingEnabledUseCase(prefs),
            refreshMetadata = RefreshMetadataUseCase(metadata),
        )
    }

    @Test
    fun `setLocale persists new locale and triggers force refresh`() = runTest(mainRule.dispatcher.scheduler) {
        val vm = newVm()
        val captured = slot<String?>()
        coEvery { metadata.refresh(captureNullable(captured), any()) } returns Result.Success(fakeMetadata())

        vm.setLocale("ru_RU")
        advanceUntilIdle()

        assertEquals("ru_RU", prefs.current().cardLocale)
        coVerify { metadata.refresh(any(), force = true) }
    }

    @Test
    fun `setTheme persists`() = runTest(mainRule.dispatcher.scheduler) {
        val vm = newVm()
        vm.setTheme(ThemeMode.Dark)
        advanceUntilIdle()
        assertEquals(ThemeMode.Dark, prefs.current().theme)
    }

    @Test
    fun `setCrashReportingEnabled persists`() = runTest(mainRule.dispatcher.scheduler) {
        val vm = newVm()
        vm.setCrashReportingEnabled(false)
        advanceUntilIdle()
        assertEquals(false, prefs.current().crashReportingEnabled)
    }

    @Test
    fun `state mirrors prefs after change`() = runTest(mainRule.dispatcher.scheduler) {
        val vm = newVm()
        vm.state.test {
            assertEquals("en_US", awaitItem().prefs.cardLocale)
            vm.setLocale("de_DE")
            // Prefs flow drives state.prefs.cardLocale; tolerate intermediate emissions.
            var seen: String? = null
            while (seen != "de_DE") seen = awaitItem().prefs.cardLocale
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refreshMetadataNow toggles isRefreshing flag and posts message`() = runTest(mainRule.dispatcher.scheduler) {
        val vm = newVm()
        vm.refreshMetadataNow()
        advanceUntilIdle()
        val s = vm.state.value
        assertEquals(false, s.isRefreshingMetadata)
        assertNotNull(s.message)
    }

    @Test
    fun `refreshMetadataNow surfaces failure as a message`() = runTest(mainRule.dispatcher.scheduler) {
        coEvery { metadata.refresh(any(), any()) } returns Result.Error(IllegalStateException("offline"))
        val vm = newVm()
        vm.refreshMetadataNow()
        advanceUntilIdle()
        val s = vm.state.value
        assertEquals(false, s.isRefreshingMetadata)
        assertNotNull(s.message)
    }

    @Test
    fun `metadataRefreshedAtMs reflects metadata current emissions`() = runTest(mainRule.dispatcher.scheduler) {
        val vm = newVm()
        current.value = fakeMetadata(refreshedAtMs = 4242L)
        advanceUntilIdle()
        assertEquals(4242L, vm.state.value.metadataRefreshedAtMs)
    }

    @Test
    fun `dismissMessage clears state`() = runTest(mainRule.dispatcher.scheduler) {
        val vm = newVm()
        vm.refreshMetadataNow()
        advanceUntilIdle()
        assertNotNull(vm.state.value.message)
        vm.dismissMessage()
        assertNull(vm.state.value.message)
    }
}
