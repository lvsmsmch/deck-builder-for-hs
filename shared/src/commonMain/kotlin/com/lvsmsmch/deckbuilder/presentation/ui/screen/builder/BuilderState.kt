package com.lvsmsmch.deckbuilder.presentation.ui.screen.builder

import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.domain.entities.ClassMeta
import com.lvsmsmch.deckbuilder.domain.entities.DeckCardEntry
import com.lvsmsmch.deckbuilder.domain.entities.GameFormat
import com.lvsmsmch.deckbuilder.presentation.UiText
import com.lvsmsmch.deckbuilder.presentation.paging.CardPageState
import com.lvsmsmch.deckbuilder.domain.entities.DeckRules

data class BuilderState(
    val phase: Phase = Phase.ClassPicker,
    val chosenClass: ClassMeta? = null,
    val deckName: String? = null,
    val heroCardId: Int? = null,
    val format: GameFormat = GameFormat.STANDARD,
    val deck: Map<Int, DeckCardEntry> = emptyMap(),
    val pool: CardPageState = CardPageState(),
    val isSaving: Boolean = false,
    val saveError: UiText? = null,
    val toast: UiText? = null,
    val singleton: Boolean = false,
    val skipExitConfirm: Boolean = false,
    val skipIncompleteSaveConfirm: Boolean = false,
) {
    val cardCount: Int get() = deck.values.sumOf { it.count }
    val maxDeckSize: Int get() = DeckRules.maxCardCountFor(deck.values.map { it.card })
    val canSave: Boolean get() = cardCount > 0 && chosenClass != null && !isSaving
    val deckEntries: List<DeckCardEntry>
        get() = deck.values.sortedWith(compareBy({ it.card.manaCost }, { it.card.name }))
}

enum class Phase { ClassPicker, Loading, Editing }



sealed interface BuilderEffect {
    data class DeckSaved(val code: String) : BuilderEffect
}
