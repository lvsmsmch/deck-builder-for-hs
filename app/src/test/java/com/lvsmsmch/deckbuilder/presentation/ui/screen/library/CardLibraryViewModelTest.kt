package com.lvsmsmch.deckbuilder.presentation.ui.screen.library

import app.cash.turbine.test
import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.domain.entities.AppPreferences
import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.domain.entities.CardFilters
import com.lvsmsmch.deckbuilder.domain.entities.Metadata
import com.lvsmsmch.deckbuilder.domain.entities.Page
import com.lvsmsmch.deckbuilder.domain.repositories.MetadataRepository
import com.lvsmsmch.deckbuilder.domain.repositories.PreferencesRepository
import com.lvsmsmch.deckbuilder.domain.usecases.AcknowledgeNewSetUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.SearchCardsUseCase
import com.lvsmsmch.deckbuilder.util.MainDispatcherRule
import com.lvsmsmch.deckbuilder.util.fakeMetadata
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CardLibraryViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val metadataRepo: MetadataRepository = mockk(relaxed = true)
    private val prefs: PreferencesRepository = mockk()
    private val searchCardsUc: SearchCardsUseCase = mockk()
    private val ackUc: AcknowledgeNewSetUseCase = mockk(relaxed = true)
    private val current = MutableStateFlow<Metadata?>(null)

    private fun newVm(
        initialMeta: Metadata? = null,
        searchResult: Result<Page<Card>> = Result.Success(Page(emptyList(), 1, 1, 0)),
    ): CardLibraryViewModel {
        current.value = initialMeta
        coEvery { metadataRepo.current } returns current
        coEvery { metadataRepo.loadFromCache(any()) } returns initialMeta
        coEvery { prefs.preferences } returns flowOf(AppPreferences())
        coEvery { prefs.current() } returns AppPreferences()
        coEvery { searchCardsUc(any(), any(), any(), any()) } returns searchResult
        return CardLibraryViewModel(
            searchCards = searchCardsUc,
            metadata = metadataRepo,
            prefs = prefs,
            acknowledgeNewSet = ackUc,
        )
    }

    @Test
    fun `loads first page once metadata is available from cache`() = runTest(mainRule.dispatcher.scheduler) {
        val meta = fakeMetadata()
        newVm(initialMeta = meta)
        coVerify(timeout = 1000) { searchCardsUc(any(), 1, any(), any()) }
    }

    @Test
    fun `does not load until metadata arrives, then loads`() = runTest(mainRule.dispatcher.scheduler) {
        // No metadata up front, no cache.
        coEvery { metadataRepo.loadFromCache(any()) } returns null
        val vm = newVm(initialMeta = null)
        // Nothing fetched yet — the VM is parked waiting for metadata.
        coVerify(exactly = 0) { searchCardsUc(any(), any(), any(), any()) }
        current.value = fakeMetadata()
        coVerify(timeout = 1000) { searchCardsUc(any(), 1, any(), any()) }
    }

    @Test
    fun `state mirrors metadata emissions for filter sheet`() = runTest(mainRule.dispatcher.scheduler) {
        val vm = newVm(initialMeta = fakeMetadata())
        assertEquals(3, vm.state.value.metadata?.classes?.size)
    }

    @Test
    fun `locale change in metadata triggers refetch`() = runTest(mainRule.dispatcher.scheduler) {
        val vm = newVm(initialMeta = fakeMetadata(locale = "en_US"))
        // first load triggered above
        current.value = fakeMetadata(locale = "ru_RU", refreshedAtMs = 9_999L)
        // Two loads now: initial + locale change.
        coVerify(atLeast = 2, timeout = 1000) {
            searchCardsUc(any(), 1, any(), any())
        }
    }

    @Test
    fun `error result surfaces errorMessage in state`() = runTest(mainRule.dispatcher.scheduler) {
        val vm = newVm(
            initialMeta = fakeMetadata(),
            searchResult = Result.Error(IllegalStateException("offline")),
        )
        // Wait briefly for the search to complete.
        coVerify(timeout = 1000) { searchCardsUc(any(), any(), any(), any()) }
        assertEquals("offline", vm.state.value.errorMessage)
    }

    @Test
    fun `applyFilters with same filters does not reload`() = runTest(mainRule.dispatcher.scheduler) {
        val vm = newVm(initialMeta = fakeMetadata())
        coVerify(timeout = 1000) { searchCardsUc(any(), any(), any(), any()) }
        vm.applyFilters(CardFilters())
        // Still only the initial load.
        coVerify(exactly = 1) { searchCardsUc(any(), any(), any(), any()) }
    }

    @Test
    fun `toggleClass updates state and reloads`() = runTest(mainRule.dispatcher.scheduler) {
        val passed = slot<CardFilters>()
        coEvery { searchCardsUc(capture(passed), any(), any(), any()) } returns
            Result.Success(Page(emptyList(), 1, 1, 0))
        val vm = newVm(initialMeta = fakeMetadata())
        vm.toggleClass("mage")
        assertTrue("mage" in vm.state.value.filters.classes)
    }
}
