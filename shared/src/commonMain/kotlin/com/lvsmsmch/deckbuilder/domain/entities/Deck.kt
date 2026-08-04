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
    val maxCardCount: Int get() = DeckRules.maxCardCountFor(cards.map { it.card })
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
    val maxCardCount: Int,
    val savedAtMs: Long,
    /** Cards per mana cost, 0..7+; empty when card data is not loaded yet. */
    val manaCurve: List<Int> = emptyList(),
)

data class SavedDeckSource(
    val code: String,
    val name: String,
    val heroCardId: Int?,
    val format: GameFormat,
    val cardIds: List<Int>,
)

val Card.isPrinceRenathal: Boolean
    get() = slug.equals("REV_018", ignoreCase = true) ||
        slug.equals("CORE_REV_018", ignoreCase = true) ||
        id in DeckRules.PRINCE_RENATHAL_DBF_IDS

val Card.isWhizbangDeck: Boolean
    get() = name.equals("Splendiferous Whizbang", ignoreCase = true) ||
        name.equals("Whizbang the Wonderful", ignoreCase = true) ||
        id in DeckRules.WHIZBANG_DBF_IDS

/**
 * Deck-size rules in one place. Both the assembled [Deck] and the saved-deck
 * projection (which only has dbfIds) must agree, otherwise a deck shows a
 * different "x/30" in the list than on its own screen.
 */
object DeckRules {
    const val DEFAULT_MAX_CARDS = 30
    const val RENATHAL_MAX_CARDS = 40
    const val WHIZBANG_MAX_CARDS = 1

    val PRINCE_RENATHAL_DBF_IDS = setOf(79767, 111689)
    val WHIZBANG_DBF_IDS = setOf(50477, 104819)

    fun maxCardCountFor(cards: List<Card>): Int = when {
        cards.any { it.isWhizbangDeck } -> WHIZBANG_MAX_CARDS
        cards.any { it.isPrinceRenathal } -> RENATHAL_MAX_CARDS
        else -> DEFAULT_MAX_CARDS
    }

    fun maxCardCountForDbfIds(dbfIds: Collection<Int>): Int = when {
        dbfIds.any { it in WHIZBANG_DBF_IDS } -> WHIZBANG_MAX_CARDS
        dbfIds.any { it in PRINCE_RENATHAL_DBF_IDS } -> RENATHAL_MAX_CARDS
        else -> DEFAULT_MAX_CARDS
    }
}

/**
 * Default hero avatars (Malfurion, Jaina, …) are collectible `HERO` cards, so
 * plain searches return them; they are noise in the library and the pool.
 * Real hero cards (Bloodreaver Gul'dan etc.) carry card text and stay.
 */
val Card.isDefaultHeroAvatar: Boolean
    get() {
        if (!cardType.slug.equals("hero", ignoreCase = true)) return false
        if (text?.isNotBlank() == true) return false
        return CanonicalHeroIdRegex.matches(slug)
    }

private val CanonicalHeroIdRegex = Regex("""^HERO_\d+[a-z]*${'$'}""")

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
