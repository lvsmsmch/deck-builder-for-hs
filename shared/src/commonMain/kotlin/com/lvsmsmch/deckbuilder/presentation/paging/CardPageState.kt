package com.lvsmsmch.deckbuilder.presentation.paging

import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.domain.entities.CardFilters
import com.lvsmsmch.deckbuilder.domain.entities.Page
import com.lvsmsmch.deckbuilder.domain.entities.isDefaultHeroAvatar
import com.lvsmsmch.deckbuilder.presentation.UiText
import com.lvsmsmch.deckbuilder.presentation.toUiText

/**
 * One paged card list: filters, loaded items, paging cursor and load state.
 * Shared by the card library and the deck-builder pool so both behave the
 * same way (identical loading flags, error handling and page merging).
 *
 * [contentVersion] increments whenever the list is replaced, which the UI uses
 * to scroll back to the top after a filter change.
 */
data class CardPageState(
    val filters: CardFilters = CardFilters(),
    val cards: List<Card> = emptyList(),
    val page: Int = 1,
    val pageCount: Int = 0,
    val totalCount: Int = 0,
    val isLoadingFirstPage: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: UiText? = null,
    val contentVersion: Long = 0L,
) {
    val hasMore: Boolean get() = page < pageCount
    val activeFilterCount: Int get() = filters.activeFilterCount

    /** True while the very first page is in flight and nothing is on screen yet. */
    val isInitialLoad: Boolean get() = isLoadingFirstPage && cards.isEmpty()
    val canLoadMore: Boolean get() = hasMore && !isLoadingMore && !isLoadingFirstPage

    fun onLoadStarted(replace: Boolean): CardPageState =
        copy(isLoadingFirstPage = replace, isLoadingMore = !replace, error = null)

    fun onPageLoaded(loaded: Page<Card>, replace: Boolean): CardPageState {
        // Default hero avatars are collectible HERO cards; they are noise in
        // every list we page through, so they never reach the UI.
        val visible = loaded.items.filterNot { it.isDefaultHeroAvatar }
        return copy(
            cards = if (replace) visible else cards + visible,
            page = loaded.pageNumber,
            pageCount = loaded.pageCount,
            totalCount = loaded.totalCount,
            isLoadingFirstPage = false,
            isLoadingMore = false,
            error = null,
            contentVersion = if (replace) contentVersion + 1 else contentVersion,
        )
    }

    fun onLoadFailed(t: Throwable): CardPageState = copy(
        isLoadingFirstPage = false,
        isLoadingMore = false,
        error = t.toUiText(),
    )

    /** Applies a search result, whichever way it went. */
    fun onResult(result: Result<Page<Card>>, replace: Boolean): CardPageState = when (result) {
        is Result.Success -> onPageLoaded(result.data, replace)
        is Result.Error -> onLoadFailed(result.throwable)
    }
}
