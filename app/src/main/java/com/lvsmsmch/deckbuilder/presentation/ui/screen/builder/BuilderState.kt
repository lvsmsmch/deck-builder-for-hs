package com.lvsmsmch.deckbuilder.presentation.ui.screen.builder

import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.domain.entities.CardSort
import com.lvsmsmch.deckbuilder.domain.entities.ClassMeta
import com.lvsmsmch.deckbuilder.domain.entities.DeckCardEntry
import com.lvsmsmch.deckbuilder.domain.entities.GameFormat
import com.lvsmsmch.deckbuilder.domain.entities.SortDir
import com.lvsmsmch.deckbuilder.domain.entities.SortKey

data class BuilderState(
    val phase: Phase = Phase.ClassPicker,
    val chosenClass: ClassMeta? = null,
    val heroCardId: Int? = null,
    val format: GameFormat = GameFormat.STANDARD,
    val deck: Map<Int, DeckCardEntry> = emptyMap(),
    val pool: PoolState = PoolState(),
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val toast: String? = null,
    val maxDeckSize: Int = 30,
    val singleton: Boolean = false,
) {
    val cardCount: Int get() = deck.values.sumOf { it.count }
    val canSave: Boolean get() = cardCount > 0 && chosenClass != null && !isSaving
    val deckEntries: List<DeckCardEntry>
        get() = deck.values.sortedWith(compareBy({ it.card.manaCost }, { it.card.name }))
}

enum class Phase { ClassPicker, Editing }

data class PoolState(
    val cards: List<Card> = emptyList(),
    val page: Int = 1,
    val pageCount: Int = 0,
    val totalCount: Int = 0,
    val textQuery: String = "",
    val sort: CardSort = CardSort(SortKey.MANA_COST, SortDir.ASC),
    val manaCosts: Set<Int> = emptySet(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
) {
    val hasMore: Boolean get() = page < pageCount
    val activeFilterCount: Int get() = if (manaCosts.isEmpty()) 0 else 1
}

sealed interface BuilderEffect {
    data class DeckSaved(val code: String) : BuilderEffect
}
