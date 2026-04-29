package com.lvsmsmch.deckbuilder.domain.entities

data class Deck(
    val code: String,
    val format: GameFormat,
    val hero: Card?,
    val heroClass: ClassMeta?,
    val cards: List<DeckCardEntry>,
    val sideboardCards: List<DeckSideboard> = emptyList(),
    val invalidCardIds: List<Int> = emptyList(),
) {
    val cardCount: Int get() = cards.sumOf { it.count }
}

data class DeckCardEntry(
    val card: Card,
    val count: Int,
)

data class DeckSideboard(
    val owner: Card,
    val cards: List<DeckCardEntry>,
)

/** Lightweight projection used by the Saved-Decks listing — keeps row rendering cheap. */
data class DeckPreview(
    val code: String,
    val name: String,
    val classSlug: String?,
    val className: String?,
    val heroCardId: Int,
    val heroSlug: String?,
    val format: GameFormat,
    val cardCount: Int,
    val savedAtMs: Long,
)

enum class GameFormat(val apiSlug: String) {
    STANDARD("standard"),
    WILD("wild"),
    CLASSIC("classic"),
    TWIST("twist"),
    UNKNOWN("unknown");

    val displayName: String
        get() = when (this) {
            STANDARD -> "Standard"
            WILD -> "Wild"
            CLASSIC -> "Classic"
            TWIST -> "Twist"
            UNKNOWN -> "—"
        }

    companion object {
        fun fromApi(slug: String?): GameFormat {
            if (slug.isNullOrBlank()) return UNKNOWN
            return entries.firstOrNull { it.apiSlug.equals(slug, ignoreCase = true) } ?: UNKNOWN
        }
    }
}
