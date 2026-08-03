package com.lvsmsmch.deckbuilder.data.db

/**
 * Sets that exist in HearthstoneJSON data but are never shown as cards in the
 * app, and must not be treated as real releases anywhere else either.
 *
 * - `EXPERT1` / `VANILLA` / `LEGACY` are the historical Classic buckets whose
 *   cards are duplicated by the current Core set.
 * - `PLACEHOLDER_*` is Blizzard's internal parking bucket. `PLACEHOLDER_202204`
 *   for instance holds ~650 collectible cards that were rotated out of Core in
 *   April 2022 (ids like `CORE_AT_003`), each a duplicate of a card that also
 *   exists in its original expansion. python-hearthstone never lists these
 *   buckets in its `CardSet` enum, so counting them as "unknown sets" produced
 *   a rotation warning that could never clear.
 */
internal object HiddenCardSets {

    val TOKENS = listOf("EXPERT1", "VANILLA", "LEGACY")

    const val PLACEHOLDER_PREFIX = "PLACEHOLDER"

    fun isHidden(set: String?): Boolean {
        if (set == null) return false
        return set in TOKENS || set.startsWith(PLACEHOLDER_PREFIX)
    }
}
