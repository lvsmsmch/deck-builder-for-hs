package com.lvsmsmch.deckbuilder.presentation.paging

import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.domain.entities.Page
import com.lvsmsmch.deckbuilder.presentation.UiText
import com.lvsmsmch.deckbuilder.testCard
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CardPageStateTest {

    private fun card(id: Int) = testCard(id)

    private fun page(items: List<Card>, number: Int = 1, pages: Int = 1, total: Int = items.size) =
        Page(items = items, pageNumber = number, pageCount = pages, totalCount = total)

    @Test
    fun first_page_replaces_items_and_bumps_content_version() {
        val loaded = CardPageState()
            .onLoadStarted(replace = true)
            .onPageLoaded(page(listOf(card(1), card(2)), pages = 2, total = 4), replace = true)

        assertEquals(listOf(1, 2), loaded.cards.map { it.id })
        assertEquals(1L, loaded.contentVersion)
        assertEquals(4, loaded.totalCount)
        assertTrue(loaded.hasMore)
        assertFalse(loaded.isLoadingFirstPage)
    }

    @Test
    fun next_page_appends_and_keeps_content_version() {
        val first = CardPageState().onPageLoaded(page(listOf(card(1)), number = 1, pages = 2, total = 2), replace = true)
        val second = first
            .onLoadStarted(replace = false)
            .onPageLoaded(page(listOf(card(2)), number = 2, pages = 2, total = 2), replace = false)

        assertEquals(listOf(1, 2), second.cards.map { it.id })
        assertEquals(first.contentVersion, second.contentVersion)
        assertFalse(second.hasMore)
        assertFalse(second.canLoadMore)
    }

    @Test
    fun default_hero_avatars_never_reach_the_list() {
        val hero = testCard(id = 7, slug = "HERO_01", name = "Garrosh Hellscream", text = null, typeSlug = "hero")
        val loaded = CardPageState().onPageLoaded(page(listOf(hero, card(1))), replace = true)

        assertEquals(listOf(1), loaded.cards.map { it.id })
    }

    @Test
    fun real_hero_cards_are_kept() {
        val deathKnight = testCard(
            id = 42,
            slug = "ICC_481",
            name = "Bloodreaver Gul'dan",
            text = "Battlecry: Summon all friendly Demons.",
            typeSlug = "hero",
        )
        val loaded = CardPageState().onPageLoaded(page(listOf(deathKnight)), replace = true)

        assertEquals(listOf(42), loaded.cards.map { it.id })
    }

    @Test
    fun failure_clears_loading_flags_and_keeps_previous_cards() {
        val loaded = CardPageState().onPageLoaded(page(listOf(card(1))), replace = true)
        val failed = loaded.onLoadStarted(replace = false).onLoadFailed(IllegalStateException("boom"))

        assertNotNull(failed.error)
        assertFalse(failed.isLoadingFirstPage)
        assertFalse(failed.isLoadingMore)
        assertEquals(listOf(1), failed.cards.map { it.id })
    }

    /** Technical exception text must never surface: it maps to a localized message. */
    @Test
    fun failure_is_reported_as_a_localized_message() {
        val failed = CardPageState().onLoadFailed(IllegalStateException("Truncated varint in stream"))
        val error = failed.error
        assertTrue(error is UiText.Resource)
    }

    @Test
    fun successful_result_clears_a_previous_error() {
        val failed = CardPageState().onLoadFailed(IllegalStateException("boom"))
        val recovered = failed.onResult(Result.Success(page(listOf(card(1)))), replace = true)

        assertNull(recovered.error)
        assertEquals(listOf(1), recovered.cards.map { it.id })
    }

    @Test
    fun initial_load_is_only_reported_while_the_list_is_empty() {
        val loading = CardPageState().onLoadStarted(replace = true)
        assertTrue(loading.isInitialLoad)

        val reloading = CardPageState()
            .onPageLoaded(page(listOf(card(1))), replace = true)
            .onLoadStarted(replace = true)
        assertFalse(reloading.isInitialLoad)
    }

    @Test
    fun load_more_is_blocked_while_a_request_is_in_flight() {
        val state = CardPageState().onPageLoaded(page(listOf(card(1)), pages = 3), replace = true)
        assertTrue(state.canLoadMore)
        assertFalse(state.onLoadStarted(replace = false).canLoadMore)
    }
}
