package com.lvsmsmch.deckbuilder.presentation.ui.screen.battlegrounds

import com.lvsmsmch.deckbuilder.domain.entities.Card

data class BattlegroundsState(
    val tab: BgTab = BgTab.Minions,
    val tiers: Set<String> = emptySet(),
    val minionTypes: Set<String> = emptySet(),
    val textQuery: String = "",
    val cards: List<Card> = emptyList(),
    val page: Int = 1,
    val pageCount: Int = 0,
    val totalCount: Int = 0,
    val isLoadingFirstPage: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
) {
    val hasMore: Boolean get() = page < pageCount
}

enum class BgTab(val label: String) {
    Minions("Minions"),
    Heroes("Heroes"),
}
