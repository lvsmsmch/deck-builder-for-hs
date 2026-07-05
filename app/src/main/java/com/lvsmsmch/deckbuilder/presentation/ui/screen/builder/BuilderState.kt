package com.lvsmsmch.deckbuilder.presentation.ui.screen.builder

import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.domain.entities.CardFilters
import com.lvsmsmch.deckbuilder.domain.entities.CardClassScope
import com.lvsmsmch.deckbuilder.domain.entities.CardSort
import com.lvsmsmch.deckbuilder.domain.entities.ClassMeta
import com.lvsmsmch.deckbuilder.domain.entities.DeckCardEntry
import com.lvsmsmch.deckbuilder.domain.entities.GameFormat
import com.lvsmsmch.deckbuilder.domain.entities.isPrinceRenathal
import com.lvsmsmch.deckbuilder.domain.entities.isWhizbangDeck
import com.lvsmsmch.deckbuilder.domain.entities.SortDir
import com.lvsmsmch.deckbuilder.domain.entities.SortKey

data class BuilderState(
    val phase: Phase = Phase.ClassPicker,
    val chosenClass: ClassMeta? = null,
    val deckName: String? = null,
    val heroCardId: Int? = null,
    val format: GameFormat = GameFormat.STANDARD,
    val deck: Map<Int, DeckCardEntry> = emptyMap(),
    val pool: PoolState = PoolState(),
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val toast: String? = null,
    val singleton: Boolean = false,
) {
    val cardCount: Int get() = deck.values.sumOf { it.count }
    val maxDeckSize: Int
        get() = when {
            deck.values.any { it.card.isWhizbangDeck } -> 1
            deck.values.any { it.card.isPrinceRenathal } -> 40
            else -> 30
        }
    val canSave: Boolean get() = cardCount > 0 && chosenClass != null && !isSaving
    val deckEntries: List<DeckCardEntry>
        get() = deck.values.sortedWith(compareBy({ it.card.manaCost }, { it.card.name }))
}

enum class Phase { ClassPicker, Loading, Editing }

data class PoolState(
    val cards: List<Card> = emptyList(),
    val page: Int = 1,
    val pageCount: Int = 0,
    val totalCount: Int = 0,
    val filters: CardFilters = CardFilters(sort = CardSort(SortKey.MANA_COST, SortDir.ASC)),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val contentVersion: Long = 0L,
) {
    val hasMore: Boolean get() = page < pageCount
    val activeFilterCount: Int get() = filters.activeFilterCount()
}

internal fun CardFilters.activeFilterCount(): Int {
    var n = 0
    if (classes.isNotEmpty()) n++
    if (classScope != CardClassScope.ALL) n++
    if (sets.isNotEmpty()) n++
    if (format != com.lvsmsmch.deckbuilder.domain.entities.CardFormatFilter.ALL) n++
    if (rarities.isNotEmpty()) n++
    if (types.isNotEmpty()) n++
    if (minionTypes.isNotEmpty()) n++
    if (keywords.isNotEmpty()) n++
    if (spellSchools.isNotEmpty()) n++
    if (manaCosts.isNotEmpty()) n++
    if (!collectibleOnly) n++
    if (textQuery.isNotBlank()) n++
    return n
}

sealed interface BuilderEffect {
    data class DeckSaved(val code: String) : BuilderEffect
}
