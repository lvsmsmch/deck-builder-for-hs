package com.lvsmsmch.deckbuilder.presentation.ui.screen.cardbacks

import com.lvsmsmch.deckbuilder.domain.entities.CardBack

data class CardBacksState(
    val category: String? = null,
    val textQuery: String = "",
    val items: List<CardBack> = emptyList(),
    val page: Int = 1,
    val pageCount: Int = 0,
    val totalCount: Int = 0,
    val isLoadingFirstPage: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val selected: CardBack? = null,
) {
    val hasMore: Boolean get() = page < pageCount
}
