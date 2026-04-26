package com.lvsmsmch.deckbuilder.presentation.ui.screen.library

import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.domain.entities.CardFilters
import com.lvsmsmch.deckbuilder.domain.entities.Metadata

data class CardLibraryState(
    val filters: CardFilters = CardFilters(),
    val cards: List<Card> = emptyList(),
    val page: Int = 1,
    val pageCount: Int = 0,
    val totalCount: Int = 0,
    val isLoadingFirstPage: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val metadata: Metadata? = null,
    val newSetBanner: com.lvsmsmch.deckbuilder.domain.entities.Expansion? = null,
) {
    val hasMore: Boolean get() = page < pageCount
}
