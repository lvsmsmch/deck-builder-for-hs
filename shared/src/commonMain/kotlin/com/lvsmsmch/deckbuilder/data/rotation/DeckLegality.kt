package com.lvsmsmch.deckbuilder.data.rotation

import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.domain.entities.Deck
import com.lvsmsmch.deckbuilder.domain.entities.DeckCardEntry
import com.lvsmsmch.deckbuilder.domain.entities.StandardRotation

/**
 * Lazy Standard-legality check. UI uses this to flag previously-saved Standard
 * decks whose cards have rotated to Wild.
 *
 * Comparison happens on the raw HsJson set token (`BLACK_TEMPLE`). The domain
 * `Expansion.slug` is the lowercase-dash form (`black-temple`); we re-uppercase
 * to match how rotation snapshots are stored.
 */
fun isStandardLegal(deck: Deck, rotation: StandardRotation): Boolean =
    deck.cards.all { entry -> isStandardLegal(entry.card, rotation) }

fun isStandardLegal(card: Card, rotation: StandardRotation): Boolean {
    val slug = card.cardSet?.slug ?: return false
    return slug.toRotationToken() in rotation.standardSets
}

/** Cards in a deck whose set rotated to Wild. */
fun rotatedOut(deck: Deck, rotation: StandardRotation): List<DeckCardEntry> =
    deck.cards.filterNot { isStandardLegal(it.card, rotation) }

internal fun String.toRotationToken(): String = uppercase().replace('-', '_')
